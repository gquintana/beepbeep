#!/usr/bin/env bash
TARGET_DIR="$(dirname $0)/target"
ES_VERSION=5.0.0
ES_TAR=${TARGET_DIR}/elasticsearch-${ES_VERSION}.tar.gz
ES_DIR=${TARGET_DIR}/elasticsearch-${ES_VERSION}
ES_PID=${TARGET_DIR}/elasticsearch.pid
mkdir -p ${TARGET_DIR}
if [ ! -f "${ES_TAR}" ]; then
  echo "Downloading Elasticsearch ${ES_VERSION}"
  curl -o ${ES_TAR} https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-${ES_VERSION}.tar.gz
fi
if [ ! -d "${ES_DIR}" ]; then
  tar -xzf ${ES_TAR} -C ${TARGET_DIR}
fi
echo "Starting Elasticsearch"
${ES_DIR}/bin/elasticsearch -d -p ${ES_PID}
sleep 30s
curl http://localhost:9200/_cluster/health?wait_for_status=yellow&timeout=30s
echo "Started Elasticsearch with PID $(<${ES_PID})"
