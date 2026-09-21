# Phase 12 — Privacy policy, store text, and release visual assets

Date: 2026-09-21
Status: **CONDITIONALLY READY**

The English and Turkish release drafts are factually aligned with the current Android
implementation. The public product identity and publisher brand are final. Publication
still depends on legal/policy inputs, Pages activation, production configuration,
rights clearance, and real Android screenshot capture.

## Store identity

- Final public/store name: `Project Blue: Deep Drift`
- Public publisher/studio brand: `Blueborn Games`
- Support contact: `ykucukcinar@gmail.com`
- Android application ID: `com.game.diver.deepdivedrift`

The final name is used by the Android label, desktop window title, in-game menu and
About/Legal text, listings, privacy drafts, README, and generated title artwork.
Historic Java class names, artifact IDs, filenames, and implementation packages remain
unchanged because they are technical identifiers rather than public metadata.

## Google Play text limits and counts

Google Play currently permits 30 characters for the app name, 80 for the short
description, and 4,000 for the full description. Counts below normalize line endings
to a single character and include the line breaks/bullet markers in the full copy.

| Draft | App name | Short description | Full description | Result |
| --- | ---: | ---: | ---: | --- |
| English | 24 / 30 | 64 / 80 | 1,504 / 4,000 | Within limits |
| Turkish | 24 / 30 | 67 / 80 | 1,572 / 4,000 | Within limits |

Official source: [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en-NZ).

The Turkish listing is a localized store draft. It does not claim a Turkish in-game
interface; the current in-game UI is English. `STORE LANGUAGE DECISION REQUIRED`.

## Feature claim audit

| Claim | Status | Evidence / action |
| --- | --- | --- |
| Five-stage run | `VERIFIED` | `GameBalance.STAGES` defines Sunlit Reef, Sinking Ruins, Current Maze, Blackwater Trench, and Abyssal Rift. |
| Abyssal Octopus boss | `VERIFIED` | Current code and achievements use Abyssal Octopus and implement a multi-stage boss. Marketing screenshots containing the asset remain rights-blocked. |
| Four dive suits | `VERIFIED` | `DiverSuit` defines Tideline Blue, Salvage Green, Rescue Red, and Abyss Black. |
| Run upgrades | `VERIFIED` | Runs present upgrade choices affecting harpoon, oxygen, and movement behavior. Copy uses functional wording rather than promising unimplemented rewards. |
| Permanent equipment | `VERIFIED` | `PermanentUpgrade` defines five locally persisted equipment lines with three levels. |
| 40 achievements | `VERIFIED` | `Achievement` contains 40 entries and `AchievementStore` persists unlocks locally. |
| Four challenge modifiers | `VERIFIED` | No Harpoon, No Oxygen Pickups, No Special Powers, and No Upgrades are implemented. |
| Easy, Normal, and Hard | `VERIFIED` | `RunDifficulty` defines three player-selected difficulties. |
| Pressure Pearls | `VERIFIED` | Can be earned through play and spent on local equipment/suit progression; no billing is involved. |
| Underwater arcade survival gameplay | `VERIFIED` | Implemented single-player diver movement, oxygen management, enemies, harpoons, pickups, combos, and stage progression. |
| Core gameplay works offline | `REWORD` | Keep the qualified wording “core gameplay remains available without a network connection.” Ads and consent processing use the network. |
| Fully offline / no network activity | `REMOVE` | False because Google Mobile Ads and UMP perform network processing. |
| Ad-free / no advertising | `REMOVE` | False because the Android app contains ad infrastructure and sparse interstitial capability. |
| Advertising in marketing copy | `REWORD` | Do not clutter the description. Select Yes in Play's Ads declaration so the store displays `Contains ads`; keep technical processing details in privacy/Data Safety material. |
| Rewarded gameplay benefit | `REMOVE` | The technical path exists, but no approved player benefit or player-facing placement exists. `REWARDED BENEFIT DECISION REQUIRED`. |
| No account or in-app purchases | `VERIFIED` | No account, login, billing, or purchase implementation is present. |

Google requires an accurate Ads declaration and automatically shows the `Contains ads`
label when Yes is selected. Official source: [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en).

## Privacy-policy audit

Both privacy drafts distinguish these data paths:

- Local game data: Pressure Pearls, equipment/install state, suits, achievements,
  high score and run completion, difficulty/challenge configuration, and audio,
  display, screen-shake, and flashing-effect settings remain on the device. Current
  run state is in memory. Android application backup is disabled.
- Google advertising and consent processing: GMA Next-Gen and UMP may process IP-based
  approximate location, product interaction, diagnostics, and device/account
  identifiers for advertising, analytics, and fraud prevention as documented by
  Google. Consent status is refreshed at startup; ads are requested only when UMP
  permits them.

The drafts do not claim no internet access, no advertising, that every kind of data
stays on the device, or that there is no third-party processing. They make no legal
compliance guarantee. They remain drafts until the production advertising setup,
audience, countries, languages, effective date, legal controller identity, and live
Pages URL are approved.

`PRIVACY POLICY INPUT REQUIRED`

- publisher/controller legal name (`LEGAL NAME INPUT REQUIRED`)
- active HTTPS privacy-policy URL (`GITHUB PAGES ACTIVATION/URL VERIFICATION REQUIRED`)
- effective date
- account phone/address verification where required by Google Play

The public name, `Blueborn Games` publisher/studio brand, support contact,
`com.game.diver.deepdivedrift` package identifier, and expected Pages URLs are confirmed.
The expected privacy URL is `https://giginar.github.io/JavaPlatformer/privacy.html` and
must not be treated as live until Pages activation and URL verification succeed.

## Screenshot inventory

| File | Subject | Status | Action |
| --- | --- | --- | --- |
| `desktop/01-main-menu.png` | Old desktop main menu | `STALE` | Recapture current Android menu; current menu has additional shipping options. |
| `desktop/02-options.png` | Desktop options | `STALE` | Do not use for phone listing; it shows desktop-only display settings. |
| `desktop/03-gameplay.png` | Early-stage gameplay | `RECAPTURE REQUIRED` | Recapture from current Pixel 8 Android build with final touch UI. |
| `desktop/04-gameplay-action.png` | Harpoon action | `RECAPTURE REQUIRED` | Recapture a clear current Android action scene. |
| `desktop/05-leviathan.png` | Old boss encounter | `DO NOT USE` | Old `ABYSS LEVIATHAN` label and unresolved boss-asset rights. |

Detailed capture order and optional captions are in
[`store/screenshots/README.md`](../screenshots/README.md).

## Screenshot technical requirements

Google Play accepts up to eight screenshots per device type and requires at least two
across device types. Each must be JPEG or 24-bit PNG without alpha, have dimensions
from 320 to 3,840 px, and have a long edge no more than twice the short edge. For game
promotion surfaces, provide at least three 16:9 landscape screenshots at 1920×1080 or
higher that show actual gameplay.

The game is landscape. The existing Pixel 8 AVD captures at native `2400×1080`. That
20:9 image exceeds Play's 2:1 edge-ratio limit and cannot be uploaded unchanged. Export
at `1920×1080`: crop only unused side area when the entire HUD and gameplay remain
accurate, or recapture using a 1920×1080 logical display. Never stretch or compress the
image. Capture after immersive mode settles; if system bars, notifications, or provider
text appear, recapture rather than painting over game content.

Seven or eight phone screenshots are recommended. Optional tablet screenshots must
come from a real tablet layout; for large-screen presentation use at least four 16:9
landscape images between 1,080 and 7,680 px.

Official source: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).

## Screenshot tooling assessment

Existing desktop launch arguments can open the shop, suits, setup, achievements,
options, controls, About/Legal, and gameplay, then capture a framebuffer after a delay.
Boss and shortened-stage shortcuts remain behind the existing development gate, whose
production constant is false. This is enough for composition rehearsal; Android phone
screens must still be captured from the Android build. No new helper or production
shortcut is justified for Phase 12.

## Feature graphic

`store/google-play/assets/feature-graphic-1024x500.png` exists. It is a 1024×500,
24-bit RGB PNG without alpha and meets the mandatory file specification. It uses the
rights-documented CC0/OFL visual chain and does not contain the rights-blocked boss.

Status: **CONDITIONALLY USABLE**. The generated image uses the final
`PROJECT BLUE: DEEP DRIFT` title. Its prominent title must still be checked in Play's
cutoff previews after regeneration.

If revision is required, use this production brief:

- retain the navy/cyan underwater pixel-art palette, diver, harpoon, ruins, bubbles,
  light shafts, and non-boss sea threats;
- export exactly 1024×500 as JPEG or 24-bit PNG without alpha;
- keep the focal action and any final wordmark near the center, with background-only
  detail near edges that may be cropped;
- keep fine detail limited and preserve clear association with the app icon and game;
- omit the boss until rights are resolved, device frames, store badges, rankings,
  testimonials, prices, calls to action, and time-sensitive claims;
- localize any retained branding text if the final store-language plan requires it.

## App icon

Status: **NEEDS POLISH**.

`store/google-play/assets/icon-512.png` is a 512×512, 32-bit PNG with alpha, about
126 KB, contains no debug/test marking, and meets the basic Play upload requirements.
The Android manifest uses the identical 512×512 image from
`drawable-nodpi/deepdive_icon.png`. No `mipmap-anydpi-v26` adaptive-icon XML or separate
foreground/background resources exist, so Android adaptive-mask presentation has not
been prepared or verified. Keep the current diver/harpoon identity; add adaptive
launcher resources and inspect circle, squircle, rounded-square, and teardrop masks
after the final brand/name decision.

## Visual rights

- CC0/OFL icon, feature-graphic, environmental, character, and font source chains are
  recorded.
- Eight audio assets still lack complete commercial-rights evidence. Audio does not
  prevent static screenshot preparation but blocks production distribution.
- The Abyssal Octopus is an AI-generated project asset whose applicable provider and
  distribution terms are not retained or resolved.
- `BOSS SCREENSHOT: RIGHTS BLOCKED`
- The stale boss screenshot must not be uploaded. A current boss screenshot also cannot
  be marked production-ready until the rights gate is cleared.

## Release visual checklist

- [x] High-resolution Play icon draft exists and meets basic upload dimensions/format
- [ ] Final Play icon approved after store-name/brand decision
- [ ] Adaptive Android launcher icon foreground/background and mask review
- [x] Feature graphic draft exists and meets mandatory dimensions/format
- [ ] Feature graphic cutoff review with the final store name
- [ ] Seven or eight current phone screenshots captured from Android
- [ ] At least three 1920×1080 actual-gameplay phone screenshots
- [ ] Optional tablet screenshots, if chosen, captured from a tablet layout
- [ ] Stale boss screenshot excluded/replaced
- [ ] Boss screenshot rights gate cleared before any boss marketing image is used
- [x] Final public/store name selected: `Project Blue: Deep Drift`
- [x] Final-draft English text within Play limits
- [x] Final-draft Turkish text within Play limits
- [ ] Store languages selected; Turkish listing does not imply Turkish in-game UI
- [x] Public publisher/studio brand and support/privacy contact supplied
- [ ] Legal controller name and effective date supplied
- [ ] GitHub Pages manually activated and expected privacy/support URLs verified live
- [ ] Active privacy-policy URL published and checked

## Documentation consistency

- Package is consistently `com.game.diver.deepdivedrift`.
- Public publisher/studio branding is consistently `Blueborn Games`; this is not a
  claim that the brand is an incorporated company or the verified legal controller.
- Expected Pages base URL is `https://giginar.github.io/JavaPlatformer/`.
  `GITHUB PAGES MANUAL ACTIVATION REQUIRED` and
  `GITHUB PAGES ACTIVATION/URL VERIFICATION REQUIRED` remain open.
- Current supported Android floor is `minSdk 24`; `targetSdk 36` is current. No API 21
  claim was found in the audited release materials.
- Phase 9's 150-test count and Phase 10's 151-test count are historical evidence from
  their respective points in time, not contradictory marketing claims; no test count
  was added to store copy.
- Phase 10 correctly distinguishes the normal unoptimized workflow from its separate
  optimized R8 release profile.
- The qualified offline claim is retained. No no-ads or no-third-party-processing
  claim remains in release copy.
- The current boss name is Abyssal Octopus. The single `Leviathan` screenshot is marked
  stale and prohibited from store use.
- About/Legal accurately separates local game data from Google advertising processing
  and already exposes the audio and boss-rights review state.
- Phase 11 long-run/endurance QA and designated low-end sustained-performance QA are
  explicitly deferred, not treated as passed.

## Product decision gates

- `TARGET AUDIENCE DECISION REQUIRED`
- `PUBLISHING COUNTRIES DECISION REQUIRED`
- `STORE LANGUAGE DECISION REQUIRED`
- `REWARDED BENEFIT DECISION REQUIRED`

## Production blockers carried forward

- eight audio assets lack complete commercial-rights evidence
- AI boss provider/distribution terms unresolved
- JLayer/JOrbis notice/legal review pending
- real AdMob configuration pending
- production ad IDs pending
- production signing pending
- unsigned production AAB
- actual 16 KB runtime environment unavailable
- Phase 11 long-run/endurance QA deferred
- designated low-end sustained performance QA deferred
- stale boss screenshot requires replacement

Phase 12 release-text and visual preparation is **CONDITIONALLY READY**. The project may
proceed to **Phase 13 — Upload Key, Production Signing and Closed-Test AAB** while these
gates remain visible; Phase 13 must not represent the app as production-ready or clear
the outstanding rights, configuration, policy, QA, identity, contact, or visual gates.
