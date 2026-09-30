# nimbus-jenkins-jobs

**Jenkins jobs as code, using the [Job DSL](https://plugins.jenkins.io/job-dsl/) plugin.**

This repository holds declarative definitions of every Jenkins job, view, and
managed config file. A single "seed" pipeline reads these definitions and
generates the entire Jenkins job catalog automatically — so your Jenkins
controller can be rebuilt from scratch, and every job change is reviewed in Git
rather than clicked together in the UI.

It is one of three repositories that together manage Jenkins entirely as code:

| Repository | Layer | Responsibility |
|------------|-------|----------------|
| [`nimbus-jenkins`](../nimbus-jenkins) | Provisioning (Chef) | Builds the Jenkins host, installs plugins, registers the shared library and the seed job |
| **`nimbus-jenkins-jobs`** (this repo) | Jobs (Job DSL) | Declarative job/view/config definitions + the seed pipeline that creates them |
| [`nimbus-jenkins-shared-libraries`](../nimbus-jenkins-shared-libraries) | Library (Groovy) | Reusable pipeline steps and classes consumed by every generated job |

> This is a **generic template**. Every organisation-specific value has been
> replaced with a named placeholder (see [Placeholder convention](#placeholder-convention)).
> Fork it, search-and-replace the placeholders, and point the seed job at your fork.

---

## Use case

You want your Jenkins jobs to be:

- **Reproducible** – rebuild the whole controller's jobs by running one seed job.
- **Reviewable** – job definitions live in Git and go through pull requests.
- **Environment-aware** – the same repo drives `dev`, `staging`, and `prod`
  controllers, each getting only the jobs and config appropriate to it.
- **Consistent** – jobs share permissions, views, and pipeline logic instead of
  each being configured by hand.

---

## How it works

```
                    ┌──────────────────────────────────────────┐
   run seed job ──▶ │  seed/master.groovy  (the seed pipeline)  │
                    └──────────────────────────────────────────┘
                                     │
       ┌─────────────────────────────┼──────────────────────────────┐
       ▼                             ▼                              ▼
 reads envs/<env>/jobs.txt    walks jobdefinitions/          applies configs/<env>/
 (which jobs on this host)    (runs Job DSL per file)        (managed config files)
```

**One-time setup:** create a Pipeline job in Jenkins manually and point it at
`seed/master.groovy`. (In this stack, the `nimbus-jenkins` cookbook registers that
seed job for you.)

**Every run**, the seed pipeline executes these stages:

| Stage | What it does |
|-------|--------------|
| **Setup Environment** | Checks out this repo plus the shared library (`nimbus-jenkins-shared-libraries`) into `saaslib/` so job definitions can import `com.nimbus.*` classes. |
| **Import data from job files** | Reads `envs/<environment>/jobs.txt` — each line is an [Ant-style glob](https://ant.apache.org/manual/dirtasks.html#patterns) selecting which job definitions apply to this controller. |
| **Setup Views** | Builds categorized dashboard views (tabs) from the top-level folders under `jobdefinitions/`. |
| **Setup Jobs** | Walks `jobdefinitions/`, runs Job DSL for each matched `*.groovy`, injecting per-environment parameters (AWS account, region, git URLs, etc.). |
| **Setup Configs** | Applies `configs/<environment>/<region>/*.groovy` to create Jenkins **managed config files** that pipelines read at runtime. |
| **Setup Access** | Grants authenticated users permission to run the seed job. |

The environment and region are derived automatically from the Jenkins URL by the
shared-library steps `getDeployEnvironment()` / `getDeployRegion()`.

---

## Repository layout

```
seed/
  master.groovy        The seed pipeline — entry point that generates everything.

envs/                  One folder per environment (dev, staging, prod).
  <env>/jobs.txt       Ant-style globs selecting which job definitions install
                       on that environment's controller.

configs/               Per-environment, per-region managed config files.
  <env>/<region>/settings.groovy   Creates the config files pipelines consume
                                    (AWS accounts, Chef server, Slack, etc.).

jobdefinitions/        The job definitions, grouped by area:
  Chef/                  Cookbook + Chef server jobs
  Infra/                 AWS, CloudFormation, Terraform, Splunk, Harness, etc.
  K8S/                   EKS / Kubernetes jobs (ECR, monitoring, ...)
  samples/               Copy-and-adapt templates for new pipeline jobs

pipelines/             Standalone utility pipelines (e.g. enable/disable all jobs).
utilities/             Shared Groovy helpers (PlatformUtils).
s3_download_kubeconfig.sh   Helper script used by some K8S jobs.
```

### How a job definition looks

Each `jobdefinitions/**/**.groovy` file uses Job DSL to declare one or more jobs.
Definitions typically:

1. `import com.nimbus.JobPermissions` (from the shared library) to set per-team access.
2. Declare a `pipelineJob` / `multibranchPipelineJob`.
3. Point the pipeline at a script that calls a shared-library step
   (e.g. `terraformPipeline`, `helmDeployPipeline`).

See `jobdefinitions/samples/` for annotated starting points.

---

## Placeholder convention

Everything in this repo uses named placeholders so you can tell at a glance what
each value represents. **The same convention is used across all three repos.**

| Placeholder | What it represents | Example use |
|-------------|--------------------|-------------|
| `nimbus` | Your company / organisation | Slack `teamDomain` (`nimbus`), `com.nimbus.*` package |
| `skyline` | A Git project / repository namespace | `def project = 'skyline'`, Chef org |
| `saaseng` | The team that owns shared infrastructure | shared-library repo namespace, credential IDs |
| `skyline-dev` | An AWS account, named `<project>-<environment>` | `AWSAccountName` in configs |
| `ops`, `dev`, `developers`, `cs`, `qa`, `analytics` | Team names used for job permissions | `setPermissions(this, ['ops'], ...)` |
| `dev` / `staging` / `prod` | Deployment environments | folders under `envs/` and `configs/` |
| `git.nimbus.io` | Your Git host | clone URLs |

---

## Adapting this to your organisation

1. **Environments** – add or rename folders under `envs/` and `configs/`
   (ships with `dev`, `staging`, `prod`).
2. **Project name** – job definitions use `def project = 'skyline'`; set this
   to your Git project/repository namespace.
3. **Shared library** – the seed clones
   `saaseng/nimbus-jenkins-shared-libraries.git`; job defs import
   `com.nimbus.JobPermissions`. Point these at your own fork/package.
4. **Team names** – the team list passed to `setPermissions` /
   `setFolderPermissions` (e.g. `['ops', 'dev']`) must match the teams defined in
   the shared library's `com.nimbus.JobPermissions`.
5. **Config values** – edit `configs/**/settings.groovy` (AWS account numbers,
   Chef server, Slack channel, hosted zone IDs, etc.).
6. **Notifications** – the seed posts to Slack channel `#ci-notifications` on the
   `nimbus` workspace; change these to your own.

---

## Security notes

- **Never commit real secrets.** Store credentials in Jenkins and reference them
  by credential ID (as the samples do with `gitCreds`, `JiraCredsId`, etc.).
- The AWS account numbers, hosted zone IDs, and URLs under `configs/` are
  **placeholder values**. Replace them with your own and treat account IDs and
  internal hostnames as sensitive if your fork is public.
