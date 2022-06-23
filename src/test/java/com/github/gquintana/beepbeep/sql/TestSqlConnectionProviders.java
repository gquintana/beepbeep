package com.github.gquintana.beepbeep.sql;

import java.nio.file.Path;

public class TestSqlConnectionProviders {
    public static DriverSqlConnectionProvider createSqlConnectionProvider(Path folder) {
        return DriverSqlConnectionProvider.create("jdbc:h2:file:" + folder.toAbsolutePath(), "sa", "");
    }

    public static DriverSqlConnectionProvider createSqlConnectionProvider() {
        return DriverSqlConnectionProvider.create("jdbc:h2:mem:test", "sa", "");
    }
    /*
     * To Test with MySQL
     * - docker run --name beepbeep-mysql  -e MYSQL_ROOT_PASSWORD=beepbeep -e MYSQL_DATABASE=beepbeep -e MYSQL_USER=beepbeep -e MYSQL_PASSWORD=beepbeep -v $(pwd)/target/mysql:/var/lib/mysql -p 3306:3306 -d mysql:5.7
     * - return DriverSqlConnectionProvider.create("jdbc:mysql://172.17.0.1:3306/beepbeep", "beepbeep", "beepbeep");
     * To Test with PostgreSQL
     * - docker run --name beepbeep-postgresql  -e POSTGRES_DB=beepbeep -e POSTGRES_USER=beepbeep -e POSTGRES_PASSWORD=beepbeep -v $(pwd)/target/postgres:/var/lib/postgresql -p 5432:5432 -d postgres:9.5
     * - return DriverSqlConnectionProvider.create("jdbc:postgresql://172.17.0.1:5432/beepbeep", "beepbeep", "beepbeep");
     */
}
