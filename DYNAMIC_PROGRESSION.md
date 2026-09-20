# Dynamic run and progression tuning

The run is split into five time-based depth stages. Each stage changes the background tint,
enemy roles, encounter combinations, hazard pool, spawn cadence, and reaction windows.

| Stage | Name | Newly emphasized threats |
| --- | --- | --- |
| 1 | Sunlit Reef | Small fish and low/high reef shelves |
| 2 | Sinking Ruins | Fast fish, ranged electric eels, falling rocks |
| 3 | Current Maze | Multi-lane formations, piranha swarms, sea mines |
| 4 | Blackwater Trench | Shark combinations, crushing gates, thermal vents |
| 5 | Abyssal Rift | Mixed role gauntlets, one-hit damage, and the Abyssal Octopus finale |

The default stage duration is `180` seconds, producing a roughly 15-minute route before the
finale. An explicit development build can override it for a short desktop test run:

```powershell
java -jar .\lwjgl3\target\DeepDiveDrift-1.0.0.jar --autostart --stage-duration=15
```

Accepted values are 5–600 seconds when the internal development gate is enabled. Runtime
arguments cannot enable progression shortcuts in the production build. In a development build,
`F2` advances to the next stage and `F3` summons the boss when `--debug` is also supplied.

Distance uses the 64-world-unit diver sprite as a 1.8 m reference and the authored background
scroll speed. The first milestones are 500 m, 1 km, and then every whole kilometre. Stage and
milestone celebrations keep the simulation moving but clear active threats and suppress new
ones for 2.7 seconds.

Temporary pickups are Pressure Shield, Time Bubble, Magnetic Current, Harpoon Overdrive, and
Torpedo Dash. A completed dive first floors distance to one base Pressure Pearl per full 100 m.
The run multiplier is the selected difficulty multiplier plus `0.20` for each active challenge.
The completed Salvage Map multiplier is then applied, and the combined result is rounded once to
the nearest integer. Reward and wallet arithmetic saturate at the integer limit rather than
wrapping negative.

Each dive and retry receives a persisted increasing run sequence. Completion records the most
recently rewarded sequence in the same preferences update as the wallet credit, so repeated end,
retry, or menu flows cannot credit the same run again.

Each shop equipment item has three permanent levels. Buying levels 1, 2, and 3
starts a 5-, 10-, and 15-minute installation respectively; pearls are charged when
installation starts. A saved completion timestamp lets installation continue offline.
Only one level per item can be installing at a time, while different items can install
concurrently. Previously completed levels remain usable during the next installation.
At the start of each dive (including retries), equipment bonuses are captured for that
entire dive, including its Salvage Map reward. Finishing an installation cannot change
the ongoing dive. The NO UPGRADES challenge uses no equipment bonuses.

Existing purchased levels remain completed. Legacy fourth levels are reduced to three
with a one-time refund of the fourth-level pearl cost.

The primary `DeepDiveDriftPrefs` store now carries `save.schemaVersion = 1`. A missing version is
legacy version 0. Migration retains the existing enum-backed keys, installations, suits,
achievements, career counters, difficulty, and challenge settings. Migrations run in numeric order,
write the new version only after a successful step, and are safe to repeat. Invalid fields are
repaired individually: negative currency/counters and out-of-range equipment levels are clamped,
invalid enum selections fall back to existing defaults, and invalid non-positive installation
timestamps are removed without resetting unrelated data.

Future additions should append stable enum values and keys rather than rename existing values.
New equipment needs a new `progression.level.<NAME>` and installation key; new suits need an unlock
key and must preserve `progression.suit.selected` fallback behavior. A selectable weapon, pilot, or
vehicle system would also need a stable selection key, explicit legacy default, migration tests,
and shop/setup UI. Achievement additions can continue using `achievement.unlocked.<NAME>` plus a
non-negative counter only when a persistent aggregate is required.

Run upgrades are offered at 1,000, 2,500, and 5,000 score, then every additional 2,500 score
for the rest of the dive. Upgrades stop appearing once their effect reaches a meaningful cap,
and choices tied to mechanics disabled by a challenge are excluded. Run levels reset whenever
a new dive starts. Dive Shop equipment remains separate and persists between dives.

Each transition increases the displayed physical depth, moves the scenery upward, reduces
available light, and accelerates rising silt to sell the feeling of descending. After stage five,
the full-height Abyssal Octopus alternates aimed ink salvos, thrown broodlings, and telegraphed
tentacle lane sweeps. Ink bursts temporarily obscure the playfield. Any positive damage in stage
five consumes all remaining oxygen; an active shield still prevents the hit from becoming damage.
