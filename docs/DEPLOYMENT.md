# Deployment guide — public browser access and Android

## Web MVP, same-origin API

1. Use Docker Compose locally: `docker compose up --build`.
2. On a host with a domain, configure reverse-proxy HTTPS (e.g. Caddy, nginx or managed ingress) to expose **only the web container port 3000**. Keep FastAPI port 8000 internal. The Next.js server forwards `/api/*` and `/health` to `MIND_API_SERVER_URL` on the private network.
3. Do not expose a development HTTP URL publicly. Enable automated TLS certificates and security updates for your hosting environment.
4. Test on actual Safari iPhone, Chrome Android, desktop browsers. No install is required to browse the website; PWA installation is optional and **not a prerequisite**.
5. Test MP3 seek/range with long 12-minute streams, slow connection, reconnect/pause, sharing and font load. Browser timer is best-effort when tabs are backgrounded or the device suspends.
6. The public frontend uses no user accounts or server profile storage. Publishing it does not cause profiles to sync. Draft a correct privacy policy explaining local storage/notifications and any host/access logs.
7. Web reminders **do not work with page fully closed**. True closed-browser Web Push needs service worker, authenticated push subscriptions, a server scheduler, opt-in and infrastructure not shipped here.

## Android

Use a valid reachable **HTTPS** `mindApiBaseUrl` in Android release builds (Gradle property); emulator development uses `10.0.2.2`. Build and sign with your own signing keystore, add approved screenshots/graphics, and test notification permission, reboot reminder, battery optimization/background audio, Bluetooth and foreground media notification controls. The project does not contain a prebuilt APK, signing keys, or a release-ready Google Play listing.

## Rights review before launch

- Confirm exact rights for bundled **Pyidaungsu 2.5.3 Regular** embedding and redistribution. This is pending; do not assume it is cleared.
- Confirm the synthesized tracks are suitable and accurately described. There are no recordings of actual Buddha chants, and no actual music by famous artists.
- The 100 quotes are original reflective lines; do not attribute them to Buddha, Marcus Aurelius, or other people.
- Developer portrait is supplied by the project owner and preserved unchanged.

## Production blockers

- Android APK / device QA and Next production build must be completed.
- Profiles/streaks/rewards are local only; add authentication, idempotent server-side event ledger, conflict handling and timezone rules for cloud sync.
- Reminder precision and browser background audio cannot be guaranteed across all devices.
- Need official deployment domain, privacy/terms, and support channel prior to broad distribution.
