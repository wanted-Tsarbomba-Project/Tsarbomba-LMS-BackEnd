import os
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from urllib.parse import urlparse

import requests
from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI()

MAX_DATASET_SIZE_BYTES = 50 * 1024 * 1024
MIN_TIMEOUT_MS = 100
MAX_TIMEOUT_MS = 10_000


class ExecuteRequest(BaseModel):
    code: str
    datasetAccessUrl: str | None = None
    timeoutMs: int = 5000


class ExecuteResponse(BaseModel):
    stdout: str | None
    stderr: str | None
    success: bool
    executionTimeMs: int


@app.post("/execute", response_model=ExecuteResponse)
def execute(request: ExecuteRequest):
    start_time = time.time()

    try:
        timeout_seconds = normalize_timeout(request.timeoutMs)

        with tempfile.TemporaryDirectory() as temp_directory:
            work_directory = Path(temp_directory)
            code_path = work_directory / "solution.py"

            code_path.write_text(request.code, encoding="utf-8")

            environment = create_execution_environment(
                request.datasetAccessUrl,
                work_directory
            )

            result = subprocess.run(
                [sys.executable, str(code_path)],
                cwd=work_directory,
                env=environment,
                capture_output=True,
                text=True,
                timeout=timeout_seconds
            )

            return ExecuteResponse(
                stdout=result.stdout.strip() if result.stdout else None,
                stderr=result.stderr.strip() if result.stderr else None,
                success=result.returncode == 0,
                executionTimeMs=elapsed_time_ms(start_time)
            )

    except subprocess.TimeoutExpired:
        return ExecuteResponse(
            stdout=None,
            stderr="코드 실행 시간이 초과되었습니다.",
            success=False,
            executionTimeMs=elapsed_time_ms(start_time)
        )

    except Exception:
        return ExecuteResponse(
            stdout=None,
            stderr="코드 실행 중 오류가 발생했습니다.",
            success=False,
            executionTimeMs=elapsed_time_ms(start_time)
        )


def create_execution_environment(
    dataset_access_url: str | None,
    work_directory: Path
) -> dict[str, str]:
    environment = {
        "PATH": os.environ.get("PATH", ""),
        "PYTHONUNBUFFERED": "1"
    }

    if dataset_access_url:
        dataset_path = work_directory / "dataset.csv"
        download_dataset(dataset_access_url, dataset_path)
        environment["DATASET_PATH"] = str(dataset_path)

    return environment


def download_dataset(dataset_access_url: str, destination: Path):
    validate_dataset_url(dataset_access_url)

    with requests.get(
        dataset_access_url,
        stream=True,
        timeout=(3, 30)
    ) as response:
        response.raise_for_status()

        downloaded_size = 0

        with destination.open("wb") as dataset_file:
            for chunk in response.iter_content(chunk_size=8192):
                if not chunk:
                    continue

                downloaded_size += len(chunk)

                if downloaded_size > MAX_DATASET_SIZE_BYTES:
                    raise ValueError("데이터셋 크기 제한을 초과했습니다.")

                dataset_file.write(chunk)


def validate_dataset_url(dataset_access_url: str):
    parsed_url = urlparse(dataset_access_url)
    hostname = parsed_url.hostname or ""

    is_gcs_host = (
        hostname == "storage.googleapis.com"
        or hostname.endswith(".storage.googleapis.com")
    )

    if parsed_url.scheme != "https" or not is_gcs_host:
        raise ValueError("허용되지 않은 데이터셋 URL입니다.")


def normalize_timeout(timeout_ms: int) -> float:
    normalized_timeout = max(
        MIN_TIMEOUT_MS,
        min(timeout_ms, MAX_TIMEOUT_MS)
    )

    return normalized_timeout / 1000


def elapsed_time_ms(start_time: float) -> int:
    return int((time.time() - start_time) * 1000)
