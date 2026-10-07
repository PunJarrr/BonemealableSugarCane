package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class YamlTemplateTest {

    private static final String TEMPLATE = """
            # header comment
            a:
              # comment about b
              b: 1
              nested:
                deep: false
              text: "x"

            c:
              b: 2
            """;

    @Test
    void keepsTemplateWhenNoValues() {
        assertEquals(TEMPLATE, YamlTemplate.render(TEMPLATE, Map.of()));
    }

    @Test
    void replacesValuesAtAnyDepthAndKeepsComments() {
        String out = YamlTemplate.render(TEMPLATE, Map.of("a.b", 5, "a.nested.deep", true, "c.b", 7));
        assertEquals("""
                # header comment
                a:
                  # comment about b
                  b: 5
                  nested:
                    deep: true
                  text: "x"

                c:
                  b: 7
                """, out);
    }

    @Test
    void sameKeyNameInDifferentSectionsIsNotConfused() {
        String out = YamlTemplate.render(TEMPLATE, Map.of("c.b", 9));
        assertEquals(1, load(out).getInt("a.b"));
        assertEquals(9, load(out).getInt("c.b"));
    }

    @Test
    void ignoresUnknownKeysAndNonScalars() {
        String out = YamlTemplate.render(TEMPLATE, Map.of("a.unknown", 1, "a.b", List.of(1, 2)));
        assertEquals(TEMPLATE, out);
    }

    @Test
    void stringsRoundTripThroughYaml() {
        String tricky = "&cQuote \" backslash \\ new\nline 'single' #hash: colon";
        String out = YamlTemplate.render(TEMPLATE, Map.of("a.text", tricky));
        assertEquals(tricky, load(out).getString("a.text"));
    }

    @Test
    void toYamlRejectsUnsupportedTypes() {
        assertNull(YamlTemplate.toYaml(List.of("x")));
        assertEquals("3.5", YamlTemplate.toYaml(3.5));
    }

    private static YamlConfiguration load(String text) {
        return YamlConfiguration.loadConfiguration(new java.io.StringReader(text));
    }
}
