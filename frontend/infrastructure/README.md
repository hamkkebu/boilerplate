# Frontend Service Infrastructure

Frontend 서비스를 위한 Terraform 인프라 구성입니다.

## 리소스

이 구성은 다음 리소스를 생성합니다:

- **S3 Bucket**: 정적 웹사이트 호스팅
- **CloudFront Distribution**: CDN 및 HTTPS 지원
- **Origin Access Identity**: CloudFront에서 S3 접근
- **CloudWatch Log Group**: CloudFront 로그

## 아키텍처

```
User -> CloudFront -> S3 Bucket (Private)
```

- S3 버킷은 private으로 설정
- CloudFront를 통해서만 접근 가능
- HTTPS 자동 리디렉션
- SPA 라우팅 지원 (404 -> index.html)

## 사전 요구사항

1. 공통 인프라가 먼저 배포되어 있어야 합니다 (선택사항)
2. Vue.js 프로젝트가 빌드 가능한 상태여야 합니다

## 배포

```bash
cd frontend/infrastructure

# terraform.tfvars 파일 생성 및 수정
cp terraform.tfvars.example terraform.tfvars

# Terraform 초기화
terraform init -backend-config="bucket=your-terraform-state-bucket"

# 변경사항 확인
terraform plan

# 배포
terraform apply
```

## 프론트엔드 빌드 및 배포

### 1. Vue.js 애플리케이션 빌드

```bash
cd frontend
npm install
npm run build
```

### 2. S3에 업로드

```bash
# S3 버킷 이름 확인
terraform output s3_bucket_name

# 빌드된 파일 업로드
aws s3 sync dist/ s3://hamkkebu-dev-frontend/ --delete
```

### 3. CloudFront 캐시 무효화

```bash
# CloudFront Distribution ID 확인
terraform output cloudfront_distribution_id

# 캐시 무효화
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/*"
```

## 자동 배포 스크립트

`deploy.sh` 스크립트를 생성하여 배포를 자동화할 수 있습니다:

```bash
#!/bin/bash
set -e

# 빌드
echo "Building frontend..."
npm run build

# S3 업로드
echo "Uploading to S3..."
BUCKET_NAME=$(cd infrastructure && terraform output -raw s3_bucket_name)
aws s3 sync dist/ s3://$BUCKET_NAME/ --delete

# CloudFront 캐시 무효화
echo "Invalidating CloudFront cache..."
DISTRIBUTION_ID=$(cd infrastructure && terraform output -raw cloudfront_distribution_id)
aws cloudfront create-invalidation \
  --distribution-id $DISTRIBUTION_ID \
  --paths "/*"

echo "Deployment completed!"
```

## 환경별 설정

### 개발 환경

빌드 시 환경 변수 설정:

```bash
# .env.development
VUE_APP_API_URL=http://your-alb-url.ap-northeast-2.elb.amazonaws.com/api
```

### 프로덕션 환경

```bash
# .env.production
VUE_APP_API_URL=https://api.yourdomain.com
```

## CloudFront 설정

### 캐시 동작

- **기본 동작**: TTL 1시간
- **/static/***: TTL 1일 (정적 에셋)

### SPA 라우팅

Vue Router를 사용하는 경우, CloudFront는 404 및 403 에러를 `index.html`로 리디렉션합니다.

### HTTPS

기본적으로 CloudFront의 기본 인증서를 사용합니다.

커스텀 도메인 사용 시:
1. ACM에서 인증서 발급 (us-east-1 리전)
2. `main.tf`의 주석 처리된 부분을 활성화
3. Route53에서 도메인 설정

## 성능 최적화

### 1. Gzip 압축

CloudFront는 자동으로 gzip 압축을 활성화합니다.

### 2. 정적 에셋 캐싱

Vue CLI로 빌드하면 파일명에 해시가 포함되므로 장기 캐싱이 가능합니다.

### 3. CloudFront Price Class

비용 최적화를 위해 Price Class를 조정할 수 있습니다:
- `PriceClass_All`: 모든 엣지 로케이션
- `PriceClass_200`: 북미, 유럽, 아시아 (기본값)
- `PriceClass_100`: 북미, 유럽

## 모니터링

### CloudFront 메트릭

CloudWatch에서 다음 메트릭을 확인할 수 있습니다:
- Requests
- BytesDownloaded
- 4xxErrorRate
- 5xxErrorRate

### 로그

CloudFront 액세스 로그는 다음 위치에 저장됩니다:
```
/aws/cloudfront/hamkkebu-dev/frontend
```

## CORS 설정

Backend와 통신을 위해 CORS 설정이 필요할 수 있습니다.

Backend의 Spring Boot 설정:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://your-cloudfront-domain.cloudfront.net")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
```

## 커스텀 도메인 설정

### 1. ACM 인증서 발급 (us-east-1)

```bash
aws acm request-certificate \
  --domain-name yourdomain.com \
  --validation-method DNS \
  --region us-east-1
```

### 2. Terraform 변수 추가

```hcl
variable "domain_name" {
  description = "Custom domain name"
  type        = string
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN"
  type        = string
}
```

### 3. Route53 레코드 생성

```hcl
resource "aws_route53_record" "frontend" {
  zone_id = var.route53_zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}
```

## 트러블슈팅

### CloudFront에서 404 에러

1. S3 버킷에 파일이 올바르게 업로드되었는지 확인
2. CloudFront 캐시 무효화
3. CloudFront 설정에서 에러 페이지 설정 확인

### CORS 에러

1. Backend CORS 설정 확인
2. CloudFront 도메인이 허용 목록에 있는지 확인

### 캐시 문제

```bash
# 모든 캐시 무효화
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/*"

# 특정 경로만 무효화
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/index.html" "/static/*"
```

## 비용 최적화

1. CloudFront Price Class를 필요한 지역으로 제한
2. S3 Lifecycle 정책으로 오래된 버전 삭제
3. CloudFront 로그 보관 기간 설정

## 정리

리소스 삭제:

```bash
# S3 버킷 비우기
aws s3 rm s3://hamkkebu-dev-frontend/ --recursive

# Terraform으로 리소스 삭제
terraform destroy
```
