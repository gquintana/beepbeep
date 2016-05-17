package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.util.ParametersBuilder;
import org.junit.runners.Parameterized;

import java.util.List;

public class ScriptStartEventEqualsTest extends BaseEqualsHashcodeTest<ScriptEvent> {
    public ScriptStartEventEqualsTest(ScriptEvent actual, ScriptEvent expected, boolean equals) {
        super(actual, expected, equals);
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Script script1 = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        Script script2 = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        return new ParametersBuilder()
            .add(new ScriptStartEvent(script1), new ScriptStartEvent(script1), true)
            .add(new ScriptStartEvent(script1), new ScriptStartEvent(script2), false)
            .add(new ScriptStartEvent(script1), new ScriptEndEvent(script1, 1), false)
            .build();
    }

}
