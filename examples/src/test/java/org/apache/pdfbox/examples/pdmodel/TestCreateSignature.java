/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.pdmodel;

import junit.framework.TestCase;
import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.examples.signature.TSAClient;
import org.apache.pdfbox.io.IOUtils;
import org.apache.wink.client.MockHttpServer;
import org.bouncycastle.tsp.TSPValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;

/**
 * Test for CreateSignature
 */
public class TestCreateSignature extends TestCase
{
    private final String inDir = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private final String outDir = "target/test-output/";
    private final String keystorePath = inDir + "keystore.p12";
    private final String password = "123456";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new File("target/test-output").mkdirs();
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void testDetachedSHA256() throws IOException, GeneralSecurityException
    {
        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
        signing.signDetached(new File(inDir + "sign_me.pdf"), new File(outDir + "signed.pdf"));

        // TODO verify the signed PDF file
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest and
     * a signed timestamp from a Time Stamping Authority (TSA) server.
     *
     * This is not a complete test because we don't have the ability to return a valid
     * response, so we return a cached response which is well-formed, but does not match
     * the timestamp or nonce in the request. This allows us to test the basic TSA mechanism
     * and test the nonce, which is a good start.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void testDetachedSHA256WithTSA() throws IOException, GeneralSecurityException
    {
        // mock TSA response content
        InputStream input = new FileInputStream(inDir + "tsa_response.asn1");
        byte[] content = IOUtils.toByteArray(input);
        input.close();

        // mock TSA server (RFC 3161)
        MockHttpServer mockServer = new MockHttpServer(15371);
        mockServer.startServer();
        String tsaUrl = "http://localhost:" + mockServer.getServerPort() + "/";
        MockHttpServer.MockHttpServerResponse response = new MockHttpServer.MockHttpServerResponse();
        response.setMockResponseContent(content);
        response.setMockResponseContentType("application/timestamp-reply");
        response.setMockResponseCode(200);
        mockServer.setMockHttpServerResponses(response);

        // TSA client
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        TSAClient tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);

        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF (will fail due to nonce and timestamp differing)
        try
        {
            String inPath = inDir + "sign_me_tsa.pdf";
            String outPath = outDir + "signed_tsa.pdf";
            CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
            signing.signDetached(new File(inPath), new File(outPath), tsaClient);
        }
        catch (IOException e)
        {
            assertTrue(e.getCause() instanceof TSPValidationException);
        }

        // TODO verify the signed PDF file
    }
}
