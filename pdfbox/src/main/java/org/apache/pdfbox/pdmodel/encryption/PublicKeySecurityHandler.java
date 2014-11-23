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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This class implements the public key security handler described in the PDF specification.
 *
 * @see PublicKeyProtectionPolicy to see how to protect document with this security handler.
 * @author Benoit Guillon
 */
public final class PublicKeySecurityHandler extends SecurityHandler
{
    /** The filter name. */
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
     * @throws IOException If there is an error accessing data.
     */
    @Override
    public void decryptDocument(PDDocument doc, DecryptionMaterial decryptionMaterial) throws IOException
    {
        this.document = doc;
        PDEncryption dictionary = doc.getEncryption();
        prepareForDecryption( dictionary, doc.getDocument().getDocumentID(), decryptionMaterial );
        proceedDecryption();
    }

    /**
     * Prepares everything to decrypt the document.
     *
     * If {@link #decryptDocument(PDDocument, DecryptionMaterial)} is used, this
     * method is called from there. Only if decryption of single objects is
     * needed this should be called instead.
     *
     * @param encryption encryption dictionary, can be retrieved via
     * {@link PDDocument#getEncryption()}
     * @param documentIDArray document id which is returned via
     * {@link org.apache.pdfbox.cos.COSDocument#getDocumentID()} (not used by
     * this handler)
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws IOException If there is an error accessing data. If verbose mode
     * is enabled, the exception message will provide more details why the the
     * match wasn't successful.
     */
    @Override
    public void prepareForDecryption(PDEncryption encryption, COSArray documentIDArray,
            DecryptionMaterial decryptionMaterial)
            throws IOException
    {
        if (!(decryptionMaterial instanceof PublicKeyDecryptionMaterial))
        {
            throw new IOException(
                    "Provided decryption material is not compatible with the document");
        }

        decryptMetadata = encryption.isEncryptMetaData();
        if (encryption.getLength() != 0)
        {
            this.keyLength = encryption.getLength();
        }

        PublicKeyDecryptionMaterial material = (PublicKeyDecryptionMaterial) decryptionMaterial;

        try
        {
            boolean foundRecipient = false;

            // the decrypted content of the enveloped data that match
            // the certificate in the decryption material provided
            byte[] envelopedData = null;

            // the bytes of each recipient in the recipients array
            byte[][] recipientFieldsBytes = new byte[encryption.getRecipientsLength()][];

            int recipientFieldsLength = 0;
            int i = 0;
            String extraInfo = "";
            for (; i < encryption.getRecipientsLength(); i++)
            {
                COSString recipientFieldString = encryption.getRecipientStringAt(i);
                byte[] recipientBytes = recipientFieldString.getBytes();
                CMSEnvelopedData data = new CMSEnvelopedData(recipientBytes);
                Iterator<?> recipCertificatesIt = data.getRecipientInfos().getRecipients().iterator();
                int j = 0;
                while (recipCertificatesIt.hasNext())
                {
                    RecipientInformation ri = (RecipientInformation) recipCertificatesIt.next();
                    // Impl: if a matching certificate was previously found it is an error,
                    // here we just don't care about it
                    X509Certificate certificate = material.getCertificate();
                    X509CertificateHolder materialCert = null;
                    if (null != certificate)
                    {
                        materialCert = new X509CertificateHolder(certificate.getEncoded());
                    }
                    RecipientId rid = ri.getRID();
                    if (rid.match(materialCert) && !foundRecipient)
                    {
                        foundRecipient = true;
                        PrivateKey privateKey = (PrivateKey) material.getPrivateKey();
                        envelopedData = ri.getContent(new JceKeyTransEnvelopedRecipient(privateKey).setProvider("BC"));
                        break;
                    }
                    j++;
                    if (certificate != null)
                    {
                        extraInfo += "\n" + j + ": ";
                        if (rid instanceof KeyTransRecipientId)
                        {
                            KeyTransRecipientId ktRid = (KeyTransRecipientId) rid;
                            BigInteger ridSerialNumber = ktRid.getSerialNumber();
                            if (ridSerialNumber != null)
                            {
                                String certSerial = "unknown";
                                BigInteger certSerialNumber = certificate.getSerialNumber();
                                if (certSerialNumber != null)
                                {
                                    certSerial = certSerialNumber.toString(16);
                                }
                                extraInfo += "serial-#: rid " + ridSerialNumber.toString(16)
                                        + " vs. cert " + certSerial + " issuer: rid \'"
                                        + ktRid.getIssuer() + "\' vs. cert \'"
                                        + (materialCert == null ? "null" : materialCert.getIssuer()) + "\' ";
                            }
                        }
                    }
                }
                recipientFieldsBytes[i] = recipientBytes;
                recipientFieldsLength += recipientBytes.length;
            }
            if (!foundRecipient || envelopedData == null)
            {
                throw new IOException("The certificate matches none of " + i
                        + " recipient entries" + extraInfo);
            }
            if (envelopedData.length != 24)
            {
                throw new IOException("The enveloped data does not contain 24 bytes");
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
            for (byte[] recipientFieldsByte : recipientFieldsBytes)
            {
                System.arraycopy(recipientFieldsByte, 0, sha1Input, sha1InputOffset,
                        recipientFieldsByte.length);
                sha1InputOffset += recipientFieldsByte.length;
            }

            MessageDigest md = MessageDigests.getSHA1();
            byte[] mdResult = md.digest(sha1Input);

            // we have the encryption key ...
            encryptionKey = new byte[this.keyLength / 8];
            System.arraycopy(mdResult, 0, encryptionKey, 0, this.keyLength / 8);
        }
        catch (CMSException e)
        {
            throw new IOException(e);
        }
        catch (KeyStoreException e)
        {
            throw new IOException(e);
        }
        catch (CertificateEncodingException e)
        {
            throw new IOException(e);
        }
    }
    
    /**
     * Prepare the document for encryption.
     *
     * @param doc The document that will be encrypted.
     *
     * @throws IOException If there is an error while encrypting.
     */
    @Override
    public void prepareDocumentForEncryption(PDDocument doc) throws IOException
    {
        if (keyLength == 256)
        {
            throw new IOException("256 bit key length is not supported yet for public key security");
        }
        try
        {
            Security.addProvider(new BouncyCastleProvider());

            PDEncryption dictionary = doc.getEncryption();
            if (dictionary == null) 
            {
                dictionary = new PDEncryption();
            }

            dictionary.setFilter(FILTER);
            dictionary.setLength(this.keyLength);
            dictionary.setVersion(2);
            dictionary.setSubFilter(SUBFILTER);

            byte[][] recipientsField = new byte[policy.getNumberOfRecipients()][];

            // create the 20 bytes seed

            byte[] seed = new byte[20];

            KeyGenerator key;
            try
            {
                key = KeyGenerator.getInstance("AES");
            }
            catch (NoSuchAlgorithmException e)
            {
                // should never happen
                throw new RuntimeException(e);
            }

            key.init(192, new SecureRandom());
            SecretKey sk = key.generateKey();
            System.arraycopy(sk.getEncoded(), 0, seed, 0, 20); // create the 20 bytes seed


            Iterator<PublicKeyRecipient> it = policy.getRecipientsIterator();
            int i = 0;


            while(it.hasNext())
            {
                PublicKeyRecipient recipient = it.next();
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

            MessageDigest sha1 = MessageDigests.getSHA1();
            byte[] mdResult = sha1.digest(sha1Input);

            this.encryptionKey = new byte[this.keyLength/8];
            System.arraycopy(mdResult, 0, this.encryptionKey, 0, this.keyLength/8);

            doc.setEncryptionDictionary(dictionary);
            doc.getDocument().setEncryptionDictionary(dictionary.getCOSDictionary());
        }
        catch(GeneralSecurityException e)
        {
            throw new IOException(e);
        }
    }

    private ASN1Primitive createDERForRecipient(byte[] in, X509Certificate cert)
            throws IOException, GeneralSecurityException
    {
        String algorithm = "1.2.840.113549.3.2";
        AlgorithmParameterGenerator apg;
        KeyGenerator keygen;
        Cipher cipher;
        try
        {
            apg = AlgorithmParameterGenerator.getInstance(algorithm);
            keygen = KeyGenerator.getInstance(algorithm);
            cipher = Cipher.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }
        catch (NoSuchPaddingException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }

        AlgorithmParameters parameters = apg.generateParameters();

        ASN1InputStream input = new ASN1InputStream(parameters.getEncoded("ASN.1"));
        ASN1Primitive object = input.readObject();
        input.close();

        keygen.init(128);
        SecretKey secretkey = keygen.generateKey();

        cipher.init(1, secretkey, parameters);
        byte[] bytes = cipher.doFinal(in);

        KeyTransRecipientInfo recipientInfo = computeRecipientInfo(cert, secretkey.getEncoded());
        DERSet set = new DERSet(new RecipientInfo(recipientInfo));

        AlgorithmIdentifier algorithmId = new AlgorithmIdentifier(new ASN1ObjectIdentifier(algorithm), object);
        EncryptedContentInfo encryptedInfo = 
                new EncryptedContentInfo(PKCSObjectIdentifiers.data, algorithmId, new DEROctetString(bytes));
        EnvelopedData enveloped = new EnvelopedData(null, set, encryptedInfo, (ASN1Set) null);

        ContentInfo contentInfo = new ContentInfo(PKCSObjectIdentifiers.envelopedData, enveloped);
        return contentInfo.toASN1Primitive();
    }

    private KeyTransRecipientInfo computeRecipientInfo(X509Certificate x509certificate, byte[] abyte0)
        throws IOException, CertificateEncodingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException
    {
        ASN1InputStream input = new ASN1InputStream(x509certificate.getTBSCertificate());
        TBSCertificateStructure certificate = TBSCertificateStructure.getInstance(input.readObject());
        input.close();

        AlgorithmIdentifier algorithmId = certificate.getSubjectPublicKeyInfo().getAlgorithm();

        IssuerAndSerialNumber serial = new IssuerAndSerialNumber(
                certificate.getIssuer(),
                certificate.getSerialNumber().getValue());

        Cipher cipher;
        try
        {
            cipher = Cipher.getInstance(algorithmId.getAlgorithm().getId());
        }
        catch (NoSuchAlgorithmException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }
        catch (NoSuchPaddingException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }

        cipher.init(1, x509certificate.getPublicKey());

        DEROctetString octets = new DEROctetString(cipher.doFinal(abyte0));
        RecipientIdentifier recipientId = new RecipientIdentifier(serial);
        return new KeyTransRecipientInfo(recipientId, algorithmId, octets);
    }
}
