package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


/**
 * BurpExtender class to register
 * the extension with Burp Suite
 */
public class BurpExtender implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Copy as Python aiohttp");
        api.userInterface().registerContextMenuItemsProvider(new MyContextMenuItemsProvider(api));
        api.logging().logToOutput("'Copy as Python aiohttp' extension loaded!");
        api.logging().logToOutput("Remember to install the required dependencies with:");
        api.logging().logToOutput("pip install 'aiohttp[speedups]' aiofiles aiocsv");
    }
}
