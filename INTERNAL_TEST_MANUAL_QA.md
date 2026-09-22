# Project Blue: Deep Drift — Internal Test Manual QA

Target Play build: `com.game.diver.deepdivedrift`, version `1.0.58` (`versionCode 59`).

This checklist describes the current Google Play Internal Testing build and its source-implemented behavior. Record PASS/FAIL, device, Android version, network, language, and a short note for every failed item. Capture logcat for every crash or ANR. The previously planned multi-hour Phase 11 endurance pass remains deferred; the stability session here is 20–30 minutes.

## Installation and update

- [ ] Join the Internal Testing track with the tester Google account and install **Project Blue: Deep Drift** from its Google Play test link. Do not sideload for this pass.
- [ ] In Android app info or with `adb shell dumpsys package com.game.diver.deepdivedrift`, confirm package `com.game.diver.deepdivedrift`, `versionName=1.0.58`, `versionCode=59`, and installer `com.android.vending`.
- [ ] Cold-launch from the launcher. Confirm no crash, black screen, ANR, or permanently stuck loading UI.
- [ ] If testing an update over an older internal build, note the old version, update through Play without uninstalling, and confirm language, audio/display settings, high score, achievements, Pressure Pearls, equipment, suits, difficulty, and challenge selections remain present.
- [ ] Force-close and relaunch once before changing any saved value. Confirm the existing save loads.

## First launch and localization

- [ ] On a clean install, confirm **Choose Language / Dil Seçin** appears before the main menu.
- [ ] Choose English, confirm, complete the startup sound choice if shown, and verify the main menu is English.
- [ ] Open **Options > Language**, switch English → Türkçe, and inspect the main menu, Dive Setup, Dive Shop, Achievements, Options, Controls, About/Legal, pause screen, upgrade choice, and result screen.
- [ ] In Turkish, specifically find clear, unbroken examples of `Ş/ş`, `Ğ/ğ`, `İ/ı`, `Ç/ç`, `Ö/ö`, and `Ü/ü`. Useful screens include **Ayarlar**, **Müzik**, **Ses Efektleri**, **Ekran Sarsıntısı**, **Parlama Efektleri**, **Metin Boyutu: Büyük**, **Dil: Türkçe**, and the Turkish challenge/suit descriptions.
- [ ] Switch Türkçe → English. Force-close and relaunch; confirm English persists. Repeat with Turkish selected and confirm Turkish persists.
- [ ] Confirm no missing-square glyphs, corrupted accents, clipped labels, or stale mixed-language text after either live switch.

## Main menu

- [ ] **Play**: opens Dive Setup after the transition.
- [ ] **Dive Shop**: opens both Equipment and Dive Suits tabs; Back returns to the menu.
- [ ] **Achievements**: opens the 40-entry achievement list; scrolling/navigation and Back work.
- [ ] **Sound** row: tapping the row mutes/unmutes all audio. The `−` and `+` controls change master volume in 25% steps from 0% through 100%; the displayed percentage and sound output agree and persist after restart.
- [ ] **Controls**: opens the touch-control reference and Back returns to the menu. Confirm Swim, Fire, Dash, Pause, Help, Navigate, Select, Change, and Back are listed.
- [ ] **Options**: opens without a crash. This is the primary regression path for build 1.0.58/59.
- [ ] Android Back from the main menu exits cleanly without an ANR.

## Options / Settings

- [ ] Cold-launch, enter Options, press Back, and repeat at least 10 times. Confirm every open renders all rows and never crashes.
- [ ] Toggle **Music** OFF/ON. Confirm background music stops/starts and the value persists after leaving Options, background/resume, and force-close/relaunch.
- [ ] Toggle **Sound Effects** OFF/ON. Confirm selection, confirm, harpoon, hit, oxygen, breath, and game-over effects follow the setting and it persists.
- [ ] Toggle **Screen Shake** OFF/ON. In gameplay, take damage in each state; camera shake must be absent when OFF and visible when ON.
- [ ] Toggle **Flash Effects** OFF/ON. Compare damage flash, low-oxygen warning, enemy/boss hit flashes, and blinking hazards; flashing must be suppressed when OFF without hiding gameplay objects.
- [ ] Toggle **Text Size** DEFAULT/LARGE. Inspect every Options row, Controls, Dive Setup, Dive Shop, Achievements, pause, upgrade selection, HUD, and result text for clipping or overlap; confirm persistence.
- [ ] Toggle **Language** EN → TR → EN and run the localization checks above.
- [ ] Open **Controls** from Options and return; confirm Options state and selected language remain intact.
- [ ] Open **About / Legal**, verify version `1.0.58`, notices, privacy text, and Back.
- [ ] If **Privacy Options** is visible, open it, close it with and without making a change, and confirm audio/game rendering resumes. It is shown only when UMP reports that privacy options are required.
- [ ] Press Android Back and the on-screen Back row. Both must return to the main menu.
- [ ] Leave Options onscreen, press Home for at least 30 seconds, return, and verify the screen and values survive. Lock/unlock once while Options is open and repeat.

## Dive Setup, difficulty, and challenge modifiers

- [ ] Select **Easy**: confirm slower threats, softer damage, slower oxygen drain, stronger run-upgrade effects, and a lower reward multiplier than Normal.
- [ ] Select **Normal**: confirm the standard balance and `1.00x` reward multiplier.
- [ ] Select **Hard**: confirm faster threats, heavier damage, faster oxygen drain, weaker run-upgrade effects, and a higher reward multiplier than Normal.
- [ ] Leave Dive Setup, relaunch, and confirm the chosen difficulty persists.
- [ ] Test **No Harpoon** alone: Fire must never create a harpoon. At the boss, survive the special unarmed endurance flow; do not expect a normal weapon kill.
- [ ] Test **No Oxygen Pickups** alone: no oxygen tanks spawn and the sealed-loop oxygen drain is greatly reduced.
- [ ] Test **No Special Powers** alone: no power-up pickup spawns; oxygen pickups still can.
- [ ] Test **No Upgrades** alone: suit bonuses, permanent equipment, and run-upgrade choices/effects are disabled.
- [ ] Enable all four modifiers together. Confirm the **Total Lockdown** indication and increased reward multiplier, then start and verify all four restrictions at once.
- [ ] Return to Dive Setup after restart and confirm modifier selections persist. Clear every modifier before ordinary balance testing.

## Core gameplay

- [ ] Start a Normal run. Verify touch Swim moves upward while held and releasing lets the diver sink; movement remains controllable at the screen edges.
- [ ] Fire repeatedly. Confirm harpoons originate at the diver, respect cooldown/active-shot limits, hit enemies, and disappear appropriately.
- [ ] Collide with enemies, enemy shots, and hazards. Confirm oxygen/damage changes once per valid hit, temporary invulnerability works, and the diver cannot become stuck or invisible.
- [ ] Let oxygen drain, collect an oxygen tank, and confirm the meter rises without exceeding capacity. Let oxygen reach zero and confirm the game-over result appears.
- [ ] Kill enemies and confirm score increases. Chain at least 3 quick kills and confirm combo/scoring rises (combo multiplier starts above one kill and caps at 3x).
- [ ] Collect and verify each special power: **Pressure Shield** blocks damage for about 7 seconds; **Time Bubble** slows enemies/hazards for about 5 seconds; **Magnetic Current** pulls pickups for about 10 seconds; **Harpoon Overdrive** speeds and pierces harpoons for about 8 seconds; **Torpedo Dash** stores a dash charge and activates only when Dash is pressed.
- [ ] Pause with the on-screen pause control, use Resume, open Controls/help if offered, and return to menu. Start another run and verify Android Home/resume enters a safe paused state.
- [ ] On game over, use Retry and confirm a clean new run: score, stage, entities, powers, combo, and oxygen reset. On a later result use Menu and confirm the main menu returns.

## Run upgrades

Upgrade choices occur at 1,000, 2,500, and 5,000 score, then every additional 2,500 points. Pick each category in separate runs or successive choices and compare before/after behavior.

- [ ] **Rapid Fire**: hold/repeat Fire and verify the harpoon cooldown drops by 18% per level, up to its useful cap.
- [ ] **Piercing**: line up enemies and verify a harpoon passes through one additional target per level.
- [ ] **Air Recycler**: compare oxygen loss over the same measured time; drain drops by 15% per level.
- [ ] **Pressurized Tanks**: compare an oxygen pickup before/after; each pickup restores 10 more oxygen per level, up to its useful cap.
- [ ] **Hydro Fins**: compare ascent/sink responsiveness; swimming agility rises by 12% per level.
- [ ] Confirm choosing an upgrade resumes the same run and the effect ends when the run ends. With **No Upgrades**, confirm the choice screen/effects do not occur.

## Five-stage continuous run

There is no stage-select or per-stage unlock menu in the player build. Reach every stage by surviving one continuous run. Each stage lasts 180 seconds by default; the finale becomes ready after stage 5, roughly 15 minutes after starting, before boss time.

- [ ] Stage 1 **Sunlit Reef**: start any run; verify its title/depth banner, baseline spawn rate, enemies, pickups, hazards, and transition after about 3 minutes.
- [ ] Stage 2 **Sinking Ruins**: survive the first transition; verify the new title/background/depth, higher threat speed, and no stale stage-1 celebration or entity overlap.
- [ ] Stage 3 **Current Maze**: survive the second transition and verify the next visual/difficulty step and continued scoring/pickups.
- [ ] Stage 4 **Blackwater Trench**: survive the third transition and verify increased enemy/hazard pressure without a freeze or lost controls.
- [ ] Stage 5 **Abyssal Rift**: survive the fourth transition, verify its title/background and final-stage damage behavior, then survive until the boss warning/entry.
- [ ] At every transition, confirm the completed-stage achievement (first time only), safe celebration interval, HUD stage/depth update, clean entity clearing, and uninterrupted audio.

## Abyssal Octopus boss

- [ ] Confirm the Abyssal Octopus enters from the right after the Abyssal Rift finale warning and settles into its arena; ordinary spawns do not corrupt the encounter.
- [ ] Above 50% HP, observe all three repeating attacks: a three-shot ink spread aimed around the locked player position, three brood minions, and a telegraphed tentacle lane strike.
- [ ] Below 50% HP, confirm the boss changes color/rage state and its rest/attack cadence becomes faster.
- [ ] Fire into the valid boss hit area and confirm hit feedback and the boss health bar decrease. The normal weapon kill requires 60 hits before piercing/multishot effects.
- [ ] Take an ink, minion, tentacle, or boss collision hit and confirm damage, feedback, and recovery. Boss collision is lethal by design.
- [ ] Die during the fight, then Retry; confirm a clean Stage 1 run. Return to Menu from another boss death and confirm normal result/ad policy behavior.
- [ ] Defeat the boss, confirm victory, **Colossus Falls** and game-completion achievement handling, Pressure Pearl award, high-score update, and stable post-boss result flow.
- [ ] Press Menu after victory and confirm return to the main menu, with an interstitial only if the exact AD 1 policy below is eligible.

## Pressure Pearls, permanent equipment, and suits

- [ ] Complete or lose a run after travelling at least 100 m. Confirm the result awards Pressure Pearls based on whole hundreds of metres and the displayed run reward multiplier; retrying/result navigation must not duplicate one run’s reward.
- [ ] Restart and verify the Pearl wallet persists.
- [ ] In **Dive Shop > Equipment**, buy and practically verify all five categories: **Pressure Tank** (+10 starting oxygen/level), **Reinforced Suit** (+1.5 seconds starting shield/level), **Magnetic Clasp** (+35 pickup magnet range/level), **Twin Launcher** (+1 starting harpoon pierce/level), and **Salvage Map** (+15% Pearl reward/level).
- [ ] Confirm each has three levels, rising costs, and real-time installation durations of 5, 10, and 15 minutes for levels 1, 2, and 3. Close/restart during an install and confirm its timer and completion persist, including time spent offline.
- [ ] In **Dive Suits**, verify **Tideline Blue** is initially available and reloads the harpoon 10% faster.
- [ ] Earn/spend 20 Pearls to unlock/select **Salvage Green**; verify +90 pickup magnet range.
- [ ] Earn/spend 45 Pearls to unlock/select **Rescue Red**; verify +25 starting oxygen.
- [ ] Earn/spend 80 Pearls to unlock/select **Abyss Black**; verify +15% swimming agility.
- [ ] For all four suits, verify unlock, selection, appearance, bonus, and restart persistence. With **No Upgrades**, confirm suit and equipment bonuses are suppressed for that run.

## Achievements

The system contains 40 achievements covering cumulative kills, combos, single-run score/distance, oxygen and power pickups, each stage, full completion/boss, flawless/no-shot/no-collection/no-power play, each difficulty, and challenge/lockdown completions. Do not require earning all 40 in one manual pass.

- [ ] **First Blood**: kill one enemy.
- [ ] **Triple Strike**: reach a 3-kill combo.
- [ ] **Fresh Air**: collect the first oxygen tank.
- [ ] **Power Surge**: collect the first special power.
- [ ] **Getting Your Fins Wet**: travel 100 m in one run.
- [ ] **Five Figures**: reach 10,000 score in one run.
- [ ] **Reef Diver**: complete Sunlit Reef.
- [ ] **Challenger**: complete any stage with at least one challenge modifier.
- [ ] **Colossus Falls**: defeat the Abyssal Octopus.
- [ ] For each tested achievement, verify one in-game unlock notification, unlocked state in Achievements, no duplicate notification, correct language after switching, and persistence after force-close/relaunch and Play update.

## Lifecycle, audio, display, and network

- [ ] During menu, Options, active gameplay, pause, upgrade selection, and result screens: press Home/use Recents, wait, and return. Confirm stable rendering/input and appropriate pause behavior.
- [ ] During gameplay, lock for at least 30 seconds and unlock. Confirm no oxygen/enemy time jump, stuck touch, duplicate audio, or crash.
- [ ] If practical, receive/dismiss a phone call or notification interruption and verify audio pauses/resumes once and gameplay remains paused/safe.
- [ ] Force-close during menu and during a paused run, relaunch, and confirm persistent settings/progression are intact; an active run is not promised as resumable.
- [ ] Test music, every listed SFX, main-menu mute, 0/25/50/75/100% master volume, interruption/resume, and Bluetooth/wired output if available.
- [ ] On this device and at least one different aspect ratio, inspect notch/cutout, rounded corners, gesture and three-button navigation bars, safe-area placement, touch targets, clipping, and Turkish/large-text overflow.
- [ ] The Android activity is landscape/fullscreen. Rotate the physical phone while running and confirm Android keeps the intended landscape layout without recreating into a broken portrait state.
- [ ] Run on normal Wi-Fi, mobile data if practical, airplane mode, network loss during gameplay, and restored network. Core menus, settings, save/progression, and gameplay must remain usable offline; only consent/ad loading may be unavailable.

## AD TEST MAP

The installed 1.0.58/59 Play APK contains Google’s official demo App ID `ca-app-pub-3940256099942544~3347511713`, rewarded ID ending `5224354917`, and interstitial ID ending `1033173712`. Google demo ads should be visibly marked as test/demo inventory. Never treat an unmarked live ad as expected in this build.

### AD 1 — Interstitial

- **Format:** Interstitial.
- **Source:** `DeepDiveDrift.returnToMenuFromResults`, `InterstitialPolicy`, and Android `AndroidAdvertisingService`.
- **Where:** Only the transition from a completed result screen (death or victory) to the main menu.
- **How tester triggers it:** Complete three runs in the same app session (a death or victory calls `endGame` and counts once), then press **Menu** on the third result while online, after UMP permits ads and the interstitial has loaded. The first eligible display does not require an initial 10-minute wait. After an interstitial actually displays, complete at least three more runs and wait at least 10 minutes from that display before pressing Menu on another result.
- **Automatic/user initiated:** Display is automatic after the user presses Menu on an eligible result; there is no “watch ad” choice.
- **Frequency/cooldown:** At least 3 completed runs since the previous displayed interstitial, at least 10 minutes since that display, one evaluation per result-to-menu opportunity, never during active gameplay, never while another fullscreen item is active. A displayed rewarded test suppresses the next interstitial opportunity.
- **Expected result:** Google demo interstitial opens fullscreen, gameplay/audio lifecycle pauses, close returns to the main menu, and a fresh ad begins loading.
- **Failure fallback:** If consent disallows requests, no ad is cached, offline loading fails, Activity is unavailable, another fullscreen item is active, or show fails, return to the main menu immediately. Core gameplay remains available. Load/show failures are logged; no reward exists.
- **Test/demo ID:** Yes, official Google interstitial demo ID ending `1033173712`.
- **Consent dependency:** Yes. UMP must report that ads can be requested, the required consent form must no longer be pending, and the SDK/cache must be ready.

### AD 2 — Rewarded technical QA path

- **Format:** Rewarded.
- **Source:** `AndroidLauncher.startRewardedQaIfRequested`, `DeepDiveDrift.showRewarded`, and Android `AndroidAdvertisingService`.
- **Where:** No normal player-facing screen or button. It is a TEST-build-only launch-intent QA hook.
- **How tester triggers it:** Force-stop the app, then run `adb shell am start -n com.game.diver.deepdivedrift/com.game.diver.android.AndroidLauncher --ez deepdive.qa.rewarded true`. Resolve any required UMP form. The launcher polls for a cached rewarded ad for up to about 30 seconds.
- **Automatic/user initiated:** QA-initiated through ADB; once requested, it opens automatically when cached.
- **Frequency/cooldown:** No player-facing frequency system. One launch-extra request polls 60 times at 500 ms. If it opens, the next interstitial opportunity is suppressed.
- **Expected result:** A Google demo rewarded ad opens. Watching to the reward event logs `earned`; closing logs `closed` and resumes the game.
- **Gameplay reward/benefit:** None. The callback explicitly logs that no gameplay benefit is granted. There is currently no real player-facing rewarded benefit.
- **Failure fallback:** If unavailable/offline/blocked by consent, it times out and logs a warning; the game continues and grants nothing. Show failure destroys the ad, closes the fullscreen gate, and reloads when allowed.
- **Test/demo ID:** Yes, official Google rewarded demo ID ending `5224354917`.
- **Consent dependency:** Yes, identical UMP/load gate to interstitial.

### Consent / privacy flow

- [ ] On ordinary launch, UMP refreshes consent information. If a required form exists, it appears before ads load; gameplay must remain stable if the form or network fails.
- [ ] After UMP says ads can be requested, GMA initializes and caches one rewarded and one interstitial. Refusing/non-eligibility must prevent requests as directed by UMP without blocking gameplay.
- [ ] **Privacy Options** appears in Options only when UMP marks it required. Opening/closing it must pause/resume fullscreen lifecycle cleanly and refresh consent/ad state.
- [ ] Optional TEST-only forced geography requires a valid hashed test-device ID and ADB extras `deepdive.qa.ump_reset`, `deepdive.qa.ump_geography`, and `deepdive.qa.ump_test_device`; do not use an arbitrary ID or assume natural geography will show a form.
- [ ] With airplane mode, both ad types may fail to cache/show; the result-to-menu flow and all core gameplay must still work. Restore the network and confirm loading can recover on resume/after a fullscreen close.

### Formats not implemented

- **Banner:** Not implemented. In-game status “banners” are game UI, not ads.
- **Rewarded interstitial:** Not implemented.
- **App open:** Not implemented.
- **Native:** Not implemented.
- **Other ad formats:** Not implemented.

## Save compatibility and stability

- [ ] Before updating, record language, master/music/SFX values, screen shake, flash effects, text size, selected difficulty/modifiers, high score, achievement count, Pearl balance, every equipment level/timer, unlocked suits, and selected suit.
- [ ] Update through Google Play without clearing data. Recheck every recorded value and complete one run; confirm no migration reset, enum-name corruption, duplicated reward, or invalid selected suit.
- [ ] Restart twice, background/resume, lock/unlock, force-close/relaunch, and reboot the phone if practical. Confirm persistence after each.
- [ ] Play continuously for 20–30 minutes, include multiple stage transitions, repeated menu/Options visits, pickups/upgrades, pause/resume, at least one death/retry, and a result-to-menu path. Watch filtered logcat for `AndroidRuntime`, `FATAL EXCEPTION`, `libGDX`, package process death, and ANR entries.
- [ ] End the session by confirming no new crash/ANR/native tombstone and no save regression. The multi-hour Phase 11 endurance test is deferred and is not part of this checklist.
