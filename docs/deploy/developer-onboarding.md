# 팀원 AWS 접속 온보딩 가이드 (SSM 방식)

> 처음 보는 팀원도 그대로 따라 하면 **AWS 콘솔 접속 + (필요 시) 로컬에서 RDS·내부포트 터널**까지 되도록 정리한 문서.
> 우리는 SSH(`.pem`)를 **폐기**했다. 모든 접속은 **SSM Session Manager**로 한다(22 포트 전부 닫힘). 배경은 [`ssm-vs-ssh.md`](ssm-vs-ssh.md), 전체 인프라 지도는 [`OVERVIEW.md`](OVERVIEW.md).

---

## 0. 시작 전 — 관리자에게 받는 것

| 받는 것 | 누가 필요 | 비고 |
|---|---|---|
| **콘솔 로그인 정보**(계정ID·사용자명·임시 비번) | 전원 | 관리자가 IAM User를 만들어 전달 |
| **액세스 키**(Access Key ID + Secret) | **로컬 CLI/터널 쓸 사람만** | 본인이 직접 못 만든다(정책상 차단) → 관리자가 발급해 1회 전달 |

- 계정 ID: **`143924590342`**
- 콘솔 로그인 URL: **`https://143924590342.signin.aws.amazon.com/console`**
- 리전: **서울 `ap-northeast-2`** (이거 아니면 아무것도 안 보임 — 1순위 함정)
- 이 문서는 사용자명이 **`cola`** 인 사람 기준으로 쓴다. **본인 사용자명으로 바꿔서** 따라 해라.

---

## 1. 큰 그림 — 나는 어디까지 해야 하나?

```
┌─ Part A. 콘솔 접속 ─────────────── 전원 필수 (키 불필요)
│   로그인 → MFA 등록 → 재로그인 → EC2 셸 접속·정보 조회·Grafana
│
└─ Part B. 로컬 CLI ─────────────── 내 PC 도구로 RDS/내부포트 볼 사람만
    도구 설치 → 액세스키 → aws configure → MFA 세션 → SSM 터널 → IntelliJ
```

**대부분은 Part A면 충분하다.** 콘솔에서 EC2 셸 들어가고(`docker logs` 등), 인스턴스 정보 보고, Grafana로 지표·로그 다 본다.

**Part B가 필요한 경우는 딱 하나** — 내 PC의 **IntelliJ/DBeaver를 RDS(3306)에 직접 붙이거나**, 내 브라우저로 **Prometheus(9090)** 를 열어야 할 때. 이건 콘솔에 기능이 없고 **로컬 포트 포워딩(터널)** 으로만 된다.

---

## Part A. 콘솔 접속 (전원 필수)

### A-1. 첫 로그인
1. `https://143924590342.signin.aws.amazon.com/console` 접속
2. **IAM 사용자**로 로그인: 사용자명 `cola` + 받은 임시 비번
3. 비번 변경하라고 나오면 새 비번 설정

### A-2. MFA 등록 ⚠️ (가장 많이 막히는 곳)
처음 로그인하면 **MFA 등록 말곤 거의 다 막혀 보인다.** 정상이다 — MFA 강제 정책이라 MFA를 켜야 권한이 열린다.

1. 우상단 **사용자명 `cola` → 보안 자격 증명(Security credentials)**
2. **멀티 팩터 인증(MFA)** → **MFA 디바이스 할당**
3. ⚠️ **디바이스 이름을 본인 사용자명과 *정확히 동일*하게** 입력 → **`cola`** (`cola-phone` 같은 거 ❌)
   - 정책이 `mfa/<사용자명>` 만 본인이 만들도록 허용한다. 이름이 다르면 **"권한이 필요함 (CreateVirtualMFADevice)"** 에러가 난다.
4. **인증 관리자 앱(Authenticator app)** 선택
5. 폰에 OTP 앱 설치(Google Authenticator / Microsoft Authenticator / Authy 아무거나) → **QR 스캔**
6. 앱에 뜨는 **연속된 6자리 코드 2개**를 차례로 입력 → **MFA 추가**

### A-3. 재로그인 + 리전 확인
1. **로그아웃 → 다시 로그인** (이번엔 비번 + MFA 코드 둘 다)
   - MFA 등록만으론 현재 세션에 MFA가 안 묻어 있다. **재로그인해야** 권한이 열린다.
2. 우상단 리전이 **"아시아 태평양(서울) ap-northeast-2"** 인지 확인. 아니면 바꿔라.
3. EC2 콘솔 → **인스턴스 4개 보이면 성공.** (안 보이면 99% 리전 문제)

### A-4. EC2 박스 셸 접속 (콘솔)
1. EC2 → 인스턴스 → 접속할 박스 체크 → 상단 **연결(Connect)**
2. **Session Manager** 탭 → **연결** → 브라우저 셸이 열린다
3. 예: spring 박스에서
   ```bash
   docker compose ps          # 컨테이너 상태
   docker logs lms-spring --tail 50
   ```

> 로그·대시보드는 **Grafana** 가 더 편하다: `http://13.124.63.188:3000` (공개 URL, 자체 로그인). 여긴 IAM 권한도 필요 없다.

**여기까지가 전원 필수.** 내 PC 도구로 RDS를 봐야 하면 Part B로.

---

## Part B. 로컬 CLI + RDS 터널 (필요한 사람만)

> 전제: **Windows / PowerShell** 기준(겪은 오류가 대부분 여기서 난다). macOS는 각 단계 끝의 📌 macOS 박스 참고.

### B-1. 도구 2개 설치
PowerShell에서:
```powershell
winget install -e --id Amazon.AWSCLI
winget install -e --id Amazon.SessionManagerPlugin
```
**새 PowerShell 창**에서 확인:
```powershell
aws --version                 # aws-cli/2.x ...
session-manager-plugin        # The Session Manager plugin was installed successfully
```
> 📌 **macOS**: AWS CLI는 [공식 pkg 설치](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html), 플러그인은 [Session Manager Plugin 설치](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html).

### B-2. 액세스 키 받기
- 본인은 정책상 **자기 액세스 키를 못 만든다**(키 난립·유출 통제 목적). **관리자에게 요청**해라.
- 관리자가 발급하면 **Secret은 그 순간 딱 한 번만** 보인다 → 안전하게 보관(비번 관리자 등). 잃어버리면 재발급뿐.

### B-3. 키 등록
```powershell
aws configure
```
- **Access Key ID**: 받은 값 (복붙, 앞뒤 공백 X)
- **Secret Access Key**: 받은 값 (끝까지 복사됐는지 확인 — 잘리면 인증 실패)
- **Default region name**: `ap-northeast-2`
- **Default output format**: `json`

### B-4. MFA 세션 토큰 받기 ⚠️
MFA가 강제라 **그냥 키만으론 호출이 거부된다.** 세션 토큰을 받아 환경변수에 심어야 권한이 열린다.

폰 OTP 앱에서 `cola`의 **새 6자리 코드**를 보고 `<OTP>` 자리에 넣어 실행:
```powershell
$creds = aws sts get-session-token --serial-number arn:aws:iam::143924590342:mfa/cola --token-code <OTP> | ConvertFrom-Json
$env:AWS_ACCESS_KEY_ID = $creds.Credentials.AccessKeyId
$env:AWS_SECRET_ACCESS_KEY = $creds.Credentials.SecretAccessKey
$env:AWS_SESSION_TOKEN = $creds.Credentials.SessionToken
```
- `mfa/cola` 의 `cola` = **A-2에서 만든 MFA 디바이스 이름**(=사용자명).
- 이 3줄은 **지금 이 PowerShell 창에만** 유효하다. 창 닫으면 사라지니 다음에 또 받으면 된다(정상). 만료 12시간.

### B-5. 동작 검증
```powershell
aws sts get-caller-identity      # "Arn": ".../user/cola" 나오면 인증 OK
aws ssm describe-instance-information --query "InstanceInformationList[].{Name:ComputerName,Id:InstanceId,Ping:PingStatus}" --output table
```
박스 4개가 `Online` 으로 표에 뜨면 → 로컬 SSM 작동 확인.

| 인스턴스 ID | 박스 |
|---|---|
| `i-07969a6c70f60dc73` | ② spring **← RDS 터널 경유지** |
| `i-0e325068e6a1be783` | ④ chatbot |
| `i-00bfa394e4fe445f0` | ① frontend |
| `i-0c66ea4abdb267e15` | ③ monitoring |

### B-6. RDS 터널 띄우기 ⚠️
RDS는 인터넷에서 직접 못 들어간다. **spring 박스를 경유**하는 터널로만 접근한다(SSH `-L`을 대체).

**(1) 터널 파라미터 JSON을 파일로 만든다** — PowerShell이 인라인 따옴표를 깨먹어서 `file://`로 빼는 게 안전하다:
```powershell
@'
{"host":["codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com"],"portNumber":["3306"],"localPortNumber":["13306"]}
'@ | Out-File -FilePath $env:USERPROFILE\rds-tunnel.json -Encoding ascii
```
> **왜 localPort 13306?** 로컬에 MySQL이 깔려 있으면 3306을 이미 점유해서 터널이 안 열린다. 충돌 피하려고 **13306**으로 받는다. (로컬 MySQL 없으면 3306 그대로 써도 된다)
> `-Encoding ascii` 필수 — PowerShell 기본 UTF-16이면 aws CLI가 JSON을 못 읽는다.

**(2) 터널 실행** (B-4 env가 살아있는 그 창에서):
```powershell
aws ssm start-session --target i-07969a6c70f60dc73 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters file://$env:USERPROFILE\rds-tunnel.json
```
성공하면 이렇게 뜨고 **창이 그대로 유지된다**(이게 정상 — 명령이 "끝나는" 게 아니라 터널이 살아있는 상태):
```
Port 13306 opened for sessionId cola-xxxx.
Waiting for connections...
```
- 이 창은 **닫지 마라**(닫으면 터널 끊김). DB 접속은 **IntelliJ/다른 창**에서.
- 끝낼 땐 `Ctrl+C`.

### B-7. IntelliJ로 접속
- **Host** `localhost` / **Port** `13306` / **User** `admin` / **Password** (RDS 마스터 암호) / **Database** `codebomba`
- **Test Connection** 성공 + `SHOW DATABASES;` 에 `codebomba` 보이면 끝.
- 접속하면 터널 창에 `Connection accepted ...` 로그가 뜬다.

---

## 트러블슈팅 — 다들 똑같이 겪는다

| 증상 | 원인 / 해결 |
|---|---|
| MFA 등록 시 **"권한이 필요함 (CreateVirtualMFADevice on mfa/cola-phone)"** | 디바이스 이름이 사용자명과 다름. **이름을 `cola`(사용자명과 정확히 동일)로** 다시. |
| 로그인했는데 EC2·S3 어디나 **AccessDenied** | MFA 안 묻은 세션. **로그아웃 → MFA 코드 넣고 재로그인.** |
| EC2 **"이 리전에는 인스턴스가 없음"** | 리전이 서울이 아님. 우상단을 **아시아 태평양(서울) ap-northeast-2** 로. |
| `get-session-token` → **InvalidClientTokenId** | 액세스 키가 무효(삭제됐거나 오타). ① 잔존 env 정리 → ② 키 재확인 → ③ 안 되면 관리자에게 **키 재발급** 요청. (정리: `Remove-Item Env:AWS_ACCESS_KEY_ID,Env:AWS_SECRET_ACCESS_KEY,Env:AWS_SESSION_TOKEN -ErrorAction SilentlyContinue`) |
| `aws configure` 했는데도 InvalidClientTokenId | Secret이 **끝까지 복사 안 됨** 또는 ID/Secret 뒤바뀜. 다시 등록(복붙 권장). |
| `describe-instance-information` → **AccessDenied** | B-4 env 3줄을 안 심었거나 **다른 창**에서 실행. 같은 창에서 세션 토큰 심고 바로. |
| 터널 명령에서 **`Invalid JSON: Expecting property name ...`** | PowerShell이 인라인 따옴표를 깨먹음. **B-6의 `file://` 파일 방식** 사용. |
| 터널이 **`Starting session`에서 멈추고 `Waiting for connections` 안 뜸** | 로컬 3306을 로컬 MySQL이 점유. `Get-NetTCPConnection -LocalPort 3306` 로 확인 → **localPort를 13306**으로(B-6). |
| 한참 뒤 호출이 다 막힘 | 세션 토큰 **12시간 만료**. B-4 다시. |

---

## 매일 쓰는 치트시트 (재접속)

PowerShell 새 창마다 MFA 세션을 다시 심어야 한다:
```powershell
# 1) MFA 세션 (새 OTP)
$creds = aws sts get-session-token --serial-number arn:aws:iam::143924590342:mfa/cola --token-code <OTP> | ConvertFrom-Json
$env:AWS_ACCESS_KEY_ID = $creds.Credentials.AccessKeyId
$env:AWS_SECRET_ACCESS_KEY = $creds.Credentials.SecretAccessKey
$env:AWS_SESSION_TOKEN = $creds.Credentials.SessionToken

# 2-a) spring 박스 셸
aws ssm start-session --target i-07969a6c70f60dc73

# 2-b) RDS 터널 (localhost:13306 → RDS:3306) — 파일은 B-6에서 한 번 만들어두면 재사용
aws ssm start-session --target i-07969a6c70f60dc73 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters file://$env:USERPROFILE\rds-tunnel.json
```

| 자주 쓰는 값 | |
|---|---|
| 계정 / 리전 | `143924590342` / `ap-northeast-2` |
| 그룹 | `codebomb-developers` |
| MFA serial | `arn:aws:iam::143924590342:mfa/<사용자명>` |
| RDS | `codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com:3306` / db `codebomba` / user `admin` |
| Grafana | `http://13.124.63.188:3000` |
