package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.util.ParametersBuilder;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Instant;
import java.util.stream.Stream;

public class ResultEventEqualsTest extends BaseEqualsHashcodeTest<ScriptEvent>{

    static Stream<Arguments> getParameters() {
        Script script1 = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        Script script2 = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        return new ParametersBuilder()
            .add(new ResultEvent(script1, 1, "Line 1"), new ResultEvent(script1, 1, "Line 1"), true)
            .add(new ResultEvent(script1, 1, "Line 1"), new ResultEvent(script1, 2, "Line 1"), false)
            .add(new ResultEvent(script1, 1, "Line 1"), new ResultEvent(script2, 1, "Line 1"), false)
            .add(new ResultEvent(script1, 1, "Line 1"), new ResultEvent(script1, 1, "Line 2"), false)
            .add(new ResultEvent(script1, 1, "Line 1"), new ScriptEndEvent(script1, 1, Instant.now().minusMillis(10L)), false)
            .build();
    }

}
