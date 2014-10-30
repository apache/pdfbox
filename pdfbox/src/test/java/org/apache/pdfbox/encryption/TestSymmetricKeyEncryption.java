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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.crypto.Cipher;

import junit.framework.TestCase;
import static junit.framework.TestCase.fail;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
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
    private final File testResultsDir = new File("target/test-output/crypto");

    private AccessPermission permission1;
    private AccessPermission permission2;

    private int sizePriorToEncryption = -1;
    private int sizePriorToEncryptionInnerSubfile = -1;

    static final String PASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";

    static byte[] inputFileAsByteArray = null;
    static byte[] inputInner = null;

    static long page0size = -1;

    /**
     * Simple test document that gets encrypted by the test cases.
     */
    private PDDocument document;

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

        String testFileName = "test.pdf";
        inputFileAsByteArray = getFileAsByteArray(testFileName);
        try
        {
            sizePriorToEncryption = inputFileAsByteArray.length;
            document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        }
        catch (Exception t)
        {
            System.err.println(testFileName + " " + t.getMessage());
            t.printStackTrace();
            throw t;
        }
    }

    public byte[] getFileAsByteArray(String testFileName) throws Exception,
                                                                 IOException
    {
        InputStream is = TestSymmetricKeyEncryption.class
                .getResourceAsStream(testFileName);
        try
        {
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true)
                {
                    int c = is.read();
                    if (c == -1)
                    {
                        break;
                    }
                    baos.write(c);
                }
                baos.close();
                return baos.toByteArray();
            }
            finally
            {
                is.close();
            }
        }
        catch (Exception t)
        {
            System.err.println(testFileName + " " + t.getMessage());
            t.printStackTrace();
            throw t;
        }
        finally
        {
            is.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception
    {
        document.close();
    }

    /**
     * Protect a document with a public certificate and try to open it with the
     * corresponding private certificate.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    public void testProtection() throws Exception
    {
        testSymmEncrForKeySize(40, sizePriorToEncryption, document, PASSWORD, permission1);
        document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        testSymmEncrForKeySize(128, sizePriorToEncryption, document, PASSWORD, permission1);

        //TODO
        // 1) check permissions
        // 2) 256 key length
        //document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        //testSymmEncrForKeySize(256, sizePriorToEncryption, document, PASSWORD);
    }

    public void testSymmEncrForKeySize(int keyLength,
            int sizePriorToEncr, PDDocument doc, String password, AccessPermission permission) throws IOException
    {
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

        PDDocument encryptedDoc = encrypt(keyLength, sizePriorToEncr, doc, "", permission);

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

            File pdfFile = new File(testResultsDir, keyLength + "-bit-decrypted.pdf");
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
            PDDocument doc, String spec, AccessPermission permission) throws IOException
    {
        AccessPermission ap = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(PASSWORD, PASSWORD, ap);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPermissions(permission);
        doc.protect(spp);

        File pdfFile = new File(testResultsDir, keyLength + "-bit-encrypted.pdf");

        doc.save(pdfFile);
        doc.close();
        long sizeEncrypted = pdfFile.length();
        PDDocument encryptedDoc = PDDocument.load(pdfFile);
        Assert.assertTrue(keyLength
                + "-bit encrypted pdf should not have same size as plain one",
                sizeEncrypted != sizePriorToEncr);

        return encryptedDoc;
    }


}
