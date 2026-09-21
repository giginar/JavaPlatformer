# Google Play Veri Güvenliği çalışma sayfası

Son inceleme: 21 Eylül 2026

**POLİTİKA YAPILANDIRMASI BEKLİYOR. Bu belge Play Console'a gönderilmiş bir beyan değildir. Üretim AAB'si, AdMob hesabı yapılandırması ve Play Console soru metniyle son kez karşılaştırılmalıdır.**

Durum etiketleri:

- `CONFIRMED`: kod, paket veya güncel resmi SDK belgesiyle doğrulandı.
- `PENDING PRODUCT DECISION`: hedef kitle, ülke, dil veya ürün seçimi gerekiyor.
- `PENDING CURRENT POLICY VERIFICATION`: mevcut resmi belge, kullanılan bileşen için kesin sonuç vermiyor.
- `MANUAL PLAY CONSOLE VERIFICATION`: gönderim anındaki Console sorusu ve yayın AAB'siyle doğrulanmalı.

## Önerilen üst düzey cevaplar

| Play Console konusu | Önerilen cevap | Durum | Dayanak |
| --- | --- | --- | --- |
| Uygulama veri topluyor veya paylaşıyor mu? | Evet | `CONFIRMED` | Google Mobile Ads Next-Gen otomatik toplama ve paylaşma bildirimi |
| Toplanan veriler aktarım sırasında şifreleniyor mu? | Google Mobile Ads verileri için evet; bütün yayın paketinin Console cevabı son AAB ile doğrulanmalı | `MANUAL PLAY CONSOLE VERIFICATION` | Google, SDK verilerinin TLS ile şifrelendiğini bildirir |
| Kullanıcı veri silme isteğinde bulunabilir mi? | Yerel oyun verileri uygulama verileri temizlenerek veya uygulama kaldırılarak silinir. Yayıncı hesabı/sunucusu yoktur. Google tarafından işlenen veriler ve Console'daki küresel silme cevabı ayrıca doğrulanmalı | `MANUAL PLAY CONSOLE VERIFICATION` | Yerel depolama ve Google SDK ayrımı |
| Hesap oluşturuluyor mu? | Hayır | `CONFIRMED` | Hesap ve kimlik doğrulama özelliği yok |
| Çocuklara yönelik uygulama veri uygulamaları | Hedef yaş grupları seçilmedi | `PENDING PRODUCT DECISION` | `TARGET AUDIENCE DECISION REQUIRED` |

## Play veri türü matrisi

“Toplanıyor” ve “paylaşılıyor” değerleri yalnızca teorik SDK yeteneğine dayanmaz. Google'ın kullanılan Mobile Ads Next-Gen SDK için yayımladığı otomatik davranışı gösterir. Nihai cevaplar, Play'de dağıtılan üretim sürümünde reklamların etkin olacağı varsayımıyla hazırlanmıştır.

| Play veri kategorisi | Toplanıyor? | Paylaşılıyor? | İşleme amacı | Zorunlu/isteğe bağlı | Geçici? | Kullanıcı silme talep edebilir mi? | Kaynak/kanıt | Durum |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Konum → Yaklaşık konum | Evet | Evet | Reklam veya pazarlama; analitik; sahtekârlığı önleme, güvenlik ve uyumluluk | Öneri: zorunlu; bütün kullanıcılara sunulan genel bir SDK veri opt-out'u yok. Console'da doğrulanmalı | Hayır olarak beyan edilmeli; geçici işleme iddiasını destekleyen kanıt yok | Yayıncı sunucusunda kopya yok; IP/Google verisi için Google denetimleri geçerli. Console cevabı doğrulanmalı | GMA'nın IP adresi toplaması ve IP'den genel konum tahmini | `CONFIRMED`; zorunluluk/silme: `MANUAL PLAY CONSOLE VERIFICATION` |
| Uygulama etkinliği → Uygulama etkileşimleri | Evet | Evet | Reklam veya pazarlama; analitik; sahtekârlığı önleme, güvenlik ve uyumluluk | Öneri: zorunlu; Console'da doğrulanmalı | Hayır olarak beyan edilmeli | Yayıncı sunucusunda kopya yok; Google denetimleri geçerli. Console cevabı doğrulanmalı | GMA'nın uygulama açılışı, dokunma ve video görüntüleme gibi ürün etkileşimleri bildirimi | `CONFIRMED`; zorunluluk/silme: `MANUAL PLAY CONSOLE VERIFICATION` |
| Uygulama bilgileri ve performansı → Tanılama | Evet | Evet | Reklam veya pazarlama; analitik; sahtekârlığı önleme, güvenlik ve uyumluluk | Öneri: zorunlu; Console'da doğrulanmalı | Hayır olarak beyan edilmeli | Yayıncı sunucusunda kopya yok; Google denetimleri geçerli. Console cevabı doğrulanmalı | GMA'nın açılış süresi, takılma oranı ve enerji kullanımını bildirmesi | `CONFIRMED`; zorunluluk/silme: `MANUAL PLAY CONSOLE VERIFICATION` |
| Cihaz veya diğer kimlikler | Evet | Evet | Reklam veya pazarlama; analitik; sahtekârlığı önleme, güvenlik ve uyumluluk | Öneri: zorunlu; yaş/gizlilik sinyalleri bazı kimlikleri engelleyebilir, ancak kategori genel SDK akışıdır | Hayır olarak beyan edilmeli | AAID Android ayarlarından sınırlanabilir, sıfırlanabilir veya silinebilir; diğer Google verileri için Google denetimleri geçerli. Console cevabı doğrulanmalı | GMA'nın AAID, App Set ID ve uygun olduğunda hesapla ilişkili diğer kimlikler bildirimi; birleşik manifestte `AD_ID` | `CONFIRMED`; silme: `MANUAL PLAY CONSOLE VERIFICATION` |

## Yerel birinci taraf verileri

| Veri | Saklama/akış | Play Veri Güvenliği sonucu | Durum |
| --- | --- | --- | --- |
| Pressure Pearls, ekipman seviyeleri, kurulum zamanları ve kostümler | libGDX Preferences ile yalnızca cihazda | Cihaz dışına gönderilmediği için “toplanan” veri değildir | `CONFIRMED` |
| Başarımlar, skor, koşu sıra/tamamlama kayıtları ve oyun sayaçları | Yalnızca cihazda | “Toplanan” veri değildir | `CONFIRMED` |
| Zorluk, meydan okuma, ses, görüntü, sarsıntı ve flaş ayarları | Yalnızca cihazda | “Toplanan” veri değildir | `CONFIRMED` |
| Devam eden koşu durumu | Bellekte, koşu süresince | “Toplanan” veri değildir | `CONFIRMED` |

Android manifesti `allowBackup=false` kullanır ve yedekleme kuralları tüm uygulama verisini hariç tutar. Birinci taraf kodunda bu verileri sunucuya, hesaba veya buluta gönderen bir yol bulunmaz.

## UMP ve izin tercihleri

UMP uygulama açılışında izin durumunu günceller, gerektiğinde Google tarafından sağlanan formu gösterir ve gizlilik seçeneklerinin yeniden açılmasını sağlar. Kod reklam isteğini `canRequestAds()` sonucuna bağlar. UMP'nin GMA bildirimine ek, ayrı bir Play veri kategorisi oluşturduğunu doğrulayan sürüme özgü resmi bir veri açıklaması bulunamadı; kanıtsız ek kategori işaretlenmemelidir.

UMP `4.0.0` için Reklam Kimliği kullanımı beklenmez: Google'ın sürüm notları UMP `2.2.0` ile Advertising ID kullanımının tamamen kaldırıldığını belirtir. Paketteki `AD_ID` izninin kaynağı GMA Next-Gen'dir.

UMP'ye özgü ek veri kategorisi: `PENDING CURRENT POLICY VERIFICATION`.

## Üretim öncesi zorunlu kontrol

- Son üretim AAB'sinin SDK ve izin envanterini Play App Bundle Explorer ile karşılaştırın.
- Play Console'un o tarihteki “toplanan”, “paylaşılan”, “zorunlu/isteğe bağlı”, “geçici” ve silme sorularını yeniden okuyun.
- Hedef kitle çocukları içeriyorsa Families veri uygulamalarını ve reklam yapılandırmasını tamamlamadan formu göndermeyin.
- AdMob Privacy & Messaging mesajlarının seçilen ülkelerde yayımlandığını ve gizlilik seçeneklerinin cihazda göründüğünü doğrulayın.
- Yayıncı adı, iletişim e-postası ve HTTPS gizlilik politikası URL'sini doldurun.

Resmi dayanaklar: [Play Veri Güvenliği tanımları](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en), [GMA Next-Gen veri açıklaması](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure), [UMP sürüm notları](https://developers.google.com/admob/android/next-gen/privacy/release-notes).
