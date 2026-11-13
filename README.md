# customer-service

통신 BSS 고객정보 서비스 샘플이다. 회원 가입, 로그인, 비밀번호 재설정, 본인 확인, 내 정보 조회·수정, 상담원 고객 조회, 상담원 문구 템플릿 조회, 관리자 회원 관리·계정 아이디 검색·감사 기록 조회·내보내기·본인 확인 기관 연결 점검·상담원 일괄 배정을 제공한다.

- 스택: Java 21, Spring Boot, Spring Security, MyBatis(XML), Spring JDBC(계정 아이디 검색), Thymeleaf, Oracle
- 소스: `src/main/java`, `src/main/resources`
- 빌드: `pom.xml`
- DB 스키마: `db/schema.sql`
- 설계·명세: `docs/spec`(도메인 개요, 데이터 모델), `docs/designs`(기능 설계문서), `docs/api`(OpenAPI 계약)
