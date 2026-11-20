# 👑 HAIDER OS TV — Android 4.4.4 APK

Android TV home replacement app for older Haier TVs (minSdk 19). Black-and-gold interface, apps list, file/APK browser, basic media-opening, system settings, hardware info and an optional LAN phone remote receiver. No games. **Does not replace or upgrade Android firmware.**

## Download compiled APK
Open **Actions → Build APK → latest successful run → Artifacts → HAIDER-OS-TV-APK**. Extract the ZIP and install `app-debug.apk` using ES File Explorer on your TV. The GitHub Actions workflow builds on push; check that its status is green before downloading.

## Connect your iPhone remote app
Open **Phone Remote** in HAIDER OS TV, start receiver and read the actual TV IP and pairing token. iPhone must be on the same trusted Wi-Fi. Your previously created remote app needs its command sender integrated; it is not automatically paired.

Send HTTP POST to `http://TV_IP:8765/command` with header `X-Haider-Token: TOKEN_SHOWN_ON_TV` and JSON body `{"key":"DOWN"}`. Supported commands: `UP`, `DOWN`, `LEFT`, `RIGHT`, `OK`, `BACK`, `HOME`, `VOLUME_UP`, `VOLUME_DOWN`. This controls the HAIDER OS TV app while active, not the protected system UI. Disable the receiver after use. HTTP is unencrypted, so use only on trusted private networks.

## Safety and status
This repo contains Android source code and a GitHub Actions build pipeline, not confirmed working firmware. Real-device verification on this exact Haier model is still required. Keep the factory Haier launcher as fallback. Android 4.4 APK install may require enabling Unknown sources in device settings.