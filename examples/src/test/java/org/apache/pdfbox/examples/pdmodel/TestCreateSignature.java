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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.examples.interactive.form.CreateSimpleForm;
import org.apache.pdfbox.examples.signature.CreateEmptySignatureForm;
import org.apache.pdfbox.examples.signature.CreateSignature;
import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Hex;
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

        checkSignature(new File(inDir, "sign_me.pdf"), new File(outDir, fileName));
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
            signing.signDetached(new File(inPath), new File(outPath), tsaUrl);
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

        checkSignature(new File(inPath), destFile);
    }

    /**
     * Test when visually signing externally on an existing signature field on a file which has
     * been signed before.
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException 
     */
    @Test
    public void testPDFBox3978() throws IOException, NoSuchAlgorithmException, KeyStoreException, 
                                        CertificateException, UnrecoverableKeyException, 
                                        CMSException, OperatorCreationException, GeneralSecurityException
    {
        String filename        = outDir + "EmptySignatureForm.pdf";
        String filenameSigned1 = outDir + "EmptySignatureForm-signed1.pdf";
        String filenameSigned2 = outDir + "EmptySignatureForm-signed2.pdf";

        if (!externallySign)
        {
            return;
        }

        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // create file with empty signature
        CreateEmptySignatureForm.main(new String[]{filename});

        // sign PDF
        CreateSignature signing1 = new CreateSignature(keystore, password.toCharArray());
        signing1.setExternalSigning(false);
        signing1.signDetached(new File(filename), new File(filenameSigned1));

        checkSignature(new File(filename), new File(filenameSigned1));

        PDDocument doc1 = PDDocument.load(new File(filenameSigned1));
        List<PDSignature> signatureDictionaries = doc1.getSignatureDictionaries();
        Assert.assertEquals(1, signatureDictionaries.size());
        doc1.close();

        // do visual signing in the field
        FileInputStream fis = new FileInputStream(jpegPath);
        CreateVisibleSignature signing2 = new CreateVisibleSignature(keystore, password.toCharArray());
        signing2.setVisibleSignDesigner(filenameSigned1, 0, 0, -50, fis, 1);
        signing2.setVisibleSignatureProperties("name", "location", "Security", 0, 1, true);
        signing2.setExternalSigning(externallySign);
        signing2.signPDF(new File(filenameSigned1), new File(filenameSigned2), null, "Signature1");
        fis.close();

        checkSignature(new File(filenameSigned1), new File(filenameSigned2));

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
    private void checkSignature(File origFile, File signedFile)
            throws IOException, CMSException, OperatorCreationException, GeneralSecurityException
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

            // inspiration:
            // http://stackoverflow.com/a/26702631/535646
            // http://stackoverflow.com/a/9261365/535646
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
            Store certificatesStore = signedData.getCertificates();
            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation signerInformation = signers.iterator().next();
            Collection matches = certificatesStore.getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
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

        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), password.toCharArray());

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password.toCharArray());
        signing.setExternalSigning(externallySign);

        final String fileNameSigned = getOutputFileName("SimpleForm_signed{0}.pdf");
        final String fileNameResaved1 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved1.pdf");
        final String fileNameResaved2 = getOutputFileName("SimpleForm_signed{0}_incrementallyresaved2.pdf");
        signing.signDetached(new File("target/SimpleForm.pdf"), new File(outDir + fileNameSigned));

        checkSignature(new File("target/SimpleForm.pdf"), new File(outDir, fileNameSigned));

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
        checkSignature(new File("target/SimpleForm.pdf"), new File(outDir, fileNameResaved1));

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

        doc = PDDocument.load(new File(outDir, fileNameSigned));

        fileOutputStream = new FileOutputStream(new File(outDir, fileNameResaved2));
        field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
        field.setValue("New Value 2");
        expectedImage2 = new PDFRenderer(doc).renderImage(0);

        // compare images, image must has changed
        Assert.assertEquals(oldImage.getWidth(), expectedImage2.getWidth());
        Assert.assertEquals(oldImage.getHeight(), expectedImage2.getHeight());
        Assert.assertEquals(oldImage.getType(), expectedImage2.getType());
        expectedData = (DataBufferInt) oldImage.getRaster().getDataBuffer();
        actualData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
        Assert.assertEquals(expectedData.getData().length, actualData.getData().length);
        Assert.assertFalse(Arrays.equals(expectedData.getData(), actualData.getData()));

        // new style incremental save: add only the objects that have changed
        Set<COSDictionary> objectsToWrite = new HashSet<COSDictionary>();
        objectsToWrite.add(field.getCOSObject());
        objectsToWrite.add(field.getWidgets().get(0).getAppearance().getCOSObject());
        objectsToWrite.add((COSDictionary) field.getWidgets().get(0).getAppearance().getNormalAppearance().getCOSObject());
        doc.saveIncremental(fileOutputStream, objectsToWrite);
        doc.close();

        checkSignature(new File("target/SimpleForm.pdf"), new File(outDir, fileNameResaved2));
        doc = PDDocument.load(new File(outDir, fileNameResaved2));

        field = doc.getDocumentCatalog().getAcroForm().getField("SampleField");
        Assert.assertEquals("New Value 2", field.getValueAsString());
        actualImage2 = new PDFRenderer(doc).renderImage(0);
        // compare images, equality proves that the appearance has been updated too
        Assert.assertEquals(expectedImage2.getWidth(), actualImage2.getWidth());
        Assert.assertEquals(expectedImage2.getHeight(), actualImage2.getHeight());
        Assert.assertEquals(expectedImage2.getType(), actualImage2.getType());
        expectedData = (DataBufferInt) expectedImage2.getRaster().getDataBuffer();
        actualData = (DataBufferInt) actualImage2.getRaster().getDataBuffer();
        Assert.assertArrayEquals(expectedData.getData(), actualData.getData());
        doc.close();
    }
}
