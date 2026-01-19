
# RAG Chat Application Using Ollama

**RAG Chat Application:** A local AI-powered chat system that retrieves relevant **health tips** from documents and provides context-aware responses.
It uses **PostgreSQL + pgvector** for semantic search, enabling fast and accurate information retrieval from your health knowledge base.

**How To Run In Local:**
* Make sure docker is running in local.
* Use these commands in terminal;
```
1)  ./start-ollama.sh
2) docker compose build
3) docker compose up -d
4) mvn spring-boot:run
```
**Chat API Endpoint:**

```
http://localhost:8087/mysbragapp/api/rag_chat/healthy_tips
```

---

## 1. Ollama

**Ollama** is a local LLM (Large Language Model) server that runs models on your machine.
Think of it as a **local AI API server**—similar to OpenAI—but everything runs locally instead of in the cloud.

**Key Features:**

* Hosts multiple models (chat and embedding models).
* Provides endpoints like `/api/chat` (text generation) and `/api/embed` (vector embeddings).
* Can be run via **Docker** or installed natively.
* Handles **model execution** on your local CPU/GPU.

---

## 2. `nomic-embed-text` (Embedding Model)

* Converts text into **vector embeddings** for semantic search.
* **Use in RAG App:** Called via `/api/embed` to create embeddings for document chunks.
* **Vector dimension:** 768 (default if the model cannot report it).
* **Memory footprint:** ~1–1.5 GB; recommended for local setups.

---

## 3. `mistral` (Chat / LLM Model)

* Generates **context-aware responses** based on prompts.
* **Use in RAG App:** Called via `/api/chat` to answer user queries using retrieved embeddings.
* **Memory requirement:** Typically 4–6 GB or more depending on model size.

---

## 4. How They Work Together

| Component        | Model                  | Endpoint     | Purpose                                                           |
|------------------|------------------------|--------------|-------------------------------------------------------------------|
| **Embedding**    | `nomic-embed-text`     | `/api/embed` | Convert text chunks into vectors for storing in pgvector          |
| **Chat / LLM**   | `mistral`              | `/api/chat`  | Generate answers using retrieved embeddings from the vector store |
| **Vector Store** | pgvector in PostgreSQL | N/A          | Store and search embeddings for similarity search                 |

**Analogy:**

* **Embeddings →** “index” your documents like a search engine
* **Chat →** “answer” your questions using the indexed documents

---

## 5. Common Issues & Solutions (Local RAG)

| # | Error / Issue                                 | Context             | Root Cause                                         | Recommended Solution                                                              |
|---|-----------------------------------------------|---------------------|----------------------------------------------------|-----------------------------------------------------------------------------------|
| 1 | `model "mxbai-embed-large" not found (404)`   | Startup / embedding | Spring AI defaults to missing model                | Set embedding model:<br>`spring.ai.ollama.embedding.model=nomic-embed-text`       |
| 2 | `model requires more system memory (4.5 GiB)` | Document ingestion  | Embedding model too large for available RAM        | Use smaller embedding model (`nomic-embed-text`) or increase Docker/Ollama memory |
| 3 | `expected 1024 dimensions, not 768`           | PgVector insert     | PgVector table schema mismatched                   | Drop & recreate table or alter column to `vector(768)`                            |
| 4 | `model requires more system memory (4.5 GiB)` | Chat API            | Chat model too large for available RAM             | Switch to smaller chat model (`llama3.2:3b`, `phi3:mini`)                         |
| 5 | Repeated `Retry count: 1,2,3…`                | Runtime logs        | Spring AI retrying on non-recoverable errors       | Disable retries for local use:<br>`spring.ai.retry.enabled=false`                 |
| 6 | Confusion about memory location               | Troubleshooting     | Ollama uses native system/Docker RAM, not JVM heap | Do not tune JVM heap; adjust Ollama model size or Docker memory                   |
| 7 | `docker exec ... ollama pull ...` fails       | Model pull          | Ollama container not running                       | Verify Ollama runtime (Docker vs native) and pull model accordingly               |

---

## 6. Model & Vector Dimension Reference

| Embedding Model                 | Vector Dimension | Approx. RAM | Notes                                               |
|---------------------------------|------------------|-------------|-----------------------------------------------------|
| `mxbai-embed-large`             | 1536             | ~4.5 GB     | Too large for local laptop memory (~3.7 GB)         |
| `nomic-embed-text`              | 768              | ~1–1.5 GB   | Recommended for local use; fits PgVector 768 schema |
| `openai-text-embedding-3-large` | 1536             | ~3–4 GB     | Cloud API; not for local Ollama                     |
| `openai-text-embedding-3-small` | 1024             | ~2 GB       | Smaller, less accurate; for limited memory          |

**PgVector Table Mapping:**

| Embedding Model     | PgVector `vector(n)` |
|---------------------|----------------------|
| `nomic-embed-text`  | `vector(768)`        |
| `mxbai-embed-large` | `vector(1536)`       |

---

## 7. Observed Runtime Errors

| Error Type / Exception                        | HTTP / System Code | Context            | Root Cause                          | Recommended Fix                                    |
|-----------------------------------------------|--------------------|--------------------|-------------------------------------|----------------------------------------------------|
| `ResourceAccessException`                     | N/A                | POST `/api/embed`  | Ollama not running                  | Start Ollama or check port                         |
| `ConnectException` / `ClosedChannelException` | N/A                | POST `/api/embed`  | Network/socket issue                | Ensure Ollama is accessible from Spring Boot       |
| `NonTransientAiException`                     | 404                | Embedding call     | Missing model `"mxbai-embed-large"` | Pull model or switch to `nomic-embed-text`         |
| `TransientAiException`                        | 500                | Embedding call     | Model memory exceeds available RAM  | Increase system/Docker memory or use lighter model |
| `WARN` fallback                               | N/A                | PgVectorStore init | Failed to get embedding dims        | Check embedding API & model config                 |
| ApplicationContext startup failure            | N/A                | Spring Boot init   | Embedding model failure             | Fix Ollama model/API availability                  |

---