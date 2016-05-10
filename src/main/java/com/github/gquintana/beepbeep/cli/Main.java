package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchPipelineBuilder;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import com.github.gquintana.beepbeep.store.ScriptStore;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

public class Main {
    @Option(name = "--files", aliases = {"-f"}, usage = "Files: exemple /folder/**/prefix*.ext", required = true)
    public String files;
    @Option(name = "--type", aliases = {"-t"}, usage = "Database type: sql, http", required = true)
    public String type;
    @Option(name = "--url", aliases = {"-d"}, usage = "Database URL", required = true)
    public String url;
    @Option(name = "--username", aliases = {"-u"}, usage = "Database URL")
    public String username;
    @Option(name = "--password", aliases = {"-p"}, usage = "Database password")
    public String password;
    @Option(name = "--store", aliases = {"-s"}, usage = "Table/collection/index to store ran script and not execute them again")
    public String store;


    public void run(CmdLineParser cmdLineParser) throws IOException, CmdLineException {
        PipelineBuilder pipelineBuilder = createPipelineBuilder(cmdLineParser);
        Consumer<ScriptStartEvent> scriptInput = pipelineBuilder.build();
        ScriptScanner scanner = ScriptScanners.files(files, scriptInput);
        scanner.scan();
    }

    PipelineBuilder createPipelineBuilder(CmdLineParser cmdLineParser) throws CmdLineException {
        PipelineBuilder pipelineBuilder = null;
        switch (type) {
            case "sql":
                pipelineBuilder = new SqlPipelineBuilder();
                break;
            case "http":
                pipelineBuilder = new HttpPipelineBuilder();
                break;
            case "elasticsearch":
                pipelineBuilder = new ElasticsearchPipelineBuilder();
                break;
            default:
                throw new CmdLineException(cmdLineParser, "Invalid type " + type, new IllegalArgumentException());
        }
        Consumer<ScriptEvent> lineOutput = new PrintConsumer<>();
        pipelineBuilder.withUrl(url)
            .withUsername(username)
            .withPassword(password)
            .withEndConsumer(lineOutput);
        if (store != null) {
            pipelineBuilder.withScriptStore(store);
            // Create table/index
            ScriptStore store = pipelineBuilder.getScriptStore();
            store.prepare();
        }
        return pipelineBuilder;
    }


    public static void main(String[] args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            main.run(parser);
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        } catch (IOException | BeepBeepException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }
}
