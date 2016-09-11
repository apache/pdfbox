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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.examples.signature.TSAClient;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.wink.client.MockHttpServer;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPValidationException;
import org.bouncycastle.util.Store;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test for CreateSignature. Each test case will run twice: once with SignatureInterface
 * and once using external signature creation scenario.
 */
@RunWith(Parameterized.class)
public class TestCreateSignature
{
    private static final String inDir = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private static final String outDir = "target/test-output/";
    private static final String keystorePath = inDir + "keystore.p12";
    private static final String jpegPath = inDir + "stamp.jpg";
    private static final String password = "123456";
    private static Certificate certificate;

    @Parameterized.Parameter
    public boolean externallySign;

    /**
     * Values for {@link #externallySign} test parameter to specify if signing should be conducted
     * using externally singing scenario ({@code true}) or SignatureInterface ({@code false}).
     */
    @Parameterized.Parameters
    public static Collection signingTypes()
    {
        return Arrays.asList(false, true);
    }

    @BeforeClass
    public static void init() throws Exception
    {
        new File("target/test-output").mkdirs();
        
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());
        certificate = keystore.getCertificateChain(keystore.aliases().nextElement())[0];
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws CMSException
     * @throws OperatorCreationException
     */
    @Test
    public void testDetachedSHA256()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileName = getOutputFileName("signed{0}.pdf");
        signing.signDetached(new File(inDir + "sign_me.pdf"), new File(outDir + fileName));

        checkSignature(new File(outDir + fileName));
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest and a signed
     * timestamp from a Time Stamping Authority (TSA) server.
     *
     * This is not a complete test because we don't have the ability to return a valid response, so
     * we return a cached response which is well-formed, but does not match the timestamp or nonce
     * in the request. This allows us to test the basic TSA mechanism and test the nonce, which is a
     * good start.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws CMSException
     * @throws OperatorCreationException
     */
    @Test
    public void testDetachedSHA256WithTSA()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
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
            String outPath = outDir + getOutputFileName("signed{0}_tsa.pdf");
            CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
            signing.setExternalSigning(externallySign);
            signing.signDetached(new File(inPath), new File(outPath), tsaClient);
        }
        catch (IOException e)
        {
            Assert.assertTrue(e.getCause() instanceof TSPValidationException);
        }

        // TODO verify the signed PDF file
        // TODO create a file signed with TSA
    }
    
    /**
     * Test creating visual signature.
     *
     * @throws IOException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     */
    @Test
    public void testCreateVisibleSignature()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF
        String inPath = inDir + "sign_me.pdf";
        FileInputStream fis = new FileInputStream(jpegPath);
        CreateVisibleSignature signing = new CreateVisibleSignature(keystore, password.toCharArray());
        signing.setVisibleSignDesigner(inPath, 0, 0, -50, fis, 1);
        signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing.setExternalSigning(externallySign);

        File destFile = new File(outDir + getOutputFileName("signed{0}_visible.pdf"));
        signing.signPDF(new File(inPath), destFile, null);
        fis.close();

        checkSignature(destFile);
    }

    private String getOutputFileName(String filePattern)
    {
        return MessageFormat.format(filePattern,(externallySign ? "_ext" : ""));
    }

    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    private void checkSignature(File file)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        PDDocument document = PDDocument.load(file);
        List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
        if (signatureDictionaries.isEmpty())
        {
            Assert.fail("no signature found");
        }
        for (PDSignature sig : document.getSignatureDictionaries())
        {
            COSString contents = (COSString) sig.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = sig.getSignedContent(fis);
            fis.close();

            // inspiration:
            // http://stackoverflow.com/a/26702631/535646
            // http://stackoverflow.com/a/9261365/535646
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
            Store certificatesStore = signedData.getCertificates();
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation signerInformation = signers.iterator().next();
            Collection matches = certificatesStore.getMatches(signerInformation.getSID());
            X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
            X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);

            Assert.assertEquals(certificate, certFromSignedData);

            // CMSVerifierCertificateNotValidException means that the keystore wasn't valid at signing time
            if (!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certFromSignedData)))
            {
                Assert.fail("Signature verification failed");
            }
            break;
        }
        document.close();
    }
}
