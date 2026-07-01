# Craft Light Casino Plugin

Paper 1.21+ (1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4...) için geliştirilmiş, kendi ekonomisi (**LCoin**)
olan gazino ve market eklentisi.

## Derleme (Build)

Bu proje Gradle kullanır. `gradlew` / `gradlew.bat` script'leri ve `gradle/wrapper/gradle-wrapper.properties`
dosyası projede hazır. **Tek eksik**, internet erişimi olmayan bir ortamda üretildiği için
`gradle/wrapper/gradle-wrapper.jar` binary dosyasının kendisi eklenemedi. Bunu tamamlamak çok kolay,
aşağıdaki iki yoldan birini seç:

### Yöntem A (Önerilen - IntelliJ IDEA)
1. Projeyi IntelliJ IDEA ile aç (Gradle projesi olarak otomatik algılar).
2. IDE, internet varsa `gradle-wrapper.jar` dosyasını otomatik indirir.
3. Sağdaki Gradle panelinden `build` -> `shadowJar` görevini çalıştır.
4. Çıktı: `build/libs/CraftLightCasino-1.0.0.jar`

### Yöntem B (Bilgisayarında Gradle kuruluysa)
```bash
# Herhangi bir gradle sürümü kurulu olsun yeterli
gradle wrapper --gradle-version 8.10
./gradlew shadowJar
```

### Yöntem C (Gradle hiç kurulu değilse)
1. https://gradle.org/releases/ adresinden Gradle 8.10 indir, zip'i aç.
2. `gradle-8.10/bin` klasörünü PATH'e ekle ya da tam yoldan çalıştır: `gradle-8.10\bin\gradle.bat wrapper`
3. Proje klasöründe `gradle wrapper` komutunu çalıştır -> `gradle-wrapper.jar` otomatik oluşur.
4. Ardından `./gradlew shadowJar` ile derle.

Derleme bitince oluşan `.jar` dosyasını sunucunun `plugins/` klasörüne at, sunucuyu başlat.

## Komutlar

### Gazino
| Komut | Açıklama |
|---|---|
| `/alanayarla <id 1-20> 1` | O ID'li gazino alanının 1. köşesini, bulunduğun yere ayarlar |
| `/alanayarla <id> 2` | 2. köşeyi ayarlar |
| `/alanayarla <id> blok` | Baktığın bloğu, renk değiştirecek "gökkuşağı beton" olarak ayarlar |
| `/loyna <id>` | O gazinonun oyun menüsünü açar |
| `/loyna <bahis> <id>` | Menüyü açar ve direkt belirttiğin miktarda bahis ekler |
| `/gazinohologram` | Bulunduğun yere "Craft Light" başlıklı açıklama hologramı koyar |

**Oyun akışı:** Menüde 5 farklı renkte at var. Önce alttaki **1) Bahis Ekle** ya da
**2) Bahsi Kendin Ayarla** ile bahsini belirle, sonra bir ata tıkla (seçilir),
aynı ata tekrar tıklayınca yarış başlar. Gerçek dünyadaki beton blok hızlıca renk
değiştirir ve rastgele bir renkte durur. Doğru tahmin edersen bahsin **x2** olur.
**3) Bahsi Geri Çek** ile istediğin an bahsini iptal edip parani geri alabilirsin.
Menüyü kapatırsan (yarış başlamadıysa) bahsin otomatik iade edilir; dupe/kaybolma riski yoktur.

### LCoin (Ekonomi)
| Komut | Açıklama |
|---|---|
| `/lcoin` | Bakiyeni gösteren menüyü açar (kafan + açıklama) |
| `/lcoinver <oyuncu> <miktar>` | (Yetkili) Oyuncuya LCoin verir |
| `/lcoinparacek <miktar> <oyuncu>` | (Yetkili) Oyuncudan senin hesabına LCoin çeker. Oyuncunun parası yetersizse sadece elindeki kadarını çeker, asla eksi bakiye oluşturmaz |

### Market
| Komut | Açıklama |
|---|---|
| `/lmarket` | Marketi açar (satın alma) |
| `/lmarketesyaekle <isim> <#id>` | Elindeki eşyayı o isim/ID ile markete ekler (max 15 eşya) |
| `/lmarketitemsil <isim> <#id>` | Eşyayı market'ten siler |
| `/lmarketitemyeriayarla` | Market'i düzenleme modunda açar; bir eşyaya tıkla sonra taşımak istediğin slota tıkla |
| `/lmarketparaayarla <isim> <#id> <fiyat>` | O eşyanın fiyatını ayarlar |

Market'te bir oyuncu eşyaya tıklayınca: parası yetiyorsa **önce para çekilir, sonra eşya verilir**
(sırası dupe'a karşı önemlidir), envanterde yer yoksa işlem hiç başlamaz, başarılı olursa
sohbete `✔ Başarıyla İtemi Satın Aldın!` mesajı düşer.

## Yetkiler
- `craftlight.admin` (varsayılan: op) — tüm yönetici komutlarını kullanabilir (`/alanayarla`,
  `/lcoinver`, `/lcoinparacek`, `/gazinohologram`, market yönetim komutları).

## Dupe Önlemleri
- Tüm LCoin işlemleri `synchronized`/kilit altında yapılır ve **her işlemden hemen sonra** diske yazılır.
- Bakiye asla eksiye düşemez; yetersiz bakiyede işlem tamamen reddedilir.
- Market satın almada önce para çekilir, envanter kontrolü başarısızsa işlem baştan reddedilir (yarım işlem yok).
- Gazino GUI'sinde item çekme/bırakma tamamen engellenmiştir (`InventoryClickEvent` ve `InventoryDragEvent` iptal edilir).
- Bahis parası menüye girer girmez bakiyeden düşer; menü kapatılırsa (yarış başlamadıysa) otomatik iade edilir, para asla ortada kaybolmaz.
- `/lcoinparacek` komutu, hedef oyuncunun bakiyesinden fazlasını asla çekemez.

## Notlar
- Ekonomi verisi: `plugins/CraftLightCasino/lcoin.yml`
- Gazino alanları: `plugins/CraftLightCasino/casinos.yml`
- Market eşyaları: `plugins/CraftLightCasino/market.yml`
- Ayarlar (bahis artış miktarı, min/max bahis, kazanma çarpanı, yarış süresi, max alan/eşya sayısı vb.): `plugins/CraftLightCasino/config.yml`
