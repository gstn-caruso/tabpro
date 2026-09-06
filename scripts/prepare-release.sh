#!/usr/bin/env bash
# Invocado por @semantic-release/exec (prepareCmd) con la versión nueva ya
# decidida por el commit-analyzer. Deja el árbol de trabajo con:
#   - todos los poms (raíz + módulos) en esa versión
#   - las menciones de versión del README actualizadas
#   - el .deb ya construido con ese nombre
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
