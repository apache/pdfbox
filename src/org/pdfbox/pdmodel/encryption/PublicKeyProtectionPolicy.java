/**
 * Copyright (c) 2003-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */

package org.pdfbox.pdmodel.encryption;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents the protection policy to use to protect 
 * a document with the public key security handler as described
 * in the PDF specification 1.6 p104. 
 * 
 * PDF documents are encrypted so that they can be decrypted by 
 * one or more recipients. Each recipient have its own access permission. 
 * 
 * The following code sample shows how to protect a document using
 * the public key security handler. In this code sample, <code>doc</code> is 
 * a <code>PDDocument</code> object.
 * 
 * <pre>
 * PublicKeyProtectionPolicy policy = new PublicKeyProtectionPolicy();
 * PublicKeyRecipient recip = new PublicKeyRecipient();
 * AccessPermission ap = new AccessPermission(); 
 * ap.setCanModify(false);
 * recip.setPermission(ap);
 * 
 * // load the recipient's certificate
 * InputStream inStream = new FileInputStream(certificate_path);
 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
 * X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);
 * inStream.close();
 * 
 * recip.setX509(certificate); // set the recipient's certificate
 * policy.addRecipient(recip);
 * policy.setEncryptionKeyLength(128); // the document will be encrypted with 128 bits secret key
 * doc.protect(policy);
 * doc.save(out);
 * </pre>
 * 
 * 
 * @see org.pdfbox.pdmodel.PDDocument#protect(ProtectionPolicy)
 * @see AccessPermission
 * @see PublicKeyRecipient
 * 
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 * 
 * @version $Revision: 1.2 $
 */
public class PublicKeyProtectionPolicy extends ProtectionPolicy 
{

    /** 
     * The list of recipients.
     */
    private ArrayList recipients = null;

    /** 
     * The X509 certificate used to decrypt the current document.
     */
    private X509Certificate decryptionCertificate;
    
    /**
     * Constructor for encryption. Just creates an empty recipients list.
     */
    public PublicKeyProtectionPolicy()
    {
        recipients = new ArrayList();
    }
    
    /**
     * Adds a new recipient to the recipients list.
     * 
     * @param r A new recipient.
     */
    public void addRecipient(PublicKeyRecipient r)
    {
        recipients.add(r);
    }
    
    /**
     * Removes a recipient from the recipients list.
     * 
     * @param r The recipient to remove.
     * 
     * @return true If a recipient was found and removed.
     */
    public boolean removeRecipient(PublicKeyRecipient r)
    {
        return recipients.remove(r);
    }
    
    /**
     * Returns an iterator to browse the list of recipients. Object
     * found in this iterator are <code>PublicKeyRecipient</code>.
     * 
     * @return The recipients list iterator.
     */    
    public Iterator getRecipientsIterator()
    {
        return recipients.iterator();
    }

    /** 
     * Getter of the property <tt>decryptionCertificate</tt>.
     * 
     * @return  Returns the decryptionCertificate.
     */
    public X509Certificate getDecryptionCertificate() 
    {
        return decryptionCertificate;
    }

    /** 
     * Setter of the property <tt>decryptionCertificate</tt>.
     * 
     * @param aDecryptionCertificate The decryption certificate to set.
     */
    public void setDecryptionCertificate(X509Certificate aDecryptionCertificate) 
    {
        this.decryptionCertificate = aDecryptionCertificate;
    }
    
    /**
     * Returns the number of recipients.
     * 
     * @return The number of recipients.
     */
    public int getRecipientsNumber()
    {
        return recipients.size();
    }
}
