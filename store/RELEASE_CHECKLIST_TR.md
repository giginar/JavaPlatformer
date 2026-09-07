# DeepDive Drift yayın kontrol listesi

## Ortak

- [x] Windows ve Android için ortak otomatik sürüm; 34 commit için `1.0.34`, Android version code `35`
- [x] Otomatik birim testleri
- [x] Klavye, fare, gamepad ve dokunmatik girişleri
- [x] Çevrimdışı çalışma; reklam, analitik ve hesap SDK’sı yok
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
- [ ] Kapalı test/üretim erişimi için hesabın istediği test koşullarını tamamlama
- [ ] İmzalı AAB’yi yükleyip Play App Signing’i etkinleştirme

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
