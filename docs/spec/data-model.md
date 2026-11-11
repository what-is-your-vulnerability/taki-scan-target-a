# 통신 고객정보 서비스 데이터 모델

역할·기능 ID는 `domain.md`를 따른다. 필드 이름은 구현 언어와 관계없이 그대로 쓴다.

## 1. 엔티티

### 1.1 엔티티 목록

| 엔티티 | 영문 이름 | 설명 |
|---|---|---|
| 고객 | `Customer` | 서비스에 가입한 개인 고객 |
| 계정 | `Account` | 로그인 신원. 고객 계정과 직원 계정 모두 |
| 요금제 | `Plan` | 판매 상품과 가격·할인 규칙 |
| 가입 계약 | `Subscription` | 고객의 회선 하나와 그 요금제 계약 |
| 가입 변경 이력 | `SubscriptionChange` | 가입 계약의 요금제·상태 변경 기록 |
| 청구서 | `Invoice` | 계약별 월 청구 내역 |
| 결제수단 | `PaymentMethod` | 고객 소유 카드·계좌 |
| 상담 이력 | `ConsultationRecord` | 상담원이 남긴 상담 기록 |
| 일괄 작업 | `BatchJob` | 일괄 등록 파일 처리와 정기 처리 실행 단위 |
| 감사 기록 | `AuditEvent` | 직원·시스템의 주요 행위 기록 |
| 본인 확인 요청 | `IdentityVerification` | 가입 신청 계정의 외부 본인 확인 요청과 결과 |

### 1.2 필드

| 엔티티 | 필드 | 설명 |
|---|---|---|
| Customer | `customer_id` | 고객 식별자. 서버가 발급하는 양의 정수 |
| | `name`, `birth_date`, `resident_reg_no` | 이름, 생년월일, 주민등록번호 |
| | `phone`, `email`, `address` | 연락처 휴대폰 번호, 이메일, 주소 |
| | `credit_status` | 신용 등급 구분(내부) |
| | `collection_status` | 수납 상태: `NORMAL` / `OVERDUE` / `COLLECTION` (내부) |
| | `assigned_consultant_id` | 배정 상담원의 `account_id`. 없을 수 있다 |
| | `created_at`, `updated_at` | 생성·수정 시각 |
| Account | `account_id`, `login_id` | 계정 식별자, 로그인 아이디(전체에서 유일) |
| | `customer_id` | 고객 계정이면 연결된 고객. 직원 계정은 비어 있다 |
| | `staff_name` | 직원 계정의 이름. 고객 계정은 비어 있다 |
| | `signup_ref` | 가입 신청 참조값(F1). 서버가 발급하고, 본인 확인을 마치면 더 쓰지 않는다 |
| | `password_hash` | 비밀번호 저장값(복원 불가 형태) |
| | `role` | `CUSTOMER` / `CONSULTANT` / `BILLING_OPERATOR` / `ADMIN` |
| | `status` | `PENDING_VERIFICATION` / `ACTIVE` / `LOCKED` / `SUSPENDED` / `DELETED` |
| | `failed_login_count` | 연속 로그인 실패 횟수 |
| | `identity_verified_at`, `password_changed_at`, `last_login_at` | 본인 확인 완료, 비밀번호 변경, 마지막 로그인 시각 |
| Plan | `plan_id`, `plan_code`, `name` | 요금제 식별자, 업무 코드(유일), 이름 |
| | `monthly_fee` | 월 기본료(원, 0 이상 정수) |
| | `commitment_discount_rate`, `min_commitment_months` | 약정 할인율(0~100 %), 할인 적용 최소 약정 개월 수 |
| | `early_termination_fee` | 약정 중 해지·하향 변경 시 위약금 기준액 |
| | `available_from`, `available_to` | 판매 기간 |
| | `status` | `ON_SALE` / `SUSPENDED` / `RETIRED` |
| Subscription | `subscription_id`, `customer_id`, `plan_id` | 가입 계약 식별자, 계약 고객, 현재 요금제 |
| | `line_number` | 회선 번호 |
| | `commitment_months`, `commitment_start`, `commitment_end` | 약정 기간 |
| | `status` | `PENDING` / `ACTIVE` / `SUSPENDED` / `TERMINATED` |
| | `version` | 변경될 때마다 1씩 오르는 번호 |
| SubscriptionChange | `change_id`, `subscription_id` | 변경 이력 식별자, 대상 계약 |
| | `change_type` | `PLAN_CHANGE` / `STATUS_CHANGE` |
| | `from_plan_id`, `to_plan_id`, `from_status`, `to_status` | 변경 전후 값 |
| | `requested_by`, `effective_date`, `version_after`, `created_at` | 요청 계정(또는 `SYSTEM`), 적용일, 변경 후 `version`, 기록 시각 |
| Invoice | `invoice_id`, `customer_id`, `subscription_id` | 청구서 식별자, 청구 대상 고객·계약 |
| | `billing_period` | 청구 월(`YYYY-MM`) |
| | `base_amount`, `discount_amount`, `adjustment_amount`, `tax_amount` | 기본료, 할인, 정정 조정액, 부가세 |
| | `total_amount` | 청구 총액 = 기본료 − 할인 + 조정액 + 부가세, 0 이상 |
| | `status` | `DRAFT` / `ISSUED` / `CORRECTED` / `CONFIRMED` |
| | `payment_status` | `UNPAID` / `PAID` / `FAILED` |
| | `version`, `correction_reason` | 정정마다 1씩 오르는 번호(이전 버전 보존), 정정 사유(정정 시 필수) |
| | `issued_by`, `confirmed_by` | 발행·확정한 계정 |
| | `issued_at`, `due_date`, `confirmed_at` | 발행 시각, 납부 기한, 확정 시각 |
| PaymentMethod | `payment_method_id`, `customer_id` | 결제수단 식별자, 소유 고객 |
| | `method_type`, `holder_name` | `CARD` / `BANK_ACCOUNT`, 명의자 이름 |
| | `card_last4` | 카드번호 끝 4자리(카드일 때) |
| | `bank_code`, `bank_account_no` | 은행 코드, 계좌번호(계좌일 때) |
| | `gateway_token_ref` | 외부 결제 대행이 발급한 결제수단 참조값 |
| | `auto_debit`, `status`, `registered_at` | 자동이체 사용 여부, `PENDING` / `ACTIVE` / `REMOVED`, 등록 완료 시각 |
| ConsultationRecord | `record_id`, `customer_id` | 상담 이력 식별자, 상담 고객 |
| | `consultant_id` | 작성 상담원의 `account_id` |
| | `channel` | `PHONE` / `CHAT` / `VISIT` |
| | `customer_note` | 고객에게 공개하는 상담 요약 |
| | `internal_memo` | 직원 전용 내부 메모 |
| | `created_at`, `updated_at` | 작성·수정 시각 |
| BatchJob | `job_id`, `job_type` | 작업 식별자, `PLAN_IMPORT` / `CUSTOMER_IMPORT` / `MONTHLY_BILLING` / `AUTO_DEBIT` |
| | `requested_by` | 요청 계정. 정기 처리는 `SYSTEM` |
| | `original_filename`, `file_size`, `content_type` | 업로드 파일 정보(일괄 등록일 때) |
| | `stored_file_ref` | 서버가 정한 저장 위치 식별자 |
| | `row_count`, `success_count`, `error_count` | 처리 건수 |
| | `status` | `UPLOADED` / `RUNNING` / `COMPLETED` / `FAILED` / `CANCELLED` |
| | `created_at`, `finished_at` | 생성·종료 시각 |
| AuditEvent | `event_id`, `actor_id` | 기록 식별자, 행위자(`account_id` 또는 `SYSTEM` / `PAYMENT_GATEWAY`) |
| | `action`, `target_type`, `target_id`, `customer_id` | 행위, 대상 엔티티·식별자, 관련 고객 |
| | `occurred_at`, `result` | 발생 시각, `SUCCESS` / `DENIED` / `FAILED` |
| IdentityVerification | `verification_id`, `account_id` | 본인 확인 요청 식별자, 대상 가입 신청 계정 |
| | `agency_result_ref` | 외부 본인 확인 기관이 발급한 결과 참조값. 완료 처리 때 기록 |
| | `status` | `REQUESTED` / `COMPLETED` / `FAILED` / `EXPIRED` |
| | `expires_at`, `completed_at`, `created_at` | 완료 가능 기한, 완료 시각, 생성 시각 |

### 1.3 관계

| 관계 | 다중성 |
|---|---|
| Customer — Account(`CUSTOMER`) | 1 : 1 |
| Customer — Subscription | 1 : N |
| Plan — Subscription | 1 : N |
| Subscription — SubscriptionChange | 1 : N |
| Subscription — Invoice | 1 : N (계약·청구 월마다 1건) |
| Customer — PaymentMethod | 1 : N (자동이체 사용 결제수단은 고객당 최대 1개) |
| Customer — ConsultationRecord | 1 : N |
| Account(`CONSULTANT`) — Customer | 1 : N (`assigned_consultant_id`) |
| Account(`CONSULTANT`) — ConsultationRecord | 1 : N (`consultant_id`) |
| Account — BatchJob | 1 : N (`requested_by`) |
| Account — IdentityVerification | 1 : N (결과 하나는 요청 하나에만 쓴다) |

## 2. 고객정보 필드 등급

| 등급 | 이름 | 해당 정보 |
|---|---|---|
| G1 | 고유식별정보 | 주민등록번호 |
| G2 | 금융정보 | 카드번호, 계좌번호, 결제 대행 참조값 |
| G3 | 인증정보 | 비밀번호 |
| G4 | 일반 개인정보 | 이름, 생년월일, 연락처, 이메일, 주소, 회선 번호, 고객 공개 상담 내용 |
| G5 | 내부 업무정보 | 내부 메모, 신용 등급, 수납 상태, 배정 상담원 |

응답 포함 여부: 원문 / 마스킹 / 미포함. 상담원은 배정 고객 기준이다.

| 필드 | 등급 | 저장 원칙 | 표시 형태 | 본인(고객) | 상담원 | 청구 담당 | 관리자 |
|---|---|---|---|---|---|---|---|
| `resident_reg_no` | G1 | 암호화 저장 | `900101-1******` | 마스킹 | 미포함 | 미포함 | 미포함 |
| 카드번호 원문 | G2 | 저장하지 않음(결제 대행 보관) | — | 미포함 | 미포함 | 미포함 | 미포함 |
| `card_last4` | G2 | 일반 | `****-****-****-1234` | 마스킹 | 마스킹 | 마스킹 | 마스킹 |
| `bank_account_no` | G2 | 암호화 저장 | `*******5678` | 마스킹 | 마스킹 | 마스킹 | 마스킹 |
| `gateway_token_ref` | G2 | 암호화 저장 | — | 미포함 | 미포함 | 미포함 | 미포함 |
| `password_hash` | G3 | 복원 불가 형태 | — | 미포함 | 미포함 | 미포함 | 미포함 |
| `name` | G4 | 일반 | `홍*동` | 원문 | 마스킹 | 마스킹 | 원문 |
| `birth_date` | G4 | 일반 | `1990-**-**` | 원문 | 마스킹 | 미포함 | 마스킹 |
| `phone` | G4 | 일반 | `010-1234-****` | 원문 | 마스킹 | 마스킹 | 마스킹 |
| `email` | G4 | 일반 | `ho***@example.com` | 원문 | 마스킹 | 미포함 | 마스킹 |
| `address` | G4 | 일반 | `서울특별시 중구 ***` | 원문 | 마스킹 | 마스킹 | 마스킹 |
| `line_number` | G4 | 일반 | `010-1234-****` | 원문 | 마스킹 | 마스킹 | 마스킹 |
| `customer_note` | G4 | 일반 | 원문 | 원문 | 원문 | 미포함 | 원문 |
| `internal_memo` | G5 | 일반 | 원문 | 미포함 | 원문 | 미포함 | 원문 |
| `credit_status` | G5 | 일반 | 원문 | 미포함 | 원문 | 원문 | 원문 |
| `collection_status` | G5 | 일반 | 원문 | 미포함 | 원문 | 원문 | 원문 |
| `assigned_consultant_id` | G5 | 일반 | 원문 | 미포함 | 미포함 | 미포함 | 원문 |

- G4 원문은 위 표에서 원문으로 정한 역할의 응답에만 담는다.
- 목록 응답에는 `customer_id`, `name`, `phone`, 계정 `status`만 담고, 각 필드는 위 표의 역할별 형태를 따른다. 관리자 회원 목록은 `account_id`, `role`을 더 담는다(F3).
- 직원 계정의 `staff_name`은 고객정보가 아니며 관리자 회원 목록·상세에만 담는다.

### 2.1 짧은 값·다른 입력 형식의 마스킹 형태

위 표의 형태가 그대로 맞지 않는 값은 아래처럼 표시한다. 어떤 경우에도 원문 전체를 그대로 보이지 않는다.

| 필드 | 경우 | 표시 형태 | 예 |
|---|---|---|---|
| `name` | 2자 | 첫 글자 + `*` | `홍길` → `홍*` |
| `name` | 4자 이상 | 첫 글자와 마지막 글자만 표시, 나머지는 `*` | `남궁민수` → `남**수` |
| `email` | `@` 앞 아이디가 2자 이하 | 아이디 첫 글자 + `*` + 도메인 | `ab@example.com` → `a*@example.com` |
| `phone`, `line_number` | 하이픈 없이 입력된 11자리 | 하이픈을 넣어 뒤 4자리를 가림 | `01012345678` → `010-1234-****` |
| `phone`, `line_number` | 하이픈 없이 입력된 10자리 | 하이픈을 넣어 뒤 4자리를 가림 | `0111234567` → `011-123-****` |

## 3. 상태 전이

### 3.1 계정(Account.status)

| 현재 | 다음 | 일으키는 주체 | 조건 |
|---|---|---|---|
| (없음) | `PENDING_VERIFICATION` | 고객 | 회원 가입 신청(F1) |
| `PENDING_VERIFICATION` | `ACTIVE` | 시스템 | 본인 확인 결과를 서버가 확인함 |
| `ACTIVE` | `LOCKED` | 시스템(로그인 처리) | 연속 로그인 실패가 정책 횟수에 이름. 그 실패한 로그인 요청을 처리할 때 바뀐다 |
| `LOCKED` | `ACTIVE` | 일괄 처리, 관리자 | 잠금 시간 경과 또는 관리자 해제 |
| `ACTIVE`, `LOCKED` | `SUSPENDED` | 관리자 | 정지 사유 기록 |
| `SUSPENDED` | `ACTIVE` | 관리자 | 정지 해제 사유 기록 |
| `ACTIVE`, `LOCKED`, `SUSPENDED` | `DELETED` | 관리자 | 고객 탈퇴 요청 또는 관리자 결정. 진행 중 계약은 `TERMINATED` 이후 |

`DELETED`는 종료 상태다. 관리자는 본인 계정의 상태를 바꿀 수 없다.

### 3.2 가입 계약(Subscription.status)

| 현재 | 다음 | 일으키는 주체 | 조건 |
|---|---|---|---|
| (없음) | `PENDING` | 고객, 상담원(배정 고객), 일괄 처리 | 가입 신청 |
| `PENDING` | `ACTIVE` | 관리자, 일괄 처리 | 개통 완료(추정) |
| `ACTIVE` | `ACTIVE` | 고객, 상담원(배정 고객) | 요금제 변경(F4). `version` 증가, 변경 이력 기록 |
| `ACTIVE` | `SUSPENDED` | 관리자 | 이용 정지 결정 |
| `SUSPENDED` | `ACTIVE` | 관리자 | 정지 해제 |
| `ACTIVE`, `SUSPENDED` | `TERMINATED` | 관리자 | 해지 확정. 약정 중이면 위약금 청구 대상 |

- 요금제 변경 대상은 변경일에 `ON_SALE`이고 판매 기간 안인 요금제뿐이다.
- 요청자가 본 `version`과 현재 `version`이 다르면 변경하지 않는다.
- `TERMINATED`는 종료 상태다.

### 3.3 청구서(Invoice.status)

| 현재 | 다음 | 일으키는 주체 | 조건 |
|---|---|---|---|
| (없음) | `DRAFT` | 일괄 처리 | 월 청구서 생성 |
| `DRAFT` | `ISSUED` | 청구 담당 | 발행 |
| `ISSUED`, `CORRECTED` | `CORRECTED` | 청구 담당 | 정정 사유 필수, `version` 증가, 이전 버전 보존 |
| `ISSUED`, `CORRECTED` | `CONFIRMED` | 청구 담당 | 확정 |

- `CONFIRMED`는 종료 상태다. 금액·상태를 누구도 바꿀 수 없다.
- `payment_status`의 `PAID`·`FAILED`는 외부 결제 대행의 확인된 통지로만 바뀐다.
- 고객은 `ISSUED` 이후 청구서만 조회한다. `DRAFT`는 고객에게 보이지 않는다.

## 4. 소유 관계

| 엔티티 | 고객의 "본인 소유" | 상담원의 접근 범위 | 기타 |
|---|---|---|---|
| Customer | 로그인 계정의 `customer_id`와 같은 고객 | `assigned_consultant_id`가 본인 `account_id`인 고객 | 청구 담당은 청구 대상 고객만 |
| Account | 본인 `account_id` | 없음 | 관리자는 본인 외 계정 |
| Subscription | `customer_id`가 본인 | 배정 고객의 계약 | |
| Invoice | `customer_id`가 본인 | 배정 고객의 청구서(조회만) | 청구 담당은 전체 |
| PaymentMethod | `customer_id`가 본인 | 배정 고객 것 조회(마스킹)만 | 청구 담당은 조회(마스킹)만. 결제 대행 통지는 등록 요청한 결제수단에만 반영 |
| ConsultationRecord | `customer_id`가 본인(`customer_note`만) | 배정 고객 기록 조회, 본인이 `consultant_id`인 기록만 수정 | |
| BatchJob | 해당 없음 | 해당 없음 | 요청 관리자 본인 작업. 결과는 요청자 권한 범위 안에서만 반영 |

- 소유 판단에 쓰는 `customer_id`와 `account_id`는 서버가 확인한 로그인 신원에서 얻는다.
- 하위 엔티티의 소유는 상위 연결을 따라 정한다. 예: 청구서 → 가입 계약 → 고객.
- 배정이 바뀌면 이전 상담원은 그 고객에 접근할 수 없다. 이미 작성한 상담 이력의 `consultant_id`는 유지한다.
