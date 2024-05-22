package burp;

import java.util.List;
import java.util.Optional;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

public class ReqParser {
    private final List<HttpRequestResponse> messages;

    public ReqParser(List<HttpRequestResponse> messages) {
        this.messages = messages;
    }

    /**
     * Convert the requests to a Python script
     *
     * @param session If the requests should be part of a session
     * @param imports If the script should include imports
     * @param main If the script should include a main function
     * @return The Python script as a string
     */
    public String asPythonScript(boolean session, boolean imports, boolean main) {
        StringBuilder sb = new StringBuilder();

        if (imports) {
            if (main) {
                sb.append(buildImports("aiohttp", "asyncio"));
            } else {
                sb.append(buildImports("aiohttp"));
            }
            sb.append("\n\n");
        }

        sb.append(parseRequests(session, 0));

        if (main) {
            sb.append("\n\n");
            sb.append("async def main():\n");

            if (session) {
                sb.append(Utility.indent(1) + "async with aiohttp.ClientSession() as client:\n");
                for (int i = 0; i < this.messages.size(); i++) {
                    sb.append(Utility.indent(2) + "(status, headers, cookies, length, body) = await req" + i
                            + "_fetch(client)\n");
                    sb.append(Utility.indent(2) + "# TODO: add your logic here\n");
                }
            } else {
                for (int i = 0; i < messages.size(); i++) {
                    sb.append(Utility.indent(1) + "(status, headers, cookies, length, body) = await req" + i
                            + "_fetch()\n");
                    sb.append(Utility.indent(1) + "# TODO: add your logic here\n");
                }
            }

            sb.append("\n\n");
            sb.append("if __name__ == '__main__':\n");
            sb.append(Utility.indent(1) + "asyncio.run(main())\n");
        }

        return sb.toString();
    }

    /**
     * Build the imports for the Python script
     *
     * @return The imports as a string
     */
    private String buildImports(String... packages) {
        StringBuilder sb = new StringBuilder();
        for (String pkg : packages) {
            sb.append("import " + pkg + "\n");
        }
        return sb.toString();
    }

    /**
     * Parse the requests
     *
     * @param session If the requests should be part of a session
     * @param baseIndent The base indentation level
     * @return The requests as a string
     */
    private String parseRequests(boolean session, int baseIndent) {
        int reqIdx = 0;
        StringBuilder sb = new StringBuilder();

        for (HttpRequestResponse message : this.messages) {
            HttpRequest request = message.request();
            sb.append(requestToFunction(request, baseIndent, session, Optional.of(reqIdx)));
            // Add 2 new lines between requests
            if (reqIdx < messages.size() - 1) {
                sb.append("\n\n");
            }
            reqIdx++;
        }
        return sb.toString();
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
            sb.append(Utility.indent(baseIndent) + "async def " + prefix + "fetch(client):\n");
        } else {
            sb.append(Utility.indent(baseIndent) + "async def " + prefix + "fetch():\n");
        }
        sb.append(Utility.indent(baseIndent + 1) + "url = '" + request.url() + "'\n");
        sb.append(Utility.indent(baseIndent + 1) + "method = '" + request.method() + "'\n");
        // Check for cookies
        sb.append(parseCookies(request, baseIndent + 1));
        // Check for headers other than cookies
        sb.append(parseHeaders(request, baseIndent + 1));
        // Chef if there is a body
        if (request.body().length() > 0) {
            sb.append(Utility.indent(baseIndent + 1) + "data = '"
                    + Utility.escapeQuotes(request.bodyToString()) + "'\n");
        } else {
            sb.append(Utility.indent(baseIndent + 1) + "data = None\n");
        }
        // Make the request
        if (session) {
            sb.append(Utility.indent(baseIndent + 1)
                    + "async with client.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False) as response:\n");
        } else {
            sb.append(Utility.indent(baseIndent + 1)
                    + "async with aiohttp.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False) as response:\n");
        }
        sb.append(Utility.indent(baseIndent + 2)
                + "return (response.status, response.headers, response.cookies, response.headers.get('content-length', 0), await response.text())\n");
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
            sb.append(Utility.indent(baseIndent) + "cookies = {\n");
            for (String cookie : request.header(cookieString).value().split(";")) {
                String[] parts = cookie.split("=", 2);
                sb.append(Utility.indent(baseIndent + 1) + "'" + parts[0].trim() + "': '"
                        + Utility.escapeQuotes(parts[1].trim()) + "',\n");
            }
            sb.append(Utility.indent(baseIndent) + "}\n");
        } else {
            sb.append(Utility.indent(baseIndent) + "cookies = None\n");
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

        // Check how many headers are present excluding Cookie and
        // Content-Length
        int headerCount = 0;
        for (HttpHeader header : request.headers()) {
            if (!header.name().equalsIgnoreCase(cookieString)
                    && !header.name().equalsIgnoreCase("Content-Length")) {
                headerCount++;
            }
        }

        // Parse the headers
        if (headerCount > 0) {
            sb.append(Utility.indent(baseIndent) + "headers = {\n");
            for (HttpHeader header : request.headers()) {
                if (!header.name().equalsIgnoreCase(cookieString)
                        && !header.name().equalsIgnoreCase("Content-Length")) {
                    sb.append(Utility.indent(baseIndent + 1) + "'" + header.name() + "': '"
                            + Utility.escapeQuotes(header.value()) + "',\n");
                }
            }
            sb.append(Utility.indent(baseIndent) + "}\n");
        } else {
            sb.append(Utility.indent(baseIndent) + "headers = None\n");
        }
        return sb.toString();
    }
}
