import os
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from typing import Optional

import httpx
from fastapi import FastAPI
from pydantic import BaseModel, Field


app = FastAPI(title="CodeBomba Python Runner")

DEFAULT_TIMEOUT_MS = 5000
MIN_TIMEOUT_MS = 100
MAX_TIMEOUT_MS = 10000
DATASET_DOWNLOAD_TIMEOUT_SECONDS = 10


class ExecuteRequest(BaseModel):
    code: str
    dataset_access_url: Optional[str] = Field(
        default=None,
        alias="datasetAccessUrl",
    )
    timeout_ms: int = Field(
        default=DEFAULT_TIMEOUT_MS,
        alias="timeoutMs",
    )

    model_config = {
        "populate_by_name": True,
    }


class ExecuteResponse(BaseModel):
    stdout: Optional[str]
    stderr: Optional[str]
    success: bool
    executionTimeMs: int


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/execute", response_model=ExecuteResponse)
def execute(request: ExecuteRequest) -> ExecuteResponse:
    started_at = time.monotonic()

    if not request.code or not request.code.strip():
        return failure_response(
            "실행할 코드가 비어 있습니다.",
            started_at,
        )

    timeout_ms = normalize_timeout(request.timeout_ms)

    try:
        with tempfile.TemporaryDirectory(prefix="codebomba-") as temp_dir:
            work_dir = Path(temp_dir)
            code_path = work_dir / "solution.py"

            code_path.write_text(
                request.code,
                encoding="utf-8",
            )

            execution_env = create_execution_environment()

            if request.dataset_access_url:
                dataset_path = work_dir / "dataset.csv"

                download_dataset(
                    request.dataset_access_url,
                    dataset_path,
                )

                execution_env["DATASET_PATH"] = str(dataset_path)

            result = subprocess.run(
                [sys.executable, str(code_path)],
                cwd=work_dir,
                env=execution_env,
                capture_output=True,
                text=True,
                timeout=timeout_ms / 1000,
                shell=False,
            )

            return ExecuteResponse(
                stdout=empty_to_none(result.stdout),
                stderr=empty_to_none(result.stderr),
                success=result.returncode == 0,
                executionTimeMs=elapsed_ms(started_at),
            )

    except subprocess.TimeoutExpired:
        return failure_response(
            f"코드 실행 제한 시간 {timeout_ms}ms를 초과했습니다.",
            started_at,
        )

    except httpx.HTTPStatusError as exception:
        return failure_response(
            f"데이터셋 다운로드에 실패했습니다. HTTP 상태: "
            f"{exception.response.status_code}",
            started_at,
        )

    except httpx.RequestError:
        return failure_response(
            "데이터셋 다운로드 서버에 연결할 수 없습니다.",
            started_at,
        )

    except Exception as exception:
        return failure_response(
            f"코드 실행 중 오류가 발생했습니다: {type(exception).__name__}",
            started_at,
        )


def download_dataset(
    dataset_access_url: str,
    destination: Path,
) -> None:
    with httpx.Client(
        timeout=DATASET_DOWNLOAD_TIMEOUT_SECONDS,
        follow_redirects=False,
    ) as client:
        response = client.get(dataset_access_url)
        response.raise_for_status()

        destination.write_bytes(response.content)


def create_execution_environment() -> dict[str, str]:
    allowed_environment_keys = (
        "PATH",
        "PYTHONPATH",
        "PYTHONHOME",
        "LANG",
        "LC_ALL",
        "TZ",
    )

    return {
        key: os.environ[key]
        for key in allowed_environment_keys
        if key in os.environ
    }


def normalize_timeout(timeout_ms: int) -> int:
    return max(
        MIN_TIMEOUT_MS,
        min(timeout_ms, MAX_TIMEOUT_MS),
    )


def empty_to_none(value: str) -> Optional[str]:
    stripped_value = value.strip()
    return stripped_value if stripped_value else None


def elapsed_ms(started_at: float) -> int:
    return int((time.monotonic() - started_at) * 1000)


def failure_response(
    message: str,
    started_at: float,
) -> ExecuteResponse:
    return ExecuteResponse(
        stdout=None,
        stderr=message,
        success=False,
        executionTimeMs=elapsed_ms(started_at),
    )
