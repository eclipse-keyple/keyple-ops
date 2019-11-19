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

