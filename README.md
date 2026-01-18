### RAG Chat Application Using Ollama

**RAG Chat Application:** A local AI-powered chat system that retrieves relevant health tips from documents and provides context-aware responses.
Uses PostgreSQL + pgvector for semantic search, enabling fast and accurate information retrieval from your health knowledge base.

Chat API Endpoint:
```
http://localhost:8087/mysbragapp/api/rag_chat
```
---

### **1. Ollama**

**Ollama** is a local LLM (Large Language Model) server that lets you run models on your own machine. Think of it as a **local AI API server**, similar to OpenAI’s API, but everything runs locally instead of in the cloud.

Key points:

* Hosts multiple models (chat models and embedding models).
* Provides endpoints like `/api/chat` (for text generation) and `/api/embed` (for embeddings).
* Can be run in a **Docker container** or installed natively.
* Handles the **heavy lifting of model execution** on your local hardware (CPU/GPU).

---

### **2. `nomic-embed-text`**

* This is an **embedding model** provided by Ollama.
* **Purpose:** Converts text into **vector embeddings** (numerical arrays) so it can be stored in a vector database like **PostgreSQL + pgvector**.
* **Use case in your RAG app:** When you ingest documents, the app calls `/api/embed` to get embeddings for each chunk of text. These embeddings are later used for **similarity search** during retrieval.
* **Typical vector size:** 1536 (default if the model cannot report it).

---

### **3. `mistral`**

* This is a **chat (LLM) model** provided by Ollama.
* **Purpose:** Generates text responses based on prompts.
* **Use case in your RAG app:** When you query the system, your app calls `/api/chat`, which uses the `mistral` model to produce **context-aware answers** based on the retrieved chunks from the vector store.
* **Memory requirement:** Usually larger than embeddings (4–6+ GB).

---

### **How They Work Together in Your RAG App**

| Component        | Model                  | Endpoint     | Purpose                                                                           |
|------------------|------------------------|--------------|-----------------------------------------------------------------------------------|
| **Embedding**    | `nomic-embed-text`     | `/api/embed` | Convert text chunks into vectors for storing in pgvector                          |
| **Chat / LLM**   | `mistral`              | `/api/chat`  | Generate answers to user queries using retrieved embeddings from the vector store |
| **Vector Store** | pgvector in PostgreSQL | N/A          | Store and search embeddings for similarity search                                 |

---

💡 **Analogy:**

* Embeddings (`nomic-embed-text`) → “index” your documents like a search engine.
* Chat (`mistral`) → “answer” your questions using the indexed documents.

### Summary Table: Issues, Root Cause, and Solutions (Local RAG)

| # | Issue / Error Message (Short)                             | Where It Occurred               | Root Cause                                                                                     | Recommended Solution                                                                                     |
|---|-----------------------------------------------------------|---------------------------------|------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| 1 | `model "mxbai-embed-large" not found (404)`               | Application startup / embedding | Spring AI defaulted to `mxbai-embed-large`, but the model was not pulled in Ollama             | Explicitly configure embedding model and pull it:<br>`spring.ai.ollama.embedding.model=nomic-embed-text` |
| 2 | `model requires more system memory (4.5 GiB)` (Embedding) | Document ingestion              | `mxbai-embed-large` embedding model requires more RAM than Ollama has access to                | Use a smaller embedding model (`nomic-embed-text`) or increase Ollama/Docker memory                      |
| 3 | `expected 1024 dimensions, not 768`                       | PgVector insert                 | PgVector table was created with 1024 dims (old model) but new embedding model outputs 768 dims | Drop and recreate vector table or alter column to `vector(768)`                                          |
| 4 | `model requires more system memory (4.5 GiB)` (Chat)      | Chat API call                   | Chat model (e.g., 7B/8B LLM) too large for available Ollama memory                             | Switch to smaller chat model (`llama3.2:3b`, `phi3:mini`)                                                |
| 5 | Repeated `Retry count: 1,2,3…`                            | Runtime logs                    | Spring AI retries on non-recoverable memory errors                                             | Disable retries for local use: `spring.ai.retry.enabled=false`                                           |
| 6 | Confusion about “where memory is needed”                  | Troubleshooting                 | Ollama uses native system/Docker RAM, not JVM heap                                             | Do not tune JVM heap; tune Ollama model size or Docker memory                                            |
| 7 | `docker exec ... ollama pull ...` fails                   | Model pull                      | Ollama not running in Docker under expected container name                                     | Verify Ollama runtime (Docker vs native) and pull model accordingly                                      |


### Model & Resource Guidance (Key Reference)

| Embedding Model                 | Output Vector Dimension | Approx. Memory Required (RAM) | Notes                                                                               |
|---------------------------------|-------------------------|-------------------------------|-------------------------------------------------------------------------------------|
| `mxbai-embed-large`             | 1536                    | ~4.5 GB                       | Default in Spring AI if not overridden; too large for local laptop memory (~3.7 GB) |
| `nomic-embed-text`              | 768                     | ~1–1.5 GB                     | Recommended for local use; fits laptop memory; matches PgVector 768-dim schema      |
| `openai-text-embedding-3-large` | 1536                    | ~3–4 GB                       | Cloud API embedding; if using local Ollama, not applicable                          |
| `openai-text-embedding-3-small` | 1024                    | ~2 GB                         | Smaller, less accurate; suitable for limited memory                                 |


### PgVector Table Mapping
* Your PgVector vector table dimensions must match the embedding output:

  | Embedding Model     | PgVector `vector(n)` |
  |---------------------|----------------------|
  | `nomic-embed-text`  | `vector(768)`        |
  | `mxbai-embed-large` | `vector(1536)`       |


### Errors Observed and Suggested Solutions
| **Error Type / Exception**                    | **HTTP / System Code** | **Context / Trigger**          | **Root Cause**                                                                        | **Notes / Solution Direction**                                                                                             |
|-----------------------------------------------|------------------------|--------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `ResourceAccessException`                     | N/A                    | POST `/api/embed`              | Could not connect to Ollama API (localhost:11434)                                     | Ollama server not running or wrong port. Start the Ollama container.                                                       |
| `ConnectException` / `ClosedChannelException` | N/A                    | POST `/api/embed`              | Network/socket issue connecting to embedding API                                      | Ensure Ollama server is running and accessible from Spring Boot.                                                           |
| `NonTransientAiException`                     | HTTP 404               | Embedding call                 | Model `"mxbai-embed-large"` not found                                                 | Wrong or missing model in Ollama. Pull the model or update Spring properties to use a valid model like `nomic-embed-text`. |
| `TransientAiException`                        | HTTP 500               | Embedding call                 | Model requires more memory than available (e.g., 4.5 GiB required, 3.7 GiB available) | Increase system memory, allocate more resources to Docker/Ollama, or use a lighter model.                                  |
| `WARN` fallback                               | N/A                    | `PgVectorStore` initialization | Failed to get embedding dimensions from model; fallback to default `1536`             | Check that the embedding API is available and the correct model is configured.                                             |
| `ApplicationContext` startup failure          | N/A                    | Spring Boot initialization     | Triggered by embedding model failure (`404` / `500`)                                  | Fix Ollama model and API availability before starting Spring Boot.                                                         |
