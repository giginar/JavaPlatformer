# Android telefonda test ve Google Play'e hazırlık

Windows için `build-windows-installer.ps1` EXE üretir. Android için karşılığı
`scripts/build-android-test.ps1` betiğidir. Aynı oyun kodundan telefon için APK ve
Google Play için AAB üretilir. Komutları proje kökündeki PowerShell terminalinde çalıştırın.

## İlk deneme: APK'yı telefona kopyala

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-android-test.ps1
```

1. `android/target/store/google-play/DeepDiveDrift-1.0.34-universal.apk` dosyasını USB dosya aktarımıyla telefonun İndirilenler klasörüne kopyalayın. Dosyadaki sürüm Windows kurulumuyla aynı hesaptan gelir; `1.0.34` örneği 34 commit içindir.
2. Telefonda Dosyalar / Dosyalarım uygulamasından APK'yı açın.
3. Android isterse bu dosya yöneticisi için **Bilinmeyen uygulamaları yükle / Bu kaynaktan izin ver** seçeneğini açıp kuruluma dönün. Menü adı Android sürümüne göre değişebilir.
4. **Yükle**, ardından **Aç** düğmesine dokunun. Oyunun adı **DeepDive Drift**.

Bu yerel test Google Play hesabı ve Android Studio gerektirmez.
Derleme bilgisayarında Java 21, Android SDK Platform 36 ve Build Tools 36.0.0 gerekir;
SDK yolu `local.properties` veya `ANDROID_SDK_ROOT` üzerinden ayarlanır.
Paketin minimum Android sürümü 5.0'dır (API 21); desteklenen sürümlerde gerçek cihaz testi ayrıca yapılmalıdır.

## USB ile tek komutta kur ve aç

Telefonda geliştirici seçeneklerini etkinleştirip **USB hata ayıklama** seçeneğini açın.
USB veri kablosunu bağlayın, telefonun kilidini açın ve bilgisayara hata ayıklama izni isteyen
bildirimi onaylayın. Windows'ta telefon üreticisinin ADB sürücüsü gerekebilir.
[Android'in resmi cihaz kurulum rehberi](https://developer.android.com/studio/run/device).

Mevcut APK'yı kurmak için:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-android-test.ps1 -SkipBuild -Install
```

Kod değişikliğinden sonra derleyip kurmak için `-SkipBuild` parametresini kaldırın.
Birden fazla telefon/emülatör varsa `-DeviceId SERI_NUMARASI` ekleyin.
Betik paketleri doğrular, APK'yı `adb install -r` ile kurar ve Android'e oyunu açma isteği gönderir.
Android'in açılış isteğini kabul etmesi, oyunun sorunsuz çalıştığı anlamına gelmez; menüyü telefonda kontrol edin.

## Sorun giderme

Aşağıdaki ADB yolu bu projedeki yerel SDK içindir; başka SDK kullanılıyorsa yolu uyarlayın.

```powershell
.\.android-sdk\platform-tools\adb.exe devices -l
```

| Sonuç | Yapılacak işlem |
| --- | --- |
| Liste boş | Veri aktarabilen USB kablosu kullanın; USB hata ayıklamasını ve üretici sürücüsünü kontrol edin. |
| `unauthorized` | Telefonun kilidini açıp USB hata ayıklama iznini onaylayın. |
| `offline` | Kabloyu çıkarıp bağlayın ve telefonun kilidini açın. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Önceki kurulum farklı anahtarla imzalanmıştır. Aynı anahtarla derleyin veya eski kurulumu kaldırın; kaldırma yerel skor ve ilerlemeyi siler. |
| Oyun açılırken kapanıyor | Hemen ardından aşağıdaki komutla hata kaydını alın. |

```powershell
.\.android-sdk\platform-tools\adb.exe logcat -d -b crash > .\android\target\android-crash.txt
```

Hata kaydı boşsa, oyunu yeniden açmayı denedikten hemen sonra Android hata iletilerini alın:

```powershell
.\.android-sdk\platform-tools\adb.exe logcat -d -v threadtime AndroidRuntime:E '*:S' > .\android\target\android-startup.txt
```

Bu dosyayı Android/MagicOS sürümü ve telefon modeliyle birlikte incelemek, cihazdaki
kesin kapanma nedenini belirlemeyi sağlar. Birden fazla cihaz bağlıysa ADB komutuna
`-s SERI_NUMARASI` ekleyin.

Test APK'sı `.mvn/tools/android-debug.keystore` anahtarıyla imzalanır. Aynı anahtarla yapılan
yeniden kurulum yerel kayıtları korur. Google Play sürümü farklı imzayla dağıtılabileceği için
yerel test APK'sından Play sürümüne doğrudan güncelleme mümkün olmayabilir.

## Telefonda denenecekler

- Ana menü, dalış hazırlığı, ayarlar, oyun içi mağaza ve başarımlar dokunmayla kullanılabiliyor mu?
- Yüzme düğmesini basılı tutarken diğer parmakla ateş edilebiliyor mu? Çentik ve sistem hareket alanları düğmeleri kapatıyor mu?
- Duraklatma, devam etme ve Android geri düğmesi doğru çalışıyor mu?
- Ana ekrana dönüp oyuna geri gelince koşu duraklatılmış kalıyor mu? Ses doğru devam ediyor mu?
- Ekranı kilitleyip açınca görüntü ve ses geri geliyor mu?
- Oyunu kapatıp yeniden açınca skor, inciler, kostümler, başarımlar ve ayarlar korunuyor mu?
- Uçak modunda oynanabiliyor mu? Uzun koşuda ve ahtapot savaşında akıcılık yeterli mi?
- Telefonun iki yatay yönünde arayüz okunabiliyor mu?

Test sonuçlarıyla birlikte telefon modeli ve Android sürümünü kaydedin. Mağaza için
gerçek telefondan menü ve oynanış ekran görüntüleri alın.

## Google Play'e geçiş

1. Telefon testlerini tamamlayın. [Yayın kontrol listesindeki](../RELEASE_CHECKLIST_TR.md) destek adresi, gizlilik politikası ve varlık hakları gibi eksikleri kapatın.
2. Google Play geliştirici hesabını ve istenen kimlik/cihaz doğrulamalarını tamamlayın. Uygulamayı **DeepDive Drift**, türünü **Oyun** olarak oluşturun.
3. Paket kimliğini (`com.game.diver.deepdivedrift`) ilk yüklemeden önce kesinleştirin; [Google Play paket adları kalıcıdır](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).
4. Özel upload anahtarı oluşturun:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\create-android-upload-key.ps1
Copy-Item .\keystore.properties.example .\keystore.properties
```

`keystore.properties` içindeki alanları yerelde doldurun. Parolaları sohbet veya Git'e eklemeyin;
anahtarın ve parolaların güvenli yedeğini saklayın.

5. Her yeni Play yüklemesini yeni bir committen üretin. `versionName`, Windows ile aynı otomatik sürümdür (`1.0.34` gibi). `versionCode`, kök `pom.xml` içindeki `android.version-code` başlangıç değeri + Git commit sayısıdır (34 commit için `35`). Normal sürümlerde elle değiştirilmez; aynı committen tekrar derlemek sürümü artırmaz. İmzalı paketi üretip doğrulayın:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-android-release.ps1 -RequireSignedBundle
```

6. `android/target/store/google-play/DeepDiveDrift-1.0.34-google-play.aab` dosyasını (34 commit örneği; güncel sürüm dosyasını seçin) Play Console'da **Dahili test / Internal testing** sürümüne yükleyin ve Play App Signing'i yapılandırın. `-unsigned.aab` dosyası yükleme için hazır değildir; APK yerel test içindir.
7. Kendinizi test kullanıcısı ekleyip katılım bağlantısını aynı Google hesabıyla telefonda açın. Play Store üzerinden gelen sürümü de test edin.
8. [Mağaza metinlerini](listing-tr.md), ikon/özellik görselini, telefon ekran görüntülerini, gizlilik politikası URL'sini, veri güvenliği, reklam, içerik derecelendirmesi ve hedef kitle beyanlarını tamamlayın.
9. Hesabın gerektirdiği kapalı test ve üretim erişimi sürecini tamamladıktan sonra yayına başvurun.

5 Eylül 2026 kontrolü: yeni telefon uygulamaları için [hedef API gereksinimi 36](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en).
Proje bu değeri kullanır. Paket doğrulaması [16 KB ZIP/ELF uyumluluğunu](https://developer.android.com/guide/practices/page-sizes) denetler;
16 KB sayfa boyutlu cihaz/emülatörde çalışma testi ayrıca gerekir.

13 Kasım 2023 sonrasında açılmış kişisel geliştirici hesaplarında üretim erişimine başvurmadan önce
en az **12 test kullanıcısının kesintisiz 14 gün kapalı teste katılmış olması** gerekir.
Dahili test bu şartın yerine geçmez. [Google Play test koşulları](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).

## Bu çalışma sırasında doğrulanan durum

- 5 Eylül 2026: Android derlemesi ve 44 birim testi geçti.
- Evrensel test APK'sının imzası, ZIP hizalaması, AAB yapısı ve dört ABI'nin ELF hizalaması doğrulandı.
- AAB şu anda imzasızdır; özel upload anahtarı henüz yapılandırılmamıştır.
- Bağlı telefon bulunmadığından gerçek cihazdaki kurulum, açılış ve oynanış henüz doğrulanmadı.

## 7 Eylül 2026: açılışta kapanma için paketleme düzeltmesi

Honor 400 geri bildirimi sonrasında eski `1.0.34` APK incelendi. Başlangıçta
`AudioManager.initialize()` tarafından yüklenen `underwater.mp3` dahil sekiz ses
dosyasının tamamı APK içinde sıkıştırılmıştı (ZIP method `8`, DEFLATED).
libGDX Android ses yükleyicisi `AssetManager.openFd` kullanır;
[Android bu çağrıda sıkıştırılmış dosyalar için hata verir](https://developer.android.com/reference/android/content/res/AssetManager#openFd(java.lang.String)).

`android/BundleConfig.json`, ses dosyalarını APK'da sıkıştırılmadan saklayacak biçimde
AAB'ye eklendi. Bu kural [AAB'den üretilen APK'lara da uygulanır](https://developer.android.com/build/building-cmdline#customize_apk_generation).
`scripts/VerifyAndroidAudio.java`, son APK'yı açıp ses dosyalarının varlığını, boyutunu ve
gerçek ZIP yönteminin `0` (STORED) olduğunu doğrular. Eski APK bu kontrolde reddedilir.

Düzeltme aynı Git commitinden üretildiğinde sürüm yine `1.0.34` kalır. Arkadaşınıza
yeniden üretilmiş `.apk` dosyasını gönderin; önceden gönderilmiş RAR/APK kopyası eski
paketi içerebilir. Dosyanın SHA-256 özeti yanında `.apk.sha256` olarak üretilir.
Aynı test anahtarıyla imzalanan yeni APK mevcut kurulumun üzerine yüklenebilir.

Doğrulama: 44 birim testi, APK/AAB sürümü, imza ve 16 KB kontrolleri geçti.
Sekiz ses dosyası hem evrensel APK'da hem de örnek Android 15 / ARM64 cihaz tanımıyla
AAB'den üretilen `base-master.apk` içinde sıkıştırılmadan saklanıyor.
Eski ve yeni APK'nın imza sertifikaları aynı. 7 Eylül düzeltilmiş evrensel APK SHA-256:
`0291fce1155f90f4b91172b78f7f08a7dab444c3fc0ada213af123e0f5b87a45`.

Bu bulgu paketleme kusurunu doğrular. Honor 400'ün gerçek çökme kaydı ve düzeltme
sonrası telefondaki açılış testi henüz alınmadığından, cihazda başka bir hata bulunmadığı
sonucu çıkarılmamalıdır.
