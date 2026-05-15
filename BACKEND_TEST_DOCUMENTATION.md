# DeckOS — Backend Test Dokümantasyonu

**Sürüm:** 1.0  
**Tarih:** 2026-05-13  
**Hazırlayan:** Tolga Boz  
**Kapsam:** Frontend geliştirmesine geçmeden önce backend mimarisinin tam doğrulanması

---

## TEST İLERLEME TABLOSU

> Bu tablo her test adımı tamamlandıkça güncellenir.  
> `[ ]` = Bekleniyor &nbsp;|&nbsp; `[x]` = Geçti &nbsp;|&nbsp; `[!]` = Başarısız

### Bölüm 3 — Ortam Kurulumu
- [x] **3.1** PostgreSQL `healthy`
- [x] **3.1** MongoDB `healthy`
- [x] **3.1** Redis `healthy`
- [x] **3.1** Kafka `healthy`
- [x] **3.3** auth-service `/actuator/health` → UP
- [x] **3.3** card-service `/actuator/health` → UP
- [x] **3.3** gamification-service `/actuator/health` → UP
- [x] **3.3** api-gateway `/actuator/health` → UP

### Bölüm 4 — Birim Testleri
- [x] **4.1** AuthServiceTest (4/4)
- [x] **4.1** SM2AlgorithmTest (9/9)
- [x] **4.1** GamificationServiceTest (4/4)

### Bölüm 6 — API Uçtan Uca Testleri
#### 6.1 Auth
- [x] **6.1.1** Register — yeni kullanıcı → 201 + token
- [x] **6.1.1** Register — duplicate username → 400
- [x] **6.1.2** Login — geçerli kimlik → 200 + token
- [x] **6.1.2** Login — yanlış şifre → 401
- [x] **6.1.3** Korunan endpoint — token yok → 401
#### 6.2 Deck
- [x] **6.2.1** Deck oluştur → 201
- [x] **6.2.2** Deck listesi getir → 200
- [x] **6.2.3** Tek deck getir → 200
- [x] **6.2.4** Deck güncelle → 200
- [x] **6.2.3** Var olmayan deck → 404
#### 6.3 Kart
- [x] **6.3.1** Kart oluştur → 201
- [x] **6.3.2** Deck kartlarını listele → 200
- [x] **6.3.3** Due cards listele → 200
- [x] **6.3.4** Kart güncelle → 200
#### 6.4 Review (SM-2 + Kafka)
- [x] **6.4** Review rating=4 → repetitions=1, intervalDays advances
- [x] **6.4** Review rating=0 → repetitions=0, easeFactor decreases
- [x] **6.4** Review rating=6 → 400 validation hatası
#### 6.5 Gamification
- [x] **6.5.1** Progress → xp=32, badges: FIRST_REVIEW, STREAK_3
- [x] **6.5.2** Leaderboard (public, token yok) → 200
#### 6.6 Çapraz Kullanıcı Güvenliği
- [x] **6.6** user2 → user1 deck'ine erişim engellendi (404)
- [x] **6.6** user2 → user1 kartını silme engellendi (404)
- [x] **6.6** user2 → user1 kartını review edemez (404)
#### 6.7 Validation
- [x] **6.7** Geçersiz email/kısa şifre → 400 + field errors
- [x] **6.7** Rating > 5 → 400

### Bölüm 7 — Güvenlik Testleri
- [x] **7.1** Geçersiz token formatı → 401
- [x] **7.1** Authorization header yok → 401
- [x] **7.1** Bearer prefix eksik → 401
- [x] **7.1** Manipüle edilmiş payload (imza hatalı) → 401
- [x] **7.2** Downstream servis doğrudan erişim (bypass testi) → 403

### Bölüm 8 — Kafka
- [x] **8.3** Topic listesi → `review-completed` mevcut
- [x] **8.2** Consumer lag → 0 (4/4 event tüketildi)

---

## İçindekiler

1. [Genel Bakış](#1-genel-bakış)
2. [Test Piramidi ve Strateji](#2-test-piramidi-ve-strateji)
3. [Test Ortamı Kurulumu](#3-test-ortamı-kurulumu)
4. [Birim Testleri](#4-birim-testleri)
5. [Entegrasyon Testleri](#5-entegrasyon-testleri)
6. [API Uçtan Uca Testleri](#6-api-uçtan-uca-testleri)
7. [Güvenlik Testleri](#7-güvenlik-testleri)
8. [Kafka Event-Driven Testleri](#8-kafka-event-driven-testleri)
9. [Performans Testleri](#9-performans-testleri)
10. [Test Koşum Kılavuzu](#10-test-koşum-kılavuzu)
11. [Kapsam Raporlama](#11-kapsam-raporlama)
12. [Mevcut Boşluklar ve Sonraki Adımlar](#12-mevcut-boşluklar-ve-sonraki-adımlar)

---

## 1. Genel Bakış

### 1.1 Mimari Özet

| Servis | Port | Veritabanı | Bağımlılıklar |
|--------|------|------------|---------------|
| api-gateway | 8080 | — | auth, card, gamification servisleri |
| auth-service | 8081 | PostgreSQL (srll_auth) | — |
| card-service | 8082 | PostgreSQL (srll_cards) + MongoDB | Kafka |
| gamification-service | 8083 | MongoDB (srll_gamification) + Redis | Kafka |

### 1.2 Kritik Akışlar (Test Önceliği)

```
[Kullanıcı] → [API Gateway :8080]
                    ↓ JWT doğrulama
       ┌────────────┼────────────────┐
  auth-service  card-service   gamification-service
   (register/    (deck/card/      (XP/level/
    login)        review)          badges)
       ↓              ↓  Kafka event      ↓
   PostgreSQL    PostgreSQL +     MongoDB +
                 MongoDB          Redis
```

### 1.3 Test Hedefleri

- Tüm REST endpoint'lerinin beklenen HTTP kodlarını döndürdüğünün doğrulanması
- JWT auth flow'unun güvenli çalışmasının kanıtlanması
- SM-2 algoritmasının tüm edge case'lerde doğru sonuç üretmesinin kanıtlanması
- Kafka review event'inin doğru gamification güncellemesini tetiklediğinin doğrulanması
- Gateway'in kullanıcı bilgilerini servislere doğru ilettiğinin doğrulanması
- Hata durumlarında tutarlı `ApiResponse` formatının dönüldüğünün doğrulanması

---

## 2. Test Piramidi ve Strateji

```
        ▲  E2E / Postman
       ▲▲▲  Integration Tests (TestContainers)
      ▲▲▲▲▲  Unit Tests (JUnit 5 + Mockito)
```

| Katman | Araç | Kapsam Hedefi | Koşum Süresi |
|--------|------|---------------|--------------|
| Birim | JUnit 5, Mockito | %80+ | < 30s |
| Entegrasyon | TestContainers + Spring Boot Test | Kritik akışlar | < 5 dak |
| E2E | Postman / curl | Tüm endpoint'ler | < 10 dak |
| Performans | JMeter | P95 < 500ms | Manuel |

---

## 3. Test Ortamı Kurulumu

### 3.1 Docker ile Altyapı Ayağa Kaldırma

```powershell
# Proje kök dizininde çalıştırın
cd c:\Dev\spaced-repetition-language-learning

# Tüm altyapıyı başlat (uygulamalar hariç)
docker-compose up -d postgres mongodb redis kafka

# Servislerin hazır olmasını bekleyin
docker-compose ps
```

Beklenen çıktı — tüm servisler `healthy` durumunda olmalı:

```
NAME        STATUS    PORTS
postgres    healthy   0.0.0.0:5432->5432/tcp
mongodb     healthy   0.0.0.0:27017->27017/tcp
redis       healthy   0.0.0.0:6379->6379/tcp
kafka       healthy   0.0.0.0:9092->9092/tcp
```

### 3.2 Servisleri Sırasıyla Başlatma

```powershell
# Terminal 1 — Auth Service
cd auth-service
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
$env:DB_HOST="localhost"; $env:DB_USER="srll"; $env:DB_PASS="srll_pass"
mvn spring-boot:run

# Terminal 2 — Card Service
cd card-service
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
$env:DB_HOST="localhost"; $env:MONGO_HOST="localhost"
$env:KAFKA_BOOTSTRAP="localhost:9092"
mvn spring-boot:run

# Terminal 3 — Gamification Service
cd gamification-service
$env:MONGO_HOST="localhost"; $env:REDIS_HOST="localhost"
$env:KAFKA_BOOTSTRAP="localhost:9092"
mvn spring-boot:run

# Terminal 4 — API Gateway
cd api-gateway
$env:JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1zcmxsLXRlc3Rpbmctb25seQ=="
mvn spring-boot:run
```

### 3.3 Sağlık Kontrolleri

```powershell
# Her servis için actuator health kontrolü
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/health
```

Beklenen yanıt her servis için:
```json
{ "status": "UP" }
```

### 3.4 Test Dependencies (pom.xml)

Her servisteki `pom.xml`'e aşağıdaki bağımlılıklar eklenmiş olmalı:

```xml
<!-- Entegrasyon testleri için TestContainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST katmanı testi -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>spring-mock-mvc</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 4. Birim Testleri

### 4.1 Mevcut Testler (Geçmeli)

```powershell
# Tüm birim testlerini çalıştır
mvn test -pl auth-service,card-service,gamification-service
```

| Test Sınıfı | Test Sayısı | Durum |
|-------------|-------------|-------|
| `AuthServiceTest` | 4 | Mevcut |
| `SM2AlgorithmTest` | 7 | Mevcut |
| `GamificationServiceTest` | 4 | Mevcut |

### 4.2 Eksik Birim Testleri — Yazılması Gerekenler

#### 4.2.1 DeckService Testleri

**Dosya:** `card-service/src/test/java/com/srll/card/service/DeckServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock DeckRepository deckRepository;
    @InjectMocks DeckService deckService;

    // Test 1: Kullanıcıya ait deck listesi döner
    @Test
    void getDecks_returnsOnlyUserDecks() {
        Long userId = 1L;
        List<Deck> decks = List.of(new Deck(), new Deck());
        when(deckRepository.findByUserId(userId)).thenReturn(decks);

        List<DeckResponse> result = deckService.getDecks(userId);

        assertThat(result).hasSize(2);
        verify(deckRepository).findByUserId(userId);
    }

    // Test 2: Başka kullanıcının deck'i erişilemez
    @Test
    void getDeck_wrongUser_throwsResourceNotFoundException() {
        when(deckRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.getDeck(1L, 99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // Test 3: Deck silme card cascade kontrolü
    @Test
    void deleteDeck_ownedByUser_callsDelete() {
        Deck deck = new Deck();
        deck.setId(1L);
        when(deckRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(deck));

        deckService.deleteDeck(1L, 1L);

        verify(deckRepository).delete(deck);
    }

    // Test 4: Deck oluşturmada userId deck'e atanır
    @Test
    void createDeck_setsUserIdOnDeck() {
        DeckRequest request = new DeckRequest("Spanish Basics", "desc", "es");
        when(deckRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeckResponse result = deckService.createDeck(request, 42L);

        // Kaydedilen entity'nin userId == 42 olduğunu doğrula
        ArgumentCaptor<Deck> captor = ArgumentCaptor.forClass(Deck.class);
        verify(deckRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(42L);
    }
}
```

#### 4.2.2 CardService Testleri

**Dosya:** `card-service/src/test/java/com/srll/card/service/CardServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock CardRepository cardRepository;
    @Mock DeckRepository deckRepository;
    @Mock ReviewHistoryRepository reviewHistoryRepository;
    @Mock KafkaTemplate<String, ReviewCompletedEvent> kafkaTemplate;
    @Mock SM2Algorithm sm2Algorithm;
    @InjectMocks CardService cardService;

    // Test 1: Review sonrası Kafka event yayınlanır
    @Test
    void review_publishesKafkaEvent() {
        Card card = buildCard(1L, 1L);
        ReviewRequest req = new ReviewRequest(4);
        SM2Result sm2Result = new SM2Result(1, 6, 2.5);

        when(cardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(card));
        when(sm2Algorithm.calculate(any(), any(), any(), eq(4))).thenReturn(sm2Result);
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reviewHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.review(1L, req, 1L);

        ArgumentCaptor<ReviewCompletedEvent> eventCaptor =
            ArgumentCaptor.forClass(ReviewCompletedEvent.class);
        verify(kafkaTemplate).send(eq("review-completed"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getRating()).isEqualTo(4);
        assertThat(eventCaptor.getValue().isPassed()).isTrue(); // rating >= 3
    }

    // Test 2: Başarısız review (rating < 3) passed=false gönderir
    @Test
    void review_failedRating_sendsPassedFalse() {
        Card card = buildCard(1L, 1L);
        ReviewRequest req = new ReviewRequest(1); // failed
        when(cardRepository.findByIdAndDeckUserId(1L, 1L)).thenReturn(Optional.of(card));
        when(sm2Algorithm.calculate(any(), any(), any(), eq(1))).thenReturn(new SM2Result(0, 1, 2.3));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reviewHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.review(1L, req, 1L);

        ArgumentCaptor<ReviewCompletedEvent> captor = ArgumentCaptor.forClass(ReviewCompletedEvent.class);
        verify(kafkaTemplate).send(eq("review-completed"), captor.capture());
        assertThat(captor.getValue().isPassed()).isFalse();
    }

    // Test 3: Due cards doğru zamanla filtrelenir
    @Test
    void getDueCards_returnsCardsWithNextReviewBeforeNow() {
        Long userId = 1L;
        List<Card> dueCards = List.of(buildCard(1L, 1L), buildCard(2L, 1L));
        when(cardRepository.findDueCards(eq(userId), any(LocalDateTime.class)))
            .thenReturn(dueCards);

        List<CardResponse> result = cardService.getDueCards(userId);

        assertThat(result).hasSize(2);
    }

    // Test 4: Farklı deck'teki karta erişim engellenir
    @Test
    void createCard_deckNotOwnedByUser_throwsException() {
        when(deckRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(1L, new CardRequest("f", "b"), 99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private Card buildCard(Long id, Long userId) {
        Deck deck = new Deck(); deck.setUserId(userId);
        Card card = new Card(); card.setId(id); card.setDeck(deck);
        card.setIntervalDays(1); card.setRepetitions(0); card.setEaseFactor(2.5);
        return card;
    }
}
```

#### 4.2.3 BadgeAwarder Testleri

**Dosya:** `gamification-service/src/test/java/com/srll/gamification/service/BadgeAwarderTest.java`

```java
@ExtendWith(MockitoExtension.class)
class BadgeAwarderTest {

    @InjectMocks BadgeAwarder badgeAwarder;

    // İlk review'da FIRST_REVIEW badge verilir
    @Test
    void award_firstReview_grantsFirstReviewBadge() {
        UserProgress progress = new UserProgress();
        progress.setTotalReviews(1);
        progress.setEarnedBadges(new ArrayList<>());

        badgeAwarder.award(progress);

        assertThat(progress.getEarnedBadges()).contains("FIRST_REVIEW");
    }

    // 100 review'da REVIEWS_100 badge verilir
    @Test
    void award_100Reviews_grantsReviews100Badge() {
        UserProgress progress = new UserProgress();
        progress.setTotalReviews(100);
        progress.setEarnedBadges(new ArrayList<>(List.of("FIRST_REVIEW")));

        badgeAwarder.award(progress);

        assertThat(progress.getEarnedBadges()).contains("REVIEWS_100");
    }

    // Zaten kazanılmış badge tekrar eklenmez
    @Test
    void award_alreadyEarned_doesNotDuplicate() {
        UserProgress progress = new UserProgress();
        progress.setTotalReviews(1);
        progress.setEarnedBadges(new ArrayList<>(List.of("FIRST_REVIEW")));

        badgeAwarder.award(progress);

        long count = progress.getEarnedBadges().stream()
            .filter("FIRST_REVIEW"::equals).count();
        assertThat(count).isEqualTo(1);
    }

    // Level 5'te LEVEL_5 badge verilir
    @Test
    void award_level5_grantsLevel5Badge() {
        UserProgress progress = new UserProgress();
        progress.setLevel(5);
        progress.setEarnedBadges(new ArrayList<>());

        badgeAwarder.award(progress);

        assertThat(progress.getEarnedBadges()).contains("LEVEL_5");
    }
}
```

#### 4.2.4 SM2Algorithm Edge Case Testleri (Mevcut Testlere Ek)

```java
// Ease factor minimum 1.3'ün altına düşmez (art arda kötü rating)
@Test
void easeFactorNeverDropsBelowMinimum_afterMultipleHardRatings() {
    SM2Result result = algorithm.calculate(5, 10, 1.4, 2);
    assertThat(result.getEaseFactor()).isGreaterThanOrEqualTo(1.3);

    SM2Result result2 = algorithm.calculate(5, 10, 1.3, 2);
    assertThat(result2.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
}

// Rating 5 (çok kolay) ease factor'ü artırır
@Test
void perfectRating_increasesEaseFactor() {
    SM2Result result = algorithm.calculate(1, 1, 2.5, 5);
    assertThat(result.getEaseFactor()).isGreaterThan(2.5);
}

// Rating sınır değerleri: 0 ve 5
@Test
void boundaryRatings_doNotThrowException() {
    assertThatNoException().isThrownBy(() -> algorithm.calculate(0, 0, 2.5, 0));
    assertThatNoException().isThrownBy(() -> algorithm.calculate(3, 6, 2.5, 5));
}
```

---

## 5. Entegrasyon Testleri

TestContainers kullanarak gerçek veritabanlarıyla entegrasyon testleri.

### 5.1 Auth Service — Repository Entegrasyon Testleri

**Dosya:** `auth-service/src/test/java/com/srll/auth/repository/UserRepositoryIntegrationTest.java`

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("srll_auth_test")
        .withUsername("srll")
        .withPassword("srll_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserRepository userRepository;

    @Test
    void save_thenFindByUsername_returnsUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encoded_password");
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void existsByUsername_duplicateUsername_returnsTrue() {
        User user = new User();
        user.setUsername("duplicate"); user.setEmail("d@d.com");
        user.setPassword("pass"); user.setRole(Role.ROLE_USER); user.setEnabled(true);
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("duplicate")).isTrue();
    }

    @Test
    void findByEmail_nonExistent_returnsEmpty() {
        assertThat(userRepository.findByEmail("nonexistent@test.com")).isEmpty();
    }
}
```

### 5.2 Card Service — Review Flow Entegrasyon Testi

**Dosya:** `card-service/src/test/java/com/srll/card/service/CardReviewIntegrationTest.java`

```java
@SpringBootTest
@Testcontainers
class CardReviewIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("srll_cards_test")
        .withUsername("srll").withPassword("srll_pass");

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired CardService cardService;
    @Autowired CardRepository cardRepository;
    @Autowired DeckRepository deckRepository;
    @Autowired ReviewHistoryRepository reviewHistoryRepository;

    @Test
    void review_savesHistoryToMongo_andUpdatesCardInPostgres() {
        // Hazırlık: deck ve card oluştur
        Deck deck = new Deck(); deck.setUserId(1L);
        deck.setName("Test Deck"); deck.setLanguage("en");
        deck = deckRepository.save(deck);

        Card card = new Card(); card.setDeck(deck);
        card.setFront("Hello"); card.setBack("Merhaba");
        card.setIntervalDays(1); card.setRepetitions(0); card.setEaseFactor(2.5);
        card.setNextReview(LocalDateTime.now().minusMinutes(1));
        card = cardRepository.save(card);

        // Review gönder
        cardService.review(card.getId(), new ReviewRequest(4), 1L);

        // PostgreSQL'de card güncellendi mi?
        Card updated = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(updated.getRepetitions()).isEqualTo(1);
        assertThat(updated.getIntervalDays()).isEqualTo(1);
        assertThat(updated.getNextReview()).isAfter(LocalDateTime.now());

        // MongoDB'de review history kaydedildi mi?
        List<ReviewHistory> history = reviewHistoryRepository
            .findByCardIdOrderByReviewedAtDesc(card.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getRating()).isEqualTo(4);
    }
}
```

### 5.3 Gamification Service — Kafka Consumer Entegrasyon Testi

**Dosya:** `gamification-service/src/test/java/com/srll/gamification/service/ReviewEventConsumerIntegrationTest.java`

```java
@SpringBootTest
@Testcontainers
class ReviewEventConsumerIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired KafkaTemplate<String, ReviewCompletedEvent> kafkaTemplate;
    @Autowired UserProgressRepository progressRepository;

    @Test
    void reviewEvent_consumed_updatesUserProgress() throws InterruptedException {
        Long userId = 42L;
        ReviewCompletedEvent event = new ReviewCompletedEvent(userId, 1L, 4, true);

        kafkaTemplate.send("review-completed", event);

        // Kafka async tüketimi için bekle
        await().atMost(10, SECONDS).untilAsserted(() -> {
            Optional<UserProgress> progress = progressRepository.findByUserId(userId);
            assertThat(progress).isPresent();
            assertThat(progress.get().getXp()).isGreaterThan(0);
            assertThat(progress.get().getTotalReviews()).isEqualTo(1);
        });
    }

    @Test
    void multipleReviews_accumulateXpAndLevelUp() throws InterruptedException {
        Long userId = 43L;
        // 10 başarılı review = 100 XP = level 2
        for (int i = 0; i < 10; i++) {
            kafkaTemplate.send("review-completed",
                new ReviewCompletedEvent(userId, (long) i, 4, true));
        }

        await().atMost(15, SECONDS).untilAsserted(() -> {
            Optional<UserProgress> progress = progressRepository.findByUserId(userId);
            assertThat(progress).isPresent();
            assertThat(progress.get().getLevel()).isGreaterThanOrEqualTo(2);
        });
    }
}
```

---

## 6. API Uçtan Uca Testleri

### 6.1 Tam Auth Flow

Tüm testleri çalıştırmadan önce Docker altyapısının ve servislerin ayakta olduğundan emin olun.

#### 6.1.1 Kayıt (Register)

```powershell
$registerBody = @{
    username = "testuser"
    email    = "testuser@example.com"
    password = "Test1234!"
} | ConvertTo-Json

$registerResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/auth/register" `
    -ContentType "application/json" `
    -Body $registerBody

$registerResp | ConvertTo-Json -Depth 3
```

**Beklenen yanıt (HTTP 201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "testuser",
    "role": "ROLE_USER"
  }
}
```

**Hata senaryosu — duplicate username (HTTP 400):**
```json
{
  "success": false,
  "message": "Username already taken"
}
```

#### 6.1.2 Giriş (Login) ve Token Alma

```powershell
$loginBody = @{
    username = "testuser"
    password = "Test1234!"
} | ConvertTo-Json

$loginResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$TOKEN = $loginResp.data.token
Write-Host "Token alındı: $($TOKEN.Substring(0,20))..."
```

**Beklenen yanıt (HTTP 200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser",
    "role": "ROLE_USER"
  },
  "errors": null
}
```

#### 6.1.3 Token Olmadan Korunan Endpoint (Unauthorized)

```powershell
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks"
} catch {
    Write-Host "HTTP Durum: $($_.Exception.Response.StatusCode)" # 401 bekleniyor
}
```

**Beklenen:** HTTP 401 Unauthorized

---

### 6.2 Deck Yönetimi

#### 6.2.1 Deck Oluşturma

```powershell
$deckBody = @{
    name        = "İspanyolca Temel"
    description = "A1 seviyesi kelimeler"
    language    = "es"
} | ConvertTo-Json

$deckResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/decks" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $deckBody

$DECK_ID = $deckResp.data.id
Write-Host "Deck oluşturuldu, ID: $DECK_ID"
```

**Beklenen yanıt (HTTP 201):**
```json
{
  "success": true,
  "message": "Deck created",
  "data": {
    "id": 1,
    "name": "İspanyolca Temel",
    "description": "A1 seviyesi kelimeler",
    "language": "es",
    "cardCount": 0,
    "createdAt": "2026-05-13T10:00:00"
  },
  "errors": null
}
```

#### 6.2.2 Deck Listesi Getirme

```powershell
$decks = Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/decks" `
    -Headers @{ Authorization = "Bearer $TOKEN" }

Write-Host "Toplam deck sayısı: $($decks.data.Count)"
```

**Beklenen:** HTTP 200, kullanıcıya ait deck listesi

#### 6.2.3 Tek Deck Getirme

```powershell
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/decks/$DECK_ID" `
    -Headers @{ Authorization = "Bearer $TOKEN" }
```

**Hata senaryosu — var olmayan ID (HTTP 404):**
```json
{
  "success": false,
  "message": "Deck not found",
  "data": null,
  "errors": null
}
```

#### 6.2.4 Deck Güncelleme

```powershell
$updateBody = @{
    name        = "İspanyolca Temel (Güncellendi)"
    description = "A1-A2 seviyesi kelimeler"
    language    = "es"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method PUT `
    -Uri "http://localhost:8080/api/decks/$DECK_ID" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $updateBody
```

**Beklenen:** HTTP 200, güncellenmiş deck verisi

#### 6.2.5 Deck Silme

```powershell
Invoke-RestMethod `
    -Method DELETE `
    -Uri "http://localhost:8080/api/decks/$DECK_ID" `
    -Headers @{ Authorization = "Bearer $TOKEN" }
```

**Beklenen:** HTTP 200 `{"success":true,"message":"Deck deleted"}`

---

### 6.3 Kart Yönetimi

#### 6.3.1 Kart Oluşturma

```powershell
# Önce test için yeni bir deck oluştur
$deckBody = @{ name = "Test Deck"; description = "test"; language = "es" } | ConvertTo-Json
$deckResp = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/decks" `
    -ContentType "application/json" -Headers @{ Authorization = "Bearer $TOKEN" } -Body $deckBody
$DECK_ID = $deckResp.data.id

# Kart oluştur
$cardBody = @{
    front = "Hola"
    back  = "Merhaba"
} | ConvertTo-Json

$cardResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/decks/$DECK_ID/cards" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $cardBody

$CARD_ID = $cardResp.data.id
Write-Host "Kart oluşturuldu, ID: $CARD_ID"
```

**Beklenen yanıt (HTTP 201):**
```json
{
  "success": true,
  "message": "Card created",
  "data": {
    "id": 1,
    "front": "Hola",
    "back": "Merhaba",
    "intervalDays": 1,
    "repetitions": 0,
    "easeFactor": 2.5,
    "nextReview": "2026-05-13T10:00:00",
    "deckId": 1
  },
  "errors": null
}
```

#### 6.3.2 Deck'teki Kartları Listeleme

```powershell
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/decks/$DECK_ID/cards" `
    -Headers @{ Authorization = "Bearer $TOKEN" }
```

#### 6.3.3 Tekrar Edilecek Kartları Getirme

```powershell
$dueCards = Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/cards/due" `
    -Headers @{ Authorization = "Bearer $TOKEN" }

Write-Host "Bugün tekrar edilecek kart: $($dueCards.data.Count)"
```

**Beklenen:** HTTP 200, nextReview <= şimdiki zaman olan kartlar

#### 6.3.4 Kart Güncelleme

```powershell
$updateBody = @{ front = "Hola!"; back = "Merhaba!" } | ConvertTo-Json

Invoke-RestMethod `
    -Method PUT `
    -Uri "http://localhost:8080/api/cards/$CARD_ID" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $updateBody
```

---

### 6.4 Kart Review Akışı (Kritik — SM-2 + Kafka)

Bu test hem PostgreSQL güncellemesini, hem MongoDB tarihini, hem de Kafka'dan gamification güncellemesini doğrular.

```powershell
# Rating 4 (iyi) ile review
$reviewBody = @{ rating = 4 } | ConvertTo-Json

$reviewResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/cards/$CARD_ID/review" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $reviewBody

$reviewResp | ConvertTo-Json -Depth 3
```

**Beklenen yanıt (HTTP 200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "deckId": 1,
    "front": "Hola",
    "back": "Merhaba",
    "intervalDays": 1,
    "repetitions": 1,
    "easeFactor": 2.5,
    "nextReview": "2026-05-14T10:00:00"
  }
}
```

**Doğrulama kontrol listesi:**
- [ ] `repetitions` == 1 (ilk başarılı review)
- [ ] `intervalDays` == 1 (SM-2 algoritması ilk geçişte 1 gün verir)
- [ ] `nextReview` ~bugün + 1 gün
- [ ] rating >= 3 ise `easeFactor` sabit veya artar

**Rating 0 (tamamen unutulmuş) ile review:**
```powershell
$reviewBody = @{ rating = 0 } | ConvertTo-Json
$reviewResp = Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/cards/$CARD_ID/review" `
    -ContentType "application/json" `
    -Headers @{ Authorization = "Bearer $TOKEN" } `
    -Body $reviewBody
```

**Beklenen:** `repetitions: 0` (sıfırlanır), `intervalDays: 1`, `easeFactor` azalır

---

### 6.5 Gamification Akışı

#### 6.5.1 Kullanıcı İlerleme Durumu

```powershell
# Review yapıldıktan ~2-3 saniye sonra kontrol et (Kafka async)
Start-Sleep -Seconds 3

$progress = Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/gamification/progress" `
    -Headers @{ Authorization = "Bearer $TOKEN" }

$progress.data | ConvertTo-Json
```

**Beklenen yanıt (HTTP 200):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "xp": 10,
    "level": 1,
    "totalReviews": 1,
    "totalCorrect": 1,
    "earnedBadges": ["FIRST_REVIEW"],
    "lastReviewDate": "2026-05-13"
  }
}
```

**Doğrulama kontrol listesi:**
- [ ] `xp` > 0 (review sonrası XP verildi)
- [ ] `totalReviews` == 1
- [ ] `earnedBadges` içinde `"FIRST_REVIEW"` var (ilk review badge'i)
- [ ] İlk başarısız review için `totalCorrect` == 0

#### 6.5.2 Liderboard (Public Endpoint — Token Gerekmez)

```powershell
$leaderboard = Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/gamification/leaderboard"

$leaderboard.data | ConvertTo-Json
```

**Beklenen:** HTTP 200, `data` array (boş veya dolu)

---

### 6.6 Çapraz Kullanıcı Güvenlik Testleri

Bu testler, bir kullanıcının başka kullanıcının kaynaklarına erişemeyeceğini doğrular.

```powershell
# İkinci kullanıcıyı kaydet ve token al
$user2Body = @{
    username = "hacker"
    email    = "hacker@example.com"
    password = "Hack1234!"
} | ConvertTo-Json
$user2Resp = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/register" `
    -ContentType "application/json" -Body $user2Body
$TOKEN2 = $user2Resp.data.token

# user2 ile user1'in deck'ine erişim
try {
    Invoke-RestMethod `
        -Method GET `
        -Uri "http://localhost:8080/api/decks/$DECK_ID" `
        -Headers @{ Authorization = "Bearer $TOKEN2" }
    Write-Host "GÜVENLİK AÇIĞI: Başka kullanıcının deck'ine erişildi!"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404 -or $statusCode -eq 403) {
        Write-Host "BAŞARILI: Başka kullanıcının deck'i engellendi (HTTP $statusCode)"
    } else {
        Write-Host "HATA: Beklenmedik status $statusCode"
    }
}

# user2 ile user1'in kartını silme girişimi
try {
    Invoke-RestMethod `
        -Method DELETE `
        -Uri "http://localhost:8080/api/cards/$CARD_ID" `
        -Headers @{ Authorization = "Bearer $TOKEN2" }
    Write-Host "GÜVENLİK AÇIĞI: Başka kullanıcının kartı silindi!"
} catch {
    Write-Host "BAŞARILI: Başka kullanıcının kartı silinemedi"
}
```

---

### 6.7 Doğrulama (Validation) Testleri

```powershell
# Geçersiz email ile kayıt
$invalidBody = @{
    username = "x"
    email    = "not-an-email"
    password = "123"  # çok kısa
} | ConvertTo-Json

try {
    Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/register" `
        -ContentType "application/json" -Body $invalidBody
} catch {
    $resp = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Hata alanları: $($resp.errors | ConvertTo-Json)"
}
```

**Beklenen:** HTTP 400, `errors` alanında field-level hata mesajları

```powershell
# Rating sınır dışı değer
$invalidReview = @{ rating = 6 } | ConvertTo-Json  # max 5
try {
    Invoke-RestMethod -Method POST `
        -Uri "http://localhost:8080/api/cards/$CARD_ID/review" `
        -ContentType "application/json" `
        -Headers @{ Authorization = "Bearer $TOKEN" } `
        -Body $invalidReview
} catch {
    Write-Host "Beklenen hata: $($_.Exception.Response.StatusCode)"  # 400
}
```

---

### 6.8 Otomatik E2E Test Script'i

Yukarıdaki tüm adımları sırasıyla çalıştıran tam script:

**Dosya:** `scripts/e2e-test.ps1`

```powershell
<#
.SYNOPSIS
    DeckOS — Backend E2E Test Script
.DESCRIPTION
    Tüm backend endpoint'lerini sırasıyla test eder.
    Ön koşul: Docker altyapısı ve tüm servisler çalışıyor olmalı.
#>

param(
    [string]$GatewayUrl = "http://localhost:8080",
    [string]$TestUser   = "e2e_user_$(Get-Random)",
    [string]$TestPass   = "E2eTest1234!"
)

$ErrorActionPreference = "Stop"
$PASS = 0; $FAIL = 0

function Test-Case {
    param([string]$Name, [scriptblock]$Block)
    Write-Host "`n--- $Name ---" -ForegroundColor Cyan
    try {
        & $Block
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:PASS++
    } catch {
        Write-Host "[FAIL] $Name : $_" -ForegroundColor Red
        $script:FAIL++
    }
}

# 1. Register
Test-Case "Register yeni kullanıcı" {
    $body = @{ username=$TestUser; email="$TestUser@test.com"; password=$TestPass } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method POST -Uri "$GatewayUrl/api/auth/register" `
        -ContentType "application/json" -Body $body
    if (-not $resp.success) { throw "Register başarısız: $($resp.message)" }
    $script:TOKEN = $resp.data.token
}

# 2. Login
Test-Case "Login ve token alma" {
    $body = @{ username=$TestUser; password=$TestPass } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method POST -Uri "$GatewayUrl/api/auth/login" `
        -ContentType "application/json" -Body $body
    if (-not $resp.data.token) { throw "Token alınamadı" }
}

# 3. Deck oluştur
Test-Case "Deck oluştur" {
    $body = @{ name="E2E Test Deck"; description="test"; language="en" } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method POST -Uri "$GatewayUrl/api/decks" `
        -ContentType "application/json" `
        -Headers @{ Authorization="Bearer $script:TOKEN" } -Body $body
    $script:DECK_ID = $resp.data.id
    if (-not $script:DECK_ID) { throw "Deck ID alınamadı" }
}

# 4. Kart oluştur
Test-Case "Kart oluştur" {
    $body = @{ front="Hello"; back="Merhaba" } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method POST `
        -Uri "$GatewayUrl/api/decks/$($script:DECK_ID)/cards" `
        -ContentType "application/json" `
        -Headers @{ Authorization="Bearer $script:TOKEN" } -Body $body
    $script:CARD_ID = $resp.data.id
    if (-not $script:CARD_ID) { throw "Card ID alınamadı" }
}

# 5. Due cards
Test-Case "Due cards listele" {
    $resp = Invoke-RestMethod -Method GET -Uri "$GatewayUrl/api/cards/due" `
        -Headers @{ Authorization="Bearer $script:TOKEN" }
    if ($null -eq $resp.data) { throw "Data null" }
}

# 6. Review gönder (rating 4)
Test-Case "Kart review (rating=4)" {
    $body = @{ rating=4 } | ConvertTo-Json
    $resp = Invoke-RestMethod -Method POST `
        -Uri "$GatewayUrl/api/cards/$($script:CARD_ID)/review" `
        -ContentType "application/json" `
        -Headers @{ Authorization="Bearer $script:TOKEN" } -Body $body
    if ($resp.data.repetitions -lt 1) { throw "repetitions artmadı: $($resp.data.repetitions)" }
}

# 7. Gamification (Kafka async — 3 saniye bekle)
Test-Case "Gamification XP kontrolü" {
    Start-Sleep -Seconds 3
    $resp = Invoke-RestMethod -Method GET `
        -Uri "$GatewayUrl/api/gamification/progress" `
        -Headers @{ Authorization="Bearer $script:TOKEN" }
    if ($resp.data.xp -le 0) { throw "XP verilmedi: $($resp.data.xp)" }
    if ($resp.data.earnedBadges -notcontains "FIRST_REVIEW") {
        throw "FIRST_REVIEW badge eksik: $($resp.data.earnedBadges)"
    }
}

# 8. Leaderboard (public)
Test-Case "Leaderboard (public, token gereksiz)" {
    Invoke-RestMethod -Method GET -Uri "$GatewayUrl/api/gamification/leaderboard"
}

# 9. Token olmadan erişim reddi
Test-Case "Yetkisiz erişim reddi" {
    $status = 0
    try {
        Invoke-RestMethod -Method GET -Uri "$GatewayUrl/api/decks"
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
    }
    if ($status -ne 401) { throw "401 beklendi, $status geldi" }
}

# 10. Temizlik: deck sil
Test-Case "Deck sil" {
    Invoke-RestMethod -Method DELETE `
        -Uri "$GatewayUrl/api/decks/$($script:DECK_ID)" `
        -Headers @{ Authorization="Bearer $script:TOKEN" }
}

# Özet
Write-Host "`n========================================" -ForegroundColor White
Write-Host "SONUÇ: $PASS PASS, $FAIL FAIL" -ForegroundColor $(if ($FAIL -eq 0) { "Green" } else { "Red" })
Write-Host "========================================`n" -ForegroundColor White

if ($FAIL -gt 0) { exit 1 }
```

Kullanım:
```powershell
.\scripts\e2e-test.ps1
# veya özel gateway URL ile:
.\scripts\e2e-test.ps1 -GatewayUrl "http://localhost:8080"
```

---

## 7. Güvenlik Testleri

### 7.1 JWT Doğrulama Kontrolleri

```powershell
# 1. Geçersiz token formatı
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks" `
        -Headers @{ Authorization = "Bearer not.valid.token" }
} catch { Write-Host "Beklenen 401: $($_.Exception.Response.StatusCode)" }

# 2. Token olmadan (Authorization header yok)
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks"
} catch { Write-Host "Beklenen 401: $($_.Exception.Response.StatusCode)" }

# 3. Bearer prefix olmadan
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks" `
        -Headers @{ Authorization = $TOKEN }  # "Bearer " prefix'i yok
} catch { Write-Host "Beklenen 401: $($_.Exception.Response.StatusCode)" }

# 4. Manipüle edilmiş payload (imza geçersiz)
$parts    = $TOKEN.Split(".")
$fakePart = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('{"userId":999,"role":"ROLE_ADMIN"}'))
$fakeToken = "$($parts[0]).$fakePart.$($parts[2])"
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks" `
        -Headers @{ Authorization = "Bearer $fakeToken" }
} catch { Write-Host "Beklenen 401 (imza hatalı): $($_.Exception.Response.StatusCode)" }
```

### 7.2 Gateway Header Injection Koruması

Downstream servislere doğrudan bağlanarak gateway'i bypass etme girişimi:

```powershell
# Card Service'e doğrudan bağlan, sahte X-User-Id gönder
try {
    Invoke-RestMethod -Method GET -Uri "http://localhost:8082/api/decks" `
        -Headers @{
            "X-User-Id"   = "1"
            "X-User-Role" = "ROLE_ADMIN"
        }
    Write-Host "DİKKAT: Downstream servis doğrudan erişime izin veriyor!"
} catch {
    Write-Host "BAŞARILI: Downstream servis header injection'ı reddetti"
}
```

**Beklenen:** Downstream servislerin gateway dışından gelen istekleri reddetmesi (HTTP 401/403)

> **Not:** Mevcut mimaride `GatewayHeaderAuthFilter` yalnızca gateway'den gelen `X-User-Id` ve `X-User-Role` header'larına güveniyor. Eğer servislere doğrudan erişim mümkünse, ağ katmanında (Docker network izolasyonu) veya servis içinde ek doğrulama gerekebilir.

### 7.3 Rate Limiting Kontrolü

```powershell
# 10 hızlı istek göndererek rate limiting var mı kontrol et
1..10 | ForEach-Object {
    $body = @{ username="spam$_"; email="spam$_@t.com"; password="p" } | ConvertTo-Json
    try {
        $r = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/register" `
            -ContentType "application/json" -Body $body
        Write-Host "İstek $_ : $($r.success)"
    } catch {
        Write-Host "İstek $_ : HTTP $($_.Exception.Response.StatusCode.value__)"
    }
}
```

**Beklenen:** Mevcut durumda rate limiting yok — bu bir **eksiklik** olarak kaydedilmiştir (bkz. Bölüm 12).

---

## 8. Kafka Event-Driven Testleri

### 8.1 Event Yayınlanıyor mu Kontrolü

```powershell
# Kafka consumer'ı başlat (ayrı terminal)
docker exec -it spaced-repetition-language-learning-kafka-1 kafka-console-consumer `
    --bootstrap-server localhost:9092 `
    --topic review-completed `
    --from-beginning `
    --max-messages 5
```

Sonra review gönderin ve Kafka consumer'da event göründüğünü doğrulayın:

```json
{
  "userId": 1,
  "cardId": 1,
  "rating": 4,
  "passed": true
}
```

### 8.2 Consumer Lag Kontrolü

```powershell
docker exec spaced-repetition-language-learning-kafka-1 kafka-consumer-groups `
    --bootstrap-server localhost:9092 `
    --group gamification-group `
    --describe
```

**Beklenen:** `LAG` sütununun 0'a yakın olması (mesajlar tüketildi)

### 8.3 Kafka Topic Listesi Doğrulama

```powershell
docker exec spaced-repetition-language-learning-kafka-1 kafka-topics `
    --bootstrap-server localhost:9092 `
    --list
```

**Beklenen çıktı:**
```
review-completed
```

### 8.4 Dead Letter Queue (Hata Senaryosu)

```powershell
# Geçersiz event formatı gönder
docker exec spaced-repetition-language-learning-kafka-1 kafka-console-producer `
    --bootstrap-server localhost:9092 `
    --topic review-completed

# Producer'da şunu yaz: (geçersiz JSON)
> not-valid-json
```

**Beklenen:** Gamification service'in geçersiz mesajı işleyemediğinde uygulamanın çökmemesi.  
**Mevcut durum:** Hata yönetimi `@KafkaListener`'da kontrol edilmeli.

---

## 9. Performans Testleri

### 9.1 JMeter Test Planı

**Dosya:** `jmeter/backend-load-test.jmx`

Aşağıdaki test planı manuel olarak oluşturulmalıdır:

```
JMeter Test Plan
├── Thread Group: Auth (10 kullanıcı, 60 saniye)
│   ├── HTTP Request: POST /api/auth/register
│   ├── HTTP Request: POST /api/auth/login
│   └── Response Assertion: status 200
│
├── Thread Group: Card Operations (50 kullanıcı, 120 saniye)
│   ├── HTTP Header Manager: Authorization Bearer ${token}
│   ├── HTTP Request: POST /api/decks
│   ├── HTTP Request: POST /api/decks/${deckId}/cards
│   ├── HTTP Request: GET /api/cards/due
│   └── HTTP Request: POST /api/cards/${cardId}/review
│
├── Thread Group: Leaderboard (100 kullanıcı, 60 saniye)
│   └── HTTP Request: GET /api/gamification/leaderboard
│
└── Listeners
    ├── Summary Report
    ├── Response Time Graph
    └── Active Threads Over Time
```

### 9.2 Hedef Metrikler

| Endpoint | P50 | P95 | P99 | Max Throughput |
|----------|-----|-----|-----|----------------|
| POST /api/auth/login | < 100ms | < 300ms | < 500ms | 50 req/s |
| GET /api/decks | < 50ms | < 150ms | < 300ms | 200 req/s |
| POST /api/cards/{id}/review | < 200ms | < 500ms | < 1000ms | 100 req/s |
| GET /api/gamification/leaderboard | < 20ms | < 50ms | < 100ms | 500 req/s |

### 9.3 Hızlı Performans Smoke Testi

```powershell
# 10 ardışık review süresi ölçümü
$TOKEN = (Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json" `
    -Body (@{ username="testuser"; password="Test1234!" } | ConvertTo-Json)).data.token

$times = @()
1..10 | ForEach-Object {
    $start = Get-Date
    Invoke-RestMethod -Method GET -Uri "http://localhost:8080/api/decks" `
        -Headers @{ Authorization = "Bearer $TOKEN" } | Out-Null
    $elapsed = (Get-Date) - $start
    $times += $elapsed.TotalMilliseconds
    Write-Host "İstek $_ : $([math]::Round($elapsed.TotalMilliseconds))ms"
}

$avg = ($times | Measure-Object -Average).Average
$max = ($times | Measure-Object -Maximum).Maximum
Write-Host "`nOrtalama: $([math]::Round($avg))ms | Max: $([math]::Round($max))ms"
```

---

## 10. Test Koşum Kılavuzu

### 10.1 Tam Test Koşumu (Sıralı)

```powershell
# Adım 1: Altyapıyı başlat
docker-compose up -d postgres mongodb redis kafka
Start-Sleep -Seconds 10  # servislerin hazır olmasını bekle

# Adım 2: Birim testleri
Write-Host "=== BİRİM TESTLERİ ===" -ForegroundColor Yellow
mvn test -pl auth-service,card-service,gamification-service
if ($LASTEXITCODE -ne 0) { Write-Host "HATA: Birim testleri başarısız!"; exit 1 }

# Adım 3: Uygulama servislerini başlat (ayrı terminallerde veya docker-compose ile)
docker-compose up -d auth-service card-service gamification-service api-gateway
Start-Sleep -Seconds 30  # Spring Boot başlangıç süresi

# Adım 4: Sağlık kontrolü
@(8080, 8081, 8082, 8083) | ForEach-Object {
    $resp = Invoke-RestMethod "http://localhost:$_/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "Servis :$_ -> $($resp.status)"
}

# Adım 5: E2E testleri
Write-Host "=== E2E TESTLERİ ===" -ForegroundColor Yellow
.\scripts\e2e-test.ps1

# Adım 6: Temizlik
docker-compose down
```

### 10.2 Sadece Birim Testleri (CI/CD)

```powershell
mvn test -pl auth-service,card-service,gamification-service `
    -Dspring.profiles.active=test
```

### 10.3 Sadece Entegrasyon Testleri (TestContainers)

```powershell
mvn verify -pl auth-service,card-service,gamification-service `
    -P integration-tests
```

### 10.4 Beklenen Test Çıktısı

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  -- auth-service
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0  -- card-service SM2
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  -- gamification-service
[INFO] BUILD SUCCESS
```

---

## 11. Kapsam Raporlama

### 11.1 JaCoCo Konfigürasyonu

Her servisin `pom.xml`'ine eklenecek:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>  <!-- %70 minimum -->
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 11.2 Kapsam Raporu Üretme

```powershell
mvn verify -pl auth-service,card-service,gamification-service

# Raporları aç
Start-Process "auth-service/target/site/jacoco/index.html"
Start-Process "card-service/target/site/jacoco/index.html"
Start-Process "gamification-service/target/site/jacoco/index.html"
```

### 11.3 Mevcut Kapsam Tahmini

| Servis | Sınıf | Satır |
|--------|-------|-------|
| auth-service | ~70% | ~65% |
| card-service (SM-2) | ~90% | ~85% |
| gamification-service | ~60% | ~55% |

> Yukarıdaki rakamlar tahmindir; gerçek değerler JaCoCo raporu ile doğrulanmalıdır.

---

## 12. Mevcut Boşluklar ve Sonraki Adımlar

### 12.1 Kritik Eksiklikler (Frontend'e Geçmeden Önce)

| # | Sorun | Etki | Öneri |
|---|-------|------|-------|
| 1 | `GamificationController.getProgress()` hiç test edilmemiş | Orta | Birim testi yazılmalı |
| 2 | `CardService` birim testleri yok | Yüksek | Bölüm 4.2.2 eklenmeli |
| 3 | `DeckService` birim testleri yok | Yüksek | Bölüm 4.2.1 eklenmeli |
| 4 | Downstream servislere doğrudan erişim (bypass) testi belirsiz | Yüksek | Docker network izolasyonu doğrulanmalı |
| 5 | Kafka `Dead Letter Queue` eksik | Orta | Consumer'da error handler tanımlanmalı |
| 6 | `nextReview` alanı null olduğunda kart zamanlaması test edilmemiş | Orta | Edge case testi eklenecek |

### 12.2 Güvenlik Boşlukları

| # | Sorun | Öneri |
|---|-------|-------|
| 1 | Rate limiting yok | `spring-cloud-gateway`'de `RequestRateLimiter` filtresi ekle |
| 2 | JWT `exp` claim sona erme testi yok | Kısa süreli token ile expiry testi yaz |
| 3 | SQL injection koruması test edilmemiş | Spring Data JPA otomatik koruma sağlar; doğrulama testi ekle |
| 4 | Admin rol endpoint koruması test edilmemiş | `ROLE_ADMIN` gerektiren işlemler varsa test edilmeli |

### 12.3 Performans Boşlukları

| # | Sorun | Öneri |
|---|-------|-------|
| 1 | `getLeaderboard` Redis cache mekanizması test edilmemiş | Redis sorted set okuma testi |
| 2 | Büyük deck altında `getDueCards` JPQL sorgu performansı bilinmiyor | 10.000 kart ile N+1 testi |
| 3 | Kafka consumer'ın yüksek event hacminde lag metrikleri izlenmiyor | JMeter ile 1000 req/s test |

### 12.4 Öncelikli Aksiyon Planı

```
Önce (Frontend'e geçmeden kritik):
  [1] DeckService ve CardService birim testleri yaz
  [2] E2E test script'ini çalıştır ve tüm testlerin geçtiğini doğrula
  [3] Docker network izolasyonunu doğrula (bypass koruması)

Sonra (İlk sprint içinde):
  [4] JaCoCo konfigürasyonu ekle, %70 hedefine ulaş
  [5] Kafka DLQ error handler ekle
  [6] Rate limiting Gateway'e ekle

Daha sonra:
  [7] JMeter yük testi ile performans baseline oluştur
  [8] JWT expiry test senaryosu ekle
```

---

## Ek A: Test Verisi Sıfırlama

```powershell
# PostgreSQL — test verilerini sil
docker exec spaced-repetition-language-learning-postgres-1 psql -U srll -d srll_auth -c "DELETE FROM users WHERE username LIKE 'e2e_%';"
docker exec spaced-repetition-language-learning-postgres-1 psql -U srll -d srll_cards -c "DELETE FROM decks WHERE name LIKE 'E2E%';"

# MongoDB — test progress kayıtlarını sil
docker exec spaced-repetition-language-learning-mongodb-1 mongosh srll_gamification --eval "db.user_progress.deleteMany({userId: {\$gt: 1000}})"

# Redis — test verilerini temizle
docker exec spaced-repetition-language-learning-redis-1 redis-cli FLUSHDB
```

---

## Ek B: Hızlı Referans — Endpoint Listesi

| Method | Yol | Auth | Servis |
|--------|-----|------|--------|
| POST | /api/auth/register | Hayır | auth-service |
| POST | /api/auth/login | Hayır | auth-service |
| GET | /api/decks | JWT | card-service |
| GET | /api/decks/{id} | JWT | card-service |
| POST | /api/decks | JWT | card-service |
| PUT | /api/decks/{id} | JWT | card-service |
| DELETE | /api/decks/{id} | JWT | card-service |
| GET | /api/decks/{deckId}/cards | JWT | card-service |
| GET | /api/cards/due | JWT | card-service |
| POST | /api/decks/{deckId}/cards | JWT | card-service |
| PUT | /api/cards/{id} | JWT | card-service |
| DELETE | /api/cards/{id} | JWT | card-service |
| POST | /api/cards/{id}/review | JWT | card-service |
| GET | /api/gamification/progress | JWT | gamification-service |
| GET | /api/gamification/leaderboard | Hayır | gamification-service |

---

*Bu doküman, projenin frontend geliştirme aşamasına geçilmeden önce backend güvenilirliğini kanıtlamak amacıyla hazırlanmıştır. Testlerin tamamı geçtikten sonra PROGRESS.md güncellenmelidir.*
