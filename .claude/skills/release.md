---
name: release
description: Cut a new Tichu Guru release — bump versionCode/versionName, build release APK, tag, and publish to GitHub
disable-model-invocation: true
---

Steps for a new release:

1. Ask the user for the new versionCode (integer) and versionName (string, e.g. "2.1") if not provided.
2. Edit `app/build.gradle.kts`: update `versionCode` and `versionName` in the `defaultConfig` block.
3. Run: `rtk ./gradlew assembleRelease`
4. Run: `rtk git add app/build.gradle.kts && rtk git commit -m "release v<versionCode>"`
5. Run: `rtk git tag v<versionCode> && rtk git push && rtk git push --tags`
6. Run: `rtk gh release create v<versionCode> --title "v<versionCode>" --generate-notes app/build/outputs/apk/release/app-release.apk`
7. Report the release URL to the user.
