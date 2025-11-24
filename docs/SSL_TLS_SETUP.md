# SSL/TLS 설정 가이드

이 문서는 Spring Boot 애플리케이션에 HTTPS를 설정하는 방법을 설명합니다.

## 개발 환경: 자체 서명 인증서 (Self-Signed Certificate)

### 1. 자체 서명 인증서 생성

```bash
# keystore 디렉토리 생성
mkdir -p backend/src/main/resources/keystore

# 자체 서명 인증서 생성 (유효기간 365일)
keytool -genkeypair \
  -alias hamkkebu \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore backend/src/main/resources/keystore/keystore.p12 \
  -validity 365 \
  -storepass changeit

# 프롬프트에서 요청하는 정보 입력:
# - 이름과 성: localhost
# - 조직 단위: Development
# - 조직: Hamkkebu
# - 구/군/시: Seoul
# - 시/도: Seoul
# - 국가 코드: KR
```

### 2. application-dev.yml 설정

```yaml
server:
  port: 8443  # HTTPS 포트
  ssl:
    enabled: true
    key-store: classpath:keystore/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: hamkkebu

  # HTTP를 HTTPS로 리다이렉트 (선택사항)
  # 별도의 Configuration 클래스 필요
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. 브라우저에서 접속

```
https://localhost:8443
```

⚠️ **주의**: 자체 서명 인증서이므로 브라우저에서 보안 경고가 표시됩니다.
개발 환경에서는 "고급" → "안전하지 않음(localhost)(으)로 이동" 클릭하여 진행합니다.

---

## 프로덕션 환경: Let's Encrypt 인증서

### 1. Certbot 설치

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install certbot

# macOS
brew install certbot
```

### 2. 인증서 발급

```bash
# Standalone 방식 (포트 80, 443이 비어있어야 함)
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# 인증서 위치:
# - /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# - /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 3. PEM을 PKCS12로 변환

Spring Boot는 PKCS12 형식을 권장합니다.

```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out /etc/letsencrypt/live/yourdomain.com/keystore.p12 \
  -name hamkkebu \
  -passout pass:your-strong-password
```

### 4. application-prod.yml 설정

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:/etc/letsencrypt/live/yourdomain.com/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}  # 환경 변수로 관리
    key-store-type: PKCS12
    key-alias: hamkkebu
```

### 5. 환경 변수 설정

```bash
export SSL_KEYSTORE_PASSWORD=your-strong-password
```

### 6. 자동 갱신 설정

Let's Encrypt 인증서는 90일마다 만료되므로 자동 갱신 설정이 필요합니다.

```bash
# Cron Job 추가
sudo crontab -e

# 매월 1일 오전 3시에 갱신 시도 + Spring Boot 재시작
0 3 1 * * certbot renew --post-hook "systemctl restart hamkkebu-backend"
```

---

## HTTP → HTTPS 리다이렉트 설정

HTTP (8080)로 접속한 요청을 HTTPS (8443)로 자동 리다이렉트하려면:

### 1. HttpToHttpsRedirectConfig.java 생성

(이미 생성되어 있음: `backend/src/main/java/com/hamkkebu/boilerplate/common/config/HttpToHttpsRedirectConfig.java`)

### 2. application.yml 설정

```yaml
server:
  http:
    port: 8080  # HTTP 포트 (리다이렉트용)
```

---

## 테스트

### 1. HTTPS 연결 테스트

```bash
curl -k https://localhost:8443/actuator/health
```

### 2. HTTP → HTTPS 리다이렉트 테스트

```bash
curl -L http://localhost:8080/actuator/health
```

---

## 보안 권장 사항

### 1. 강력한 암호 사용

```bash
# 랜덤 암호 생성
openssl rand -base64 32
```

### 2. 암호는 환경 변수나 Secret Manager에 저장

```yaml
# ❌ 나쁜 예
key-store-password: changeit

# ✅ 좋은 예
key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

### 3. 최신 TLS 프로토콜 사용

```yaml
server:
  ssl:
    enabled-protocols: TLSv1.3,TLSv1.2
    ciphers: >
      TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
      TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
```

### 4. HSTS 헤더 활성화

이미 SecurityConfig.java에 설정되어 있습니다:

```java
.httpStrictTransportSecurity(hsts -> hsts
    .includeSubDomains(true)
    .maxAgeInSeconds(31536000) // 1년
)
```

---

## 트러블슈팅

### 문제 1: "Invalid keystore format"

**원인**: 잘못된 keystore 형식

**해결**:
```bash
# keystore 형식 확인
keytool -list -v -keystore backend/src/main/resources/keystore/keystore.p12
```

### 문제 2: "Cannot find alias 'hamkkebu'"

**원인**: alias 이름이 잘못됨

**해결**:
```bash
# alias 목록 확인
keytool -list -keystore backend/src/main/resources/keystore/keystore.p12
```

### 문제 3: Certbot 인증 실패

**원인**: 포트 80이 이미 사용 중

**해결**:
```bash
# 포트 80 사용 중인 프로세스 확인
sudo lsof -i :80

# 또는 DNS 방식 사용
sudo certbot certonly --manual --preferred-challenges dns -d yourdomain.com
```

---

## 참고 자료

- [Spring Boot SSL Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.configure-ssl)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [OWASP TLS Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)
