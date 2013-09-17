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

package org.apache.pdfbox.pdmodel.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.cms.EnvelopedData;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientIdentifier;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This class implements the public key security handler
 * described in the PDF specification.
 *
 * @see PDF Spec 1.6 p104
 *
 * @see PublicKeyProtectionPolicy to see how to protect document with this security handler.
 *
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 * @version $Revision: 1.3 $
 */
public class PublicKeySecurityHandler extends SecurityHandler
{

    /**
     * The filter name.
     */
    public static final String FILTER = "Adobe.PubSec";

    private static final String SUBFILTER = "adbe.pkcs7.s4";

    private PublicKeyProtectionPolicy policy = null;

    /**
     * Constructor.
     */
    public PublicKeySecurityHandler()
    {
    }

    /**
     * Constructor used for encryption.
     *
     * @param p The protection policy.
     */
    public PublicKeySecurityHandler(PublicKeyProtectionPolicy p)
    {
        policy = p;
        this.keyLength = policy.getEncryptionKeyLength();
    }

    /**
     * Decrypt the document.
     *
     * @param doc The document to decrypt.
     * @param decryptionMaterial The data used to decrypt the document.
     *
     * @throws CryptographyException If there is an error during decryption.
     * @throws IOException If there is an error accessing data.
     */
    public void decryptDocument(PDDocument doc, DecryptionMaterial decryptionMaterial)
        throws CryptographyException, IOException
    {
        this.document = doc;

        PDEncryptionDictionary dictionary = doc.getEncryptionDictionary();

        prepareForDecryption( dictionary, doc.getDocument().getDocumentID(),
        											decryptionMaterial );
        
        proceedDecryption();
    }

    /**
     * Prepares everything to decrypt the document.
     *
     * If {@link #decryptDocument(PDDocument, DecryptionMaterial)} is used, this method is
     * called from there. Only if decryption of single objects is needed this should be called instead.
     *
     * @param encDictionary  encryption dictionary, can be retrieved via {@link PDDocument#getEncryptionDictionary()}
     * @param documentIDArray  document id which is returned via {@link COSDocument#getDocumentID()} (not used by this handler)
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void prepareForDecryption(PDEncryptionDictionary encDictionary, COSArray documentIDArray,
				 														 DecryptionMaterial decryptionMaterial)
    throws CryptographyException, IOException
    {
    	
	      if(encDictionary.getLength() != 0)
	      {
	          this.keyLength = encDictionary.getLength();
	      }
	
	      if(!(decryptionMaterial instanceof PublicKeyDecryptionMaterial))
	      {
	          throw new CryptographyException(
	              "Provided decryption material is not compatible with the document");
	      }
	
	      PublicKeyDecryptionMaterial material = (PublicKeyDecryptionMaterial)decryptionMaterial;
	
	      try
	      {
	          boolean foundRecipient = false;
	
	          // the decrypted content of the enveloped data that match
	          // the certificate in the decryption material provided
	          byte[] envelopedData = null;
	
	          // the bytes of each recipient in the recipients array
	          byte[][] recipientFieldsBytes = new byte[encDictionary.getRecipientsLength()][];
	
	          int recipientFieldsLength = 0;
	
	          for(int i=0; i<encDictionary.getRecipientsLength(); i++)
	          {
	              COSString recipientFieldString = encDictionary.getRecipientStringAt(i);
	              byte[] recipientBytes = recipientFieldString.getBytes();
	              CMSEnvelopedData data = new CMSEnvelopedData(recipientBytes);
	              Iterator recipCertificatesIt = data.getRecipientInfos().getRecipients().iterator();
	              while(recipCertificatesIt.hasNext())
	              {
	                  RecipientInformation ri =
	                      (RecipientInformation)recipCertificatesIt.next();
	                  // Impl: if a matching certificate was previously found it is an error,
	                  // here we just don't care about it
	                  if(ri.getRID().match(material.getCertificate()) && !foundRecipient)
	                  {
	                      foundRecipient = true;
	                      envelopedData = ri.getContent(material.getPrivateKey(), "BC");
	                      break;
	                  }
	              }
	              recipientFieldsBytes[i] = recipientBytes;
	              recipientFieldsLength += recipientBytes.length;
	          }
	          if(!foundRecipient || envelopedData == null)
	          {
	              throw new CryptographyException("The certificate matches no recipient entry");
	          }
	          if(envelopedData.length != 24)
	          {
	              throw new CryptographyException("The enveloped data does not contain 24 bytes");
	          }
	          // now envelopedData contains:
	          // - the 20 bytes seed
	          // - the 4 bytes of permission for the current user
	
	          byte[] accessBytes = new byte[4];
	          System.arraycopy(envelopedData, 20, accessBytes, 0, 4);
	
	          currentAccessPermission = new AccessPermission(accessBytes);
	          currentAccessPermission.setReadOnly();
	
	           // what we will put in the SHA1 = the seed + each byte contained in the recipients array
	          byte[] sha1Input = new byte[recipientFieldsLength + 20];
	
	          // put the seed in the sha1 input
	          System.arraycopy(envelopedData, 0, sha1Input, 0, 20);
	
	          // put each bytes of the recipients array in the sha1 input
	          int sha1InputOffset = 20;
	          for(int i=0; i<recipientFieldsBytes.length; i++)
	          {
	              System.arraycopy(
	                  recipientFieldsBytes[i], 0,
	                  sha1Input, sha1InputOffset, recipientFieldsBytes[i].length);
	              sha1InputOffset += recipientFieldsBytes[i].length;
	          }
	
	          MessageDigest md = MessageDigest.getInstance("SHA-1");
	          byte[] mdResult = md.digest(sha1Input);
	
	          // we have the encryption key ...
	          encryptionKey = new byte[this.keyLength/8];
	          System.arraycopy(mdResult, 0, encryptionKey, 0, this.keyLength/8);
	      }
	      catch(CMSException e)
	      {
	          throw new CryptographyException(e);
	      }
	      catch(KeyStoreException e)
	      {
	          throw new CryptographyException(e);
	      }
	      catch(NoSuchProviderException e)
	      {
	          throw new CryptographyException(e);
	      }
	      catch(NoSuchAlgorithmException e)
	      {
	          throw new CryptographyException(e);
	      }
    }
    
    /**
     * Prepare the document for encryption.
     *
     * @param doc The document that will be encrypted.
     *
     * @throws CryptographyException If there is an error while encrypting.
     */
    public void prepareDocumentForEncryption(PDDocument doc) throws CryptographyException
    {

        try
        {
            Security.addProvider(new BouncyCastleProvider());

            PDEncryptionDictionary dictionary = doc.getEncryptionDictionary();
            if (dictionary == null) 
            {
                dictionary = new PDEncryptionDictionary();
            }

            dictionary.setFilter(FILTER);
            dictionary.setLength(this.keyLength);
            dictionary.setVersion(2);
            dictionary.setSubFilter(SUBFILTER);

            byte[][] recipientsField = new byte[policy.getRecipientsNumber()][];

            // create the 20 bytes seed

            byte[] seed = new byte[20];

            KeyGenerator key = KeyGenerator.getInstance("AES");
            key.init(192, new SecureRandom());
            SecretKey sk = key.generateKey();
            System.arraycopy(sk.getEncoded(), 0, seed, 0, 20); // create the 20 bytes seed


            Iterator it = policy.getRecipientsIterator();
            int i = 0;


            while(it.hasNext())
            {
                PublicKeyRecipient recipient = (PublicKeyRecipient)it.next();
                X509Certificate certificate = recipient.getX509();
                int permission = recipient.getPermission().getPermissionBytesForPublicKey();

                byte[] pkcs7input = new byte[24];
                byte one = (byte)(permission);
                byte two = (byte)(permission >>> 8);
                byte three = (byte)(permission >>> 16);
                byte four = (byte)(permission >>> 24);

                System.arraycopy(seed, 0, pkcs7input, 0, 20); // put this seed in the pkcs7 input

                pkcs7input[20] = four;
                pkcs7input[21] = three;
                pkcs7input[22] = two;
                pkcs7input[23] = one;

                ASN1Primitive obj = createDERForRecipient(pkcs7input, certificate);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                DEROutputStream k = new DEROutputStream(baos);

                k.writeObject(obj);

                recipientsField[i] = baos.toByteArray();

                i++;
            }

            dictionary.setRecipients(recipientsField);

            int sha1InputLength = seed.length;

            for(int j=0; j<dictionary.getRecipientsLength(); j++)
            {
                COSString string = dictionary.getRecipientStringAt(j);
                sha1InputLength += string.getBytes().length;
            }


            byte[] sha1Input = new byte[sha1InputLength];

            System.arraycopy(seed, 0, sha1Input, 0, 20);

            int sha1InputOffset = 20;


            for(int j=0; j<dictionary.getRecipientsLength(); j++)
            {
                COSString string = dictionary.getRecipientStringAt(j);
                System.arraycopy(
                    string.getBytes(), 0,
                    sha1Input, sha1InputOffset, string.getBytes().length);
                sha1InputOffset += string.getBytes().length;
            }

            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] mdResult = md.digest(sha1Input);

            this.encryptionKey = new byte[this.keyLength/8];
            System.arraycopy(mdResult, 0, this.encryptionKey, 0, this.keyLength/8);

            doc.setEncryptionDictionary(dictionary);
            doc.getDocument().setEncryptionDictionary(dictionary.encryptionDictionary);

        }
        catch(NoSuchAlgorithmException ex)
        {
            throw new CryptographyException(ex);
        }
        catch(NoSuchProviderException ex)
        {
            throw new CryptographyException(ex);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new CryptographyException(e);
        }

    }

    private ASN1Primitive createDERForRecipient(byte[] in, X509Certificate cert)
        throws IOException,
               GeneralSecurityException
    {

        String s = "1.2.840.113549.3.2";

        AlgorithmParameterGenerator algorithmparametergenerator = AlgorithmParameterGenerator.getInstance(s);
        AlgorithmParameters algorithmparameters = algorithmparametergenerator.generateParameters();
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(algorithmparameters.getEncoded("ASN.1"));
        ASN1InputStream asn1inputstream = new ASN1InputStream(bytearrayinputstream);
        ASN1Primitive derobject = asn1inputstream.readObject();
        KeyGenerator keygenerator = KeyGenerator.getInstance(s);
        keygenerator.init(128);
        SecretKey secretkey = keygenerator.generateKey();
        Cipher cipher = Cipher.getInstance(s);
        cipher.init(1, secretkey, algorithmparameters);
        byte[] abyte1 = cipher.doFinal(in);
        DEROctetString deroctetstring = new DEROctetString(abyte1);
        KeyTransRecipientInfo keytransrecipientinfo = computeRecipientInfo(cert, secretkey.getEncoded());
        DERSet derset = new DERSet(new RecipientInfo(keytransrecipientinfo));
        AlgorithmIdentifier algorithmidentifier = new AlgorithmIdentifier(new DERObjectIdentifier(s), derobject);
        EncryptedContentInfo encryptedcontentinfo =
            new EncryptedContentInfo(PKCSObjectIdentifiers.data, algorithmidentifier, deroctetstring);
        EnvelopedData env = new EnvelopedData(null, derset, encryptedcontentinfo, (ASN1Set) null);
        ContentInfo contentinfo =
            new ContentInfo(PKCSObjectIdentifiers.envelopedData, env);
        return contentinfo.toASN1Primitive();
    }

    private KeyTransRecipientInfo computeRecipientInfo(X509Certificate x509certificate, byte[] abyte0)
        throws GeneralSecurityException, IOException
    {
        ASN1InputStream asn1inputstream =
            new ASN1InputStream(new ByteArrayInputStream(x509certificate.getTBSCertificate()));
        TBSCertificateStructure tbscertificatestructure =
            TBSCertificateStructure.getInstance(asn1inputstream.readObject());
        AlgorithmIdentifier algorithmidentifier = tbscertificatestructure.getSubjectPublicKeyInfo().getAlgorithmId();
        IssuerAndSerialNumber issuerandserialnumber =
            new IssuerAndSerialNumber(
                tbscertificatestructure.getIssuer(),
                tbscertificatestructure.getSerialNumber().getValue());
        Cipher cipher = Cipher.getInstance(algorithmidentifier.getObjectId().getId());
        cipher.init(1, x509certificate.getPublicKey());
        DEROctetString deroctetstring = new DEROctetString(cipher.doFinal(abyte0));
        RecipientIdentifier recipId = new RecipientIdentifier(issuerandserialnumber);
        return new KeyTransRecipientInfo( recipId, algorithmidentifier, deroctetstring);
    }

}
