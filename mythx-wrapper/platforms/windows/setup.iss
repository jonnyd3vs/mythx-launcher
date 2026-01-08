; MythX Launcher Installer Script

[Setup]
AppName=MythX Launcher
AppVersion=1.0.0
DefaultDirName={pf}\MythXLauncher
DefaultGroupName=MythX Launcher
UninstallDisplayIcon={app}\MythXLauncher.exe
OutputDir=.
OutputBaseFilename=MythXLauncherInstaller
Compression=lzma
SolidCompression=yes
DisableProgramGroupPage=yes
SetupIconFile=icon.ico
AppSupportURL=https://mythxrsps.com/support
AppPublisher=MythX Team
AppPublisherURL=https://mythxrsps.com
VersionInfoVersion=1.0.0.0

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"
Name: "autoStart"; Description: "Start automatically with Windows"; GroupDescription: "Startup options:"

[Files]
Source: "mythx-wrapper.exe"; DestDir: "{app}"; DestName: "MythXLauncher.exe"; Flags: ignoreversion
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
; Optional files (uncomment if needed later)
; Source: "platforms\Windows\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\MythX Launcher"; Filename: "{app}\MythXLauncher.exe"
Name: "{userdesktop}\MythX Launcher"; Filename: "{app}\MythXLauncher.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\MythXLauncher.exe"; Description: "Launch MythX Launcher"; Flags: nowait postinstall skipifsilent

[Registry]
; Write at install
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
    ValueType: string; ValueName: "MythXLauncher"; ValueData: """{app}\MythXLauncher.exe"""; Tasks: autoStart

; Explicitly delete at uninstall
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
    ValueName: "MythXLauncher"; Flags: uninsdeletevalue

[UninstallDelete]
Type: filesandordirs; Name: "{app}"
