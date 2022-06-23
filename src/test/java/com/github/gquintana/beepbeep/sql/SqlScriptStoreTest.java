package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.github.gquintana.beepbeep.sql.TestSqlConnectionProviders.createSqlConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SqlScriptStoreTest {
    private SingleSqlConnectionProvider connectionProvider;
    private SqlScriptStore store;


    @BeforeEach
    public void setUp() {
        connectionProvider = new SingleSqlConnectionProvider(
            createSqlConnectionProvider());
    }

    private SqlScriptStore prepareStore(boolean sequence) {
        store = new SqlScriptStore(connectionProvider, "beepbeep", sequence);
        store.prepare();
        return store;
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try(Connection connection= connectionProvider.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE beepbeep");
            if (store.isSequence()) {
                statement.execute("DROP SEQUENCE beepbeep_seq");
            }
        }
        connectionProvider.close();
    }

    private ScriptInfo<Integer> createInfo() {
        ScriptInfo<Integer> info = new ScriptInfo<>();
        info.setFullName("/test/script.sql");
        info.setSize(421);
        info.setSha1("123abc");
        info.setStatus(ScriptStatus.STARTED);
        info.setStartDate(Instant.now());
        return info;
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testCreate(boolean sequence) {
        // Given
        prepareStore(sequence);
        ScriptInfo<Integer> info = createInfo();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getVersionAsInt()).isEqualTo(1);
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testCreate_AlreadyExists(boolean sequence) {
        // Given
        prepareStore(sequence);
        ScriptInfo<Integer> info = createInfo();
        ScriptInfo<Integer> info2 = store.create(info);
        // When
        assertThatThrownBy(() -> store.create(info2)).isInstanceOf(ScriptStoreException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testUpdate(boolean sequence) {
        // Given
        prepareStore(sequence);
        ScriptInfo<Integer> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
        // Then
        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getVersionAsInt()).isEqualTo(2);
        ScriptInfo<Integer> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testUpdate_NotFound(boolean sequence) {
        // Given
        prepareStore(sequence);
        ScriptInfo<Integer> info = createInfo();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        info.setId(12);
        info.setVersion("1");
        // When
        assertThatThrownBy(() -> store.update(info)).isInstanceOf(ScriptStoreException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testUpdate_ConcurrentModification(boolean sequence) {
        // Given
        prepareStore(sequence);
        ScriptInfo<Integer> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        String version = info.getVersion();
        info = store.update(info);
        info.setVersion(version);
        info.setStatus(ScriptStatus.FAILED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        ScriptInfo<Integer> info2 = info;
        // When
        assertThatThrownBy(() -> store.update(info2)).isInstanceOf(ScriptStoreException.class);
    }
}
