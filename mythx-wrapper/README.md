[//]: # (# SpawnPvP Installer)

[//]: # ()
[//]: # (The **SpawnPvP Wrapper** is a lightweight Go-based launcher and update manager for a SpawnPvP client. It provides:)

[//]: # ()
[//]: # (- ✅ JRE management &#40;download and extraction&#41;)

[//]: # (- ✅ Automatic launcher JAR update via remote version API)

[//]: # (- ✅ Launcher execution via bundled Java)

[//]: # (- ✅ Auto-start registration on Windows &#40;via registry&#41;)

[//]: # (- ✅ Hidden console window for end-user experience)

[//]: # (- ✅ Daily rotating logs for diagnostics)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## ⚙️ Features)

[//]: # ()
[//]: # (- **Silent JRE bootstrap** if Java is missing or incorrect &#40;e.g., Java 1.8.0_352&#41;)

[//]: # (- **Remote launcher version check** against API defined in `.env`)

[//]: # (- **Automatic jar download** if new version is available)

[//]: # (- **Auto-start on Windows logon** via Registry &#40;`HKCU\Software\Microsoft\Windows\CurrentVersion\Run`&#41;)

[//]: # (- **Runs user GUI without flashing terminal windows**)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## 🚀 Build Instructions &#40;Windows&#41;)

[//]: # ()
[//]: # (Make sure you are on a Windows machine.)

[//]: # (The resulting executable will be saved to: wrapper/platforms/Windows/RealmLauncher.exe)

[//]: # ()
[//]: # (### 1. Move into wrapper module)

[//]: # (```bash)

[//]: # (# change directory to wrapper)

[//]: # (cd wrapper)

[//]: # (```)

[//]: # ()
[//]: # (### 2. Optional: Embed icon into Windows executable. You can assign a custom icon to the generated `.exe` file.)

[//]: # (### Prerequisites)

[//]: # (Make sure `$GOPATH/bin` is in your `PATH`.)

[//]: # (Install `rsrc` if you haven't already:)

[//]: # (- Go tool `rsrc` installed)

[//]: # (```bash)

[//]: # (go install github.com/akavel/rsrc@latest)

[//]: # (```)

[//]: # ()
[//]: # (- A valid `.ico` file in the directory, named `resources/icon.ico`)

[//]: # (```bash)

[//]: # (# creates icon for RealmLauncher.exe)

[//]: # (rsrc -ico resources/icon.ico -o rsrc.syso)

[//]: # (```)

[//]: # (✅ Your executable will now include the icon.)

[//]: # (> 💡 Note: `rsrc.syso` will be automatically picked up by Go during build if it’s in the same directory as `main.go`.)

[//]: # ()
[//]: # ()
[//]: # (### 3. Build Windows executable &#40;GUI mode, no console&#41;)

[//]: # ()
[//]: # (```bash)

[//]: # (# creates RealmLauncher.exe with hidden terminal for windows platform)

[//]: # (go build -ldflags="-s -w -H windowsgui" -o platforms/Windows/RealmLauncher.exe)

[//]: # (```)

[//]: # ()
[//]: # ()
[//]: # (### 4. Open RealmLauncher.iss in Inno Setup and compile &#40;F9&#41;)

[//]: # (### Output: platforms/Windows/RealmLauncherInstaller.exe)

[//]: # ()
[//]: # (---)


## 🍎 Build Instructions (macOS Silicon / Apple ARM)

Ensure you are on a macOS device with Apple Silicon (M1/M2/M3).

The resulting `.app` bundle will be saved to: wrapper/platforms/Mac/arm/RealmLauncher.app

### 🛠️ Create Application steps
```bash
# Change to wrapper directory
cd spawnpvp-wrapper

# Ensure the script is executable
chmod +x scripts/build_mac_arm_app.sh

# Run the build script
./scripts/build_mac_arm_app.sh
```

### Create .dmg package
```bash

#### 📦 Inside the Build Script

The script performs the following tasks:

✅ Compiles an ARM64 macOS binary
✅ Wraps it in a proper .app bundle with Info.plist
✅ Assigns an icon (.icns) if available
✅ Sets the bundle name to RealmLauncher
#### 🔒 Note: macOS requires apps to be signed and notarized for Gatekeeper compatibility.
This app is currently unsigned by default.
For broader distribution (outside your own system), consider:
Signing the .app with a Developer ID Application certificate
Notarizing it via Xcode command-line tools (xcrun altool, notarytool)

### 🧩 How to Launch Unsigned App on macOS

If macOS blocks the app:

1. Right-click `RealmLauncher.app` → Click **Open**
2. Click **Open** again in the Gatekeeper warning
3. The app will now be allowed to run
