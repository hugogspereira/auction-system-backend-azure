#!/usr/bin/env bash

docker compose  --env-file ./.env -f docker-compose.yaml  up -d --build --force-recreate