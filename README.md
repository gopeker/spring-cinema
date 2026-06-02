# Spring Cinema

A Spring Boot + Angular movie theater application with PostgreSQL database and LLM-powered chatbot.

## Features

- Movie listings and booking
- PostgreSQL database for data persistence
- AI chatbot for movie recommendations using Ollama (Qwen 2.5 1.5B model)
- Docker Compose for easy setup

## Quick Start

```bash
# Start all services
docker compose up -d

# For fresh start (removes existing data)
docker compose down -v && docker compose up -d
```

## Services

| Service          | URL |
|------------------|-----|
| Angular Frontend | http://localhost |
| Spring App       | http://localhost:8080 |
| PostgreSQL       | localhost:5432 |
| Ollama           | http://localhost:11434 |

## Chatbot

Access the AI chatbot at: http://localhost/chatbot

The chatbot uses Qwen 2.5 1.5B model via Ollama and remembers conversation history.

## Stop

```bash
docker compose down
```
