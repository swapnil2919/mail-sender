# CI / Workflow Guide

This folder holds the **GitHub Actions** workflow that builds your APK in the
cloud — no Android Studio or local SDK needed.

- **File:** [`build-apk.yml`](build-apk.yml)
- **What it does:** on every push (and on manual trigger), it builds a debug APK
  and uploads it as a downloadable artifact.

---

## How the build flow works

```
git push  →  GitHub Actions runs build-apk.yml  →  APK uploaded as artifact  →  you download & install
```

1. Push code to GitHub (branch `main` or `master`).
2. Go to the repo's **Actions** tab → watch "Build APK" run (~3–5 min).
3. Open the finished run → **Artifacts** → download `app-debug-apk`.
4. Unzip → `app-debug.apk` → copy to phone → install.

> You can also start a build manually: **Actions → Build APK → Run workflow**
> (enabled by the `workflow_dispatch:` line).

---

## What each part of `build-apk.yml` means

| Section | Purpose |
|---|---|
| `on: push` / `workflow_dispatch` | When the build runs (on push + manual button) |
| `runs-on: ubuntu-latest` | Builds on a free Linux cloud machine |
| `actions/checkout` | Downloads your code into the runner |
| `actions/setup-java` | Installs the JDK (Java) needed to build |
| `android-actions/setup-android` | Installs the Android SDK |
| `gradle/actions/setup-gradle` | Installs Gradle (the build tool) |
| `gradle assembleDebug` | The actual build command → produces the APK |
| `actions/upload-artifact` | Makes the APK downloadable from the run |

> Note: this project has **no `gradlew` wrapper** committed. That's fine — the
> workflow installs Gradle directly via `gradle-version`, so the build works
> without it.

---

## When to change this workflow file

The workflow needs updating **less often** than the app code. Touch it only in
these cases:

| Line | Change it when... | Change to |
|---|---|---|
| `java-version: '17'` | A newer Android Gradle Plugin requires newer Java | e.g. `'21'` |
| `gradle-version: '8.7'` | You bump the Android Gradle Plugin in `build.gradle.kts` | a matching newer Gradle |
| `actions/checkout@v4` | GitHub deprecates the action version | `@v5`, etc. |
| `actions/setup-java@v4` | GitHub deprecates the action version | newer version |
| `android-actions/setup-android@v3` | GitHub deprecates the action version | newer version |
| `gradle/actions/setup-gradle@v3` | GitHub deprecates the action version | newer version |
| `actions/upload-artifact@v4` | GitHub deprecates the action version | newer version |

### The key relationship 🔗
```
app/build.gradle.kts  ←→  build-apk.yml
  (Gradle plugin)          (gradle-version + java-version)
```
When you bump the **Android Gradle Plugin** in `build.gradle.kts`, it may require
a **newer Gradle** and/or **newer Java** — update `gradle-version` /
`java-version` here to match. The build log tells you if there's a mismatch
(e.g. *"Gradle X requires Java Y"*), so you don't have to guess.

### What you DON'T change here
- **Android SDK version numbers** (`compileSdk` / `targetSdk`) live only in
  `app/build.gradle.kts`. The workflow installs whatever the SDK needs
  automatically. ✅

---

## Building a RELEASE (signed) APK later — optional

The current workflow builds a **debug** APK (fine for personal use). If you ever
want a signed **release** APK:

1. Generate a keystore (one-time).
2. Add the keystore + passwords as **GitHub repository secrets**.
3. Add a `assembleRelease` step that signs using those secrets.

Ask for help when you reach that point — it's a small addition.

---

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Push rejected: *"without `workflow` scope"* | Your token lacks `workflow` scope → regenerate token with `repo` + `workflow` scopes |
| Build fails: *"Gradle X requires Java Y"* | Bump `java-version` in this file |
| Build fails: SDK/license errors | Usually transient — re-run; `setup-android` accepts licenses automatically |
| No APK in artifacts | Check the build step actually succeeded (green ✓) before the upload step |

---

## Related docs
- [`../../README.md`](../../README.md) — app overview & setup
- [`../../MAINTENANCE.md`](../../MAINTENANCE.md) — what to change when Android/Google updates
