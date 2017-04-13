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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;

/**
 * Tests for symmetric key encryption.
 *
 * IMPORTANT! When making changes in the encryption / decryption methods, do
 * also check whether the six generated encrypted files (to be found in
 * pdfbox/target/test-output/crypto and named *encrypted.pdf) can be opened with
 * Adobe Reader by providing the owner password and the user password.
 *
 * @author Ralf Hauser
 * @author Tilman Hausherr
 *
 */
public class TestSymmetricKeyEncryption extends TestCase
{
    /**
     * Logger instance.
     */
    private static final Log LOG = LogFactory.getLog(TestSymmetricKeyEncryption.class);

    private final File testResultsDir = new File("target/test-output/crypto");

    private AccessPermission permission;

    static final String USERPASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";
    static final String OWNERPASSWORD = "abcdefghijk1234567890abcdefghijk1234567890";

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

        permission = new AccessPermission();
        permission.setCanAssembleDocument(false);
        permission.setCanExtractContent(false);
        permission.setCanExtractForAccessibility(true);
        permission.setCanFillInForm(false);
        permission.setCanModify(false);
        permission.setCanModifyAnnotations(false);
        permission.setCanPrint(true);
        permission.setCanPrintDegraded(false);
        permission.setReadOnly();
    }

    /**
     * Test that permissions work as intended: the user psw ("user") is enough
     * to open the PDF with possibly restricted rights, the owner psw ("owner")
     * gives full permissions. The 3 files of this test were created by Maruan
     * Sayhoun, NOT with PDFBox, but with Adobe Acrobat to ensure "the gold
     * standard". The restricted permissions prevent printing and text
     * extraction. In the 128 and 256 bit encrypted files, AssembleDocument,
     * ExtractForAccessibility and PrintDegraded are also disabled.
     */
    public void testPermissions() throws Exception
    {
        AccessPermission fullAP = new AccessPermission();
        AccessPermission restrAP = new AccessPermission();
        restrAP.setCanPrint(false);
        restrAP.setCanExtractContent(false);
        restrAP.setCanModify(false);

        byte[] inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-40bit.pdf");
        checkPerms(inputFileAsByteArray, "owner", fullAP);
        checkPerms(inputFileAsByteArray, "user", restrAP);
        try
        {
            checkPerms(inputFileAsByteArray, "", null);
            fail("wrong password not detected");
        }
        catch (IOException ex)
        {
            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
        }

        restrAP.setCanAssembleDocument(false);
        restrAP.setCanExtractForAccessibility(false);
        restrAP.setCanPrintDegraded(false);

        inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-128bit.pdf");
        checkPerms(inputFileAsByteArray, "owner", fullAP);
        checkPerms(inputFileAsByteArray, "user", restrAP);
        try
        {
            checkPerms(inputFileAsByteArray, "", null);
            fail("wrong password not detected");
        }
        catch (IOException ex)
        {
            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
        }

        inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-256bit.pdf");
        checkPerms(inputFileAsByteArray, "owner", fullAP);
        checkPerms(inputFileAsByteArray, "user", restrAP);
        try
        {
            checkPerms(inputFileAsByteArray, "", null);
            fail("wrong password not detected");
        }
        catch (IOException ex)
        {
            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
        }
    }

    private void checkPerms(byte[] inputFileAsByteArray, String password,
            AccessPermission expectedPermissions) throws IOException
    {
        PDDocument doc = PDDocument.load(inputFileAsByteArray, password);

        AccessPermission currentAccessPermission = doc.getCurrentAccessPermission();

        // check permissions
        assertEquals(expectedPermissions.isOwnerPermission(), currentAccessPermission.isOwnerPermission());
        if (!expectedPermissions.isOwnerPermission())
        {
            assertEquals(true, currentAccessPermission.isReadOnly());
        }
        assertEquals(expectedPermissions.canAssembleDocument(), currentAccessPermission.canAssembleDocument());
        assertEquals(expectedPermissions.canExtractContent(), currentAccessPermission.canExtractContent());
        assertEquals(expectedPermissions.canExtractForAccessibility(), currentAccessPermission.canExtractForAccessibility());
        assertEquals(expectedPermissions.canFillInForm(), currentAccessPermission.canFillInForm());
        assertEquals(expectedPermissions.canModify(), currentAccessPermission.canModify());
        assertEquals(expectedPermissions.canModifyAnnotations(), currentAccessPermission.canModifyAnnotations());
        assertEquals(expectedPermissions.canPrint(), currentAccessPermission.canPrint());
        assertEquals(expectedPermissions.canPrintDegraded(), currentAccessPermission.canPrintDegraded());

        new PDFRenderer(doc).renderImage(0);

        doc.close();
    }

    /**
     * Protect a document with a key and try to reopen it with that key and compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    public void testProtection() throws Exception
    {
        byte[] inputFileAsByteArray = getFileResourceAsByteArray("Acroform-PDFBOX-2333.pdf");
        int sizePriorToEncryption = inputFileAsByteArray.length;

        testSymmEncrForKeySize(40, false, sizePriorToEncryption, inputFileAsByteArray, 
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(128, false, sizePriorToEncryption, inputFileAsByteArray, 
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(128, true, sizePriorToEncryption, inputFileAsByteArray, 
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(256, true, sizePriorToEncryption, inputFileAsByteArray, 
                USERPASSWORD, OWNERPASSWORD, permission);
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

        int sizeOfFileWithEmbeddedFile = inputFileWithEmbeddedFileAsByteArray.length;

        File extractedEmbeddedFile
                = extractEmbeddedFile(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray), "innerFile.pdf");

        testSymmEncrForKeySizeInner(40, false, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(128, false, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(128, true, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(256, true, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);
    }

    private void testSymmEncrForKeySize(int keyLength, boolean preferAES,
            int sizePriorToEncr, byte[] inputFileAsByteArray,
            String userpassword, String ownerpassword,
            AccessPermission permission) throws IOException
    {
        PDDocument document = PDDocument.load(inputFileAsByteArray);
        String prefix = "Simple-";
        int numSrcPages = document.getNumberOfPages();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<BufferedImage> srcImgTab = new ArrayList<>();
        List<byte[]> srcContentStreamTab = new ArrayList<>();
        for (int i = 0; i < numSrcPages; ++i)
        {
            srcImgTab.add(pdfRenderer.renderImage(i));
            InputStream unfilteredStream = document.getPage(i).getContents();
            byte[] bytes = IOUtils.toByteArray(unfilteredStream);
            unfilteredStream.close();
            srcContentStreamTab.add(bytes);
        }

        PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document,
                prefix, permission, userpassword, ownerpassword);

        Assert.assertEquals(numSrcPages, encryptedDoc.getNumberOfPages());
        pdfRenderer = new PDFRenderer(encryptedDoc);
        for (int i = 0; i < encryptedDoc.getNumberOfPages(); ++i)
        {
            // compare rendering
            BufferedImage bim = pdfRenderer.renderImage(i);
            ValidateXImage.checkIdent(bim, srcImgTab.get(i));

            // compare content streams
            InputStream unfilteredStream = encryptedDoc.getPage(i).getContents();
            byte[] bytes = IOUtils.toByteArray(unfilteredStream);
            unfilteredStream.close();
            Assert.assertArrayEquals("content stream of page " + i + " not identical",
                    srcContentStreamTab.get(i),
                    bytes);
        }

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-decrypted.pdf");
        encryptedDoc.setAllSecurityToBeRemoved(true);
        encryptedDoc.save(pdfFile);
        encryptedDoc.close();
    }

    // encrypt with keylength and permission, save, check sizes before and after encryption
    // reopen, decrypt and return document
    private PDDocument encrypt(int keyLength, boolean preferAES, int sizePriorToEncr,
            PDDocument doc, String prefix, AccessPermission permission,
            String userpassword, String ownerpassword) throws IOException
    {
        AccessPermission ap = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerpassword, userpassword, ap);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPreferAES(preferAES);
        spp.setPermissions(permission);
        
        // This must have no effect and should only log a warning.
        doc.setAllSecurityToBeRemoved(true);
        
        doc.protect(spp);

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-encrypted.pdf");

        doc.save(pdfFile);
        doc.close();
        long sizeEncrypted = pdfFile.length();
        Assert.assertTrue(keyLength
                + "-bit " + (preferAES ? "AES" : "RC4") + " encrypted pdf should not have same size as plain one",
                sizeEncrypted != sizePriorToEncr);

        PDDocument encryptedDoc;

        // test with owner password => full permissions
        encryptedDoc = PDDocument.load(pdfFile, ownerpassword);
        Assert.assertTrue(encryptedDoc.isEncrypted());
        Assert.assertTrue(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());
        encryptedDoc.close();

        // test with owner password => restricted permissions
        encryptedDoc = PDDocument.load(pdfFile, userpassword);
        Assert.assertTrue(encryptedDoc.isEncrypted());
        Assert.assertFalse(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

        assertEquals(permission.getPermissionBytes(), encryptedDoc.getCurrentAccessPermission().getPermissionBytes());

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
        Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
        Assert.assertEquals(1, embeddedFileNames.size());
        Map.Entry<String, PDComplexFileSpecification> entry = embeddedFileNames.entrySet().iterator().next();
        LOG.info("Processing embedded file " + entry.getKey() + ":");
        PDComplexFileSpecification complexFileSpec = entry.getValue();
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

    private void testSymmEncrForKeySizeInner(int keyLength, boolean preferAES,
            int sizePriorToEncr, byte[] inputFileWithEmbeddedFileAsByteArray,
            File embeddedFilePriorToEncryption,
            String userpassword, String ownerpassword) throws IOException
    {
        PDDocument document = PDDocument.load(inputFileWithEmbeddedFileAsByteArray);
        PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document, "ContainsEmbedded-", permission, userpassword, ownerpassword);

        File decryptedFile = new File(testResultsDir, "DecryptedContainsEmbedded-" + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");
        encryptedDoc.setAllSecurityToBeRemoved(true);
        encryptedDoc.save(decryptedFile);

        File extractedEmbeddedFile = extractEmbeddedFile(new FileInputStream(decryptedFile), "decryptedInnerFile-" + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");

        Assert.assertEquals(keyLength + "-bit " + (preferAES ? "AES" : "RC4") + " decrypted inner attachment pdf should have same size as plain one",
                embeddedFilePriorToEncryption.length(), extractedEmbeddedFile.length());

        // compare the two embedded files
        Assert.assertArrayEquals(
                getFileAsByteArray(embeddedFilePriorToEncryption),
                getFileAsByteArray(extractedEmbeddedFile));
        encryptedDoc.close();
    }

    private byte[] getFileResourceAsByteArray(String testFileName) throws IOException
    {
        return IOUtils.toByteArray(TestSymmetricKeyEncryption.class.getResourceAsStream(testFileName));
    }

    private byte[] getFileAsByteArray(File f) throws IOException
    {
        return IOUtils.toByteArray(new FileInputStream(f));
    }
}
