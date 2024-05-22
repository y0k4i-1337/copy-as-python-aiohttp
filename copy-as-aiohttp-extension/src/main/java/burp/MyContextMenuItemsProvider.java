package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MyContextMenuItemsProvider implements ContextMenuItemsProvider {
    private final MontoyaApi api;

    public MyContextMenuItemsProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.PROXY, ToolType.INTRUDER, ToolType.REPEATER,
                ToolType.LOGGER)) {
            final List<HttpRequestResponse> messages;
            if (!event.selectedRequestResponses().isEmpty()) {
                messages = event.selectedRequestResponses();
            } else {
                Optional<MessageEditorHttpRequestResponse> message =
                        event.messageEditorRequestResponse();
                messages = message.isPresent() ? List.of(message.get().requestResponse())
                        : Collections.emptyList();
            }

            // Create a list of menu items
            List<Component> menuItemList = new ArrayList<>();

            // Create main menu items
            JMenuItem copyRequestsItem = new JMenuItem("Copy requests");
            JMenuItem copyRequestsWithSessionItem =
                    new JMenuItem("Copy requests with session object");
            JMenuItem passwdSprayTemplateItem =
                    new JMenuItem("Generate password spraying template");

            // Create sub-menu items
            JMenu generateScriptMenu = new JMenu("Generate script");
            JMenuItem copyRequestsOnScriptItem = new JMenuItem("Copy requests");
            JMenuItem copyRequestsWithSessionOnScriptItem =
                    new JMenuItem("Copy requests with session object");

            // Add sub-menu items to the sub-menus
            generateScriptMenu.add(copyRequestsOnScriptItem);
            generateScriptMenu.add(copyRequestsWithSessionOnScriptItem);

            // Add items to the menu
            menuItemList.add(copyRequestsItem);
            menuItemList.add(copyRequestsWithSessionItem);
            menuItemList.add(generateScriptMenu);
            menuItemList.add(passwdSprayTemplateItem);

            // Add action listeners
            copyRequestsItem.addActionListener(e -> copyRequests(messages));

            return menuItemList;
        }

        return Collections.emptyList();
    }


    /**
     * Copy the selected requests to the clipboard as Python functions
     *
     * @param messages The selected requests
     * @return void
     */
    private void copyRequests(List<HttpRequestResponse> messages) {
        StringBuilder sb = new StringBuilder();
        int reqIdx = 0;

        sb.append("# Generated by \"Copy as Python aiohttp\" Burp extension\n");
        for (HttpRequestResponse message : messages) {
            HttpRequest request = message.request();
            sb.append(requestToFunction(request, 0, false, Optional.of(reqIdx)));
            // Add 2 new lines between requests
            if (reqIdx < messages.size() - 1) {
                sb.append("\n\n");
            }
            reqIdx++;
        }
        copyToClipboard(sb);
    }


    /**
     * Convert a request to a Python function
     *
     * @param request The request to convert
     * @param baseIndent The base indentation level
     * @param session If the request should be part of a session
     * @param requestIndex The index of the request (optional)
     * @return The Python function
     */
    private String requestToFunction(HttpRequest request, int baseIndent, boolean session,
            Optional<Integer> requestIndex) {
        StringBuilder sb = new StringBuilder();
        String prefix = requestIndex.isPresent() ? "req" + requestIndex.get() + "_" : "req_";

        if (session) {
            sb.append(indent(baseIndent) + "async def " + prefix + "fetch(client):\n");
        } else {
            sb.append(indent(baseIndent) + "async def " + prefix + "fetch():\n");
        }
        sb.append(indent(baseIndent + 1) + "url = '" + request.url() + "'\n");
        sb.append(indent(baseIndent + 1) + "method = '" + request.method() + "'\n");
        // Check for cookies
        sb.append(parseCookies(request, baseIndent + 1));
        // Check for headers other than cookies
        sb.append(parseHeaders(request, baseIndent + 1));
        // Chef if there is a body
        if (request.body().length() > 0) {
            sb.append(indent(baseIndent + 1) + "data = '" + escapeQuotes(request.bodyToString())
                    + "'\n");
        } else {
            sb.append(indent(baseIndent + 1) + "data = None\n");
        }
        // Make the request
        if (session) {
            sb.append(indent(baseIndent + 1)
                    + "async with client.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False) as response:\n");
        } else {
            sb.append(indent(baseIndent + 1)
                    + "async with aiohttp.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False) as response:\n");
        }
        sb.append(indent(baseIndent + 2)
                + "return (response.status, response.headers, response.cookies, await response.text())\n");
        return sb.toString();
    }


    /**
     * Parse the cookies from the request
     *
     * @param request The request to parse
     * @param baseIndent The base indentation level
     * @return The cookies as a string
     */
    private String parseCookies(HttpRequest request, int baseIndent) {
        StringBuilder sb = new StringBuilder();
        final String cookieString = "Cookie";
        if (request.hasHeader(cookieString)) {
            sb.append(indent(baseIndent) + "cookies = {\n");
            for (String cookie : request.header("Cookie").value().split(";")) {
                String[] parts = cookie.split("=", 2);
                sb.append(indent(baseIndent + 1) + "'" + parts[0].trim() + "': '"
                        + escapeQuotes(parts[1].trim()) + "',\n");
            }
            sb.append(indent(baseIndent) + "}\n");
        } else {
            sb.append(indent(baseIndent) + "cookies = None\n");
        }
        return sb.toString();
    }


    /**
     * Parse the headers from the request
     *
     * @param request The request to parse
     * @param baseIndent The base indentation level
     * @return The headers as a string
     */
    private String parseHeaders(HttpRequest request, int baseIndent) {
        StringBuilder sb = new StringBuilder();
        final String cookieString = "Cookie";

        if ((request.hasHeader(cookieString) && request.headers().size() > 1)
                || (!request.hasHeader(cookieString) && !request.headers().isEmpty())) {
            sb.append(indent(baseIndent) + "headers = {\n");
            for (HttpHeader header : request.headers()) {
                if (!header.name().equalsIgnoreCase(cookieString)) {
                    sb.append(indent(baseIndent + 1) + "'" + header.name() + "': '"
                            + escapeQuotes(header.value()) + "',\n");
                }
            }
            sb.append(indent(baseIndent) + "}\n");
        } else {
            sb.append(indent(baseIndent) + "headers = None\n");
        }
        return sb.toString();
    }


    /**
     * Copy the given text to the clipboard
     *
     * @param text The text to copy
     * @return void
     */
    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text),
                null);
    }


    /**
     * Copy the given text to the clipboard
     *
     * @param text The text to copy
     * @return void
     */
    private void copyToClipboard(StringBuilder text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text.toString()), null);
    }


    /**
     * Return a string with white spaces to indent the code
     *
     * @param level The level of indentation
     * @return Indent level multiplied by 4 spaces
     */
    private String indent(int level) {
        return "    ".repeat(level);
    }


    /**
     * Escape quotes in a string
     *
     * @param value The string to escape
     * @return The escaped string
     */
    private static String escapeQuotes(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r",
                "\\r");
    }


}
