# DeepDive Drift yayın kontrol listesi

## Ortak

- [x] Windows ve Android için ortak otomatik sürüm; `versionName` commit sayısından, Android `versionCode` temel değer + commit sayısından üretilir
- [x] Otomatik birim testleri
- [x] Klavye, fare, gamepad ve dokunmatik girişleri
- [ ] Temel çevrimdışı çalışma yeniden doğrulanacak; Google Ads + UMP var, analitik ve hesap SDK’sı yok
- [x] Görsel, ikon ve mağaza görseli haklarının belgelenmesi (CC0/OFL kaynak zinciri eklendi)
- [ ] Ses ve müzik haklarının belgelenmesi veya dosyaların lisanslı alternatiflerle değiştirilmesi
- [ ] Gerçek cihazlarda son kalite testi
- [ ] Yayıncı destek e-postası ve destek sayfası

## Google Play

- [x] Paket adı: `com.game.diver.deepdivedrift`
- [x] `targetSdk 36`, dört ABI ve AAB üretimi
- [x] APK ZIP hizalaması ve dört ABI için 16 KB ELF uyumluluğu doğrulandı
- [x] Android ses dosyaları sıkıştırılmadan paketleniyor; evrensel APK ve örnek ARM64 split üzerinde doğrulandı
- [x] Veri güvenliği ve mağaza metni taslakları
- [x] 512×512 ikon ve 1024×500 özellik görseli üretildi
- [ ] Google Play geliştirici hesabı ve kimlik doğrulaması
- [ ] Özel upload keystore oluşturma ve güvenli yedek
- [ ] Gizlilik politikasındaki e-posta alanını doldurma ve HTTPS üzerinde yayınlama
- [ ] Gerçek Android cihazdan telefon ekran görüntüleri alma
- [ ] İkonu, özellik görselini ve telefon ekran görüntülerini Play Console’a yükleme
- [ ] İçerik derecelendirmesi, hedef kitle ve uygulama erişimi anketleri
- [ ] Amaçlanan hedef yaş gruplarını seçme; çocuklar dahilse Families, nötr yaş ekranı ve reklam SDK uygunluğunu tamamlama
- [ ] Yayın ülkelerini ve nihai mağaza/gizlilik politikası dillerini seçme
- [ ] Veri Güvenliği çalışma sayfasını son üretim AAB'si ve güncel Play Console sorularıyla doğrulama
- [ ] Advertising ID beyanını son hedef kitle ve `AD_ID` izniyle doğrulama
- [ ] AdMob Privacy & Messaging bölgesel mesajlarını yayımlama ve gizlilik seçeneklerini gerçek cihazda doğrulama
- [ ] Ödüllü reklam faydasını ayrıca onaylama; onaya kadar oyuncuya gösterilen yerleşimi kapalı tutma
- [ ] Kapalı test/üretim erişimi için hesabın istediği test koşullarını tamamlama
- [ ] İmzalı AAB'yi yükleyip Play App Signing'i etkinleştirme

Phase 7 iç politika çalışma sayfası: [Google Play politika, hedef kitle ve Veri Güvenliği denetimi](google-play/phase-7-policy-audit.md).

Yayına engel durumlar geçerlidir: sekiz ses varlığının ticari kullanım kanıtı, AI boss sağlayıcı/dağıtım koşulları, JLayer/JOrbis bildirim incelemesi, fiziksel cihaz ve reklam/UMP QA'sı, gerçek AdMob yapılandırması/üretim kimlikleri ve üretim imzalama tamamlanmamıştır.

## Steam

- [x] Java gerektirmeyen Windows ve Linux paketleri
- [x] Açılmış depot klasörleri ve SteamPipe VDF üreticisi
- [x] Klavye/fare ve gamepad desteği
- [x] Türkçe/İngilizce mağaza açıklaması taslakları
- [x] Güncel Steam grafik varlıkları ve beş adet 1920×1080 gerçek oyun ekran görüntüsü
- [x] Windows paketinde yerel açılış testi
- [ ] Steamworks ortak hesabı, banka/vergi ve Steam Direct işlemleri
- [ ] App ID ile Windows/Linux Depot ID’lerini betiğe verme
- [ ] Grafik varlıklarını ve ekran görüntülerini Steamworks’e yükleme
- [ ] Desteklenen diller, sistem gereksinimleri ve içerik anketini doldurma
- [ ] SteamPipe build’ini özel test dalına yükleme ve temiz makinelerde test
- [ ] Mağaza sayfası ile build’i Valve incelemesine gönderme
