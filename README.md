# GMC Monitor – Android App

A WebView-based Android app for monitoring radiation data from [gmc.tvipper.com](https://gmc.tvipper.com).

---

## 🔧 Features

- Loads website in WebView
- Supports Android App Links
- Shows notifications for high radiation levels
- Background API check every 15 minutes via WorkManager

---

## 📦 App Links

Automatically opens the app when the user clicks on a link like:

```
https://gmc.tvipper.com/*
```

### Requirements:
- Intent filter in `AndroidManifest.xml`
- Active `assetlinks.json` at:
  ```
  https://gmc.tvipper.com/.well-known/assetlinks.json
  ```

---

## 🔐 Signing and Certificates

- App is signed by Google Play
- SHA-256 fingerprint from `deployment_cert.der` is used in `assetlinks.json`

---

## 🗃️ Files You Should Keep

- `upload-keystore.jks` – Your upload key (private, keep secure)
- `upload_certificate.pem` – Public key (submit to Google if needed)
- `deployment_cert.der` – Google’s app signing certificate (public)
- `.well-known/assetlinks.json` – For App Link validation

---

## 📜 Permissions

- `INTERNET`
- `POST_NOTIFICATIONS` (Android 13+)

---

## 💡 Note

Play Integrity API is **not implemented**.

---

© 2025 Jakob / Tvipper.com
