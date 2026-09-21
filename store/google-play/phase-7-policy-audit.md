# Phase 7 — Google Play policy, audience, and Data Safety audit

Audit date: September 21, 2026

Status: **CONDITIONALLY READY** for Phase 8 validation. This is an internal preparation document, not a Play Console submission.

## Decision gates

- `TARGET AUDIENCE DECISION REQUIRED`
- `PUBLISHING COUNTRIES DECISION REQUIRED`
- `STORE LANGUAGE DECISION REQUIRED`
- `REWARDED BENEFIT DECISION REQUIRED`
- `POLICY CONFIGURATION PENDING`

No audience, child-directed, under-age-of-consent, country, store-language, or rewarded-benefit choice is made in this audit.

## Current official sources

All sources below were accessed September 21, 2026.

| Policy area | Official source | Relevant requirement |
| --- | --- | --- |
| Target audience | [Google Play: Manage target audience and app content settings](https://support.google.com/googleplay/android-developer/answer/9867159?hl=en) | Select only intentionally targeted age groups; mixed audiences need a neutral age screen; local law can treat people under 21 as children. |
| Families | [Google Play Families Policy Requirements](https://support.google.com/googleplay/android-developer/answer/9893335?hl=en) | Any selected group that includes children brings the applicable Families requirements. |
| Families data practices | [Google Play Families data practices](https://support.google.com/googleplay/android-developer/answer/11043825?hl=en) | Child and unknown-age users have identifier, SDK, data, and advertising restrictions. |
| Families ad SDKs | [Families Self-Certified Ads SDK Program](https://support.google.com/googleplay/android-developer/answer/12955712?hl=en) | Ads served to children or unknown-age users must use a self-certified SDK/version and comply with child ad rules. |
| AdMob Families treatment | [AdMob: comply with Google Play Families Policy](https://support.google.com/admob/answer/6223431?hl=en) | Child/unknown requests need child treatment, a G maximum content rating, and eligible child-directed ad sources. |
| Ads | [Google Play Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753?hl=en) and [Better Ads guidance](https://support.google.com/googleplay/android-developer/answer/12271244?hl=en-GB) | No unexpected disruptive interstitials; a full-screen ad after a score screen is an example of a natural break. |
| Data Safety | [Google Play Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) | Include SDK transfers, apply Play's collection/sharing definitions, and declare globally for the distributed package. |
| GMA data | [GMA Next-Gen Play data disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure) | GMA automatically collects and shares IP address, interactions, diagnostics, and device/account identifiers for ads, analytics, and fraud prevention. |
| GMA age treatment | [GMA Next-Gen targeting](https://developers.google.com/admob/android/next-gen/targeting) | Configure age-restricted treatment and maximum ad content rating; restrictive privacy signals constrain personalization and identifier transmission. |
| UMP | [UMP Next-Gen privacy setup](https://developers.google.com/admob/android/next-gen/privacy) and [release notes](https://developers.google.com/admob/android/next-gen/privacy/release-notes) | Refresh consent each launch, show required forms/privacy options, gate ads with `canRequestAds()`; UMP 2.2.0 removed Advertising ID use. |
| Advertising ID | [Google Play Advertising ID policy](https://support.google.com/googleplay/android-developer/answer/6048248?hl=en) and [Android 13 behavior changes](https://developer.android.com/about/versions/13/behavior-changes-13) | Target 33+ apps need `AD_ID` to access AAID; use is constrained by user choices and child treatment. |
| Consent regions | [AdMob EEA requirements](https://support.google.com/admob/answer/13554116?hl=en), [European regulations messages](https://support.google.com/admob/answer/7666519?hl=en), and [US state regulations messages](https://support.google.com/admob/answer/10862202?hl=en) | Applicable regions require correctly configured consent/privacy messages; UMP can present Google Privacy & Messaging flows. |
| Foreground service | [Play foreground-service requirements](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en) | Actual foreground-service uses/types in the published bundle must match Play declarations. |
| Country availability | [Distribute app releases to specific countries](https://support.google.com/googleplay/android-developer/answer/7550024?hl=en) | Play country targeting follows the user's Play country and must be chosen explicitly. |

## Product facts

Project Blue: Deep Drift is a single-player arcade survival game with English and Turkish in-game UI. The player controls a diver, manages oxygen, fires a harpoon at hostile fish, sharks, piranhas, eels, sea mines, and a multi-stage Abyssal Octopus boss, collects power-ups, and can fail from damage or oxygen loss. Presentation is pixel/cartoon style with impact particles, flashes, screen shake, and tense boss presentation, without blood or gore. Flashing and screen-shake effects can be disabled.

Pressure Pearls are earned through play and spent on local equipment and suit progression. There is no billing, real-money purchase, loot box, gambling, account, chat, social feature, user-generated content, location sharing, cloud save, or in-game external link. Difficulty and challenge modes increase reaction and survival demands. The Android build contains ads; the player-facing rewarded placement remains disabled.

## Target audience analysis

The repository contains no approved target-audience decision. Visual style, color, or accessibility is not evidence that children are intentionally targeted.

Current Play Console age-group choices are:

- Age 5 and under
- Ages 6–8
- Ages 9–12
- Ages 13–15
- Ages 16–17
- Ages 18 and over

`TARGET AUDIENCE DECISION REQUIRED`

| Case | Consequence | Status |
| --- | --- | --- |
| A — Audience excludes children | Select no child age group. Families child-audience rules do not arise solely from the product's art style. Regional consent and age-of-consent duties can still apply. The listing and imagery must not unintentionally appeal to children in a way inconsistent with the declaration. | `PENDING PRODUCT DECISION` |
| B — Mixed audience | Families applies to child groups. Implement a neutral age screen before collecting age-sensitive data or requesting ads. Child and unknown-age users need child-safe SDK/data/ad treatment; adult treatment may occur only after age is established neutrally. | `PENDING PRODUCT DECISION` |
| C — Primarily/solely child-directed | Families applies throughout. Configure child treatment before SDK initialization/ad requests, prevent restricted identifiers, use child-appropriate ads/content, and meet all child data/privacy requirements. | `PENDING PRODUCT DECISION` |

Selections for ages 13–15 or 16–17 do not by themselves settle every jurisdiction: Google instructs developers to consider local definitions of children, which can extend to users under 21.

## Families policy analysis

If any selected group includes children:

- Families Policy becomes applicable to the relevant audience.
- Ads shown to children and users of unknown age must use only eligible Families Self-Certified Ads SDK versions and child-appropriate ad sources/content.
- Interest-based advertising and remarketing must not be used for child or unknown-age users.
- AAID and other restricted persistent identifiers must not be transmitted for child/unknown-age treatment. For solely child-directed apps targeting Android 13+, Play says the app should not request `AD_ID`.
- A mixed-audience app needs a neutral age screen with no preselected age, age hint, or design that encourages an adult answer. Until a user is known to be an adult, the default treatment must meet child/unknown-age restrictions.
- Child-facing ad UI must be clearly distinguishable, avoid deceptive placement, and meet Families full-screen close/dismiss rules.
- Privacy disclosures and data practices must accurately describe child handling.

The current Self-Certified Ads SDK list explicitly names `com.google.android.gms:play-services-ads` 19.0.0+, while this project uses the different Next-Gen artifact `com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk` 1.4.0. Next-Gen supports age treatment, but the current Families list does not explicitly name that artifact. If children are selected, obtain an official answer or move to an explicitly listed eligible SDK/version before production child ad serving.

Next-Gen Families eligibility: `PENDING CURRENT POLICY VERIFICATION`.

Current code intentionally sets no child-directed or under-age flag and contains no age screen because the product decision is unresolved: `POLICY CONFIGURATION PENDING`.

## GMA policy configuration matrix

| Audience case | Required/possible ad treatment | Code/config implication | Decision status |
| --- | --- | --- | --- |
| A — excludes children | Use the final audience, regional law, UMP response, and ad-account settings. Do not assert child treatment solely from appearance. Set a maximum rating compatible with the final content/audience declaration. | Configure Next-Gen `AgeRestrictedTreatment` only after the actual treatment is known; configure maximum ad content rating; AAID may be available when permission and privacy signals allow it. | `PENDING PRODUCT DECISION` |
| B — mixed | Child and unknown-age users require child-safe, non-personalized treatment; known adults may use treatment allowed by regional consent. Most restrictive signal wins. | Add an approved neutral age screen before SDK/ad decisions. Set CHILD for child/unknown-age paths and the appropriate known-adult value otherwise; use maximum rating G for child/unknown-age ads; verify certified SDK eligibility. | `PENDING PRODUCT DECISION`; SDK eligibility `PENDING CURRENT POLICY VERIFICATION` |
| C — primarily/solely children | Treat all users as children, serve only child-appropriate non-interest-based ads, prevent AAID transmission, and use maximum rating G. | Set CHILD before initialization/request, review/remove `AD_ID` as policy requires, and use only eligible Families-certified SDK/ad sources. | `PENDING PRODUCT DECISION`; SDK eligibility `PENDING CURRENT POLICY VERIFICATION` |

Next-Gen's current unified `AgeRestrictedTreatment` API replaces the older separate child-directed/under-age request flags. Personalization also depends on UMP consent and publisher privacy settings. No age flag should be added until the audience decision and regional plan are approved.

## Advertising policy audit

| Check | Finding | Status |
| --- | --- | --- |
| Interstitial placement | The only opportunity is after a completed Results screen when the user explicitly chooses Return to Menu. Google identifies post-score screens as natural breaks. | `POLICY COMPLIANT BY DESIGN` |
| First opportunity/frequency | Never first opportunity; at most one per three completed runs; ten-minute minimum after display; one opportunity suppressed after a rewarded ad. | `POLICY COMPLIANT BY DESIGN` |
| Gameplay interruption | No launch, active-gameplay, stage-start, or stage-transition interstitial path exists. | `POLICY COMPLIANT BY DESIGN` |
| Dismissibility/creative | Google-rendered full-screen behavior and close control depend on the served creative and device. | `MANUAL DEVICE VERIFICATION REQUIRED` |
| UI distinction/taps | Game UI has no ad-lookalike control; Return to Menu is the related navigation trigger. Confirm that a loaded ad appears only after release and no stale callback produces an unrelated display. | Design: `POLICY COMPLIANT BY DESIGN`; runtime: `MANUAL DEVICE VERIFICATION REQUIRED` |
| Rewarded | Technical infrastructure exists, but no player-facing placement or benefit exists. Any future placement must be explicit opt-in and disclose its benefit before viewing. | `REWARDED BENEFIT DECISION REQUIRED` |

A future cosmetic/non-economic rewarded benefit does not create an economy or purchase by itself. It still requires a clear pre-ad description, voluntary opt-in, reward only after the earned callback, accurate UI, and any audience-specific Families treatment. Phase 3 economy remains unchanged.

## Actual data-flow inventory

### First-party game data

| Data | Storage | Off-device flow | Result |
| --- | --- | --- | --- |
| Pressure Pearls; equipment purchase/install levels and timers; suits | libGDX Preferences | None in first-party code | Local only |
| Achievements; high score; completed-run sequence; kill, oxygen, power-up, victory, and perfect-run counters | libGDX Preferences | None | Local only |
| Difficulty, challenge, audio, volume, display, shake, and flash settings | libGDX Preferences | None | Local only |
| Active run state/configuration | Memory during play; selected setup preferences persist locally | None | Local only |

Android backup is disabled and backup rules exclude application data. These local-only values are not “collected” under Play's off-device definition.

### Third-party SDK data

GMA Next-Gen `1.4.0` automatically collects and shares IP address/approximate-location inference, app/product interactions, diagnostics, AAID, App Set ID, and—where applicable—other signed-in-account-related identifiers. Google maps the purposes to advertising, analytics, and fraud prevention and says transfer uses TLS.

UMP `4.0.0` refreshes consent information, displays required Google forms, exposes privacy-option status, and controls request eligibility. The app does not request ads until `canRequestAds()` permits it. UMP no longer uses Advertising ID according to Google's release notes.

## Data Safety mapping

The detailed proposed response matrix is in [data-safety-tr.md](data-safety-tr.md). Proposed collected/shared categories are:

| Play category | Collected | Shared | Purposes | Evidence/status |
| --- | --- | --- | --- | --- |
| Approximate location | Yes | Yes | Advertising or marketing; analytics; fraud prevention, security, compliance | GMA IP disclosure — `CONFIRMED` |
| App activity → App interactions | Yes | Yes | Same | GMA interaction disclosure — `CONFIRMED` |
| App info and performance → Diagnostics | Yes | Yes | Same | GMA diagnostics disclosure — `CONFIRMED` |
| Device or other IDs | Yes | Yes | Same | GMA identifier disclosure and merged `AD_ID` — `CONFIRMED` |

Treat the four categories as required and non-ephemeral in the proposed form because all-user opt-out and ephemeral-processing criteria are not established. Confirm those UI choices, deletion response, and final AAB at submission: `MANUAL PLAY CONSOLE VERIFICATION`.

## Advertising ID

The merged `com.google.android.gms.permission.AD_ID` permission is contributed by the GMA Next-Gen AAR, not UMP. The target SDK is 36, so Android returns AAID only when this permission is present and user/platform/privacy restrictions allow it. GMA's disclosure lists AAID among automatically collected/shared identifiers.

The proposed Play Advertising ID declaration is **Yes — used for advertising**, subject to the wording shown in the current Console: `MANUAL PLAY CONSOLE VERIFICATION`. Device IDs must also be represented in Data Safety. If the final audience includes children, child treatment prevents transmission and a solely child-directed configuration targeting 33+ requires reassessing/removing the permission. Do not change the permission before the audience choice.

## UMP and privacy options

Implementation findings:

- consent information is refreshed once on app startup;
- `loadAndShowConsentFormIfRequired` handles required forms;
- a privacy-options entry is exposed only when UMP reports it required;
- ad initialization/loading is gated by `canRequestAds()`; and
- cached ads are cleared if eligibility is lost.

This matches the documented UMP flow. Production readiness still depends on creating/publishing the correct Google Privacy & Messaging messages, using real AdMob IDs, and device testing each applicable region and choice path. Previous-consent behavior and failure paths must also be exercised because `canRequestAds()` can rely on a valid prior state.

## Play Console declaration preparation

| Topic | Proposed factual response | Evidence | Decision required |
| --- | --- | --- | --- |
| Contains ads | Yes | GMA packaged; interstitial/rewarded infrastructure | No; final Console entry manual |
| Target audience | Unanswered; select only intentionally designed age groups | No approved product decision | Yes — target audience |
| Data Safety | Yes, GMA categories listed above are collected/shared | GMA disclosure and package | Console verification; audience can change treatment |
| Privacy policy | EN/TR drafts updated for local data, GMA, UMP, AAID, Blueborn Games, and support contact | Repository documents | Legal controller name, effective date, live HTTPS URL |
| Advertising ID | Proposed Yes, advertising purpose | Merged permission and GMA behavior | Console wording/manual verification; audience may change manifest decision |
| Content rating | Use factual inventory below; no rating assigned here | Source, art, listing | Complete IARC questionnaire |
| App access | No account, login, membership, or restricted area; all game content is directly accessible | Game implementation | Confirm current build in Console |
| In-app purchases | No | No billing SDK; local Pearls only | No |
| Countries/regions | Unanswered | No launch decision in repository | Yes — countries |
| Store languages | EN and TR drafts exist; game UI supports English and Turkish | Assets and store files | Yes — final listing/policy languages |

## Content-rating questionnaire input

- Fantasy/cartoon violence: harpoon attacks against hostile sea creatures and a boss; impact particles/flashes; no blood or gore.
- Weapons: a harpoon and upgrades including rapid fire and piercing.
- Enemies/boss: fish, sharks, piranhas, eels, sea mines, and a large multi-stage Abyssal Octopus.
- Fear/horror: underwater danger, oxygen depletion, damage/failure, darker boss presentation, and tense approach warning; no graphic horror imagery.
- Gambling/loot boxes: none.
- Sexual content/nudity: none observed.
- Profanity: none observed.
- Drugs, alcohol, tobacco: none observed.
- User interaction, chat, UGC: none.
- Location sharing: none in product features; GMA may infer approximate location from IP for SDK purposes.
- Purchases: no real-money or in-app purchases; local Pressure Pearls are earned through gameplay.
- Ads: yes in the Android production plan; interstitial technical path exists, rewarded player-facing path disabled.

`CONTENT RATING QUESTIONNAIRE INPUT PREPARED`

## Countries and languages

No explicit country/region choice exists: `PUBLISHING COUNTRIES DECISION REQUIRED`.

EEA, UK, and Switzerland distribution makes the configured certified-CMP consent flow material; relevant US states can require state-privacy messages. Countries also affect legal definitions of a child and age-of-consent treatment. Continue using UMP/Google Privacy & Messaging for supported geographic flows; do not build a custom geo-consent system.

The game UI supports English and Turkish. English and Turkish store listings and privacy-policy drafts exist, but the final Play Store language selection remains a product decision: `STORE LANGUAGE DECISION REQUIRED`.

## SDK policy inventory

| SDK/component | Role | User-data/policy relevance |
| --- | --- | --- |
| GMA Next-Gen 1.4.0 | Ads and ad requests | Processes the four disclosed data categories; material to Ads, Data Safety, Families, consent, and AAID declarations |
| UMP 4.0.0 | Consent/privacy messages and request eligibility | Material to regional consent and privacy-options behavior; no AAID use since UMP 2.2.0 |
| Google networking/identity dependencies (Cronet, OkHttp bridge, ads identifier, App Set ID, Play services) | Transitive GMA support | Identifier/network components support the disclosed GMA behavior; core Play services libraries do not independently establish additional end-user categories without a using SDK |
| AndroidX WorkManager/Room/DataStore/Lifecycle/Startup/Core and Kotlin/coroutines/Tink/Protobuf | Transitive scheduling, persistence, lifecycle, cryptography, runtime | Functional/transitive components; no evidence that they independently send first-party gameplay data off device |
| libGDX and gdx-controllers/native runtime | Game/render/input/runtime | Functional game runtime; local Preferences store game state; no first-party telemetry path |

## Merged-manifest permission audit

| Permission | Contributing dependency/use | Runtime use | Sensitive/restricted? | Play implication | Status |
| --- | --- | --- | --- | --- | --- |
| `INTERNET` | GMA/Cronet and Android app | Ads, consent, network transport | Normal | Supports disclosed SDK flows; contains-ads/privacy/Data Safety | `CONFIRMED` |
| `ACCESS_NETWORK_STATE` | GMA/Cronet/WorkManager | Connectivity state for network work | Normal | No standalone runtime prompt; document network dependency | `CONFIRMED` |
| `READ_BASIC_PHONE_STATE` | GMA Next-Gen | Basic telephony/network/software state available on supported Android versions | Normal permission, not runtime dangerous | Covered by final SDK/data review; no separate Play permission form identified | `CONFIRMED` origin; `MANUAL PLAY CONSOLE VERIFICATION` final bundle |
| `com.google.android.gms.permission.AD_ID` | GMA Next-Gen | Access to AAID when available | Policy-sensitive identifier permission | Advertising ID declaration and Device IDs Data Safety entry | `CONFIRMED`; audience consequence pending |
| `WAKE_LOCK` | App `useWakelock=true` and WorkManager | Keeps screen/processor awake during app/work | Normal | No special Play declaration; validate battery behavior | `CONFIRMED` |
| `FOREGROUND_SERVICE` | WorkManager transitive manifest | Supports WorkManager foreground execution; first-party code enqueues no worker | Normal permission with policy-governed actual service use/types | Verify bundle/actual use and Console FGS declaration at submission | `MANUAL PLAY CONSOLE VERIFICATION` |
| `${applicationId}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | AndroidX Core | Signature-protected internal dynamic receivers | App-defined signature permission | No public sensitive-permission declaration | `CONFIRMED` |

`RECEIVE_BOOT_COMPLETED` appears in WorkManager's source manifest but is removed by the GMA manifest merger directive and is absent from the final merged manifest. No permission is safely removable solely from the current evidence. Reassess `AD_ID` after the audience decision and reassess dormant WorkManager/FGS components only through supported SDK dependency/configuration changes.

## Documentation consistency and blockers

This phase updates both privacy drafts and the Data Safety worksheet, replaces the in-game implication that no analytics-related processing occurs, and uses the current boss name in store text. “Core gameplay works offline” remains accurate; advertising and consent require a network. The project has no standalone analytics SDK, but GMA's own analytics-purpose collection must always remain disclosed.

The existing desktop boss screenshot still displays the old Leviathan label and must be recaptured before store use. Google Play phone screenshots remain a physical-device task.

Existing blockers remain:

- eight audio assets lack complete commercial-rights evidence;
- AI boss provider/distribution terms are unresolved;
- JLayer/JOrbis notice/legal packaging review is pending;
- physical Android performance, lifecycle, safe-area, accessibility, UMP, and advertising QA is pending;
- real AdMob configuration and production IDs are pending; and
- production signing/upload key configuration is pending.

These items are not passed by this policy audit.
