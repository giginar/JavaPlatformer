# DeepDiveDrift

DeepDiveDrift, libGDX ile geliştirilmiş hızlı tempolu bir su altı hayatta kalma oyunudur. Dalgıcın yüksekliğini kontrol et, farklı saldırı düzenlerine sahip deniz canlılarını zıpkınla avla, derinlik yükseltmelerini seç ve Abyss Leviathan'ı yen.

## Oynanış

- Oksijen saniyede azalır. Oksijen tüpleri büyük, düşman avlamak küçük miktarda oksijen kazandırır.
- Düşmanla çarpışmak türüne göre oksijen kaybettirir, combo'yu bozar ve kısa süreli dokunulmazlık verir.
- Üç saniye içinde yapılan ardışık avlar combo oluşturur; puan çarpanı en fazla `x3` olur.
- `1000`, `2500` ve `5000` puanda yeni derinlik seviyeleri açılır. Her geçişte rastgele üç yükseltmeden biri seçilir.
- `6500` puanda normal düşmanlar çekilir ve çok aşamalı Abyss Leviathan boss savaşı başlar.
- Boss yenildiğinde dalış tamamlanır; oksijen biterse koşu sona erer.
- Skor, oksijen ve hareket hesapları FPS'den bağımsızdır.

## Düşmanlar

- **Small Fish:** Dalgalı ve öngörülebilir başlangıç düşmanı.
- **Fast Fish:** Yüksek hızla geniş bir zikzak çizerek ilerler.
- **Shark:** Dalgıcın yüksekliğine kilitlenir, saldırısını haber verir ve ileri atılır.
- **Piranha Swarm:** Hedefin çevresinde formasyon kurar, ardından kama biçiminde hücum eder.
- **Abyss Leviathan:** Oyuncuyu takip eden, saldırı öncesi uyarı veren, öfke evresi ve ayrı can çubuğu bulunan final boss'u.

## Yükseltmeler

Her yükseltme en fazla üç seviyeye çıkarılabilir:

- **Rapid Fire:** Zıpkın bekleme süresini seviye başına `%18` azaltır.
- **Piercing:** Zıpkının delebileceği hedef sayısını artırır.
- **Air Recycler:** Oksijen tüketimini seviye başına `%15` azaltır.
- **Pressurized Tanks:** Tüplerden alınan oksijene seviye başına `+10` ekler.
- **Hydro Fins:** Yüzme çevikliğini seviye başına `%12` artırır.

## Görsel geri bildirim ve erişilebilirlik

- Yüzme baloncukları, zıpkın izleri, darbe kıvılcımları, patlamalar ve oksijen parçacıkları havuzlanarak tekrar kullanılır.
- Dalgıç ve düşmanlarda nefes alma, esneme, yalpalama ve saldırı animasyonları bulunur.
- Güçlü darbeler hit-stop ve ekran sarsıntısı üretir.
- Ayarlardan ekran sarsıntısı ve yanıp sönen hasar/uyarı efektleri ayrı ayrı kapatılabilir.

## Kontroller

| Tuş | İşlev |
| --- | --- |
| `Space`, `W`, `↑` | Yukarı yüz; bırakınca alçal |
| `Z`, `X` | Zıpkın at |
| `P`, `Esc` | Oyunu duraklat / devam et |
| `T` | Kontrol yardımını yeniden göster |
| `↑`, `↓`, `←`, `→`, `Enter` | Menü ve ayarlarda gezin |
| `←`, `→`, `1`-`3`, `Enter` | Yükseltme seç |
| `R`, `Enter` | Koşu bittikten sonra yeniden başla |
| `Esc` | Sonuç ekranından ana menüye dön |

## Görüntü ayarları

Ayarlar ekranından aşağıdaki seçenekler değiştirilebilir:

- Pencereli, çerçevesiz ve gerçek tam ekran modu
- `960x540` ile `2560x1440` arasında çözünürlükler
- VSync ve `60 / 120 / 144 / 240 / sınırsız` FPS limiti
- `4x MSAA` kenar yumuşatma; bu seçenek yeniden başlatmada uygulanır
- Müzik, ses efektleri, ekran sarsıntısı ve flaş efektleri

Son pencereli boyut ve tüm görüntü tercihleri oturumlar arasında saklanır.

## Çalıştırma

Gereksinim: Java 21.

Windows:

```powershell
.\gradlew.bat lwjgl3:run
```

Linux veya macOS:

```bash
./gradlew lwjgl3:run
```

Testleri çalıştırmak için:

```powershell
.\gradlew.bat test
```

Dağıtılabilir masaüstü JAR'ını üretmek için:

```powershell
.\gradlew.bat clean lwjgl3:jar
```

Çıktı `lwjgl3/build/libs/DeepDiveDrift-1.0.0.jar` altında oluşur.

Java'yı beraberinde taşıyan platform paketleri için `packageWinX64`, `packageLinuxX64`,
`packageMacM1` veya `packageMacX64` görevleri kullanılabilir. Bu görevler uygun Temurin 21
çalışma zamanını ilk kullanımda indirir ve çıktıları `lwjgl3/build/construo/dist` altında üretir.

## Launcher seçenekleri

Argümanlar Gradle üzerinden `--args="..."` ile veya doğrudan JAR'a verilebilir:

```powershell
.\gradlew.bat lwjgl3:run --args="--debug --windowed --resolution=1280x720 --fps=144"
java -jar lwjgl3/build/libs/DeepDiveDrift-1.0.0.jar --fullscreen --vsync --msaa
```

Desteklenen bayraklar:

- `--windowed`, `--borderless`, `--fullscreen`
- `--vsync`, `--no-vsync`
- `--msaa`, `--no-msaa`
- `--resolution=GENİŞLİKxYÜKSEKLİK`
- `--fps=60`, `120`, `144`, `240` veya `0` (sınırsız)
- `--debug`: OpenGL debug çıktısını ve oyun içi test kısayollarını açar

Debug modunda `F2` bir sonraki derinlik eşiğine ilerletir, `F3` boss savaşını başlatır.

## Mimari

- `DeepDiveDrift`: Ekran geçişlerini, ayar kaplamasını ve uygulama kaynaklarının yaşam döngüsünü yönetir.
- `BaseScreen`: Bütün ekranlara yeniden boyutlandırılabilir sabit `1280x720` oyun alanı sağlar.
- `GameSession` / `GameBalance`: Test edilebilir skor, oksijen, combo, zorluk ve yükseltme kurallarını tutar.
- `GameAssets`, `FontManager`, `AudioManager`: Ağır kaynakları bir kez yükler ve ekranlar arasında paylaşır.
- `ParticleSystem`: Sınırlı ve havuzlanan parçacıkların güncelleme/çizim yaşam döngüsünü yönetir.
- `DisplaySettings` / `DisplaySettingsStore`: Çalışma zamanı görüntü ayarlarını uygular ve masaüstü tercihlerini saklar.
- `Lwjgl3Launcher`: HDPI, pencere modu, VSync, FPS, MSAA, debug çıktısı ve özel uygulama ikonlarını yapılandırır.

## Proje yapısı

```text
assets/                         Görsel, ses ve font dosyaları
core/src/main/java/com/game/
├── diver/                      Dalgıç, zıpkın, oksijen tüpü ve arka plan
├── effects/                    Havuzlanan parçacık sistemi
├── enemies/                    Düşman davranışları ve boss
├── manager/                    Texture, font ve ses yönetimi
├── model/                      Oyun durumu, denge ve yükseltmeler
├── screen/                     Ana menü, ayarlar ve oyun ekranları
└── settings/                   Görüntü ayarları ve kalıcı tercihler
core/src/test/                  JUnit 5 denge, oturum, yükseltme ve efekt testleri
lwjgl3/                         Masaüstü launcher, ikonlar ve paketleme görevleri
```
