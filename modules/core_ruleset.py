import re

ADB_FORBIDDEN_REGEX = [
    re.compile(r"\badb\s+pull\s+/data/", re.IGNORECASE),
    re.compile(r"\badb\s+shell\s+rm\b", re.IGNORECASE),
    re.compile(r"\badb\s+install\b.*\.apk\b", re.IGNORECASE),
    re.compile(r"\b--record\b", re.IGNORECASE),
    re.compile(r"\bmonkey\b", re.IGNORECASE),
]
