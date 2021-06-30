def call(String version) {
  imagePullPolicy = "IfNotPresent"
  if (version == '2.0') version = '2.0.5'
  if (version.endsWith('!')) {
    version = version.substring(0, version.length() - 1);
    imagePullPolicy = "Always"
  }
	return """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: java-builder
    image: eclipsekeyple/java-builder:${version}
    imagePullPolicy: ${imagePullPolicy}
    command: ["/usr/local/bin/uid_entrypoint"]
    args: ["cat"]
    tty: true
    resources:
      requests:
        cpu: 250m
        memory: 1Gi
      limits:
        cpu: 2
        memory: 4Gi
    volumeMounts:
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
"""
}
