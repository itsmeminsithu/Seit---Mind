# Changelog

## 0.8.0 — Organized Navigation & Settings (2026-10-26)
- Redesigned Next.js web UI: separate **Home, Explore, Progress, Profile, Settings, About, Meditation Player, Completion & Share** screens. No more all-in-one screen.
- Responsive desktop pill navigation, mobile bottom navigation; settings accessible from header or profile.
- Home now shows welcome, small progress summary, featured sessions and bilingual quote only; **editing controls are isolated to Settings**.
- Meditation Player has its own screen with 10/20/30/custom minutes, guided breathing, pause/finish, optional fade-out; audio element stays mounted across navigation and compact mini player returns to it.
- Settings groups: Profile/photo, Daily Reminder, Share Card Privacy, Erase Local Data, About.
- Android Jetpack Compose uses a `Scaffold` top bar and bottom tab navigation; isolated screens for Player, Settings, Profile and Completion; settings controls are grouped in separate cards.
- Kept original developer photo unchanged, displayed only in About; kept Pyidaungsu Unicode, 100 bilingual quotes, 12 long test MP3s, rewards and PNG exports.
- Default Docker host web port changed to **3100** to avoid conflict with another app on 3000; container still uses 3000.
- Preserves the same browser localStorage profile key as v0.7; users upgrading on the same host/port retain local preferences unless they clear browser site data.
- Added focused navigation tests and adjusted tests to reflect the new About screen.
- Full native Android APK and Next.js production builds still require local environment verification; see `docs/RELEASE_CHECKLIST_v0.8.md`.

## 0.7.0
- Original release candidate: offline tracks, rewards, bilingual quote library, share cards, developer profile and local reminders.
