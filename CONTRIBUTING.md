# Contributing to unshort-android

Thank you for helping improve digital wellbeing tooling for Android.

## Development setup

1. Fork and clone the repository.
2. Run `npm install`.
3. Generate native project: `npm run prebuild`.
4. Build and install on a device: `npm run android`.

Use a **development build** (`expo-dev-client`). Expo Go cannot load the native blocker module.

## Areas for contribution

### YouTube view IDs

When YouTube updates break Shorts tab detection, add entries to:

- [`shorts-view-ids.json`](shorts-view-ids.json)
- [`modules/unshort-core/android/src/main/assets/shorts-view-ids.json`](modules/unshort-core/android/src/main/assets/shorts-view-ids.json)

Include:

- YouTube app version (if known)
- `contentDescriptions` that match the Shorts tab
- `viewIds` from Layout Inspector / accessibility tools

### Device calibration profiles

Add JSON profiles to [`community-profiles/default-profiles.json`](community-profiles/default-profiles.json):

```json
{
  "name": "device-name-5-tabs",
  "deviceModel": "Pixel 8",
  "tabCount": 5,
  "shortsTabIndex": 1,
  "leftPct": 0.2,
  "topPct": 0.92,
  "widthPct": 0.2,
  "heightPct": 0.06
}
```

Export a profile from the in-app settings screen to capture normalized coordinates.

## Testing overlay blocking

1. Grant overlay + accessibility permissions in the app.
2. Activate protection.
3. Open the official YouTube app.
4. Try tapping the Shorts tab — navigation should not occur.
5. Open another app — overlay must not appear.

Enable **debug overlay** in advanced settings to visualize the blocked region (red semi-transparent).

## Code guidelines

- Keep overlay show/hide on the **native hot path** (`OverlayController`, accessibility service).
- TypeScript is for onboarding, settings, and bridge wrappers only.
- Match existing Kotlin style in `modules/unshort-core/android/`.
- Run `npm run typecheck` before opening a PR.

## Pull requests

- Describe YouTube version and device tested.
- Attach screenshots or screen recording for UI changes.
- Note any Play Store policy implications for accessibility or overlay usage.

## Reporting detection failures

Open an issue with:

- Device model and Android version
- YouTube app version
- Bottom tab count and Shorts tab position
- Whether heuristic, accessibility, or calibration mode was used
