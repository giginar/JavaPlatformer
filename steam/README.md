# Steam yayın akışı

## 1. Depot içeriklerini üret

```powershell
.\gradlew.bat :lwjgl3:prepareSteam
```

Windows başlangıç dosyası `DeepDiveDrift.exe`’dir. Çalışma dizini depot kökü olarak bırakılabilir. Linux başlangıç dosyası `DeepDiveDrift`’tir.

Steamworks > Installation > General Installation bölümünde ayrı başlatma seçenekleri tanımlayın:

- Windows 64-bit: `DeepDiveDrift.exe`
- Linux 64-bit: `DeepDiveDrift`

Windows ve Linux depolarını geliştirici paketine ekleyin; aksi halde özel dal testi sırasında ilgili platforma dosya kurulmaz. Linux sürümünü en güncel Ubuntu LTS veya SteamOS üzerinde gerçek Steam istemcisinden ayrıca test edin.

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

Linux deposu açılmadıysa `-LinuxDepotId` atlanabilir. Oluşan dosyalar `steam/generated` altında tutulur ve Git tarafından yok sayılır.

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
