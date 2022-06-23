package com.github.gquintana.beepbeep.sql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverSqlConnectionProviderTest {
    @Test
    public void testCreate() {
        // When
        DriverSqlConnectionProvider connectionProvider = DriverSqlConnectionProvider.create("jdbc:h2:mem:test", "sa", null);
        // Then
        assertThat(connectionProvider.getDriverClass()).isEqualTo(org.h2.Driver.class);
        assertThat(connectionProvider.getUsername()).isEqualTo("sa");

    }



}
