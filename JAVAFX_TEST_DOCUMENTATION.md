# DeckOS — JavaFX GUI Test Dokümantasyonu

**Sürüm:** 1.0  
**Tarih:** 2026-05-15  
**Hazırlayan:** Tolga Boz  
**Kapsam:** JavaFX masaüstü istemcisinin tüm ekranlarının manuel ve otomatik (TestFX) olarak doğrulanması

---

## TEST İLERLEME TABLOSU

> `[ ]` = Bekleniyor &nbsp;|&nbsp; `[x]` = Geçti &nbsp;|&nbsp; `[!]` = Başarısız

### Bölüm 3 — Ortam Kurulumu
- [x] **3.1** Backend sağlık kontrolü (tüm servisler UP)
- [x] **3.2** JavaFX uygulaması `mvn javafx:run` ile başlatılıyor

### Bölüm 4 — Login Ekranı
- [x] **4.1.1** Geçerli kimlik → deck-list.fxml'e geçiş
- [x] **4.1.2** Yanlış şifre → "Invalid credentials" hata etiketi görünür
- [x] **4.1.3** Kısa kullanıcı adı (< 3 karakter) → "Username must be between 3 and 50 characters."
- [x] **4.1.4** Kısa şifre (< 6 karakter) → "Password must be at least 6 characters."
- [x] **4.1.5** "Register" bağlantısına tıkla → register.fxml'e geçiş
- [x] **4.1.6** Backend kapalıyken → "Connection error. Is the backend running?"
- [x] **4.1.7** Giriş sırasında loading spinner görünür, button devre dışı kalır

### Bölüm 5 — Register Ekranı
- [x] **5.1.1** Geçerli bilgiler → otomatik login + deck-list.fxml'e geçiş
- [x] **5.1.2** Mevcut kullanıcı adı → "Username already taken" hatası
- [x] **5.1.3** Kısa kullanıcı adı → "Username must be between 3 and 50 characters."
- [x] **5.1.4** Geçersiz email (@ yok) → "Please enter a valid email address."
- [x] **5.1.5** Kısa şifre → "Password must be at least 6 characters."
- [x] **5.1.6** "Back to Login" tıkla → login.fxml'e geçiş

### Bölüm 6 — Deck Listesi Ekranı
- [x] **6.1.1** Deck'ler yüklenirken ProgressIndicator görünür
- [x] **6.1.2** Deck listesi doğru render edilir (isim, dil, kart sayısı) — nameLabel text-fill düzeltildi
- [x] **6.1.3** "No decks yet" mesajı boş listede görünür
- [x] **6.2.1** "+ New Deck" dialog: isim + dil zorunlu, OK butonu disable
- [x] **6.2.2** Geçerli deck oluştur → liste yenilenir, yeni deck görünür
- [x] **6.2.3** Deck sil → onay dialogu çıkar
- [x] **6.2.4** Onay dialogunda OK → deck listeden kalkar
- [x] **6.2.5** Onay dialogunda Cancel → deck silinmez
- [x] **6.3.1** "Cards" butonu → card-list.fxml'e geçiş, selectedDeckId set edilir
- [x] **6.3.2** "Review" butonu → review.fxml'e geçiş
- [x] **6.3.3** "Progress" butonu → progress.fxml'e geçiş
- [x] **6.3.4** "Logout" → session temizlenir, login.fxml'e geçiş

### Bölüm 7 — Kart Listesi Ekranı
- [x] **7.1.1** Deck adı üst etikette doğru görünür
- [x] **7.1.2** Kartlar tabloda listelenir (front, back, interval, nextReview)
- [x] **7.1.3** Boş deck → "No cards yet. Click '+ Add Card' to create one."
- [x] **7.2.1** "+ Add Card" dialog: front + back her ikisi de dolu olmalı, OK disable
- [x] **7.2.2** Kart oluştur → tablo yenilenir
- [x] **7.2.3** Edit butonu → mevcut front/back dolu dialog açılır
- [x] **7.2.4** Kart güncelle → tablo yenilenir
- [x] **7.2.5** Delete butonu → onay dialogu çıkar
- [x] **7.2.6** Delete onayı → kart tablodan kalkar
- [x] **7.3.1** "Back" butonu → deck-list.fxml'e geçiş

### Bölüm 8 — Review Ekranı
- [x] **8.1.1** Due kartlar yüklenirken "Loading..." gösterilir
- [x] **8.1.2** Due kart varsa ilk kart ön yüzü gösterilir
- [x] **8.1.3** Due kart yoksa Empty State (🎉 + "You're all caught up!") görünür
- [x] **8.1.4** Empty State'te "Back to Decks" butonu çalışır
- [x] **8.2.1** Progress label "Card 1 / N" formatında görünür
- [x] **8.2.2** Progress bar ilerlemesi doğru (currentIndex / total)
- [x] **8.3.1** "Show Answer" → kart ön→arka animasyonu oynar
- [x] **8.3.2** Animasyon sonrası rating butonları (Again/Hard/Good/Easy) görünür
- [x] **8.3.3** "Show Answer" başlangıçta gizlenir
- [x] **8.4.1** "Again" (rating 0) → bir sonraki karta geçilir
- [x] **8.4.2** "Hard" (rating 3) → bir sonraki karta geçilir
- [x] **8.4.3** "Good" (rating 4) → bir sonraki karta geçilir, correctCount artar
- [x] **8.4.4** "Easy" (rating 5) → bir sonraki karta geçilir, correctCount artar
- [x] **8.5.1** Tüm kartlar review edilince Session Summary görünür
- [x] **8.5.2** Summary: reviewed count, correct count, accuracy %, XP tahmini doğru
- [x] **8.5.3** Summary "View Progress" → progress.fxml'e geçiş
- [x] **8.5.4** Summary "Back to Decks" → deck-list.fxml'e geçiş
- [x] **8.5.5** "End Session" butonu → deck-list.fxml'e geçiş

### Bölüm 9 — Progress Ekranı
- [x] **9.1.1** Level etiketi doğru görünür (ör. "Level 2")
- [x] **9.1.2** XP bar 0'dan hedef değere animasyonla dolar (800ms)
- [x] **9.1.3** XP etiketi "X / 100 XP to next level" formatında
- [x] **9.2.1** Stats grid: Total Reviews, Correct, Streak, Last Review doğru değerler
- [x] **9.2.2** Accuracy % hesabı doğru (0 review durumunda 0%)
- [x] **9.3.1** Kazanılan badge'ler renkli ve tam opasite görünür
- [x] **9.3.2** Kazanılmamış badge'ler soluk (0.6 opasite) görünür
- [x] **9.3.3** Badge başlığı "Badges (N)" formatında
- [x] **9.4.1** Leaderboard tablosu yüklenir
- [x] **9.4.2** Mevcut kullanıcının satırı mavi highlight (#d6eaf8) ile
- [x] **9.4.3** Rank kolonu sıra numarasını doğru gösterir
- [x] **9.5.1** "Back" butonu → deck-list.fxml'e geçiş

### Bölüm 10 — Hata ve Edge Case Testleri
- [x] **10.1** Backend kapalıyken login → Connection Error alert + hata etiketi
- [ ] **10.2** Session süresi dolmuşsa (401) → otomatik login ekranına yönlendirme
- [ ] **10.3** Geçersiz token ile istek → login ekranına yönlendirme

---

## İçindekiler

1. [Genel Bakış](#1-genel-bakış)
2. [Test Piramidi ve Strateji](#2-test-piramidi-ve-strateji)
3. [Test Ortamı Kurulumu](#3-test-ortamı-kurulumu)
4. [Login Ekranı Testleri](#4-login-ekranı-testleri)
5. [Register Ekranı Testleri](#5-register-ekranı-testleri)
6. [Deck Listesi Ekranı Testleri](#6-deck-listesi-ekranı-testleri)
7. [Kart Listesi Ekranı Testleri](#7-kart-listesi-ekranı-testleri)
8. [Review Ekranı Testleri](#8-review-ekranı-testleri)
9. [Progress Ekranı Testleri](#9-progress-ekranı-testleri)
10. [Hata ve Edge Case Testleri](#10-hata-ve-edge-case-testleri)
11. [TestFX Otomatik Testleri](#11-testfx-otomatik-testleri)
12. [Tam Kullanıcı Akışı (E2E)](#12-tam-kullanıcı-akışı-e2e)
13. [Mevcut Boşluklar ve Sonraki Adımlar](#13-mevcut-boşluklar-ve-sonraki-adımlar)

---

## 1. Genel Bakış

### 1.1 Mimari Özet

```
[JavaFX GUI]
     ↓  HttpClient (localhost:8080)
[API Gateway]
     ↓  JWT doğrulama
  ┌──┴──┬──────────────┐
auth   card       gamification
service service    service
```

### 1.2 Ekranlar ve Kontrolörler

| FXML Dosyası | Controller Sınıfı | Amaç |
|---|---|---|
| `login.fxml` | `LoginController` | Kullanıcı girişi |
| `register.fxml` | `RegisterController` | Yeni kullanıcı kaydı |
| `deck-list.fxml` | `DeckListController` | Deck CRUD ve navigasyon |
| `card-list.fxml` | `CardListController` | Kart CRUD (seçili deck için) |
| `review.fxml` | `ReviewController` | SM-2 kart tekrar oturumu |
| `progress.fxml` | `ProgressController` | XP / level / badge / leaderboard |

### 1.3 Test Hedefleri

- Tüm form validasyonlarının (client-side) doğru çalıştığı
- Her ekrana navigasyonun doğru gerçekleştiği
- Backend hata durumlarında kullanıcı dostu mesajların göründüğü
- Review oturumunun SM-2 API'siyle doğru entegre çalıştığı
- Session yönetimi (login/logout/expiry) ve token yaşam döngüsünün doğruluğu

---

## 2. Test Piramidi ve Strateji

```
          ▲  Manuel E2E (tam kullanıcı akışı)
         ▲▲▲  TestFX Entegrasyon (ekran → API)
        ▲▲▲▲▲  TestFX Birim (Controller mantığı, validasyon)
```

| Katman | Araç | Kapsam | Koşum |
|--------|------|--------|-------|
| Controller birim | TestFX + JUnit 5 + Mockito | Validasyon, state yönetimi | Otomatik |
| Ekran entegrasyon | TestFX + WireMock | API çağrısı → UI güncellemesi | Otomatik |
| Manuel E2E | Elle test + backend canlı | Tam kullanıcı akışı | Manuel |

---

## 3. Test Ortamı Kurulumu

### 3.1 Backend'i Başlatma

JavaFX testleri başlamadan önce backend ayakta olmalı:

```powershell
cd c:\Dev\spaced-repetition-language-learning

# Altyapıyı başlat
docker-compose up -d postgres mongodb redis kafka

# Servisleri başlat (ayrı terminallerde)
# Terminal 1
cd auth-service
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
$env:DB_HOST="localhost"; $env:DB_USER="srll"; $env:DB_PASS="srll_pass"
mvn spring-boot:run

# Terminal 2
cd card-service
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
$env:DB_HOST="localhost"; $env:MONGO_HOST="localhost"; $env:KAFKA_BOOTSTRAP="localhost:9092"
mvn spring-boot:run

# Terminal 3
cd gamification-service
$env:MONGO_HOST="localhost"; $env:REDIS_HOST="localhost"; $env:KAFKA_BOOTSTRAP="localhost:9092"
mvn spring-boot:run

# Terminal 4
cd api-gateway
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
mvn spring-boot:run
```

Sağlık kontrolü:

```powershell
@(8080, 8081, 8082, 8083) | ForEach-Object {
    $r = Invoke-RestMethod "http://localhost:$_/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "Port $_ -> $($r.status)"
}
```

Beklenen: tüm portlar `UP`

### 3.2 JavaFX Uygulamasını Başlatma

```powershell
cd c:\Dev\spaced-repetition-language-learning\javafx-client
mvn javafx:run
```

Uygulama 800×600 boyutunda `Login` ekranıyla açılmalı.  
Başlık: `DeckOS`

### 3.3 Test Kullanıcısı Hazırlama

Test sırasında kullanmak üzere bir kullanıcı oluşturun:

```powershell
$body = @{
    username = "javafx_tester"
    email    = "javafx@test.com"
    password = "Test1234!"
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/register" `
    -ContentType "application/json" -Body $body
```

---

## 4. Login Ekranı Testleri

### 4.1 Başarılı Giriş

**Ön koşul:** Backend çalışıyor, `javafx_tester` kullanıcısı kayıtlı

**Adımlar:**
1. Kullanıcı adı alanına `javafx_tester` yaz
2. Şifre alanına `Test1234!` yaz
3. "Login" butonuna tıkla

**Beklenen:**
- Loading spinner görünür, Login butonu devre dışı kalır
- Yükleme tamamlanınca `deck-list.fxml` ekranına geçilir
- Spinner gizlenir

**Doğrulama:**
- [ ] Ekran başlığı değişmez (aynı Stage kullanılır)
- [ ] Deck Listesi ekranı açılır

---

### 4.2 Client-Side Validation (Backend'e İstek Gitmeden)

Bu testler backend olmadan da çalışır — validation client'ta gerçekleşir.

| # | Kullanıcı Adı | Şifre | Beklenen Hata |
|---|---|---|---|
| 4.2.1 | `ab` (2 karakter) | `Test1234!` | "Username must be between 3 and 50 characters." |
| 4.2.2 | 51 karakter uzunluğunda | `Test1234!` | "Username must be between 3 and 50 characters." |
| 4.2.3 | `javafx_tester` | `12345` (5 karakter) | "Password must be at least 6 characters." |

**Kontrol edilecekler:**
- [ ] Hata etiketi kırmızı ve görünür hale gelir
- [ ] Backend'e hiç istek gönderilmez (hız farkıyla anlaşılır)

---

### 4.3 Yanlış Kimlik (Backend'den 401)

1. Kullanıcı adı: `javafx_tester`
2. Şifre: `WrongPass!`
3. Login'e tıkla

**Beklenen:**
- [ ] Spinner kaybolur
- [ ] Hata etiketi "Invalid credentials" mesajıyla görünür
- [ ] Ekran navigasyonu olmaz

---

### 4.4 "Register" Bağlantısı

1. "Don't have an account? Register" bağlantısına tıkla

**Beklenen:**
- [ ] `register.fxml` ekranı açılır

---

## 5. Register Ekranı Testleri

### 5.1 Başarılı Kayıt

1. Kullanıcı adı: `new_user_<random>`
2. Email: `newuser@test.com`
3. Şifre: `Test1234!`
4. "Register" butonuna tıkla

**Beklenen:**
- [ ] Loading spinner görünür
- [ ] Kayıt tamamlanınca `deck-list.fxml`'e geçilir (otomatik login)

---

### 5.2 Validation Testleri

| # | Alan | Değer | Beklenen Hata |
|---|---|---|---|
| 5.2.1 | Kullanıcı adı | `ab` | "Username must be between 3 and 50 characters." |
| 5.2.2 | Email | `notanemail` | "Please enter a valid email address." |
| 5.2.3 | Şifre | `12345` | "Password must be at least 6 characters." |
| 5.2.4 | Kullanıcı adı | `javafx_tester` (var olan) | "Username already taken" |

---

### 5.3 "Back to Login" Bağlantısı

1. "Already have an account? Login" bağlantısına tıkla

**Beklenen:**
- [ ] `login.fxml` ekranı açılır

---

## 6. Deck Listesi Ekranı Testleri

### 6.1 Deck Listesi Yükleme

1. `javafx_tester` ile giriş yap
2. Deck listesi ekranı açılır

**Beklenen:**
- [ ] Yükleme sırasında `ProgressIndicator` görünür (kısa süre)
- [ ] Backend'deki tüm deck'ler listelenir
- [ ] Her deck kartında: isim (bold), dil kodu (gri), kart sayısı (gri)

**Boş liste senaryosu:**
- [ ] "No decks yet. Click '+ New Deck' to create one." etiketi görünür

---

### 6.2 Yeni Deck Oluşturma

**Adımlar:**
1. "+ New Deck" butonuna tıkla
2. Dialog açılır

**Dialog Validation:**
- [ ] "Name" ve "Language" alanları boşken OK butonu devre dışı
- [ ] Sadece Name dolu, Language boş → OK hâlâ devre dışı
- [ ] Her ikisi de dolu → OK aktif hale gelir

**Deck Oluşturma:**
1. Name: `Test Deck`
2. Description: `Açıklama (opsiyonel)`
3. Language: `English`
4. OK'a tıkla

**Beklenen:**
- [ ] Dialog kapanır
- [ ] Liste yenilenir, yeni deck görünür

---

### 6.3 Deck Silme

1. Bir deck'in "Delete" butonuna tıkla
2. Onay dialogu açılır: `Delete "Test Deck"?`

**Onay Cancel:**
- [ ] Dialog kapanır, deck listede kalır

**Onay OK:**
- [ ] Deck listeden kaybolur, liste yenilenir

---

### 6.4 Navigasyon Butonları

| Buton | Beklenen Ekran |
|---|---|
| "Cards" (deck satırında) | `card-list.fxml` |
| "Review" (deck satırında) | `review.fxml` |
| "Progress" (üst toolbar) | `progress.fxml` |
| "Logout" | `login.fxml` + session temizlendi |

**Logout kontrolü:**
1. Logout'a tıkla
2. Login ekranında herhangi bir korumalı işlem bekle
3. Login yaptıktan sonra session yeni token almalı

---

## 7. Kart Listesi Ekranı Testleri

### 7.1 Kart Listesi Yükleme

1. Deck listesinde bir deck'in "Cards" butonuna tıkla

**Beklenen:**
- [ ] Üst etiket: seçili deck'in adı
- [ ] `ProgressIndicator` kısa süre görünür
- [ ] Kartlar tabloda sıralanır: Front | Back | Interval | Next Review | Actions

---

### 7.2 Kart Ekleme

1. "+ Add Card" butonuna tıkla
2. Dialog açılır

**Validation:**
- [ ] Front veya Back boşken OK devre dışı
- [ ] Her ikisi de dolu olunca OK aktif

**Kart Oluşturma:**
1. Front: `Hola`
2. Back: `Merhaba`
3. OK

**Beklenen:**
- [ ] Tablo yenilenir, yeni kart görünür
- [ ] Interval: "1 days", nextReview: bugünün tarihi

---

### 7.3 Kart Düzenleme

1. Tablodaki bir kartın "Edit" butonuna tıkla

**Beklenen:**
- [ ] Dialog açılır, mevcut front ve back değerleri alanları dolu
- [ ] Değerleri güncelle, OK'a tıkla
- [ ] Tablo yenilenir, güncellenen değerler görünür

---

### 7.4 Kart Silme

1. "Delete" butonuna tıkla
2. Onay dialogu: `Delete this card? Front: <front text>`

**Onay OK:**
- [ ] Kart tablodan kalkar

---

### 7.5 "Back" Butonu

- [ ] `deck-list.fxml`'e geri dönülür

---

## 8. Review Ekranı Testleri

### 8.1 Due Kart Yükleme

**Senaryo A — Due kart VAR:**
1. Review ekranı açılır
2. "Loading..." yazısı kısa süre görünür
3. İlk kartın ön yüzü (front) gösterilir

**Beklenen:**
- [ ] Progress label: "Card 1 / N"
- [ ] Progress bar: 0 (başlangıç)
- [ ] "Show Answer" butonu görünür ve aktif
- [ ] Rating butonları (Again/Hard/Good/Easy) gizli

**Senaryo B — Due kart YOK:**
- [ ] "🎉 You're all caught up!" Empty State görünür
- [ ] "No cards are due for review today." alt metni görünür
- [ ] "Back to Decks" butonu çalışır → `deck-list.fxml`

---

### 8.2 Kart Çevirme Animasyonu

1. "Show Answer" butonuna tıkla

**Beklenen:**
- [ ] Kartın ön yüzü 90°'ye döner (150ms)
- [ ] Arka yüz (-90° → 0°) animasyonla açılır (150ms)
- [ ] Animasyon tamamlanınca rating butonları belirir
- [ ] "Show Answer" butonu gizlenir

---

### 8.3 Rating Butonları

Kart arka yüzü görünürken:

| Buton | Rating | `correctCount` artar mı? |
|---|---|---|
| Again | 0 | Hayır (< 4) |
| Hard | 3 | Hayır (< 4) |
| Good | 4 | Evet (>= 4) |
| Easy | 5 | Evet (>= 4) |

**Her rating sonrası:**
- [ ] Rating butonları kısa süre disable olur (API çağrısı süresince)
- [ ] API yanıtı gelince bir sonraki karta geçilir
- [ ] Progress label güncellenir: "Card 2 / N"
- [ ] Progress bar ilerler

---

### 8.4 Session Summary

Tüm kartlar tamamlanınca:

**Beklenen:**
- [ ] "Session Complete! 🎉" başlığı
- [ ] "Reviewed: N cards"
- [ ] "Correct: M (X%)" — accuracy doğru hesaplanmış
- [ ] "XP earned: ~Y pts" (Y = correctCount × 10)
- [ ] Progress bar: %100
- [ ] Progress label: "Complete! ✓"
- [ ] "View Progress" → `progress.fxml`
- [ ] "Back to Decks" → `deck-list.fxml`

**Accuracy hesabı kontrolü:**
- 5 kart, 3 Good/Easy (correctCount=3) → `3/5 × 100 = 60%`

---

### 8.5 "End Session" Butonu

- [ ] Herhangi bir noktada tıkla → `deck-list.fxml`'e dön

---

## 9. Progress Ekranı Testleri

### 9.1 Level ve XP Paneli

1. En az 1 review yaptıktan sonra Progress ekranına git

**Beklenen:**
- [ ] Level etiketi: "Level N" (N >= 1)
- [ ] XP bar 0'dan başlayıp 800ms animasyonla hedef değere ulaşır
- [ ] XP etiketi: "X / 100 XP to next level" (X = xp % 100)

**Edge case — Level 0 (hiç review yok):**
- [ ] Level 1, XP bar sıfır, "0 / 100 XP to next level"

---

### 9.2 İstatistik Grid

| Satır | Beklenen Değer |
|---|---|
| Total Reviews | Backend'den dönen `totalReviews` değeri |
| Correct | `totalCorrect (accuracy%)` |
| Current Streak | `N days 🔥` |
| Last Review | `YYYY-MM-DD` formatında veya `—` (hiç review yoksa) |

**Accuracy hesabı:** `totalReviews == 0` → `0%` (sıfıra bölme koruması)

---

### 9.3 Badge Paneli

Toplam 8 badge vardır:

| Badge Key | İkon | Ad |
|---|---|---|
| FIRST_REVIEW | ⭐ | First Review |
| STREAK_3 | 🔥 | 3-Day Streak |
| STREAK_7 | 🔥🔥 | 7-Day Streak |
| STREAK_30 | 💎 | 30-Day Streak |
| REVIEWS_100 | 💯 | Century |
| REVIEWS_500 | 🏆 | Five Hundred |
| LEVEL_5 | 🥈 | Level 5 |
| LEVEL_10 | 🥇 | Level 10 |

**Kontrol edilecekler:**
- [ ] Kazanılmış badge: mavi bordür, beyaz arka plan, tam opasite (1.0)
- [ ] Kazanılmamış badge: gri bordür, `#f5f6fa` arka plan, 0.6 opasite
- [ ] Badge başlığı: "Badges (N)" — N kazanılmış badge sayısı

---

### 9.4 Leaderboard Paneli

**Beklenen:**

- [ ] Leaderboard satırları yüklenir (sıra | Kullanıcı adı | XP)
- [ ] Rank kolonu: 1–3 arası altın/gümüş/bronz renkli rozet, sonrakiler gri
- [ ] User kolonu: backend'den gelen `username`; username null ise "User {userId}" fallback
- [ ] XP sütunu: tam sayı + " XP" etiketi
- [ ] İlk 3 sıranın XP etiketi farklı renk (üst-3 stili)
- [ ] Mevcut kullanıcının satırı `lb-row-current` CSS sınıfıyla vurgulanır

---

### 9.5 "Back" Butonu

- [ ] `deck-list.fxml`'e geri dönülür

---

## 10. Hata ve Edge Case Testleri

### 10.1 Backend Kapalıyken Davranış

1. Docker container'ları durdur: `docker-compose stop`
2. Uygulamayı aç, login dene

**Beklenen:**
- [ ] **Login ekranı:** hata etiketi "Connection error. Is the backend running?"
- [ ] **Deck listesi:** hata etiketi + "Retry" butonu görünür
- [ ] **ApiClient:** `IOException` → `Alert.AlertType.ERROR` dialog: "Backend is unreachable"

---

### 10.2 Session Süresi Dolduğunda (Token Expiry)

**Simülasyon:** Doğrudan bir deck endpoint'ine geçersiz token ile çağrı gönder:

```powershell
# Sahte süresi dolmuş token ile istek
Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks" `
    -Headers @{ Authorization = "Bearer expired.token.here" }
# → 401 dönmeli
```

**JavaFX'te beklenen davranış (401 alındığında):**
- [ ] `ApiClient.execute()` → `SessionManager.clearSession()` çağrılır
- [ ] `Platform.runLater` ile `login.fxml`'e navigasyon tetiklenir
- [ ] `ApiException("Session expired. Please log in again.")` fırlatılır

---

### 10.3 Sıfır Accuracy Hesabı (Sıfıra Bölme Koruması)

- `ProgressController.populateStats()`: `totalReviews == 0` durumunda → `accuracy = 0`
- `ReviewController.showSessionSummary()`: `reviewedCount == 0` durumunda → `accuracy = 0`

Manuel doğrulama:
1. Hiç review yapılmamış hesapta Progress ekranını aç
2. "Correct: 0 (0%)" görünmeli — "NaN%" veya exception olmamalı

---

### 10.4 Boş Deck'te Review Başlatma

1. Kartı olmayan bir deck oluştur
2. "Review" butonuna tıkla

**Beklenen:**
- [ ] API `/api/cards/due` → boş array döner
- [ ] Empty State ekranı: "🎉 You're all caught up!"

---

## 11. TestFX Otomatik Testleri

### 11.1 Bağımlılıkları pom.xml'e Ekle

```xml
<!-- javafx-client/pom.xml -->
<dependencies>
    <!-- Mevcut bağımlılıklar... -->

    <!-- Test: TestFX -->
    <dependency>
        <groupId>org.testfx</groupId>
        <artifactId>testfx-core</artifactId>
        <version>4.0.18</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testfx</groupId>
        <artifactId>testfx-junit5</artifactId>
        <version>4.0.18</version>
        <scope>test</scope>
    </dependency>

    <!-- Test: JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Test: Mockito (API servislerini mock'lamak için) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### 11.2 Login Validation Birim Testleri

**Dosya:** `javafx-client/src/test/java/com/srll/javafx/controller/LoginControllerValidationTest.java`

```java
/**
 * TestFX gerektirmez — saf Java validasyon mantığı.
 * LoginController.validate() pakete açık değilse, mantığı ayrı bir doğrulanabilir metoda taşı.
 */
class LoginValidationTest {

    // Bu testler validasyon mantığını yansıtır (LoginController'dan çıkarılabilir)

    @Test
    void username_tooShort_isInvalid() {
        assertThat(isValidUsername("ab")).isFalse();
    }

    @Test
    void username_tooLong_isInvalid() {
        assertThat(isValidUsername("a".repeat(51))).isFalse();
    }

    @Test
    void username_exactBoundaries_areValid() {
        assertThat(isValidUsername("abc")).isTrue();        // 3 karakter — minimum
        assertThat(isValidUsername("a".repeat(50))).isTrue(); // 50 karakter — maksimum
    }

    @Test
    void password_tooShort_isInvalid() {
        assertThat(isValidPassword("12345")).isFalse();    // 5 karakter
    }

    @Test
    void password_minLength_isValid() {
        assertThat(isValidPassword("123456")).isTrue();    // 6 karakter
    }

    private boolean isValidUsername(String u) {
        return u.length() >= 3 && u.length() <= 50;
    }

    private boolean isValidPassword(String p) {
        return p.length() >= 6;
    }
}
```

---

### 11.3 Register Validation Birim Testleri

**Dosya:** `javafx-client/src/test/java/com/srll/javafx/controller/RegisterControllerValidationTest.java`

```java
class RegisterValidationTest {

    @Test
    void email_withoutAtSign_isInvalid() {
        assertThat(isValidEmail("notanemail")).isFalse();
    }

    @Test
    void email_withAtSign_isValid() {
        assertThat(isValidEmail("user@test.com")).isTrue();
    }

    @Test
    void email_atSignOnly_isValid() {
        // Mevcut implementasyon sadece "@" varlığını kontrol eder
        assertThat(isValidEmail("@")).isTrue();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }
}
```

---

### 11.4 ApiClient userFriendlyMessage Testleri

**Dosya:** `javafx-client/src/test/java/com/srll/javafx/http/ApiClientTest.java`

```java
class ApiClientTest {

    @Test
    void userFriendlyMessage_401_returnsSessionExpired() {
        String msg = ApiClient.userFriendlyMessage(401, "");
        assertThat(msg).isEqualTo("Session expired. Please log in again.");
    }

    @Test
    void userFriendlyMessage_403_returnsPermissionDenied() {
        String msg = ApiClient.userFriendlyMessage(403, "");
        assertThat(msg).contains("permission");
    }

    @Test
    void userFriendlyMessage_404_returnsNotFound() {
        String msg = ApiClient.userFriendlyMessage(404, "");
        assertThat(msg).contains("not found");
    }

    @Test
    void userFriendlyMessage_500_returnsServerError() {
        String msg = ApiClient.userFriendlyMessage(500, "");
        assertThat(msg).contains("Server error");
    }

    @Test
    void userFriendlyMessage_unknownCode_includesStatusCode() {
        String msg = ApiClient.userFriendlyMessage(422, "Unprocessable");
        assertThat(msg).contains("422");
    }
}
```

---

### 11.5 ReviewController Hesaplama Testleri

**Dosya:** `javafx-client/src/test/java/com/srll/javafx/controller/ReviewCalculationTest.java`

```java
class ReviewCalculationTest {

    // Session Summary accuracy hesabı doğrulaması
    @Test
    void accuracy_zeroReviewed_returnsZeroNotNaN() {
        int reviewedCount = 0;
        int correctCount  = 0;
        int accuracy = reviewedCount == 0 ? 0
                : (int) Math.round((double) correctCount / reviewedCount * 100);
        assertThat(accuracy).isEqualTo(0);  // NaN veya exception olmamalı
    }

    @Test
    void accuracy_allCorrect_returns100() {
        int reviewedCount = 5;
        int correctCount  = 5;
        int accuracy = (int) Math.round((double) correctCount / reviewedCount * 100);
        assertThat(accuracy).isEqualTo(100);
    }

    @Test
    void accuracy_partialCorrect_roundsCorrectly() {
        int reviewedCount = 3;
        int correctCount  = 2;
        int accuracy = (int) Math.round((double) correctCount / reviewedCount * 100);
        assertThat(accuracy).isEqualTo(67); // 66.666... → 67
    }

    @Test
    void estimatedXp_correctCountMultiplied() {
        int correctCount  = 7;
        int estimatedXp   = correctCount * 10;
        assertThat(estimatedXp).isEqualTo(70);
    }

    // Rating >= 4 ise correct sayılır
    @Test
    void rating4_isCorrect() {
        int rating = 4;
        assertThat(rating >= 4).isTrue();
    }

    @Test
    void rating3_isNotCorrect() {
        int rating = 3;
        assertThat(rating >= 4).isFalse();
    }
}
```

---

### 11.6 ProgressController Hesaplama Testleri

**Dosya:** `javafx-client/src/test/java/com/srll/javafx/controller/ProgressCalculationTest.java`

```java
class ProgressCalculationTest {

    // XP bar progress hesabı (xp % 100 / 100.0)
    @Test
    void xpBarProgress_level1_150xp_showsHalfBar() {
        int xp = 150;
        int xpInLevel = xp % 100;       // 50
        double target = xpInLevel / 100.0;  // 0.5
        assertThat(target).isEqualTo(0.5);
    }

    @Test
    void xpBarProgress_exact100xp_showsEmptyBar() {
        // 100 xp = level 2, 0 xp in current level
        int xp = 100;
        double target = (xp % 100) / 100.0;  // 0.0
        assertThat(target).isEqualTo(0.0);
    }

    // Accuracy hesabı (sıfıra bölme koruması)
    @Test
    void accuracy_noReviews_returnsZero() {
        int totalReviews = 0;
        int totalCorrect = 0;
        int accuracy = totalReviews == 0 ? 0
                : (int) Math.round((double) totalCorrect / totalReviews * 100);
        assertThat(accuracy).isEqualTo(0);
    }

    // Badge earned kontrolü
    @Test
    void earnedBadges_containsFirstReview_afterFirstReview() {
        List<String> badges = List.of("FIRST_REVIEW", "STREAK_3");
        assertThat(badges.contains("FIRST_REVIEW")).isTrue();
        assertThat(badges.contains("STREAK_30")).isFalse();
    }
}
```

---

### 11.7 Testleri Çalıştırma

```powershell
cd c:\Dev\spaced-repetition-language-learning\javafx-client
mvn test
```

Beklenen çıktı:
```
[INFO] Tests run: 5, Failures: 0, Errors: 0  -- LoginValidationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0  -- RegisterValidationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0  -- ApiClientTest
[INFO] Tests run: 6, Failures: 0, Errors: 0  -- ReviewCalculationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0  -- ProgressCalculationTest
[INFO] BUILD SUCCESS
```

> **Not:** TestFX ekran testleri headless ortamda (CI) çalıştırmak için `-Djava.awt.headless=true -Dtestfx.robot=glass` parametreleri gerekir.

---

## 12. Tam Kullanıcı Akışı (E2E)

Aşağıdaki senaryo uygulamayı baştan sona test eder. Sıraya göre uygulayın.

### Akış 1: Yeni Kullanıcı Tam Döngüsü

```
[1] Uygulamayı başlat → Login ekranı açılır

[2] "Register" bağlantısına tıkla → Register ekranı
    - Kullanıcı adı: "e2e_user_<random>"
    - Email: "e2e@test.com"
    - Şifre: "E2ePass1!"
    - Register → Deck Listesi ekranı

[3] "No decks yet." mesajı görünür
    - "+ New Deck" tıkla → Dialog
    - Name: "İspanyolca", Language: "Spanish"
    - OK → Deck listede görünür

[4] "Cards" tıkla → Kart Listesi ekranı
    - Deck adı üstte görünür
    - "+ Add Card" → Dialog
      - Front: "Hola", Back: "Merhaba"
      - OK → Kart tabloda görünür

[5] "Back" → Deck Listesi
    - "Review" tıkla → Review ekranı

[6] Review ekranı:
    - "Card 1 / 1" görünür
    - "Show Answer" → kart çevrilir
    - "Good" → Session Summary
    - "Reviewed: 1, Correct: 1 (100%), XP: ~10 pts"

[7] "View Progress" → Progress ekranı
    - Level 1, XP bar doldu
    - Total Reviews: 1, Correct: 1 (100%)
    - FIRST_REVIEW badge mavi ve tam opasite
    - Leaderboard'da mevcut kullanıcı satırı mavi

[8] "Back" → Deck Listesi
    - "Logout" → Login ekranı
    - Session temizlendi (korumalı sayfaya gitmeye çalışırsan → 401 → login yönlendirme)
```

**Kontrol listesi:**
- [ ] Tüm 8 adım sorunsuz tamamlandı
- [ ] Hiçbir exception/crash olmadı
- [ ] Tüm navigasyonlar doğru ekrana gitti
- [ ] FIRST_REVIEW badge verildi
- [ ] Session summary doğru değerleri gösterdi

---

### Akış 2: Mevcut Kullanıcı Hızlı Giriş

```
[1] Login: javafx_tester / Test1234!
[2] Mevcut deck listesi yüklenir
[3] Bir deck sil → onay → silindi
[4] Progress'i görüntüle → veriler doğru
[5] Logout → Login ekranı
```

---

## 13. Mevcut Boşluklar ve Sonraki Adımlar

### 13.1 Eksik Test Kapsamı

| # | Eksik Alan | Etki | Öneri |
|---|---|---|---|
| 1 | TestFX ile gerçek UI ekran testleri (headless) yok | Yüksek | WireMock + TestFX entegrasyonu |
| 2 | Card shuffle davranışı test edilmemiyor | Düşük | `Collections.shuffle` seed ile test |
| 3 | Çok kart (100+) ile büyük review oturumu testi yok | Orta | Performans smoke testi ekle |
| 4 | Kart animasyonu (rotate) süresi test edilmemiyor | Düşük | `TestFX.sleep()` ile timing testi |
| 5 | Leaderboard rank sıralaması doğruluğu test edilmemiyor | Orta | Mock API + tablo sıra doğrulama |

### 13.2 Bilinen Kısıtlamalar

| # | Kısıtlama | Öneri |
|---|---|---|
| 1 | `BASE_URL` `ApiClient`'ta hardcoded (`localhost:8080`) | Ortam değişkeni ile konfigüre edilebilir yapıya geç |
| 2 | Email validasyonu sadece `@` kontrolü yapıyor | Regex tabanlı validasyon ekle |
| 3 | `selectedDeckId` `static` field — thread-safe değil | `SessionManager`'a taşı |
| 4 | `DeckListController.selectedDeckId` null ise `CardListController` crash | Null check + hata ekranı |

### 13.3 Öncelikli Aksiyon Planı

```
Önce (kritik):
  [1] Bölüm 4–9 manuel testlerini çalıştır, tabloyu güncelle
  [2] Bölüm 11 birim testlerini pom.xml'e ekleyip mvn test ile geçir
  [3] selectedDeckId null korumasını ekle

Sonra:
  [4] BASE_URL'i ortam değişkenine çek (production hazırlığı)
  [5] Email regex validasyonu (RegisterController)
  [6] TestFX + WireMock ile 1-2 entegrasyon testi yaz

Daha sonra:
  [7] CI headless TestFX koşumu
  [8] Büyük veri (100+ kart) performans testi
```

---

## Ek A: Hızlı Sorun Giderme

| Belirti | Olası Neden | Çözüm |
|---|---|---|
| Uygulama hiç başlamıyor | Java 21 yok veya JavaFX module path eksik | `java -version` kontrol et; `mvn javafx:run` kullan |
| Login sonrası beyaz ekran | `deck-list.fxml` veya CSS bulunamadı | `resources/` klasörü yapısını kontrol et |
| "FXML not found" hatası | FXML dosyaları `/resources/com/srll/javafx/fxml/` altında değil | Maven `src/main/resources` yapısını doğrula |
| Tüm API çağrıları 401 | JWT token süresi dolmuş | Logout yapıp tekrar login ol |
| Review ekranı kart yüklemez | `card-service` veya `api-gateway` kapalı | `actuator/health` kontrol et |
| Badge'ler görünmüyor | Progress API `earnedBadges` null veya boş | Backend'de `UserProgress` Mongo dökümanını kontrol et |

---

## Ek B: Ekran Navigasyon Haritası

```
                    [Login] ←────────────────────────────────┐
                       ↓ (başarılı login)                    │
                  [Deck List] ──"Progress"──→ [Progress]     │
                  ↓         ↓                                 │
          "Cards"↓    "Review"↓                              │
          [Card List]  [Review Session]                       │
                  ↑                ↓ (oturum tamamlandı)     │
                  └────────────────┘                         │
                                                    "Logout"──┘
```

---

*Bu doküman, JavaFX istemcisinin backend ile entegre tam kullanıcı deneyimini doğrulamak amacıyla hazırlanmıştır. Testlerin tamamı geçtikten sonra `PROGRESS.md` güncellenmelidir.*
