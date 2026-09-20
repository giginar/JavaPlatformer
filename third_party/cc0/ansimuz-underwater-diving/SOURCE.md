# Ansimuz — Underwater Diving

- Author: Luis Zuno (Ansimuz)
- Asset page: https://ansimuz.itch.io/underwater-diving
- License shown by the author: Creative Commons Zero v1.0 Universal (CC0-1.0)
- AI disclosure shown by the author: No generative AI was used
- Accessed: 2026-08-08
- License copy: [`../CC0-1.0.txt`](../CC0-1.0.txt)

The author page lists the tileset, parallax background, player sprites, enemy sprites,
and effects as parts of the same CC0 asset pack. The source PNGs retained here were
downloaded from the pack's public browser demo at:

`https://html-classic.itch.zone/html/768850/demo/media/`

## Retained source files

| File | SHA-256 |
| --- | --- |
| `background.png` | `CFF6C72694646A3707DA22A6A56ACBCCC22CB2427C051F5B89875F29592DA192` |
| `props.png` | `D8171F40E1F0C04BBFAC2F83A127F8D7C468E2CB0C907271436D4D8118CBEBD4` |
| `entities-player.png` | `3D66D558F7C85606A9E1D53A21C0DC998F86883118E06E42001C4AD6C34C737B` |
| `entities-fish.png` | `F133A275B234D719ECC6E0F07107A6C0755C2FA70C105397EE7E6CB6F842AC27` |
| `entities-fish-big.png` | `693DB9F043CEC6DD20CEB953FCEA794A7CDC6569572893B6BD5596C85D06EF4C` |
| `entities-fish-dart.png` | `FBBFF7C1E82381882BEF0E98FA4897833C01206FAD1F5FE7F1EF4ADE7D3DF97D` |

`scripts/build-licensed-art.ps1` crops, flips, and composes these sources to create
the shipped sprites, background, icons, and storefront images. The harpoon, oxygen
tank, bubbles, light rays, and simple layout geometry are original deterministic
drawings made by that script. Storefront text uses the separately licensed Orbitron
font distributed with the game.

The retained background source was imported with the other verified CC0 files in
Git commit `edd77507e620db125dec14109f323061ac49341d`. A later cleanup removed it
while the deterministic build still referenced it. Phase 4 restored that exact Git
blob and verified the SHA-256 above; no external file was downloaded.
