package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;

import static com.github.gquintana.beepbeep.sql.TestSqlConnectionProviders.createSqlConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SqlScriptStoreTest {
    private SingleSqlConnectionProvider connectionProvider;
    private SqlScriptStore store;
    private final boolean sequence;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{false}, {true}});
    }

    public SqlScriptStoreTest(boolean sequence) {
        this.sequence = sequence;
    }

    @Before
    public void setUp() {
        connectionProvider = new SingleSqlConnectionProvider(
            createSqlConnectionProvider());
        store = new SqlScriptStore(connectionProvider, "beepbeep", sequence);
        store.prepare();
    }

    @After
    public void tearDown() throws Exception {
        try(Connection connection= connectionProvider.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE beepbeep");
            if (sequence) {
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

    @Test
    public void testCreate() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getVersion()).isEqualTo(1);
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @Test(expected = ScriptStoreException.class)
    public void testCreate_AlreadyExists() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        info = store.create(info);
        // When
        info = store.create(info);
    }

    @Test
    public void testUpdate() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
        // Then
        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getVersion()).isEqualTo(2);
        ScriptInfo<Integer> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_NotFound() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        info.setId(12);
        info.setVersion(1);
        // When
        info = store.update(info);
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_ConcurrentModification() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
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
