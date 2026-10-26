# စိတ် - Mind · v0.8 — Organized Android & Web Screens

**Your space for peace, focus & better sleep.**

Developer: **Min SiThu** · https://minsithu.org

**Status: UI Redesign / Beta Source. Not yet approved for public production release.**

## Preview

Live view (no Codespaces): https://itsmeminsithu.github.io/Seit---Mind/

![Meet the Developer](docs/previews/01-meet-developer.png)
![Home — Find your calm](docs/previews/02-home.png)
![Profile and Explore](docs/previews/03-profile-explore.png)
![Daily Wisdom](docs/previews/04-daily-wisdom.png)
![Player — Deep Calm](docs/previews/05-player-deep-calm.png)
![Guided Breathing](docs/previews/06-guided-breathing.png)
![Share Card](docs/previews/07-share-card-sunset.png)
![Share Card](docs/previews/08-share-card-forest.png)

## What changed
Previous versions mixed user profile form, reminders, meditation library, player, rewards, and developer info on one long page. Version **0.8 separates these into task-focused screens** on both Android and Web:

| Screen | What users see |
|---|---|
| Home | Welcome, quick summary, featured meditations and daily bilingual quote |
| Explore | Category filters and meditation audio catalog |
| Player | Chosen meditation, 10/20/30/custom timer, audio playback, breathing, fade-out |
| Progress | Stars, day streak, encouraging guidance and daily wisdom |
| Profile | User avatar, name, simple stats, links to settings and About |
| Settings | **Only after tapping Settings**: Edit Profile, Daily Reminder, Share Privacy, Data Controls |
| About | Original developer photo + name + website; no GitHub repository displayed |
| Completion | Mood, quote, stars, share/save square/story PNG |

Responsive navigation: desktop header + mobile bottom tabs. Android uses Compose NavigationBar. Original user photo was preserved in the **About** section only and is never shown on the Share Card.

## Mac quick start (Docker)

The default host port is **3100** to avoid conflicting with your other app at 3000.

If v0.7 is already running on port 3100, open a Terminal:

```sh
cd ~/Downloads/Seit-Mind-v0.7
docker compose down
```

Extract the new ZIP to Downloads, then:

```sh
cd ~/Downloads/Seit-Mind-v0.8
docker compose up --build
```

Open **http://localhost:3100**. Inside Docker, Next.js still listens on port 3000. FastAPI listens on container port 8000; browser accesses it through the same-origin `/api` proxy. Do not change the right-hand side of `"3100:3000"`.

Rebuilding or changing project folders will **not** erase browser localStorage on the same `http://localhost:3100` origin. To inspect containers from another Terminal run `docker compose ps`.

## Run without Docker

```sh
# Terminal 1, from backend/
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

```sh
# Terminal 2, from web/
cp .env.example .env.local
npm install
npm run dev -- -p 3100
```

## Android Native

Open `android/` in Android Studio with appropriate Android SDK 35 + JDK 17. Native Kotlin + Jetpack Compose project requires real Gradle build verification. The backend default is emulator `http://10.0.2.2:8000`. Physical Android requires `mindApiBaseUrl` set in `android/gradle.properties` to your computer's LAN API address, or public HTTPS API. Android Player/Settings/Completed screens are isolated; background service lifecycle remains in `MeditationService.kt`.

## Features preserved

- 12 synthesized demo MP3s, each > 12 minutes; 10/20/30 or custom 1–180-minute duration.
- 80% minimum completed playback to earn stars; 60 stars maximum per day; local streaks, supportive messaging.
- Guided breathing, optional 30-second audio fade, Android foreground playback service.
- 100 original English/Myanmar quotes; mood-based after meditation.
- Square 1080×1080 or Story 1080×1920 PNG export/share; four themes, optional privacy fields.
- Local user profile and photo, daily reminders (web notifications require browser/page open), About developer.
- Pyidaungsu Unicode font is bundled by user request; verify font redistribution permissions before public release.

## Tests / Limitations

```sh
python -m pytest backend/test_api.py tests/ -q
cd web && node --test tests/*.test.mjs
```

The isolated workspace could run Python and Node unit tests and TypeScript JSX parse checks, but could not install dependencies from npm registry or compile Android APK. **Do not claim production readiness until Docker/Android builds and device testing pass**. Cloud profile sync, true web background push and verified font redistribution rights are not yet included. See `docs/RELEASE_CHECKLIST_v0.8.md`.
