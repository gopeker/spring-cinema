#!/bin/bash
set -e

# Start Ollama server in background
echo "Starting Ollama server..."
nohup ollama serve > /tmp/ollama.log 2>&1 &

# Wait for server to be ready
echo "Waiting for Ollama to be ready..."
for i in {1..30}; do
  if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "Ollama server is ready."
    break
  fi
  sleep 2
done

# Pull model if not already present
echo "Checking for qwen2.5:1.5b model..."
if ! ollama list | grep -q qwen2.5:1.5b; then
  echo "Pulling qwen2.5:1.5b model..."
  ollama pull qwen2.5:1.5b
else
  echo "Model already present."
fi

# Keep container running
echo "Ollama is ready."
tail -f /dev/null