---
name: adb-install
description: Build the debug APK and install it on the connected Android device via ADB
disable-model-invocation: true
---

Steps:

1. Run: `rtk ./gradlew assembleDebug`
2. Run: `cmd.exe /c "C:\Users\jason\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk"`
3. Report success or the ADB error output to the user.
