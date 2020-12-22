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
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
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
public final class PublicKeySecurityHandler extends SecurityHandler<PublicKeyProtectionPolicy>
{
    /** The filter name. */
    public static final String FILTER = "Adobe.PubSec";

    private static final String SUBFILTER4 = "adbe.pkcs7.s4";
    private static final String SUBFILTER5 = "adbe.pkcs7.s5";

    /**
     * Constructor.
     */
    public PublicKeySecurityHandler()
    {
    }

    /**
     * Constructor used for encryption.
     *
     * @param publicKeyProtectionPolicy The protection policy.
     */
    public PublicKeySecurityHandler(final PublicKeyProtectionPolicy publicKeyProtectionPolicy)
    {
        super(publicKeyProtectionPolicy);
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
    public void prepareForDecryption(final PDEncryption encryption, final COSArray documentIDArray,
                                     final DecryptionMaterial decryptionMaterial)
            throws IOException
    {
        if (!(decryptionMaterial instanceof PublicKeyDecryptionMaterial))
        {
            throw new IOException(
                    "Provided decryption material is not compatible with the document - "
                            + "did you pass a null keyStore?");
        }

        setDecryptMetadata(encryption.isEncryptMetaData());
        final PDCryptFilterDictionary defaultCryptFilterDictionary = encryption.getDefaultCryptFilterDictionary();
        if (defaultCryptFilterDictionary != null && defaultCryptFilterDictionary.getLength() != 0)
        {
            setKeyLength(defaultCryptFilterDictionary.getLength());
        }
        else if (encryption.getLength() != 0)
        {
            setKeyLength(encryption.getLength());
        }

        final PublicKeyDecryptionMaterial material = (PublicKeyDecryptionMaterial) decryptionMaterial;

        try
        {
            boolean foundRecipient = false;

            final X509Certificate certificate = material.getCertificate();
            X509CertificateHolder materialCert = null;
            if (certificate != null)
            {
                materialCert = new X509CertificateHolder(certificate.getEncoded());
            }

            // the decrypted content of the enveloped data that match
            // the certificate in the decryption material provided
            byte[] envelopedData = null;

            // the bytes of each recipient in the recipients array
            COSArray array = encryption.getCOSObject().getCOSArray(COSName.RECIPIENTS);
            if (array == null && defaultCryptFilterDictionary != null)
            {
                array = defaultCryptFilterDictionary.getCOSObject().getCOSArray(COSName.RECIPIENTS);
            }
            if (array == null)
            {
                throw new IOException("/Recipients entry is missing in encryption dictionary");
            }
            final byte[][] recipientFieldsBytes = new byte[array.size()][];
            //TODO encryption.getRecipientsLength() and getRecipientStringAt() should be deprecated

            int recipientFieldsLength = 0;
            final StringBuilder extraInfo = new StringBuilder();
            for (int i = 0; i < array.size(); i++)
            {
                final COSString recipientFieldString = (COSString) array.getObject(i);
                final byte[] recipientBytes = recipientFieldString.getBytes();
                final CMSEnvelopedData data = new CMSEnvelopedData(recipientBytes);
                final Collection<RecipientInformation> recipCertificatesIt = data.getRecipientInfos()
                        .getRecipients();
                int j = 0;
                for (final RecipientInformation ri : recipCertificatesIt)
                {
                    // Impl: if a matching certificate was previously found it is an error,
                    // here we just don't care about it
                    final RecipientId rid = ri.getRID();
                    if (!foundRecipient && rid.match(materialCert))
                    {
                        foundRecipient = true;
                        final PrivateKey privateKey = (PrivateKey) material.getPrivateKey();
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

            final byte[] accessBytes = new byte[4];
            System.arraycopy(envelopedData, 20, accessBytes, 0, 4);

            final AccessPermission currentAccessPermission = new AccessPermission(accessBytes);
            currentAccessPermission.setReadOnly();
            setCurrentAccessPermission(currentAccessPermission);

            // what we will put in the SHA1 = the seed + each byte contained in the recipients array
            final byte[] sha1Input = new byte[recipientFieldsLength + 20];

            // put the seed in the sha1 input
            System.arraycopy(envelopedData, 0, sha1Input, 0, 20);

            // put each bytes of the recipients array in the sha1 input
            int sha1InputOffset = 20;
            for (final byte[] recipientFieldsByte : recipientFieldsBytes)
            {
                System.arraycopy(recipientFieldsByte, 0, sha1Input, sha1InputOffset,
                        recipientFieldsByte.length);
                sha1InputOffset += recipientFieldsByte.length;
            }

            final byte[] mdResult;
            if (encryption.getVersion() == 4 || encryption.getVersion() == 5)
            {
                if (encryption.getVersion() == 4)
                {
                    mdResult = MessageDigests.getSHA1().digest(sha1Input);
                }
                else
                {
                    mdResult = MessageDigests.getSHA256().digest(sha1Input);
                }

                // detect whether AES encryption is used. This assumes that the encryption algo is 
                // stored in the PDCryptFilterDictionary
                // However, crypt filters are used only when V is 4 or 5.
                if (defaultCryptFilterDictionary != null)
                {
                    final COSName cryptFilterMethod = defaultCryptFilterDictionary.getCryptFilterMethod();
                    setAES(COSName.AESV2.equals(cryptFilterMethod) ||
                           COSName.AESV3.equals(cryptFilterMethod));
                }
            }
            else
            {
                mdResult = MessageDigests.getSHA1().digest(sha1Input);
            }

            // we have the encryption key ...
            setEncryptionKey(new byte[getKeyLength() / 8]);
            System.arraycopy(mdResult, 0, getEncryptionKey(), 0, getKeyLength() / 8);
        }
        catch (final CMSException | KeyStoreException | CertificateEncodingException e)
        {
            throw new IOException(e);
        }
    }

    private void appendCertInfo(final StringBuilder extraInfo, final KeyTransRecipientId ktRid,
                                final X509Certificate certificate, final X509CertificateHolder materialCert)
    {
        final BigInteger ridSerialNumber = ktRid.getSerialNumber();
        if (ridSerialNumber != null)
        {
            String certSerial = "unknown";
            final BigInteger certSerialNumber = certificate.getSerialNumber();
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
    public void prepareDocumentForEncryption(final PDDocument doc) throws IOException
    {
        try
        {
            PDEncryption dictionary = doc.getEncryption();
            if (dictionary == null) 
            {
                dictionary = new PDEncryption();
            }

            dictionary.setFilter(FILTER);
            dictionary.setLength(getKeyLength());
            final int version = computeVersionNumber();
            dictionary.setVersion(version);

            // remove CF, StmF, and StrF entries that may be left from a previous encryption
            dictionary.removeV45filters();

            // create the 20 bytes seed
            final byte[] seed = new byte[20];

            final KeyGenerator key;
            try
            {
                key = KeyGenerator.getInstance("AES");
            }
            catch (final NoSuchAlgorithmException e)
            {
                // should never happen
                throw new RuntimeException(e);
            }

            key.init(192, new SecureRandom());
            final SecretKey sk = key.generateKey();

            // create the 20 bytes seed
            System.arraycopy(sk.getEncoded(), 0, seed, 0, 20);

            final byte[][] recipientsFields = computeRecipientsField(seed);

            int shaInputLength = seed.length;

            for (final byte[] field : recipientsFields)
            {
                shaInputLength += field.length;
            }

            final byte[] shaInput = new byte[shaInputLength];

            System.arraycopy(seed, 0, shaInput, 0, 20);

            int shaInputOffset = 20;

            for (final byte[] recipientsField : recipientsFields)
            {
                System.arraycopy(recipientsField, 0, shaInput, shaInputOffset, recipientsField.length);
                shaInputOffset += recipientsField.length;
            }

            final byte[] mdResult;
            switch (version)
            {
                case 4:
                    dictionary.setSubFilter(SUBFILTER5);
                    mdResult = MessageDigests.getSHA1().digest(shaInput);
                    prepareEncryptionDictAES(dictionary, COSName.AESV2, recipientsFields);
                    break;
                case 5:
                    dictionary.setSubFilter(SUBFILTER5);
                    mdResult = MessageDigests.getSHA256().digest(shaInput);
                    prepareEncryptionDictAES(dictionary, COSName.AESV3, recipientsFields);
                    break;
                default:
                    dictionary.setSubFilter(SUBFILTER4);
                    mdResult = MessageDigests.getSHA1().digest(shaInput);
                    dictionary.setRecipients(recipientsFields);
                    break;
            }

            setEncryptionKey(new byte[getKeyLength() / 8]);
            System.arraycopy(mdResult, 0, getEncryptionKey(), 0, getKeyLength() / 8);

            doc.setEncryptionDictionary(dictionary);
            doc.getDocument().setEncryptionDictionary(dictionary.getCOSObject());
        }
        catch(final GeneralSecurityException e)
        {
            throw new IOException(e);
        }
    }

    private void prepareEncryptionDictAES(final PDEncryption encryptionDictionary, final COSName aesVName, final byte[][] recipients)
    {
        final PDCryptFilterDictionary cryptFilterDictionary = new PDCryptFilterDictionary();
        cryptFilterDictionary.setCryptFilterMethod(aesVName);
        cryptFilterDictionary.setLength(getKeyLength());
        final COSArray array = new COSArray();
        for (final byte[] recipient : recipients)
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

    private byte[][] computeRecipientsField(final byte[] seed) throws GeneralSecurityException, IOException
    {
        final PublicKeyProtectionPolicy protectionPolicy = getProtectionPolicy();
        final byte[][] recipientsField = new byte[protectionPolicy.getNumberOfRecipients()][];
        final Iterator<PublicKeyRecipient> it = protectionPolicy.getRecipientsIterator();
        int i = 0;
        
        while(it.hasNext())
        {
            final PublicKeyRecipient recipient = it.next();
            final X509Certificate certificate = recipient.getX509();
            final int permission = recipient.getPermission().getPermissionBytesForPublicKey();
            
            final byte[] pkcs7input = new byte[24];
            final byte one = (byte)(permission);
            final byte two = (byte)(permission >>> 8);
            final byte three = (byte)(permission >>> 16);
            final byte four = (byte)(permission >>> 24);

            // put this seed in the pkcs7 input
            System.arraycopy(seed, 0, pkcs7input, 0, 20);

            pkcs7input[20] = four;
            pkcs7input[21] = three;
            pkcs7input[22] = two;
            pkcs7input[23] = one;

            final ASN1Primitive obj = createDERForRecipient(pkcs7input, certificate);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            obj.encodeTo(baos, ASN1Encoding.DER);

            recipientsField[i] = baos.toByteArray();

            i++;
        }
        return recipientsField;
    }

    private ASN1Primitive createDERForRecipient(final byte[] in, final X509Certificate cert)
            throws IOException, GeneralSecurityException
    {
        final String algorithm = PKCSObjectIdentifiers.RC2_CBC.getId();
        final AlgorithmParameterGenerator apg;
        final KeyGenerator keygen;
        final Cipher cipher;
        try
        {
            apg = AlgorithmParameterGenerator.getInstance(algorithm, SecurityProvider.getProvider());
            keygen = KeyGenerator.getInstance(algorithm, SecurityProvider.getProvider());
            cipher = Cipher.getInstance(algorithm, SecurityProvider.getProvider());
        }
        catch (final NoSuchAlgorithmException e)
        {
            // happens when using the command line app .jar file
            throw new IOException("Could not find a suitable javax.crypto provider for algorithm " + 
                    algorithm + "; possible reason: using an unsigned .jar file", e);
        }
        catch (final NoSuchPaddingException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }

        final AlgorithmParameters parameters = apg.generateParameters();

        final ASN1Primitive object;
        try (ASN1InputStream input = new ASN1InputStream(parameters.getEncoded("ASN.1")))
        {
            object = input.readObject();
        }

        keygen.init(128);
        final SecretKey secretkey = keygen.generateKey();

        cipher.init(1, secretkey, parameters);
        final byte[] bytes = cipher.doFinal(in);

        final KeyTransRecipientInfo recipientInfo = computeRecipientInfo(cert, secretkey.getEncoded());
        final DERSet set = new DERSet(new RecipientInfo(recipientInfo));

        final AlgorithmIdentifier algorithmId = new AlgorithmIdentifier(new ASN1ObjectIdentifier(algorithm), object);
        final EncryptedContentInfo encryptedInfo =
                new EncryptedContentInfo(PKCSObjectIdentifiers.data, algorithmId, new DEROctetString(bytes));
        final EnvelopedData enveloped = new EnvelopedData(null, set, encryptedInfo, (ASN1Set) null);

        final ContentInfo contentInfo = new ContentInfo(PKCSObjectIdentifiers.envelopedData, enveloped);
        return contentInfo.toASN1Primitive();
    }

    private KeyTransRecipientInfo computeRecipientInfo(final X509Certificate x509certificate, final byte[] abyte0)
        throws IOException, CertificateEncodingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException
    {
        final TBSCertificate certificate;
        try (ASN1InputStream input = new ASN1InputStream(x509certificate.getTBSCertificate()))
        {
            certificate = TBSCertificate.getInstance(input.readObject());
        }

        final AlgorithmIdentifier algorithmId = certificate.getSubjectPublicKeyInfo().getAlgorithm();

        final IssuerAndSerialNumber serial = new IssuerAndSerialNumber(
                certificate.getIssuer(),
                certificate.getSerialNumber().getValue());

        final Cipher cipher;
        try
        {
            cipher = Cipher.getInstance(algorithmId.getAlgorithm().getId(),
                    SecurityProvider.getProvider());
        }
        catch (final NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            // should never happen, if this happens throw IOException instead
            throw new RuntimeException("Could not find a suitable javax.crypto provider", e);
        }

        cipher.init(1, x509certificate.getPublicKey());

        final DEROctetString octets = new DEROctetString(cipher.doFinal(abyte0));
        final RecipientIdentifier recipientId = new RecipientIdentifier(serial);
        return new KeyTransRecipientInfo(recipientId, algorithmId, octets);
    }
}
