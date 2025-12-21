# 1. 프로젝스 소개

- 다양한 상품을 거래할 수 있는 종합 쇼핑몰 이커머스 서비스를 개발하고 운영하기 위한 프로젝트
- 화면 개발부터, ERD 설계, 서버 개발, 배포까지 전 과정 경험 목표.
- 배포 URL : http://34.168.163.93/
    - 슈퍼 관리자 계정
        - ID:admin@kmarket.com
        - PW:adminkmarket


### 1-1. 프로젝트 아키텍처
- 아키텍처
 <img width="1231" height="845" alt="image" src="https://github.com/user-attachments/assets/72a8bfaa-baf4-4bb1-9fc4-5de9087ae274" />

- ERD
  <img width="1588" height="1096" alt="image" src="https://github.com/user-attachments/assets/3aff1dc9-8544-4260-be7c-14a65027be8f" />


### 1-2. 사용 기술

- **Backend**: Java, Spring Boot, JPA/Hibernate
- **Database**: MySQL
- **Frontend**: HTML/CSS/JS, Bootstrap, Thymeleaf
- **Infrastructure**: GitHub Actions, Docker, GCP, Nginx



### 1.3. 핵심 기능

**1.3.1 회원 관리 시스템**

- Spring Security 기반 인증/인가
- 일반 사용자와 판매자 역할 구분
- 회원가입, 로그인, 권한 관리

**1.3.2. 상품 관리 시스템**

- 판매자의 상품 등록/수정/삭제
- 상품 이미지 업로드 및 관리
- 재고 관리 시스템

**1.3.3. 주문 처리 시스템**

- 장바구니 기능
- 재고 확인 기반 주문 생성
- 주문 상태 관리


### 1-4. Git 워크플로우 및 테스트 전략

### 브랜치 관리 전략

- **main**: 배포 가능한 안정적인 코드 관리
- **dev**: 개발 완료된 기능들의 통합 및 테스트 환경
- **feat**: 기능별 개발 브랜치 (기능 단위로 분리하여 개발)

### 개발 프로세스

- feat 브랜치에서 기능 개발 완료 후 dev 브랜치로 병합
- dev 브랜치에서 통합 테스트 진행
- 안정성 확인 후 main 브랜치로 최종 병합

### 커밋 컨벤션

- `feat: #이슈번호 기능 설명` - 새로운 기능 추가
- `fix: #이슈번호 버그 수정 설명` - 버그 수정
- `docs: #이슈번호 문서 설명` - 문서 수정
- `chore: #이슈번호 작업 설명` - 빌드, 설정 파일 수정
- `refactor: #이슈번호 리팩토링 설명` - 코드 리팩토링

### 테스트 전략

- **단위 테스트**: 핵심 비즈니스 로직에 대한 단위 테스트 작성
- **테스트 커버리지**: 높은 테스트 커버리지 유지를 통한 품질 관리
- **테스트 기반 개발**: dev 브랜치 병합 전 필수 테스트 통과

1-5. API 문서화

- **Swagger UI**: http://34.182.127.192/swagger-ui/index.html#/
- 비동기 처리 API (장바구니, 주문) 중심 문서화
- SSR 기반 웹 애플리케이션의 AJAX 통신 인터페이스
