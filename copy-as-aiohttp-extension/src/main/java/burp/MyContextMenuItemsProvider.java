package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyContextMenuItemsProvider implements ContextMenuItemsProvider {
    private final MontoyaApi api;

    public MyContextMenuItemsProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.PROXY, ToolType.INTRUDER, ToolType.REPEATER,
                ToolType.LOGGER)) {
            final List<HttpRequestResponse> messages = event.selectedRequestResponses();
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
            copyRequestsItem.addActionListener(e -> copyMessages(messages));

            return menuItemList;
        }

        return Collections.emptyList();
    }


    private void copyMessages(List<HttpRequestResponse> messages) {
        StringBuilder sb = new StringBuilder();
        final String cookieString = "Cookie";
        int reqIdx = 0;

        for (HttpRequestResponse message : messages) {
            String prefix = "req" + reqIdx + "_";
            sb.append("async def " + prefix + "fetch():\n");
            HttpRequest request = message.request();
            sb.append(indent(1) + "url = '" + request.url() + "'\n");
            sb.append(indent(1) + "method = '" + request.method() + "'\n");
            // Check for cookies
            if (request.hasHeader(cookieString)) {
                sb.append(indent(1) + "cookies = {\n");
                for (String cookie : request.header("Cookie").value().split(";")) {
                    String[] parts = cookie.split("=", 2);
                    sb.append(
                            indent(2) + "'" + parts[0].trim() + "': '" + escapeQuotes(parts[1].trim()) + "',\n");
                }
                sb.append(indent(1) + "}\n");
            } else {
                sb.append(indent(1) + "cookies = None\n");
            }
            // Check for headers other than cookies
            if ((request.hasHeader(cookieString) && request.headers().size() > 1)
                    || (!request.hasHeader(cookieString) && !request.headers().isEmpty())) {
                sb.append(indent(1) + "headers = {\n");
                for (HttpHeader header : request.headers()) {
                    if (!header.name().equalsIgnoreCase(cookieString)) {
                        sb.append(
                                indent(2) + "'" + header.name() + "': '" + escapeQuotes(header.value()) + "',\n");
                    }
                }
                sb.append(indent(1) + "}\n");
            } else {
                sb.append(indent(1) + "headers = None\n");
            }
            // Chef if there is a body
            if (request.body().length() > 0) {
                sb.append(indent(1) + "data = '" + escapeQuotes(request.bodyToString()) + "'\n");
            } else {
                sb.append(indent(1) + "data = None\n");
            }
            // Make the request
            sb.append(indent(1)
                    + "async with aiohttp.request(method, url, headers=headers, cookies=cookies, data=data) as response:\n");
            sb.append(indent(2)
                    + "return (response.status, response.headers, response.cookies, await response.text())\n\n");

            reqIdx++;
        }
        // Copy the script to the clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection (sb.toString()), null);
    }


    private String indent(int level) {
        return "    ".repeat(level);
    }


    private static String escapeQuotes(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r",
                "\\r");
    }


}
