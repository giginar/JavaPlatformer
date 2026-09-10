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
| Android | Sol kenar şeridinde herhangi bir yeri basılı tut | Sağ kenar şeridinde herhangi bir yere dokun | Seçeneğe dokun, sağ üstteki `Ⅱ` simgesinden duraklat |

Kenar şeritleri ekranın tüm yüksekliğinde, geniş ekranlardaki siyah yan boşluklar dahil çalışır; aynı anda yüzüp ateş edilebilir. `SWIM` / `FIRE` ipuçları köşelerde görünür ve ilk 4 saniyede kaybolur; dokunma alanları aktif kalır. Şerit genişliği cihazın ekran yoğunluğuna göre ayarlanır. Dash yükü varsa alt ortadaki küçük `DASH` düğmesine dokunulur.

Pause ve dash düğmeleri ekranın güvenli alanlarına göre yerleşir. Mesafe, sonraki mesafe hedefi ve bölüm ilerlemesi üstte; combo sağda pause alanından ayrı gösterilir. Yükseltme ekranı tamamen opaktır. Menü, ayarlar, yükseltme seçimi, duraklatma ve sonuç ekranlarının tamamı dokunmatik ve gamepad ile kullanılabilir.

Ana menüdeki veya OPTIONS içindeki **CONTROLS** sayfası, kullanılan klavye/fare, gamepad veya dokunmatiğe göre kontrol rehberini otomatik değiştirir. Oyun başlangıcındaki kısa ipuçları korunur; diğer menülerin altında sürekli kontrol açıklaması gösterilmez. DIVE SHOP'taki pearl bakiyesi, ekipman ve kıyafet sekmelerinin ortak başlığında görünür ve alışverişten sonra hemen güncellenir.

Menü düğmeleri fare üzerlerine geldiğinde parlar ve yazıları hafif büyür; fare veya parmak basılıyken içeri oturur. Seçim bırakınca tamamlanır, düğmenin dışına sürükleyip bırakmak işlemi iptal eder. Görsel efektlerin boyutları `GameConfig` içindeki `BUTTON_*` değerlerinden ayarlanabilir.

## Hızlı ses kontrolü

Telefonda ilk açılışta, ana menüden önce **PLAY SILENTLY / ENABLE SOUND** seçimi gösterilir.
Seçim yapılana kadar müzik ve ses efektleri çalmaz. Tercih cihazda hatırlanır;
sessiz oynamayı seçtiyseniz sonraki açılış da sessiz olur.

Ana menüdeki **SOUND** satırına dokunarak tüm oyun seslerini susturabilir veya önceki
ses seviyesine dönebilirsiniz. **− / +** düğmeleri müzik ve efektlerin ortak sesini
yüzde 25'lik adımlarla değiştirir. Klavye/gamepad ile SOUND satırını seçip sol/sağ ile
seviyeyi değiştirebilir, seçim tuşuyla susturabilirsiniz.

Ana menüde ses sıfıra indirildiğinde veya susturulduğunda OPTIONS içindeki **MUSIC**
ve **SOUND EFFECTS** de **OFF** olur. Ana menüden ses tekrar açıldığında ikisi de açılır.
OPTIONS'tan yalnızca birini açmak, son ses seviyesini geri getirerek sadece o ses türünü
etkinleştirir; ikisini de kapatmak ana menüyü **SOUND OFF** durumuna getirir. Ses açıkken
seviyeyi değiştirmek, müzik ve efektlerin ayrı seçimlerini korur. Bu ayarlar sonraki
açılışta da hatırlanır.

## Oynanış

- Dalgıç görseli %50 büyütüldü; ince, açık renkli kenar çizgisi koyu arka planlarda takibi kolaylaştırır. Kamera görüş alanı, yüzme hareketi ve çarpışma alanı aynı kalır.
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

İkinci komut masaüstü için bütün bağımlılıkları ve varlıkları içeren çalıştırılabilir JAR üretir. Maven temel sürümü kök `pom.xml` içindeki `<version>` alanından yönetilir. Windows kurulum dosyası ve Android APK/AAB sürümleri, bu temel sürüme Git commit sayısını ekleyen ortak `scripts/get-project-version.ps1` betiğinden gelir.

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

## Android telefonda deneme

Telefona kopyalanıp kurulabilecek APK'yı üretmek ve doğrulamak için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-android-test.ps1
```

Çıktı: `android/target/store/google-play/DeepDiveDrift-1.0.34-universal.apk` (34 commit için).
APK ve AAB dosya adları ile Android'deki `versionName`, Windows kurulumuyla aynı
otomatik sürümü kullanır: `1.0.34`, sonraki committe `1.0.35`.
Google Play'in sayısal `versionCode` değeri de otomatik hesaplanır:
`pom.xml` içindeki `android.version-code` başlangıç değeri + Git commit sayısı
(34 commit için `1 + 34 = 35`). Commit olmadan tekrar derleme sürümü artırmaz.
Dosyayı telefona kopyalayıp açarak kurabilirsiniz. Bu test için Google Play hesabı gerekmez.

USB hata ayıklaması açık bir telefona mevcut APK'yı yükleyip oyunu açmak için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-android-test.ps1 -SkipBuild -Install
```

Kod değişikliğinden sonra yeniden derlemek için `-SkipBuild` parametresini kaldırın.
Birden fazla cihaz bağlıysa `-DeviceId SERI_NUMARASI` ekleyin.
Kurulum, sorun giderme, telefon testleri ve Play Console adımları:
[Android test ve Google Play rehberi](store/google-play/ANDROID_TEST_TR.md).

Android paketlemesi `android/BundleConfig.json` ile ses dosyalarını APK içinde
sıkıştırmadan saklar. libGDX'in Android ses yükleyicisi bunu gerektirir;
derleme ve doğrulama betikleri son APK'daki ses dosyalarını da denetler.
Aynı committen tekrar üretilen APK'ları ayırt etmek için yanında `.apk.sha256` dosyası oluşur.
Kurulumdan sonra anında kapanma durumunda [cihaz hata kaydını alma adımlarını](store/google-play/ANDROID_TEST_TR.md#sorun-giderme) izleyin.

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

Google Play'e göndermeden önce imzayı zorunlu tutan doğrulamayı çalıştırın:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-android-release.ps1 -SkipBuild -RequireSignedBundle
```

Bu komut imzasız AAB için hata verir; APK/AAB içindeki sürüm bilgilerini de ortak
sürüm hesabıyla karşılaştırır. Her yeni Play yüklemesini yeni bir committen üretin;
`android.version-code` başlangıç değerini normal sürümlerde elle artırmak gerekmez.
Paket doğrulaması, gerçek cihaz testi ve Play Console incelemesinin yerine geçmez.

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
