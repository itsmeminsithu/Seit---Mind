# Release acceptance — v0.7

## Automated validation run in build environment

- [x] FastAPI API tests and source integrity tests (see test logs)
- [x] Web reward and quote test suites (Node built-in test runner)
- [x] Verify developer JPG is byte-for-byte same as original upload on both platforms
- [x] Verify Pyidaungsu TTF copies are byte-for-byte identical to original user font
- [x] Verify 100 bilingual original quote pairs are identical on both platforms
- [x] Verify 12 unique demo MP3s exceed 10 minutes in duration
- [x] Basic TSX syntax parse only (NOT a full Next production build)
- [ ] Native APK build and Google Play compatibility (Android SDK unavailable)
- [ ] Next.js production bundle (npm registry DNS inaccessible)
- [ ] Release build, signing, Play Store policy review, screenshots, privacy policy
- [ ] Verify third-party font redistribution/web embedding permission
- [ ] HTTPS domain deployment

## Manual QA — required before handing to real end users

- [ ] Web UI works in Safari on iPhone, Chrome on Android, Chrome/Edge/Firefox desktop.
- [ ] Audio reliably loads, pauses/resumes, loops for 20/30/custom minutes, reaches reward threshold only while actually playing.
- [ ] 79.9% completion gives zero; 80% gives listened-minute stars, cap 60/day, streak increments once/day.
- [ ] Closing or suspending a browser does not accidentally award time the audio never played.
- [ ] 1080×1080 and 1080×1920 PNG dimensions accurate in all four themes; Myanmar characters readable and not clipped.
- [ ] Check long Myanmar + English quotes and privacy toggles across mobile browsers and Android.
- [ ] New user name/photo editable and not replaced with developer image; no personal data shown on share card until toggle is enabled.
- [ ] Developer portrait visible unchanged only in About section, website correct.
- [ ] Page-close web reminder limitation is clearly presented; Android reminder permission and reboot reschedule tested.
- [ ] Android notification pause/resume and Bluetooth/background/screen-off playback tested.
- [ ] Ask real users to review generated audio for comfort/volume changes; these are demos, not professional recordings.

**Ship decision:** No public launch until outstanding device/build/licensing/privacy tasks are completed.
