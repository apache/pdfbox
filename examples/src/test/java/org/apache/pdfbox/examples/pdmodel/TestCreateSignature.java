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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInputStream;
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
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Hex;
import org.apache.wink.client.MockHttpServer;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test for CreateSignature. Each test case will run twice: once with SignatureInterface
 * and once using external signature creation scenario.
 */
class TestCreateSignature
{
    private static CertificateFactory certificateFactory = null;
    private static KeyStore keyStore = null;
    private static final String IN_DIR = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private static final String OUT_DIR = "target/test-output/";
    private static final String KEYSTORE_PATH = IN_DIR + "keystore.p12";
    private static final String JPEG_PATH = IN_DIR + "stamp.jpg";
    private static final String PASSWORD = "123456";
    private static final String TSA_RESPONSE = "tsa_response.asn1";
    private static Certificate certificate;
    private static String tsa;


    public boolean externallySign;

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
    }

    /**
     * Signs a PDF using the "adbe.pkcs7.detached" SubFilter with the SHA-256 digest.
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
    void testDetachedSHA256(final boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        final CreateSignature signing = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileName = getOutputFileName("signed{0}.pdf");
        final String fileName2 = getOutputFileName("signed{0}-late-tsa.pdf");
        signing.signDetached(new File(IN_DIR + "sign_me.pdf"), new File(OUT_DIR + fileName));

        checkSignature(new File(IN_DIR, "sign_me.pdf"), new File(OUT_DIR, fileName), false);

        // Also test CreateEmbeddedTimeStamp if tsa URL is available
        if (tsa == null || tsa.isEmpty())
        {
            System.err.println("No TSA URL defined, test skipped");
            return;
        }
        
        final CreateEmbeddedTimeStamp tsaSigning = new CreateEmbeddedTimeStamp(tsa);
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
    void testDetachedSHA256WithTSA(final boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // mock TSA response content
        final byte[] content = Files.readAllBytes(Paths.get(IN_DIR, TSA_RESPONSE));

        // mock TSA server (RFC 3161)
        final MockHttpServer mockServer = new MockHttpServer(15371);
        mockServer.startServer();
        final String brokenMockTSA = "http://localhost:" + mockServer.getServerPort() + "/";
        final MockHttpServer.MockHttpServerResponse response = new MockHttpServer.MockHttpServerResponse();
        response.setMockResponseContent(content);
        response.setMockResponseContentType("application/timestamp-reply");
        response.setMockResponseCode(200);
        mockServer.setMockHttpServerResponses(response);

        final String inPath = IN_DIR + "sign_me_tsa.pdf";
        final String outPath = OUT_DIR + getOutputFileName("signed{0}_tsa.pdf");

        // sign PDF (will fail due to nonce and timestamp differing)
        final CreateSignature signing1 = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing1.setExternalSigning(externallySign);
        try
        {
            signing1.signDetached(new File(inPath), new File(outPath), brokenMockTSA);
            fail("This should have failed");
        }
        catch (final IOException e)
        {
            assertTrue(e.getCause() instanceof TSPValidationException);
            new File(outPath).delete();
        }

        mockServer.stopServer();

        if (tsa == null || tsa.isEmpty())
        {
            System.err.println("No TSA URL defined, test skipped");
            return;
        }

        final CreateSignature signing2 = new CreateSignature(keyStore, PASSWORD.toCharArray());
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
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testCreateSignedTimeStamp(final boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        if (externallySign)
        {
            return; // runs only once, independent of externallySign
        }
        if (tsa == null || tsa.isEmpty())
        {
            System.err.println("No TSA URL defined, test skipped");
            return;
        }
        final String fileName = getOutputFileName("timestamped{0}.pdf");
        final CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsa);
        signing.signDetached(new File(IN_DIR + "sign_me.pdf"), new File(OUT_DIR + fileName));

        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR + fileName)))
        {
            final PDSignature signature = doc.getLastSignatureDictionary();
            final byte[] totalFileContent = Files.readAllBytes(new File(OUT_DIR, fileName).toPath());
            final byte[] signedFileContent = signature.getSignedContent(totalFileContent);
            final byte[] contents = signature.getContents();
            final TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents));
            final ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
            final Collection<? extends Certificate> certs = certificateFactory.generateCertificates(certStream);

            final String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
            // compare the hash of the signed content with the hash in the timestamp
            assertArrayEquals(MessageDigest.getInstance(hashAlgorithm).digest(signedFileContent),
                    timeStampToken.getTimeStampInfo().getMessageImprintDigest());

            final X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
            SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
            SigUtils.validateTimestampToken(timeStampToken);
            SigUtils.verifyCertificateChain(timeStampToken.getCertificates(),
                    certFromTimeStamp,
                    timeStampToken.getTimeStampInfo().getGenTime());
        }
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
    void testCreateVisibleSignature(final boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        final String inPath = IN_DIR + "sign_me_visible.pdf";
        final File destFile;
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            final CreateVisibleSignature signing = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing.setVisibleSignDesigner(inPath, 0, 0, -50, fis, 1);
            signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing.setExternalSigning(externallySign);
            destFile = new File(OUT_DIR + getOutputFileName("signed{0}_visible.pdf"));
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
    void testCreateVisibleSignature2(final boolean externallySign)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        final String inPath = IN_DIR + "sign_me_visible.pdf";
        final File destFile;

        final CreateVisibleSignature2 signing = new CreateVisibleSignature2(keyStore, PASSWORD.toCharArray());
        final Rectangle2D humanRect = new Rectangle2D.Float(100, 200, 150, 50);
        signing.setImageFile(new File(JPEG_PATH));
        signing.setExternalSigning(externallySign);
        destFile = new File(OUT_DIR + getOutputFileName("signed{0}_visible2.pdf"));
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
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testPDFBox3978(final boolean externallySign) throws IOException, NoSuchAlgorithmException,
                                        CertificateException, UnrecoverableKeyException, 
                                        CMSException, OperatorCreationException, GeneralSecurityException,
                                        TSPException, CertificateVerificationException
    {
        final String filename        = OUT_DIR + "EmptySignatureForm.pdf";
        final String filenameSigned1 = OUT_DIR + "EmptySignatureForm-signed1.pdf";
        final String filenameSigned2 = OUT_DIR + "EmptySignatureForm-signed2.pdf";

        if (!externallySign)
        {
            return;
        }

        // create file with empty signature
        CreateEmptySignatureForm.main(new String[]{filename});

        // sign PDF
        final CreateSignature signing1 = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing1.setExternalSigning(false);
        signing1.signDetached(new File(filename), new File(filenameSigned1));

        checkSignature(new File(filename), new File(filenameSigned1), false);

        try (PDDocument doc1 = Loader.loadPDF(new File(filenameSigned1)))
        {
            final List<PDSignature> signatureDictionaries = doc1.getSignatureDictionaries();
            assertEquals(1, signatureDictionaries.size());
        }

        // do visual signing in the field
        try (FileInputStream fis = new FileInputStream(JPEG_PATH))
        {
            final CreateVisibleSignature signing2 = new CreateVisibleSignature(keyStore, PASSWORD.toCharArray());
            signing2.setVisibleSignDesigner(filenameSigned1, 0, 0, -50, fis, 1);
            signing2.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
            signing2.setExternalSigning(true);
            signing2.signPDF(new File(filenameSigned1), new File(filenameSigned2), null, "Signature1");
        }

        checkSignature(new File(filenameSigned1), new File(filenameSigned2), false);

        try (PDDocument doc2 = Loader.loadPDF(new File(filenameSigned2)))
        {
            final List<PDSignature> signatureDictionaries = doc2.getSignatureDictionaries();
            assertEquals(2, signatureDictionaries.size());
        }
    }

    private String getOutputFileName(final String filePattern)
    {
        return MessageFormat.format(filePattern,(externallySign ? "_ext" : ""));
    }

    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    private void checkSignature(final File origFile, final File signedFile, final boolean checkTimeStamp)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
            TSPException, CertificateVerificationException
    {
        final String origPageKey;
        try (PDDocument document = Loader.loadPDF(origFile))
        {
            // get string representation of pages COSObject
            origPageKey = document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString();
        }
        try (PDDocument document = Loader.loadPDF(signedFile))
        {
            // PDFBOX-4261: check that object number stays the same 
            assertEquals(origPageKey, document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString());

            final List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
            if (signatureDictionaries.isEmpty())
            {
                fail("no signature found");
            }
            for (final PDSignature sig : document.getSignatureDictionaries())
            {
                final byte[] contents = sig.getContents();

                // verify that getSignedContent() brings the same content
                // regardless whether from an InputStream or from a byte array
                final byte[] totalFileContent = Files.readAllBytes(signedFile.toPath());
                final byte[] signedFileContent1 = sig.getSignedContent(new ByteArrayInputStream(totalFileContent));
                final byte[] signedFileContent2 = sig.getSignedContent(totalFileContent);
                assertArrayEquals(signedFileContent1, signedFileContent2);

                // verify that all getContents() methods returns the same content
                try (FileInputStream fis = new FileInputStream(signedFile))
                {
                    final byte[] contents2 = sig.getContents(IOUtils.toByteArray(fis));
                    assertArrayEquals(contents, contents2);
                }
                final byte[] contents3 = sig.getContents(new FileInputStream(signedFile));
                assertArrayEquals(contents, contents3);

                // inspiration:
                // http://stackoverflow.com/a/26702631/535646
                // http://stackoverflow.com/a/9261365/535646
                final CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(signedFileContent1), contents);
                final Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
                final Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
                final SignerInformation signerInformation = signers.iterator().next();
                @SuppressWarnings("unchecked") final Collection<X509CertificateHolder> matches = certificatesStore
                        .getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
                final X509CertificateHolder certificateHolder = matches.iterator().next();
                assertArrayEquals(certificate.getEncoded(), certificateHolder.getEncoded());
                // CMSVerifierCertificateNotValidException means that the keystore wasn't valid at signing time
                if (!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificateHolder)))
                {
                    fail("Signature verification failed");
                }

                final TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
                if (checkTimeStamp)
                {
                    assertNotNull(timeStampToken);
                    SigUtils.validateTimestampToken(timeStampToken);

                    final TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

                    // compare the hash of the signed content with the hash in the timestamp
                    final byte[] tsMessageImprintDigest = timeStampInfo.getMessageImprintDigest();
                    final String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
                    final byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
                    assertArrayEquals(sigMessageImprintDigest, tsMessageImprintDigest, "timestamp signature verification failed");                    

                    final Store<X509CertificateHolder> tsCertStore = timeStampToken.getCertificates();

                    // get the certificate from the timeStampToken
                    @SuppressWarnings("unchecked") final // TimeStampToken.getSID() is untyped
                    Collection<X509CertificateHolder> tsCertStoreMatches = tsCertStore.getMatches(timeStampToken.getSID());
                    final X509CertificateHolder certHolderFromTimeStamp = tsCertStoreMatches.iterator().next();
                    final X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(certHolderFromTimeStamp);

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

    private String calculateDigestString(final InputStream inputStream) throws NoSuchAlgorithmException, IOException
    {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Hex.getString(md.digest(IOUtils.toByteArray(inputStream)));
    }

    /**
     * PDFBOX-3811: make sure that calling saveIncrementalForExternalSigning() more than once
     * brings the same result.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testPDFBox3811(final boolean externallySign) throws IOException, NoSuchAlgorithmException
    {
        if (!externallySign)
        {
            return;
        }
        
        // create simple PDF
        PDDocument document = new PDDocument();
        final PDPage page = new PDPage();
        document.addPage(page);
        new PDPageContentStream(document, page).close();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        
        document = Loader.loadPDF(baos.toByteArray());
        // for stable digest
        document.setDocumentId(12345L);
        
        final PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        document.addSignature(signature);
        final int[] reserveByteRange = signature.getByteRange();

        final String digestString = calculateDigestString(document.saveIncrementalForExternalSigning(new ByteArrayOutputStream()).getContent());
        boolean caught = false;
        try
        {
            document.saveIncrementalForExternalSigning(new ByteArrayOutputStream());
        }
        catch (final IllegalStateException ex)
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
    void testSaveIncrementalAfterSign(final boolean externallySign) throws Exception
    {
        final BufferedImage oldImage;
        final BufferedImage expectedImage1;
        final BufferedImage actualImage1;
        final BufferedImage expectedImage2;
        final BufferedImage actualImage2;

        CreateSimpleForm.main(new String[0]); // creates "target/SimpleForm.pdf"

        // sign PDF
        final CreateSignature signing = new CreateSignature(keyStore, PASSWORD.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileNameSigned = getOutputFileName("SimpleForm_signed{0}.pdf");
        final String fileNameResaved1 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved1.pdf");
        final String fileNameResaved2 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved2.pdf");
        signing.signDetached(new File("target/SimpleForm.pdf"), new File(OUT_DIR + fileNameSigned));

        checkSignature(new File("target/SimpleForm.pdf"), new File(OUT_DIR, fileNameSigned), false);
        
        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameSigned)))
        {
            oldImage = new PDFRenderer(doc).renderImage(0);
            
            final FileOutputStream fileOutputStream = new FileOutputStream(new File(OUT_DIR, fileNameResaved1));
            final PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            field.setValue("New Value 1");

            // Test of PDFBOX-4509: only "Helv" font should be there
            final Collection<COSName> fonts = (Collection<COSName>) field.getWidgets().get(0).getAppearance().
                    getNormalAppearance().getAppearanceStream().getResources().getFontNames();
            assertTrue(fonts.contains(COSName.HELV));
            assertEquals(1, fonts.size());

            expectedImage1 = new PDFRenderer(doc).renderImage(0);

            // compare images, image must has changed
            assertEquals(oldImage.getWidth(), expectedImage1.getWidth());
            assertEquals(oldImage.getHeight(), expectedImage1.getHeight());
            assertEquals(oldImage.getType(), expectedImage1.getType());
            final DataBufferInt expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
            final DataBufferInt actualData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
            assertEquals(expectedData.getData().length, actualData.getData().length);
            assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

            // old style incremental save: create a "path" from the root to the objects that need an update
            doc.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);
            doc.getDocumentCatalog().getAcroForm().getCOSObject().setNeedToBeUpdated(true);
            field.getCOSObject().setNeedToBeUpdated(true);
            final PDAppearanceDictionary appearance = field.getWidgets().get(0).getAppearance();
            appearance.getCOSObject().setNeedToBeUpdated(true);
            appearance.getNormalAppearance().getCOSObject().setNeedToBeUpdated(true);
            doc.saveIncremental(fileOutputStream);
        }
        checkSignature(new File("target/SimpleForm.pdf"), new File(OUT_DIR, fileNameResaved1), false);
        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameResaved1)))
        {
            final PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            assertEquals("New Value 1", field.getValueAsString());
            actualImage1 = new PDFRenderer(doc).renderImage(0);
            // compare images, equality proves that the appearance has been updated too
            assertEquals(expectedImage1.getWidth(), actualImage1.getWidth());
            assertEquals(expectedImage1.getHeight(), actualImage1.getHeight());
            assertEquals(expectedImage1.getType(), actualImage1.getType());
            final DataBufferInt expectedData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
            final DataBufferInt actualData = (DataBufferInt) actualImage1.getRaster().getDataBuffer();
            assertArrayEquals(expectedData.getData(), actualData.getData());
        }

        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameSigned)))
        {
            final FileOutputStream fileOutputStream = new FileOutputStream(new File(OUT_DIR, fileNameResaved2));
            final PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            field.setValue("New Value 2");
            expectedImage2 = new PDFRenderer(doc).renderImage(0);

            // compare images, image must has changed
            assertEquals(oldImage.getWidth(), expectedImage2.getWidth());
            assertEquals(oldImage.getHeight(), expectedImage2.getHeight());
            assertEquals(oldImage.getType(), expectedImage2.getType());
            final DataBufferInt expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
            final DataBufferInt actualData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
            assertEquals(expectedData.getData().length, actualData.getData().length);
            assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

            // new style incremental save: add only the objects that have changed
            final Set<COSDictionary> objectsToWrite = new HashSet<>();
            objectsToWrite.add(field.getCOSObject());
            objectsToWrite.add(field.getWidgets().get(0).getAppearance().getCOSObject());
            objectsToWrite.add(field.getWidgets().get(0).getAppearance().getNormalAppearance().getCOSObject());
            doc.saveIncremental(fileOutputStream, objectsToWrite);
        }
        checkSignature(new File("target/SimpleForm.pdf"), new File(OUT_DIR, fileNameResaved2), false);
        try (PDDocument doc = Loader.loadPDF(new File(OUT_DIR, fileNameResaved2)))
        {
            final PDField field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
            assertEquals("New Value 2", field.getValueAsString());
            actualImage2 = new PDFRenderer(doc).renderImage(0);
            // compare images, equality proves that the appearance has been updated too
            assertEquals(expectedImage2.getWidth(), actualImage2.getWidth());
            assertEquals(expectedImage2.getHeight(), actualImage2.getHeight());
            assertEquals(expectedImage2.getType(), actualImage2.getType());
            final DataBufferInt expectedData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
            final DataBufferInt actualData = (DataBufferInt) actualImage2.getRaster().getDataBuffer();
            assertArrayEquals(expectedData.getData(), actualData.getData());
        }
    }

    @ParameterizedTest
	@MethodSource("signingTypes")
    void testPDFBox4784(final boolean externallySign) throws Exception
    {
        if (!externallySign)
        {
            return;
        }
        final Date signingTime = new Date();

        final byte[] defaultSignedOne = signEncrypted(null, signingTime);
        final byte[] defaultSignedTwo = signEncrypted(null, signingTime);
        assertFalse(Arrays.equals(defaultSignedOne, defaultSignedTwo));

        // a dummy value for FixedSecureRandom is used (for real use-cases a secure value should be provided)
        final byte[] fixedRandomSignedOne = signEncrypted(new FixedSecureRandom(new byte[128]),
                signingTime);
        final byte[] fixedRandomSignedTwo = signEncrypted(new FixedSecureRandom(new byte[128]),
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
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testCRL(final boolean externallySign) throws IOException, CMSException, CertificateException, TSPException,
            OperatorCreationException, CertificateVerificationException, NoSuchAlgorithmException
    {
        if (externallySign)
        {
            return; // runs only once, independent of externallySign
        }
        final String hexSignature;
        try (BufferedReader bfr =
            new BufferedReader(new InputStreamReader(new FileInputStream(IN_DIR + "hexsignature.txt"))))
        {
            hexSignature = bfr.readLine();
        }

        final CMSSignedData signedData = new CMSSignedData(Hex.decodeHex(hexSignature));
        final Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        final SignerInformation signerInformation = signers.iterator().next();
        final Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        @SuppressWarnings("unchecked") final // SignerInformation.getSID() is untyped
        Collection<X509CertificateHolder> matches = certificatesStore.getMatches(signerInformation.getSID());
        final X509CertificateHolder certificateHolder = matches.iterator().next();
        final X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        SigUtils.checkCertificateUsage(certFromSignedData);

        final TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
        SigUtils.validateTimestampToken(timeStampToken);
        @SuppressWarnings("unchecked") final // TimeStampToken.getSID() is untyped
        Collection<X509CertificateHolder> tstMatches =
            timeStampToken.getCertificates().getMatches((Selector<X509CertificateHolder>) timeStampToken.getSID());
        final X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
        final X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(tstCertHolder);
        // merge both stores using a set to remove duplicates
        final HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<>();
        certificateHolderSet.addAll(certificatesStore.getMatches(null));
        certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
        SigUtils.verifyCertificateChain(new CollectionStore<>(certificateHolderSet),
                certFromTimeStamp,
                timeStampToken.getTimeStampInfo().getGenTime());
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);

        // compare the hash of the signature with the hash in the timestamp
        final byte[] tsMessageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
        final String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
        final byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
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
    @ParameterizedTest
	@MethodSource("signingTypes")
    void testAddValidationInformation(final boolean externallySign)
            throws IOException, GeneralSecurityException, OCSPException, OperatorCreationException, CMSException
    {
        if (externallySign)
        {
            return; // runs only once, independent of externallySign
        }
        final File inFile = new File("target/pdfs", "notCertified_368835_Sig_en_201026090509.pdf");
        final String name = inFile.getName();
        final String substring = name.substring(0, name.lastIndexOf('.'));

        final File outFile = new File(OUT_DIR, substring + "_LTV.pdf");
        final AddValidationInformation addValidationInformation = new AddValidationInformation();
        addValidationInformation.validateSignature(inFile, outFile);

        try (PDDocument doc = Loader.loadPDF(outFile))
        {
            final PDSignature signature = doc.getLastSignatureDictionary();
            final byte[] contents = signature.getContents();
            final PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
            final COSDictionary dssDict = docCatalog.getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
            final COSArray dssCertArray = dssDict.getCOSArray(COSName.getPDFName("Certs"));
            final COSDictionary vriDict = dssDict.getCOSDictionary(COSName.getPDFName("VRI"));
            // Check that all known signature certificates are in the VRI/signaturehash/Cert array
            final byte[] signatureHash = MessageDigest.getInstance("SHA-1").digest(contents);
            final String hexSignatureHash = Hex.getString(signatureHash);
            System.out.println("hexSignatureHash: " + hexSignatureHash);
            final CMSSignedData signedData = new CMSSignedData(contents);
            final Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
            final HashSet<X509CertificateHolder> certificateHolderSet =
                    new HashSet<>(certificatesStore.getMatches(null));
            final COSDictionary sigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexSignatureHash));
            final COSArray sigCertArray = sigDict.getCOSArray(COSName.getPDFName("Cert"));
            final Set<X509CertificateHolder> sigCertHolderSetFromVRIArray = new HashSet<>();
            for (int i = 0; i < sigCertArray.size(); ++i)
            {
                final COSStream certStream = (COSStream) sigCertArray.getObject(i);
                try (COSInputStream is = certStream.createInputStream())
                {
                    sigCertHolderSetFromVRIArray.add(new X509CertificateHolder(IOUtils.toByteArray(is)));
                }
            }
            for (final X509CertificateHolder holder : certificateHolderSet)
            {
                if (holder.getSubject().toString().contains("QuoVadis OCSP Authority Signature"))
                {
                    continue; // not relevant here
                }
                assertTrue(sigCertHolderSetFromVRIArray.contains(holder),
                        "VRI/signaturehash/Cert array doesn't contain " + holder.getSubject());
            }
            // Get all certificates. Each one should either be issued (= signed) by a certificate of the set
            final Set<X509Certificate> certSet = new HashSet<>();
            for (int i = 0; i < dssCertArray.size(); ++i)
            {
                final COSStream certStream = (COSStream) dssCertArray.getObject(i);
                try (COSInputStream is = certStream.createInputStream())
                {
                    final X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
                    certSet.add(cert);
                }                
            }
            for (final X509Certificate cert : certSet)
            {
                boolean verified = false;
                for (final X509Certificate cert2 : certSet)
                {
                    try
                    {
                        cert.verify(cert2.getPublicKey(), SecurityProvider.getProvider());
                        verified = true;
                    }
                    catch (final GeneralSecurityException ex)
                    {
                        // not the issuer
                    }
                }
                assertTrue(verified,
                    "Certificate " + cert.getSubjectX500Principal() + " not issued by any certificate in the Certs array");
            }
            // Each CRL should be signed by one of the certificates in Certs
            final Set<X509CRL> crlSet = new HashSet<>();
            final COSArray crlArray = dssDict.getCOSArray(COSName.getPDFName("CRLs"));
            for (int i = 0; i < crlArray.size(); ++i)
            {
                final COSStream crlStream = (COSStream) crlArray.getObject(i);
                try (COSInputStream is = crlStream.createInputStream())
                {
                    final X509CRL cert = (X509CRL) certificateFactory.generateCRL(is);
                    crlSet.add(cert);
                }                
            }
            for (final X509CRL crl : crlSet)
            {
                boolean crlVerified = false;
                X509Certificate crlIssuerCert = null;
                for (final X509Certificate cert : certSet)
                {
                    try
                    {
                        crl.verify(cert.getPublicKey(), SecurityProvider.getProvider());
                        crlVerified = true;
                        crlIssuerCert = cert;
                    }
                    catch (final GeneralSecurityException ex)
                    {
                        // not the issuer
                    }
                }
                assertTrue(crlVerified, "issuer of CRL not found in Certs array");
                
                final byte[] crlSignatureHash = MessageDigest.getInstance("SHA-1").digest(crl.getSignature());
                final String hexCrlSignatureHash = Hex.getString(crlSignatureHash);
                System.out.println("hexCrlSignatureHash: " + hexCrlSignatureHash);
                
                // Check that the issueing certificate is in the VRI array
                final COSDictionary crlSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexCrlSignatureHash));
                final COSArray certArray2 = crlSigDict.getCOSArray(COSName.getPDFName("Cert"));
                final COSStream certStream = (COSStream) certArray2.getObject(0);
                final X509CertificateHolder certHolder2;
                try (COSInputStream is2 = certStream.createInputStream())
                {
                    certHolder2 = new X509CertificateHolder(IOUtils.toByteArray(is2));
                }
                
                assertEquals(certHolder2, new X509CertificateHolder(crlIssuerCert.getEncoded()),
                        "CRL issuer certificate missing in VRI " + hexCrlSignatureHash);
            }   final Set<OCSPResp> oscpSet = new HashSet<>();
            final COSArray ocspArray = dssDict.getCOSArray(COSName.getPDFName("OCSPs"));
            for (int i = 0; i < ocspArray.size(); ++i)
            {
                final COSStream ocspStream = (COSStream) ocspArray.getObject(i);
                try (COSInputStream is = ocspStream.createInputStream())
                {
                    final OCSPResp ocspResp = new OCSPResp(is);
                    oscpSet.add(ocspResp);
                }
            }
            for (final OCSPResp ocspResp : oscpSet)
            {
                final BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResp.getResponseObject();
                assertEquals(OCSPResponseStatus.SUCCESSFUL, ocspResp.getStatus());
                assertTrue(basicResponse.getCerts().length >= 1, "OCSP should have at least 1 certificate");
                final byte[] ocspSignatureHash = MessageDigest.getInstance("SHA-1").digest(basicResponse.getSignature());
                final String hexOcspSignatureHash = Hex.getString(ocspSignatureHash);
                System.out.println("ocspSignatureHash: " + hexOcspSignatureHash);
                final long secondsOld = (System.currentTimeMillis() - basicResponse.getProducedAt().getTime()) / 1000;
                assertTrue(secondsOld < 10, "OCSP answer is too old, is from " + secondsOld + " seconds ago");
                
                final X509CertificateHolder ocspCertHolder = basicResponse.getCerts()[0];
                final ContentVerifierProvider verifier = new JcaContentVerifierProviderBuilder().setProvider(SecurityProvider.getProvider()).build(ocspCertHolder);
                assertTrue(basicResponse.isSignatureValid(verifier));

                final COSDictionary ocspSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexOcspSignatureHash));

                // Check that the Cert is in the VRI array
                final COSArray certArray2 = ocspSigDict.getCOSArray(COSName.getPDFName("Cert"));
                final COSStream certStream = (COSStream) certArray2.getObject(0);
                final X509CertificateHolder certHolder2;
                try (COSInputStream is2 = certStream.createInputStream())
                {
                    certHolder2 = new X509CertificateHolder(IOUtils.toByteArray(is2));
                }

                assertEquals(certHolder2, ocspCertHolder, "OCSP certificate is not in the VRI array");
            }
        }
    }

    private byte[] signEncrypted(final SecureRandom secureRandom, final Date signingTime) throws Exception
    {
        final KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(KEYSTORE_PATH), PASSWORD.toCharArray());

        final CreateSignature signing = new CreateSignature(keystore, PASSWORD.toCharArray());
        signing.setExternalSigning(true);

        final File inFile = new File(IN_DIR + "sign_me_protected.pdf");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument doc = null;
        try
        {
            doc = Loader.loadPDF(inFile, " ");

            if (secureRandom != null)
            {
                doc.getEncryption().getSecurityHandler().setCustomSecureRandom(secureRandom);
            }

            final PDSignature signature = new PDSignature();
            signature.setName("Example User");
            final Calendar cal = Calendar.getInstance();
            cal.setTime(signingTime);
            signature.setSignDate(cal);

            doc.addSignature(signature);
            doc.setDocumentId(12345l);
            final ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(baos);
            // invoke external signature service
            return IOUtils.toByteArray(externalSigning.getContent());
        }
        finally
        {
            IOUtils.closeQuietly(doc);
            IOUtils.closeQuietly(baos);
        }
    }
}
