#!/bin/bash
# Setup Android SDK — idempotent, safe to run multiple times

ANDROID_SDK="/home/runner/android-sdk"
CMDLINE_TOOLS="${ANDROID_SDK}/cmdline-tools/latest"
SDKMANAGER="${CMDLINE_TOOLS}/bin/sdkmanager"

if [ -d "${ANDROID_SDK}/platforms/android-34" ]; then
    echo "[sdk] Android SDK already set up ✓"
    echo "sdk.dir=${ANDROID_SDK}" > local.properties
    exit 0
fi

echo "[sdk] Setting up Android SDK (one-time, ~300MB)..."

mkdir -p "${ANDROID_SDK}/cmdline-tools"

TOOLS_ZIP="/tmp/cmdtools.zip"
wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
     -O "${TOOLS_ZIP}"
unzip -q "${TOOLS_ZIP}" -d /tmp/cmdtools_extract
mkdir -p "${CMDLINE_TOOLS}"
mv /tmp/cmdtools_extract/cmdline-tools/* "${CMDLINE_TOOLS}/"
rm -rf /tmp/cmdtools_extract "${TOOLS_ZIP}"

export PATH="${CMDLINE_TOOLS}/bin:${PATH}"
export ANDROID_SDK_ROOT="${ANDROID_SDK}"

echo "[sdk] Accepting licenses..."
yes | "${SDKMANAGER}" --sdk_root="${ANDROID_SDK}" --licenses > /dev/null 2>&1

echo "[sdk] Installing platform android-34 and build-tools..."
"${SDKMANAGER}" --sdk_root="${ANDROID_SDK}" \
    "platforms;android-34" \
    "build-tools;34.0.0"

echo "sdk.dir=${ANDROID_SDK}" > local.properties
echo "[sdk] Android SDK setup complete ✓"
