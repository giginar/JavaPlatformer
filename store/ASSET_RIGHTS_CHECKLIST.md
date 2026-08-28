# Ticari varlık hakları kontrolü

## Durum

| Varlık grubu | Durum | Kanıt |
| --- | --- | --- |
| Oyun görselleri | **İNCELEME** | CC0 kaynak zincirine ek olarak `enemy_octopus_boss.png` üretken AI ile oluşturuldu; kayıt `assets/GENERATED_ASSETS.md` içinde |
| Windows/macOS/Android ikonları | **HAZIR** | CC0 dalgıçtan ve projeye özgü geometrilerden deterministik üretim |
| Steam ve Google Play görselleri | **HAZIR** | Yalnızca doğrulanmış CC0/OFL kaynaklarından deterministik üretim |
| Masaüstü ekran görüntüleri | **HAZIR** | Gerçek oyun framebuffer çıktısı; yalnızca doğrulanmış oyun görsellerini içerir |
| Fontlar | **HAZIR** | Orbitron, SIL Open Font License 1.1; tam metin dağıtımda |
| Sesler ve müzik | **BEKLİYOR** | Mevcut WAV/MP3 dosyalarının kaynak veya satın alma kayıtları bulunamadı |

## Görsel kaynak zinciri

- Yazarın sayfası: https://ansimuz.itch.io/underwater-diving
- Sayfadaki lisans: Creative Commons Zero v1.0 Universal
- Sayfadaki AI beyanı: “No generative AI was used”
- Erişim tarihi: 2026-08-08
- Kaynak dosyalar ve özetler: `third_party/cc0/ansimuz-underwater-diving/SOURCE.md`
- CC0 tam metni: `third_party/cc0/CC0-1.0.txt`
- Yeniden üretim komutu: `powershell -ExecutionPolicy Bypass -File scripts/build-licensed-art.ps1`
- Dağıtılan bildirim: `assets/THIRD_PARTY_NOTICES.md`
- Üretken AI ile oluşturulan oyun varlığı kaydı: `assets/GENERATED_ASSETS.md`

Mızrak, oksijen tüpü, baloncuklar, ışık huzmeleri ve basit düzen geometrileri
`scripts/build-licensed-art.ps1` içinde projeye özgü olarak çizilir. Başlık ve mağaza
metinleri, OFL lisanslı Orbitron ile oluşturulur. Önceki kaynağı belirsiz oyun
görselleri, ikonlar ve AI ile üretilmiş mağaza illüstrasyonları bu temiz kaynak
zinciriyle değiştirilmiştir. Kullanılmayan `libgdx.png` ve `white_pixel.png` dağıtımdan
çıkarılmıştır.

## Onay kutuları

- [ ] Üretken AI ile oluşturulan ahtapot varlığı için platform beyanı ve dağıtım koşulları doğrulandı
- [x] Uygulama ikonlarının kaynak ve üretim kaydı eklendi
- [x] Steam ve Google Play mağaza görsellerinde lisanssız içerik yok
- [x] Font lisansı dağıtıma eklendi
- [x] Gerekli üçüncü taraf bildirimi dağıtıma eklendi
- [ ] Tüm seslerin ve müziğin ticari kullanım hakkı doğrulandı

Bu nedenle görsel zincirindeki CC0/OFL varlıklar hazırdır; üretken AI ile oluşturulan ahtapot
varlığının platform beyanı ayrıca doğrulanmalıdır. Oyunun tüm ticari varlık kontrolünün hazır
sayılabilmesi için ses ve müzik dosyaları da ayrıca değiştirilmelidir veya özgün lisans
kanıtları bulunmalıdır.
