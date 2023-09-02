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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.examples.interactive.form.CreateSimpleForm;
import org.apache.pdfbox.examples.signature.CreateEmbeddedTimeStamp;
import org.apache.pdfbox.examples.signature.CreateEmptySignatureForm;
import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.examples.signature.CreateSignedTimeStamp;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature2;
import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.examples.signature.cert.CertificateVerificationException;
import org.apache.pdfbox.examples.signature.validation.AddValidationInformation;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Hex;

import org.apache.wink.client.MockHttpServer;

import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TSPValidationException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test for CreateSignature. Each test case will run twice: once with SignatureInterface
 * and once using external signature creation scenario.
 */
class TestCreateSignature
{
    private static final String IN_DIR = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private static final String OUT_DIR = "target/test-output/";
    private static final String KEYSTORE_PATH = IN_DIR + "keystore.p12";
    private static final String JPEG_PATH = IN_DIR + "stamp.jpg";
    private static final String PASSWORD = "123456";
    private static final String TSA_RESPONSE = "tsa_response.asn1";
    private static final String SIMPLE_FORM_FILENAME = "target/TestCreateSignatureSimpleForm.pdf";

    private static CertificateFactory certificateFactory = null;
    private static KeyStore keyStore = null;
    private static Certificate certificate;
    private static String tsa;

    /**
     * Values for {@link #externallySign} test parameter to specify if signing should be conducted
     * using externally signing scenario ({@code true}) or SignatureInterface ({@code false}).
     */

    private static Collection<Boolean> signingTypes()
    {
        return Arrays.asList(false, true);
    }

    @BeforeAll
    static void init() throws Exception
    {
        Security.addProvider(SecurityProvider.getProvider());
        certificateFactory = CertificateFactory.getInstance("X.509");

        // load the keystore
        keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(KEYSTORE_PATH), PASSWORD.toCharArray());

        new File("target/test-output").mkdirs();

        certificate = keyStore.getCertificateChain(keyStore.aliases().nextElement())[0];
        tsa = System.getProperty("org.apache.pdfbox.examples.pdmodel.tsa");

        // don't use the default file name, because it's used by other tests that run concurrently
        CreateSimpleForm.main(new String[] { SIMPLE_FORM_FILENAME });
    }

    /**
     * Test whether local machine has the correct time. If not, other tests may fail with "OCSP
     * answer is too old".
     */
    @Test
    void testTimeDifference() throws IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Date localTime = new Date();

        // https://stackoverflow.com/questions/4442192/
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName("time.nist.gov");
        timeClient.setDefaultTimeout(5000);
        TimeInfo timeInfo;
        long returnTime;
        try
        {
            timeInfo = timeClient.getTime(inetAddress);
            returnTime = timeInfo.getReturnTime();
        }
        catch (SocketTimeoutException ex)
        {
            // Won't work behind a proxy. Nor on our CI :-(
            System.out.println("Socket timeout when trying to get time from NTP server; trying google");

            String dateString;
            try
            {
                HttpsURLConnection con = (HttpsURLConnection) new URL("https://www.google.com/").openConnection();
                if (con.getResponseCode() != HttpsURLConnection.HTTP_OK)
                {
                    System.out.println("Google returns " + con.getResponseCode());
                    return;
                }
                dateString = con.getHeaderField("Date");
                con.disconnect();
            }
            catch (IOException ioex)
            {
                System.out.println("failed to access google: " + ioex.getMessage());
                return;
            }
            ZonedDateTime zdt = DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateString, ZonedDateTime::from);
            returnTime = Date.from(zdt.toInstant()).getTime();
        }
        System.out.println("Remote time: " + sdf.format(new Date(returnTime)));
        System.out.println("Local  time: " + sdf.format(localTime));
        long diff = Math.abs(localTime.getTime() - returnTime) / 1000;
        assertTrue(diff < 15, "Local time is off by more than " + diff + " seconds");
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest.
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws GeneralSecurityException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws TSPException
     * @throws CertificateVerificationException
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testDetachedSHA256(boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException, URISyntaxException
    {
        // sign PDF
        CreateSignature signing = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileName = getOutputFileName("signed{0}.pdf", externallySign);
        final String fileName2 = getOutputFileName("signed{0}-late-tsa.pdf", externallySign);
        signing.signDetached(new File(IN_DIR + "sign_me.pdf"), new File(OUT_DIR + fileName));

        checkSignature(new File(IN_DIR, "sign_me.pdf"), new File(OUT_DIR, fileName), false);

        // Also test CreateEmbeddedTimeStamp if tsa URL is available
        Assumptions.assumeTrue(tsa != null && !tsa.isEmpty(), "No TSA URL defined, test skipped");

        CreateEmbeddedTimeStamp tsaSigning = new CreateEmbeddedTimeStamp(tsa);
        tsaSigning.embedTimeStamp(new File(OUT_DIR, fileName), new File(OUT_DIR, fileName2));
        checkSignature(new File(OUT_DIR, fileName), new File(OUT_DIR, fileName2), true);
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
     * @throws TSPException
     * @throws CertificateVerificationException
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testDetachedSHA256WithTSA(boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // mock TSA response content
        byte[] content = Files.readAllBytes(Paths.get(IN_DIR, TSA_RESPONSE));

        // mock TSA server (RFC 3161)
        MockHttpServer mockServer = new MockHttpServer(externallySign ? 15371 : 15372);
        mockServer.startServer();
        String brokenMockTSA = "http://localhost:" + mockServer.getServerPort() + "/";
        MockHttpServer.MockHttpServerResponse response = new MockHttpServer.MockHttpServerResponse();
        response.setMockResponseContent(content);
        response.setMockResponseContentType("application/timestamp-reply");
        response.setMockResponseCode(200);
        mockServer.setMockHttpServerResponses(response);

        String inPath = IN_DIR + "sign_me_tsa.pdf";
        String outPath = OUT_DIR + getOutputFileName("signed{0}_tsa.pdf", externallySign);

        // sign PDF (will fail due to nonce and timestamp differing)
        CreateSignature signing1 = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing1.setExternalSigning(externallySign);
        try
        {
            signing1.signDetached(new File(inPath), new File(outPath), brokenMockTSA);
            fail("This should have failed");
        }
        catch (IOException e)
        {
            assertTrue(e.getCause() instanceof TSPValidationException);
            new File(outPath).delete();
        }

        mockServer.stopServer();

        Assumptions.assumeTrue(tsa != null && !tsa.isEmpty(), "No TSA URL defined, test skipped");

        CreateSignature signing2 = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing2.setExternalSigning(externallySign);
        signing2.signDetached(new File(inPath), new File(outPath), tsa);
        checkSignature(new File(inPath), new File(outPath), true);
        System.out.println("TSA test successful");
    }

    /**
     * Test timestamp only signature (ETSI.RFC3161).
     * 
     * @throws IOException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     * @throws TSPException
     * @throws CertificateVerificationException 
     */
    @Test
    void testCreateSignedTimeStamp()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException, OCSPException
    {
        Assumptions.assumeTrue(tsa != null && !tsa.isEmpty(), "No TSA URL defined, test skipped");

        final String fileName = "timestamped.pdf";
        CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsa);
        signing.signDetached(new File(IN_DIR + "sign_me.pdf"), new File(OUT_DIR + fileName));

        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR + fileName)))
        {
            PDSignature signature = doc.getLastSignatureDictionary();
            byte[] totalFileContent = Files.readAllBytes(new File(OUT_DIR, fileName).toPath());
            byte[] signedFileContent = signature.getSignedContent(totalFileContent);
            byte[] contents = signature.getContents();
            TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents));
            ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
            Collection<? extends Certificate> certs = certificateFactory.generateCertificates(certStream);

            String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
            // compare the hash of the signed content with the hash in the timestamp
            assertArrayEquals(MessageDigest.getInstance(hashAlgorithm).digest(signedFileContent),
                    timeStampToken.getTimeStampInfo().getMessageImprintDigest());

            X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
            SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
            SigUtils.validateTimestampToken(timeStampToken);
            SigUtils.verifyCertificateChain(timeStampToken.getCertificates(),
                    certFromTimeStamp,
                    timeStampToken.getTimeStampInfo().getGenTime());
        }

        File inFile = new File(OUT_DIR, fileName);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(OUT_DIR, substring + "_LTV.pdf");
        AddValidationInformation addValidationInformation = new AddValidationInformation();
        addValidationInformation.validateSignature(inFile, outFile);

        checkLTV(outFile);
    }

    /**
     * Test creating visual signature.
     *
     * @throws IOException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     * @throws TSPException
     * @throws CertificateVerificationException
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testCreateVisibleSignature(boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        String inPath = IN_DIR + "sign_me_visible.pdf";
        File destFile;
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            CreateVisibleSignature signing = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing.setVisibleSignDesigner(inPath, 0, 0, -50, fis, 1);
            signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing.setExternalSigning(externallySign);
            destFile = new File(OUT_DIR + getOutputFileName("signed{0}_visible.pdf", externallySign));
            signing.signPDF(new File(inPath), destFile, null);
        }

        checkSignature(new File(inPath), destFile, false);
    }

    /**
     * Test creating visual signature with the modernized example.
     *
     * @throws IOException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     * @throws TSPException
     * @throws CertificateVerificationException
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testCreateVisibleSignature2(boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        String inPath = IN_DIR + "sign_me_visible.pdf";
        File destFile;

        CreateVisibleSignature2 signing = new CreateVisibleSignature2(keyStore, PASSWORD.toCharArray());
        Rectangle2D humanRect = new Rectangle2D.Float(100, 200, 150, 50);
        signing.setImageFile(new File(JPEG_PATH));
        signing.setExternalSigning(externallySign);
        destFile = new File(OUT_DIR + getOutputFileName("signed{0}_visible2.pdf", externallySign));
        signing.signPDF(new File(inPath), destFile, humanRect, null);

        checkSignature(new File(inPath), destFile, false);
    }

    /**
     * Test when visually signing externally on an existing signature field on a file which has
     * been signed before.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     * @throws TSPException
     * @throws CertificateVerificationException
     */
    @Test
    void testPDFBox3978() throws IOException, NoSuchAlgorithmException, 
                                        CertificateException, UnrecoverableKeyException, 
                                        CMSException, OperatorCreationException, GeneralSecurityException,
                                        TSPException, CertificateVerificationException
    {
        String filename        = OUT_DIR + "EmptySignatureForm.pdf";
        String filenameSigned1 = OUT_DIR + "EmptySignatureForm-signed1.pdf";
        String filenameSigned2 = OUT_DIR + "EmptySignatureForm-signed2.pdf";

        // create file with empty signature
        CreateEmptySignatureForm.main(new String[]{filename});

        // sign PDF
        CreateSignature signing1 = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing1.setExternalSigning(false);
        signing1.signDetached(new File(filename), new File(filenameSigned1));

        checkSignature(new File(filename), new File(filenameSigned1), false);

        try (PDDocument doc1 = Loader.loadPDF(new File(filenameSigned1)))
        {
            List<PDSignature> signatureDictionaries = doc1.getSignatureDictionaries();
            assertEquals(1, signatureDictionaries.size());
        }

        // do visual signing in the field
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            CreateVisibleSignature signing2 = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing2.setVisibleSignDesigner(filenameSigned1, 0, 0, -50, fis, 1);
            signing2.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing2.setExternalSigning(true);
            signing2.signPDF(new File(filenameSigned1), new File(filenameSigned2), null, "Signature1");
        }

        checkSignature(new File(filenameSigned1), new File(filenameSigned2), false);

        try (PDDocument doc2 = Loader.loadPDF(new File(filenameSigned2)))
        {
            List<PDSignature> signatureDictionaries = doc2.getSignatureDictionaries();
            assertEquals(2, signatureDictionaries.size());
        }
    }

    @ParameterizedTest
    @MethodSource("signingTypes")
    void testDoubleVisibleSignatureOnEncryptedFile(boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
            TSPException, CertificateVerificationException
    {
        // sign PDF
        String inPath = "target/pdfs/PDFBOX-2469-1-AcroForm-AES128.pdf";
        CreateVisibleSignature signing;
        File destFile;
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            signing = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing.setVisibleSignDesigner(inPath, 0, 0, -50, fis, 1);
            signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing.setExternalSigning(externallySign);
            destFile = new File(OUT_DIR, getOutputFileName("2signed{0}_visible.pdf", externallySign));
            signing.signPDF(new File(inPath), destFile, null);
        }

        checkSignature(new File(inPath), destFile, false);

        inPath = destFile.getAbsolutePath();
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            signing = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing.setVisibleSignDesigner(inPath, 200, 100, -50, fis, 1);
            signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing.setExternalSigning(externallySign);
            destFile = new File(OUT_DIR, getOutputFileName("2signed{0}_visible_signed{0}_visible.pdf", externallySign));
            signing.signPDF(new File(inPath), destFile, null);
        }

        checkSignature(new File(inPath), destFile, false);

        // PDFBOX-5243: check that there are two annotations
        try (PDDocument doc = Loader.loadPDF(destFile))
        {
            List<PDAnnotation> annotations = doc.getPage(0).getAnnotations();
            assertEquals(2, annotations.size());
        }
    }

    private String getOutputFileName(String filePattern, boolean externallySign)
    {
        return MessageFormat.format(filePattern, (externallySign ? "_ext" : ""));
    }

    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    private void checkSignature(File origFile, File signedFile, boolean checkTimeStamp)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
            TSPException, CertificateVerificationException
    {
        String origPageKey;
        try (PDDocument document = Loader.loadPDF(origFile))
        {
            // get string representation of pages COSObject
            origPageKey = document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString();
        }
        try (PDDocument document = Loader.loadPDF(signedFile))
        {
            // early detection of problems in the page structure
            int p = 0;
            PDPageTree pageTree = document.getPages();
            for (PDPage page : document.getPages())
            {
                assertEquals(p, pageTree.indexOf(page));
                ++p;
            }

            // PDFBOX-4261: check that object number stays the same 
            assertEquals(origPageKey, document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString());

            List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
            if (signatureDictionaries.isEmpty())
            {
                fail("no signature found");
            }
            for (PDSignature sig : document.getSignatureDictionaries())
            {
                byte[] contents = sig.getContents();

                // verify that getSignedContent() brings the same content
                // regardless whether from an InputStream or from a byte array
                byte[] totalFileContent = Files.readAllBytes(signedFile.toPath());
                byte[] signedFileContent1 = sig.getSignedContent(new ByteArrayInputStream(totalFileContent));
                byte[] signedFileContent2 = sig.getSignedContent(totalFileContent);
                assertArrayEquals(signedFileContent1, signedFileContent2);

                // verify that all getContents() methods returns the same content
                try (FileInputStream fis = new FileInputStream(signedFile))
                {
                    byte[] contents2 = sig.getContents(((InputStream) fis).readAllBytes());
                    assertArrayEquals(contents, contents2);
                }
                byte[] contents3 = sig.getContents(new FileInputStream(signedFile));
                assertArrayEquals(contents, contents3);

                // inspiration:
                // http://stackoverflow.com/a/26702631/535646
                // http://stackoverflow.com/a/9261365/535646
                CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(signedFileContent1), contents);
                Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
                Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
                SignerInformation signerInformation = signers.iterator().next();
                @SuppressWarnings("unchecked")
                Collection<X509CertificateHolder> matches = certificatesStore
                        .getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
                X509CertificateHolder certificateHolder = matches.iterator().next();
                assertArrayEquals(certificate.getEncoded(), certificateHolder.getEncoded());
                // CMSVerifierCertificateNotValidException means that the keystore wasn't valid at signing time
                if (!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificateHolder)))
                {
                    fail("Signature verification failed");
                }

                TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
                if (checkTimeStamp)
                {
                    assertNotNull(timeStampToken);
                    SigUtils.validateTimestampToken(timeStampToken);

                    TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

                    // compare the hash of the signed content with the hash in the timestamp
                    byte[] tsMessageImprintDigest = timeStampInfo.getMessageImprintDigest();
                    String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
                    byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
                    assertArrayEquals(sigMessageImprintDigest, tsMessageImprintDigest, "timestamp signature verification failed");                    

                    Store<X509CertificateHolder> tsCertStore = timeStampToken.getCertificates();

                    // get the certificate from the timeStampToken
                    @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
                    Collection<X509CertificateHolder> tsCertStoreMatches = tsCertStore.getMatches(timeStampToken.getSID());
                    X509CertificateHolder certHolderFromTimeStamp = tsCertStoreMatches.iterator().next();
                    X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(certHolderFromTimeStamp);

                    SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
                    SigUtils.verifyCertificateChain(tsCertStore, certFromTimeStamp, timeStampInfo.getGenTime());
                }
                else
                {
                    assertNull(timeStampToken);
                }
            }
        }
    }

    private String calculateDigestString(InputStream inputStream) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Hex.getString(md.digest(inputStream.readAllBytes()));
    }

    /**
     * PDFBOX-3811: make sure that calling saveIncrementalForExternalSigning() more than once
     * brings the same result.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    void testPDFBox3811() throws IOException, NoSuchAlgorithmException
    {        
        // create simple PDF
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        new PDPageContentStream(document, page).close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        
        document = Loader.loadPDF(baos.toByteArray());
        // for stable digest
        document.setDocumentId(12345L);
        
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        document.addSignature(signature);
        int[] reserveByteRange = signature.getByteRange();

        String digestString = calculateDigestString(document.saveIncrementalForExternalSigning(new ByteArrayOutputStream()).getContent());
        boolean caught = false;
        try
        {
            document.saveIncrementalForExternalSigning(new ByteArrayOutputStream());
        }
        catch (IllegalStateException ex)
        {
            caught = true;
        }
        assertTrue(caught, "IllegalStateException should have been thrown");
        signature.setByteRange(reserveByteRange);
        assertEquals(digestString, calculateDigestString(document.saveIncrementalForExternalSigning(new ByteArrayOutputStream()).getContent()));
    }

    /**
     * Create a simple form PDF, sign it, reload it, change a field value, incrementally save it.
     * This should not break the signature, and the value and its display must have changed as
     * expected. Do this both for the old and new incremental save methods.
     *
     * @throws Exception
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testSaveIncrementalAfterSign(boolean externallySign) throws Exception
    {
        BufferedImage oldImage, expectedImage1, actualImage1, expectedImage2, actualImage2;

        // sign PDF
        CreateSignature signing = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileNameSigned = getOutputFileName("SimpleForm_signed{0}.pdf", externallySign);
        final String fileNameResaved1 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved1.pdf", externallySign);
        final String fileNameResaved2 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved2.pdf", externallySign);

        signing.signDetached(new File(SIMPLE_FORM_FILENAME), new File(OUT_DIR + fileNameSigned));

        checkSignature(new File(SIMPLE_FORM_FILENAME), new File(OUT_DIR, fileNameSigned), false);

        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameSigned)))
        {
            oldImage = new PDFRenderer(doc).renderImage(0);
            
            FileOutputStream fileOutputStream = new FileOutputStream(new File(OUT_DIR, fileNameResaved1));
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            field.setValue("New Value 1");

            // Test of PDFBOX-4509: only "Helv" font should be there
            Collection<COSName> fonts = (Collection<COSName>) field.getWidgets().get(0).getAppearance().
                    getNormalAppearance().getAppearanceStream().getResources().getFontNames();
            assertTrue(fonts.contains(COSName.HELV));
            assertEquals(1, fonts.size());

            expectedImage1 = new PDFRenderer(doc).renderImage(0);

            // compare images, image must have changed
            assertEquals(oldImage.getWidth(), expectedImage1.getWidth());
            assertEquals(oldImage.getHeight(), expectedImage1.getHeight());
            assertEquals(oldImage.getType(), expectedImage1.getType());
            DataBufferInt expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
            DataBufferInt actualData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
            assertEquals(expectedData.getData().length, actualData.getData().length);
            assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

            // old style incremental save: create a "path" from the root to the objects that need an update
            doc.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);
            doc.getDocumentCatalog().getAcroForm().getCOSObject().setNeedToBeUpdated(true);
            field.getCOSObject().setNeedToBeUpdated(true);
            PDAppearanceDictionary appearance = field.getWidgets().get(0).getAppearance();
            appearance.getCOSObject().setNeedToBeUpdated(true);
            appearance.getNormalAppearance().getCOSObject().setNeedToBeUpdated(true);
            doc.saveIncremental(fileOutputStream);
        }
        checkSignature(new File(SIMPLE_FORM_FILENAME), new File(OUT_DIR, fileNameResaved1), false);
        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameResaved1)))
        {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            assertEquals("New Value 1", field.getValueAsString());
            actualImage1 = new PDFRenderer(doc).renderImage(0);
            // compare images, equality proves that the appearance has been updated too
            assertEquals(expectedImage1.getWidth(), actualImage1.getWidth());
            assertEquals(expectedImage1.getHeight(), actualImage1.getHeight());
            assertEquals(expectedImage1.getType(), actualImage1.getType());
            DataBufferInt expectedData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
            DataBufferInt actualData = (DataBufferInt) actualImage1.getRaster().getDataBuffer();
            assertArrayEquals(expectedData.getData(), actualData.getData());
        }

        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameSigned)))
        {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(OUT_DIR, fileNameResaved2));
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            field.setValue("New Value 2");
            expectedImage2 = new PDFRenderer(doc).renderImage(0);

            // compare images, image must have changed
            assertEquals(oldImage.getWidth(), expectedImage2.getWidth());
            assertEquals(oldImage.getHeight(), expectedImage2.getHeight());
            assertEquals(oldImage.getType(), expectedImage2.getType());
            DataBufferInt expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
            DataBufferInt actualData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
            assertEquals(expectedData.getData().length, actualData.getData().length);
            assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

            // new style incremental save: add only the objects that have changed
            Set<COSDictionary> objectsToWrite = new HashSet<>();
            objectsToWrite.add(field.getCOSObject());
            objectsToWrite.add(field.getWidgets().get(0).getAppearance().getCOSObject());
            objectsToWrite.add(field.getWidgets().get(0).getAppearance().getNormalAppearance().getCOSObject());
            doc.saveIncremental(fileOutputStream, objectsToWrite);
        }
        checkSignature(new File(SIMPLE_FORM_FILENAME), new File(OUT_DIR, fileNameResaved2), false);
        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameResaved2)))
        {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            assertEquals("New Value 2", field.getValueAsString());
            actualImage2 = new PDFRenderer(doc).renderImage(0);
            // compare images, equality proves that the appearance has been updated too
            assertEquals(expectedImage2.getWidth(), actualImage2.getWidth());
            assertEquals(expectedImage2.getHeight(), actualImage2.getHeight());
            assertEquals(expectedImage2.getType(), actualImage2.getType());
            DataBufferInt expectedData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
            DataBufferInt actualData = (DataBufferInt) actualImage2.getRaster().getDataBuffer();
            assertArrayEquals(expectedData.getData(), actualData.getData());
        }
    }

    @Test
    void testPDFBox4784() throws Exception
    {
        Date signingTime = new Date();

        byte[] defaultSignedOne = signEncrypted(null, signingTime);
        byte[] defaultSignedTwo = signEncrypted(null, signingTime);
        assertFalse(Arrays.equals(defaultSignedOne, defaultSignedTwo));

        // a zero placeholder value for FixedSecureRandom is used (a secure value should be provided for real use-cases )
        byte[] fixedRandomSignedOne = signEncrypted(new FixedSecureRandom(new byte[128]),
                signingTime);
        byte[] fixedRandomSignedTwo = signEncrypted(new FixedSecureRandom(new byte[128]),
                signingTime);
        assertArrayEquals(fixedRandomSignedOne, fixedRandomSignedTwo);
    }

    /**
     * Test getting CRLs when OCSP (adobe-ocsp.geotrust.com) is unavailable.
     * This validates the certificates of the signature from the file 083698.pdf, which is 
     * 109TH CONGRESS 2D SESSION H. R. 5500, from MAY 25, 2006.
     *
     * @throws IOException
     * @throws CMSException
     * @throws CertificateException
     * @throws TSPException
     * @throws OperatorCreationException
     * @throws CertificateVerificationException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    void testCRL() throws IOException, CMSException, CertificateException, TSPException,
            OperatorCreationException, CertificateVerificationException, NoSuchAlgorithmException
    {
        String hexSignature;
        try (BufferedReader bfr = 
            new BufferedReader(new InputStreamReader(new FileInputStream(IN_DIR + "hexsignature.txt"))))
        {
            hexSignature = bfr.readLine();
        }

        CMSSignedData signedData = new CMSSignedData(Hex.decodeHex(hexSignature));
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        SignerInformation signerInformation = signers.iterator().next();
        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        @SuppressWarnings("unchecked") // SignerInformation.getSID() is untyped
        Collection<X509CertificateHolder> matches = certificatesStore.getMatches(signerInformation.getSID());
        X509CertificateHolder certificateHolder = matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        SigUtils.checkCertificateUsage(certFromSignedData);

        TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
        SigUtils.validateTimestampToken(timeStampToken);
        @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
        Collection<X509CertificateHolder> tstMatches =
            timeStampToken.getCertificates().getMatches((Selector<X509CertificateHolder>) timeStampToken.getSID());
        X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
        X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(tstCertHolder);
        // merge both stores using a set to remove duplicates
        HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<>();
        certificateHolderSet.addAll(certificatesStore.getMatches(null));
        certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
        SigUtils.verifyCertificateChain(new CollectionStore<>(certificateHolderSet),
                certFromTimeStamp,
                timeStampToken.getTimeStampInfo().getGenTime());
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);

        // compare the hash of the signature with the hash in the timestamp
        byte[] tsMessageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
        String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
        byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
        assertArrayEquals(tsMessageImprintDigest, sigMessageImprintDigest);

        certFromSignedData.checkValidity(timeStampToken.getTimeStampInfo().getGenTime());
        SigUtils.verifyCertificateChain(certificatesStore, certFromSignedData, timeStampToken.getTimeStampInfo().getGenTime());
    }

    /**
     * Test adding LTV information. This tests the status quo. If we use a new file (or if the file
     * gets updated) then the test may have to be adjusted. The test is not really perfect, but it
     * tries to check a minimum of things that should match. If the test fails and you didn't change
     * anything in signing, then find out whether some external servers involved are unresponsive.
     * At the time of writing this, the OCSP server http://ocsp.quovadisglobal.com responds with 502
     * "UNAUTHORIZED". That is not a problem as long as the CRL URL works.
     *
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     * @throws org.bouncycastle.cert.ocsp.OCSPException
     * @throws org.bouncycastle.operator.OperatorCreationException
     * @throws org.bouncycastle.cms.CMSException
     */
    @Test
    void testAddValidationInformation()
            throws IOException, GeneralSecurityException, OCSPException, OperatorCreationException, CMSException
    {
        File inFile = new File("target/pdfs", "notCertified_368835_Sig_en_201026090509.pdf");
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(OUT_DIR, substring + "_LTV.pdf");
        AddValidationInformation addValidationInformation = new AddValidationInformation();
        addValidationInformation.validateSignature(inFile, outFile);

        checkLTV(outFile);
    }

    private void checkLTV(File outFile)
            throws IOException, GeneralSecurityException, OCSPException, OperatorCreationException,
            CMSException
    {
        try (PDDocument doc = Loader.loadPDF(outFile))
        {
            PDSignature signature = doc.getLastSignatureDictionary();
            byte[] contents = signature.getContents();
            PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
            COSDictionary dssDict = docCatalog.getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
            COSArray dssCertArray = dssDict.getCOSArray(COSName.getPDFName("Certs"));
            COSDictionary vriDict = dssDict.getCOSDictionary(COSName.getPDFName("VRI"));
            // Check that all known signature certificates are in the VRI/signaturehash/Cert array
            byte[] signatureHash = MessageDigest.getInstance("SHA-1").digest(contents);
            String hexSignatureHash = Hex.getString(signatureHash);
            System.out.println("hexSignatureHash: " + hexSignatureHash);
            CMSSignedData signedData = new CMSSignedData(contents);
            Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
            HashSet<X509CertificateHolder> certificateHolderSet =
                    new HashSet<>(certificatesStore.getMatches(null));
            COSDictionary sigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexSignatureHash));
            COSArray sigCertArray = sigDict.getCOSArray(COSName.getPDFName("Cert"));
            Set<X509CertificateHolder> sigCertHolderSetFromVRIArray = new HashSet<>();
            for (int i = 0; i < sigCertArray.size(); ++i)
            {
                COSStream certStream = (COSStream) sigCertArray.getObject(i);
                try (InputStream is = certStream.createInputStream())
                {
                    sigCertHolderSetFromVRIArray.add(new X509CertificateHolder(is.readAllBytes()));
                }
            }
            for (X509CertificateHolder holder : certificateHolderSet)
            {
                if (holder.getSubject().toString().contains("QuoVadis OCSP Authority Signature"))
                {
                    continue; // not relevant here
                }
                // disabled until PDFBOX-5203 is fixed
//                assertTrue(sigCertHolderSetFromVRIArray.contains(holder),
//                        "File '" + outFile + "' Root/DSS/VRI/" + hexSignatureHash +
//                                "/Cert array doesn't contain a certificate with subject '" +
//                                holder.getSubject() + "' and serial " + holder.getSerialNumber());
            }
            // Get all certificates. Each one should either be issued (= signed) by a certificate of the set
            Set<X509Certificate> certSet = new HashSet<>();
            for (int i = 0; i < dssCertArray.size(); ++i)
            {
                COSStream certStream = (COSStream) dssCertArray.getObject(i);
                try (InputStream is = certStream.createInputStream())
                {
                    X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
                    certSet.add(cert);
                }
            }
            for (X509Certificate cert : certSet)
            {
                boolean verified = false;
                for (X509Certificate cert2 : certSet)
                {
                    try
                    {
                        cert.verify(cert2.getPublicKey(), SecurityProvider.getProvider());
                        verified = true;
                    }
                    catch (GeneralSecurityException ex)
                    {
                        // not the issuer
                    }
                }
                // disabled until PDFBOX-5203 is fixed
//                assertTrue(verified,
//                    "Certificate " + cert.getSubjectX500Principal() + " not issued by any certificate in the Certs array");
            }
            // Each CRL should be signed by one of the certificates in Certs
            Set<X509CRL> crlSet = new HashSet<>();
            COSArray crlArray = dssDict.getCOSArray(COSName.getPDFName("CRLs"));
            for (int i = 0; i < crlArray.size(); ++i)
            {
                COSStream crlStream = (COSStream) crlArray.getObject(i);
                try (InputStream is = crlStream.createInputStream())
                {
                    X509CRL cert = (X509CRL) certificateFactory.generateCRL(is);
                    crlSet.add(cert);
                }
            }
            for (X509CRL crl : crlSet)
            {
                boolean crlVerified = false;
                X509Certificate crlIssuerCert = null;
                for (X509Certificate cert : certSet)
                {
                    try
                    {
                        crl.verify(cert.getPublicKey(), SecurityProvider.getProvider());
                        crlVerified = true;
                        crlIssuerCert = cert;
                    }
                    catch (GeneralSecurityException ex)
                    {
                        // not the issuer
                    }
                }
                assertTrue(crlVerified, "issuer of CRL not found in Certs array");
                
                BEROctetString encodedSignature = new BEROctetString(crl.getSignature());
                byte[] crlSignatureHash = MessageDigest.getInstance("SHA-1").digest(encodedSignature.getEncoded());
                String hexCrlSignatureHash = Hex.getString(crlSignatureHash);
                System.out.println("hexCrlSignatureHash: " + hexCrlSignatureHash);
                
                // Check that the issueing certificate is in the VRI array
                COSDictionary crlSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexCrlSignatureHash));
                COSArray certArray2 = crlSigDict.getCOSArray(COSName.getPDFName("Cert"));
                COSStream certStream = (COSStream) certArray2.getObject(0);
                X509CertificateHolder certHolder2;
                try (InputStream is2 = certStream.createInputStream())
                {
                    certHolder2 = new X509CertificateHolder(is2.readAllBytes());
                }
                
                assertEquals(certHolder2, new X509CertificateHolder(crlIssuerCert.getEncoded()),
                        "CRL issuer certificate missing in VRI " + hexCrlSignatureHash);
            }
            Set<OCSPResp> oscpSet = new HashSet<>();
            COSArray ocspArray = dssDict.getCOSArray(COSName.getPDFName("OCSPs"));
            for (int i = 0; i < ocspArray.size(); ++i)
            {
                COSStream ocspStream = (COSStream) ocspArray.getObject(i);
                try (InputStream is = ocspStream.createInputStream())
                {
                    OCSPResp ocspResp = new OCSPResp(is);
                    oscpSet.add(ocspResp);
                }
            }
            for (OCSPResp ocspResp : oscpSet)
            {
                BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResp.getResponseObject();
                assertEquals(OCSPResponseStatus.SUCCESSFUL, ocspResp.getStatus());
                assertTrue(basicResponse.getCerts().length >= 1, "OCSP should have at least 1 certificate");
                BEROctetString encodedSignature = new BEROctetString(basicResponse.getSignature());
                byte[] ocspSignatureHash = MessageDigest.getInstance("SHA-1").digest(encodedSignature.getEncoded());
                String hexOcspSignatureHash = Hex.getString(ocspSignatureHash);
                System.out.println("ocspSignatureHash: " + hexOcspSignatureHash);
                long secondsOld = (System.currentTimeMillis() - basicResponse.getProducedAt().getTime()) / 1000;
                assertTrue(secondsOld < 20, "OCSP answer is too old, is from " + secondsOld + " seconds ago");
                
                X509CertificateHolder ocspCertHolder = basicResponse.getCerts()[0];
                ContentVerifierProvider verifier = new JcaContentVerifierProviderBuilder().setProvider(SecurityProvider.getProvider()).build(ocspCertHolder);
                assertTrue(basicResponse.isSignatureValid(verifier));

                COSDictionary ocspSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexOcspSignatureHash));

                // Check that the Cert is in the VRI array
                COSArray certArray2 = ocspSigDict.getCOSArray(COSName.getPDFName("Cert"));
                COSStream certStream = (COSStream) certArray2.getObject(0);
                X509CertificateHolder certHolder2;
                try (InputStream is2 = certStream.createInputStream())
                {
                    certHolder2 = new X509CertificateHolder(is2.readAllBytes());
                }

                assertEquals(certHolder2, ocspCertHolder, "OCSP certificate is not in the VRI array");
            }
        }
    }

    private byte[] signEncrypted(SecureRandom secureRandom, Date signingTime) throws Exception
    {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(KEYSTORE_PATH), PASSWORD.toCharArray());

        CreateSignature signing = new CreateSignature(keystore, PASSWORD.toCharArray());
        signing.setExternalSigning(true);

        File inFile = new File(IN_DIR + "sign_me_protected.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument doc = null;
        try
        {
            doc = Loader.loadPDF(inFile, " ");

            if (secureRandom != null)
            {
                doc.getEncryption().getSecurityHandler().setCustomSecureRandom(secureRandom);
            }

            PDSignature signature = new PDSignature();
            signature.setName("Example User");
            Calendar cal = Calendar.getInstance();
            cal.setTime(signingTime);
            signature.setSignDate(cal);

            doc.addSignature(signature);
            doc.setDocumentId(12345l);
            ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(baos);
            // invoke external signature service
            return externalSigning.getContent().readAllBytes();
        }
        finally
        {
            IOUtils.closeQuietly(doc);
            IOUtils.closeQuietly(baos);
        }
    }
}
