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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.examples.signature.TSAClient;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.wink.client.MockHttpServer;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPValidationException;
import org.bouncycastle.util.Store;

/**
 * Test for CreateSignature
 */
public class TestCreateSignature extends TestCase
{
    private final String inDir = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private final String outDir = "target/test-output/";
    private final String keystorePath = inDir + "keystore.p12";
    private final String jpegPath = inDir + "stamp.jpg";
    private final String password = "123456";
    private Certificate certificate;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
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
     */
    public void testDetachedSHA256()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
        signing.signDetached(new File(inDir + "sign_me.pdf"), new File(outDir + "signed.pdf"));

        checkSignature(new File(outDir + "signed.pdf"));
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
     */
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
            String outPath = outDir + "signed_tsa.pdf";
            CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
            signing.signDetached(new File(inPath), new File(outPath), tsaClient);
        }
        catch (IOException e)
        {
            assertTrue(e.getCause() instanceof TSPValidationException);
        }

        // TODO verify the signed PDF file
        // TODO create a file signed with TSA
    }
    
    /**
     * Test creating visual signature.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
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
        signing.setVisibleSignatureProperties(inPath, 0, 0, -50, fis, 1);
        signing.setSignatureProperties("name", "location", "Security", 0, 1, true);
        File destFile = new File(outDir + "signed_visible.pdf");
        signing.signPDF(new File(inPath), destFile);

        checkSignature(destFile);
    }

    //TODO expand this into a full verify (if possible)
    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    private void checkSignature(File file)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
    {
        PDDocument document = PDDocument.load(file);
        COSDictionary trailer = document.getDocument().getTrailer();
        COSDictionary root = (COSDictionary) trailer.getDictionaryObject(COSName.ROOT);
        COSDictionary acroForm = (COSDictionary) root.getDictionaryObject(COSName.ACRO_FORM);
        COSArray fields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);
        COSDictionary sig = null;
        for (int i = 0; i < fields.size(); i++)
        {
            COSDictionary field = (COSDictionary) fields.getObject(i);
            if (COSName.SIG.equals(field.getCOSName(COSName.FT)))
            {
                sig = (COSDictionary) field.getDictionaryObject(COSName.V);

                COSString contents = (COSString) sig.getDictionaryObject(COSName.CONTENTS);
                COSArray byteRange = (COSArray) sig.getDictionaryObject(COSName.BYTERANGE);
                
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                
                byte[] buf = new byte[byteRange.getInt(1) + byteRange.getInt(3)];
                raf.seek(byteRange.getInt(0));
                raf.readFully(buf, 0, byteRange.getInt(1));
                raf.seek(byteRange.getInt(2));
                raf.readFully(buf, byteRange.getInt(1), byteRange.getInt(3));
                raf.close();
                
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
                
                assertEquals(certificate, certFromSignedData);

                // code below doesn't work - maybe because the signature can indeed not be verified?
                
//                if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certFromSignedData)))
//                {
//                    System.out.println("Signature verified");
//                }
//                else
//                {
//                    System.out.println("Signature verification failed");
//                }

                break;
            }
        }
        if (sig == null)
        {
            fail("no signature found");
        }
        document.close();
    }
}
