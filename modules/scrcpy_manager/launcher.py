import json
import shlex
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional

from modules.core_ruleset import ADB_FORBIDDEN_REGEX


@dataclass(frozen=True)
class ScrcpyConfig:
    scrcpy_binary: str
    adb_binary: str
    default_args: List[str]
    allowed_binaries: List[str]
    forbidden_patterns: List[str]

    @staticmethod
    def from_json(path: Path) -> "ScrcpyConfig":
        data = json.loads(path.read_text(encoding="utf-8"))
        return ScrcpyConfig(
            scrcpy_binary=data["scrcpy_binary"],
            adb_binary=data["adb_binary"],
            default_args=list(data.get("default_args", [])),
            allowed_binaries=list(data["allowed_binaries"]),
            forbidden_patterns=list(data["forbidden_patterns"]),
        )


class GuardEngine:
    def __init__(
        self,
        allowed_binaries: Iterable[str],
        forbidden_patterns: Iterable[str],
    ) -> None:
        self._allowed_binaries = set(allowed_binaries)
        self._forbidden_patterns = list(forbidden_patterns)

    def validate(self, command: List[str]) -> None:
        if not command:
            raise ValueError("Command cannot be empty.")

        binary = command[0]
        if binary not in self._allowed_binaries:
            raise ValueError(f"Binary '{binary}' is not in the allowlist.")

        rendered = " ".join(shlex.quote(part) for part in command)
        for pattern in self._forbidden_patterns:
            if pattern in rendered:
                raise ValueError(f"Command contains forbidden pattern: {pattern}")

        for regex in ADB_FORBIDDEN_REGEX:
            if regex.search(rendered):
                raise ValueError(f"Command violates core ruleset: {regex.pattern}")


class ScrcpyLauncher:
    def __init__(self, config_path: Optional[Path] = None) -> None:
        resolved_path = config_path or Path("config/scrcpy_defaults.json")
        self._config = ScrcpyConfig.from_json(resolved_path)
        self._guard = GuardEngine(
            allowed_binaries=self._config.allowed_binaries,
            forbidden_patterns=self._config.forbidden_patterns,
        )

    def run_scrcpy(
        self,
        device_id: Optional[str] = None,
        extra_args: Optional[List[str]] = None,
        check: bool = True,
    ) -> subprocess.CompletedProcess:
        command = [self._config.scrcpy_binary]
        if device_id:
            command.extend(["-s", device_id])
        command.extend(self._config.default_args)
        if extra_args:
            command.extend(extra_args)
        return self._run_command(command, check=check)

    def run_adb(
        self,
        adb_args: List[str],
        check: bool = True,
    ) -> subprocess.CompletedProcess:
        command = [self._config.adb_binary, *adb_args]
        return self._run_command(command, check=check)

    def _run_command(
        self,
        command: List[str],
        check: bool = True,
    ) -> subprocess.CompletedProcess:
        self._guard.validate(command)
        return subprocess.run(command, check=check)
