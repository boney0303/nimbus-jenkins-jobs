#!/bin/bash

bucket=$1
mkdir /tmp/kube_configs && cd /tmp/kube_configs

aws s3 cp --recursive s3://$bucket .

for file in `ls`
do
    kube_files=$file:$kube_files
done

KUBECONFIG=$(echo "$kube_files" | sed -n 's/:$//p')

kubectl config view --flatten > ~/.kube/eks_kubeconfig

rm -rf /tmp/kube_configs; echo

echo "New kubeconfig file: ~/.kube/eks_kubeconfig"
echo "###############################################################"
echo "###############################################################"; echo
echo "Please execute: export KUBECONFIG=~/.kube/eks_kubeconfig"; echo
echo "###############################################################"
echo "###############################################################"; echo
