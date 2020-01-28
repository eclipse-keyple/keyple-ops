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

java release process
------------
Release process
Artifacts : keyple-java-core, keyple-java-calypso, keyple-java-plugin-stub, keyple-java-plugin-pcsc, keyple-java-plugin-remotese, keyple-android-plugin-nfc, keyple-android-plugin-omapi 

create and merge a branch release_X.Y.Z into develop:
make keyple-java-example, android examples apk point to version "X.Y.Z"
edit readme.md
edit release note
set release flag to true
tag source code with tag : "vX.Y.Z"
merge develop into master
generate artifacts with release version "X.Y.Z" with jenkins pipeline
upload them to oss.sonatype.org staging repository with jenkins pipeline. Promote artifacts as release.
upload them to download.eclipse.com with jenkins pipeline.
upload them to github release manually.
create and merge a branch init_new_version into develop:
increment artifacts version
make keyple-java-example point to "+"
set release flag to false

