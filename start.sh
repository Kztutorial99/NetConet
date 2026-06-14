#!/bin/bash
set -e

echo "========================================"
echo "  NetSpeed Pro — Android Build Server"
echo "========================================"

export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))"
export PATH="${JAVA_HOME}/bin:${PATH}"
echo "[java] Using JAVA_HOME=${JAVA_HOME}"

# Step 1: Setup Android SDK (skips if already done)
bash setup_sdk.sh

# Step 2: Make gradlew executable and build
chmod +x gradlew

echo ""
echo "[build] Starting Gradle build (first run downloads dependencies ~150MB)..."
./gradlew assembleDebug --no-daemon --stacktrace 2>&1

# Step 3: Check APK was produced
APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
APK_DST="public/NetSpeedPro-debug.apk"

mkdir -p public

if [ -f "${APK_SRC}" ]; then
    cp "${APK_SRC}" "${APK_DST}"
    SIZE=$(du -sh "${APK_DST}" | cut -f1)
    echo ""
    echo "✅ APK built successfully! Size: ${SIZE}"
else
    echo ""
    echo "⚠️  APK not found. Check build logs above."
    echo '{"status":"build_failed"}' > public/status.json
fi

# Step 4: Serve download page
echo "[server] Starting download server on port 5000..."
python3 serve_apk.py
