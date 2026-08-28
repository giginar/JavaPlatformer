# DeepDive Drift

DeepDive Drift, libGDX ile geliştirilen hızlı tempolu bir su altı hayatta kalma oyunudur. Dalgıcın yüksekliğini kontrol et, zıpkınla avlan, derinlik yükseltmelerini seç ve Abyssal Octopus’a ulaş.

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
- Beş derinlik bölgesinin sonunda Abyssal Octopus savaşı başlar.
- Beşinci bölgede kalkanla engellenmeyen herhangi bir hasar koşuyu anında bitirir.
- Ekran sarsıntısı ve flaş efektleri ayarlardan ayrı ayrı kapatılabilir.
- Skor, oksijen ve hareket hesapları FPS’den bağımsızdır.

## Maven ile geliştirme

Proje Maven 3.9.16 Wrapper ve Java 21 kullanır. Yerel Maven kurulumu gerekmez:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -pl lwjgl3 -am package
java -jar .\lwjgl3\target\DeepDiveDrift-1.0.0.jar
```

İkinci komut masaüstü için bütün bağımlılıkları ve varlıkları içeren çalıştırılabilir JAR üretir. Sürüm tek noktadan, kök `pom.xml` içindeki `<version>` alanından yönetilir.

Doğrulanmış CC0/OFL kaynaklardan oyun, ikon ve mağaza görsellerini yeniden üretmek için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-licensed-art.ps1
```

Kaynak URL’leri, lisans metni ve SHA-256 kayıtları `third_party/cc0` altında tutulur.

## Windows kurulum dosyası

Java kurulumu gerektirmeyen, kurulum sihirbazlı Windows EXE dosyasını üretmek için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-windows-installer.ps1
```

Betik masaüstü JAR'ını derler, gerekli WiX araçlarını doğrulanmış proje önbelleğine
indirir ve kurulumu `lwjgl3/target/installer` altında oluşturur. Üretilen tek EXE
dosyası başka bir Windows bilgisayara doğrudan gönderilebilir.

Installer sürümü otomatik olarak `major.minor.build` biçiminde üretilir. `major`,
`minor` ve başlangıç build değeri kök `pom.xml` sürümünden; kalan build numarası
Git commit sayısından gelir. Örneğin proje sürümü `1.0.0` ve 26 commit için sürüm
`1.0.26`, çıktı adı da `DeepDive Drift-1.0.26.exe` olur. Böylece her yeni commit
installer sürümünü artırır. Geçici olarak belirli bir sürüm üretmek gerekirse
`-Version 2.1.15` parametresiyle otomatik sürüm geçersiz kılınabilir.

## Google Play paketi

Android derlemesi için Android SDK Platform 36 ve Build Tools 36.0.0 gerekir. SDK yolu `local.properties` içindeki `sdk.dir` ile veya `ANDROID_SDK_ROOT` ortam değişkeniyle belirtilir.

```powershell
.\mvnw.cmd -B -ntp -Pandroid-release -pl android -am package
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-android-release.ps1 -SkipBuild
```

Çıktılar `android/target/store/google-play` altında oluşur. Maven profili AAPT2, D8 ve Bundletool’un resmi komut satırı araçlarını kullanır; doğrulama betiği Bundletool şemasını, yerel debug anahtarıyla imzalanmış evrensel test APK’sını, ZIP hizalamasını, dört ABI için 16 KB ELF uyumluluğunu ve AAB imzasını denetler.

`keystore.properties` yoksa AAB bilinçli olarak `-unsigned.aab` adıyla üretilir. Özel yükleme anahtarını oluşturmak ve imzalı paket almak için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\create-android-upload-key.ps1
Copy-Item keystore.properties.example keystore.properties
# Parolaları keystore.properties içine girdikten sonra aynı Maven komutunu çalıştırın.
.\mvnw.cmd -B -ntp -Pandroid-release -pl android -am package
```

`keystore.properties` ve `upload-keystore.jks` Git tarafından yok sayılır. Anahtarın güvenli bir yedeğini saklayın.

## Steam paketleri

```powershell
.\mvnw.cmd -B -ntp -Psteam -pl lwjgl3 -am verify
```

Bu profil sabitlenmiş ve SHA-256 ile doğrulanan Temurin 21 JRE’lerini indirir. Depot içerikleri şuralarda oluşur:

- `lwjgl3/target/steam/windows-x64`
- `lwjgl3/target/steam/linux-x64`

Steamworks App ID ve Depot ID’leri alındıktan sonra paketleri ve VDF dosyalarını birlikte üretmek için:

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
pom.xml                          Çok modüllü Maven üst projesi ve ortak sürümler
android/                         Android launcher ve AAB profili
assets/                          Görsel, ses ve font dosyaları
core/src/main/java/com/game/     Oyun, ekranlar ve platform bağımsız giriş
core/src/test/                   JUnit 5 testleri
lwjgl3/                          Masaüstü launcher ve Steam paketleme profili
scripts/                         Paketleme, imzalama ve mağaza yükleme yardımcıları
steam/                           SteamPipe şablonları ve mağaza metinleri
store/                           Google Play metinleri ve yayın belgeleri
third_party/cc0/                 CC0 kaynak dosyaları, lisans ve doğrulama kayıtları
```
