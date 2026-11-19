#!/usr/bin/env python3
"""
Exports the Claude Code session transcripts of this project to Markdown (docs/ai-log/).

Claude Code stores every session as JSONL under ~/.claude/projects/<project-path-slug>/.
This script turns each session into a readable Markdown file with the user prompts, the agent
answers and every tool call (commands, file edits) with a truncated result, plus an index
with only the prompts.

Usage (from the repository root, Python 3.9+, no dependencies):
    python scripts/export-ai-log.py
    python scripts/export-ai-log.py --source <transcripts-dir> --out docs/ai-log
"""
from __future__ import annotations

import argparse
import json
import re
from datetime import datetime
from pathlib import Path

MAX_RESULT_CHARS = 2000
MAX_INPUT_CHARS = 4000


def default_source(repo: Path) -> Path:
    slug = re.sub(r"[^A-Za-z0-9]", "-", str(repo.resolve()))
    return Path.home() / ".claude" / "projects" / slug


def truncate(text: str, limit: int) -> str:
    text = text.rstrip()
    if len(text) <= limit:
        return text
    return text[:limit] + f"\n... [truncated, {len(text) - limit} more characters]"


def fence(text: str, lang: str = "") -> str:
    ticks = "````" if "```" in text else "```"
    return f"{ticks}{lang}\n{text}\n{ticks}"


def result_text(block: dict) -> str:
    content = block.get("content")
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "\n".join(part.get("text", f"[{part.get('type')}]") for part in content if isinstance(part, dict))
    return ""


def describe_tool(block: dict) -> str:
    name = block.get("name", "tool")
    args = block.get("input") or {}
    header = f"**Tool: `{name}`**"
    if args.get("description"):
        header += f" — {args['description']}"
    if "command" in args:
        return header + "\n\n" + fence(truncate(args["command"], MAX_INPUT_CHARS), "bash")
    if "file_path" in args:
        header += f" — `{args['file_path']}`"
        body = args.get("content") or args.get("new_string")
        if body:
            return header + "\n\n" + fence(truncate(body, MAX_INPUT_CHARS))
        return header
    if args:
        return header + "\n\n" + fence(truncate(json.dumps(args, ensure_ascii=False, indent=2), MAX_INPUT_CHARS), "json")
    return header


def is_human_prompt(entry: dict) -> bool:
    origin = entry.get("origin") or {}
    return entry.get("type") == "user" and origin.get("kind", "human") == "human" and not entry.get("isMeta")


def export_session(path: Path, out_dir: Path) -> tuple[Path, list[tuple[str, str]]]:
    entries = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    messages = [e for e in entries if e.get("type") in ("user", "assistant") and isinstance(e.get("message"), dict)]
    started = next((e["timestamp"] for e in messages if e.get("timestamp")), None)
    date = datetime.fromisoformat(started.replace("Z", "+00:00")).strftime("%Y-%m-%d") if started else "unknown-date"

    lines = [f"# Claude Code session {path.stem}", "", f"- Started: {started}", f"- Source: `{path.name}`", ""]
    prompts: list[tuple[str, str]] = []
    prompt_number = 0

    for entry in messages:
        content = entry["message"].get("content")
        blocks = [{"type": "text", "text": content}] if isinstance(content, str) else (content or [])
        role = entry["type"]
        for block in blocks:
            kind = block.get("type")
            if role == "user" and kind == "text" and is_human_prompt(entry):
                text = block.get("text", "").strip()
                if not text or text.startswith("<"):
                    continue
                prompt_number += 1
                prompts.append((entry.get("timestamp", ""), text))
                lines += [f"## Prompt {prompt_number} — {entry.get('timestamp', '')}", "", "> " + text.replace("\n", "\n> "), ""]
            elif role == "user" and kind == "tool_result":
                text = truncate(result_text(block), MAX_RESULT_CHARS)
                if text:
                    lines += ["<details><summary>Result</summary>", "", fence(text), "", "</details>", ""]
            elif role == "assistant" and kind == "text" and block.get("text", "").strip():
                lines += ["### Agent", "", block["text"].strip(), ""]
            elif role == "assistant" and kind == "tool_use":
                lines += [describe_tool(block), ""]

    target = out_dir / f"session-{date}-{path.stem[:8]}.md"
    target.write_text("\n".join(lines), encoding="utf-8")
    return target, prompts


def main() -> None:
    repo = Path(__file__).resolve().parent.parent
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--source", type=Path, default=default_source(repo), help="directory with the *.jsonl transcripts")
    parser.add_argument("--out", type=Path, default=repo / "docs" / "ai-log", help="output directory")
    args = parser.parse_args()

    sessions = sorted(args.source.glob("*.jsonl"), key=lambda p: p.stat().st_mtime)
    if not sessions:
        raise SystemExit(f"No transcripts found in {args.source}")
    args.out.mkdir(parents=True, exist_ok=True)

    index = ["# AI interaction log", "",
             "Every prompt sent to the agent (Claude Code) while building this project, in order.",
             "The full sessions, including answers and every tool call, are linked below.", ""]
    for session in sessions:
        target, prompts = export_session(session, args.out)
        index += [f"## [{target.stem}]({target.name})", ""]
        index += [f"{i}. `{ts}` — {text.splitlines()[0][:300]}" for i, (ts, text) in enumerate(prompts, 1)]
        index.append("")
        print(f"Exported {session.name} -> {target} ({len(prompts)} prompts)")
    (args.out / "README.md").write_text("\n".join(index), encoding="utf-8")


if __name__ == "__main__":
    main()
