#!/bin/bash
# filepath: /Users/angel/Documents/Genetic System/backend/scripts/backup_local_data.sh

echo "📦 Creating backup of local PostgreSQL data..."

# Crear backup completo
pg_dump -U postgres -d biotracker \
    --clean \
    --create \
    --verbose \
    --file=biotracker_backup_$(date +%Y%m%d_%H%M%S).sql

echo "✅ Local backup created successfully!"