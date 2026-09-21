# Gerçek oyun ekran görüntüleri

`desktop` klasöründeki beş PNG, oyunun gerçek framebuffer çıktısından 1920×1080 çözünürlükte alınmıştır:

- `01-main-menu.png`
- `02-options.png`
- `03-gameplay.png`
- `04-gameplay-action.png`
- `05-leviathan.png`

İlk dört görüntü güncel masaüstü mağaza taslağında kullanılabilir. `05-leviathan.png`, boss adının Abyssal Octopus olarak güncellenmesinden önce alınmıştır ve mağazaya yüklenmeden önce yeniden çekilmelidir. Google Play’e gönderimden önce dokunmatik kontrolleri ve son Android arayüzünü doğru biçimde göstermek için ayrıca gerçek bir Android cihazdan telefon ekran görüntüleri alınmalıdır.

Masaüstü yakalama seçenekleri `Lwjgl3Launcher` üzerinden `--capture=<dosya>`, `--capture-delay=<saniye>`, `--capture-exit`, `--autostart`, `--open-options`, `--capture-autoplay`, `--capture-boss` ve `--hide-tutorial` argümanlarıyla kullanılabilir.
