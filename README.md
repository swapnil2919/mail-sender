# Mail Sender

A small **personal** Android app that forwards incoming **SMS** and (optionally)
**notifications** from other apps to an **email address** you configure inside the app.

> Built for your own phone — sideloaded, not for the Play Store.

---

## What it does

```
📱 SMS / Notification arrives
        │
        ▼
  Foreground Service (keeps app alive)
        │
   Sends an email via Gmail SMTP
        │
        ▼
   ✉️  Your inbox
```

## Screen

- **Send to email** — where messages get forwarded
- **Sender Gmail** — the Gmail account used to send (needs an App Password)
- **App password** — Gmail App Password (NOT your normal password)
- **Forward SMS** / **Forward Notifications** toggles
- **Save**, **Send test email**, **Grant notification access**
- **Start / Stop Service** + status

---

## One-time setup before it works

### 1. Create a Gmail App Password (for the sender account)
1. The sender Gmail account must have **2-Step Verification ON**.
2. Go to **Google Account → Security → 2-Step Verification → App passwords**.
3. Generate one, copy the 16-character password.
4. Paste it into the app's **App Password** field.

> Why? Gmail blocks normal-password SMTP logins. App Passwords are the supported way.

### 2. Permissions
- On first **Start Service**, grant **SMS** + **Notifications** permission.
- For notification forwarding, tap **Grant notification access** and enable this app
  in the system list.

### 3. Battery (important for reliability)
- Settings → Apps → Mail Sender → **Battery → Unrestricted**
  (otherwise the phone may kill the background service).

---

## How to build

1. Open the **`mail sender`** folder in **Android Studio**.
2. Let Gradle sync (downloads dependencies).
3. Connect your phone (USB debugging ON) or use an emulator.
4. Press **Run ▶**. The app installs on your device.

To get a shareable APK:
`Build → Build Bundle(s) / APK(s) → Build APK(s)`

---

## Project structure

```
app/src/main/
├── AndroidManifest.xml          permissions + components
├── java/com/example/mailsender/
│   ├── MainActivity.kt          the single config screen
│   ├── Prefs.kt                 saves settings (SharedPreferences)
│   ├── EmailSender.kt           sends email over Gmail SMTP
│   ├── ForwarderService.kt      foreground service (keeps app alive)
│   ├── SmsReceiver.kt           catches incoming SMS
│   ├── NotificationForwardService.kt   catches other apps' notifications
│   └── BootReceiver.kt          restarts service after reboot
└── res/layout/activity_main.xml the UI
```

---

## Notes / future-proofing

- Uses stable, long-standing Android APIs (SMS receiver, foreground service,
  notification listener). To support a new Android version, bump
  `compileSdk` / `targetSdk` in `app/build.gradle.kts` and rebuild.
- All config is stored on-device in SharedPreferences; nothing leaves your phone
  except the emails you send to yourself.
```
