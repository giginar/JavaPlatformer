# Project Blue: Deep Drift yayın kontrol listesi

- Yayıncı/stüdyo: `Blueborn Games`
- Destek: `ykucukcinar@gmail.com`
- Android application ID: `com.game.diver.deepdivedrift`
- Beklenen Pages taban URL'si: `https://giginar.github.io/JavaPlatformer/`
- `GITHUB PAGES MANUAL ACTIVATION REQUIRED`
- `GITHUB PAGES ACTIVATION/URL VERIFICATION REQUIRED`

## Ortak

- [x] Windows ve Android için ortak otomatik sürüm; `versionName` commit sayısından, Android `versionCode` temel değer + commit sayısından üretilir
- [x] Otomatik birim testleri
- [x] Klavye, fare, gamepad ve dokunmatik girişleri
- [x] Temel çevrimdışı çalışma Phase 9 Pixel 8 testinde doğrulandı; Google Ads + UMP var, bağımsız analitik ve hesap SDK’sı yok
- [x] Boss dışındaki görsel, ikon ve mağaza görseli haklarının belgelenmesi (CC0/OFL kaynak zinciri eklendi)
- [ ] AI boss sağlayıcı/dağıtım koşullarını sonuçlandırma; boss varlığı ve boss içeren pazarlama görselleri bu adıma bağlıdır
- [ ] Ses ve müzik haklarının belgelenmesi veya dosyaların lisanslı alternatiflerle değiştirilmesi
- [ ] Gerçek cihazlarda son kalite testi
- [x] Herkese açık yayıncı/stüdyo markası `Blueborn Games` ve destek-gizlilik e-postası `ykucukcinar@gmail.com` olarak belirlendi
- [ ] Yasal geliştirici/veri sorumlusu adını ve hesap adresi/telefon doğrulamasını tamamlama (`LEGAL NAME INPUT REQUIRED`)
- [ ] GitHub Pages'ı GitHub Actions kaynağıyla etkinleştirip beklenen destek/gizlilik URL'lerini canlı olarak doğrulama
- [ ] JLayer/JOrbis bildirim ve hukuk incelemesini tamamlama

## Google Play

- [x] Paket adı: `com.game.diver.deepdivedrift`
- [x] `targetSdk 36`, dört ABI ve AAB üretimi
- [x] APK ZIP hizalaması ve dört ABI için 16 KB ELF uyumluluğu doğrulandı
- [x] Android ses dosyaları sıkıştırılmadan paketleniyor; evrensel APK ve örnek ARM64 split üzerinde doğrulandı
- [x] Veri Güvenliği çalışma sayfası ile EN/TR mağaza ve gizlilik taslakları hazırlandı
- [x] 512×512 Play ikon taslağı mevcut; dosya biçimi ve boyutu doğrulandı
- [x] 1024×500 özellik görseli taslağı mevcut; dosya biçimi ve boyutu doğrulandı
- [x] Nihai mağaza adı `Project Blue: Deep Drift`; uygulama etiketi, About/Legal, mağaza metinleri ve oluşturulan başlık görsellerinde aynı ad kullanılıyor
- [ ] Android için uyarlanabilir (adaptive) launcher icon foreground/background kaynaklarını hazırlama ve maske önizlemelerini doğrulama
- [ ] Google Play geliştirici hesabı ve kimlik doğrulaması
- [ ] Özel upload keystore oluşturma ve güvenli yedek
- [ ] Gizlilik politikasındaki yasal veri sorumlusu ve yürürlük tarihi alanlarını doldurup beklenen Pages URL'sini etkin HTTPS adresi olarak doğrulama
- [x] Pixel 8 Android API 36 sürümünden yedi yatay `en-US` telefon ekran görüntüsü alma; ilk üçü 1920×1080 gerçek oynanıştır
- [x] Eski `05-leviathan.png` görüntüsünü güncel telefon setinden çıkarma; hak engeli çözülmedikçe boss ekran görüntüsü kullanmama
- [ ] İsteğe bağlı tablet ekran görüntülerini yalnızca gerçek tablet düzeninden hazırlama
- [ ] İkonu, özellik görselini ve telefon ekran görüntülerini Play Console’a yükleme
- [ ] İçerik derecelendirmesi, hedef kitle ve uygulama erişimi anketleri
- [ ] Amaçlanan hedef yaş gruplarını seçme; çocuklar dahilse Families, nötr yaş ekranı ve reklam SDK uygunluğunu tamamlama
- [ ] Yayın ülkelerini ve nihai mağaza/gizlilik politikası dillerini seçme
- [ ] Veri Güvenliği çalışma sayfasını son üretim AAB'si ve güncel Play Console sorularıyla doğrulama
- [ ] Advertising ID beyanını son hedef kitle ve `AD_ID` izniyle doğrulama
- [ ] AdMob Privacy & Messaging bölgesel mesajlarını yayımlama ve gizlilik seçeneklerini gerçek cihazda doğrulama
- [ ] Ödüllü reklam faydasını ayrıca onaylama; onaya kadar oyuncuya gösterilen yerleşimi kapalı tutma
- [ ] Gerçek AdMob yapılandırmasını ve üretim reklam birimi kimliklerini ekleme/doğrulama
- [ ] Kapalı test/üretim erişimi için hesabın istediği test koşullarını tamamlama
- [ ] İmzalı AAB'yi yükleyip Play App Signing'i etkinleştirme
- [ ] 16 KB sayfa boyutlu gerçek çalışma ortamında runtime doğrulaması
- [ ] Ertelenen Phase 11 uzun süre/endurance QA ve belirlenmiş düşük seviye cihaz sürdürülebilir performans QA'sı

Phase 7 iç politika çalışma sayfası: [Google Play politika, hedef kitle ve Veri Güvenliği denetimi](google-play/phase-7-policy-audit.md).

Yayına engel durumlar geçerlidir: sekiz ses varlığının ticari kullanım kanıtı, AI boss sağlayıcı/dağıtım koşulları, JLayer/JOrbis bildirim incelemesi, gerçek AdMob yapılandırması ve üretim kimlikleri, üretim imzalama, imzasız üretim AAB'si, gerçek 16 KB çalışma ortamı, ertelenen Phase 11 uzun süre/endurance QA'sı, belirlenmiş düşük seviye cihaz sürdürülebilir performans QA'sı ve eski boss ekran görüntüsünün değiştirilmesi tamamlanmamıştır.

## Steam

- [x] Java gerektirmeyen Windows ve Linux paketleri
- [x] Açılmış depot klasörleri ve SteamPipe VDF üreticisi
- [x] Klavye/fare ve gamepad desteği
- [x] Türkçe/İngilizce mağaza açıklaması taslakları
- [ ] Steam grafik varlıkları ile dört boss dışı 1920×1080 masaüstü görüntüsü mevcut; eski boss görüntüsü ad ve hak engeli nedeniyle kullanılmamalı
- [x] Windows paketinde yerel açılış testi
- [ ] Steamworks ortak hesabı, banka/vergi ve Steam Direct işlemleri
- [ ] App ID ile Windows/Linux Depot ID’lerini betiğe verme
- [ ] Grafik varlıklarını ve ekran görüntülerini Steamworks’e yükleme
- [ ] Desteklenen diller, sistem gereksinimleri ve içerik anketini doldurma
- [ ] SteamPipe build’ini özel test dalına yükleme ve temiz makinelerde test
- [ ] Mağaza sayfası ile build’i Valve incelemesine gönderme
