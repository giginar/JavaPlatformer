# Hangi betiği çalıştırmalıyım?

Günlük kullanımda **`build.ps1`** yeterli. Komutları proje klasöründe çalıştırın:

```powershell
# Windows kurulumu (.exe) ve Android paketleri (.apk + .aab)
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1

# Yalnızca Android
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Target Android

# Yalnızca Windows
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Target Windows

# Steam Windows/Linux depot paketleri
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1 -Target Steam
```

Bu komutlar ilgili hedefi temizleyip mevcut kaynakları yeniden derler. `All`
(varsayılan) Windows ve Android üretir; Steam ayrıca seçilir. Derleme bitince
çıktı yolları terminalde yazılır. Komutlar paket üretir; kurulum veya mağazaya
yükleme yapmaz.

| Paket | Çıktı |
| --- | --- |
| Windows kurulumu | `lwjgl3/target/installer/DeepDive Drift-<sürüm>.exe` |
| Android APK | `android/target/store/google-play/DeepDiveDrift-<sürüm>-universal.apk` |
| Google Play AAB | `android/target/store/google-play/DeepDiveDrift-<sürüm>-google-play.aab` (anahtar ayarlanmamışsa `-unsigned.aab`) |
| Steam | `lwjgl3/target/steam/windows-x64` ve `linux-x64`; her klasörde `version.txt` |

## Sürüm otomatik nasıl belirleniyor?

Tek kaynak `get-project-version.ps1` betiğidir:

- Temel sürüm kök `pom.xml` içindeki `<version>` alanından okunur.
- Temel sürümün son sayısına `git rev-list --count HEAD` eklenir.
- Örneğin `1.0.0` + 39 commit → `1.0.39`; sonraki commit → `1.0.40`.
- Android `versionCode` = `pom.xml` içindeki `android.version-code` + commit sayısı.
- Windows kurulum sürümü, Android APK/AAB sürümü, Steam EXE sürümü ve Steam VDF
  açıklaması aynı hesabı kullanır. Sürüm numarası komutlara elle yazılmaz.

Her committen sonra aynı derleme komutunu çalıştırın. Commit atmak mevcut
EXE/APK dosyalarını kendiliğinden yenilemez; yeni paket için derleme gerekir.
Commit olmadan tekrar derleme sürümü artırmaz. Commitlenmemiş değişiklikler
pakete dahil edilir; betik sürümün yalnızca commit sayısını temsil ettiğini bildirir.
Paketlerin farklı sürümler taşımaması için derleme devam ederken commit atmayın.

Yalnızca sıradaki derlemenin sürümünü görmek için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\get-project-version.ps1
```

Maven bağımlılıklarının temel sürümü ve ara JAR adı `1.0.0` olarak kalabilir;
dağıtılan paketlerin sürümü yukarıdaki ortak hesapla belirlenir.

## Diğer betikler ne işe yarıyor?

| Betik | Görevi / ne zaman kullanılır? |
| --- | --- |
| `build.ps1` | Günlük derleme girişi; varsayılan Windows + Android. |
| `build-windows-installer.ps1` | Windows EXE üretir; `build.ps1 -Target Windows` bunu çağırır. |
| `build-android-test.ps1` | APK/AAB üretir ve doğrular. USB üzerinden kurmak için ayrıca `-Install` kullanılabilir. |
| `build-android-release.ps1` | Maven Android profilinin çağırdığı paketleme yardımcısı; SDK parametrelerini Maven verir. Günlük kullanımda `build.ps1 -Target Android` seçin. |
| `verify-android-release.ps1` | Android paketi üretir ve doğrular; `-SkipBuild` mevcut paketi doğrular. `-RequireSignedBundle` Google Play yükleme imzasını zorunlu tutar. |
| `verify-android-dependency-lock.ps1` | Kilitli reklam bağımlılıklarının tam envanterini ve SHA-256 değerlerini doğrular. |
| `verify-desktop-smoke.ps1` | Paketlenmiş masaüstü JAR'ını sekiz güvenli ekranda başlatır, gerçek kare yakalar ve temiz çıkışı doğrular. |
| `verify-project.ps1` | Testleri, masaüstü paketi/smoke geçişini, Android DISABLED/TEST yapılarını, paket doğrulamasını ve beklenen PRODUCTION hatalarını tek komutta çalıştırır. |
| `build-steam-packages.ps1` | Maven Steam profilinin çağırdığı paketleme yardımcısı. Günlük kullanımda `build.ps1 -Target Steam` seçin. |
| `prepare-steam-release.ps1` | App/Depot ID'leriyle Steam paketlerini ve yükleme VDF dosyalarını üretir. |
| `upload-steam-build.ps1` | Hazırlanan VDF ile Steam'e yükler; paket derlemez. |
| `get-project-version.ps1` | Ortak sürümü hesaplar; paket derlemez. |
| `create-android-upload-key.ps1` | Google Play için yükleme anahtarı oluşturur; her derlemede çalıştırılmaz. |
| `build-licensed-art.ps1` | Lisanslı kaynaklardan oyun/mağaza görsellerini üretir; EXE/APK derlemez. |
| `export-store-art.ps1` | Mağaza görsellerini dışa aktarır; EXE/APK derlemez. |

Android SDK ve imzalama ayrıntıları [Android rehberinde](../store/google-play/ANDROID_TEST_TR.md),
Steam kimlikleri ve yükleme adımları [Steam rehberinde](../steam/README.md) bulunur.

## Android advertising build modes

`DEEPDRIFT_ADS_MODE` accepts `DISABLED`, `TEST`, or `PRODUCTION` and defaults to
`DISABLED`. `TEST` always generates Google's official demo App ID and ad-unit IDs.
`PRODUCTION` requires `DEEPDRIFT_ADMOB_APP_ID`, `DEEPDRIFT_REWARDED_AD_UNIT_ID`, and
`DEEPDRIFT_INTERSTITIAL_AD_UNIT_ID`; missing, malformed, or demo values fail the build.
Production identifiers remain outside tracked source. The Android dependency graph is
versioned and checksum-locked in `android/ads-dependencies.lock`.

## Optimized Android release verification

The ordinary Android workflow remains unoptimized for development and QA. To build and
verify the separate R8-shrunk, optimized, and obfuscated TEST package, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-android-optimized-release.ps1
```

The optimized APK and AAB use the `-optimized-` filename qualifier. R8 writes
`mapping.txt`, `usage.txt`, `seeds.txt`, the resolved configuration, consumer-rule
selection, warnings, version, and size comparison below
`android/target/store/google-play/DeepDiveDrift-<version>-r8/`. Retain the mapping from
every production release with that release's artifacts so crash traces can be
deobfuscated. These generated per-build reports are not committed.

The optimized verifier checks TEST advertising metadata, required resources and service
descriptors, all four ABIs, every packaged ELF LOAD alignment, 16 KB APK ZIP alignment,
R8 feature enablement, zero warnings, JNI descriptors, reflective controller loading,
and persisted enum names. Android resource shrinking is intentionally separate and is
not enabled by this custom non-AGP pipeline.
