package com.github.gquintana.beepbeep.file;

import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.util.Maps;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.gquintana.beepbeep.util.Converters.convert;

/**
 * Store ran script in a YAML file
 */
public class YamlFileScriptStore extends FileScriptStore {
    private final Yaml yaml;

    public YamlFileScriptStore(Path file) {
        super(file);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    @Override
    protected Stream<ScriptInfo<Integer>> load() throws IOException {
        if (!Files.exists(getFile())) {
            return Stream.empty();
        }
        try (Reader reader= new InputStreamReader(Files.newInputStream(getFile()), StandardCharsets.UTF_8)) {
            List<Map<String, Object>> marshalled = yaml.load(reader);
            return  marshalled.stream().map(this::unmarshall);
        }
    }

    private ScriptInfo<Integer> unmarshall(Map<String, Object> map) {
        return new ScriptInfo<>(
            convert(map.get("id"), Integer.class),
            convert(map.get("version"), Integer.class),
            convert(map.get("full_name"), String.class),
            convert(map.get("size"), Long.class),
            convert(map.get("sha1"), String.class),
            convert(map.get("start_date"), Instant.class),
            convert(map.get("end_date"), Instant.class),
            convert(map.get("status"), ScriptStatus.class)
        );
    }

    @Override
    protected void save(List<ScriptInfo<Integer>> infos) throws IOException{
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(getFile()), StandardCharsets.UTF_8)) {
            List<Map<String, Object>> marshalled = infos.stream().map(this::marshal).collect(Collectors.toList());
            yaml.dump(marshalled, writer);
        }
    }

    private Map<String, Object> marshal(ScriptInfo<Integer> info) {
        Maps.Builder<String, Object> mapBuilder = Maps.<String, Object>builder()
            .put("id", info.getId())
            .put("full_name", info.getFullName())
            .put("version", info.getVersion())
            .put("size", info.getSize())
            .put("sha1", info.getSha1())
            .put("status", info.getStatus().name())
            .put("start_date", info.getStartDate().toString());
        if (info.getEndDate() != null) {
            mapBuilder.put("end_date", info.getEndDate().toString());
        }
        return mapBuilder.build();
    }
}
