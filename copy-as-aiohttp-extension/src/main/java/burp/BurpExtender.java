package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class BurpExtender implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Copy as Python aiohttp");
        api.userInterface().registerContextMenuItemsProvider(new MyContextMenuItemsProvider(api));
        api.extension().registerUnloadingHandler(
                () -> api.logging().logToOutput("'Copy as Python aiohttp' extension unloaded!"));
        api.logging().logToOutput("'Copy as Python aiohttp' extension loaded!");
    }
}
