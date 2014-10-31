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
package org.apache.pdfbox.encryption;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.crypto.Cipher;

import junit.framework.TestCase;
import static junit.framework.TestCase.fail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;

/**
 * Tests for public key encryption.
 *
 * @author Ralf Hauser
 */
public class TestSymmetricKeyEncryption extends TestCase
{
    /**
     * Logger instance.
     */
    private static final Log LOG = LogFactory.getLog(TestSymmetricKeyEncryption.class);

    private final File testResultsDir = new File("target/test-output/crypto");

    private AccessPermission permission1;
    private AccessPermission permission2;

    static final String PASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        testResultsDir.mkdirs();

        if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE)
        {
            // we need strong encryption for these tests
            fail("JCE unlimited strength jurisdiction policy files are not installed");
        }

        permission1 = new AccessPermission();
        permission1.setCanAssembleDocument(false);
        permission1.setCanExtractContent(false);
        permission1.setCanExtractForAccessibility(true);
        permission1.setCanFillInForm(false);
        permission1.setCanModify(false);
        permission1.setCanModifyAnnotations(false);
        permission1.setCanPrint(false);
        permission1.setCanPrintDegraded(false);
        permission1.setReadOnly();

        permission2 = new AccessPermission();
        permission2.setCanAssembleDocument(false);
        permission2.setCanExtractContent(false);
        permission2.setCanExtractForAccessibility(true);
        permission2.setCanFillInForm(false);
        permission2.setCanModify(false);
        permission2.setCanModifyAnnotations(false);
        permission2.setCanPrint(true); // it is true now !
        permission2.setCanPrintDegraded(false);
    }

    /**
     * Protect a document with a key and try to reopen it with that key and
     * compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    public void testProtection() throws Exception
    {
        byte[] inputFileAsByteArray = getFileResourceAsByteArray("test.pdf");
        int sizePriorToEncryption = inputFileAsByteArray.length;

        PDDocument document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        testSymmEncrForKeySize(40, sizePriorToEncryption, document, PASSWORD, permission1);

        document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        testSymmEncrForKeySize(128, sizePriorToEncryption, document, PASSWORD, permission1);

        //TODO
        // 1) check permissions
        // 2) 256 key length
        //document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        //testSymmEncrForKeySize(256, sizePriorToEncryption, document, PASSWORD, permission1);
    }

    /**
     * Protect a document with an embedded PDF with a key and try to reopen it
     * with that key and compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    public void testProtectionInnerAttachment() throws Exception
    {
        String testFileName = "preEnc_20141025_105451.pdf";
        byte[] inputFileWithEmbeddedFileAsByteArray = getFileResourceAsByteArray(testFileName);

        PDDocument docWithEmbeddedFile;
        int sizeOfFileWithEmbeddedFile = inputFileWithEmbeddedFileAsByteArray.length;

        File extractedEmbeddedFile
                = extractEmbeddedFile(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray), "innerFile.pdf");

        docWithEmbeddedFile = PDDocument.load(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray));
        testSymmEncrForKeySizeInner(40, sizeOfFileWithEmbeddedFile, docWithEmbeddedFile, extractedEmbeddedFile);

        docWithEmbeddedFile = PDDocument.load(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray));
        testSymmEncrForKeySizeInner(128, sizeOfFileWithEmbeddedFile, docWithEmbeddedFile, extractedEmbeddedFile);

        //TODO enable when 256 key works
        //docWithEmbeddedFile = PDDocument.load(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray));
        //testSymmEncrForKeySizeInner(256, sizeOfFileWithEmbeddedFile, docWithEmbeddedFile, sizeOfEmbeddedFile);
    }

    private void testSymmEncrForKeySize(int keyLength,
            int sizePriorToEncr, PDDocument document, String password, AccessPermission permission) throws IOException
    {
        String prefix = "Simple-";
        int numSrcPages = document.getNumberOfPages();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        ArrayList<BufferedImage> srcImgTab = new ArrayList<BufferedImage>();
        ArrayList<ByteArrayOutputStream> srcContentStreamTab = new ArrayList<ByteArrayOutputStream>();
        for (int i = 0; i < numSrcPages; ++i)
        {
            srcImgTab.add(pdfRenderer.renderImage(i));
            COSStream contentStream = document.getPage(i).getContentStream();
            InputStream unfilteredStream = contentStream.getUnfilteredStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(unfilteredStream, baos);
            unfilteredStream.close();
            srcContentStreamTab.add(baos);
        }

        PDDocument encryptedDoc = encrypt(keyLength, sizePriorToEncr, document, prefix, permission);

        try
        {
            Assert.assertTrue(encryptedDoc.isEncrypted());
            DecryptionMaterial decryptionMaterial = new StandardDecryptionMaterial(PASSWORD);
            encryptedDoc.openProtection(decryptionMaterial);

            AccessPermission newPermission = encryptedDoc.getCurrentAccessPermission();

            Assert.assertEquals(numSrcPages, encryptedDoc.getNumberOfPages());
            pdfRenderer = new PDFRenderer(encryptedDoc);
            for (int i = 0; i < encryptedDoc.getNumberOfPages(); ++i)
            {
                // compare content stream
                BufferedImage bim = pdfRenderer.renderImage(i);
                ValidateXImage.checkIdent(bim, srcImgTab.get(i));

                // compare content streams
                COSStream contentStreamDecr = encryptedDoc.getPage(i).getContentStream();
                InputStream unfilteredStream = contentStreamDecr.getUnfilteredStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(unfilteredStream, baos);
                unfilteredStream.close();
                Assert.assertArrayEquals("content stream of page " + i + " not identical",
                        srcContentStreamTab.get(i).toByteArray(),
                        baos.toByteArray());
            }

            File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-decrypted.pdf");
            encryptedDoc.save(pdfFile);

            boolean canAssembleDocument = newPermission.canAssembleDocument();
            boolean canExtractContent = newPermission.canExtractContent();
            boolean canExtractForAccessibility = newPermission
                    .canExtractForAccessibility();
            boolean canFillInForm = newPermission.canFillInForm();
            boolean canModify = newPermission.canModify();
            boolean canModifyAnnotations = newPermission.canModifyAnnotations();
            boolean canPrint = newPermission.canPrint();
            boolean canPrintDegraded = newPermission.canPrintDegraded();
            encryptedDoc.close();
//            Assert.assertFalse(canAssembleDocument);
//            Assert.assertFalse(canExtractContent);
//            Assert.assertTrue(canExtractForAccessibility);
//            Assert.assertFalse(canFillInForm);
//            Assert.assertFalse(canModify);
//            Assert.assertFalse(canModifyAnnotations);
//            Assert.assertFalse(canPrint);
//            Assert.assertFalse(canPrintDegraded);
        }
        finally
        {
            encryptedDoc.close();
        }
    }

    public PDDocument encrypt(int keyLength, int sizePriorToEncr,
            PDDocument doc, String prefix, AccessPermission permission) throws IOException
    {
        AccessPermission ap = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(PASSWORD, PASSWORD, ap);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPermissions(permission);
        doc.protect(spp);

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-encrypted.pdf");

        doc.save(pdfFile);
        doc.close();
        long sizeEncrypted = pdfFile.length();
        PDDocument encryptedDoc = PDDocument.load(pdfFile);
        Assert.assertTrue(keyLength
                + "-bit encrypted pdf should not have same size as plain one",
                sizeEncrypted != sizePriorToEncr);
        return encryptedDoc;
    }

    // extract the embedded file, saves it, and return the extracted saved file
    private File extractEmbeddedFile(InputStream pdfInputStream, String name) throws IOException
    {
        PDDocument docWithEmbeddedFile;
        docWithEmbeddedFile = PDDocument.load(pdfInputStream);
        PDDocumentCatalog catalog = docWithEmbeddedFile.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
        Map<String, COSObjectable> embeddedFileNames = embeddedFiles.getNames();
        Assert.assertEquals(1, embeddedFileNames.size());
        Map.Entry<String, COSObjectable> entry = embeddedFileNames.entrySet().iterator().next();
        LOG.info("Processing embedded file " + entry.getKey() + ":");
        PDComplexFileSpecification complexFileSpec = (PDComplexFileSpecification) entry.getValue();
        PDEmbeddedFile embeddedFile = complexFileSpec.getEmbeddedFile();

        File resultFile = new File(testResultsDir, name);
        FileOutputStream fos = new FileOutputStream(resultFile);
        InputStream is = embeddedFile.createInputStream();
        IOUtils.copy(is, fos);
        fos.close();
        is.close();

        LOG.info("  size: " + embeddedFile.getSize());
        assertEquals(embeddedFile.getSize(), resultFile.length());

        return resultFile;
    }

    private void testSymmEncrForKeySizeInner(int keyLength,
            int sizePriorToEncr, PDDocument doc,
            File embeddedFilePriorToEncryption) throws IOException
    {
        PDDocument encryptedDoc = encrypt(keyLength, sizePriorToEncr, doc, "ContainsEmbedded-", permission1);

        try
        {
            Assert.assertTrue(encryptedDoc.isEncrypted());
            DecryptionMaterial decryptionMaterial = new StandardDecryptionMaterial(PASSWORD);
            encryptedDoc.openProtection(decryptionMaterial);

            AccessPermission permission = encryptedDoc.getCurrentAccessPermission();

            File decryptedFile = new File(testResultsDir, "DecryptedContainsEmbedded-" + keyLength + "-bit.pdf");
            encryptedDoc.save(decryptedFile);

            File extractedEmbeddedFile = extractEmbeddedFile(new FileInputStream(decryptedFile), "decryptedInnerFile-" + keyLength + "-bit.pdf");

            Assert.assertEquals(keyLength + "-bit decrypted inner attachment pdf should have same size as plain one",
                    embeddedFilePriorToEncryption.length(), extractedEmbeddedFile.length());

            // compare the two embedded files
            Assert.assertArrayEquals(
                    getFileAsByteArray(embeddedFilePriorToEncryption),
                    getFileAsByteArray(extractedEmbeddedFile));

            boolean canAssembleDocument = permission.canAssembleDocument();
            boolean canExtractContent = permission.canExtractContent();
            boolean canExtractForAccessibility = permission
                    .canExtractForAccessibility();
            boolean canFillInForm = permission.canFillInForm();
            boolean canModify = permission.canModify();
            boolean canModifyAnnotations = permission.canModifyAnnotations();
            boolean canPrint = permission.canPrint();
            boolean canPrintDegraded = permission.canPrintDegraded();
            encryptedDoc.close();
            // Assert.assertFalse(canAssembleDocument);
            // Assert.assertFalse(canExtractContent);
            // Assert.assertTrue(canExtractForAccessibility);
            // Assert.assertFalse(canFillInForm);
            // Assert.assertFalse(canModify);
            // Assert.assertFalse(canModifyAnnotations);
            // Assert.assertFalse(canPrint);
            // Assert.assertFalse(canPrintDegraded);
        }
        finally
        {
            encryptedDoc.close();
        }
    }

    private byte[] getStreamAsByteArray(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        return baos.toByteArray();
    }

    private byte[] getFileResourceAsByteArray(String testFileName) throws IOException
    {
        return getStreamAsByteArray(TestSymmetricKeyEncryption.class.getResourceAsStream(testFileName));
    }

    private byte[] getFileAsByteArray(File f) throws IOException
    {
        return getStreamAsByteArray(new FileInputStream(f));
    }

}
