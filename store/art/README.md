# Store art sources

All current source artwork in this directory is generated deterministically by
`scripts/build-licensed-art.ps1`. No generative-AI image is used in the current
storefront artwork.

The compositions use only:

- CC0 sprites and environment art from Ansimuz's **Underwater Diving** pack;
- original geometric elements drawn by the build script;
- the Orbitron typeface distributed under SIL Open Font License 1.1.

Full upstream URLs, the access date, retained source files, and their SHA-256 hashes
are recorded in `third_party/cc0/ansimuz-underwater-diving/SOURCE.md`. The CC0 legal
text is retained at `third_party/cc0/CC0-1.0.txt`.

## Generated source images

- `key-art-master-v1.png`: unbranded 1920×1080 landscape master
- `key-art-branded-landscape-v1.png`: branded 1920×1080 landscape master
- `key-art-branded-portrait-v1.png`: branded 1200×1800 portrait master
- `title-wordmark-source-v1.png`: flat chroma-key wordmark source
- `title-wordmark-v1.png`: transparent wordmark

## Rebuild

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-licensed-art.ps1
```

The script also rebuilds the in-game PNG files, desktop/mobile icons, Google Play
art, Steam capsules, library art, and page art. `scripts/export-store-art.ps1` can be
run separately when only exact storefront export dimensions need to be refreshed.
