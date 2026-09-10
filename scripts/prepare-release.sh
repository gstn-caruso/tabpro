#!/usr/bin/env bash
# Invoked by @semantic-release/exec (prepareCmd) with the new version already
# decided by commit-analyzer. Leaves the working tree with:
#   - all poms (root and modules) at that version
#   - version mentions in README updated
#   - the .deb already built with that name
set -euo pipefail

VERSION="$1"

mvn -B org.codehaus.mojo:versions-maven-plugin:2.18.0:set \
  -DnewVersion="${VERSION}" \
  -DprocessAllModules=true \
  -DgenerateBackupPoms=false

sed -i \
  -e "s/tabpro_[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*_all\.deb/tabpro_${VERSION}_all.deb/g" \
  -e "s/tabpro-app-[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\.jar/tabpro-app-${VERSION}.jar/g" \
  README.md

mvn -B -pl tabpro-app -am package
