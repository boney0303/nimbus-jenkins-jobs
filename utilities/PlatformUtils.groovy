package utilities

// Example helper utilities. Adapt these to return the values relevant to your
// own platform (cluster names, chart paths, etc.).
class PlatformUtils {
  static List getK8sChartsPath(String env) {
    if(env == "dev") {
      return [
        "",
        "master/app/app-1.0*",
        "release/app/app-0.9*"
      ]
    } else {
      return [""]
    }
  }

  static List getEKSClusters(String platform) {
    if(platform == "app") {
      return [
        "cluster-a",
        "cluster-b"
      ]
    } else {
      return []
    }
  }
}
