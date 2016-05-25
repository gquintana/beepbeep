package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.util.ParametersBuilder;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.List;

public class ScriptEndEventEqualsTest extends BaseEqualsHashcodeTest<ScriptEvent> {
    public ScriptEndEventEqualsTest(ScriptEvent actual, ScriptEvent expected, boolean equals) {
        super(actual, expected, equals);
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Script script1 = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        Script script2 = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        Exception exc1 = new NullPointerException();
        Exception exc2 = new IllegalArgumentException();
        Instant start = Instant.now();
        return new ParametersBuilder()
            .add(new ScriptEndEvent(script1, 1, start), new ScriptEndEvent(script1, 1, start), true)
            .add(new ScriptEndEvent(script1, 1, start), new ScriptEndEvent(script1, 2, start), false)
            .add(new ScriptEndEvent(script1, 1, start), new ScriptEndEvent(script2, 1, start), false)
            .add(new ScriptEndEvent(script1, 1, start), new ScriptEndEvent(script1, 1, exc1, start), false)
            .add(new ScriptEndEvent(script1, 1, exc1, start), new ScriptEndEvent(script1, 1, exc2, start), false)
            .add(new ScriptEndEvent(script1, 1, start), new LineEvent(script1, 1, "Line 1"), false)
            .build();
    }

}
