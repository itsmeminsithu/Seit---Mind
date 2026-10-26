# Actual validation records — 2026-10-26

Environment: Linux container, Python 3.13, Node 22, standalone Kotlin compiler. No Android SDK/Gradle wrapper. No npm registry access.

- `python -m pytest backend/test_api.py tests -q` → **24 passed**.
- `node --test web/tests/*.test.mjs` → **13 passed**.
- `kotlinc RewardEngine.kt DailyQuotes.kt tests/RewardEngineTest.kt ... && java -jar ...` → **9 checks passed** (pure Kotlin only).
- TypeScript 5.9 transpileModule page.tsx & layout.tsx, JSX parsing → **0 syntax errors**. This is **not** full typechecking or Next build.
- `ffprobe` in Python tests → 12 unique files, > 10 minutes each.
- Developer photo SHA256 and Pyidaungsu font SHA256 → matched original user uploads exactly on both platforms.
- `npm install` → **failed EAI_AGAIN**, registry.npmjs.org DNS/network unreachable in this environment; Next build **not verified**.
- Android SDK / Gradle wrapper absent → APK build **not verified**.

**No claim of successful device install, in-browser playback, PNG visual QA, publicly hosted site, Android store submission, or font redistribution license clearance.**
