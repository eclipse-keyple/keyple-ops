# 'Eclipse Keyple' OPS   

This is the repository for the Ops settings of the '[Eclipse Keyple](https://keyple.org/)' project.

## java-builder

This is a Docker container used to build all Keyple java-based modules.

To build locally, use:
`docker build -t eclipsekeyple/java-builder:2.0 .`

### Usage
To simulate Eclipse's usage when using Jenkins containers, use this command:
`docker run -it --rm -u $((1000100000 + RANDOM % 100000)):0 eclipsekeyple/java-builder:2.0 bash`


## cpp-builder

This is a Docker container used to build all Keyple cpp-based modules.

To build locally, use:
`docker build -t eclipsekeyple/cpp-builder:7.0 .`


### Usage
To simulate Eclipse's usage when using Jenkins containers, use this command:
`docker run -it --rm -u $((1000100000 + RANDOM % 100000)):0 eclipsekeyple/cpp-builder:7.0 bash`


Release process java
--------------------

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
* artifacts are uploaded to oss.sonatype.org staging repository with. Promote artifacts as release in sonatype to publish them into Maven Central
* * locate staging repo at : https://oss.sonatype.org/#stagingRepositories with account : keyple_bot
* * close and release artifacts following instructions : https://central.sonatype.org/pages/releasing-the-deployment.html
* create a github release and upload artifacts manually.
* modify link in download.eclipse.com/releases to point to release in github
* create branch:**init_new_version** from branch:**master**  with:
* * increment artifacts version
* * make keyple-java-example point to "+"
* * set release flag to false
* * add a new section for **new_version** in release note
* merge branch:**init_new_version** into branch:**develop** 

## Contributing

1. [Fork](https://help.github.com/articles/fork-a-repo/) the [eclipse/keyple-ops](https://github.com/eclipse/keyple-ops) repository
2. Clone repository: `git clone https://github.com/[your_github_username]/keyple-ops.git`
3. Create your feature branch: `git checkout -b my-new-feature`
4. Make your changes
5. Commit your changes: `git commit -m "Add some feature" -s`
6. Push feature branch: `git push origin my-new-feature`
7. Submit a pull request

### Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v. 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

SPDX-License-Identifier: EPL-2.0

## Bugs and feature requests

Have a bug or a feature request? Please search for existing and closed issues. If your problem or idea is not addressed yet, [please open a new issue](https://github.com/eclipse/keyple-ops/issues/new).

## Trademarks

* Eclipse Keyple and the Eclipse Keyple project are Trademarks of the Eclipse Foundation, Inc.
* Eclipse® is a Trademark of the Eclipse Foundation, Inc.
* Eclipse Foundation is a Trademark of the Eclipse Foundation, Inc.

## Copyright and license

Copyright 2020 the [Eclipse Foundation, Inc.](https://www.eclipse.org) and the [Keyple OPS authors](https://github.com/eclipse/keyple-ops/graphs/contributors). 
Code released under the [Eclipse Public License Version 2.0 (EPL-2.0)](https://github.com/eclipse/keyple-website/blob/src/LICENSE).
