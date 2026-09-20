# Google Play veri güvenliği taslağı

**POLİTİKA YAPILANDIRMASI BEKLİYOR. Bu dosya Play Console beyanı olarak kullanılamaz.**

Phase 6 Android paketinin doğrulanan teknik durumu:

- Google Mobile Ads Next-Gen `1.4.0` ve Google UMP `4.0.0` paketlenir.
- Manifestte `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_BASIC_PHONE_STATE` ve
  `com.google.android.gms.permission.AD_ID` bulunur.
- Reklam modu `DISABLED`, `TEST` veya dış kimlik gerektiren `PRODUCTION` olabilir.
- Firebase Analytics, Crashlytics, ilişkilendirme, hesap, faturalandırma, ödeme veya
  konum SDK'sı eklenmemiştir.
- Oyun ilerlemesi ve tercihler cihazda yerel olarak saklanır.

Veri türleri, toplama/paylaşma, amaçlar, saklama, silme, çocuklar ve hedef kitle
beyanları Phase 7'de resmi Google SDK davranışı ve son ürün politikasıyla eşlenmelidir.
