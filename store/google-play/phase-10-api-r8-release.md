# Phase 10 API Compatibility, 16 KB Pages, and R8 Release Validation

Date: 2026-09-21
Status: **CONDITIONALLY READY**

Deep Drift retains `minSdk 24`, `compileSdk 36`, and `targetSdk 36`. The normal
Android workflow remains unoptimized, while the separate optimized profile produces
R8-shrunk, optimized, and obfuscated TEST APK/AAB artifacts. Static package, native,
R8, update, and focused API 29/API 36 runtime checks pass. API 24/API 26 runtime and
a true 16 KB page-size runtime are blocked by the locally installed environment.

## Current Play target API policy

On 2026-09-21, the official Google Play target API policy says that, beginning
2026-08-31, new Android mobile apps and app updates must target Android 16 / API 36
or higher. Deep Drift targets API 36.

**TARGET SDK POLICY: COMPLIANT**

Source: [Google Play target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en)

## API compatibility matrix

| API | Environment | Static validation | Runtime validation | Status |
|---:|---|---|---|---|
| 24 | Lowest supported API; no local image | Manifest/R8 min API 24; source API audit | Not executed | BLOCKED BY ENVIRONMENT |
| 26 | Compatibility checkpoint; no local image | Same min-API and guarded-call audit | Not executed | BLOCKED BY ENVIRONMENT |
| 29 | Huawei SNE-LX1, arm64-v8a | Package metadata and optimized DEX pass | Launch, menu, gameplay, input, Home/resume, native load | PASS |
| 36 | Pixel 8 AVD, x86_64 | compile/target 36 and optimized package pass | Focused optimized UI, gameplay, lifecycle, GMA/UMP, rewarded TEST flow | PASS |

The machine SDK has only Android 36/36.1 system images. No large system image was
downloaded during this phase.

## API-sensitive source audit

- `AndroidLauncher` uses `WindowInsetsController` only under `SDK_INT >= R` (API 30),
  while the legacy immersive flags remain the lower-API path.
- Display cutout configuration remains in `values-v28` resources.
- Two calls to Java `String.isBlank()`, unavailable on older Android runtimes, were
  replaced by `trim().isEmpty()` in `GameConfig` and `AndroidAdvertisingService`.
- `Math.floorMod` usage is within API 24, the approved minimum.
- No first-party notification API usage was found.
- GMA, UMP, AndroidX Startup, WorkManager, manifest components, and lifecycle handling
  are provided by their packaged dependencies and consumer rules.
- No unguarded first-party API 28, 30, 33, 35, or 36-only call remains in the audited
  Android source.
- R8 runs with `--min-api 24`, Android 36 `android.jar`, and normal desugaring enabled.

## 16 KB native inventory

Both the optimized APK and optimized AAB contain the same inventory. Every ELF has
three `PT_LOAD` segments and every reported `p_align` is 16384 bytes.

| ABI | Library | Source dependency | ELF LOAD alignment | Status |
|---|---|---|---|---|
| armeabi-v7a | `libgdx.so` | `gdx-platform-1.14.2-natives-armeabi-v7a.jar` | 16384, 16384, 16384 | PASS |
| armeabi-v7a | `libgdx-freetype.so` | `gdx-freetype-platform-1.14.2-natives-armeabi-v7a.jar` | 16384, 16384, 16384 | PASS |
| arm64-v8a | `libgdx.so` | `gdx-platform-1.14.2-natives-arm64-v8a.jar` | 16384, 16384, 16384 | PASS |
| arm64-v8a | `libgdx-freetype.so` | `gdx-freetype-platform-1.14.2-natives-arm64-v8a.jar` | 16384, 16384, 16384 | PASS |
| x86 | `libgdx.so` | `gdx-platform-1.14.2-natives-x86.jar` | 16384, 16384, 16384 | PASS |
| x86 | `libgdx-freetype.so` | `gdx-freetype-platform-1.14.2-natives-x86.jar` | 16384, 16384, 16384 | PASS |
| x86_64 | `libgdx.so` | `gdx-platform-1.14.2-natives-x86_64.jar` | 16384, 16384, 16384 | PASS |
| x86_64 | `libgdx-freetype.so` | `gdx-freetype-platform-1.14.2-natives-x86_64.jar` | 16384, 16384, 16384 | PASS |

The verifier parses every ELF program header in every `.so` for every required ABI in
both artifacts. It also runs `zipalign -c -P 16 -v 4` against the APK and rejects
untracked, missing, or conflicting native libraries. ZIP entry alignment alone is not
used as evidence of ELF compatibility.

Official guidance: [Support 16 KB page sizes](https://developer.android.com/guide/practices/page-sizes)

## 16 KB runtime environment

The Pixel 8 API 36 AVD reports `getconf PAGE_SIZE = 4096`. The Huawei API 29 device
also reports `4096`. No locally installed AVD or connected device reports 16384.

**16 KB RUNTIME TEST: BLOCKED BY ENVIRONMENT**

## R8 architecture

- Android Build Tools 36.0.0 supplies D8/R8 `8.10.9-dev` from `lib/d8.jar`.
- `android-optimized-release` changes only the Android release packaging profile.
- The normal profile continues to use D8 and produces the ordinary QA artifact.
- The optimized profile invokes standalone R8 with program JARs, Android 36
  `android.jar` as the library input, min API 24, AAPT2-generated rules, selected
  dependency consumer rules, and the application rules in `android/r8-rules.pro`.
- Shrinking, optimization, obfuscation, and desugaring are enabled. Any R8 warning or
  nonzero exit terminates the build.
- R8 data-resource output is disabled because the existing deterministic resource
  merger packages non-class resources separately. Verification proves required
  services, assets, SDK resources, public suffix data, notices, and native libraries
  survive.
- Outputs include mapping, usage, seeds, resolved configuration, consumer-rule
  selection, tool version, warnings, command output, and size comparison.

## Consumer rules

The build extracts and inventories embedded rules before selecting them. Included rules
cover GMA, UMP, AndroidX core/lifecycle/savedstate/startup/vector/webkit, WorkManager,
Room, Play Services, Cronet/HTTP Engine, annotations, Gson, Kotlin/coroutines, OkHttp,
Okio, and Tink/protobuf. The correct modern coroutine variants are selected; older and
duplicate ProGuard/R8 variants are recorded as excluded. libGDX ships no applicable
embedded consumer file in this graph, so its JNI and reflective-controller contracts
are handled by narrow application rules.

## Application keep rules

| Rule | Reason |
|---|---|
| Preserve source/line/signature/annotation/inner-class attributes | Crash deobfuscation and SDK annotation/reflection metadata |
| Keep the public `AndroidLauncher` constructor, allowing optimization | Manifest-created entry point |
| Keep names and descriptors only for classes declaring native methods | libGDX/FreeType JNI lookup contract |
| Keep `AndroidControllers` and its public constructor, allowing optimization | Literal libGDX `Class.forName` and reflective construction |
| Keep the two packaged coroutine service providers and constructors | `META-INF/services` lookup |
| Keep enum constant fields for seven persisted enum types, allowing class optimization | Preference keys and stored enum values must remain stable across upgrades |

There is no broad keep-all rule and no blanket `-dontwarn` rule.

## Defects found during optimized runtime bring-up

| Finding | Classification | Resolution |
|---|---|---|
| R8 removed two unused JNI arguments from `Gdx2DPixmap.load`, causing image decode failures | ACTIONABLE, RESOLVED | Disallowed optimization of native member descriptors; verifier now requires exact `load([J[BII)Ljava/nio/ByteBuffer;` DEX descriptor |
| R8 removed `AndroidControllers`, causing a literal `Class.forName` failure | ACTIONABLE, RESOLVED | Added a narrow constructor/class keep rule and optimized DEX presence check |
| Dependency rules produced unmatched-rule informational diagnostics | SAFE OPTIONAL PATH | Retained as information; paths refer to optional/generated types absent from this dependency graph |
| Final R8 warning output | RESOLVED | `warnings.txt` contains exactly `# No R8 warnings.` |

## Optimized artifacts and size

- APK: `android/target/store/google-play/DeepDiveDrift-1.0.49-optimized-universal.apk`
- AAB: `android/target/store/google-play/DeepDiveDrift-1.0.49-optimized-google-play-unsigned.aab`
- R8 reports: `android/target/store/google-play/DeepDiveDrift-1.0.49-r8/`
- Package: `com.game.diver.deepdivedrift`
- Version: 1.0.49 (`versionCode 50`)
- Advertising: official Google TEST identifiers

| Artifact/metric | Before | After | Difference | Percentage |
|---|---:|---:|---:|---:|
| DEX bytes | 20,583,596 | 4,430,784 | -16,152,812 | -78.47% |
| Universal APK bytes | 17,556,917 | 11,760,961 | -5,795,956 | -33.01% |
| AAB bytes | 16,543,027 | 10,743,572 | -5,799,455 | -35.06% |
| Defined classes | 20,959 | 5,668 | -15,291 | -72.96% |
| Method references | 150,129 | 23,910 | -126,219 | -84.07% |

Size reduction is recorded as a build metric, not correctness evidence.

## Optimized package verification

The verifier confirms package/version, min/compile/target SDKs, TEST ad metadata,
merged manifest, GMA/UMP, assets, notices, audio storage, public suffix database,
coroutine service descriptors and providers, bundletool validation, signatures, four
ABIs, APK ZIP alignment, every ELF LOAD segment in APK and AAB, R8 features and reports,
first-party obfuscation, exact JNI descriptor, reflective controller class, and all 61
persisted enum fields in the mapping.

## Focused optimized runtime

### Pixel 8 / API 36

- Cold launch, main menu, shop, achievements, options, About/Legal, dive setup,
  gameplay/render/input, pause, Home/resume, and continue passed.
- libGDX and FreeType native libraries loaded without linker or native crashes.
- WorkManager/AndroidX Startup produced no shrinking-related failure.
- With UMP's official TEST extras (`reset=true`, EEA debug geography, and the registered
  test-device hash), the initial consent form loaded and displayed. Accepting it produced
  the expected dismiss/complete callbacks, after which GMA Next-Gen initialized in TEST
  mode. The Options-screen Privacy Options action then reopened the official UMP form;
  accepting it again returned cleanly to the app.
- Interstitial and rewarded TEST ads loaded into cache.
- The official rewarded TEST creative opened; exactly one earned callback and one close
  callback were observed, with no gameplay benefit granted; the next rewarded ad cached.
- No `ClassNotFoundException`, `NoSuchMethodError`, `NoSuchFieldError`, `VerifyError`,
  JNI lookup failure, `UnsatisfiedLinkError`, `dlopen` failure, SIGSEGV, or SIGBUS
  remained after the fixes.

### Huawei SNE-LX1 / API 29

- Install-over-update, cold launch, menu, dive setup, gameplay/render/input, native
  loading, and Home/resume passed with the same optimized TEST APK.
- GMA initialized far enough to cache an interstitial. Rewarded loading timed out twice
  after 30 seconds on this device during Phase 10, so a physical optimized rewarded
  display is not claimed. The full optimized rewarded flow passed on Pixel.
- No app crash, linker error, JNI error, reflection error, or native signal was found.

The final optimized APK SHA-256 is
`674f1e9a5bd01b7f9e72211a53677b18551e43b3c16b76291ee333fdf48cf702`.
The installed `base.apk` on both runtime targets has the identical hash, so the runtime
evidence applies to the final rebuilt artifact rather than an earlier intermediate.

## Save and update compatibility

A representative save was created with the unoptimized TEST build and the optimized
TEST APK was installed over it with `adb install -r`, without clearing data. The package
upgrade succeeded and first-install timestamps remained unchanged on both devices.
The optimized build preserved 67 pearls, equipment levels/install state, two unlocked
suits with `SALVAGE_GREEN` selected, 2/40 achievements and statistics, settings, schema,
HARD difficulty, and all four challenge flags. The R8 mapping verifier also proves every
persisted enum field keeps its historical name.

## Mapping retention

`mapping.txt` is generated below the versioned R8 report directory. Store that mapping
with every production release artifact for crash deobfuscation. Generated mapping files
remain untracked and were not uploaded in this phase.

## Resource shrinking

Code shrinking is enabled. Independent Android resource shrinking would require safely
recreating substantial AGP resource-usage analysis in this custom pipeline, so it is
intentionally omitted.

**RESOURCE SHRINKING: DEFERRED**

## Automated verification

- Starting Phase 8 baseline: `Project verification: PASS`, 150 tests.
- Final `scripts/verify-project.ps1`: `Project verification: PASS`, 151 tests,
  0 failures, 0 errors, 0 skipped.
- Final `scripts/verify-android-optimized-release.ps1`: PASS, including a fresh
  optimized TEST build, static APK/AAB verification, R8 reports, and size comparison.
- Final Pixel cold launch of the hash-matched APK: PASS; the official UMP EEA test form
  and Privacy Options reopen flow completed, TEST GMA initialized, and both
  rewarded/interstitial ads cached with no runtime error pattern.
- Optimized DEX contains zero `String.isBlank` references and retains the API 30 guard
  before `WindowInsetsController` calls.

## Manual verification required

- API 24 runtime on an installed emulator/device.
- API 26 runtime on an installed emulator/device.
- Runtime on a device or emulator confirmed with `PAGE_SIZE = 16384`.
- Physical optimized rewarded display if the Huawei network can load that inventory.
- Low-end sustained optimized performance.
- Long Stage 5/boss optimized session.
- Android controller runtime.
- Human assessment of physical speaker/headphone audio quality.

## Product decision gates

- TARGET AUDIENCE DECISION REQUIRED
- PUBLISHING COUNTRIES DECISION REQUIRED
- STORE LANGUAGE DECISION REQUIRED
- REWARDED BENEFIT DECISION REQUIRED

## Production blockers carried forward

- audio commercial-rights evidence
- AI boss provider/distribution terms
- JLayer/JOrbis notice/legal review
- stale boss screenshot
- real AdMob configuration and production IDs
- production signing
- unsigned final AAB

Phase 10 may proceed to Phase 11 for long-run performance, memory, thermal, and
regression testing. Phase 11 was not started.
