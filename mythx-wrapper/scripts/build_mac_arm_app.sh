#!/bin/bash
set -euo pipefail  # ✅ safer scripting

# === CONFIG ===
APP_NAME="AscertesWrapper"
APP_ID="com.spawnpvp.ascerteswrapper"
VERSION="1.0.0"
JAR_SOURCE="SpawnPvP-wrapper.jar"
OUTPUT_DIR="platforms/mac/arm"
ICON_SOURCE="src/main/resources/icon.icns"
JRE_SOURCE="$OUTPUT_DIR/jre"  # ✅ relative to OUTPUT_DIR for clarity

# Derived paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

APP_DIR="$PROJECT_ROOT/$OUTPUT_DIR/$APP_NAME.app"
CONTENTS_DIR="$APP_DIR/Contents"
MACOS_DIR="$CONTENTS_DIR/MacOS"
JAVA_DIR="$CONTENTS_DIR/Java"
RESOURCES_DIR="$CONTENTS_DIR/Resources"
PLIST_PATH="$CONTENTS_DIR/Info.plist"
LAUNCHER_SCRIPT="$MACOS_DIR/launcher"

# Clean up existing app dir (optional)
rm -rf "$APP_DIR"

echo "📁 Creating .app directory structure..."
mkdir -p "$MACOS_DIR" "$JAVA_DIR" "$RESOURCES_DIR"

echo "📦 Copying spawnpvp-wrapper.jar..."
cp "$PROJECT_ROOT/$JAR_SOURCE" "$JAVA_DIR/"

if [ -d "$PROJECT_ROOT/$JRE_SOURCE" ]; then
    echo "📦 Copying JRE into app bundle..."
    rsync -a "$PROJECT_ROOT/$JRE_SOURCE/" "$JAVA_DIR/jre/"
else
    echo "❌ JRE directory not found at $PROJECT_ROOT/$JRE_SOURCE"
    exit 1
fi

echo "📝 Creating launcher script..."
cat > "$LAUNCHER_SCRIPT" <<'EOF'
#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME="$DIR/../Java/jre"
JAVA_BIN="$JAVA_HOME/bin/java"
JAR="$DIR/../Java/spawnpvp-wrapper.jar"

exec "$JAVA_BIN" -Xms512m -Xmx1024m -jar "$JAR" "$@"
EOF

chmod +x "$LAUNCHER_SCRIPT"

echo "📝 Creating Info.plist..."
cat > "$PLIST_PATH" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
   "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>$APP_NAME</string>
    <key>CFBundleIdentifier</key>
    <string>$APP_ID</string>
    <key>CFBundleVersion</key>
    <string>$VERSION</string>
    <key>CFBundleExecutable</key>
    <string>launcher</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleIconFile</key>
    <string>icon.icns</string>
</dict>
</plist>
EOF

if [ -f "$PROJECT_ROOT/$ICON_SOURCE" ]; then
    echo "🎨 Copying icon.icns..."
    cp "$PROJECT_ROOT/$ICON_SOURCE" "$RESOURCES_DIR/"
else
    echo "⚠️ icon.icns not found, skipping icon setup."
fi

echo "✅ App bundle created at: $APP_DIR"

# --- Create DMG ---
DMG_DIR="$PROJECT_ROOT/$OUTPUT_DIR/dmg"
STAGING_DIR="$DMG_DIR/staging"
DMG_PATH="$DMG_DIR/${APP_NAME}_${VERSION}.dmg"

echo "📁 Preparing DMG staging folder..."
rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR"

# Copy and rename your .app inside staging as AscertesLauncher.app
cp -R "$APP_DIR" "$STAGING_DIR/AscertesLauncher.app"

# Create symlink to /Applications inside staging folder
ln -s /Applications "$STAGING_DIR/Applications"

echo "📦 Creating .dmg..."
hdiutil create -volname "$APP_NAME" \
  -srcfolder "$STAGING_DIR" \
  -ov -format UDZO "$DMG_PATH"

# Create a stable (version-free) copy for easier distribution
cp -f "$DMG_PATH" "$DMG_DIR/AscertesLauncher.dmg"
rm -f "$DMG_DIR"/AscertesWrapper_*.dmg

echo "✅ .dmg created at: $DMG_PATH"
echo "✅ Stable .dmg available at: $DMG_DIR/AscertesWrapper.dmg"