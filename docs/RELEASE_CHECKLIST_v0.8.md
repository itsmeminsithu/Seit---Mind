# v0.8 Release Verification Checklist

## Local host setup
1. Stop v0.7 Docker containers from the OLD folder using `docker compose down` (do not use `-v`).
2. From extracted `Seit-Mind-v0.8`, run `docker compose up --build` and check `http://localhost:3100`.
3. Check `docker compose ps`; web should map `3100:3000`; backend should be `Up`.

## Functional acceptance tests (manual)
- Web at mobile and desktop widths: navigation Home, Explore, Progress, Profile and Settings; ensure Home does not expose inputs.
- Settings: update name/photo and reminder; reload site; confirm values retained; hide optional name/streak/stars on share card.
- Explore: select audio; player opens; 10/20/30/custom durations work; play and pause; loop; optional fade; breathing contrast is readable.
- Navigate to Home during playback and use mini player to return. Check no unexpected audio restart.
- Complete 79%: no stars, no completion card. Complete ≥80%: redirect to completion, earned stars, streak, mood, daily quote.
- Share: 1080×1080 and 1080×1920 PNG, EN/MM/both, all themes, correct privacy selection and Pyidaungsu glyph rendering on downloaded images.
- Android: open each bottom tab; Settings only after tapping button; select session from Explore then start; service notification; pause/resume/finish; remove photo; check About developer portrait.
- Browser reminders work only while the web page remains open. Android reminders require notification permission.

## Unverified release gates
- Next.js production build **not verified in this isolated environment** (npm registry lookup failed). Run Docker build on Mac.
- Android app **not built on device in this environment** (Android dependencies unavailable); Gradle compile and device test required.
- Pyidaungsu font redistribution rights need independent verification before public distribution.
- No account sync or background web push; local profile/stars are device-specific.
- Audio files are synthesized testing samples, not clinical or commercial content.
