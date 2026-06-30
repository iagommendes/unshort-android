# unshort-android

Open-source React Native (TypeScript) Android app for digital wellbeing. Unshort draws an invisible overlay over the YouTube Shorts tab in the official YouTube app (`com.google.android.youtube`) to prevent accidental taps.

## How it works

1. **Foreground service** keeps protection alive with a persistent notification.
2. **Accessibility service** detects when YouTube is in the foreground (package-scoped only).
3. **System overlay** (`SYSTEM_ALERT_WINDOW`) blocks touches on the Shorts tab region.

Overlay positioning uses a layered strategy:

- **Heuristic** — percentage-based bottom navigation layout (default).
- **Accessibility bounds** — locates the Shorts tab node when possible.
- **Manual calibration** — user-tuned normalized coordinates and community profiles.

## Stack

- Expo Prebuild + Development Build (`expo-dev-client`)
- Local native module: [`modules/unshort-core`](modules/unshort-core)
- Kotlin: `BlockerForegroundService`, `YouTubeDetectorAccessibilityService`, `OverlayController`

> **Expo Go is not supported.** Native services require a dev or production build.

## Requirements

- Node.js 20+
- Android SDK 35
- Physical Android device (API 26+) for overlay/accessibility testing

## Setup

```bash
npm install
npx expo prebuild --platform android
npx expo run:android
```

Or with EAS:

```bash
npx eas build --profile development --platform android
```

## Permissions (manual)

Users must grant:

1. **Display over other apps** (`SYSTEM_ALERT_WINDOW`)
2. **Accessibility service** (YouTube-only detection)
3. **Notifications** (foreground service channel)

Some OEMs (Xiaomi, Samsung, Huawei) may kill background services. See [dontkillmyapp.com](https://dontkillmyapp.com) if protection stops unexpectedly.

## Project structure

```
src/                     # React Native UI (onboarding, settings)
modules/unshort-core/    # Kotlin native module + config plugin
shorts-view-ids.json     # Community-maintained YouTube view IDs
community-profiles/      # Device calibration profiles
```

## Play Store notes

- Declare Accessibility Service usage with a demo video.
- Declare `specialUse` foreground service subtype.
- Provide a clear in-app privacy disclosure.

## License

See [LICENSE](LICENSE).
