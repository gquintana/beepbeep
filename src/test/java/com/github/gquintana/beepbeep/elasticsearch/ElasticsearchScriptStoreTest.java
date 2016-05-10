package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.http.HttpClientProvider;
import com.github.gquintana.beepbeep.sql.DriverSqlConnectionProvider;
import com.github.gquintana.beepbeep.sql.SingleSqlConnectionProvider;
import com.github.gquintana.beepbeep.sql.SqlScriptStore;
import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.apache.http.client.methods.HttpDelete;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchScriptStoreTest {
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ElasticsearchRule elasticsearch = new ElasticsearchRule(temporaryFolder);

    private HttpClientProvider httpClientProvider;
    private ElasticsearchScriptStore store;

    @Before
    public void setUp() {
        httpClientProvider = new HttpClientProvider("http://" + elasticsearch.getElasticsearch().getHttpAddress());
        store = new ElasticsearchScriptStore(httpClientProvider, ".beepbeep/beepbeep");
        store.prepare();
    }

    @After
    public void tearDown() throws Exception {
        httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), new HttpDelete(".beepbeep"));
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
    public void testCreate() throws Exception {
        // Given
        ScriptInfo<String> info = createInfo();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getId()).isNotNull();
        assertThat(info.getVersion()).isEqualTo(1);
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @Test(expected = ScriptStoreException.class) @Ignore // TODO
    public void testCreate_AlreadyExists() throws Exception {
        // Given
        ScriptInfo<String> info = createInfo();
        info = store.create(info);
        // When
        info = store.create(info);
    }

    @Test
    public void testUpdate() throws Exception {
        // Given
        ScriptInfo<String> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
        // Then
        assertThat(info.getId()).isNotEmpty();
        assertThat(info.getVersion()).isEqualTo(2);
        ScriptInfo<String> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_NotFound() throws Exception {
        // Given
        ScriptInfo<String> info = createInfo();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        info.setId("12");
        info.setVersion(1);
        // When
        info = store.update(info);
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_ConcurrentModification() throws Exception {
        // Given
        ScriptInfo<String> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        int version = info.getVersion();
        info = store.update(info);
        info.setVersion(version);
        info.setStatus(ScriptStatus.FAILED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
    }
}
