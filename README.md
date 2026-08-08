# DeepDive Drift

DeepDive Drift, libGDX ile geliştirilen hızlı tempolu bir su altı hayatta kalma oyunudur. Dalgıcın yüksekliğini kontrol et, zıpkınla avlan, derinlik yükseltmelerini seç ve Abyss Leviathan’a ulaş.

## Desteklenen hedefler

- Windows ve Linux masaüstü paketleri
- Steam için açılmış Windows/Linux depot klasörleri
- Android 5.0+ (`minSdk 21`) ve Google Play için Android App Bundle
- Hedef Android API: 36

Apple/iOS ve macOS bu sürüm çalışmasının kapsamına dahil değildir.

## Kontroller

| Ortam | Yüzme | Ateş | Menü / duraklatma |
| --- | --- | --- | --- |
| Klavye | `Space`, `W`, `↑` veya sol fare | `Z`, `X` veya sağ fare | Oklar, `Enter`, `Esc`, `P` |
| Gamepad | `A`, sol çubuk veya D-pad yukarı | `X`, `B`, `RB` veya `RT` | D-pad/çubuk, `A`, `B`, `Start` |
| Android | Ekrandaki `SWIM` düğmesini basılı tut | `FIRE` düğmesine dokun | Seçeneğe dokun, sağ üstten duraklat |

Dokunmatik düğmeler ekranın güvenli alanlarına göre yerleşir. Menü, ayarlar, yükseltme seçimi, duraklatma ve sonuç ekranlarının tamamı dokunmatik ve gamepad ile kullanılabilir.

## Oynanış

- Oksijen zamanla azalır; tüpler ve avlar oksijen kazandırır.
- Art arda avlar en fazla `x3` puan çarpanına ulaşan combo oluşturur.
- `1000`, `2500` ve `5000` puanda bir yükseltme seçilir.
- `6500` puanda Abyss Leviathan savaşı başlar.
- Ekran sarsıntısı ve flaş efektleri ayarlardan ayrı ayrı kapatılabilir.
- Skor, oksijen ve hareket hesapları FPS’den bağımsızdır.

## Geliştirme

Masaüstü geliştirme için Java 21 kullanılır:

```powershell
.\gradlew.bat :lwjgl3:run
.\gradlew.bat test
```

Android derlemesi için Android SDK Platform 36 ve Build Tools 36.0.0 gerekir. Android Studio bunları kurabilir; SDK yolu `local.properties` içindeki `sdk.dir` ile belirtilir.

Doğrulanmış CC0/OFL kaynaklardan oyun, ikon ve mağaza görsellerini yeniden üretmek için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-licensed-art.ps1
```

Kaynak URL’leri, lisans metni ve SHA-256 kayıtları `third_party/cc0` altında tutulur.

## Google Play paketi

```powershell
.\gradlew.bat :android:prepareGooglePlayBundle
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-android-release.ps1
```

Çıktı `android/build/store/google-play` altında oluşur. Doğrulama betiği APK ZIP hizalamasını, dört ABI için 16 KB ELF uyumluluğunu, AAB içeriğini ve imzayı denetler. `keystore.properties` yoksa dosya bilinçli olarak `-unsigned.aab` adıyla üretilir. Özel yükleme anahtarını oluşturmak ve imzalı paket almak için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\create-android-upload-key.ps1
Copy-Item keystore.properties.example keystore.properties
# Parolaları keystore.properties içine girdikten sonra:
.\gradlew.bat :android:prepareGooglePlayBundle
```

`keystore.properties` ve `upload-keystore.jks` Git tarafından yok sayılır. Anahtarın güvenli bir yedeğini saklayın.

## Steam paketleri

```powershell
.\gradlew.bat :lwjgl3:prepareSteam
```

Depot içerikleri şuralarda oluşur:

- `lwjgl3/build/steam/windows-x64`
- `lwjgl3/build/steam/linux-x64`

Steamworks App ID ve Depot ID’leri alındıktan sonra VDF dosyalarını üretmek için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\prepare-steam-release.ps1 `
  -AppId APP_ID `
  -WindowsDepotId WINDOWS_DEPOT_ID `
  -LinuxDepotId LINUX_DEPOT_ID
```

Yükleme komutu ve mağaza adımları [steam/README.md](steam/README.md) içinde açıklanmıştır.

## Yayın belgeleri

- [Google Play mağaza metinleri](store/google-play/listing-tr.md)
- [Veri güvenliği beyanı](store/google-play/data-safety-tr.md)
- [Gizlilik politikası](store/privacy-policy-tr.md)
- [Steam mağaza metinleri](steam/store-page-tr.md)
- [Yayın kontrol listesi](store/RELEASE_CHECKLIST_TR.md)
- [Varlık hakları kontrolü](store/ASSET_RIGHTS_CHECKLIST.md)
- [Mağaza görsellerinin üretim kaydı](store/art/README.md)
- [Gerçek oyun ekran görüntüleri](store/screenshots/README.md)

## Proje yapısı

```text
android/                         Android launcher ve AAB yapılandırması
assets/                          Görsel, ses ve font dosyaları
core/src/main/java/com/game/     Oyun, ekranlar ve platform bağımsız giriş
core/src/test/                   JUnit 5 testleri
lwjgl3/                          Masaüstü launcher ve paketleme
scripts/                         İmzalama ve mağaza yükleme yardımcıları
steam/                           SteamPipe şablonları ve mağaza metinleri
store/                           Google Play metinleri ve yayın belgeleri
third_party/cc0/                 CC0 kaynak dosyaları, lisans ve doğrulama kayıtları
```
