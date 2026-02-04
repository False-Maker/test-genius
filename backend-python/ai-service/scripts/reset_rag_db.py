import sys
import os

# Add parent directory to path to import app modules
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from sqlalchemy import text
from app.database import SessionLocal
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("reset_db")

def reset_rag_db():
    logger.info("Starting RAG database reset...")
    db = SessionLocal()
    try:
        # 1. Drop all RAG-related tables
        tables_to_drop = [
            "test_suite_case",
            "test_case_suite",
            "knowledge_base_sync_log",
            "knowledge_base_permission",
            "bm25_index",
            "knowledge_document_chunk",
            "knowledge_document",
            "knowledge_base"
        ]
        
        logger.info("Dropping existing tables to clear history...")
        for table in tables_to_drop:
            try:
                db.execute(text(f"DROP TABLE IF EXISTS {table} CASCADE"))
                logger.info(f"Dropped table: {table}")
            except Exception as e:
                logger.warning(f"Failed to drop {table}: {e}")

        # 2. Reset embedding column in test_case
        logger.info("Resetting embedding column in test_case table...")
        try:
            db.execute(text("ALTER TABLE test_case DROP COLUMN IF EXISTS embedding"))
            db.commit()
        except Exception as e:
            logger.warning(f"Failed to drop embedding column: {e}")
            db.rollback()

        # 3. Re-initialize tables from SQL file
        sql_path = r"d:\Demo\test-genius\database\init\11_rag_enhancement_tables.sql"
        if not os.path.exists(sql_path):
            logger.error(f"SQL file not found: {sql_path}")
            return

        logger.info(f"Executing schema initialization from: {sql_path}")
        with open(sql_path, "r", encoding="utf-8") as f:
            sql_content = f.read()

        # Execute the SQL commands
        # Note: We execute the entire block. If this fails due to multiple commands,
        # we might need to split, but usually pgsql driver handles it.
        db.execute(text(sql_content))
        db.commit()
        logger.info("RAG tables re-initialized successfully.")

        # 4. Re-add embedding column to test_case with 512 dimensions
        logger.info("Adding test_case.embedding vector(512)...")
        try:
            db.execute(text("ALTER TABLE test_case ADD COLUMN IF NOT EXISTS embedding vector(512)"))
            db.commit()
            
            # Add index
            db.execute(text("""
                CREATE INDEX IF NOT EXISTS idx_test_case_embedding 
                ON test_case 
                USING ivfflat (embedding vector_cosine_ops) 
                WITH (lists = 100)
            """))
            db.commit()
            logger.info("test_case embedding column and index created.")
        except Exception as e:
            logger.error(f"Failed to add test_case embedding column: {e}")
            db.rollback()

        logger.info("✅ Database reset complete! All historical RAG data cleared and 512-dim schemas applied.")

    except Exception as e:
        logger.error(f"Critical error during database reset: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    reset_rag_db()
