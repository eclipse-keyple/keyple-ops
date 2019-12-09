def yaml() {
	return '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: java-builder
    image: eclipsekeyple/java-builder:1
    imagePullPolicy: Always
    command: ["/usr/local/bin/uid_entrypoint"]
    args: ["cat"]
    tty: true
    resources:
      requests:
        cpu: 250m
        memory: 1Gi
      limits:
        cpu: 1
        memory: 2Gi
    volumeMounts:
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
'''
}

return this