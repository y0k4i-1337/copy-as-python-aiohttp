package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
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

            // Create sub-menu items
            JMenu withImportsMenu = new JMenu("Copy with imports");
            JMenuItem copyRequestsWithImportsItem = new JMenuItem("Copy requests");
            JMenuItem copyRequestsWithSessionWithImportsItem =
                    new JMenuItem("Copy requests with session object");

            JMenu passwdSprayTemplateMenu = new JMenu("Password spraying template");
            JMenuItem copyRequestsPasswdSprayItem = new JMenuItem("Copy requests");
            JMenuItem copyRequestsWithSessionPasswdSprayItem =
                    new JMenuItem("Copy requests with session object");



            // Add sub-menu items to the sub-menus
            withImportsMenu.add(copyRequestsWithImportsItem);
            withImportsMenu.add(copyRequestsWithSessionWithImportsItem);

            passwdSprayTemplateMenu.add(copyRequestsPasswdSprayItem);
            passwdSprayTemplateMenu.add(copyRequestsWithSessionPasswdSprayItem);

            // Add items to the menu
            menuItemList.add(copyRequestsItem);
            menuItemList.add(copyRequestsWithSessionItem);
            menuItemList.add(withImportsMenu);
            menuItemList.add(passwdSprayTemplateMenu);

            return menuItemList;
        }

        return Collections.emptyList();
    }
}
