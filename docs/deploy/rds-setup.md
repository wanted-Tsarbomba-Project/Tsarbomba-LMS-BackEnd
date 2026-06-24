# RDS(MySQL) 구축 방법

CodeBomb LMS의 공용 데이터베이스(RDS MySQL) 생성·접속 절차.
**② Spring + ④ 챗봇 + 로컬(스키마/마이그레이션)**이 함께 쓰는 단일 MySQL이다.

> 모니터링(③) 스택 배포는 `../../monitoring-deploy/README.md` 참고.

---

## 0. 설계 결정

- **엔진**: MySQL 8.0 (스택이 MySQL/JPA)
- **공유 대상**: ② Spring, ④ 챗봇, 로컬 개발자
- **퍼블릭 액세스 = 예 (현 단계)**: 로컬에서 직접 스키마·마이그레이션 작업이 필요해서.
  대신 **SG로 접속 소스를 제한**해 방어. 운영 진입 시 사설(Private)로 조이는 걸 권장.
- **가용영역(AZ)**: **`ap-northeast-2c`로 고정** — 전 인스턴스(①②③④)와 RDS를 한 AZ에 몰아 AZ간 지연·전송요금 제거.
  > ②↔RDS는 모든 요청의 critical path라 같은 AZ가 측정 정확도에 중요. EC2/RDS의 AZ는 생성 시 고정이라, 데이터 없을 때 옮기는 게 싸다.

---

## 1. 보안그룹 (`codebomb-rds`)

EC2 → 보안그룹 → 생성. 인바운드 규칙:

| 포트 | 소스 | 비고 |
|---|---|---|
| 3306 | 내 IP | 로컬 접속 (네트워크 바뀌면 갱신) |
| 3306 | (나중) `codebomb-spring` SG | ② 생성 시 추가 |
| 3306 | (나중) `codebomb-chatbot` SG | ④ 생성 시 추가 |

- 아웃바운드: 기본(전체 허용) 유지.
- ⚠️ **인바운드를 `0.0.0.0/0`로 열지 말 것** — DB를 인터넷 전체에 노출(데이터 유출 위험). 반드시 위 특정 소스만.
- ⚠️ RDS 생성 시 "EC2 연결" 옵션을 쓰면 자동 SG(`rds-ec2-N`/`ec2-rds-N`)가 생겨 지저분해진다. **연결 안 함 + 이 SG 수동 부착**.

---

## 2. RDS 생성 (콘솔 → RDS → 데이터베이스 생성)

| 항목 | 값 |
|---|---|
| 생성 방식 | 표준 생성 |
| 엔진 | MySQL 8.0 |
| 템플릿 | **프리 티어** (자동: Single-AZ, micro, 20GB) |
| DB 인스턴스 식별자 | `codebomb-rds` |
| 마스터 사용자 | `admin` (root 금지) |
| 마스터 암호 | 강한 암호 (직접 입력·메모. **특수문자보다 영문+숫자 16자+ 권장** → 셸 꼬임 방지) |
| 인스턴스 클래스 | db.t3.micro (템플릿 자동) |
| 스토리지 | gp3 20GB, **스토리지 자동조정 끄기** |
| 가용영역 | **연결 → 추가 구성 → `ap-northeast-2c`** 지정 |
| 퍼블릭 액세스 | **예** |
| EC2 컴퓨팅 리소스 연결 | **연결 안 함** (자동 SG 생성 방지 — SG는 수동 `codebomb-rds`만) |
| VPC 보안그룹 | 기존 선택 → **codebomb-rds 만** (default·자동생성 SG 모두 제거) |
| 추가 구성 → 초기 DB 이름 | **`codebomba`** (안 넣으면 빈 DB로 생성됨) |
| 자동 백업 | 보존 1일 (또는 끔) |
| 향상된 모니터링 / Performance Insights / 로그 내보내기 | **끄기** (프리티어 초과 과금 포인트) |

생성까지 ~5분.

---

## 3. 접속 정보 (확정값)

| 키 | 값 |
|---|---|
| DB_HOST | `codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com` |
| DB_PORT | `3306` |
| DB_NAME | `codebomba` |
| DB_USERNAME | `admin` |
| DB_PASSWORD | (마스터 암호 — **레포 커밋 금지**) |

들어갈 곳:
- ④ 챗봇 EC2 `/opt/chatbot/.env`
- ② Spring `application-local.yml` 또는 Secrets
- ⚠️ 공통 설정 파일엔 `${...}` 자리만, 실제 값은 gitignore 파일/Secrets에만.

---

## 4. 연결 검증

### 4-1. 네트워크(포트) 도달 확인 — PowerShell
```powershell
Test-NetConnection -ComputerName codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com -Port 3306
```
`TcpTestSucceeded : True` → SG·퍼블릭액세스 정상. (인증과 무관한 1차 확인)

### 4-2. 로그인 — CLI (MySQL Shell)
로컬에 mysql 클라이언트가 없으면 설치(클라이언트만, 가벼움):
```powershell
winget install Oracle.MySQLShell
```
새 터미널에서:
```powershell
mysqlsh --sql -h codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com -P 3306 -u admin -p
```
접속 후:
```sql
SHOW DATABASES;   -- codebomba 보이면 OK
```

### 4-3. 로그인 — GUI (택1)
| 도구 | 설치 |
|---|---|
| IntelliJ Database 툴 | 내장 (Database 탭 → + → MySQL) |
| DBeaver | `winget install dbeaver.dbeaver` |
| MySQL Workbench | `winget install Oracle.MySQLWorkbench` |

Host/Port/User/Password에 §3 값 입력 → Test Connection.

---

## 5. 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| `Test-NetConnection` 실패 | SG 3306이 현재 내 IP 아님 → "내 IP"로 갱신 (네트워크 바뀜) / 퍼블릭액세스 "예" 확인 |
| **`1045 Access denied (using password: YES)`** | **네트워크는 OK, 인증 실패.** ① 비번 오타(특수문자 셸 꼬임) ② 마스터 사용자명이 `admin`이 아님(콘솔 **구성 탭**에서 확인) ③ 비번 분실 → RDS **수정 → 새 마스터 암호 → 즉시 적용**으로 리셋 |
| GUI에서도 1045 | 도구 문제 아님. 위 인증 원인 동일 — 사용자명/비번부터 점검 |
| `codebomba` DB 없음 | 생성 시 "초기 DB 이름" 누락 → `CREATE DATABASE codebomba;` 직접 생성 |

---

## 6. 운영 / 비용

- db.t3.micro는 12개월 프리티어(월 750h = 1대 24시간 상당).
- 안 쓸 땐 **RDS 중지** 가능(최대 7일 후 자동 재시작).
- 네트워크 바뀌면 SG 3306 소스를 현재 IP로 갱신.
- 프로젝트 종료 시: RDS 삭제(최종 스냅샷 여부 선택) + SG 정리.
