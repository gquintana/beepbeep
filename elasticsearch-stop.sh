#!/usr/bin/env bash
TARGET_DIR="$(dirname $0)/target"
ES_PID=${TARGET_DIR}/elasticsearch.pid
if [ -f "${ES_PID}" ]; then
  PID=$(<${ES_PID})
  echo "Killing Elasticsearch process ${PID}"
  kill ${PID}
fi
