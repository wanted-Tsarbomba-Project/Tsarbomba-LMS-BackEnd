# 개발 완료 기록

> 📌 이 파일은 **끝낸 작업의 일지**예요.
> 작업(이슈)을 하나 완료할 때마다 AI에게 "완료" 또는 "WORKLOG에 기록해줘" 라고 하면 아래 양식으로 정리해줘요.
> 최신 기록이 위로 오도록 **위에 쌓아** 가세요.

---

## 예시 양식

> 아래 내용은 작성 예시이며, 실제 완료 기록은 다음 섹션부터 확인한다.

## [YYYY-MM-DD] #이슈번호 [작업 제목] ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `경로/Xxx.java` | 생성 |
| `경로/Yyy.java` | 수정 |

### 주요 작업 내용

- [구현한 기능 요약 1]
- [구현한 기능 요약 2]

### 트러블슈팅

> 발생한 문제 + 원인 + 해결방법 (있을 때만)

- **문제**: [무슨 에러가 났는지]
- **원인**: [왜 발생했는지]
- **해결**: [어떻게 고쳤는지]

### 부수 결정

> 작업 중 내린 판단·컨벤션 (있을 때만)

- [예: 에러 코드는 `도메인-번호` 형식으로 통일]

---

> ✏️ 위 양식을 복사해서 작업마다 새 블록을 **맨 위에** 추가하세요.
> 트러블슈팅·부수 결정은 없으면 생략해도 돼요.

---

## 실제 개발 완료 기록

---

## [2026-06-07] #확인필요 Course / Lecture / Enrollment / Learning API 문서 정리 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `.ai/API.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 참고 파일

| 파일 | 용도 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/README.md` | Course API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/lecture/README.md` | Lecture API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/enrollment/README.md` | Enrollment API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/learning/README.md` | Learning API/도메인 구조 확인 |

### 주요 작업 내용

- `course`, `lecture`, `enrollment`, `learning` 담당 범위의 API 목록을 Controller 기준으로 정리함
- 강좌 CRUD, 강의 CRUD, 수강신청, 학습 진행률, 문제세트 학습 흐름 API를 `.ai/API.md` 양식에 맞춰 정리함
- AI 질의 테스트를 위해 미완료/확인 필요 항목을 별도 표로 분리함
- “현재 미완료된 API”, “현재 완료된 작업”, “다음 작업순서” 질문에 답변할 수 있도록 문서 기준 정보를 정리함

### 트러블슈팅

- **문제**: 기존 `.ai/API.md`, `.ai/WORKLOG.md`가 예시 템플릿 상태라 AI가 실제 프로젝트 현황을 답변하기 어려움
- **원인**: 담당 도메인별 구현 API와 완료 작업이 운영 문서에 반영되어 있지 않음
- **해결**: 담당 범위를 `course`, `lecture`, `enrollment`, `learning`으로 좁히고 Controller/README 기준으로 API 현황과 완료 작업을 문서화함

### 부수 결정

- API 구현 여부는 Controller에 엔드포인트가 존재하는지 기준으로 판단함
- 정확한 에러 코드와 Response 상세 필드는 `확인 필요`로 남기고 추후 보완 대상으로 분리함
- 프론트 연동 여부는 코드만으로 확정하기 어려워 `확인 필요` 상태로 분리함

---

## [2026-06-05] #확인필요 도메인별 README 정리 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/lecture/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/learning/README.md` | 생성/수정 |

### 주요 작업 내용

- Course 도메인의 강좌 관리, 카테고리, 문제세트 연결 API를 정리함
- Lecture 도메인의 강의 목록/상세/생성/수정/삭제 API를 정리함
- Enrollment 도메인의 수강신청 생성, 조회, 취소 API를 정리함
- Learning 도메인의 강의 진행률, 문제세트 진행률, 관리자용 학습률 조회 API를 정리함

### 부수 결정

- 도메인 README는 AI가 프로젝트 구조를 빠르게 파악하는 참고 문서로 사용함
---

## [2026-06-08] #확인필요 Course INACTIVE 강좌 재활성화 허용 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/application/service/CourseCommandService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/domain/exception/CourseErrorCode.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/application/service/CourseServiceTest.java` | 수정 |
| `http/course.http` | 수정 |
| `.ai/API.md` | 수정 |

### 주요 작업 내용

- `DRAFT → ACTIVE` 직접 변경은 기존처럼 개설 API 사용을 강제하고, `INACTIVE → ACTIVE` 재활성화는 수정 API에서 허용함
- `CRS-003` 메시지를 작성 중인 강좌 활성화에 한정되도록 조정함
- `CourseServiceTest`에 비활성 강좌 재활성화 성공 케이스를 추가함
- `http/course.http`에 비활성 강좌 재활성화 요청 예시를 추가함

### 트러블슈팅

- **문제**: 기본 `bootRun` 실행 시 `8080` 포트 사용 중으로 애플리케이션 기동 실패
- **원인**: 이미 로컬에서 `8080` 포트를 사용하는 프로세스가 존재함
- **해결**: `--server.port=18080`으로 포트를 변경해 애플리케이션 정상 기동 및 `GET /api/v1/courses` 호출까지 확인함

### 부수 결정

- 최초 개설(`DRAFT → ACTIVE`)은 `PATCH /api/v1/courses/{courseId}/publish`를 유지하고, 운영 중지 후 재노출(`INACTIVE → ACTIVE`)은 `PUT /api/v1/courses/{courseId}` 상태 수정으로 처리함
