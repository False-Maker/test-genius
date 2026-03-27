#!/usr/bin/env python3
import argparse
import json
import os
import sys


CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(CURRENT_DIR)
if PROJECT_ROOT not in sys.path:
    sys.path.append(PROJECT_ROOT)

from app.database import SessionLocal
from app.services.knowledge_base_service import KnowledgeBaseService


def validate_environment():
    missing = []

    if not os.getenv("DATABASE_URL"):
        missing.append("DATABASE_URL")

    provider = os.getenv("EMBEDDING_PROVIDER", "local").strip().lower() or "local"
    if provider not in {"local", "openai"}:
        raise SystemExit(f"Unsupported EMBEDDING_PROVIDER: {provider}")

    if provider == "openai":
        for key in ("EMBEDDING_API_KEY", "OPENAI_EMBEDDING_MODEL"):
            if not os.getenv(key):
                missing.append(key)

    if missing:
        raise SystemExit(f"Missing required environment variables: {', '.join(missing)}")

    return provider


def parse_args():
    parser = argparse.ArgumentParser(description="Backfill missing embeddings for knowledge documents.")
    parser.add_argument("--kb-id", type=int, default=None, help="Only backfill documents in the given knowledge base.")
    parser.add_argument("--batch-size", type=int, default=100, help="Number of documents to process per batch.")
    parser.add_argument("--max-batches", type=int, default=None, help="Optional maximum number of batches to run.")
    parser.add_argument("--dry-run", action="store_true", help="Only report how many documents still need backfill.")
    parser.add_argument("--output-file", type=str, default=None, help="Optional JSON file path to write the result.")
    parser.add_argument(
        "--fail-on-remaining",
        action="store_true",
        help="Exit with non-zero status when documents are still missing embeddings."
    )
    return parser.parse_args()


def main():
    args = parse_args()
    provider = validate_environment()
    db = SessionLocal()

    try:
        service = KnowledgeBaseService(db)

        if args.dry_run:
            result = {
                "kb_id": args.kb_id,
                "missing_count": service.count_documents_missing_embeddings(kb_id=args.kb_id)
            }
            remaining_count = result["missing_count"]
        else:
            result = service.backfill_missing_embeddings(
                batch_size=args.batch_size,
                max_batches=args.max_batches,
                kb_id=args.kb_id
            )
            result["kb_id"] = args.kb_id
            remaining_count = result["remaining_count"]

        result["embedding_provider"] = provider
        result_json = json.dumps(result, ensure_ascii=False)
        print(result_json)
        if args.output_file:
            with open(args.output_file, "w", encoding="utf-8") as output_file:
                output_file.write(result_json + "\n")
        if args.fail_on_remaining and remaining_count > 0:
            return 1
        return 0
    finally:
        db.close()


if __name__ == "__main__":
    raise SystemExit(main())
