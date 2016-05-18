package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.config.ConfigurationLoader;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.Pipelines;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    @Option(name = "--help", aliases = {"-h"}, usage = "Display help", help = true)
    public boolean help = false;
    @Option(name = "--files", aliases = {"-f"}, usage = "Files: exemple /folder/**/prefix*.ext")
    public String files;
    @Option(name = "--type", aliases = {"-t"}, usage = "Connection type: sql, http, elasticsearch")
    public String type;
    @Option(name = "--url", aliases = {"-d"}, usage = "Connection URL")
    public String url;
    @Option(name = "--username", aliases = {"-u"}, usage = "Connection username")
    public String username;
    @Option(name = "--password", aliases = {"-p"}, usage = "Connection password")
    public String password;
    @Option(name = "--store", aliases = {"-s"}, usage = "Table/collection/index to store ran script and not execute them again")
    public String store;
    @Option(name = "--config", aliases = {"-c"}, usage = "YAML Configuration file location")
    public String configurationFile;


    public void run() throws IOException, CmdLineException {
        PipelineBuilder pipelineBuilder = createPipelineBuilder();
        pipelineBuilder.scan();
    }

    PipelineBuilder createPipelineBuilder() throws CmdLineException {
        PipelineBuilder pipelineBuilder;
        if (configurationFile ==null) {
            pipelineBuilder = Pipelines.create(type);
        } else {
            ConfigurationLoader configurationLoader = new ConfigurationLoader();
            pipelineBuilder = configurationLoader.loadFile(Paths.get(configurationFile));
        }
        if (url != null) pipelineBuilder.withUrl(url);
        if (username != null) pipelineBuilder.withUsername(username);
        if (password != null) pipelineBuilder.withPassword(password);
        if (store != null) pipelineBuilder.withScriptStore(store);
        if (files != null) pipelineBuilder.withFilesScriptScanner(files);
        pipelineBuilder.withEndConsumer(new PrintConsumer<>());
        return pipelineBuilder;
    }


    public static void main(String ... args) {
        System.exit(doMain(args));
    }

    static int  doMain(String ... args) {
        Main main = new Main();
        CmdLineParser parser = new CmdLineParser(main);
        try {
            parser.parseArgument(args);
            if (main.help) {
                parser.printUsage(System.out);
                return 0;
            }
            System.out.println("Args: " + Arrays.stream(args).collect(Collectors.joining(" ")));
            main.run();
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
