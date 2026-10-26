# စိတ် - Mind v0.6 — Source Review & Fix Register

Scope: unpacked **v0.5.1 user-provided ZIP**. Audited Kotlin service/activity, web page/CSS, API/HTTP audio streaming, build metadata, media files, Pyidaungsu Unicode font, developer photo, developer docs. Changed code shipped in v0.6 ZIP.

## Confirmed findings addressed

| Priority | Finding | Fix |
| --- | --- | --- |
| High | App used developer portrait as the default **user** profile photo | Replace with neutral placeholder; developer attribution retains exact supplied portrait |
| High | Android emulator-only hardcoded endpoint prevented physical phone use | `BuildConfig.API_BASE_URL` uses configurable `mindApiBaseUrl` Gradle property; documented emulator vs physical device |
| High | Android release allowed unencrypted HTTP for all networks | Cleartext traffic enabled **debug only**; release requires HTTPS |
| High | Reward rules duplicated in UI/service with insufficient edge-case tests | Pure deterministic calculation libraries in JS and Kotlin; 80% threshold, 60/day cap, repeated same-day completion, missed-day behavior and invalid durations tested |
| High | Android service did not explicitly handle audio focus/lifecycle/failed setup robustly | Audio focus, wake lock, ready-state and playback errors; complete/discard distinction; notification pause/resume/finish actions |
| High | Android service could retain old completion event/data after a failed or early session | Clear stale completion on start; atomic reward/session state updates at end |
| Medium | MediaStore `RELATIVE_PATH` used unconditionally despite Android minimum API 26 | Android 10+ MediaStore with pending state; Android 8–9 app-specific storage fallback |
| Medium | Android share-card `Canvas` used default typeface instead of the supplied Myanmar font | Load Pyidaungsu on bitmap output and constrain long text widths |
| Medium | Android share card omitted the date-specific daily quote | Shared day-index formula for Android quotes; display on completion and export |
| Medium | Browser exported PNG without waiting for the web font to load | `document.fonts.load()` before rendering canvas bitmap |
| Medium | React localStorage quota/security errors could crash profile updates | Guard reads/writes and display save-failure notice |
| Medium | Browser reward integration did not have isolated tests | 9 executable tests with Node built-in test runner |
| Medium | Backend `404` implemented via generator trick | Explicit HTTPException / clean error handling, CORS from env, byte-range tests |
| Medium | Repo ignored `*.mp3`, so GitHub clone would miss all test music | Remove global MP3 exclusion so sample audio files are tracked |

## Objective evidence

- `backend`: 10 FastAPI tests pass.
- `web`: 9 Node reward logic tests pass.
- `android`: Kotlin domain checks pass with `kotlinc` (7 reward + 2 quote checks).
- `ffprobe`: 12 different audio-file SHA hashes, 12 files with duration **734.364 sec** (each >10 min).
- `ffmpeg`: all 12 MP3 tracks fully decoded from start to finish with no decoding errors (this does not measure sound quality).
- Developer picture: SHA-256 identical to latest provided `IMG_1649(3).jpeg`; no image editing.
- Pyidaungsu: project Android and Web TTF exactly match user-provided Regular TTF.
- ZIP: checked for CRC/integrity, excluded build artifacts and secrets.

## Still unverified / not finished

- **Android full native compilation**: no Android SDK/Gradle wrapper in audit environment. Test with Android Studio.
- **Next.js production build**: npm registry DNS/network inaccessible; TypeScript/JSX syntax only can be parsed offline. Full dependency resolution and production bundling unverified.
- **Browser playback**: no physical browser run; startup, audio permission, buffering, mobile background process and export/share need QA.
- **Android playback**: Android 13–15 physical devices need QA for media foreground service and battery restrictions.
- **Account syncing and server-controlled rewards**: unimplemented; data stays on each device, can be modified locally. No cloud backups.
- **Alarm vs reminder**: Android uses *inexact* recurring alarm; web notifications need open browser. No guaranteed exact-time alarm or closed-browser push.
- **User font rights**: user supplied Pyidaungsu, but redistribution/embedding rights not independently established.

## Acceptance checklist for release

1. Android Studio debug APK installs, 10min/20min/custom track plays; media notification pause and resume succeed with screen off.
2. Verify 79.9% completion earns zero; 80% earns tracked minutes, daily cap 60, streak one per day.
3. Verify both share-card privacy defaults are OFF, 1080x1080 PNG with readable Myanmar glyphs.
4. Verify notification permission and daily reminder behavior after reboot/device timezone changes.
5. Check web on iPhone Safari, Android Chrome, desktop Safari/Chrome/Edge, including silent mode and backgrounding.
6. Ship a verified HTTPS backend with CORS, terms/privacy and properly licensed font/audio.
