package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.util.ParametersBuilder;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.List;

public class LineEventEqualsTest extends BaseEqualsHashcodeTest<ScriptEvent> {
    public LineEventEqualsTest(ScriptEvent actual, ScriptEvent expected, boolean equals) {
        super(actual, expected, equals);
    }

    @Parameterized.Parameters
    public static List<Object[]> getParameters() {
        Script script1 = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        Script script2 = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        return new ParametersBuilder()
            .add(new LineEvent(script1, 1, "Line 1"), new LineEvent(script1, 1, "Line 1"), true)
            .add(new LineEvent(script1, 1, "Line 1"), new LineEvent(script1, 2, "Line 1"), false)
            .add(new LineEvent(script1, 1, "Line 1"), new LineEvent(script2, 1, "Line 1"), false)
            .add(new LineEvent(script1, 1, "Line 1"), new ScriptEndEvent(script1, 1, Instant.now().minusMillis(10L)), false)
            .build();
    }
}
