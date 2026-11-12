# F3 관리자 회원 관리 기능 설계

## 1. 목적과 범위

관리자가 회원 계정을 조회하고, 계정을 정지·정지 해제·잠금 해제·삭제하며, 직원 계정의 역할을 바꾸고, 고객에게 상담원을 배정한다.

대상: 회원 목록 조회, 회원 상세 조회, 회원 상세 화면, 정지, 정지 해제, 잠금 해제, 삭제, 역할 변경, 상담원 배정.
범위 밖: 직원 계정 생성, 고객 탈퇴 요청 접수 화면, 가입 계약 해지(계약 관리), 감사 기록 조회 화면.

엔티티·필드·상태 이름은 `spec/data-model.md`를 따른다.

## 2. 참여자와 역할

| 역할 | 할 수 있다 | 할 수 없다 |
|---|---|---|
| `ADMIN` | 이 문서의 모든 API | 본인 계정의 정지·정지 해제·잠금 해제·삭제·역할 변경, 응답 미포함 정보 열람, 마스킹 해제 |
| `CUSTOMER`, `CONSULTANT`, `BILLING_OPERATOR` | 없음 | 이 문서의 모든 API |
| `SYSTEM`(일괄 처리) | 잠금 시간 경과 계정의 `LOCKED` → `ACTIVE` | 정지, 삭제, 역할 변경 |

- 관리자 여부는 서버에 저장된 로그인 계정의 `role`로만 판단한다.
- "회원"은 고객 계정과 직원 계정을 모두 포함한다. 직원 계정은 `customer_id`가 비어 있다.

## 3. 주요 자산

| 자산 | 비고 |
|---|---|
| `Account.status` | 전이는 `data-model.md` 3.1만 따른다 |
| `Account.role` | 직원 역할은 관리자만 부여·회수한다 |
| `Customer.assigned_consultant_id` | 상담원의 고객 접근 범위를 정한다 |
| 고객 G1·G4·G5 | 관리자에게는 `data-model.md` 2장의 관리자 열 형태로만 보인다 |
| `password_hash`, 결제 대행 참조값, `bank_account_no` 원문 | 어떤 응답에도 담지 않는다 |
| `AuditEvent` | 모든 관리 행위의 기록 |
| 대상 계정의 로그인 상태 | 정지·삭제·역할 변경 후 효력을 잃는다 |

## 4. API

공통:

- 호출: `ADMIN`만. 다른 역할과 로그인하지 않은 요청은 거부한다.
- 행위자는 로그인 계정이다. 요청에 담긴 행위자 식별자·역할 값은 쓰지 않는다.
- 상태·역할·배정을 바꾸는 API는 성공·거부 모두 `AuditEvent`를 남긴다.
- 대상이 없으면 거부하고, 응답에 다른 회원 정보를 담지 않는다.

### 4.1 회원 목록 조회

`GET /api/v1/admin/members`

- 입력(파라미터): `status`, `role`, `name`(이름 부분 일치 검색어), `page`, `size`, `sort`.
- `sort`는 `created_at`, `name`, `status` 중 하나와 방향(`asc`, `desc`)만 받는다. 그 밖의 값은 거부한다.
- `status`, `role`은 `data-model.md`에 정의된 값만 받는다.
- 응답 항목: `account_id`, `role`, `customer_id`, `name`, 마스킹된 `phone`, 계정 `status`. 직원 계정은 `name`에 `staff_name`을 담고 `customer_id`, `phone`은 비어 있다.
- 하면 안 되는 일: 목록 항목에 위 필드 외 값을 담는 것.

### 4.2 회원 상세 조회

`GET /api/v1/admin/members/{account_id}`

- 응답:
  - 계정: `account_id`, `login_id`, `role`, `status`, `failed_login_count`, `identity_verified_at`, `last_login_at`, `staff_name`(직원 계정).
  - 고객 계정이면 고객: `customer_id`, `name`(원문), `birth_date`·`phone`·`email`·`address`(마스킹), `credit_status`, `collection_status`, `assigned_consultant_id`, `created_at`.
- 해야 할 일: 고객 계정 조회는 감사 기록을 남긴다.
- 하면 안 되는 일: `password_hash`, `ci`, `name` 외 G4 원문을 담는 것. `DELETED` 계정의 G1·G2·G3 값을 담는 것.

### 4.3 회원 정지

`POST /api/v1/admin/members/{account_id}/suspend`

- 입력: `reason`(필수).
- 해야 할 일: 대상이 `ACTIVE` 또는 `LOCKED`일 때 `SUSPENDED`로 바꾼다. 대상 계정의 기존 로그인 상태는 다음 요청부터 효력을 잃는다.
- 하면 안 되는 일: 본인 계정 정지. 다른 상태 계정 변경.

### 4.3-1 회원 일괄 정지

`POST /api/v1/admin/members/bulk-suspend`

- 입력: `account_ids`(1~50개, 각 항목은 영문·숫자·`_`·`-` 64자 이하), `reason`(필수).
- 해야 할 일: 목록에 있는 계정 중 `ACTIVE` 또는 `LOCKED`인 계정만 `SUSPENDED`로 바꾼다. 중복 항목은 한 번만 처리한다. 응답은 정지된 `suspended`와 바꾸지 않은 `skipped`(없는 계정, 정지할 수 없는 상태)의 `account_id` 목록이다. 계정별로 감사 기록을 남긴다.
- 하면 안 되는 일: 목록에 없는 계정의 상태를 바꾸는 것. `ACTIVE`·`LOCKED` 아닌 계정(`DELETED`, `PENDING_VERIFICATION`, 이미 `SUSPENDED`)의 변경. 목록에 본인 계정이 있는 요청 처리(전체를 거부한다).

### 4.4 회원 정지 해제

`POST /api/v1/admin/members/{account_id}/unsuspend`

- 입력: `reason`(필수).
- 해야 할 일: 대상이 `SUSPENDED`일 때만 `ACTIVE`로 바꾼다.
- 하면 안 되는 일: 본인 계정 변경. `DELETED`, `PENDING_VERIFICATION`, `LOCKED`, `ACTIVE` 계정 변경.

### 4.5 회원 잠금 해제

`POST /api/v1/admin/members/{account_id}/unlock`

- 해야 할 일: 대상이 `LOCKED`일 때만 `ACTIVE`로 바꾸고 `failed_login_count`를 0으로 한다.
- 하면 안 되는 일: 본인 계정 변경. 다른 상태 계정 변경.

### 4.6 회원 삭제

`DELETE /api/v1/admin/members/{account_id}`

- 입력(파라미터): `reason_code`(`CUSTOMER_REQUEST` 또는 `ADMIN_DECISION`, 필수).
- 해야 할 일: 대상이 `ACTIVE`, `LOCKED`, `SUSPENDED`이고, 고객 계정이면 그 고객의 모든 `Subscription`이 `TERMINATED`일 때만 `DELETED`로 바꾼다. 이후 그 고객의 G1·G2·G3은 조회되지 않는다. 대상이 상담원 계정이면 그 상담원의 배정은 모두 해제된다. 대상 계정의 기존 로그인 상태는 다음 요청부터 효력을 잃는다.
- 하면 안 되는 일: 본인 계정 삭제. 진행 중 계약이 있는 고객 삭제. `DELETED` 계정 재처리.

### 4.7 역할 변경

`PUT /api/v1/admin/members/{account_id}/role`

- 입력: `role`(`CONSULTANT`, `BILLING_OPERATOR`, `ADMIN` 중 하나), `reason`(필수).
- 해야 할 일: 대상이 직원 계정(`customer_id` 없음)이고 `DELETED`가 아닐 때만 역할을 바꾼다. 대상 계정의 기존 로그인 상태는 다음 요청부터 효력을 잃는다. `CONSULTANT`에서 다른 역할로 바뀌면 그 상담원의 배정은 모두 해제된다.
- 하면 안 되는 일: 본인 역할 변경. 고객 계정의 역할 변경. 직원 계정을 `CUSTOMER`로 바꾸는 것.

### 4.8 상담원 배정

`PUT /api/v1/admin/customers/{customer_id}/assigned-consultant`

- 입력: `consultant_account_id`(배정 해제는 빈 값).
- 해야 할 일: 대상 상담원이 `role`이 `CONSULTANT`이고 `ACTIVE`인 계정일 때만 `assigned_consultant_id`를 바꾼다. 이전 상담원은 다음 요청부터 그 고객에 접근할 수 없다.
- 하면 안 되는 일: 상담원이 아닌 계정을 배정하는 것. 한 고객에 상담원을 둘 이상 두는 것. `DELETED` 고객 배정.

### 4.9 회원 상세 화면

`GET /api/v1/admin/members/{account_id}/view`

- 관리자가 브라우저에서 보는 회원 상세 화면이다. 응답은 HTML 문서다.
- 표시 항목과 형태는 4.2 회원 상세 조회와 같다(`name` 원문, 그 밖의 G4는 마스킹).
- 해야 할 일: 고객이 입력한 값(`name`, `address` 등)은 입력한 글자 그대로 보이게 표시한다. 고객 계정 조회는 감사 기록을 남긴다.
- 하면 안 되는 일: 고객이 입력한 값 때문에 화면의 동작이나 다른 표시 내용이 바뀌는 것. 4.2에서 담지 않는 정보를 화면에 보이는 것.

### 4.10 감사 기록 조회

`GET /api/v1/admin/audit-events`

- 입력: `actor_id`(행위자, 선택), `action`(동작, 선택), `from`·`to`(기간, `YYYY-MM-DD`, 선택, 양 끝 날짜 포함, 운영 정책의 시간대(기본 KST) 기준 날짜), `sort`(`occurred_at`·`action`·`result` 중 하나와 `asc`·`desc`, 기본 `occurred_at,desc`), `page`, `size`.
- 해야 할 일: 조건에 맞는 감사 기록 항목을 돌려준다. 이 조회도 감사 기록에 남긴다. 날짜 형식이 틀리거나 `from`이 `to`보다 늦으면 거부한다.
- 하면 안 되는 일: 항목에 `event_id`, `actor_id`, `action`, `target_type`, `target_id`, `customer_id`, `result`, `occurred_at` 밖의 값(자유 입력 `reason` 포함)을 담는 것. 기간·행위자·동작 값이 다른 조건이 되는 것.
- 저장하는 값: 감사 기록은 행위자, 일시, 처리한 정보주체(`target_id`·`customer_id`), 수행 업무(`action`) 외에 **접속지 정보(요청을 보낸 쪽의 IP 주소)**도 함께 남긴다(「개인정보의 안전성 확보조치 기준」 제2조 제3호 접속기록 항목). IP는 요청 헤더가 아니라 연결 주소를 쓰고, 조회 응답에는 담지 않는다.

### 4.11 회원 목록 내보내기

`GET /api/v1/admin/exports/members`

- 입력: `status`, `role`(회원 목록 조회와 같은 필터, 선택).
- 해야 할 일: 조건에 맞는 회원(최대 1000건, 가입 최근순, `DELETED` 계정 제외)을 CSV 파일로 내려 준다. 열은 `account_id`, `role`, `customer_id`, `name`(원문), `phone`·`email`(마스킹), `status`뿐이다. 서버는 CSV를 임시 파일에 만든 뒤 응답으로 내리고 임시 파일을 지운다. 내보내기는 감사 기록에 남긴다. 운영 기록(로그)에는 행위자와 건수만 남긴다.
- 하면 안 되는 일: `phone`·`email` 원문을 CSV나 임시 파일, 운영 기록에 담는 것. 응답 뒤에 임시 파일을 남기는 것. 4.2에서 담지 않는 정보(`ci`, `birth_date`, `address`, `password_hash`)를 담는 것.

## 5. 데이터 처리 흐름

상태 변경(정지·정지 해제·잠금 해제·삭제):

1. 관리자가 대상 계정과 사유를 보낸다.
2. 서버는 로그인 계정의 저장된 `role`이 `ADMIN`인지 확인한다.
3. 서버는 대상이 관리자 본인이 아닌지 확인한다.
4. 서버는 대상의 현재 상태가 요청한 전이의 시작 상태인지 확인한다(삭제는 계약 상태도 확인).
5. 서버는 상태를 바꾸고 감사 기록을 남긴다.
6. 대상 계정의 다음 요청부터 바뀐 상태로 판단한다.

일괄 정지:

1. 관리자가 계정 목록과 사유를 보낸다.
2. 서버는 관리자 여부를 확인하고, 목록에 본인 계정이 있으면 전체를 거부한다.
3. 서버는 목록 항목의 형식과 개수를 확인한다.
4. 서버는 목록 항목을 값으로 분리해 대상 계정을 조회하고, 현재 상태가 `ACTIVE`·`LOCKED`인 계정만 고른다.
5. 서버는 고른 계정만 `SUSPENDED`로 바꾸고(변경 시점에도 상태 조건을 건다) 계정별 감사 기록을 남긴다.

내보내기:

1. 관리자가 필터를 보낸다.
2. 서버는 관리자 여부를 확인한다.
3. 서버는 회원을 조회해 마스킹한 행으로 CSV를 만들고, 임시 파일에 쓴 뒤 읽어 응답으로 내린다.
4. 서버는 요청이 끝나면 임시 파일을 지우고, 감사 기록에 남긴다. 운영 기록에는 행위자와 건수만 남긴다.

감사 기록 조회:

1. 관리자가 조건(행위자, 동작, 기간, 정렬, 페이지)을 보낸다.
2. 서버는 관리자 여부를 확인한다.
3. 서버는 기간 날짜 형식과 정렬 값을 확인하고, 조건을 값으로 분리해 조회한다.
4. 서버는 허용된 항목 필드만 응답에 담고, 이 조회를 감사 기록에 남긴다.

역할 변경·상담원 배정:

1. 관리자가 대상과 새 값을 보낸다.
2. 서버는 관리자 여부와 본인 대상 여부를 확인한다.
3. 서버는 대상 계정 종류와 새 값이 허용 범위인지 확인한다.
4. 서버는 값을 바꾸고 감사 기록을 남긴다.
5. 영향을 받는 계정(대상 직원, 이전 상담원)의 다음 요청부터 바뀐 값으로 판단한다.

## 6. 업무 불변조건

1. 이 문서의 모든 API는 `ADMIN`만 호출할 수 있다. 같은 회원 자원의 조회·정지·정지 해제·잠금 해제·삭제·역할 변경에 같은 기준을 적용한다.
2. 관리자 여부는 서버에 저장된 로그인 계정의 `role`로만 판단한다. 요청에 담긴 역할·행위자 값은 쓰지 않는다.
3. 관리자는 본인 계정을 정지·정지 해제·잠금 해제·삭제·역할 변경할 수 없다.
4. 정지는 `ACTIVE`·`LOCKED` → `SUSPENDED`, 정지 해제는 `SUSPENDED` → `ACTIVE`, 잠금 해제는 `LOCKED` → `ACTIVE`, 삭제는 `ACTIVE`·`LOCKED`·`SUSPENDED` → `DELETED`만 허용한다.
5. `DELETED` 계정은 어떤 API로도 다른 상태가 되지 않는다.
6. 고객 계정 삭제는 그 고객의 모든 `Subscription`이 `TERMINATED`일 때만 한다.
7. 정지·정지 해제·역할 변경은 사유가 있어야 하고, 삭제는 `reason_code`가 있어야 한다.
8. 역할 변경은 직원 계정만 대상으로 하고, 값은 `CONSULTANT`, `BILLING_OPERATOR`, `ADMIN`뿐이다. 고객 계정의 역할은 바뀌지 않고, 직원 계정은 `CUSTOMER`가 되지 않는다.
9. 계정이 정지·삭제되거나 역할이 바뀌면, 그 계정의 기존 로그인 상태는 다음 요청부터 바뀐 값으로 판단되거나 효력을 잃는다.
10. 목록 응답 항목은 `account_id`, `role`, `customer_id`, `name`, 마스킹된 `phone`, 계정 `status`뿐이다.
11. 상세 응답은 `birth_date`, `phone`, `email`, `address`를 마스킹 형태로만 담고, `ci`와 `password_hash`를 담지 않는다. `DELETED` 계정의 G1·G2·G3은 담지 않는다.
12. 목록의 `sort`는 정해진 값만 받고, `name` 검색어는 이름 부분 일치로만 해석한다. 검색어나 정렬 값이 다른 조건·다른 회원 정보 반환·데이터 변경으로 이어지지 않는다.
13. 상담원 배정 대상은 `ACTIVE` 상태의 `CONSULTANT` 계정뿐이고, 고객마다 최대 한 명이다.
14. 배정이 바뀌면 이전 상담원은 다음 요청부터 그 고객을 조회·수정할 수 없다.
15. 상세 조회(화면 포함), 상태·역할·배정 변경은 행위자·시각·대상·결과(`SUCCESS`/`DENIED`/`FAILED`)를 감사 기록에 남긴다.
16. 고객이 입력한 값(`name`, `address` 등)은 관리자 화면에 입력된 글자 그대로 표시되고, 화면의 동작이나 다른 표시 내용을 바꾸지 않는다.
17. 거부 응답에는 대상이나 다른 회원의 정보를 담지 않는다.
18. 일괄 정지는 요청 목록에 있는 계정만 바꾸고, 목록 항목은 계정 식별자 값으로만 해석한다. 항목 값이 다른 조건·다른 계정의 변경으로 이어지지 않는다.
19. 일괄 정지도 항목마다 4번 상태 전이 규칙(정지는 `ACTIVE`·`LOCKED`에서만)을 적용한다. `DELETED`는 되살아나지 않고, `PENDING_VERIFICATION` 계정은 정지 해제로 본인확인을 건너뛸 수 없다.
20. 일괄 정지 목록에 본인 계정이 한 개라도 있으면(위치와 무관하게) 요청 전체를 거부하고 어떤 계정도 바꾸지 않는다.
21. 감사 기록 조회(4.10)는 `ADMIN`만 호출할 수 있고, 같은 방식으로 판단한다. 이 조회는 행위자·시각·결과를 감사 기록에 남긴다.
22. 감사 기록 조회의 기간·행위자·동작 값은 값으로만 해석하고, 정렬은 정해진 값만 받는다. 기간 값은 `YYYY-MM-DD` 형식이 아니면 거부하고 `from`이 `to`보다 늦어도 거부한다. 기간은 운영 정책의 시간대(`spec/domain.md` 4장, 기본 KST)의 날짜로 해석하며 양 끝 날짜의 하루 전체를 포함한다. 기간 밖 기록이 결과에 들어가지 않는다.
23. 감사 기록 조회 응답 항목은 `event_id`, `actor_id`, `action`, `target_type`, `target_id`, `customer_id`, `result`, `occurred_at`뿐이다. 자유 입력 사유(`reason`)는 개인정보가 들어 있을 수 있어 담지 않는다.
24. 회원 목록 내보내기(4.11)는 `ADMIN`만 호출할 수 있고, 같은 회원 자원의 다른 API와 같은 기준으로 판단한다. 내보내기는 행위자·시각·결과를 감사 기록에 남긴다.
25. 내보내기 CSV의 열은 `account_id`, `role`, `customer_id`, `name`, `phone`, `email`, `status`뿐이고 `phone`·`email`은 마스킹 형태다. `DELETED` 계정은 담지 않는다.
26. 내보내기가 만드는 임시 파일은 어떤 요청 경로(필터 유무)에서도 응답이 끝난 뒤 남지 않고, 그 내용에는 `phone`·`email` 원문이 없다.
27. 운영 기록(로그)에는 `phone`·`email` 원문을 남기지 않는다. 내보내기 로그는 행위자와 건수만 담는다.
