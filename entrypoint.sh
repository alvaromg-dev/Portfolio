#!/usr/bin/env bash
set -euo pipefail

PROFILE=""

usage() {
  echo "Usage: $0 -p <dev|pre|pro>"
  exit 1
}

while getopts "p:" opt; do
  case $opt in
    p) PROFILE="$OPTARG" ;;
    *) usage ;;
  esac
done

if [[ -z "$PROFILE" ]]; then
  usage
fi

if [[ "$PROFILE" != "dev" && "$PROFILE" != "pre" && "$PROFILE" != "pro" ]]; then
  echo "Error: invalid profile '$PROFILE'. Use one of: dev, pre, pro"
  exit 1
fi

ENV_FILE=".env.${PROFILE}"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: environment file '$ENV_FILE' not found"
  exit 1
fi

export PROFILE
docker compose up --build -d
