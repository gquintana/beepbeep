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
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.Normalizer;
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

    /**
     * Execute HTTP request
     */
    private HttpResponse execute(HttpRequest httpRequest) throws IOException {
        HttpResponse httpResponse = httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), httpRequest);
        return httpResponse;
    }

    /**
     * Execute HTTP request
     */
    private <T> T execute(HttpRequest httpRequest, ResponseHandler<T> httpResponseHandler) throws IOException {
        return httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), httpRequest, httpResponseHandler);
    }

    private <T> T readJsonContent(HttpResponse httpResponse, Class<T> type) throws IOException {
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            return objectMapper.readValue(inputStream, type);
        }
    }

    private void checkError(HttpResponse httpResponse) throws IOException {
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() >= 400 && statusLine.getStatusCode() < 600) {
            JsonNode jsonNode = readJsonContent(httpResponse, JsonNode.class);
            StringBuilder message = new StringBuilder().append("HTTP error ").append(statusLine.getStatusCode()).append(", ").append(statusLine.getReasonPhrase());
            JsonNode error = jsonNode.get("error");
            if (error != null) {
                JsonNode reason = error.get("reason");
                message.append(", ").append((reason == null ? error : reason).asText());
            }
            throw new ScriptStoreException(message.toString());
        }
    }

    /**
     * Create index with settings if needed
     */
    public void prepare() {
        String[] splitIndex = indexType.split("/");
        String index = splitIndex[0];
        String type = splitIndex[1];
        try {
            // Get index
            HttpResponse httpResponse = execute(new HttpGet(index));
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
            httpResponse = execute(httpRequest);
            checkError(httpResponse);
            // Wait for index
            httpResponse = execute(new HttpGet("_cluster/health/" + index + "?" + URLEncoder.encode("wait_for_status=yellow&timeout=10s", "UTF-8")));
            checkError(httpResponse);
            // Force refresh
            httpResponse = execute(new HttpGet(index + "/_refresh"));
            checkError(httpResponse);
        } catch (IOException e) {
            throw new ScriptStoreException("Prepare index " + index + " failed", e);
        }
    }

    static String fullNameToId(String fullName) {
        // Remove accents
        String s = Normalizer.normalize(fullName, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Remove special chars
        s = s.replaceAll("[^\\w-]", "_");
        // Remove starting underscore, it could clash with _search...
        s = s.replaceFirst("^_+", "");
        return s;
    }

    @Override
    public ScriptInfo<String> getByFullName(String fullName) {
        try {
            String id = fullNameToId(fullName);
            // 1 requête par Id
            HttpGet httpRequest = new HttpGet(indexType + "/" + id);
            ScriptInfo<String> info = execute(httpRequest, new GetByFullNameResponseHandler1());
            // 2 requête par Full name
            if (info == null) {
                httpRequest = new HttpGet(indexType + "/_search?version=true&q=" + URLEncoder.encode("full_name:\"" + fullName + "\"", "UTF-8"));
                info = execute(httpRequest, new GetByFullNameResponseHandler2(fullName));
            }
            return info;
        } catch (IOException e) {
            throw new ScriptStoreException("Search script " + fullName + " failed", e);
        }
    }

    private class GetByFullNameResponseHandler1 implements ResponseHandler<ScriptInfo<String>> {
        @Override
        public ScriptInfo<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            if (httpResponse.getStatusLine().getStatusCode() == 404) {
                return null;
            }
            checkError(httpResponse);
            JsonNode jsonNode = readJsonContent(httpResponse, JsonNode.class);
            return read((ObjectNode) jsonNode);
        }
    }

    private class GetByFullNameResponseHandler2 implements ResponseHandler<ScriptInfo<String>> {
        private String fullName;

        public GetByFullNameResponseHandler2(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public ScriptInfo<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            checkError(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ScriptStoreException("Search script " + fullName + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            JsonNode jsonNode = readJsonContent(httpResponse, JsonNode.class);
            JsonNode hits = jsonNode.path("hits");
            int totalHits = hits.path("total").asInt();
            if (totalHits <= 0) {
                return null;
            }
            return read((ObjectNode) hits.get("hits").get(0));
        }
    }

    @Override
    public ScriptInfo<String> create(ScriptInfo<String> info) {
        try {
            HttpPut httpRequest = new HttpPut(indexType + "/" + fullNameToId(info.getFullName()) + "?refresh=true");
            httpRequest.setEntity(write(info));
            return execute(httpRequest, new CreateResponseHandler(info));
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
            checkError(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != 201) {
                throw new ScriptStoreException("Create script " + info.getFullName() + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            JsonNode jsonNode = readJsonContent(httpResponse, JsonNode.class);
            String id = jsonNode.get("_id").asText();
            int version = jsonNode.get("_version").asInt();
            info.setId(id);
            info.setVersion(version);
            return info;
        }
    }


    @Override
    public ScriptInfo<String> update(ScriptInfo<String> info) {
        String fullName = info.getFullName();
        try {
            HttpPut httpRequest = new HttpPut(indexType + "/" + info.getId() + "?refresh=true&version=" + info.getVersion());
            httpRequest.setEntity(write(info));
            return execute(httpRequest, new UpdateResponseHandler(info));
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
            checkError(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != 200) { // 409 means conflicts
                throw new ScriptStoreException("Update script " + info.getFullName() + " failed, " + httpResponse.getStatusLine().getReasonPhrase());
            }
            JsonNode jsonNode = readJsonContent(httpResponse, JsonNode.class);
            int version = jsonNode.get("_version").asInt();
            info.setVersion(version);
            return info;
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
