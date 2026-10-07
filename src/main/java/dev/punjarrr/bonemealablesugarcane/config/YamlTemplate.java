package dev.punjarrr.bonemealablesugarcane.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills user values into the bundled config.yml text, line by line, so every comment,
 * blank line and the ASCII art survive (Bukkit's YAML writer would drop them).
 *
 * <p>Keys can be nested to any depth; the dotted path is built from indentation.
 * Only scalar values (booleans, numbers, strings) are supported. The template must not
 * contain lists, flow mappings or multi-line strings.
 */
final class YamlTemplate {

    // "  key: value" or "key:" (a section). Group 1 = indent, 2 = key, 3 = value (may be null).
    private static final Pattern KEY_LINE = Pattern.compile("^( *)([A-Za-z0-9_-]+):(?:[ \\t]+(.*))?$");

    private YamlTemplate() {}

    /**
     * Returns {@code template} with the value of every key that appears in {@code values}
     * replaced. Keys in {@code values} that the template doesn't contain are ignored, and
     * so are values that aren't scalars.
     */
    static String render(String template, Map<String, Object> values) {
        StringBuilder out = new StringBuilder(template.length() + 256);
        Deque<Integer> indents = new ArrayDeque<>();
        Deque<String> keys = new ArrayDeque<>();

        String[] lines = template.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher m = KEY_LINE.matcher(line);
            if (m.matches()) {
                int indent = m.group(1).length();
                String key = m.group(2);
                String value = m.group(3);

                while (!indents.isEmpty() && indents.peekLast() >= indent) {
                    indents.removeLast();
                    keys.removeLast();
                }

                if (value == null || value.isBlank()) {
                    indents.addLast(indent);
                    keys.addLast(key);
                } else {
                    String path = keys.isEmpty() ? key : String.join(".", keys) + "." + key;
                    String yaml = values.containsKey(path) ? toYaml(values.get(path)) : null;
                    if (yaml != null) {
                        line = m.group(1) + key + ": " + yaml;
                    }
                }
            }
            out.append(line);
            if (i < lines.length - 1) out.append('\n');
        }
        return out.toString();
    }

    /** Serializes a scalar as inline YAML, or returns {@code null} for unsupported types. */
    static String toYaml(Object value) {
        if (value instanceof Boolean || value instanceof Integer || value instanceof Long
                || value instanceof Double || value instanceof Float) {
            return value.toString();
        }
        if (value instanceof String s) {
            // Always double-quote: values starting with & (color codes) would otherwise be YAML anchors.
            return "\"" + s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t") + "\"";
        }
        return null;
    }
}
