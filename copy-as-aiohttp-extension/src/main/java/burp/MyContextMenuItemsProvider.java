package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MyContextMenuItemsProvider implements ContextMenuItemsProvider {

    public MyContextMenuItemsProvider(MontoyaApi api) {}

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
            copyRequestsItem.addActionListener(e -> copyRequests(messages, false));
            copyRequestsWithSessionItem.addActionListener(e -> copyRequests(messages, true));
            copyRequestsOnScriptItem.addActionListener(e -> copyRequestsOnScript(messages, false));
            copyRequestsWithSessionOnScriptItem
                    .addActionListener(e -> copyRequestsOnScript(messages, true));
            passwdSprayTemplateItem.addActionListener(e -> generatePasswdSprayTemplate(messages));

            return menuItemList;
        }

        return Collections.emptyList();
    }

    /**
     * Copy the selected requests to the clipboard as Python functions
     *
     * @param messages The selected requests
     * @param session Whether to include a session object
     * @return void
     */
    private void copyRequests(List<HttpRequestResponse> messages, boolean session) {
        ReqParser parser = new ReqParser(messages);
        String output = parser.asPythonScript(session, false, false);
        Utility.copyToClipboard(output);
    }

    /**
     * Generate a Python script with the selected requests
     *
     * @param messages The selected requests
     * @param session Whether to include a session object
     * @return void
     */
    private void copyRequestsOnScript(List<HttpRequestResponse> messages, boolean session) {
        ReqParser parser = new ReqParser(messages);
        String output = parser.asPythonScript(session, true, true);
        Utility.copyToClipboard(output);
    }

    /**
     * Generate a password spraying template
     *
     * @param messages The selected requests
     * @return void
     */
    private void generatePasswdSprayTemplate(List<HttpRequestResponse> messages) {
        ReqParser parser = new ReqParser(messages);
        String script = parser.asPasswordSprayingTemplate();
        Utility.copyToClipboard(script);
    }

}
