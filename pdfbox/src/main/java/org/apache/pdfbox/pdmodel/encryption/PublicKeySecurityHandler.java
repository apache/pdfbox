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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
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
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;

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

    private static final String SUBFILTER4 = "adbe.pkcs7.s4";
    private static final String SUBFILTER5 = "adbe.pkcs7.s5";

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
     * Prepares everything to decrypt the document.
     *
     * @param encryption encryption dictionary, can be retrieved via
     * {@link PDDocument#getEncryption()}
     * @param documentIDArray document id which is returned via
     * {@link org.apache.pdfbox.cos.COSDocument#getDocumentID()} (not used by
     * this handler)
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws IOException If there is an error accessing data. If verbose mode
     * is enabled, the exception message will provide more details why the
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

        setDecryptMetadata(encryption.isEncryptMetaData());
        if (encryption.getLength() != 0)
        {
            this.keyLength = encryption.getLength();
        }

        PublicKeyDecryptionMaterial material = (PublicKeyDecryptionMaterial) decryptionMaterial;

        try
        {
            boolean foundRecipient = false;

            X509Certificate certificate = material.getCertificate();
            X509CertificateHolder materialCert = null;
            if (certificate != null)
            {
                materialCert = new X509CertificateHolder(certificate.getEncoded());
            }

            // the decrypted content of the enveloped data that match
            // the certificate in the decryption material provided
            byte[] envelopedData = null;

            // the bytes of each recipient in the recipients array
            COSArray array = (COSArray) encryption.getCOSObject().getItem(COSName.RECIPIENTS);
            if (array == null)
            {
                PDCryptFilterDictionary defaultCryptFilterDictionary = encryption.getDefaultCryptFilterDictionary();
                array = (COSArray) defaultCryptFilterDictionary.getCOSObject().getItem(COSName.RECIPIENTS);
            }
            byte[][] recipientFieldsBytes = new byte[array.size()][];
            //TODO encryption.getRecipientsLength() and getRecipientStringAt() should be deprecated

            int recipientFieldsLength = 0;
            StringBuilder extraInfo = new StringBuilder();
            for (int i = 0; i < array.size(); i++)
            {
                COSString recipientFieldString = (COSString) array.getObject(i);
                byte[] recipientBytes = recipientFieldString.getBytes();
                CMSEnvelopedData data = new CMSEnvelopedData(recipientBytes);
                Collection<RecipientInformation> recipCertificatesIt = data.getRecipientInfos()
                        .getRecipients();
                int j = 0;
                for (RecipientInformation ri : recipCertificatesIt)
                {
                    // Impl: if a matching certificate was previously found it is an error,
                    // here we just don't care about it
                    RecipientId rid = ri.getRID();
                    if (!foundRecipient && rid.match(materialCert))
                    {
                        foundRecipient = true;
                        PrivateKey privateKey = (PrivateKey) material.getPrivateKey();
                        // might need to call setContentProvider() if we use PKI token, see
                        // http://bouncy-castle.1462172.n4.nabble.com/CMSException-exception-unwrapping-key-key-invalid-unknown-key-type-passed-to-RSA-td4658109.html
                        envelopedData = ri.getContent(new JceKeyTransEnvelopedRecipient(privateKey));
                        break;
                    }
                    j++;
                    if (certificate != null)
                    {
                        extraInfo.append('\n');
                        extraInfo.append(j);
                        extraInfo.append(": ");
                        if (rid instanceof KeyTransRecipientId)
                        {
                            appendCertInfo(extraInfo, (KeyTransRecipientId) rid, certificate, materialCert);
                        }
                    }
                }
                recipientFieldsBytes[i] = recipientBytes;
                recipientFieldsLength += recipientBytes.length;
            }
            if (!foundRecipient || envelopedData == null)
            {
                throw new IOException("The certificate matches none of " + array.size()
                        + " recipient entries" + extraInfo.toString());
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

            AccessPermission currentAccessPermission = new AccessPermission(accessBytes);
            currentAccessPermission.setReadOnly();
            setCurrentAccessPermission(currentAccessPermission);

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

            byte[] mdResult;
            if (encryption.getVersion() == 4 || encryption.getVersion() == 5)
            {
                mdResult = MessageDigests.getSHA256().digest(sha1Input);

                // detect whether AES encryption is used. This assumes that the encryption algo is 
                // stored in the PDCryptFilterDictionary
                // However, crypt filters are used only when V is 4 or 5.
                PDCryptFilterDictionary defaultCryptFilterDictionary = encryption.getDefaultCryptFilterDictionary();
                if (defaultCryptFilterDictionary != null)
                {
                    COSName cryptFilterMethod = defaultCryptFilterDictionary.getCryptFilterMethod();
                    setAES(COSName.AESV2.equals(cryptFilterMethod) ||
                           COSName.AESV3.equals(cryptFilterMethod));
                }
            }
            else
            {
                mdResult = MessageDigests.getSHA1().digest(sha1Input);
            }

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

    private void appendCertInfo(StringBuilder extraInfo, KeyTransRecipientId ktRid, 
            X509Certificate certificate, X509CertificateHolder materialCert)
    {
        BigInteger ridSerialNumber = ktRid.getSerialNumber();
        if (ridSerialNumber != null)
        {
            String certSerial = "unknown";
            BigInteger certSerialNumber = certificate.getSerialNumber();
            if (certSerialNumber != null)
            {
                certSerial = certSerialNumber.toString(16);
            }
            extraInfo.append("serial-#: rid ");
            extraInfo.append(ridSerialNumber.toString(16));
            extraInfo.append(" vs. cert ");
            extraInfo.append(certSerial);
            extraInfo.append(" issuer: rid \'");
            extraInfo.append(ktRid.getIssuer());
            extraInfo.append("\' vs. cert \'");
            extraInfo.append(materialCert == null ? "null" : materialCert.getIssuer());
            extraInfo.append("\' ");
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
        try
        {
            PDEncryption dictionary = doc.getEncryption();
            if (dictionary == null) 
            {
                dictionary = new PDEncryption();
            }

            dictionary.setFilter(FILTER);
            dictionary.setLength(this.keyLength);
            int version = computeVersionNumber();
            dictionary.setVersion(version);

            // remove CF, StmF, and StrF entries that may be left from a previous encryption
            dictionary.removeV45filters();

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

            // create the 20 bytes seed
            System.arraycopy(sk.getEncoded(), 0, seed, 0, 20);

            byte[][] recipientsFields = computeRecipientsField(seed);

            int shaInputLength = seed.length;

            for (byte[] field : recipientsFields)
            {
                shaInputLength += field.length;
            }

            byte[] shaInput = new byte[shaInputLength];

            System.arraycopy(seed, 0, shaInput, 0, 20);

            int shaInputOffset = 20;

            for (byte[] recipientsField : recipientsFields)
            {
                System.arraycopy(recipientsField, 0, shaInput, shaInputOffset, recipientsField.length);
                shaInputOffset += recipientsField.length;
            }

            byte[] mdResult;
            if (version == 4 || version == 5)
            {
                dictionary.setSubFilter(SUBFILTER5);
                mdResult = MessageDigests.getSHA256().digest(shaInput);
                COSName aesVName = version == 5 ? COSName.AESV3 : COSName.AESV2;
                prepareEncryptionDictAES(dictionary, aesVName, recipientsFields);
            }
            else
            {
                dictionary.setSubFilter(SUBFILTER4);
                mdResult = MessageDigests.getSHA1().digest(shaInput);
                dictionary.setRecipients(recipientsFields);
            }

            this.encryptionKey = new byte[this.keyLength/8];
            System.arraycopy(mdResult, 0, this.encryptionKey, 0, this.keyLength/8);

            doc.setEncryptionDictionary(dictionary);
            doc.getDocument().setEncryptionDictionary(dictionary.getCOSObject());
        }
        catch(GeneralSecurityException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Computes the version number of the StandardSecurityHandler based on the encryption key
     * length. See PDF Spec 1.6 p 93 and
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/pdf/adobe_supplement_iso32000.pdf">PDF
     * 1.7 Supplement ExtensionLevel: 3</a>
     *
     * @return The computed version number.
     */
    private int computeVersionNumber()
    {
        switch (keyLength)
        {
            case 40:
                return 1;
            case 128:
                return 2; // prefer RC4 (AES 128 doesn't work yet)
                //return 4; // prefer AES
            case 256:
                return 5;
            default:
                throw new IllegalArgumentException("key length must be 40, 128 or 256");
        }
    }

    private void prepareEncryptionDictAES(PDEncryption encryptionDictionary, COSName aesVName, byte[][] recipients)
    {
        PDCryptFilterDictionary cryptFilterDictionary = new PDCryptFilterDictionary();
        cryptFilterDictionary.setCryptFilterMethod(aesVName);
        cryptFilterDictionary.setLength(keyLength);
        COSArray array = new COSArray();
        for (byte[] recipient : recipients)
        {
            array.add(new COSString(recipient));
        }
        cryptFilterDictionary.getCOSObject().setItem(COSName.RECIPIENTS, array);
        array.setDirect(true);
        encryptionDictionary.setDefaultCryptFilterDictionary(cryptFilterDictionary);
        encryptionDictionary.setStreamFilterName(COSName.DEFAULT_CRYPT_FILTER);
        encryptionDictionary.setStringFilterName(COSName.DEFAULT_CRYPT_FILTER);
        cryptFilterDictionary.getCOSObject().setDirect(true);
        setAES(true);
    }

    private byte[][] computeRecipientsField(byte[] seed) throws GeneralSecurityException, IOException
    {
        byte[][] recipientsField = new byte[policy.getNumberOfRecipients()][];
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
        return recipientsField;
    }

    private ASN1Primitive createDERForRecipient(byte[] in, X509Certificate cert)
            throws IOException, GeneralSecurityException
    {
        String algorithm = PKCSObjectIdentifiers.RC2_CBC.getId();
        AlgorithmParameterGenerator apg;
        KeyGenerator keygen;
        Cipher cipher;
        try
        {
            apg = AlgorithmParameterGenerator.getInstance(algorithm, SecurityProvider.getProvider());
            keygen = KeyGenerator.getInstance(algorithm, SecurityProvider.getProvider());
            cipher = Cipher.getInstance(algorithm, SecurityProvider.getProvider());
        }
        catch (NoSuchAlgorithmException e)
        {
            // happens when using the command line app .jar file
            throw new IOException("Could not find a suitable javax.crypto provider for algorithm " + 
                    algorithm + "; possible reason: using an unsigned .jar file", e);
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
        TBSCertificate certificate = TBSCertificate.getInstance(input.readObject());
        input.close();

        AlgorithmIdentifier algorithmId = certificate.getSubjectPublicKeyInfo().getAlgorithm();

        IssuerAndSerialNumber serial = new IssuerAndSerialNumber(
                certificate.getIssuer(),
                certificate.getSerialNumber().getValue());

        Cipher cipher;
        try
        {
            cipher = Cipher.getInstance(algorithmId.getAlgorithm().getId(),
                    SecurityProvider.getProvider());
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProtectionPolicy()
    {
        return policy != null;
    }
}
