# Maintenance Guide — Mail Sender

A cheat sheet for **what to change and where** when Android updates or Google
changes a policy in the future.

> TL;DR: **95% of future updates = bump two numbers in `app/build.gradle.kts` and rebuild.**

---

## Quick reference table

| Scenario | File(s) to change |
|---|---|
| New Android version (normal) | `app/build.gradle.kts` (bump SDK numbers) |
| New Android restrictions (background/service) | `AndroidManifest.xml` + `ForwarderService.kt` |
| Permission flow breaks | `MainActivity.kt` |
| Gmail / email sending breaks | `EmailSender.kt` only |
| Play Store policy on SMS | ❌ irrelevant — this app is sideloaded |
| Stored config (email) needs format change | `Prefs.kt` |

---

## 🟢 1. New Android version released (the normal case)

**File:** `app/build.gradle.kts`

```kotlin
android {
    compileSdk = 34        // ← bump to the new version (e.g. 35, 36)

    defaultConfig {
        targetSdk = 34     // ← bump to match
    }
}
```

Steps:
1. Change `compileSdk` and `targetSdk` to the new number.
2. Bump dependency versions in the `dependencies { }` block if newer ones exist.
3. Rebuild.

Usually it just works — Android keeps backward compatibility.

---

## 🟡 2. New Android version ADDS restrictions (the real work)

Each Android version tends to tighten **background execution**, **permissions**,
and **foreground services**. If the app breaks after a version bump, look here:

| What Android tightened | File to edit | What to change |
|---|---|---|
| New permission required | `AndroidManifest.xml` | Add a new `<uses-permission>` |
| Foreground service rules | `AndroidManifest.xml` + `ForwarderService.kt` | Update `foregroundServiceType`, `startForeground()` call |
| Runtime permission flow | `MainActivity.kt` | Update `requestNeededPermissions()` |
| Notification listener behavior | `NotificationForwardService.kt` | Adjust listener code |

> ⚠️ **Most likely future breakage = the foreground service.**
> That means `ForwarderService.kt` plus the `<service>` and
> `FOREGROUND_SERVICE*` permission lines in `AndroidManifest.xml`.

### Common concrete examples (history repeats)
- **Android 13 (SDK 33):** required `POST_NOTIFICATIONS` runtime permission
  → handled in `MainActivity.kt` + manifest.
- **Android 14 (SDK 34):** required a specific `foregroundServiceType` and a
  matching `FOREGROUND_SERVICE_*` permission → manifest + `ForwarderService.kt`.
- **Future versions:** expect more of the same pattern. Same files.

---

## 🔵 3. Google changes a POLICY (not the OS)

Policy changes usually affect **distribution**, not your code:

| Policy change | Affects | File to change |
|---|---|---|
| Play Store SMS rules | Only if you publish to Play Store | ❌ None — you sideload |
| Gmail SMTP / auth (e.g. app passwords) | How email is sent | `EmailSender.kt` |
| Gmail blocks SMTP entirely | Email method | `EmailSender.kt` (switch to Apps Script / Gmail API) |

> Since this app is **sideloaded** (installed directly, not via Play Store),
> Google's SMS *policy* never affects you. The only thing to watch is
> **Gmail authentication**, which lives entirely in `EmailSender.kt`.

### If Gmail SMTP ever stops working
Replace the SMTP logic in `EmailSender.kt` with an HTTP call to a free
**Google Apps Script** web app that sends the email via `GmailApp.sendEmail`.
That removes the need for an app password. (Ask for help converting it.)

---

## 📂 What each file is responsible for

| File | Responsibility | Touch it when... |
|---|---|---|
| `app/build.gradle.kts` | SDK versions + dependencies | New Android version / updating libraries |
| `AndroidManifest.xml` | Permissions + components | New permission or service rules |
| `ForwarderService.kt` | Foreground service (keeps app alive) | Foreground service rules change |
| `SmsReceiver.kt` | Catches incoming SMS | SMS handling changes |
| `NotificationForwardService.kt` | Catches other apps' notifications | Notification rules change |
| `MainActivity.kt` | The config screen + permission requests | Permission flow changes |
| `EmailSender.kt` | Sends the email (SMTP) | Email/Gmail auth changes |
| `Prefs.kt` | Stores config (SharedPreferences) | Rarely — only if config format changes |
| `BootReceiver.kt` | Restarts service after reboot | Boot behavior changes |

---

## ✅ The good news

- Your **config** (email address) lives in `SharedPreferences` (`Prefs.kt`) and
  **never** needs changing for OS/policy updates.
- The APIs used (SMS receiver, foreground service, SMTP) are **stable and
  long-standing** — they rarely break.
- The build is reproducible in the cloud via GitHub Actions
  (`.github/workflows/build-apk.yml`) — no local toolchain needed.

---

## 🔁 Standard update routine (do this when a new Android drops)

1. Open `app/build.gradle.kts` → bump `compileSdk` and `targetSdk`.
2. Bump dependency versions if newer exist.
3. Commit & push → GitHub Actions builds a fresh APK.
4. Download the APK from the **Actions → Artifacts** section.
5. Install on your phone, confirm SMS + notifications still forward.
6. If something broke, check the table in section 2 above.
