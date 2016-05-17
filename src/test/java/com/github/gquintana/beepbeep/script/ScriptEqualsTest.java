package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.BaseEqualsHashcodeTest;
import com.github.gquintana.beepbeep.util.ParametersBuilder;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

public class ScriptEqualsTest extends BaseEqualsHashcodeTest<Script> {

    public ScriptEqualsTest(Script actual, Script expected, boolean equals) {
        super(actual, expected, equals);
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Script script1 = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        Script script2 = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        return new ParametersBuilder()
            .add(script1, script1, true)
            .add(script1, ResourceScript.create(TestFiles.class, "sql/init/01_create.sql"), true)
            .add(script2, ResourceScript.create(TestFiles.class, "sql/init/02_data.sql"), true)
            .add(script1, script2, false)
            .build();
    }

}
