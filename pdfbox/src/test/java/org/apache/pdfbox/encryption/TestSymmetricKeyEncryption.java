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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler;
import org.apache.pdfbox.pdmodel.graphics.image.ValidateXImage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


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
class TestSymmetricKeyEncryption
{
    /**
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(TestSymmetricKeyEncryption.class);

    private static final File testResultsDir = new File("target/test-output/crypto");

    private static AccessPermission permission;

    static final String USERPASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";
    static final String OWNERPASSWORD = "abcdefghijk1234567890abcdefghijk1234567890";

    @BeforeAll
    static void setUp() throws Exception
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
        permission.setCanPrintFaithful(false);
        permission.setReadOnly();
    }

    /**
     * Test that permissions work as intended: the user psw ("user") is enough
     * to open the PDF with possibly restricted rights, the owner psw ("owner")
     * gives full permissions. The 3 files of this test were created by Maruan
     * Sahyoun, NOT with PDFBox, but with Adobe Acrobat to ensure "the gold
     * standard". The restricted permissions prevent printing and text
     * extraction. In the 128 and 256 bit encrypted files, AssembleDocument,
     * ExtractForAccessibility and PrintDegraded are also disabled.
     * 
     * @throws java.io.IOException
     */
    @Test
    void testPermissions() throws IOException
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
        restrAP.setCanPrintFaithful(false);

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
        try (PDDocument doc = Loader.loadPDF(inputFileAsByteArray, password))
        {
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
            assertEquals(expectedPermissions.canPrintFaithful(), currentAccessPermission.canPrintFaithful());
            
            new PDFRenderer(doc).renderImage(0);
        }
    }

    /**
     * Protect a document with a key and try to reopen it with that key and compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    @Test
    void testProtection() throws Exception
    {
        String filename = "Acroform-PDFBOX-2333.pdf";
        byte[] inputFileAsByteArray = getFileResourceAsByteArray(filename);
        int sizePriorToEncryption = inputFileAsByteArray.length;

        testSymmEncrForKeySize(filename, 40, false, sizePriorToEncryption, inputFileAsByteArray,
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(filename, 128, false, sizePriorToEncryption, inputFileAsByteArray,
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(filename, 128, true, sizePriorToEncryption, inputFileAsByteArray,
                USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(filename, 256, true, sizePriorToEncryption, inputFileAsByteArray,
                USERPASSWORD, OWNERPASSWORD, permission);
    }

    /**
     * PDFBOX-4308: test that index colorspace table string doesn't get
     * corrupted when encrypting. This happened because the colorspace was
     * referenced twice, once in the resources dictionary and once in an image
     * in the resources dictionary, and when saving the PDF the string was saved
     * twice, once as a direct object and once as an indirect object (both from
     * the same java object). Encryption used the wrong object number and/or the
     * object was encrypted twice.
     *
     * @throws IOException
     */
    @Test
    void testPDFBox4308() throws IOException
    {
        String filename = "PDFBOX-4308.pdf";
        byte[] inputFileAsByteArray = Files.readAllBytes(Paths.get("target/pdfs/" + filename));
        int sizePriorToEncryption = inputFileAsByteArray.length;

        testSymmEncrForKeySize(filename, 40, false, sizePriorToEncryption, inputFileAsByteArray,
                USERPASSWORD, OWNERPASSWORD, permission);
    }

    /**
     * Protect a document with an embedded PDF with a key and try to reopen it
     * with that key and compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    @Test
    void testProtectionInnerAttachment() throws Exception
    {
        String testFileName = "preEnc_20141025_105451.pdf";
        byte[] inputFileWithEmbeddedFileAsByteArray = getFileResourceAsByteArray(testFileName);

        int sizeOfFileWithEmbeddedFile = inputFileWithEmbeddedFileAsByteArray.length;

        File extractedEmbeddedFile
                = extractEmbeddedFile(
                        new RandomAccessReadBuffer(inputFileWithEmbeddedFileAsByteArray),
                        "innerFile.pdf");

        testSymmEncrForKeySizeInner(40, false, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(128, false, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(128, true, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(256, true, sizeOfFileWithEmbeddedFile, 
                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);
    }

    /**
     * PDFBOX-4453: verify that identical encrypted strings are really decrypted each.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox4453() throws IOException
    {
        final int TESTCOUNT = 1000;
        File file = new File(testResultsDir,"PDFBOX-4453.pdf");
        try (PDDocument doc = new PDDocument())
        {
            doc.addPage(new PDPage());
            for (int i = 0; i < TESTCOUNT; ++i)
            {
                // strings must be in different dictionaries so that the actual
                // encryption key changes
                COSDictionary dict = new COSDictionary();
                doc.getPage(0).getCOSObject().setItem(COSName.getPDFName("_Test-" + i), dict);
                // need two different keys so that there are both encrypted and decrypted COSStrings
                // with value "0"
                dict.setString("key1", "3");
                dict.setString("key2", "0");
            }
            
            //RC4-40
            StandardProtectionPolicy spp =
                    new StandardProtectionPolicy("12345", "", new AccessPermission());
            spp.setEncryptionKeyLength(40);
            spp.setPreferAES(false);
            doc.protect(spp);
            doc.save(file);
        }

        try (PDDocument doc = Loader.loadPDF(file))
        {
            assertTrue(doc.isEncrypted());
            for (int i = 0; i < TESTCOUNT; ++i)
            {
                COSDictionary dict =
                        doc.getPage(0).getCOSObject().getCOSDictionary(COSName.getPDFName("_Test-" + i));
                assertEquals("3", dict.getString("key1"));
                assertEquals("0", dict.getString("key2"));
            }
        }
    }

    /**
     * test AESV3 with R=5 and excess bytes.
     * 
     * @throws IOException 
     */
    @Test
    void testPDFBox5639() throws IOException
    {
        File file = new File("target/pdfs", "PDFBOX-5639.pdf");
        try (PDDocument document = Loader.loadPDF(file, "JUL2023rfi"))
        {
            assertEquals(2, document.getNumberOfPages());
        }
    }

    private void testSymmEncrForKeySize(String filename, int keyLength, boolean preferAES,
            int sizePriorToEncr, byte[] inputFileAsByteArray,
            String userpassword, String ownerpassword,
            AccessPermission permission) throws IOException
    {
        PDDocument document = Loader.loadPDF(inputFileAsByteArray);
        String prefix = filename + "-Simple-";
        int numSrcPages = document.getNumberOfPages();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<BufferedImage> srcImgTab = new ArrayList<>();
        List<byte[]> srcContentStreamTab = new ArrayList<>();
        for (int i = 0; i < numSrcPages; ++i)
        {
            srcImgTab.add(pdfRenderer.renderImage(i));
            try (InputStream unfilteredStream = document.getPage(i).getContents())
            {
                srcContentStreamTab.add(unfilteredStream.readAllBytes());
            }
        }

        try (PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document,
                prefix, permission, userpassword, ownerpassword))
        {
            assertEquals(numSrcPages, encryptedDoc.getNumberOfPages());
            pdfRenderer = new PDFRenderer(encryptedDoc);
            for (int i = 0; i < encryptedDoc.getNumberOfPages(); ++i)
            {
                // compare rendering
                BufferedImage bim = pdfRenderer.renderImage(i);
                ValidateXImage.checkIdent(bim, srcImgTab.get(i));
                
                // compare content streams
                try (InputStream unfilteredStream = encryptedDoc.getPage(i).getContents())
                {
                    byte[] bytes = unfilteredStream.readAllBytes();
                    assertArrayEquals(srcContentStreamTab.get(i),bytes, "content stream of page " + i + " not identical");
                }
            }
            
            File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-decrypted.pdf");
            encryptedDoc.setAllSecurityToBeRemoved(true);
            encryptedDoc.save(pdfFile);
        }
    }

    // encrypt with keylength and permission, save, check sizes before and after encryption
    // reopen, decrypt and return document
    private PDDocument encrypt(int keyLength, boolean preferAES, int sizePriorToEncr,
            PDDocument doc, String prefix, AccessPermission permission,
            String userpassword, String ownerpassword) throws IOException
    {
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerpassword, userpassword,
                permission);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPreferAES(preferAES);
        
        // This must have no effect and should only log a warning.
        doc.setAllSecurityToBeRemoved(true);
        
        doc.protect(spp);

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + "-encrypted.pdf");

        doc.save(pdfFile);
        doc.close();
        long sizeEncrypted = pdfFile.length();
        assertNotEquals(sizeEncrypted, sizePriorToEncr,
            keyLength + "-bit " + (preferAES ? "AES" : "RC4") + " encrypted pdf should not have same size as plain one");

        // test with owner password => full permissions
        PDDocument encryptedDoc = Loader.loadPDF(pdfFile, ownerpassword);
        assertTrue(encryptedDoc.isEncrypted());
        assertTrue(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

        // Older encryption allows to get the user password when the owner password is known
        PDEncryption encryption = encryptedDoc.getEncryption();
        int revision = encryption.getRevision();
        if (revision < 5)
        {
            StandardSecurityHandler standardSecurityHandler = new StandardSecurityHandler();
            int keyLengthInBytes = encryption.getVersion() == 1 ? 5 : encryption.getLength() / 8;
            byte[] computedUserPassword = standardSecurityHandler.getUserPassword(
                    ownerpassword.getBytes(StandardCharsets.ISO_8859_1),
                    encryption.getOwnerKey(),
                    revision,
                    keyLengthInBytes);
            assertEquals(userpassword.substring(0, 32), new String(computedUserPassword, StandardCharsets.ISO_8859_1));
        }

        encryptedDoc.close();

        // test with user password => restricted permissions
        encryptedDoc = Loader.loadPDF(pdfFile, userpassword);
        assertTrue(encryptedDoc.isEncrypted());
        assertFalse(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

        assertEquals(permission.getPermissionBytes(), encryptedDoc.getCurrentAccessPermission().getPermissionBytes());

        return encryptedDoc;
    }

    // extract the embedded file, saves it, and return the extracted saved file
    private File extractEmbeddedFile(RandomAccessRead pdfSource, String name) throws IOException
    {
        PDDocument docWithEmbeddedFile = Loader.loadPDF(pdfSource);
        PDDocumentCatalog catalog = docWithEmbeddedFile.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
        Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
        assertEquals(1, embeddedFileNames.size());
        Map.Entry<String, PDComplexFileSpecification> entry = embeddedFileNames.entrySet().iterator().next();
        LOG.info("Processing embedded file {}:", entry.getKey());
        PDComplexFileSpecification complexFileSpec = entry.getValue();
        PDEmbeddedFile embeddedFile = complexFileSpec.getEmbeddedFile();

        File resultFile = new File(testResultsDir, name);
        try (FileOutputStream fos = new FileOutputStream(resultFile);
             InputStream is = embeddedFile.createInputStream())
        {
            is.transferTo(fos);
        }

        LOG.info("  size: {}", embeddedFile.getSize());
        assertEquals(embeddedFile.getSize(), resultFile.length());

        return resultFile;
    }

    private void testSymmEncrForKeySizeInner(int keyLength, boolean preferAES,
            int sizePriorToEncr, byte[] inputFileWithEmbeddedFileAsByteArray,
            File embeddedFilePriorToEncryption,
            String userpassword, String ownerpassword) throws IOException
    {
        PDDocument document = Loader.loadPDF(inputFileWithEmbeddedFileAsByteArray);
        try (PDDocument encryptedDoc = encrypt(keyLength, preferAES, sizePriorToEncr, document, "ContainsEmbedded-", permission, userpassword, ownerpassword))
        {
            File decryptedFile = new File(testResultsDir, "DecryptedContainsEmbedded-" + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");
            encryptedDoc.setAllSecurityToBeRemoved(true);
            encryptedDoc.save(decryptedFile);
            
            File extractedEmbeddedFile = extractEmbeddedFile(
                    new RandomAccessReadBufferedFile(decryptedFile), "decryptedInnerFile-"
                            + keyLength + "-bit-" + (preferAES ? "AES" : "RC4") + ".pdf");
            
            assertEquals(embeddedFilePriorToEncryption.length(), extractedEmbeddedFile.length(),
                    keyLength + "-bit " + (preferAES ? "AES" : "RC4") + " decrypted inner attachment pdf should have same size as plain one");
            
            // compare the two embedded files
            assertArrayEquals(
                    getFileAsByteArray(embeddedFilePriorToEncryption),
                    getFileAsByteArray(extractedEmbeddedFile));
        }
    }

    private byte[] getFileResourceAsByteArray(String testFileName) throws IOException
    {
        return TestSymmetricKeyEncryption.class.getResourceAsStream(testFileName).readAllBytes();
    }

    private byte[] getFileAsByteArray(File f) throws IOException
    {
        return Files.readAllBytes(f.toPath());
    }
}
