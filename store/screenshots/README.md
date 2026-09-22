# Google Play ekran görüntüsü planı

Bu klasördeki dosyalar gerçek oyun framebuffer çıktılarıdır; üretilmiş veya kurgulanmış
oynanış görselleri değildir. Nihai İngilizce Google Play telefon seti Pixel 8 Android
API 36 AVD'den ADB `screencap` ile alınmıştır. Yedi dosyanın tamamı `1920×1080`, 24-bit
RGB PNG'dir ve varsayılan `en-US` mağaza dili için İngilizce arayüz gösterir.

## Google Play telefon — en-US

Yol: `google-play-phone/en-US/`

| Dosya | İçerik | Kaynak | Durum |
| --- | --- | --- | --- |
| `01-gameplay-core.png` | Sunlit Reef temel zıpkınlı oynanış | Pixel 8 / Android API 36 / ADB screencap | `CURRENT / PLAY STORE READY` |
| `02-deep-environment.png` | Stage 3 Current Maze oynanışı | Pixel 8 / Android API 36 / geliştirme kapılı stage yakalama / ADB screencap | `CURRENT / PLAY STORE READY` |
| `03-abyssal-rift.png` | Stage 5 Abyssal Rift, boss öncesi oynanış | Pixel 8 / Android API 36 / geliştirme kapılı stage yakalama / ADB screencap | `CURRENT / PLAY STORE READY` |
| `04-dive-shop.png` | Pressure Pearls ve kalıcı ekipman | Pixel 8 / Android API 36 / ADB screencap | `CURRENT / PLAY STORE READY` |
| `05-suit-selection.png` | Dört gerçek dalış kostümü | Pixel 8 / Android API 36 / ADB screencap | `CURRENT / PLAY STORE READY` |
| `06-achievements.png` | Mevcut achievement ilerlemesi | Pixel 8 / Android API 36 / ADB screencap | `CURRENT / PLAY STORE READY` |
| `07-difficulty-challenges.png` | Easy/Normal/Hard ve challenge modifier seçenekleri | Pixel 8 / Android API 36 / ADB screencap | `CURRENT / PLAY STORE READY` |

İlk üç dosya gerçek oynanıştır. Stage 3 ve Stage 5 kareleri yalnızca üretimde kapalı
`TEST_SHORTCUTS_ENABLED` geliştirme kapısı üzerinden sahne başlangıcına alınmış; oyun,
HUD, düşmanlar ve tehlikeler Android üzerinde gerçek zamanlı çizilmiştir. Debug overlay
kapalıdır. Stage 5 karesi boss doğmadan önce alınmıştır ve hiçbir boss varlığı içermez.

## Mevcut dosyalar

| Dosya | Konu | Durum | İşlem |
| --- | --- | --- | --- |
| `desktop/01-main-menu.png` | Eski ana menü | `STALE` | Güncel menüde Dive Shop, Achievements, Sound ve Controls seçenekleri de var. Android telefonda yeniden çek. |
| `desktop/02-options.png` | Masaüstü seçenekleri | `STALE` | Pencere modu, çözünürlük, VSync ve FPS gibi masaüstüne özgü kontroller içeriyor. Telefon mağaza görselinde kullanma. |
| `desktop/03-gameplay.png` | Erken aşama oynanış | `RECAPTURE REQUIRED` | Gerçek oynanış kanıtıdır; son Android dokunmatik arayüzünü gösterecek biçimde Pixel 8'de yeniden çek. |
| `desktop/04-gameplay-action.png` | Zıpkınlı oynanış aksiyonu | `RECAPTURE REQUIRED` | Gerçek oynanış kanıtıdır; son Android sürümünden daha okunaklı bir aksiyon anı yeniden çek. |
| `desktop/05-leviathan.png` | Eski boss karşılaşması | `DO NOT USE` | `ABYSS LEVIATHAN` adı eskidir ve görüntüde hak incelemesi tamamlanmamış boss varlığı bulunur. Dosyayı yalnızca tarihsel kanıt olarak tut. |

`BOSS SCREENSHOT: RIGHTS BLOCKED` — Abyssal Octopus sağlayıcı/dağıtım koşulları
çözülmeden boss içeren yeni veya eski hiçbir ekran görüntüsü üretim pazarlama varlığı
olarak onaylanamaz.

## Telefon seti kapsamı

Pixel 8 Android sürümünden birbirini tekrar etmeyen şu yedi görüntü hazırdır:

1. Oksijen, skor ve zıpkın aksiyonunu birlikte gösteren temel oynanış.
2. Erken görüntüden belirgin biçimde farklı, daha derin bir su altı ortamı ve tehditler.
3. Stage 5 / Abyssal Rift yoğunluğu; boss görünmeden önce çekilebilir.
4. Dive Shop içindeki kalıcı ekipman ve Pressure Pearls.
5. Dört dalış kostümünü gösteren suit sekmesi.
6. Achievements ekranı.
7. Easy/Normal/Hard ve meydan okuma kurallarını gösteren Prepare Your Dive ekranı.

Sekizinci yuva için güncel ana menü veya koşu yükseltmesi seçimi kullanılabilir. Boss
görüntüsü ancak hak engeli çözüldükten sonra, güncel `ABYSSAL OCTOPUS` adıyla yeniden
çekilerek bu yuvanın yerine geçebilir.

İlk üç görsel gerçek oynanış olmalıdır. Pazarlama metin bindirmeleri isteğe bağlıdır;
ham görüntüler ayrıca korunmalıdır. Kullanılabilecek kısa başlıklar: “SURVIVE FIVE
STAGES”, “BUILD YOUR DIVER”, “CHOOSE YOUR CHALLENGE”. Türkçe mağaza görsellerinde bu
ek başlıklar ayrıca Türkçeleştirilmelidir. Başlıklar görüntünün en fazla yaklaşık %20'sini
kaplamalı; ödül, sıralama, fiyat veya yükleme çağrısı içermemelidir.

## Telefon yakalama ve dışa aktarma

- Oyun yataydır. Pixel 8 AVD'nin mevcut doğal yakalaması `2400×1080` yataydır.
- Google Play ekran görüntülerinde kısa kenar en az 320 px, uzun kenar en fazla 3840 px
  olmalı ve uzun kenar kısa kenarın iki katını aşmamalıdır. Bu nedenle `2400×1080`
  dosya doğrudan uygun değildir.
- Önerilen çıktı `1920×1080` (16:9) JPEG veya alfa kanalı olmayan 24-bit PNG'dir.
  Doğal yakalamadan yalnızca içerik ve tüm HUD güvenle korunuyorsa iki yandan toplam
  480 px kırp. Kırpma arayüzü kesiyorsa ekranı 1920×1080 mantıksal çözünürlükte yeniden
  yakala. Görüntüyü esnetme veya oranını bozma.
- Tam ekran/immersive durumu yerleştikten sonra yakala. Bildirim, operatör adı veya
  geçici sistem çubukları görünüyorsa ekranı boyamak yerine yeniden yakala.
- Google Play yayımlamak için en az iki görüntü ister ve telefon türü için en fazla
  sekiz görüntü kabul eder. Oyunların öne çıkarılma uygunluğu için en az üç adet
  1920×1080 yatay oynanış görüntüsü hedefle. Bu proje için yedi veya sekiz görüntü
  önerilir.
- Tablet görüntüleri isteğe bağlıdır. Hazırlanırsa gerçek tablet düzeninden en az dört
  adet 16:9 yatay, 1080–7680 px aralığında görüntü kullan.

Resmî gereksinim: [Google Play önizleme varlığı gereksinimleri](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB).

## Mevcut geliştirme araçları

Masaüstü yakalama seçenekleri `Lwjgl3Launcher` üzerinden `--capture=<dosya>`,
`--capture-delay=<saniye>`, `--capture-exit`, `--autostart`, `--open-options`,
`--open-controls`, `--open-about`, `--open-store`, `--open-setup`,
`--open-achievements`, `--open-suits`, `--capture-autoplay`, `--capture-boss` ve
`--hide-tutorial` argümanlarıyla kullanılabilir. Ekran açma/yakalama argümanları masaüstü
geliştirme akışındadır. Boss'a atlama ve kısaltılmış stage süresi mevcut geliştirme
kapısına bağlıdır; `TEST_SHORTCUTS_ENABLED` üretimde `false` kalır.

Android `deepdive.qa.captureStage` intent extra'sı yalnızca derleme zamanı geliştirme
kapısı açıkken Stage 2–5 başlangıcını bellekte seçer. Üretim sabiti `false` olduğunda
etkisizdir; debug overlay ayrı bir opt-in özelliktir. Araç ilerlemeyi, ekonomiyi veya
save verisini değiştirmez.
