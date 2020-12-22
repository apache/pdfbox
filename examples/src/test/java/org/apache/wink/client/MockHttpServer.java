/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.pdfbox.io.IOUtils;

/**
 * Copied from
 * http://svn.apache.org/repos/asf/wink/trunk/wink-component-test-support/src/main/java/org/apache/wink/client/MockHttpServer.java
 * on 28.7.2018.
 */
public class MockHttpServer extends Thread {

    public static class MockHttpServerResponse {

        // mock response data
        private int                 mockResponseCode        = 200;
        private final Map<String, String> mockResponseHeaders = new HashMap<>();
        private byte[]              mockResponseContent     = "received message".getBytes();
        private String              mockResponseContentType = "text/plain;charset=utf-8";
        private boolean             mockResponseContentEchoRequest;

        public void setMockResponseHeaders(final Map<String, String> headers) {
            mockResponseHeaders.clear();
            mockResponseHeaders.putAll(headers);
        }

        public void setMockResponseHeader(final String name, final String value) {
            mockResponseHeaders.put(name, value);
        }

        public Map<String, String> getMockResponseHeaders() {
            return mockResponseHeaders;
        }

        public void setMockResponseCode(final int responseCode) {
            this.mockResponseCode = responseCode;
        }

        public int getMockResponseCode() {
            return mockResponseCode;
        }

        public void setMockResponseContent(final String content) {
            mockResponseContent = content.getBytes();
        }

        public void setMockResponseContent(final byte[] content) {
            mockResponseContent = content;
        }

        public byte[] getMockResponseContent() {
            return mockResponseContent;
        }

        public void setMockResponseContentType(final String type) {
            mockResponseContentType = type;
        }

        public String getMockResponseContentType() {
            return mockResponseContentType;
        }

        public void setMockResponseContentEchoRequest(final boolean echo) {
            mockResponseContentEchoRequest = echo;
        }

        public boolean getMockResponseContentEchoRequest() {
            return mockResponseContentEchoRequest;
        }
    }

    private Thread                       serverThread            = null;
    private ServerSocket                 serverSocket            = null;
    private boolean                      serverStarted           = false;
    private ServerSocketFactory          serverSocketFactory     = null;
    private int                          serverPort;
    private int                          readTimeOut             = 5000;                                     // 5
    // seconds
    private int                          delayResponseTime       = 0;
    private static final byte[]                NEW_LINE                = "\r\n".getBytes();
    // request data
    private String                       requestMethod           = null;
    private String                       requestUrl              = null;
    private final Map<String, List<String>>    requestHeaders          = new HashMap<>();
    private final ByteArrayOutputStream        requestContent          = new ByteArrayOutputStream();
    private final List<MockHttpServerResponse> mockHttpServerResponses = new ArrayList<>();
    private int                          responseCounter         = 0;

    public MockHttpServer(final int serverPort) {
        this(serverPort, false);
    }

    public MockHttpServer(final int serverPort, final boolean ssl) {
        mockHttpServerResponses.add(new MockHttpServerResponse()); // set a
        // default
        // response
        this.serverPort = serverPort;
        try {
            serverSocketFactory = ServerSocketFactory.getDefault();
            if (ssl) {
                serverSocketFactory = SSLServerSocketFactory.getDefault();
            }
            while (serverSocket == null) {
                try {
                    serverSocket = serverSocketFactory.createServerSocket(++this.serverPort);
                } catch (final BindException e) {

                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void startServer() {
        if (serverStarted)
            return;

        // start the server thread
        start();
        serverStarted = true;

        // wait for the server thread to start
        waitForServerToStart();
    }

    private synchronized void waitForServerToStart() {
        try {
            wait(5000);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void waitForServerToStop() {
        try {
            wait(5000);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        serverThread = Thread.currentThread();
        executeLoop();
    }

    private void executeLoop() {
        serverStarted();
        try {
            while (true) {
                final Socket socket = serverSocket.accept();
                final HttpProcessor processor = new HttpProcessor(socket);
                processor.run();
            }
        } catch (final IOException e) {
            if (e instanceof SocketException) {
                if (!("Socket closed".equalsIgnoreCase(e.getMessage()) || "Socket is closed"
                    .equalsIgnoreCase(e.getMessage()))) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } finally {
            // notify that the server was stopped
            serverStopped();
        }
    }

    private synchronized void serverStarted() {
        // notify the waiting thread that the thread started
        notifyAll();
    }

    private synchronized void serverStopped() {
        // notify the waiting thread that the thread started
        notifyAll();
    }

    public synchronized void stopServer() {
        if (!serverStarted)
            return;

        try {
            serverStarted = false;
            // the server may be sleeping somewhere...
            serverThread.interrupt();
            // close the server socket
            serverSocket.close();
            // wait for the server to stop
            waitForServerToStop();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private class HttpProcessor {

        private final Socket socket;

        public HttpProcessor(final Socket socket) throws SocketException {
            // set the read timeout (5 seconds by default)
            socket.setSoTimeout(readTimeOut);
            socket.setKeepAlive(false);
            this.socket = socket;
        }

        public void run() {
            try {
                processRequest(socket);
                processResponse(socket);
            } catch (final IOException e) {
                if (e instanceof SocketException) {
                    if (!("socket closed".equalsIgnoreCase(e.getMessage()))) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    socket.shutdownOutput();
                    socket.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processRequest(final Socket socket) throws IOException {
            requestContent.reset();
            final BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
            final String requestMethodHeader = new String(readLine(is));
            processRequestMethod(requestMethodHeader);
            processRequestHeaders(is);
            processRequestContent(is);
        }

        private void processRequestMethod(final String requestMethodHeader) {
            final String[] parts = requestMethodHeader.split(" ");
            if (parts.length < 2) {
                throw new RuntimeException("illegal http request");
            }
            requestMethod = parts[0];
            requestUrl = parts[1];
        }

        private void processRequestHeaders(final InputStream is) throws IOException {
            requestHeaders.clear();
            byte[] line;
            while ((line = readLine(is)) != null) {
                final String lineStr = new String(line);
                // if there are no more headers
                if ("".equals(lineStr.trim())) {
                    break;
                }
                addRequestHeader(lineStr);
            }
        }

        private void processRequestContent(final InputStream is) throws NumberFormatException,
            IOException {
            if (!("PUT".equals(requestMethod) || "POST".equals(requestMethod))) {
                return;
            }

            final List<String> transferEncodingValues = requestHeaders.get("Transfer-Encoding");
            final String transferEncoding =
                (transferEncodingValues == null || transferEncodingValues.isEmpty()) ? null
                    : transferEncodingValues.get(0);
            if ("chunked".equals(transferEncoding)) {
                processChunkedContent(is);
            } else {
                processRegularContent(is);
            }

            if (mockHttpServerResponses.get(responseCounter).getMockResponseContentEchoRequest()) {
                mockHttpServerResponses.get(responseCounter).setMockResponseContent(requestContent
                    .toByteArray());
            }

        }

        private void processRegularContent(final InputStream is) throws IOException {
            final List<String> contentLengthValues = requestHeaders.get("Content-Length");
            final String contentLength =
                (contentLengthValues == null || contentLengthValues.isEmpty()) ? null
                    : contentLengthValues.get(0);
            if (contentLength == null) {
                return;
            }
            final int contentLen = Integer.parseInt(contentLength);
            final byte[] bytes = new byte[contentLen];
            IOUtils.populateBuffer(is, bytes);
            requestContent.write(bytes);
        }

        private void processChunkedContent(final InputStream is) throws IOException {
            requestContent.write("".getBytes());
            byte[] chunk;
            byte[] line = null;
            boolean lastChunk = false;
            // we should exit this loop only after we get to the end of stream
            while (!lastChunk && (line = readLine(is)) != null) {

                final String lineStr = new String(line);
                // a chunk is identified as:
                // 1) not an empty line
                // 2) not 0. 0 means that there are no more chunks
                if ("0".equals(lineStr)) {
                    lastChunk = true;
                }

                if (!lastChunk) {
                    // get the length of the current chunk (it is in hexadecimal
                    // form)
                    final int chunkLen = Integer.parseInt(lineStr, 16);

                    // get the chunk
                    chunk = getChunk(is, chunkLen);

                    // consume the newline after the chunk that separates
                    // between
                    // the chunk content and the next chunk size
                    readLine(is);

                    requestContent.write(chunk);
                }
            }

            // do one last read to consume the empty line after the last chunk
            if (lastChunk) {
                readLine(is);
            }
        }

        private byte[] readLine(final InputStream is) throws IOException {
            int n;
            final ByteArrayOutputStream tmpOs = new ByteArrayOutputStream();
            while ((n = is.read()) != -1) {
                if (n == '\r') {
                    n = is.read();
                    if (n == '\n') {
                        return tmpOs.toByteArray();
                    } else {
                        tmpOs.write('\r');
                        if (n != -1) {
                            tmpOs.write(n);
                        } else {
                            return tmpOs.toByteArray();
                        }
                    }
                } else if (n == '\n') {
                    return tmpOs.toByteArray();
                } else {
                    tmpOs.write(n);
                }
            }
            return tmpOs.toByteArray();
        }

        private byte[] getChunk(final InputStream is, final int len) throws IOException {
            final ByteArrayOutputStream chunk = new ByteArrayOutputStream();
            int read;
            int totalRead = 0;
            final byte[] bytes = new byte[512];
            // read len bytes as the chunk
            while (totalRead < len) {
                read = is.read(bytes, 0, Math.min(bytes.length, len - totalRead));
                chunk.write(bytes, 0, read);
                totalRead += read;
            }
            return chunk.toByteArray();
        }

        private void addRequestHeader(final String line) {
            final String[] parts = line.split(": ");
            List<String> values = requestHeaders.get(parts[0]);
            if (values == null) {
                values = new ArrayList<>();
                requestHeaders.put(parts[0], values);
            }
            values.add(parts[1]);
        }

        private void processResponse(final Socket socket) throws IOException {
            // if delaying the response failed (because it was interrupted)
            // then don't send the response
            if (!delayResponse())
                return;

            final OutputStream sos = socket.getOutputStream();
            final BufferedOutputStream os = new BufferedOutputStream(sos);
            String reason = "";
            final Status statusCode =
                Response.Status.fromStatusCode(mockHttpServerResponses.get(responseCounter)
                    .getMockResponseCode());
            if (statusCode != null) {
                reason = statusCode.toString();
            }
            os.write(("HTTP/1.1 " + mockHttpServerResponses.get(responseCounter)
                .getMockResponseCode()
                + " " + reason).getBytes());
            os.write(NEW_LINE);
            processResponseHeaders(os);
            processResponseContent(os);
            os.flush();
            responseCounter++;
        }

        // return:
        // true - delay was successful
        // false - delay was unsuccessful
        private boolean delayResponse() {
            // delay the response by delayResponseTime milliseconds
            if (delayResponseTime > 0) {
                try {
                    Thread.sleep(delayResponseTime);
                    return true;
                } catch (final InterruptedException e) {
                    return false;
                }
            }
            return true;
        }

        private void processResponseContent(final OutputStream os) throws IOException {
            if (mockHttpServerResponses.get(responseCounter).getMockResponseContent() == null) {
                return;
            }

            os.write(mockHttpServerResponses.get(responseCounter).getMockResponseContent());
        }

        private void processResponseHeaders(final OutputStream os) throws IOException {
            addServerResponseHeaders();
            for (final String header : mockHttpServerResponses.get(responseCounter)
                .getMockResponseHeaders().keySet()) {
                os.write((header + ": " + mockHttpServerResponses.get(responseCounter)
                    .getMockResponseHeaders().get(header)).getBytes());
                os.write(NEW_LINE);
            }
            os.write(NEW_LINE);
        }

        private void addServerResponseHeaders() {
            final Map<String, String> mockResponseHeaders =
                mockHttpServerResponses.get(responseCounter).getMockResponseHeaders();
            mockResponseHeaders.put("Content-Type", mockHttpServerResponses.get(responseCounter)
                .getMockResponseContentType());
            mockResponseHeaders.put("Content-Length", mockHttpServerResponses.get(responseCounter)
                .getMockResponseContent().length + "");
            mockResponseHeaders.put("Server", "Mock HTTP Server v1.0");
            mockResponseHeaders.put("Connection", "closed");
        }
    }

    public void setReadTimeout(final int milliseconds) {
        readTimeOut = milliseconds;
    }

    public void setDelayResponse(final int milliseconds) {
        delayResponseTime = milliseconds;
    }

    public String getRequestContentAsString() {
        return requestContent.toString();
    }

    public byte[] getRequestContent() {
        return requestContent.toByteArray();
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setMockHttpServerResponses(final MockHttpServerResponse... responses) {
        mockHttpServerResponses.clear();
        mockHttpServerResponses.addAll(Arrays.asList(responses));
    }

    public List<MockHttpServerResponse> getMockHttpServerResponses() {
        return mockHttpServerResponses;
    }

    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }
}
