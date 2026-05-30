# FastAPI Python Server - 프로젝트 구조 컨벤션

> ChatBot 도메인의 AI 서버. Spring Backend와 별도 프로젝트로 운영.
> Spring 측 컨벤션은 `docs/ChatBot/convention.md` 참조.

---

## 1. 개요

| 항목 | 결정 |
|------|------|
| 프레임워크 | FastAPI |
| LLM | Google Gemini 3.5 Flash |
| Python 패키지 관리 | `pip` + `requirements.txt` |
| API 키 관리 | `.env` + `python-dotenv` (`.gitignore` 필수) |
| 포트 | `8000` (Spring `application.yml`의 `fastapi.url: http://localhost:8000`) |
| 엔드포인트 | `POST /chat` |
| 프로젝트 위치 | Spring과 **별도 Git 프로젝트** |
| 프롬프트 템플릿 | Jinja2 (`.j2` 파일) |

---

## 2. 디렉토리 구조

```
fastapi-chat/
├── app/
│   ├── main.py                  # FastAPI 앱 생성 + 라우터 등록
│   ├── api/
│   │   └── chat_router.py       # POST /chat 엔드포인트
│   ├── core/
│   │   └── config.py            # 환경변수 로딩 (Settings)
│   ├── service/
│   │   ├── prompt_builder.py    # Jinja2 시스템 프롬프트 조립
│   │   └── gemini_client.py     # Gemini API 호출
│   └── schema/
│       └── chat.py              # Request/Response Pydantic 모델
├── templates/
│   ├── system_base.j2           # 공통 규칙 (영어 — 실제 사용)
│   ├── system_problem.j2        # 문제풀이 모드 (영어)
│   ├── system_free.j2           # 자유질문 모드 (영어)
│   └── ko/                      # 한국어 참조본 (사용 안 함, 유지보수용)
│       ├── system_base.j2
│       ├── system_problem.j2
│       └── system_free.j2
├── .env                         # GEMINI_API_KEY=xxx (gitignore 대상)
├── .env.example                 # GEMINI_API_KEY=your-key-here
├── .gitignore
├── requirements.txt
└── README.md
```

---

## 3. 각 모듈 책임

### 3.1 `app/main.py` — 앱 진입점

- FastAPI 앱 인스턴스 생성
- `chat_router` 등록
- CORS, 미들웨어 등 글로벌 설정

### 3.2 `app/api/chat_router.py` — 엔드포인트

- `POST /chat` 단일 엔드포인트
- Request 파싱 → `prompt_builder` 호출 → `gemini_client` 호출 → Response 반환
- **비즈니스 로직 금지**. 조합만 담당.

### 3.3 `app/core/config.py` — 설정

- `python-dotenv`로 `.env` 로딩
- `pydantic-settings`의 `BaseSettings` 사용 권장
- 환경변수:
  ```
  GEMINI_API_KEY       # 필수
  GEMINI_MODEL         # 기본값: gemini-3.5-flash
  DEFAULT_MAX_LENGTH   # 기본값: 200 (응답 길이 제한 문자 수)
  ```

### 3.4 `app/service/prompt_builder.py` — 프롬프트 조립

- Jinja2 `Environment` + `FileSystemLoader("templates")` 사용
- 모드 분기: `problem_set` 존재 여부로 판단
  - 있으면 → `system_problem.j2` 렌더링
  - 없으면 → `system_free.j2` 렌더링
- 두 템플릿 모두 `{% include 'system_base.j2' %}` 로 공통 규칙 포함
- `dataset.meta_data` JSON string → `json.loads()` → 리스트 변환 후 템플릿에 전달

### 3.5 `app/service/gemini_client.py` — Gemini API 호출

- `google-genai` SDK 사용
- `system_instruction`에 프롬프트 빌더 결과 주입
- `contents`에 conversation_history + user_message 매핑
- conversation_history의 `"ai"` role → Gemini `"model"` role 변환
- `session_progress`는 `user_message`에 prefix로 합침

### 3.6 `app/schema/chat.py` — Pydantic 모델

- `ChatRequest`: Spring이 보내는 payload 그대로 수용
- `ChatResponse`: Spring이 기대하는 응답 구조
- `Optional` 필드: `problem_set`, `problems`, `session_progress`, `dataset` (NON_NULL 정책)

---

## 4. Payload 스펙

### 4.1 Request (`POST /chat`)

Spring이 보내는 JSON. `NON_NULL` 정책으로 null 필드는 JSON에 포함되지 않음.

```json
{
  "user_message": "4번 문제 풀이 도와줘",
  "problem_set": {
    "problem_set_id": 3001,
    "title": "pandas 기초 분석 문제 세트",
    "description": "CSV 데이터를 불러와 기본 정보를 확인하는 코드 실행형 문제 세트입니다."
  },
  "problems": [
    {
      "title": "데이터 행과 열 개수 확인",
      "content": "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하는 코드를 작성하세요.",
      "problem_type": "CODE",
      "answer": null,
      "explanation": "DataFrame의 shape 속성을 사용하면 행과 열 개수를 튜플로 확인할 수 있습니다.",
      "submitted_answer": null
    }
  ],
  "session_progress": {
    "current_problem_number": 1
  },
  "dataset": {
    "meta_data": "[\"id\", \"value\"]"
  },
  "conversation_history": [
    {
      "role": "user",
      "content": "고객 세그먼트가 정확히 뭔가요?"
    },
    {
      "role": "ai",
      "content": "이전 AI 응답..."
    }
  ]
}
```

### 4.2 필드 존재 조건

| 필드 | null이 되는 경우 |
|------|----------------|
| `problem_set` | 자유질문 모드 (chatRoom.problemSetId == null) |
| `problems` | 문제가 없을 때 |
| `session_progress` | chatRoom.problemId == null |
| `dataset` | ACTIVE dataset 없음 |
| `problems[].answer` | CODE 타입 문제 |
| `problems[].submitted_answer` | 유저가 아직 제출 안 함 |

### 4.3 Response

```json
{
  "answer": "AI 응답 텍스트",
  "is_answer_detected": false,
  "retry_count": 0,
  "prompt_tokens": 0,
  "completion_tokens": 0,
  "total_tokens": 0
}
```

> **현재 미구현 필드**: `is_answer_detected` → `false`, `retry_count` → `0`, 토큰 필드 → `0` 고정.
> 추후 답변 검증 로직 구현 시 활성화 예정.

> **주의**: `is_answer_detected` 키 이름 정확히 지켜야 함. `answer_detected`로 보내면 Spring 측 역직렬화 실패.

---

## 5. Gemini API 호출 구조

### 5.1 `system_instruction` vs `contents` 경계

Implicit caching 효율을 위해 **고정 데이터 → `system_instruction`**, **변하는 데이터 → `contents`** 분리.

| 데이터 | 같은 채팅방 내 변동 | 위치 |
|--------|-------------------|------|
| 역할 + 금지 규칙 + 포맷 규칙 | 불변 | `system_instruction` |
| 문제 세트 정보 | 불변 | `system_instruction` |
| 전체 문제 목록 + 정답 | 불변 | `system_instruction` |
| dataset.meta_data | 불변 | `system_instruction` |
| CODE/TEXT 분기 가이드 | 불변 | `system_instruction` |
| session_progress | 문제 넘기면 변함 | `contents` (user_message prefix) |
| conversation_history | 매번 변함 | `contents` (멀티턴 매핑) |
| user_message | 매번 변함 | `contents` |

### 5.2 conversation_history 매핑

```python
# payload의 "ai" role → Gemini의 "model" role 변환
contents = []
for msg in conversation_history:
    role = "model" if msg.role == "ai" else "user"
    contents.append({"role": role, "parts": [{"text": msg.content}]})

# session_progress prefix + 현재 메시지 추가
current_text = user_message
if session_progress:
    current_text = f"[Current problem: #{session_progress.current_problem_number}]\n\n{user_message}"
contents.append({"role": "user", "parts": [{"text": current_text}]})
```

### 5.3 Implicit Caching

- Gemini 2.5+ 모델에서 자동 활성화 (추가 코드 불필요)
- `system_instruction` 내용이 동일하면 자동 캐시 히트 → 입력 토큰 비용 90% 할인
- **캐시 히트 극대화 조건**: `system_instruction` 텍스트가 요청 간 완전히 동일해야 함
- 변하는 데이터를 `system_instruction`에 넣지 않는 이유가 이것

---

## 6. 모드 분기 로직

```python
# prompt_builder.py 핵심 분기
def build_system_prompt(request: ChatRequest) -> str:
    if request.problem_set is not None:
        template = env.get_template("system_problem.j2")
        # 문제풀이 모드: 문제, 정답, 데이터셋 전부 주입
    else:
        template = env.get_template("system_free.j2")
        # 자유질문 모드: 일반 어시스턴트
```

| 모드 | 조건 | 템플릿 | AI 역할 |
|------|------|--------|---------|
| 문제풀이 | `problem_set` 존재 | `system_problem.j2` | 데이터 분석 튜터 |
| 자유질문 | `problem_set` 없음 | `system_free.j2` | 일반 어시스턴트 (정답 금지 유지) |

---

## 7. 에러 처리 정책

- Python 서버는 에러를 **HTTP 상태 코드로 그대로 반환**
- Spring 측 `FastApiChatClient`가 WebClient로 예외 핸들링
- Python에서 에러를 삼키고 가짜 200 보내지 않을 것

| 상황 | HTTP 응답 |
|------|----------|
| Gemini API 호출 실패 | 502 Bad Gateway |
| payload 파싱 실패 | 422 Unprocessable Entity (FastAPI 기본) |
| 템플릿 렌더 실패 | 500 Internal Server Error |
| API 키 누락 | 500 Internal Server Error (서버 시작 시 검증 권장) |

---

## 8. 환경 설정

### 8.1 `.env` 파일

```env
GEMINI_API_KEY=your-gemini-api-key-here
GEMINI_MODEL=gemini-3.5-flash
DEFAULT_MAX_LENGTH=200
```

### 8.2 `.env.example` (Git 추적 대상)

```env
GEMINI_API_KEY=your-key-here
GEMINI_MODEL=gemini-3.5-flash
DEFAULT_MAX_LENGTH=200
```

### 8.3 `.gitignore`

```
.env
__pycache__/
*.pyc
venv/
.venv/
```

---

## 9. 의존성 (`requirements.txt`)

```
fastapi
uvicorn[standard]
pydantic
pydantic-settings
python-dotenv
google-genai
jinja2
```

---

## 10. 실행 방법

```bash
# 가상환경 생성 및 활성화
python -m venv venv
source venv/bin/activate        # Mac/Linux
venv\Scripts\activate           # Windows

# 의존성 설치
pip install -r requirements.txt

# .env 파일 생성
cp .env.example .env
# .env에 GEMINI_API_KEY 입력

# 서버 실행
uvicorn app.main:app --reload --port 8000
```

---

## 11. 개발 규칙

### 11.1 코드 스타일

- 함수/변수: `snake_case`
- 클래스: `PascalCase`
- 상수: `UPPER_SNAKE_CASE`
- type hint 필수 (함수 파라미터, 반환값)

### 11.2 모듈 간 의존성 방향

```
api/ → service/ → core/
       ↓
      schema/
```

- `api/`는 `service/`와 `schema/`만 import
- `service/`는 `core/`와 `schema/`만 import
- `schema/`는 외부 의존 없음 (Pydantic 모델만)
- `core/`는 외부 의존 없음 (설정만)

### 11.3 템플릿 수정 규칙

- 프롬프트 수정 시 **Python 코드를 건드리지 않을 것** — `.j2` 파일만 수정
- 영어 템플릿 수정 후 **반드시 `ko/` 한국어 참조본도 동기화**
- 새 변수를 템플릿에 추가하면 `prompt_builder.py`의 `render()` 호출부도 업데이트

### 11.4 새 기능 추가 체크리스트

1. payload 변경 → `schema/chat.py` Pydantic 모델 수정
2. 프롬프트 변경 → `templates/*.j2` 수정 + `ko/` 동기화
3. 새 환경변수 → `config.py` + `.env.example` 추가
4. 새 의존성 → `requirements.txt` 추가
5. Spring 측 payload 변경 시 → `docs/ChatBot/handoff.md`에 변경 이력 기록

---

## 12. 관련 문서

| 문서 | 경로 | 설명 |
|------|------|------|
| Spring ChatBot 컨벤션 | `docs/ChatBot/convention.md` | Spring 측 클린아키텍쳐 규칙 |
| 프롬프트/튜터링 컨벤션 | `docs/ChatBot/prompt_engineering_convention.md` | 프롬프트 구조 + 튜터링 규칙 |
| Payload 스펙 | `docs/ChatBot/handoff.md` | FastApiChatRequest/Response 상세 |
| 글로벌 컨벤션 | `docs/CONVENTION.md` | 에러코드, API 응답 등 |
