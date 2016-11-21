package com.github.gquintana.beepbeep.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
public class MapsTest {

    @Test
    public void testFlatten() throws Exception {
        // Given
        Map<String, Object> map = Maps.<String, Object>builder()
            .put("K1", "V1")
            .put("K2", Maps.builder()
                .put("K2.1", "V2.1")
                .put("K2.2", "V2.2")
                .build()
            )
            .put("K3", Arrays.asList("V3.1", "V3.2"))
            .put("K4", Maps.builder()
                .put("K4.1", Maps.builder()
                    .put("K4.1.1", "V4.1.1")
                    .build())
                .put("K4.2", Arrays.asList("V4.2.1"))
                .build()
            )
            .build();
        // When
        Map<String, Object> flatMap = Maps.flatten(map, null);
        // Then
        assertThat(flatMap).contains(
            entry("K1", "V1"),
            entry("K2.K2.1", "V2.1"), entry("K2.K2.2", "V2.2"),
            entry("K3.0", "V3.1"), entry("K3.1", "V3.2"),
            entry("K4.K4.1.K4.1.1", "V4.1.1"),
            entry("K4.K4.2.0", "V4.2.1")
        );
    }
    @Test
    public void testFlattenPrefix() throws Exception {
        // Given
        Map<String, Object> map = Maps.<String, Object>builder()
            .put("K1", "V1")
            .put("K2", Maps.builder()
                .put("K2.1", "V2.1")
                .put("K2.2", "V2.2")
                .build()
            )
            .build();
        // When
        Map<String, Object> flatMap = Maps.flatten(map, "PFX");
        // Then
        assertThat(flatMap).contains(
            entry("PFX.K1", "V1"),
            entry("PFX.K2.K2.1", "V2.1"), entry("PFX.K2.K2.2", "V2.2")
        );
    }
}
