================================================================
GRUP BİLGİSİ
================================================================
Grup No       : 12
Ders          : İleri Java Uygulamaları
Öğretim Üyesi : Samet DİRİ

================================================================
EKİP ÜYELERİ
================================================================
1. Tolga Boz       (GitHub: tolgab35)
2. Umar Muskiev    (GitHub: LOKAR1432)

================================================================
GITHUB REPO
================================================================
https://github.com/tolgab35/spaced-repetition-language-learning

================================================================
PROJE KONUSU
================================================================
Oyunlaştırılmış Aralıklı Tekrar Algoritması Kullanan
Dil Öğrenme Uygulaması - "DeckOS"

SM-2 (Spaced Repetition) algoritması ile kelime kartı tekrarını
optimize eden, mikroservis mimarili, JavaFX masaüstü istemcili
bir dil öğrenme platformu.

================================================================
GÖREV DAĞILIMI
================================================================

--- Tolga Boz (tolgab35) ---

Backend Mikroservisler:
  - auth-service     : Kullanıcı kaydı, giriş, JWT üretimi ve doğrulama
  - card-service     : Deste/kart CRUD, SM-2 algoritması, Kafka producer
  - gamification-service : XP, seviye, rozet, streak, liderboard, Kafka consumer
  - api-gateway      : Spring Cloud Gateway, JWT doğrulama filtresi

Altyapı & DevOps:
  - docker-compose.yml : PostgreSQL, MongoDB, Redis, Kafka + 4 servis
  - Dockerfile (tüm servisler)
  - scripts/init-db.sql

Test & Performans:
  - JUnit 5 birim testleri (AuthServiceTest, SM2AlgorithmTest, GamificationServiceTest)
  - JMeter yük testi planı ve run scripti
  - Backend E2E test dökümanı (BACKEND_TEST_DOCUMENTATION.md)

JavaFX İyileştirmeleri & Dokümantasyon:
  - Neo-retro UI tema (CSS), liderboard geliştirmeleri
  - Kafka consumer yapılandırması düzeltmesi
  - README.md, ekran görüntüleri

--- Umar Muskiev (LOKAR1432) ---

JavaFX Masaüstü İstemci (javafx-client modülü):
  - MainApp.java        : Uygulama giriş noktası, ekran geçiş yönetimi
  - SessionManager.java : JWT token + kullanıcı bilgisi bellekte yönetimi
  - ApiClient.java      : HTTP GET/POST/PUT/DELETE, 401 yönlendirme, hata yönetimi
  - DTO record sınıfları: ApiResponse, AuthResponse, DeckResponse, CardResponse,
                          ReviewRequest, ProgressResponse, LeaderboardEntry

  API Servisleri:
  - AuthApiService.java       : register / login
  - DeckApiService.java       : CRUD /api/decks
  - CardApiService.java       : CRUD /api/cards, /due, /review
  - GamificationApiService.java : /progress, /leaderboard

  Ekranlar (FXML + Controller):
  - login.fxml + LoginController         : Kimlik doğrulama, hata etiketi, spinner
  - register.fxml + RegisterController   : Kayıt, istemci doğrulama
  - deck-list.fxml + DeckListController  : Dinamik deste kartları, oluştur/sil dialog
  - card-list.fxml + CardListController  : TableView, düzenle/sil dialog
  - review.fxml + ReviewController       : Kart çevirme animasyonu (RotateTransition),
                                           4 rating butonu, oturum özeti
  - progress.fxml + ProgressController  : Animasyonlu XP çubuğu (Timeline 800ms),
                                           rozet FlowPane, liderboard VBox

  JavaFX Test Dökümanı:
  - JAVAFX_TEST_DOCUMENTATION.md

================================================================
KULLANILAN TEKNOLOJİLER
================================================================
Backend    : Java 21, Spring Boot 3.2.5, Spring Cloud 2023.0.1
Güvenlik   : Spring Security, jjwt 0.12.5 (JWT)
Veritabanı : PostgreSQL 16, MongoDB 7, Redis 7
Mesajlaşma : Apache Kafka (KRaft modu)
GUI        : JavaFX 21 (FXML/MVC, HttpClient, Jackson)
Build      : Maven (multi-module)
Container  : Docker Compose
Test       : JUnit 5, Mockito
Yük Testi  : Apache JMeter 5.6

================================================================
PROJE YAPISI
================================================================
spaced-repetition-language-learning/
├── api-gateway/          Spring Cloud Gateway, JWT filtresi
├── auth-service/         Kayıt, giriş, JWT üretimi
├── card-service/         Deste/kart CRUD, SM-2 algoritması, Kafka
├── gamification-service/ XP, seviye, rozet, streak, liderboard
├── common/               ApiResponse<T>, GlobalExceptionHandler
├── javafx-client/        JavaFX masaüstü GUI
├── jmeter/               Yük testi planı (.jmx) ve run scripti
├── scripts/              init-db.sql, e2e-test.ps1
├── docker-compose.yml    Tüm servisleri tek komutla başlatır
└── README.md             Kurulum, API referansı, yük testi sonuçları

================================================================
ÇALIŞTIRMA
================================================================
1. mvn clean package -DskipTests
2. docker-compose up -d
3. Servisler hazır olunca (~ 30 sn):
   cd javafx-client && mvn javafx:run

================================================================
