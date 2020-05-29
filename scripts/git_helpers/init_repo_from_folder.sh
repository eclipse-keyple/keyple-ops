#!/bin/bash

# This script initializes an empty git repository (the "target") with a folder from an
# already existing repo (the "source"), keeping track of the complete git history
# (Logs, branches and tags).
#
# You should ensure to have a clean local copy of the source repo before
# executing the script.
# Also be sure that the target repository is actually created, remotely
# accessible and empty.
#
# This script takes 3 paramters:
#    - Param 1 = the path to your local copy of the source repo
#    - Param 2 = the folder you want to extract: as a relative path from the
#       root of the source repo
#    - Param 3 = the git url for the target repository



if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters"
    echo "Usage: $0 source_repo_path target_folder target_repo_url"
    exit 1
fi

# Create a temporary copy of the source repo, to avoid messing up things
cp -r $1 ___temp
cd ___temp

# Safety net: check that the source and target repos are different
SOURCE=$(git remote get-url origin)
if [ "$SOURCE" = "$3" ]; then
    echo "The source and target repos appears to be the same, this is not a good idea!"
    cd ..
    rm -rf ___temp
    exit 1
fi

# Retrieve all branches
git branch -r | grep -v '\->' | while read remote; do git branch --track "${remote#origin/}" "$remote"; done

# Keep only the target folder
git filter-branch --prune-empty --subdirectory-filter $2 --tag-name-filter cat -- --all

# Push everything to the target repo
git remote set-url origin $3
git push --all
git push --tags

# Clean everything before leaving
cd ..
rm -rf ___temp
