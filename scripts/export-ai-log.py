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
import html
import json
import re
import textwrap
from datetime import datetime
from pathlib import Path

MAX_RESULT_CHARS = 2000
MAX_INPUT_CHARS = 4000
EMAIL = re.compile(r"[\w.+-]+@[\w-]+(?:\.[\w-]+)+")
# Prompts sent from a Claude project thread arrive wrapped in XML; the person's own words are in these tags.
WRAPPED_PROMPT = re.compile(r'<message [^>]*from="human"[^>]*>(.*?)</message>|<cited [^>]*author="user"[^>]*>(.*?)</cited>', re.S)
INDEXED_PROMPT = re.compile(r"^## Prompt \d+ — (\S*)\n\n> (.*)$", re.M)


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


def redact(text: str) -> str:
    """The log is committed: never publish e-mail addresses."""
    return EMAIL.sub("[email redacted]", text)


def prompt_texts(text: str) -> list[str]:
    """The person's prompts in a user message: the plain text, or the words inside a project-thread wrapper."""
    text = text.strip()
    if not text.startswith("<"):
        return [text] if text else []
    return [textwrap.dedent(html.unescape(next(g for g in m.groups() if g is not None))).strip()
            for m in WRAPPED_PROMPT.finditer(text)]


def is_queued_prompt(entry: dict) -> bool:
    """A message sent while the agent was busy is stored as an attachment, not as a user entry."""
    attachment = entry.get("attachment") or {}
    return entry.get("type") == "attachment" and attachment.get("type") == "queued_command" \
        and isinstance(attachment.get("prompt"), str)


def as_message(entry: dict) -> dict:
    if not is_queued_prompt(entry):
        return entry
    return {"type": "user", "timestamp": entry.get("timestamp"),
            "message": {"content": entry["attachment"]["prompt"]}}


def is_human_prompt(entry: dict) -> bool:
    origin = entry.get("origin") or {}
    if origin.get("subkind") == "projects-relay":
        # Messages of the person relayed into a project thread (only their <cited> words are kept).
        return entry.get("type") == "user"
    return entry.get("type") == "user" and origin.get("kind", "human") == "human" and not entry.get("isMeta")


def export_session(path: Path, out_dir: Path) -> tuple[Path, list[tuple[str, str]]]:
    entries = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    messages = [as_message(e) for e in entries
                if (e.get("type") in ("user", "assistant") and isinstance(e.get("message"), dict)) or is_queued_prompt(e)]
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
                for text in prompt_texts(block.get("text", "")):
                    prompt_number += 1
                    prompts.append((entry.get("timestamp", ""), text))
                    lines += [f"## Prompt {prompt_number} — {entry.get('timestamp', '')}", "",
                              "> " + text.replace("\n", "\n> "), ""]
            elif role == "user" and kind == "tool_result":
                text = truncate(result_text(block), MAX_RESULT_CHARS)
                if text:
                    lines += ["<details><summary>Result</summary>", "", fence(text), "", "</details>", ""]
            elif role == "assistant" and kind == "text" and block.get("text", "").strip():
                lines += ["### Agent", "", block["text"].strip(), ""]
            elif role == "assistant" and kind == "tool_use":
                lines += [describe_tool(block), ""]

    target = out_dir / f"session-{date}-{path.stem[:8]}.md"
    target.write_text(redact("\n".join(lines)), encoding="utf-8")
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

    for session in sessions:
        target, prompts = export_session(session, args.out)
        print(f"Exported {session.name} -> {target} ({len(prompts)} prompts)")
    write_index(args.out)


def outside_fences(markdown: str) -> str:
    """Drops fenced blocks: tool results may quote other logs, whose prompts must not be indexed again."""
    kept, fence_ticks = [], None
    for line in markdown.splitlines():
        ticks = re.match(r"^(`{3,})", line)
        if fence_ticks is None and ticks:
            fence_ticks = ticks.group(1)
        elif fence_ticks is not None and line.strip() == fence_ticks:
            fence_ticks = None
        elif fence_ticks is None:
            kept.append(line)
    return "\n".join(kept)


def write_index(out_dir: Path) -> None:
    """Indexes every exported session in the folder, including the ones exported on other machines."""
    index = ["# AI interaction log", "",
             "Every prompt sent to the agent (Claude Code) while building this project, in order.",
             "The full sessions, including answers and every tool call, are linked below.", ""]
    exported = sorted(out_dir.glob("session-*.md"),
                      key=lambda f: (INDEXED_PROMPT.search(outside_fences(f.read_text(encoding="utf-8"))) or [None, ""])[1])
    for file in exported:
        prompts = INDEXED_PROMPT.findall(outside_fences(file.read_text(encoding="utf-8")))
        index += [f"## [{file.stem}]({file.name})", ""]
        index += [f"{i}. `{ts}` — {text[:300]}" for i, (ts, text) in enumerate(prompts, 1)]
        index.append("")
    (out_dir / "README.md").write_text(redact("\n".join(index)), encoding="utf-8")


if __name__ == "__main__":
    main()
