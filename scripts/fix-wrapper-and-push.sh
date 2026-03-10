#!/usr/bin/env bash
set -e
GRADLE_VER="8.1.1"
ZIPFILE="gradle-${GRADLE_VER}-bin.zip"
DIST_URL="https://services.gradle.org/distributions/${ZIPFILE}"
WRAPPER_DIR="./gradle/wrapper"

echo "[+] ensure wrapper dir"
mkdir -p "${WRAPPER_DIR}"

# write gradle-wrapper.properties
cat > "${WRAPPER_DIR}/gradle-wrapper.properties" <<EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/${ZIPFILE}
EOF

# download zip and extract wrapper jar
TMPZIP="/tmp/${ZIPFILE}"
if [ ! -f "${WRAPPER_DIR}/gradle-wrapper.jar" ]; then
  echo "[+] downloading ${DIST_URL} ..."
  curl -L -o "${TMPZIP}" "${DIST_URL}"
  mkdir -p /tmp/gradledist
  unzip -q "${TMPZIP}" -d /tmp/gradledist
  JARPATH=$(find /tmp/gradledist -type f -name gradle-wrapper.jar | head -n1)
  if [ -z "${JARPATH}" ]; then
    echo "gradle-wrapper.jar not found"
    exit 2
  fi
  cp "${JARPATH}" "${WRAPPER_DIR}/gradle-wrapper.jar"
  echo "[+] copied gradle-wrapper.jar"
fi

# make gradlew executable if exists
if [ -f "./gradlew" ]; then chmod +x ./gradlew; fi

# commit & push
git add gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar || true
git commit -m "Add Gradle wrapper (auto)" || true
git push || true

echo "[+] done. You can now run: ./gradlew assembleDebug"
