# Phase 9 Android Studio, Pixel Emulator, and Real-Device QA

Date: 2026-09-21
Status: **CONDITIONALLY READY**

The automated baseline and final verification pass. The repository TEST APK was installed and exercised on the existing Pixel 8 AVD and on an authorized Huawei SNE-LX1. No startup crash, ANR, progression corruption, stuck input, or navigation-blocking advertising failure was reproduced. Physical headphone quality, Android controller input, Stage 5/boss load, and production advertising/signing remain outside the executed coverage.

## Environment

- Android Studio SDK: `%LOCALAPPDATA%\Android\Sdk`
- ADB: `1.0.41`, version `37.0.1-15733141`
- Emulator: `37.1.11.0`, build `15917651`
- Installed machine platforms: `android-36`, `android-36.1`
- Installed machine build-tools: `35.0.0`, `37.0.0`
- Repository build SDK: `.android-sdk`, platform/build-tools `36/36.0.0`
- Installed system image: `android-36.1/google_apis_playstore/x86_64`
- Existing AVDs: `Pixel_8`
- TEST APK: `android/target/store/google-play/DeepDiveDrift-1.0.48-universal.apk`
- Package/version: `com.game.diver.deepdivedrift`, `1.0.48` (`49`), minSdk 24, targetSdk 36
- Advertising: TEST configuration using Google's official demo identifiers
- APK signature: valid local debug signature

## Device matrix

| Device | Type | API | Resolution | Status |
|---|---|---:|---:|---|
| Pixel 8 (`Pixel_8`, `emulator-5554`) | Existing Google Play x86_64 AVD | 36 (Android 16) | 2400x1080 landscape, 420 dpi | PASS |
| HUAWEI SNE-LX1 | Authorized physical arm64-v8a phone | 29 (Android 10) | 2340x1080 landscape, 480 dpi | PASS |

## Executed evidence matrix

The ignored `.asset-work/phase9-qa/` directory contains the cited screenshots and raw observation files. These files are intentionally not release artifacts.

| Environment | Device/AVD | API | Resolution | Test | Result | Evidence | Notes |
|---|---|---:|---:|---|---|---|---|
| Automated | Windows host | N/A | N/A | Starting unified verifier | PASS | `scripts/verify-project.ps1`: `Project verification: PASS`; 150 tests | Run before runtime QA |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Existing AVD boot and package manager | PASS | `sys.boot_completed=1`; device online; `pm list packages` responsive | Existing AVD was already running |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Fresh TEST APK install and launch | PASS | `adb install` returned `Success`; cold launch `Status: ok`, 4505 ms | Installed artifact was freshly built from this working tree |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Package/version/signature metadata | PASS | `pm path`, `dumpsys package`, build verifier | Package/version/min/target and local signature match expected values |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Main menu, dive setup, shop, achievements, controls, options, About/Legal | PASS | `pixel8/options-privacy.png`, navigation screenshots | Fonts/assets loaded; LARGE text remained readable |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Gameplay, HUD, pause, results | PASS | `pixel8/touch-gameplay.png`, `pixel8/result-1.png`, `pixel8/pause-resume-touch.png` | Rendering and letterboxed safe area were coherent |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Swim, shoot, rapid side changes, pause/continue | PASS | ADB held touch interactions and screenshots | No stuck input reproduced |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Simultaneous multitouch | MANUAL VERIFICATION REQUIRED | ADB input supplied one pointer | Emulator host interaction did not provide repeatable multitouch evidence |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Dash with a charged dash | MANUAL VERIFICATION REQUIRED | No dash charge was reached during the run | Dash button interaction did not crash, but its gameplay effect was not demonstrated |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Home/resume, short interval | PASS | Resume opened the pause screen; O2 and timer remained plausible | No wall-clock catch-up or stuck input |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Home/resume, approximately 30 seconds | PASS | Before/after screenshots; resume opened pause | O2 did not disappear and no reward duplicated |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Menu and gameplay lock/unlock | PASS | Lock/wake/dismiss-keyguard screenshots | EMULATOR TEST; gameplay resumed paused |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Activity recreation | PASS | `always_finish_activities=1`; `activity-recreated-menu.png`, `activity-recreated-active-run.png` | Menu recreated safely; active run resumed paused; setting restored to its prior unset state |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Menu process death and relaunch | PASS | PID changed `4014` to `4497`; normal menu relaunch | Exit history records only requested force stops |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Active-run process death | PASS | `process-death-active-before.png`, `process-death-relaunch.png`, `process-death-store-wallet.png` | Abandoned run produced no pearls; wallet remained 0 |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Offline launch, menu, gameplay, results-to-menu | PASS | `offline-menu.png`, `offline-gameplay.png`, `offline-results-menu.png` | UMP reported expected request failure; gameplay/navigation continued |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | UMP EEA required form and consent | PASS | `ump-eea.png`; official debug geography 1; `IABTCF_gdprApplies=1` | Official UMP TEST mechanism and test device hash used |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | UMP privacy option required and reopen | PASS | `options-privacy.png`, `ump-privacy-reopen.png` | Privacy Options row appeared and reopened Google-owned UI |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | UMP no-form-required | PASS | `ump-not-eea.png`; official debug geography 2; `IABTCF_gdprApplies=0` | GMA then initialized and cached TEST ads |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | UMP network failure | PASS | Offline log: `UMP consent information update failed: Error making request.` | Failure did not block app use |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Rewarded TEST ad load/display/earned/close/reload | PASS | `rewarded-open.png`, `rewarded-close-ready.png`; one earned callback and one close; subsequent `Rewarded ad cached` | Callback explicitly granted no game benefit; wallet stayed 0 |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Rewarded close-before-earned | BLOCKED BY ENVIRONMENT | Google TEST creative did not expose close until it displayed `Reward granted` | Back was ignored during the mandatory portion; no unsupported state manipulation used |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Interstitial frequency and placement | PASS | `interstitial-opportunity-1.png`, `-2.png`, `-3.png` | First and second completed-run returns had no ad; third showed official interstitial TEST ad |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Interstitial dismissal/menu input isolation | PASS | `interstitial-closed-menu.png` | Dismissal reached one menu; no touch leaked through |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Immersive restoration | PASS | Startup, resume, unlock, UMP/ad dismissal screenshots | Modern insets restore added; transient Google-owned UI bars were left alone while full-screen content was active |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Audio state and lifecycle | PASS | Music/SFX toggles, gameplay, pause/resume, ad transitions; no audio errors | No duplicated or stuck playback state was observed through app state |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Audible quality/headphones | MANUAL VERIFICATION REQUIRED | Emulator does not establish physical speaker/headphone quality | Requires listening hardware |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Memory snapshots | PASS | `mem-menu.txt`, `mem-after-navigation.txt`, `mem-gameplay.txt` | PSS 143278 KB, 147044 KB, 144132 KB: NO OBVIOUS UNBOUNDED GROWTH OBSERVED |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Gameplay frame observation | PASS | `surfaceflinger-gameplay-latency.txt`: 63-frame ring, about 47.8 presented FPS | No obvious stutter in representative Stage 1 play; emulator result only |
| Emulator | Pixel 8 / `Pixel_8` | 36 | 2400x1080 | Stage 5/boss memory and performance | MANUAL VERIFICATION REQUIRED | Boss was not reached in the runtime window | Desktop automated/runtime coverage remains separate |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | TEST APK install/launch and package metadata | PASS | `adb install -r`: `Success`; cold launch `Status: ok`, 4152 ms; version 1.0.48/49 | Same TEST APK as emulator |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Main UI, letterboxing, gameplay touch, pause | PASS | `huawei/launch.png`, `huawei/gameplay-touch.png` | No clipping; touch and pause operated |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Home/resume during active run | PASS | `huawei/active-home-resume.png` | Resume opened pause with coherent state |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Lock/unlock during active run | PASS | Device reported `secure=false`; `active-lock-unlock.png` | Wake/dismiss-keyguard returned to pause without crash |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Google rewarded TEST full-screen flow | PASS | `huawei/rewarded-test.png`, `huawei/rewarded-closed-game.png` | Official TEST creative displayed and returned to game; no gameplay benefit exists |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | UMP no-form runtime path | PASS | Initial consent update allowed GMA TEST inventory to cache | No consent form was required in the device's actual path |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Basic memory observation | PASS | `huawei/mem-gameplay.txt`: total PSS 117735 KB, one Activity | NO OBVIOUS UNBOUNDED GROWTH OBSERVED in focused run |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Logcat crash/ANR audit | PASS | Empty crash buffer; no ANR state | Huawei build restricts some ordinary app-log visibility, so screenshot/runtime evidence supplements logcat |
| Physical | HUAWEI SNE-LX1 | 29 | 2340x1080 | Physical speaker/headphone quality | MANUAL VERIFICATION REQUIRED | Audio controls and lifecycle were exercised without an audible assessor | Requires human listening, including headphones |
| Input | Xbox 360 Controller for Windows | N/A | N/A | Android controller QA | MANUAL VERIFICATION REQUIRED | Windows PnP found the controller; emulator `dumpsys input` exposed only keyboard/touch devices | Controller was not exposed to either Android target |
| Automated | Windows host | N/A | N/A | Final unified verifier | PASS | `Project verification: PASS`; 150 tests; Android DISABLED/TEST; desktop smoke | Run after source/report changes |
| Automated | Windows host | N/A | N/A | Whitespace/error check | PASS | `git diff --check` | No commit or push performed |

## Logcat classification

| Severity | Source | Finding | Action |
|---|---|---|---|
| INFORMATIONAL | Project Blue: Deep Drift | TEST advertising mode and Google demo identifiers logged; GMA initialized; TEST ads cached | Retain as QA evidence |
| EXPECTED TEST SDK OUTPUT | Google UMP | Offline request failed with `Error making request` | Verified app fallback; no code action |
| EXPECTED TEST SDK OUTPUT | Google Mobile Ads | TEST creative WebView/network/resource diagnostics | No code action |
| ANDROID SYSTEM NOISE | Android/WebView/emulator | Isolated WebView process exits and platform/property diagnostics | Excluded from app defects |
| INFORMATIONAL | ActivityManager | App exit history contains requested force stops used for process-death testing | No code action |
| ACTIONABLE | Project Blue: Deep Drift | System bars could remain transiently visible immediately after lifecycle return on modern Android | Fixed with focus/resume insets restoration and delayed retry, gated while full-screen ad content is active |

No uncaught exception, ANR, leaked-window error, actionable OpenGL error, actionable audio failure, or unexpected application exit was found. The final emulator app-log pattern scan and both crash buffers were empty for those categories.

## Bugs found and fixes

Reproduction: lock/unlock or return from a full-screen lifecycle transition on the Pixel AVD, then observe that gesture/status UI could remain visible instead of the game restoring immersive state.

Fix: `AndroidLauncher` now restores immersive mode on resume and focus, applies modern `WindowInsetsController` hiding on API 30+, retries once after the lifecycle transition, and skips restoration while a Google full-screen ad is active. Pixel unlock and UMP/rewarded/interstitial dismissal were rerun successfully.

The phase also adds TEST-only ADB QA hooks for official UMP debug geography/reset and rewarded display. Both are protected by `AdMode.TEST`; production mode ignores them. The rewarded callback logs evidence and grants no pearls, upgrades, oxygen, revive, equipment, or achievements.

## Manual verification required

- Repeatable simultaneous multitouch and a dash with an earned dash charge.
- Stage 5/boss memory, particle, and frame-rate observation on Android.
- Android controller behavior after the connected controller is exposed to an Android target.
- Human-audible physical speaker/headphone quality and transition checks.
- Rewarded close-before-earned with an official TEST creative that provides an early-close opportunity.
- Low-end physical-device performance; the attached Huawei is real-device evidence but is not designated as the product's low-end target.

## Product decision gates

- TARGET AUDIENCE DECISION REQUIRED
- PUBLISHING COUNTRIES DECISION REQUIRED
- STORE LANGUAGE DECISION REQUIRED
- REWARDED BENEFIT DECISION REQUIRED

## Production blockers

- audio commercial-rights evidence
- AI boss provider/distribution terms
- JLayer/JOrbis notice/legal review
- stale boss screenshot
- real AdMob configuration
- production ad IDs
- production signing
- unsigned AAB

## Recommendation

Phase 9 is CONDITIONALLY READY. Proceed to **Phase 10 — API 26, Current Target SDK, 16 KB Page Size and R8/Minify Validation** without treating the remaining manual and production blockers as resolved.
