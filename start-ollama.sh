#!/bin/sh
docker run -d \
  --name  my-ollama  \
  -p 11434:11434 \
  ollama/ollama:latest

docker run -d \
  --name pgvector \
  -p 5432:5432 \
  -e POSTGRES_DB=vectordb \
  -e POSTGRES_USER=testuser \
  -e POSTGRES_PASSWORD=testpwd \
  pgvector/pgvector:pg16

docker exec -it my-ollama ollama pull mistral
docker exec -it my-ollama ollama  pull nomic-embed-text


