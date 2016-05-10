package com.github.gquintana.beepbeep.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.Instant;

public class ElasticsearchScriptStore implements ScriptStore<String> {
    private final HttpClientProvider httpClientProvider;
    /**
     * Index/Type
     */
    private final String indexType;
    /**
     * Jackson JSON mapper
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ElasticsearchScriptStore(HttpClientProvider httpClientProvider, String indexType) {
        this.httpClientProvider = httpClientProvider;
        this.indexType = indexType;
    }

    public void prepare() {
        String[] splitIndex = indexType.split("/");
        String index = splitIndex[0];
        String type = splitIndex[1];
        try {
            HttpClient httpClient = httpClientProvider.getHttpClient();
            // Get index
            HttpResponse httpResponse = httpClient.execute(httpClientProvider.getHttpHost(), new HttpGet(index));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                return; // Index already exists
            }
            // Put index
            HttpPut httpRequest = new HttpPut(index);
            String indexSettings = "{ \"settings\": {" +
                "\"number_of_shards\":1}," +
                "\"mappings\":{" +
                "\"" + type + "\": {\"properties\": {" +
                "\"full_name\":{\"type\":\"string\",\"index\":\"not_analyzed\"}," +
                "\"status\":{\"type\":\"string\",\"index\":\"not_analyzed\" }," +
                "\"start_date\":{\"type\":\"date\"}," +
                "\"end_date\":{\"type\":\"date\"}," +
                "\"sha1\":{\"type\":\"string\",\"index\":\"not_analyzed\" }" +
                "}}}}";
            httpRequest.setEntity(new StringEntity(indexSettings));
            httpResponse = httpClient.execute(httpClientProvider.getHttpHost(), httpRequest);
        } catch (IOException e) {
            throw new ScriptStoreException("Prepare index " + index + " failed", e);
        }
    }

    @Override
    public ScriptInfo<String> getByFullName(String fullName) {
        try {
            HttpClient httpClient = httpClientProvider.getHttpClient();
            HttpGet httpRequest = new HttpGet(indexType + "/_search?version=true&q=" + URLEncoder.encode("full_name:\"" + fullName + "\""));
            return httpClient.execute(httpClientProvider.getHttpHost(), httpRequest, new GetByFullNameResponseHandler(fullName));
        } catch (IOException e) {
            throw new ScriptStoreException("Search script " + fullName + " failed", e);
        }
    }

    private class GetByFullNameResponseHandler implements ResponseHandler<ScriptInfo<String>> {
        private String fullName;

        public GetByFullNameResponseHandler(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public ScriptInfo<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ScriptStoreException("Search script " + fullName + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                JsonNode jsonNode = objectMapper.readValue(inputStream, JsonNode.class);
                JsonNode hits = jsonNode.path("hits");
                int totalHits = hits.path("total").asInt();
                if (totalHits <= 0) {
                    return null;
                }
                return read((ObjectNode) hits.get("hits").get(0));
            }
        }
    }

    @Override
    public ScriptInfo<String> create(ScriptInfo<String> info) {
        try {
            HttpClient httpClient = httpClientProvider.getHttpClient();
            HttpPost httpRequest = new HttpPost(indexType + "?refresh=true");
            httpRequest.setEntity(write(info));
            return httpClient.execute(httpClientProvider.getHttpHost(), httpRequest, new CreateResponseHandler(info));
        } catch (IOException e) {
            throw new ScriptStoreException("Create script " + info.getFullName() + " failed", e);
        }
    }

    private class CreateResponseHandler implements ResponseHandler<ScriptInfo<String>> {
        private ScriptInfo<String> info;

        public CreateResponseHandler(ScriptInfo<String> info) {
            this.info = info;
        }

        @Override
        public ScriptInfo<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            if (httpResponse.getStatusLine().getStatusCode() != 201) {
                throw new ScriptStoreException("Create script " + info.getFullName() + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                JsonNode jsonNode = objectMapper.readValue(inputStream, JsonNode.class);
                String id = jsonNode.get("_id").asText();
                int version = jsonNode.get("_version").asInt();
                info.setId(id);
                info.setVersion(version);
                return info;
            }
        }
    }


    @Override
    public ScriptInfo<String> update(ScriptInfo<String> info) {
        String fullName = info.getFullName();
        try {
            HttpClient httpClient = httpClientProvider.getHttpClient();
            HttpPut httpRequest = new HttpPut(indexType + "/" + info.getId() + "?refresh=true&version=" + info.getVersion());
            httpRequest.setEntity(write(info));
            return httpClient.execute(httpClientProvider.getHttpHost(), httpRequest, new UpdateResponseHandler(info));
        } catch (IOException e) {
            throw new ScriptStoreException("Search script " + fullName + " failed", e);
        }
    }

    private class UpdateResponseHandler implements ResponseHandler<ScriptInfo<String>> {
        private ScriptInfo<String> info;

        public UpdateResponseHandler(ScriptInfo<String> info) {
            this.info = info;
        }

        @Override
        public ScriptInfo<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            if (httpResponse.getStatusLine().getStatusCode() != 200) { // 409 means conflicts
                throw new ScriptStoreException("Update script " + info.getFullName() + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                JsonNode jsonNode = objectMapper.readValue(inputStream, JsonNode.class);
                int version = jsonNode.get("_version").asInt();
                info.setVersion(version);
                return info;
            }
        }
    }

    private HttpEntity write(ScriptInfo<String> info) {
        if (info == null) {
            return null;
        }
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("full_name", info.getFullName());
            objectNode.put("size", info.getSize());
            objectNode.put("sha1", info.getSha1());
            objectNode.put("status", info.getStatus().name());
            objectNode.put("start_date", info.getStartDate().toEpochMilli());
            if (info.getEndDate() != null) {
                objectNode.put("end_date", info.getEndDate().toEpochMilli());
            }
            String json = objectMapper.writeValueAsString(objectNode);
            return new StringEntity(json, ContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new ScriptStoreException("Failed to serialize script info", e);
        }
    }

    private ScriptInfo<String> read(ObjectNode node) {
        if (node == null) {
            return null;
        }
        String id = node.get("_id").asText();
        int version = node.get("_version").asInt();
        ObjectNode source = (ObjectNode) node.get("_source");
        String fullName = source.get("full_name").asText();
        long size = source.get("size").asLong();
        String sha1 = source.get("sha1").asText();
        String status = source.get("status").asText();
        Long startDate = source.get("start_date").asLong();
        Long endDate = source.path("end_date").asLong();
        return new ScriptInfo<>(id, version, fullName, size, sha1,
            Instant.ofEpochMilli(startDate),
            endDate == 0L ? null : Instant.ofEpochMilli(endDate),
            status == null ? null : ScriptStatus.valueOf(status));
    }

}
