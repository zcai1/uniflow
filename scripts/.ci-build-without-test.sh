#!/bin/bash

echo Entering "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")" in `pwd`

# Fail the whole script if any command fails
set -e

export SHELLOPTS
#
##if [ "$(uname)" == "Darwin" ] ; then
##  export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home)}
##else
##  export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}
##fi
#
#if [ -d "/tmp/plume-scripts" ] ; then
#  git -C /tmp/plume-scripts pull -q
#else
#  git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
#fi
#
#export AFU="${AFU:-../annotation-tools/annotation-file-utilities}"
## Don't use `AT=${AFU}/..` which causes a git failure.
#AT=$(dirname "${AFU}")
#
### Build annotation-tools (Annotation File Utilities)
#/tmp/plume-scripts/git-clone-related eisop annotation-tools "${AT}"
#if [ ! -d ../annotation-tools ] ; then
# ln -s "${AT}" ../annotation-tools
#fi
#
#echo "Running:  (cd ${AT} && ./.build-without-test.sh)"
#(cd "${AT}" && ./.build-without-test.sh)
#echo "... done: (cd ${AT} && ./.build-without-test.sh)"
