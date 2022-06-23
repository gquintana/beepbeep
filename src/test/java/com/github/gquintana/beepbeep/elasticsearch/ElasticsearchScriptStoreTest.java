package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.TestElasticsearch;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.apache.http.client.methods.HttpDelete;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
public class ElasticsearchScriptStoreTest {
    @TempDir
    Path tempDir;
    @Container
    static ElasticsearchContainer elasticsearchContainer = TestElasticsearch.createContainer();

    private HttpClientProvider httpClientProvider;
    private ElasticsearchScriptStore store;

    @BeforeEach
    public void setUp() {
        httpClientProvider = new BasicHttpClientProvider("http://" + elasticsearchContainer.getHttpHostAddress());
        store = new ElasticsearchScriptStore(httpClientProvider, ".beepbeep");
        store.prepare();
    }

    @AfterEach
    public void tearDown() throws IOException {
        httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), new HttpDelete(".beepbeep?ignore_unavailable=true"));
    }

    private ScriptInfo<String> createInfo() {
        ScriptInfo<String> info = new ScriptInfo<>();
        info.setFullName("/test/script.sql");
        info.setSize(421);
        info.setSha1("123abc");
        info.setStatus(ScriptStatus.STARTED);
        info.setStartDate(Instant.now());
        return info;
    }

    @Test
    public void testCreate()  {
        // Given
        ScriptInfo<String> info = createInfo();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getId()).isNotNull();
        assertThat(info.getVersion()).isEqualTo("0/1");
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @Test
    public void testCreate_AlreadyExists() {
        // Given
        ScriptInfo<String> info = createInfo();
        ScriptInfo<String> info2 = store.create(info);
        // When
        assertThatThrownBy(() -> store.create(info2)).isInstanceOf(ScriptStoreException.class);
    }

    @Test
    public void testUpdate()  {
        // Given
        ScriptInfo<String> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
        // Then
        assertThat(info.getId()).isNotEmpty();
        assertThat(info.getVersion()).isEqualTo("1/1");
        ScriptInfo<String> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @Test
    public void testUpdate_NotFound()  {
        // Given
        ScriptInfo<String> info = createInfo();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        info.setId("12");
        info.setVersion("1/2");
        // When
        assertThatThrownBy(() -> store.update(info)).isInstanceOf(ScriptStoreException.class);
    }

    @Test
    public void testUpdate_ConcurrentModification()  {
        // Given
        ScriptInfo<String> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        String version = info.getVersion();
        info = store.update(info);
        info.setVersion(version);
        info.setStatus(ScriptStatus.FAILED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        ScriptInfo<String> info2 = info;
        // When
        assertThatThrownBy(() -> store.update(info2))
            .isInstanceOf(ScriptStoreException.class);
    }

    @Test
    public void testFullNameToId() {
        assertThat(ElasticsearchScriptStore.fullNameToId("C:\\Program Files\\beepbeep\\logs\\beepbeep.log"))
            .isEqualTo("C__Program_Files_beepbeep_logs_beepbeep_log");
        assertThat(ElasticsearchScriptStore.fullNameToId("/home/sconnor/ça c'est un accent aigü"))
            .isEqualTo("home_sconnor_ca_c_est_un_accent_aigu");
    }
}
