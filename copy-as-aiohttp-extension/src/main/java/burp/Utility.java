package burp;

import java.awt.*;
import java.awt.datatransfer.StringSelection;


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
     * @return void
     */
    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),
                null);
    }

    /**
     * Copy the given text to the clipboard
     *
     * @param text The text to copy
     * @return void
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
}
