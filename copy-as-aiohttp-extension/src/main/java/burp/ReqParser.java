package burp;

import java.util.List;
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
                sb.append(buildPreamble());
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
                    sb.append(Utility.indent(2)
                            + "(status, headers, cookies, length, body) = await req" + i
                            + "_fetch(client)\n");
                    sb.append(Utility.indent(2) + "# TODO: add your logic here\n");
                }
            } else {
                for (int i = 0; i < messages.size(); i++) {
                    sb.append(Utility.indent(1)
                            + "(status, headers, cookies, length, body) = await req" + i
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
     * Convert the requests to a Python script for password spraying
     *
     * @return The Python script as a string
     */
    public String asPasswordSprayingTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildPreamble());
        sb.append(buildImports("aiohttp", "aiofiles", "aiocsv", "argparse", "asyncio", "logging"));
        sb.append("\n\n");
        sb.append(buildGlobals());
        sb.append("\n\n");
        sb.append(parseRequests(true, 0));
        sb.append("\n\n");
        sb.append(buildSprayFunction(this.messages.size()));
        sb.append("\n\n");
        sb.append(buildMainFunction());
        sb.append("\n\n");
        sb.append("if __name__ == '__main__':\n");
        sb.append(Utility.indent(1) + "loop = asyncio.get_event_loop()\n");
        sb.append(Utility.indent(1) + "loop.run_until_complete(main())\n");
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
            sb.append(requestToFunction(request, baseIndent, session, reqIdx));
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
            int requestIndex) {
        StringBuilder sb = new StringBuilder();
        String prefix = "req" + requestIndex + "_";

        if (session) {
            sb.append(Utility.indent(baseIndent) + "async def " + prefix
                    + "fetch(client, ssl=True, proxy=None):\n");
        } else {
            sb.append(Utility.indent(baseIndent) + "async def " + prefix
                    + "fetch(ssl=True, proxy=None):\n");
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
                    + "async with client.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False, ssl=ssl, proxy=proxy) as response:\n");
        } else {
            sb.append(Utility.indent(baseIndent + 1)
                    + "async with aiohttp.request(method, url, headers=headers, cookies=cookies, data=data, allow_redirects=False, ssl=ssl, proxy=proxy) as response:\n");
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

    /**
     * Build the preamble for the Python script
     *
     * @return The preamble as a string
     */
    private String buildPreamble() {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n");
        sb.append("\"\"\"\n");
        sb.append("Template generated by \"Copy as Python aiohttp\" Burp extension.\n");
        sb.append("See: https://github.com/y0k4i-1337/copy-as-python-aiohttp\n");
        sb.append("\"\"\"\n");
        return sb.toString();
    }

    /**
     * Build the globals for the Python script
     *
     * @return The globals as a string
     */
    private String buildGlobals() {
        StringBuilder sb = new StringBuilder();
        sb.append("JAR_UNSAFE = True\n");
        sb.append("CLIENT_TOTAL_TIMEOUT = 60\n");
        sb.append("CONN_POOL_SIZE = 10\n");
        sb.append("CONN_TTL_DNS_CACHE = 300\n");
        sb.append("SSL_VERIFY = False\n");
        sb.append("LOG_LEVEL = logging.INFO\n\n");
        sb.append(
                "logging.basicConfig(level=LOG_LEVEL, format=\"%(asctime)s - %(levelname)s - %(message)s\")\n");
        sb.append("timeout = aiohttp.ClientTimeout(total=CLIENT_TOTAL_TIMEOUT)\n");
        sb.append("conn = aiohttp.TCPConnector(\n");
        sb.append(Utility.indent(1)
                + "limit_per_host=CONN_POOL_SIZE, ttl_dns_cache=CONN_TTL_DNS_CACHE\n");
        sb.append(")\n");
        return sb.toString();
    }

    /**
     * Build the spray function for the Python script
     *
     * @param messageCount The number of messages
     * @return The spray function as a string
     */
    private String buildSprayFunction(int messageCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("async def spray(username, password, proxy=None):\n");
        sb.append(Utility.indent(1) + "jar = aiohttp.CookieJar(unsafe=JAR_UNSAFE)\n");
        sb.append(Utility.indent(1) + "try:\n");
        sb.append(Utility.indent(2) + "async with aiohttp.ClientSession(\n");
        sb.append(Utility.indent(3)
                + "timeout=timeout, cookie_jar=jar, connector=conn, connector_owner=False\n");
        sb.append(Utility.indent(2) + ") as client:\n");
        for (int i = 0; i < messageCount; i++) {
            sb.append(Utility.indent(3) + "(status, headers, cookies, length, body) = await req" + i
                    + "_fetch(client, ssl=SSL_VERIFY, proxy=proxy)\n");
            sb.append(Utility.indent(3) + "# TODO: add your logic here\n");
        }
        sb.append(Utility.indent(3) + "result = {\n");
        sb.append(Utility.indent(4) + "'username': username,\n");
        sb.append(Utility.indent(4) + "'password': password,\n");
        sb.append(Utility.indent(4) + "'status': status,\n");
        sb.append(Utility.indent(4) + "'content-length': length,\n");
        sb.append(Utility.indent(3) + "}\n");
        sb.append(Utility.indent(1) + "except Exception as e:\n");
        sb.append(Utility.indent(2) + "result = {\n");
        sb.append(Utility.indent(3) + "'username': username,\n");
        sb.append(Utility.indent(3) + "'password': password,\n");
        sb.append(Utility.indent(3) + "'error': str(e),\n");
        sb.append(Utility.indent(2) + "}\n");
        sb.append(Utility.indent(1) + "return result\n");
        return sb.toString();
    }

    /**
     * Build the main function for the Python script
     *
     * @return The main function as a string
     */
    private String buildMainFunction() {
        StringBuilder sb = new StringBuilder();
        sb.append("async def main():\n");
        sb.append(Utility.indent(1)
                + "parser = argparse.ArgumentParser(description='Password spraying script')\n");
        sb.append(Utility.indent(1)
                + "parser.add_argument('-x', '--proxy', help='Proxy URL', default=None)\n");
        sb.append(Utility.indent(1) + "parser.add_argument(\n");
        sb.append(Utility.indent(2)
                + "'-e', '--errors', help='File to save errors', default='errors.txt'\n");
        sb.append(Utility.indent(1) + ")\n");
        sb.append(Utility.indent(1) + "parser.add_argument(\n");
        sb.append(Utility.indent(2)
                + "'-o', '--output', help='File to save successful requests', default='results.csv'\n");
        sb.append(Utility.indent(1) + ")\n");
        sb.append(Utility.indent(1)
                + "parser.add_argument('usernames', help='File containing usernames')\n");
        sb.append(Utility.indent(1)
                + "parser.add_argument('passwords', help='File containing passwords')\n");
        sb.append(Utility.indent(1) + "args = parser.parse_args()\n");
        sb.append(Utility.indent(1) + "usernames = []\n");
        sb.append(Utility.indent(1) + "passwords = []\n");
        sb.append(Utility.indent(1) + "requested = []\n");
        sb.append(Utility.indent(1) + "errors = []\n\n");
        sb.append(Utility.indent(1) + "async with aiofiles.open(args.usernames, mode='r') as f:\n");
        sb.append(Utility.indent(2) + "async for line in f:\n");
        sb.append(Utility.indent(3) + "usernames.append(line.strip())\n\n");
        sb.append(Utility.indent(1) + "async with aiofiles.open(args.passwords, mode='r') as f:\n");
        sb.append(Utility.indent(2) + "async for line in f:\n");
        sb.append(Utility.indent(3) + "passwords.append(line.strip())\n\n");
        sb.append(Utility.indent(1) + "tasks = []\n");
        sb.append(Utility.indent(1) + "for password in passwords:\n");
        sb.append(Utility.indent(2) + "for username in usernames:\n");
        sb.append(Utility.indent(3) + "tasks.append(spray(username, password, args.proxy))\n");
        sb.append(Utility.indent(1) + "logging.info('Spraying %d combinations', len(tasks))\n\n");
        sb.append(Utility.indent(1) + "results = await asyncio.gather(*tasks)\n");
        sb.append(Utility.indent(1) + "for result in results:\n");
        sb.append(Utility.indent(2) + "if 'error' in result.keys():\n");
        sb.append(Utility.indent(3) + "errors.append(result)\n");
        sb.append(Utility.indent(2) + "else:\n");
        sb.append(Utility.indent(3) + "requested.append(result)\n\n");
        sb.append(Utility.indent(1) + "if len(requested) > 0:\n");
        sb.append(Utility.indent(2) + "async with aiofiles.open(args.output, 'w') as f:\n");
        sb.append(Utility.indent(3) + "fieldnames = requested[0].keys()\n");
        sb.append(Utility.indent(3)
                + "writer = aiocsv.AsyncDictWriter(f, fieldnames=fieldnames, dialect='excel')\n");
        sb.append(Utility.indent(3) + "await writer.writeheader()\n");
        sb.append(Utility.indent(3) + "await writer.writerows(requested)\n");
        sb.append(Utility.indent(2)
                + "logging.info('Saved %d successful requests to %s', len(requested), args.output)\n");
        sb.append(Utility.indent(1) + "if len(errors) > 0:\n");
        sb.append(Utility.indent(2) + "async with aiofiles.open(args.errors, 'w') as f:\n");
        sb.append(Utility.indent(3) + "for error in errors:\n");
        sb.append(Utility.indent(4)
                + "await f.write(f\"{error['username']}:{error['password']} - {error['error']}\")\n");
        sb.append(Utility.indent(2)
                + "logging.info('Saved %d errors to %s', len(errors), args.errors)\n\n");
        sb.append(Utility.indent(1) + "logging.info('Completed tasks: %d', len(requested))\n");
        sb.append(Utility.indent(1) + "logging.info('Errors: %d', len(errors))\n");

        return sb.toString();
    }
}
