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

        public void setMockResponseHeaders(Map<String, String> headers) {
            mockResponseHeaders.clear();
            mockResponseHeaders.putAll(headers);
        }

        public void setMockResponseHeader(String name, String value) {
            mockResponseHeaders.put(name, value);
        }

        public Map<String, String> getMockResponseHeaders() {
            return mockResponseHeaders;
        }

        public void setMockResponseCode(int responseCode) {
            this.mockResponseCode = responseCode;
        }

        public int getMockResponseCode() {
            return mockResponseCode;
        }

        public void setMockResponseContent(String content) {
            mockResponseContent = content.getBytes();
        }

        public void setMockResponseContent(byte[] content) {
            mockResponseContent = content;
        }

        public byte[] getMockResponseContent() {
            return mockResponseContent;
        }

        public void setMockResponseContentType(String type) {
            mockResponseContentType = type;
        }

        public String getMockResponseContentType() {
            return mockResponseContentType;
        }

        public void setMockResponseContentEchoRequest(boolean echo) {
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
    private int                          readTimeOut             = 5000; // 5 seconds
    private int                          delayResponseTime       = 0;
    private static final byte[]          NEW_LINE                = "\r\n".getBytes();
    // request data
    private String                       requestMethod           = null;
    private String                       requestUrl              = null;
    private final Map<String, List<String>>    requestHeaders    = new HashMap<>();
    private final ByteArrayOutputStream        requestContent    = new ByteArrayOutputStream();
    private final List<MockHttpServerResponse> mockHttpServerResponses = new ArrayList<>();
    private int                          responseCounter         = 0;

    public MockHttpServer(int serverPort) {
        this(serverPort, false);
    }

    public MockHttpServer(int serverPort, boolean ssl) {
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
                } catch (BindException e) {

                }
            }
        } catch (IOException e) {
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void waitForServerToStop() {
        try {
            wait(5000);
        } catch (InterruptedException e) {
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
                Socket socket = serverSocket.accept();
                HttpProcessor processor = new HttpProcessor(socket);
                processor.run();
            }
        } catch (IOException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class HttpProcessor {

        private final Socket socket;

        public HttpProcessor(Socket socket) throws SocketException {
            // set the read timeout (5 seconds by default)
            socket.setSoTimeout(readTimeOut);
            socket.setKeepAlive(false);
            this.socket = socket;
        }

        public void run() {
            try {
                processRequest(socket);
                processResponse(socket);
            } catch (IOException e) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processRequest(Socket socket) throws IOException {
            requestContent.reset();
            BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
            String requestMethodHeader = new String(readLine(is));
            processRequestMethod(requestMethodHeader);
            processRequestHeaders(is);
            processRequestContent(is);
        }

        private void processRequestMethod(String requestMethodHeader) {
            String[] parts = requestMethodHeader.split(" ");
            if (parts.length < 2) {
                throw new RuntimeException("illegal http request");
            }
            requestMethod = parts[0];
            requestUrl = parts[1];
        }

        private void processRequestHeaders(InputStream is) throws IOException {
            requestHeaders.clear();
            byte[] line;
            while ((line = readLine(is)) != null) {
                String lineStr = new String(line);
                // if there are no more headers
                if ("".equals(lineStr.trim())) {
                    break;
                }
                addRequestHeader(lineStr);
            }
        }

        private void processRequestContent(InputStream is) throws NumberFormatException,
            IOException {
            if (!("PUT".equals(requestMethod) || "POST".equals(requestMethod))) {
                return;
            }

            List<String> transferEncodingValues = requestHeaders.get("Transfer-Encoding");
            String transferEncoding =
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

        private void processRegularContent(InputStream is) throws IOException {
            List<String> contentLengthValues = requestHeaders.get("Content-Length");
            String contentLength =
                (contentLengthValues == null || contentLengthValues.isEmpty()) ? null
                    : contentLengthValues.get(0);
            if (contentLength == null) {
                return;
            }
            int contentLen = Integer.parseInt(contentLength);
            byte[] bytes = is.readNBytes(contentLen);
            requestContent.write(bytes);
        }

        private void processChunkedContent(InputStream is) throws IOException {
            requestContent.write("".getBytes());
            byte[] chunk;
            byte[] line = null;
            boolean lastChunk = false;
            // we should exit this loop only after we get to the end of stream
            while (!lastChunk && (line = readLine(is)) != null) {

                String lineStr = new String(line);
                // a chunk is identified as:
                // 1) not an empty line
                // 2) not 0. 0 means that there are no more chunks
                if ("0".equals(lineStr)) {
                    lastChunk = true;
                }

                if (!lastChunk) {
                    // get the length of the current chunk (it is in hexadecimal
                    // form)
                    int chunkLen = Integer.parseInt(lineStr, 16);

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

        private byte[] readLine(InputStream is) throws IOException {
            int n;
            ByteArrayOutputStream tmpOs = new ByteArrayOutputStream();
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

        private byte[] getChunk(InputStream is, int len) throws IOException {
            ByteArrayOutputStream chunk = new ByteArrayOutputStream();
            int read;
            int totalRead = 0;
            byte[] bytes = new byte[512];
            // read len bytes as the chunk
            while (totalRead < len) {
                read = is.read(bytes, 0, Math.min(bytes.length, len - totalRead));
                chunk.write(bytes, 0, read);
                totalRead += read;
            }
            return chunk.toByteArray();
        }

        private void addRequestHeader(String line) {
            String[] parts = line.split(": ");
            List<String> values = requestHeaders.computeIfAbsent(parts[0], k -> new ArrayList<>());
            values.add(parts[1]);
        }

        private void processResponse(Socket socket) throws IOException {
            // if delaying the response failed (because it was interrupted)
            // then don't send the response
            if (!delayResponse())
                return;

            OutputStream sos = socket.getOutputStream();
            BufferedOutputStream os = new BufferedOutputStream(sos);
            String reason = "";
            Status statusCode =
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
                } catch (InterruptedException e) {
                    return false;
                }
            }
            return true;
        }

        private void processResponseContent(OutputStream os) throws IOException {
            if (mockHttpServerResponses.get(responseCounter).getMockResponseContent() == null) {
                return;
            }

            os.write(mockHttpServerResponses.get(responseCounter).getMockResponseContent());
        }

        private void processResponseHeaders(OutputStream os) throws IOException {
            addServerResponseHeaders();
            for (String header : mockHttpServerResponses.get(responseCounter)
                .getMockResponseHeaders().keySet()) {
                os.write((header + ": " + mockHttpServerResponses.get(responseCounter)
                    .getMockResponseHeaders().get(header)).getBytes());
                os.write(NEW_LINE);
            }
            os.write(NEW_LINE);
        }

        private void addServerResponseHeaders() {
            Map<String, String> mockResponseHeaders =
                mockHttpServerResponses.get(responseCounter).getMockResponseHeaders();
            mockResponseHeaders.put("Content-Type", mockHttpServerResponses.get(responseCounter)
                .getMockResponseContentType());
            mockResponseHeaders.put("Content-Length", mockHttpServerResponses.get(responseCounter)
                .getMockResponseContent().length + "");
            mockResponseHeaders.put("Server", "Mock HTTP Server v1.0");
            mockResponseHeaders.put("Connection", "closed");
        }
    }

    public void setReadTimeout(int milliseconds) {
        readTimeOut = milliseconds;
    }

    public void setDelayResponse(int milliseconds) {
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

    public void setMockHttpServerResponses(MockHttpServerResponse... responses) {
        mockHttpServerResponses.clear();
        mockHttpServerResponses.addAll(Arrays.asList(responses));
    }

    public List<MockHttpServerResponse> getMockHttpServerResponses() {
        return mockHttpServerResponses;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }
}
