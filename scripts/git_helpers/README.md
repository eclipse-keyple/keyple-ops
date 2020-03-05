# Git Helper Scripts

Here is a set of BASH scripts which can be used to execute non-trivial git operations.

## Populate an empty repository with an existing folder, keeping the git history: [init_repo_from_folder.sh](init_repo_from_folder.sh)

This script initializes an empty git repository (the "target") with a folder from an already existing repo (the "source"), keeping track of the complete git history (Logs, branches and tags).

You should ensure to have a clean local copy of the source repo before executing the script. Also be sure the target repository is actually created, remotely accessible and empty.

This script takes 3 parameters:

- **Param 1** = *the path to your local copy of the source repo*
- **Param 2** = *the folder you want to extract: as a relative path from the root of the source repo*

- **Param 3** = *the git URL for the target repository*

### Example

Let's say you want to move the [Android](https://github.com/eclipse/keyple-java/tree/master/android) part of the Keyple project out of the [keyple-java](https://github.com/eclipse/keyple-java) repository, so that is managed in its own dedicated repository. It is obviously very important to keep the complete git history linked to the files we are moving in the new repository.

In order to do that:

1. Create an new repository, that will host the Android part of the project. (Target repo URL: *gitRepoUrl/keyple-android.git*)
2. Clone the keyple-java repository, somewhere to your workspace. (Path to the keyple-java project: *path/to/keyple-java*)
3. Run `./init_repo_from_folder.sh path/to/keyple-java android gitRepoUrl/keyple-android.git`

## Populate an empty repository from an existing repo, filtering out folders and cleaning the git history: [init_repo_excluding_folders.sh](init_repo_excluding_folders.sh)

This script initializes an empty git repository (the "target") from an already existing repo (the "source") excluding a list of folders from it. It removes the commit history associated with the excluded folders in order to retain a clean git history in the target repo.

You should ensure to have a clean local copy of the source repo before executing the script. Also be sure that the target repository is actually created, remotely accessible and empty.

This script takes at least 3 parameters:

- **Param 1** = *the path to your local copy of the source repo*
- **Param 2** = *the git url for the target repository*
- **Param 3..n** = *the folders you want to exclude: using the relative path from the root of the source repo*

### Example

Let's say you have used the previous script to move the `android` and `java` folders out of the [keyple-java](https://github.com/eclipse/keyple-java) repository, and you now want to have a new "base" repo, containing only the base gradle stuff and the documentation. Again, it is important have *in fine* a clean git history, consistent with the remaining files.

In order to do that:

1. Create a new "base" repository: *gitRepoUrl/keyple-base.git*
2. Clone the keyple-java repository, somewhere to your workspace: *path/to/keyple-java*
3. Run `./init_repo_excluding_folders.sh path/to/keyple-java gitRepoUrl/keyple-base.git android java`

## Use Case

Starting from the repository [keyple-java](https://github.com/eclipse/keyple-java), we want to reorganize the project and split it in several repositories. The goal would be to have a repo for each component. But we also want to have unique entry point for the project, via a "base" repository, gathering all the components as [git submodules](https://git-scm.com/docs/git-submodule). Of course, we want to keep a clean git history for everything.

In order not to break already existing setups based on the current keyple-java project (such as CI), the idea will be to have a resulting file tree similar to the current one.

We will be moving from:

```
keyple-java
         |______android
         |______docs
         |______gradle
         |______java________component_____keyple-core
                   |                |_____keyple-calypso
                   |                |_____keyple-plugin
                   |________example
```

To:

```she
keyple-java-base
         |______[keyple-android submodule]
         |______docs
         |______gradle
         |______java________component_____[keyple-java-core submodule]
                   |                |_____[keyple-java-calypso submodule]
                   |                |_____[keyple-java-plugin submodule]
                   |________[keyple-java-example submodule]
```

#### 1. Create the needed repositories

- *keyple-java-base*
- *keyple-android*
- *keyple-java-core*
- *keyple-java-calypso*
- *keyple-java-plugin*
- *keyple-java-example*

#### 2. Populate all the repositories...

...using the 2 scripts, as explained previously. 

*init_repo_excluding_folders.sh* would have to be used to populate "keyple-java-base". *init_repo_from_folder.sh* would be used for the five other repositories.

```shell
./init_repo_from_folder.sh path/to/keyple-java android gitRepoUrl/keyple-android.git
./init_repo_from_folder.sh path/to/keyple-java java/component/keyple-core gitRepoUrl/keyple-java-core.git
./init_repo_from_folder.sh path/to/keyple-java java/component/keyple-calypso gitRepoUrl/keyple-java-calypso.git
./init_repo_from_folder.sh path/to/keyple-java java/component/keyple-plugin gitRepoUrl/keyple-java-plugin.git
./init_repo_from_folder.sh path/to/keyple-java java/example gitRepoUrl/keyple-java-example.git

./init_repo_excluding_folders.sh path/to/keyple-java gitRepoUrl/keyple-java-base.git android java/component/keyple-core java/component/keyple-calypso java/component/keyple-plugin java/example
```

#### 3. Link all submodules in the base repository

All the repositories have been correctly created and initialized with the proper source files, keeping a clean git history. The last step is now to link everything consistently in the "keyple-java-base" repo, in order to have a useful unique entry point to the project. It is with this step that we will ensure to maintain the same file tree as in "keyple-java", in order to keep the existing integrations functional.

```shell
git clone gitRepoUrl/keyple-java-base.git
cd path/to/keyple-java-base
git submodule add gitRepoUrl/keyple-android.git android
git submodule add gitRepoUrl/keyple-java-core.git java/component/keyple-core
git submodule add gitRepoUrl/keyple-java-calypso.git java/component/keyple-calypso
git submodule add gitRepoUrl/keyple-java-plugin.git java/component/keyple-plugin
git submodule add gitRepoUrl/keyple-java-example.git java/example
git push
```

And you are all set.

Last thing to keep in mind: All these new repositories can be cloned as you would do for any other repo, but don't forget to use the "--recurse-submodules" option when cloning "keyple-java-base", in order to also fetch the submodules.

`git clone --recurse-submodules gitRepoUrl/keyple-java-base.git`