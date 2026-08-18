# Steam yayın akışı

## 1. Depot içeriklerini üret

```powershell
.\mvnw.cmd -B -ntp -Psteam -pl lwjgl3 -am verify
```

Steamworks > Installation > General Installation bölümünde platforma göre şu başlatma seçeneklerini tanımlayın:

- Windows 64-bit: executable `DeepDiveDrift.exe`, argüman yok
- Linux 64-bit: executable `runtime/bin/java`, argüman `-jar DeepDiveDrift.jar`

Çalışma dizini her iki platformda depot kökü olarak bırakılabilir. Windows ve Linux depolarını geliştirici paketine ekleyin; aksi halde özel dal testi sırasında ilgili platforma dosya kurulmaz. Linux sürümünü en güncel Ubuntu LTS veya SteamOS üzerinde gerçek Steam istemcisinden ayrıca test edin.

Herkese açık Windows yayını öncesinde `DeepDiveDrift.exe` dosyasını kendi kod imzalama sertifikanızla imzalayın; Launch4j çıktısı teknik olarak çalışsa da imzasız EXE bazı antivirüs ürünlerinde yanlış pozitif üretebilir.

## 2. Mağaza varlıklarını hazırla

Güncel ölçülerdeki kapsül, kütüphane ve ikon dosyaları `steam/assets` altında; beş adet gerçek 1920×1080 oyun görüntüsü ise `store/screenshots/desktop` altında hazırdır. Kaynak çizimleri yeniden dışa aktarmak gerekirse:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-store-art.ps1
```

## 3. Steamworks kimlikleriyle VDF üret

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\prepare-steam-release.ps1 `
  -AppId APP_ID `
  -WindowsDepotId WINDOWS_DEPOT_ID `
  -LinuxDepotId LINUX_DEPOT_ID
```

Linux deposu açılmadıysa `-LinuxDepotId` atlanabilir. Betik önce Maven Steam profilini çalıştırır, ardından `steam/generated` altında VDF dosyalarını üretir. Bu dizin Git tarafından yok sayılır.

## 4. SteamPipe’a yükle

Steamworks SDK içindeki `steamcmd.exe` kullanılır:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\upload-steam-build.ps1 `
  -SteamCmdPath 'C:\SteamworksSDK\tools\ContentBuilder\builder\steamcmd.exe' `
  -Username STEAM_KULLANICI_ADI
```

Parola ve Steam Guard kodu betiğe kaydedilmez; SteamCMD gerektiğinde etkileşimli olarak ister. Yüklemeden sonra Steamworks’te build’i bir test dalına atayın, iki platformu test edin ve ancak ardından varsayılan dala taşıyın.

App ID, Depot ID, ortak hesap, banka/vergi bilgileri ve Steam Direct işlemleri yalnızca Steamworks hesabının sahibi tarafından tamamlanabilir.
