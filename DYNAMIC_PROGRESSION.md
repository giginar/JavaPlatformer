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
finale. A desktop test run can override it without changing source code:

```powershell
java -jar .\lwjgl3\target\DeepDiveDrift-1.0.0.jar --autostart --stage-duration=15
```

Accepted values are 5–600 seconds. `F2` advances to the next stage and `F3` summons the boss
when `--debug` is also supplied.

Distance uses the 64-world-unit diver sprite as a 1.8 m reference and the authored background
scroll speed. The first milestones are 500 m, 1 km, and then every whole kilometre. Stage and
milestone celebrations keep the simulation moving but clear active threats and suppress new
ones for 2.7 seconds.

Temporary pickups are Pressure Shield, Time Bubble, Magnetic Current, Harpoon Overdrive, and
Torpedo Dash. A completed dive awards one Pressure Pearl per 100 m (modified by Salvage Map).
The Dive Shop persists pearl balance and permanent equipment levels in the existing game
preferences.

Each transition increases the displayed physical depth, moves the scenery upward, reduces
available light, and accelerates rising silt to sell the feeling of descending. After stage five,
the full-height Abyssal Octopus alternates aimed ink salvos, thrown broodlings, and telegraphed
tentacle lane sweeps. Ink bursts temporarily obscure the playfield. Any positive damage in stage
five consumes all remaining oxygen; an active shield still prevents the hit from becoming damage.
