package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

/**
 * Fake pipeline builder for testing purpose
 */
public class TestPipelineBuilder extends PipelineBuilder<TestPipelineBuilder> {
    public TestPipelineBuilder withClass(Class clazz) {
        return self();
    }

    @Override
    public TestPipelineBuilder withScriptStore(String name) {
        return self();
    }

    @Override
    public Consumer<ScriptStartEvent> createConsumers() {
        return null;
    }
}
