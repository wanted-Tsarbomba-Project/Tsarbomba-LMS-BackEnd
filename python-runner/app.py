import subprocess
import tempfile
import time
from pathlib import Path

from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI()


class ExecuteRequest(BaseModel):
    code: str


class ExecuteResponse(BaseModel):
    stdout: str | None
    stderr: str | None
    success: bool
    executionTimeMs: int


@app.post("/execute", response_model=ExecuteResponse)
def execute(request: ExecuteRequest):
    start_time = time.time()
    temp_file_path = None

    try:
        with tempfile.NamedTemporaryFile(
            mode="w",
            suffix=".py",
            delete=False,
            encoding="utf-8"
        ) as temp_file:
            temp_file.write(request.code)
            temp_file_path = temp_file.name

        result = subprocess.run(
            ["python", temp_file_path],
            capture_output=True,
            text=True,
            timeout=5
        )

        execution_time_ms = int((time.time() - start_time) * 1000)

        return ExecuteResponse(
            stdout=result.stdout.strip() if result.stdout else None,
            stderr=result.stderr.strip() if result.stderr else None,
            success=result.returncode == 0,
            executionTimeMs=execution_time_ms
        )

    except subprocess.TimeoutExpired:
        execution_time_ms = int((time.time() - start_time) * 1000)

        return ExecuteResponse(
            stdout=None,
            stderr="코드 실행 시간이 초과되었습니다.",
            success=False,
            executionTimeMs=execution_time_ms
        )

    finally:
        if temp_file_path:
            Path(temp_file_path).unlink(missing_ok=True)