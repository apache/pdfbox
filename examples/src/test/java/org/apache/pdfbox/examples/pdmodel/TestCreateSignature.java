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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
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
    private static CertificateFactory certificateFactory = null;
    private static KeyStore keyStore = null;
    private static final String inDir = "src/test/resources/org/apache/pdfbox/examples/signature/";
    private static final String outDir = "target/test-output/";
    private static final String keystorePath = inDir + "keystore.p12";
    private static final String jpegPath = inDir + "stamp.jpg";
    private static final String password = "123456";
    private static Certificate certificate;
    private static String tsa;

    @Parameterized.Parameter
    public boolean externallySign;

    /**
     * Values for {@link #externallySign} test parameter to specify if signing should be conducted
     * using externally signing scenario ({@code true}) or SignatureInterface ({@code false}).
     */
    @Parameterized.Parameters
    public static Collection signingTypes()
    {
        return Arrays.asList(false, true);
    }

    @BeforeClass
    public static void init() throws Exception
    {
        Security.addProvider(SecurityProvider.getProvider());
        certificateFactory = CertificateFactory.getInstance("X.509");

        // load the keystore
        keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keystorePath), password.toCharArray());

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
    @Test
    public void testDetachedSHA256()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        CreateSignature signing = new CreateSignature(keyStore, password.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileName = getOutputFileName("signed{0}.pdf");
        final String fileName2 = getOutputFileName("signed{0}-late-tsa.pdf");
        signing.signDetached(new File(inDir + "sign_me.pdf"), new File(outDir + fileName));

        checkSignature(new File(inDir, "sign_me.pdf"), new File(outDir, fileName), false);

        // Also test CreateEmbeddedTimeStamp if tsa URL is available
        if (tsa == null || tsa.isEmpty())
        {
            System.err.println("No TSA URL defined, test skipped");
            return;
        }
        
        CreateEmbeddedTimeStamp tsaSigning = new CreateEmbeddedTimeStamp(tsa);
        tsaSigning.embedTimeStamp(new File(outDir, fileName), new File(outDir, fileName2));
        checkSignature(new File(outDir, fileName), new File(outDir, fileName2), true);
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
    @Test
    public void testDetachedSHA256WithTSA()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // mock TSA response content
        InputStream input = new FileInputStream(inDir + "tsa_response.asn1");
        byte[] content = IOUtils.toByteArray(input);
        input.close();

        // mock TSA server (RFC 3161)
        MockHttpServer mockServer = new MockHttpServer(15371);
        mockServer.startServer();
        String brokenMockTSA = "http://localhost:" + mockServer.getServerPort() + "/";
        MockHttpServer.MockHttpServerResponse response = new MockHttpServer.MockHttpServerResponse();
        response.setMockResponseContent(content);
        response.setMockResponseContentType("application/timestamp-reply");
        response.setMockResponseCode(200);
        mockServer.setMockHttpServerResponses(response);

        String inPath = inDir + "sign_me_tsa.pdf";
        String outPath = outDir + getOutputFileName("signed{0}_tsa.pdf");

        // sign PDF (will fail due to nonce and timestamp differing)
        CreateSignature signing1 = new CreateSignature(keyStore, password.toCharArray());
        signing1.setExternalSigning(externallySign);
        try
        {
            signing1.signDetached(new File(inPath), new File(outPath), brokenMockTSA);
            Assert.fail("This should have failed");
        }
        catch (IOException e)
        {
            Assert.assertTrue(e.getCause() instanceof TSPValidationException);
            new File(outPath).delete();
        }

        mockServer.stopServer();

        if (tsa == null || tsa.isEmpty())
        {
            System.err.println("No TSA URL defined, test skipped");
            return;
        }

        CreateSignature signing2 = new CreateSignature(keyStore, password.toCharArray());
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
    public void testCreateSignedTimeStamp()
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
        CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsa);
        signing.signDetached(new File(inDir + "sign_me.pdf"), new File(outDir + fileName));

        PDDocument doc = PDDocument.load(new File(outDir + fileName));
        PDSignature signature = doc.getLastSignatureDictionary();
        COSString contents = (COSString) signature.getCOSObject().getDictionaryObject(COSName.CONTENTS);
        byte[] signedFileContent =
                signature.getSignedContent(new FileInputStream(new File(outDir, fileName)));
        TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents.getBytes()));
        certificateFactory.getInstance("X.509");
        ByteArrayInputStream certStream = new ByteArrayInputStream(contents.getBytes());
        Collection<? extends Certificate> certs = certificateFactory.generateCertificates(certStream);

        String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
        // compare the hash of the signed content with the hash in the timestamp
        Assert.assertArrayEquals(MessageDigest.getInstance(hashAlgorithm).digest(signedFileContent),
                timeStampToken.getTimeStampInfo().getMessageImprintDigest());

        X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
        SigUtils.validateTimestampToken(timeStampToken);
        SigUtils.verifyCertificateChain(timeStampToken.getCertificates(),
                certFromTimeStamp,
                timeStampToken.getTimeStampInfo().getGenTime());

        doc.close();
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
    @Test
    public void testCreateVisibleSignature()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        String inPath = inDir + "sign_me_visible.pdf";
        FileInputStream fis = new FileInputStream(jpegPath);
        CreateVisibleSignature signing = new CreateVisibleSignature(keyStore, password.toCharArray());
        signing.setVisibleSignDesigner(inPath, 0, 0, -50, fis, 1);
        signing.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing.setExternalSigning(externallySign);

        File destFile = new File(outDir + getOutputFileName("signed{0}_visible.pdf"));
        signing.signPDF(new File(inPath), destFile, null);
        fis.close();

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
    @Test
    public void testCreateVisibleSignature2()
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
                   TSPException, CertificateVerificationException
    {
        // sign PDF
        String inPath = inDir + "sign_me_visible.pdf";
        File destFile;

        CreateVisibleSignature2 signing = new CreateVisibleSignature2(keyStore, password.toCharArray());
        Rectangle2D humanRect = new Rectangle2D.Float(100, 200, 150, 50);
        signing.setImageFile(new File(jpegPath));
        signing.setExternalSigning(externallySign);
        destFile = new File(outDir + getOutputFileName("signed{0}_visible2.pdf"));
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
    public void testPDFBox3978() throws IOException, NoSuchAlgorithmException, 
                                        CertificateException, UnrecoverableKeyException, 
                                        CMSException, OperatorCreationException, GeneralSecurityException,
                                        TSPException, CertificateVerificationException
    {
        String filename        = outDir + "EmptySignatureForm.pdf";
        String filenameSigned1 = outDir + "EmptySignatureForm-signed1.pdf";
        String filenameSigned2 = outDir + "EmptySignatureForm-signed2.pdf";

        if (!externallySign)
        {
            return;
        }

        // create file with empty signature
        CreateEmptySignatureForm.main(new String[]{filename});

        // sign PDF
        CreateSignature signing1 = new CreateSignature(keyStore, password.toCharArray());
        signing1.setExternalSigning(false);
        signing1.signDetached(new File(filename), new File(filenameSigned1));

        checkSignature(new File(filename), new File(filenameSigned1), false);

        PDDocument doc1 = PDDocument.load(new File(filenameSigned1));
        List<PDSignature> signatureDictionaries = doc1.getSignatureDictionaries();
        Assert.assertEquals(1, signatureDictionaries.size());
        doc1.close();

        // do visual signing in the field
        FileInputStream fis = new FileInputStream(jpegPath);
        CreateVisibleSignature signing2 = new CreateVisibleSignature(keyStore, password.toCharArray());
        signing2.setVisibleSignDesigner(filenameSigned1, 0, 0, -50, fis, 1);
        signing2.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing2.setExternalSigning(true);
        signing2.signPDF(new File(filenameSigned1), new File(filenameSigned2), null, "Signature1");
        fis.close();

        checkSignature(new File(filenameSigned1), new File(filenameSigned2), false);

        PDDocument doc2 = PDDocument.load(new File(filenameSigned2));
        signatureDictionaries = doc2.getSignatureDictionaries();
        Assert.assertEquals(2, signatureDictionaries.size());
        doc2.close();
    }

    private String getOutputFileName(String filePattern)
    {
        return MessageFormat.format(filePattern,(externallySign ? "_ext" : ""));
    }

    // This check fails with a file created with the code before PDFBOX-3011 was solved.
    private void checkSignature(File origFile, File signedFile, boolean checkTimeStamp)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException,
            TSPException, CertificateVerificationException
    {
        PDDocument document = PDDocument.load(origFile);
        // get string representation of pages COSObject
        String origPageKey = document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString();
        document.close();

        document = PDDocument.load(signedFile);
        // PDFBOX-4261: check that object number stays the same 
        Assert.assertEquals(origPageKey, document.getDocumentCatalog().getCOSObject().getItem(COSName.PAGES).toString());

        List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
        if (signatureDictionaries.isEmpty())
        {
            Assert.fail("no signature found");
        }
        for (PDSignature sig : document.getSignatureDictionaries())
        {
            COSString contents = (COSString) sig.getCOSObject().getDictionaryObject(COSName.CONTENTS);
            
            byte[] buf = sig.getSignedContent(new FileInputStream(signedFile));

            // verify that getSignedContent() brings the same content
            // regardless whether from an InputStream or from a byte array
            FileInputStream fis2 = new FileInputStream(signedFile);
            byte[] buf2 = sig.getSignedContent(IOUtils.toByteArray(fis2));
            Assert.assertArrayEquals(buf, buf2);
            fis2.close();

            // verify that all getContents() methods returns the same content
            FileInputStream fis3 = new FileInputStream(signedFile);
            byte[] contents2 = sig.getContents(IOUtils.toByteArray(fis3));
            Assert.assertArrayEquals(contents.getBytes(), contents2);
            fis3.close();
            byte[] contents3 = sig.getContents(new FileInputStream(signedFile));
            Assert.assertArrayEquals(contents.getBytes(), contents3);

            // inspiration:
            // http://stackoverflow.com/a/26702631/535646
            // http://stackoverflow.com/a/9261365/535646
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
            Store certificatesStore = signedData.getCertificates();
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation signerInformation = signers.iterator().next();
            Collection matches = certificatesStore.getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
            X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
            Assert.assertArrayEquals(certificate.getEncoded(), certificateHolder.getEncoded());

            // CMSVerifierCertificateNotValidException means that the keystore wasn't valid at signing time
            if (!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificateHolder)))
            {
                Assert.fail("Signature verification failed");
            }

            TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
            if (checkTimeStamp)
            {
                Assert.assertNotNull(timeStampToken);
                SigUtils.validateTimestampToken(timeStampToken);

                TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

                // compare the hash of the signed content with the hash in the timestamp
                byte[] tsMessageImprintDigest = timeStampInfo.getMessageImprintDigest();
                String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
                byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
                Assert.assertArrayEquals("timestamp signature verification failed", sigMessageImprintDigest, tsMessageImprintDigest);

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
                Assert.assertNull(timeStampToken);
            }
        }
        document.close();
    }

    private String calculateDigestString(InputStream inputStream) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Hex.getString(md.digest(IOUtils.toByteArray(inputStream)));
    }

    /**
     * PDFBOX-3811: make sure that calling saveIncrementalForExternalSigning() more than once
     * brings the same result.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testPDFBox3811() throws IOException, NoSuchAlgorithmException
    {
        if (!externallySign)
        {
            return;
        }
        
        // create simple PDF
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        new PDPageContentStream(document, page).close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        
        document = PDDocument.load(baos.toByteArray());
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
        Assert.assertTrue("IllegalStateException should have been thrown", caught);
        signature.setByteRange(reserveByteRange);
        Assert.assertEquals(digestString, calculateDigestString(document.saveIncrementalForExternalSigning(new ByteArrayOutputStream()).getContent()));
    }

    /**
     * Create a simple form PDF, sign it, reload it, change a field value, incrementally save it.
     * This should not break the signature, and the value and its display must have changed as
     * expected. Do this both for the old and new incremental save methods.
     *
     * @throws Exception
     */
    @Test
    public void testSaveIncrementalAfterSign() throws Exception
    {
        BufferedImage oldImage, expectedImage1, actualImage1, expectedImage2, actualImage2;
        DataBufferInt expectedData;
        DataBufferInt actualData;
        PDField field;
        FileOutputStream fileOutputStream;

        CreateSimpleForm.main(new String[0]); // creates "target/SimpleForm.pdf"

        // sign PDF
        CreateSignature signing = new CreateSignature(keyStore, password.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileNameSigned = getOutputFileName("SimpleForm_signed{0}.pdf");
        final String fileNameResaved1 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved1.pdf");
        final String fileNameResaved2 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved2.pdf");
        signing.signDetached(new File("target/SimpleForm.pdf"), new File(outDir + fileNameSigned));

        checkSignature(new File("target/SimpleForm.pdf"), new File(outDir, fileNameSigned), false);

        PDDocument doc = PDDocument.load(new File(outDir, fileNameSigned));

        oldImage = new PDFRenderer(doc).renderImage(0);

        fileOutputStream = new FileOutputStream(new File(outDir, fileNameResaved1));
        field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
        field.setValue("New Value 1");

        // Test of PDFBOX-4509: only "Helv" font should be there
        Collection<COSName> fonts = (Collection<COSName>) field.getWidgets().get(0).getAppearance().
                getNormalAppearance().getAppearanceStream().getResources().getFontNames();
        Assert.assertTrue(fonts.contains(COSName.HELV));
        Assert.assertEquals(1, fonts.size());

        expectedImage1 = new PDFRenderer(doc).renderImage(0);

        // compare images, image must has changed
        Assert.assertEquals(oldImage.getWidth(), expectedImage1.getWidth());
        Assert.assertEquals(oldImage.getHeight(), expectedImage1.getHeight());
        Assert.assertEquals(oldImage.getType(), expectedImage1.getType());
        expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
        actualData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
        Assert.assertEquals(expectedData.getData().length, actualData.getData().length);
        Assert.assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

        // old style incremental save: create a "path" from the root to the objects that need an update
        doc.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);
        doc.getDocumentCatalog().getAcroForm().getCOSObject().setNeedToBeUpdated(true);
        field.getCOSObject().setNeedToBeUpdated(true);
        field.getWidgets().get(0).getAppearance().getCOSObject().setNeedToBeUpdated(true);
        ((COSDictionary) field.getWidgets().get(0).getAppearance().getNormalAppearance().getCOSObject()).setNeedToBeUpdated(true);
        doc.saveIncremental(fileOutputStream);
        doc.close();
        checkSignature(new File("target/SimpleForm.pdf"), new File(outDir, fileNameResaved1), false);

        doc = PDDocument.load(new File(outDir, fileNameResaved1));

        field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
        Assert.assertEquals("New Value 1", field.getValueAsString());
        actualImage1 = new PDFRenderer(doc).renderImage(0);
        // compare images, equality proves that the appearance has been updated too
        Assert.assertEquals(expectedImage1.getWidth(), actualImage1.getWidth());
        Assert.assertEquals(expectedImage1.getHeight(), actualImage1.getHeight());
        Assert.assertEquals(expectedImage1.getType(), actualImage1.getType());
        expectedData = (DataBufferInt) expectedImage1.getRaster().getDataBuffer();
        actualData = (DataBufferInt) actualImage1.getRaster().getDataBuffer();
        Assert.assertArrayEquals(expectedData.getData(), actualData.getData());
        doc.close();
    }

    @Test
    public void testPDFBox4784() throws Exception
    {
        if (!externallySign)
        {
            return;
        }
        Date signingTime = new Date();

        byte[] defaultSignedOne = signEncrypted(null, signingTime);
        byte[] defaultSignedTwo = signEncrypted(null, signingTime);
        Assert.assertFalse(Arrays.equals(defaultSignedOne, defaultSignedTwo));

        // a dummy value for FixedSecureRandom is used (for real use-cases a secure value should be provided)
        byte[] fixedRandomSignedOne = signEncrypted(new FixedSecureRandom(new byte[128]),
                signingTime);
        byte[] fixedRandomSignedTwo = signEncrypted(new FixedSecureRandom(new byte[128]),
                signingTime);
        Assert.assertArrayEquals(fixedRandomSignedOne, fixedRandomSignedTwo);
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
    public void testCRL() throws IOException, CMSException, CertificateException, TSPException,
            OperatorCreationException, CertificateVerificationException, NoSuchAlgorithmException
    {
        if (externallySign)
        {
            return; // runs only once, independent of externallySign
        }
        String hexSignature;
        BufferedReader bfr
                = new BufferedReader(new InputStreamReader(new FileInputStream(inDir + "hexsignature.txt")));
        hexSignature = bfr.readLine();
        bfr.close();

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
        HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<X509CertificateHolder>();
        certificateHolderSet.addAll(certificatesStore.getMatches(null));
        certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
        SigUtils.verifyCertificateChain(new CollectionStore<X509CertificateHolder>(certificateHolderSet),
                certFromTimeStamp,
                timeStampToken.getTimeStampInfo().getGenTime());
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);

        // compare the hash of the signature with the hash in the timestamp
        byte[] tsMessageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
        String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
        byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
        Assert.assertArrayEquals(tsMessageImprintDigest, sigMessageImprintDigest);

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
    public void testAddValidationInformation()
            throws IOException, GeneralSecurityException, OCSPException, OperatorCreationException, CMSException
    {
        if (externallySign)
        {
            return; // runs only once, independent of externallySign
        }
        File inFile = new File("target/pdfs", "QV_RCA1_RCA3_CPCPS_V4_11.pdf");
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(outDir, substring + "_LTV.pdf");
        AddValidationInformation addValidationInformation = new AddValidationInformation();
        addValidationInformation.validateSignature(inFile, outFile);

        certificateFactory.getInstance("X.509");
        PDDocument doc = PDDocument.load(outFile);

        PDSignature signature = doc.getLastSignatureDictionary();
        COSString contents = (COSString) signature.getCOSObject().getDictionaryObject(COSName.CONTENTS);

        PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
        COSDictionary dssDict = docCatalog.getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
        COSArray dssCertArray = dssDict.getCOSArray(COSName.getPDFName("Certs"));
        COSDictionary vriDict = dssDict.getCOSDictionary(COSName.getPDFName("VRI"));

        // Check that all known signature certificates are in the VRI/signaturehash/Cert array
        byte[] signatureHash = MessageDigest.getInstance("SHA-1").digest(contents.getBytes());
        String hexSignatureHash = Hex.getString(signatureHash);
        System.out.println("hexSignatureHash: " + hexSignatureHash);
        CMSSignedData signedData = new CMSSignedData(contents.getBytes());
        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        HashSet<X509CertificateHolder> certificateHolderSet =
                new HashSet<X509CertificateHolder>(certificatesStore.getMatches(null));
        COSDictionary sigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexSignatureHash));
        COSArray sigCertArray = sigDict.getCOSArray(COSName.getPDFName("Cert"));
        Set<X509CertificateHolder> sigCertHolderSetFromVRIArray = new HashSet<X509CertificateHolder>();
        for (int i = 0; i < sigCertArray.size(); ++i)
        {
            COSStream certStream = (COSStream) sigCertArray.getObject(i);
            COSInputStream is = certStream.createInputStream();
            sigCertHolderSetFromVRIArray.add(new X509CertificateHolder(IOUtils.toByteArray(is)));
            is.close();
        }
        for (X509CertificateHolder holder : certificateHolderSet)
        {
            if (holder.getSubject().toString().contains("QuoVadis OCSP Authority Signature"))
            {
                continue; // not relevant here
            }
            Assert.assertTrue("VRI/signaturehash/Cert array doesn't contain " + holder.getSubject(),
                    sigCertHolderSetFromVRIArray.contains(holder));
        }

        // Get all certificates. Each one should either be issued (= signed) by a certificate of the set
        Set<X509Certificate> certSet = new HashSet<X509Certificate>();
        for (int i = 0; i < dssCertArray.size(); ++i)
        {
            COSStream certStream = (COSStream) dssCertArray.getObject(i);
            COSInputStream is = certStream.createInputStream();
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
            is.close();
            certSet.add(cert);
        }
        for (X509Certificate cert : certSet)
        {
            boolean verified = false;
            for (X509Certificate cert2 : certSet)
            {
                try
                {
                    cert.verify(cert2.getPublicKey(), SecurityProvider.getProvider().getName());
                    verified = true;
                }
                catch (GeneralSecurityException ex)
                {
                    // not the issuer
                }
            }
            Assert.assertTrue("Certificate " + cert.getSubjectX500Principal() +
                    " not issued by any certificate in the Certs array", verified);
        }

        // Each CRL should be signed by one of the certificates in Certs
        Set<X509CRL> crlSet = new HashSet<X509CRL>();
        COSArray crlArray = dssDict.getCOSArray(COSName.getPDFName("CRLs"));
        for (int i = 0; i < crlArray.size(); ++i)
        {
            COSStream crlStream = (COSStream) crlArray.getObject(i);
            COSInputStream is = crlStream.createInputStream();
            X509CRL cert = (X509CRL) certificateFactory.generateCRL(is);
            is.close();
            crlSet.add(cert);
        }
        for (X509CRL crl : crlSet)
        {
            boolean crlVerified = false;
            X509Certificate crlIssuerCert = null;
            for (X509Certificate cert : certSet)
            {
                try
                {
                    crl.verify(cert.getPublicKey(), SecurityProvider.getProvider().getName());
                    crlVerified = true;
                    crlIssuerCert = cert;
                }
                catch (GeneralSecurityException ex)
                {
                    // not the issuer
                }
            }
            Assert.assertTrue("issuer of CRL not found in Certs array", crlVerified);

            byte[] crlSignatureHash = MessageDigest.getInstance("SHA-1").digest(crl.getSignature());
            String hexCrlSignatureHash = Hex.getString(crlSignatureHash);
            System.out.println("hexCrlSignatureHash: " + hexCrlSignatureHash);

            // Check that the issueing certificate is in the VRI array
            COSDictionary crlSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexCrlSignatureHash));
            COSArray certArray2 = crlSigDict.getCOSArray(COSName.getPDFName("Cert"));
            COSStream certStream = (COSStream) certArray2.getObject(0);
            COSInputStream is2 = certStream.createInputStream();
            X509CertificateHolder certHolder2 = new X509CertificateHolder(IOUtils.toByteArray(is2));
            is2.close();

            Assert.assertEquals("CRL issuer certificate missing in VRI " + hexCrlSignatureHash,
                    certHolder2, new X509CertificateHolder(crlIssuerCert.getEncoded()));
        }

        Set<OCSPResp> oscpSet = new HashSet<OCSPResp>();
        COSArray ocspArray = dssDict.getCOSArray(COSName.getPDFName("OCSPs"));
        for (int i = 0; i < ocspArray.size(); ++i)
        {
            COSStream ocspStream = (COSStream) ocspArray.getObject(i);
            COSInputStream is = ocspStream.createInputStream();
            OCSPResp ocspResp = new OCSPResp(is);
            is.close();
            oscpSet.add(ocspResp);
        }
        for (OCSPResp ocspResp : oscpSet)
        {
            BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResp.getResponseObject();
            Assert.assertEquals(OCSPResponseStatus.SUCCESSFUL, ocspResp.getStatus());
            Assert.assertTrue("OCSP should have at least 1 certificate", basicResponse.getCerts().length >= 1);
            byte[] ocspSignatureHash = MessageDigest.getInstance("SHA-1").digest(basicResponse.getSignature());
            String hexOcspSignatureHash = Hex.getString(ocspSignatureHash);
            System.out.println("ocspSignatureHash: " + hexOcspSignatureHash);
            long secondsOld = (System.currentTimeMillis() - basicResponse.getProducedAt().getTime()) / 1000;
            Assert.assertTrue("OCSP answer is too old, is from " + secondsOld + " seconds ago",
                        secondsOld < 10);

            X509CertificateHolder ocspCertHolder = basicResponse.getCerts()[0];
            ContentVerifierProvider verifier = new JcaContentVerifierProviderBuilder().setProvider(SecurityProvider.getProvider()).build(ocspCertHolder);
            Assert.assertTrue(basicResponse.isSignatureValid(verifier));

            COSDictionary ocspSigDict = vriDict.getCOSDictionary(COSName.getPDFName(hexOcspSignatureHash));

            // Check that the Cert is in the VRI array
            COSArray certArray2 = ocspSigDict.getCOSArray(COSName.getPDFName("Cert"));
            COSStream certStream = (COSStream) certArray2.getObject(0);
            COSInputStream is2 = certStream.createInputStream();
            X509CertificateHolder certHolder2 = new X509CertificateHolder(IOUtils.toByteArray(is2));
            is2.close();

            Assert.assertEquals("OCSP certificate is not in the VRI array", certHolder2, ocspCertHolder);
        }

        doc.close();
    }

    private byte[] signEncrypted(SecureRandom secureRandom, Date signingTime) throws Exception
    {
        CreateSignature signing = new CreateSignature(keyStore, password.toCharArray());
        signing.setExternalSigning(true);

        File inFile = new File(inDir + "sign_me_protected.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(inFile, " ");

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
            return IOUtils.toByteArray(externalSigning.getContent());
        }
        finally
        {
            IOUtils.closeQuietly(doc);
            IOUtils.closeQuietly(baos);
        }
    }
}
