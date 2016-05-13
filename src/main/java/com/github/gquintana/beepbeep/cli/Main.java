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
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    @Option(name = "--help", aliases = {"-h"}, usage = "Display help", help = true)
    public boolean help = false;
    @Option(name = "--files", aliases = {"-f"}, usage = "Files: exemple /folder/**/prefix*.ext", required = true)
    public String files;
    @Option(name = "--type", aliases = {"-t"}, usage = "Connection type: sql, http, elasticsearch", required = true)
    public String type;
    @Option(name = "--url", aliases = {"-d"}, usage = "Connection URL", required = true)
    public String url;
    @Option(name = "--username", aliases = {"-u"}, usage = "Connection username")
    public String username;
    @Option(name = "--password", aliases = {"-p"}, usage = "Connection password")
    public String password;
    @Option(name = "--store", aliases = {"-s"}, usage = "Table/collection/index to store ran script and not execute them again")
    public String store;


    public void run(CmdLineParser cmdLineParser) throws IOException, CmdLineException {
        PipelineBuilder pipelineBuilder = createPipelineBuilder(cmdLineParser);
        pipelineBuilder.scan();
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
        }
        pipelineBuilder.withFilesScriptScanner(files);
        return pipelineBuilder;
    }


    public static void main(String[] args) {
        System.exit(doMain(args));
    }

    static int  doMain(String[] args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            if (main.help) {
                parser.printUsage(System.out);
                return 0;
            }
            System.out.println("Args: " + Arrays.stream(args).collect(Collectors.joining(" ")));
            main.run(parser);
            return 0;
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return 1;
        } catch (IOException | BeepBeepException e) {
            e.printStackTrace(System.err);
            return 2;
        }
    }
}
