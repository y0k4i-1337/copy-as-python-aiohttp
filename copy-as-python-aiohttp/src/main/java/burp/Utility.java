package burp;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import org.json.JSONObject;


/**
 * Utility class with helper methods
 */
public class Utility {

    /**
     * Private constructor to prevent instantiation
     */
    private Utility() {}

    /**
     * Copy the given text to the clipboard
     *
     * @param text The text to copy
     */
    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),
                null);
    }

    /**
     * Copy the given text to the clipboard
     *
     * @param text The text to copy
     */
    public static void copyToClipboard(StringBuilder text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text.toString()), null);
    }

    /**
     * Return a string with white spaces to indent the code
     *
     * @param level The level of indentation
     * @return Indent level multiplied by 4 spaces
     */
    public static String indent(int level) {
        return "    ".repeat(level);
    }

    /**
     * Escape quotes in a string
     *
     * @param value The string to escape
     * @return The escaped string
     */
    public static String escapeQuotes(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r",
                "\\r");
    }

    /**
     * Convert a JSONObject to a string representation
     *
     * @param jsonObject The JSONObject to convert
     * @param baseIndentLevel The base indentation level
     * @param prefix The prefix to add to the string
     * @return The string representation of the JSONObject
     */
    public static String convertToJsonString(JSONObject jsonObject, int baseIndentLevel,
            Optional<String> prefix) {
        if (jsonObject.isEmpty()) {
            return indent(baseIndentLevel) + prefix.orElse("") + "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indent(baseIndentLevel) + prefix.orElse("") + "{\n");

        // Iterate over each key-value pair in the JSONObject
        for (String key : jsonObject.keySet()) {
            sb.append(indent(baseIndentLevel + 1) + "'").append(escapeQuotes(key)).append("': ");

            // Check the type of value
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                // If value is another JSONObject, recursively call the function
                sb.append("\n" + convertToJsonString((JSONObject) value, baseIndentLevel + 2,
                        Optional.of("")));
            } else if (value instanceof String) {
                // If value is a string, add quotes around it
                sb.append("'").append(escapeQuotes((String) value)).append("'");
            } else {
                // Otherwise, just add the value
                sb.append(value);
            }
            sb.append(",\n");
        }

        sb.append(indent(baseIndentLevel) + "}\n");

        return sb.toString();
    }
}
