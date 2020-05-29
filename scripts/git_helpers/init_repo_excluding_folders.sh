#!/bin/bash

# This script initializes an empty git repository (the "target") from an
# already existing repo (the "source") excluding a list a folders from it.
# It removes the commit history associated with the excluded folders in order to 
# retain a clean git history in the target repo.
#
# You should ensure to have a clean local copy of the source repo before
# executing the script.
# Also be sure that the target repository is actually created, remotely
# accessible and empty.
#
# This script takes at least 3 paramters:
#    - Param 1 = the path to your local copy of the source repo
#    - Param 2 = the git url for the target repository
#    - Param 3..n = the folders you want to exclude: using the relative  
#       path from the root of the source repo



if [ "$#" -lt 3 ]; then
    echo "Illegal number of parameters"
    echo "Usage: $0 source_repo_path target_repo_url excluded_folder1 excluded_folder2 ..."
    exit 1
fi

# Create a temporary copy of the source repo, to avoid messing up things
cp -r $1 ___temp
cd ___temp

# Safety net: check that the source and target repos are different
SOURCE=$(git remote get-url origin)
if [ "$SOURCE" = "$2" ]; then
    echo "The source and target repos appears to be the same, this is not a good idea!"
    cd ..
    rm -rf ___temp
    exit 1
fi

# Retrieve all branches
git branch -r | grep -v '\->' | while read remote; do git branch --track "${remote#origin/}" "$remote"; done

# Filter out the excluded folders
for i in "${@:3}"
do
    git filter-branch -f --prune-empty --tree-filter "rm -rf $i" --tag-name-filter cat -- --all
done


# Push everything to the target repo
git remote set-url origin $2
git push --all
git push --tags

# Clean everything before leaving
cd ..
rm -rf ___temp
