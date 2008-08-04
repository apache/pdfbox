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

package test.pdfbox.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.encryption.AccessPermission;
import org.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.pdfbox.pdmodel.encryption.PublicKeyProtectionPolicy;
import org.pdfbox.pdmodel.encryption.PublicKeyRecipient;

/**
 * Tests for public key encryption.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class TestPublicKeyEncryption extends TestCase 
{
    
    private AccessPermission accessPermission;
    private AccessPermission accessPermission2;
    
    private File publicCert1;
    private File privateCert1;
    private File publicCert2;
    private File privateCert2;
    private File input;
    private File output;
    
    private String password1 = "test1";
    private String password2 = "test2";
    
    /**
     * Constructor.
     * 
     * @param name The junit test class name.
     */
    public TestPublicKeyEncryption( String name )
    {
        super( name );
        accessPermission = new AccessPermission();
        accessPermission.setCanAssembleDocument(false);
        accessPermission.setCanExtractContent(false);                
        accessPermission.setCanExtractForAccessibility(true);
        accessPermission.setCanFillInForm(false);
        accessPermission.setCanModify(false);
        accessPermission.setCanModifyAnnotations(false);
        accessPermission.setCanPrint(false);
        accessPermission.setCanPrintDegraded(false);
        
        accessPermission2 = new AccessPermission();
        accessPermission2.setCanAssembleDocument(false);
        accessPermission2.setCanExtractContent(false);                
        accessPermission2.setCanExtractForAccessibility(true);
        accessPermission2.setCanFillInForm(false);
        accessPermission2.setCanModify(false);
        accessPermission2.setCanModifyAnnotations(false);
        accessPermission2.setCanPrint(true); // it is true now !
        accessPermission2.setCanPrintDegraded(false);
            
        publicCert1 = new File("test/encryption/test1.der");
        privateCert1 = new File("test/encryption/test1.pfx");        
        publicCert2 = new File("test/encryption/test2.der");
        privateCert2 = new File("test/encryption/test2.pfx");
        input = new File("test/input/Exolab.pdf");
        output = new File("test/encryption/output.pdf");
        
        Assert.assertTrue(publicCert1.exists() && publicCert1.isFile());        
        Assert.assertTrue(privateCert1.exists() && privateCert1.isFile());
        
        Assert.assertTrue(publicCert2.exists() && publicCert2.isFile());
        Assert.assertTrue(privateCert2.exists() && privateCert2.isFile());
        
        Assert.assertTrue(input.exists() && input.isFile());
        
    }
    
    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestPublicKeyEncryption.class );
    }
    
    /**
     * Protect a document with certificate 1 and try to open it with certificate 2
     * and catch the exception.
     * 
     * @throws Exception If there is an error during the test.
     */
    public void testProtectionError() throws Exception
    {
                        
        PDDocument doc = PDDocument.load(input);
        protect(doc, publicCert1.getAbsolutePath());
        
        doc.save(output.getAbsolutePath());
            
        doc.close();
                        
        PDDocument doc2 = PDDocument.load(output);    
        
        Exception e = null;
        
        try 
        {        
            open(doc2, privateCert2.getAbsolutePath(), password2);
        }
        catch(CryptographyException ex)
        {
            e = ex;
            System.out.println(ex.getMessage());
        }
        finally
        {
            Assert.assertNotNull(e);
        }
    }
    
    
    /**
     * Protect a document with the public certificate and try to open it with 
     * the private certificate.
     * 
     * @throws Exception If there is an error during the test.
     */
    public void testProtection() throws Exception
    {
        PDDocument doc = PDDocument.load(input);
        protect(doc, publicCert1.getAbsolutePath());
        
        //Assert.assertTrue(doc.isEncrypted());
        
        doc.save(output.getAbsolutePath());
            
        doc.close();
                        
        PDDocument doc2 = PDDocument.load(output);
        
        Assert.assertNotNull(doc2);
        
        open(doc2, privateCert1.getAbsolutePath(), password1);        
        
        Assert.assertTrue(doc2.isEncrypted());
        
        AccessPermission currentAp = doc2.getCurrentAccessPermission();
        
        Assert.assertFalse(currentAp.canAssembleDocument());
        Assert.assertFalse(currentAp.canExtractContent());
        Assert.assertTrue(currentAp.canExtractForAccessibility());
        Assert.assertFalse(currentAp.canFillInForm());
        Assert.assertFalse(currentAp.canModify());
        Assert.assertFalse(currentAp.canModifyAnnotations());
        Assert.assertFalse(currentAp.canPrint());
        Assert.assertFalse(currentAp.canPrintDegraded());
        
        doc2.close();
            
    } 
    
    
    /**
     * Protect the document for 2 recipients and try to open it.
     * 
     * @throws Exception If there is an error during the test.
     */
    public void testMultipleRecipients() throws Exception 
    {            
        
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        PDDocument doc = PDDocument.load(input);
        
        PublicKeyProtectionPolicy ppp = new PublicKeyProtectionPolicy();
        
        PublicKeyRecipient recip1 = new PublicKeyRecipient();
        PublicKeyRecipient recip2 = new PublicKeyRecipient();
        
        recip1.setPermission(accessPermission);
        recip2.setPermission(accessPermission2);
        
        InputStream inStream = new FileInputStream(publicCert1);        
        Assert.assertNotNull(cf);
        X509Certificate certificate1 = (X509Certificate)cf.generateCertificate(inStream);
        inStream.close();        
        
        InputStream inStream2 = new FileInputStream(publicCert2);        
        Assert.assertNotNull(cf);
        X509Certificate certificate2 = (X509Certificate)cf.generateCertificate(inStream2);
        inStream.close();        
        
        recip1.setX509(certificate1);
        recip2.setX509(certificate2);
        
        ppp.addRecipient(recip1);
        ppp.addRecipient(recip2);
        
        doc.protect(ppp);                
        doc.save(output.getAbsolutePath());        
        doc.close();
        
        /* open first time */
        
        PDDocument docOpen1 = PDDocument.load(output);
        
        KeyStore ks1 = KeyStore.getInstance("PKCS12");        
        ks1.load(new FileInputStream(privateCert1), password1.toCharArray());            
        PublicKeyDecryptionMaterial pdm = new PublicKeyDecryptionMaterial(ks1, null, password1);        
        docOpen1.openProtection(pdm);        
        docOpen1.close();

        /* open second time */
        
        PDDocument docOpen2 = PDDocument.load(output);
        
        KeyStore ks2 = KeyStore.getInstance("PKCS12");        
        ks2.load(new FileInputStream(privateCert2), password2.toCharArray());            
        PublicKeyDecryptionMaterial pdm2 = new PublicKeyDecryptionMaterial(ks2, null, password2);        
        docOpen2.openProtection(pdm2);        
        docOpen2.close();
                
    }
    
    
    
    private void protect(PDDocument doc, String certPath) throws Exception 
    {
        InputStream inStream = new FileInputStream(certPath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Assert.assertNotNull(cf);
        X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);
        Assert.assertNotNull(certificate);
        inStream.close();        
        
        PublicKeyProtectionPolicy ppp = new PublicKeyProtectionPolicy();                
        PublicKeyRecipient recip = new PublicKeyRecipient();
        recip.setPermission(accessPermission);
        recip.setX509(certificate);
        
        ppp.addRecipient(recip);
        
        doc.protect(ppp);
        
    }    
    
    
    private void open(PDDocument doc, String certPath, String password) throws Exception 
    {    
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(certPath), password.toCharArray());
        
        PublicKeyDecryptionMaterial pdm = new PublicKeyDecryptionMaterial(ks, null, password);
        
        doc.openProtection(pdm);

    }

}
