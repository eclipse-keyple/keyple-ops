Keyple OPS
==========
This is the repository for the Ops settings of the '[Eclipse Keyple](https://keyple.org/)' project.


java-builder
------------
This is a Docker container used to build all Keyple java-based modules.

To build locally, use:
`docker build -t eclipsekeyple/java-builder:1 .`

To simulate Eclipse's usage when using Jenkins containers, use this command:
`docker run -it --rm -u $((1000100000 + RANDOM % 100000)):0 eclipsekeyple/java-builder:1 bash`

Release process java
-----------

## Create a patch branch

Artifacts : keyple-java-core, keyple-java-calypso, keyple-java-plugin-stub, keyple-java-plugin-pcsc, keyple-java-plugin-remotese, keyple-android-plugin-nfc, keyple-android-plugin-omapi

* create branch:**release_X.Y.Z+1** from branch:**release_X.Y.Z** with :
* * Change all artifacts version to X.Y.Z+1
* * make keyple-java-example, android examples apk point to version "X.Y.Z+1"
* * edit readme.md
* * edit release note
* * set release flag to false

Add code fix then follow Release process


## Release process

Artifacts : keyple-java-core, keyple-java-calypso, keyple-java-plugin-stub, keyple-java-plugin-pcsc, keyple-java-plugin-remotese, keyple-android-plugin-nfc, keyple-android-plugin-omapi

* create branch:**release_X.Y.Z** from branch:**develop** with :
* * make keyple-java-example, android examples apk point to version "X.Y.Z"
* * edit readme.md
* * edit release note
* * set release flag to true
* tag source code with tag : **"vX.Y.Z"** and push to repo
* merge branch:**release_X.Y.Z** into branch:**master**
* generate artifacts with release version "X.Y.Z" with jenkins pipeline
* upload artifacts to oss.sonatype.org staging repository with jenkins pipeline. Promote artifacts as release in sonatype to publish them into Maven Central
* upload artifacts to download.eclipse.com/releases with jenkins pipeline.
* upload artifacts to github release manually.
* create branch:**init_new_version** from branch:**master**  with:
* * increment artifacts version
* * make keyple-java-example point to "+"
* * set release flag to false
* * add a new section for **new_version** in release note
* merge branch:**init_new_version** into branch:**develop** 
