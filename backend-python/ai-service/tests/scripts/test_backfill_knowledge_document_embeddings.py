import importlib.util
import json
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import Mock

import pytest


def load_script_module():
    script_path = Path(__file__).resolve().parents[2] / "scripts" / "backfill_knowledge_document_embeddings.py"
    spec = importlib.util.spec_from_file_location("kb_backfill_script", script_path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class TestBackfillKnowledgeDocumentEmbeddingsScript:
    def test_validate_environment_requires_database_url(self, monkeypatch):
        module = load_script_module()

        monkeypatch.delenv("DATABASE_URL", raising=False)
        monkeypatch.setenv("EMBEDDING_PROVIDER", "local")

        with pytest.raises(SystemExit, match="DATABASE_URL"):
            module.validate_environment()

    def test_validate_environment_requires_openai_credentials(self, monkeypatch):
        module = load_script_module()

        monkeypatch.setenv("DATABASE_URL", "postgresql://test")
        monkeypatch.setenv("EMBEDDING_PROVIDER", "openai")
        monkeypatch.delenv("EMBEDDING_API_KEY", raising=False)
        monkeypatch.delenv("OPENAI_EMBEDDING_MODEL", raising=False)

        with pytest.raises(SystemExit, match="EMBEDDING_API_KEY, OPENAI_EMBEDDING_MODEL"):
            module.validate_environment()

    def test_main_dry_run_outputs_provider_and_count(self, monkeypatch, capsys):
        module = load_script_module()

        db = Mock()
        service = Mock()
        service.count_documents_missing_embeddings.return_value = 3

        monkeypatch.setattr(
            module,
            "parse_args",
            lambda: SimpleNamespace(
                kb_id=2,
                batch_size=100,
                max_batches=None,
                dry_run=True,
                output_file=None,
                fail_on_remaining=False
            )
        )
        monkeypatch.setattr(module, "validate_environment", lambda: "openai")
        monkeypatch.setattr(module, "SessionLocal", lambda: db)
        monkeypatch.setattr(module, "KnowledgeBaseService", lambda _db: service)

        exit_code = module.main()

        assert exit_code == 0
        assert db.close.called

        payload = json.loads(capsys.readouterr().out.strip())
        assert payload == {
            "kb_id": 2,
            "missing_count": 3,
            "embedding_provider": "openai"
        }

    def test_main_returns_non_zero_when_remaining_documents_exist(self, monkeypatch, capsys):
        module = load_script_module()

        db = Mock()
        service = Mock()
        service.backfill_missing_embeddings.return_value = {
            "before_count": 5,
            "updated_count": 2,
            "remaining_count": 3,
            "batches": 1
        }

        monkeypatch.setattr(
            module,
            "parse_args",
            lambda: SimpleNamespace(
                kb_id=None,
                batch_size=100,
                max_batches=None,
                dry_run=False,
                output_file=None,
                fail_on_remaining=True
            )
        )
        monkeypatch.setattr(module, "validate_environment", lambda: "local")
        monkeypatch.setattr(module, "SessionLocal", lambda: db)
        monkeypatch.setattr(module, "KnowledgeBaseService", lambda _db: service)

        exit_code = module.main()

        assert exit_code == 1
        assert db.close.called

        payload = json.loads(capsys.readouterr().out.strip())
        assert payload == {
            "before_count": 5,
            "updated_count": 2,
            "remaining_count": 3,
            "batches": 1,
            "kb_id": None,
            "embedding_provider": "local"
        }

    def test_main_writes_json_output_file(self, monkeypatch, tmp_path):
        module = load_script_module()

        db = Mock()
        service = Mock()
        service.count_documents_missing_embeddings.return_value = 0
        output_file = tmp_path / "result.json"

        monkeypatch.setattr(
            module,
            "parse_args",
            lambda: SimpleNamespace(
                kb_id=9,
                batch_size=100,
                max_batches=None,
                dry_run=True,
                output_file=str(output_file),
                fail_on_remaining=False
            )
        )
        monkeypatch.setattr(module, "validate_environment", lambda: "local")
        monkeypatch.setattr(module, "SessionLocal", lambda: db)
        monkeypatch.setattr(module, "KnowledgeBaseService", lambda _db: service)

        exit_code = module.main()

        assert exit_code == 0
        payload = json.loads(output_file.read_text(encoding="utf-8"))
        assert payload == {
            "kb_id": 9,
            "missing_count": 0,
            "embedding_provider": "local"
        }
