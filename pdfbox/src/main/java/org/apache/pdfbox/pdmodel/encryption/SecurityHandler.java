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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A security handler as described in the PDF specifications.
 * A security handler is responsible of documents protection.
 *
 * @author Ben Litchfield
 * @author Benoit Guillon
 * @author Manuel Kasper
 * 
 * @param <T_POLICY> the protection policy.
 */
public abstract class SecurityHandler<T_POLICY extends ProtectionPolicy>
{
    private static final Log LOG = LogFactory.getLog(SecurityHandler.class);

    private static final short DEFAULT_KEY_LENGTH = 40;

    // see 7.6.2, page 58, PDF 32000-1:2008
    private static final byte[] AES_SALT = { (byte) 0x73, (byte) 0x41, (byte) 0x6c, (byte) 0x54 };

    /** The length in bits of the secret key used to encrypt the document. */
    private short keyLength = DEFAULT_KEY_LENGTH;

    /** The encryption key that will be used to encrypt / decrypt.*/
    private byte[] encryptionKey;

    /** The RC4 implementation used for cryptographic functions. */
    private final RC4Cipher rc4 = new RC4Cipher();

    /** Indicates if the Metadata have to be decrypted of not. */
    private boolean decryptMetadata;

    /** Can be used to allow stateless AES encryption */
    private SecureRandom customSecureRandom;

    // PDFBOX-4453, PDFBOX-4477: Originally this was just a Set. This failed in rare cases
    // when a decrypted string was identical to an encrypted string.
    // Because COSString.equals() checks the contents, decryption was then skipped.
    // This solution keeps all different "equal" objects.
    // IdentityHashMap solves this problem and is also faster than a HashMap
    private final Set<COSBase> objects = Collections.newSetFromMap(new IdentityHashMap<>());

    private boolean useAES;

    /**
     * The typed {@link ProtectionPolicy} to be used for encryption.
     */
    private T_POLICY protectionPolicy = null;
    
    /**
     * The access permission granted to the current user for the document. These
     * permissions are computed during decryption and are in read only mode.
     */
    private AccessPermission currentAccessPermission = null;

    /**
     * The stream filter name.
     */
    private COSName streamFilterName;

    /**
     * The string filter name.
     */
    private COSName stringFilterName;

    /**
     * Constructor.
     */
    protected SecurityHandler()
    {
    }

    /**
     * Constructor used for encryption.
     *
     * @param protectionPolicy The protection policy.
     */
    protected SecurityHandler(T_POLICY protectionPolicy)
    {
        this.protectionPolicy = protectionPolicy;
        keyLength = (short) protectionPolicy.getEncryptionKeyLength();
    }

    /**
     * Set whether to decrypt meta data.
     *
     * @param decryptMetadata true if meta data has to be decrypted.
     */
    protected void setDecryptMetadata(boolean decryptMetadata)
    {
        this.decryptMetadata = decryptMetadata;
    }

    /**
     * Returns true if meta data is to be decrypted.
     *
     * @return True if meta data has to be decrypted.
     */
    public boolean isDecryptMetadata()
    {
        return decryptMetadata;
    }

    /**
     * Set the string filter name.
     * 
     * @param stringFilterName the string filter name.
     */
    protected void setStringFilterName(COSName stringFilterName)
    {
        this.stringFilterName = stringFilterName;
    }

    /**
     * Set the stream filter name.
     * 
     * @param streamFilterName the stream filter name.
     */
    protected void setStreamFilterName(COSName streamFilterName)
    {
        this.streamFilterName = streamFilterName;
    }

    /**
     * Set the custom SecureRandom.
     * 
     * @param customSecureRandom the custom SecureRandom for AES encryption
     */
    public void setCustomSecureRandom(SecureRandom customSecureRandom)
    {
        this.customSecureRandom = customSecureRandom;
    }

    /**
     * Prepare the document for encryption.
     *
     * @param doc The document that will be encrypted.
     *
     * @throws IOException If there is an error with the document.
     */
    public abstract void prepareDocumentForEncryption(PDDocument doc) throws IOException;

    /**
     * Prepares everything to decrypt the document.
     *
     * @param encryption  encryption dictionary, can be retrieved via {@link PDDocument#getEncryption()}
     * @param documentIDArray  document id which is returned via {@link org.apache.pdfbox.cos.COSDocument#getDocumentID()}
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If there is an error accessing data.
     */
    public abstract void prepareForDecryption(PDEncryption encryption, COSArray documentIDArray,
            DecryptionMaterial decryptionMaterial) throws IOException;

    /**
     * Encrypt or decrypt a set of data.
     *
     * @param objectNumber The data object number.
     * @param genNumber The data generation number.
     * @param data The data to encrypt.
     * @param output The output to write the encrypted data to.
     * @param decrypt true to decrypt the data, false to encrypt it.
     *
     * @throws IOException If there is an error reading the data.
     */
    private void encryptData(long objectNumber, long genNumber, InputStream data,
                            OutputStream output, boolean decrypt) throws IOException
    {
        // Determine whether we're using Algorithm 1 (for RC4 and AES-128), or 1.A (for AES-256)
        if (useAES && encryptionKey.length == 32)
        {
            encryptDataAES256(data, output, decrypt);
        }
        else
        {
            byte[] finalKey = calcFinalKey(objectNumber, genNumber);

            if (useAES)
            {
                encryptDataAESother(finalKey, data, output, decrypt);
            }
            else
            {
                encryptDataRC4(finalKey, data, output);
            }
        }
        output.flush();
    }

    /**
     * Calculate the key to be used for RC4 and AES-128.
     *
     * @param objectNumber The data object number.
     * @param genNumber The data generation number.
     * @return the calculated key.
     */
    private byte[] calcFinalKey(long objectNumber, long genNumber)
    {
        byte[] newKey = new byte[encryptionKey.length + 5];
        System.arraycopy(encryptionKey, 0, newKey, 0, encryptionKey.length);
        // PDF 1.4 reference pg 73
        // step 1
        // we have the reference
        // step 2
        newKey[newKey.length - 5] = (byte) (objectNumber & 0xff);
        newKey[newKey.length - 4] = (byte) (objectNumber >> 8 & 0xff);
        newKey[newKey.length - 3] = (byte) (objectNumber >> 16 & 0xff);
        newKey[newKey.length - 2] = (byte) (genNumber & 0xff);
        newKey[newKey.length - 1] = (byte) (genNumber >> 8 & 0xff);
        // step 3
        MessageDigest md = MessageDigests.getMD5();
        md.update(newKey);
        if (useAES)
        {
            md.update(AES_SALT);
        }
        byte[] digestedKey = md.digest();
        // step 4
        int length = Math.min(newKey.length, 16);
        byte[] finalKey = new byte[length];
        System.arraycopy(digestedKey, 0, finalKey, 0, length);
        return finalKey;
    }

    /**
     * Encrypt or decrypt data with RC4.
     *
     * @param finalKey The final key obtained with via {@link #calcFinalKey(long, long)}.
     * @param input The data to encrypt.
     * @param output The output to write the encrypted data to.
     *
     * @throws IOException If there is an error reading the data.
     */
    protected void encryptDataRC4(byte[] finalKey, InputStream input, OutputStream output)
            throws IOException
    {
        rc4.setKey(finalKey);
        rc4.write(input, output);
    }

    /**
     * Encrypt or decrypt data with RC4.
     *
     * @param finalKey The final key obtained with via {@link #calcFinalKey(long, long)}.
     * @param input The data to encrypt.
     * @param output The output to write the encrypted data to.
     *
     * @throws IOException If there is an error reading the data.
     */
    protected void encryptDataRC4(byte[] finalKey, byte[] input, OutputStream output) throws IOException
    {
        rc4.setKey(finalKey);
        rc4.write(input, output);
    }


    /**
     * Encrypt or decrypt data with AES with key length other than 256 bits.
     *
     * @param finalKey The final key obtained with via {@link #calcFinalKey(long, long)}.
     * @param data The data to encrypt.
     * @param output The output to write the encrypted data to.
     * @param decrypt true to decrypt the data, false to encrypt it.
     *
     * @throws IOException If there is an error reading the data.
     */
    private void encryptDataAESother(byte[] finalKey, InputStream data, OutputStream output, boolean decrypt)
            throws IOException
    {
        byte[] iv = new byte[16];

        if (!prepareAESInitializationVector(decrypt, iv, data, output))
        {
            return;
        }

        try
        {
            Cipher decryptCipher = createCipher(finalKey, iv, decrypt);
            byte[] buffer = new byte[256];
            int n;
            while ((n = data.read(buffer)) != -1)
            {
                byte[] dst = decryptCipher.update(buffer, 0, n);
                if (dst != null)
                {
                    output.write(dst);
                }
            }
            output.write(decryptCipher.doFinal());
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Encrypt or decrypt data with AES256.
     *
     * @param data The data to encrypt.
     * @param output The output to write the encrypted data to.
     * @param decrypt true to decrypt the data, false to encrypt it.
     *
     * @throws IOException If there is an error reading the data.
     */
    private void encryptDataAES256(InputStream data, OutputStream output, boolean decrypt) throws IOException
    {
        byte[] iv = new byte[16];

        if (!prepareAESInitializationVector(decrypt, iv, data, output))
        {
            return;
        }

        Cipher cipher;
        try
        {
            cipher = createCipher(this.encryptionKey, iv, decrypt);
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException(e);
        }

        try (CipherInputStream cis = new CipherInputStream(data, cipher))
        {
            cis.transferTo(output);
        }
        catch (IOException exception)
        {
            // starting with java 8 the JVM wraps an IOException around a GeneralSecurityException
            // it should be safe to swallow a GeneralSecurityException
            if (!(exception.getCause() instanceof GeneralSecurityException))
            {
                throw exception;
            }
            LOG.debug("A GeneralSecurityException occurred when decrypting some stream data", exception);
        }
    }

    private Cipher createCipher(byte[] key, byte[] iv, boolean decrypt) throws GeneralSecurityException
    {
        // PKCS#5 padding is requested by PDF specification
        @SuppressWarnings({"squid:S5542","lgtm [java/weak-cryptographic-algorithm]"})
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Key keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ips = new IvParameterSpec(iv);
        cipher.init(decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, keySpec, ips);
        return cipher;
    }

    private boolean prepareAESInitializationVector(boolean decrypt, byte[] iv, InputStream data, OutputStream output) throws IOException
    {
        if (decrypt)
        {
            // read IV from stream
            int ivSize = data.readNBytes(iv, 0, iv.length);
            if (ivSize == 0)
            {
                return false;
            }
            if (ivSize != iv.length)
            {
                throw new IOException(
                        "AES initialization vector not fully read: only "
                                + ivSize + " bytes read instead of " + iv.length);
            }
        }
        else
        {
            // generate random IV and write to stream
            SecureRandom rnd = getSecureRandom();
            rnd.nextBytes(iv);
            output.write(iv);
        }
        return true;
    }

    /**
     * Returns a SecureRandom If customSecureRandom is not defined, instantiate a new SecureRandom
     * 
     * @return SecureRandom
     */
    private SecureRandom getSecureRandom()
    {
        if (customSecureRandom != null)
        {
            return customSecureRandom;
        }
        return new SecureRandom();
    }

    /**
     * This will dispatch to the correct method.
     *
     * @param obj    The object to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation Number.
     *
     * @throws IOException If there is an error getting the stream data.
     */
    public void decrypt(COSBase obj, long objNum, long genNum) throws IOException
    {
        // PDFBOX-4477: only cache strings and streams, this improves speed and memory footprint
        if (obj instanceof COSString)
        {
            if (objects.contains(obj))
            {
                return;
            }
            objects.add(obj);
            decryptString((COSString) obj, objNum, genNum);
        }
        else if (obj instanceof COSStream)
        {
            if (objects.contains(obj))
            {
                return;
            }
            objects.add(obj);
            decryptStream((COSStream) obj, objNum, genNum);
        }
        else if (obj instanceof COSDictionary)
        {
            decryptDictionary((COSDictionary) obj, objNum, genNum);
        }
        else if (obj instanceof COSArray)
        {
            decryptArray((COSArray) obj, objNum, genNum);
        }
    }

    /**
     * This will decrypt a stream.
     *
     * @param stream The stream to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws IOException If there is an error getting the stream data.
     */
    public void decryptStream(COSStream stream, long objNum, long genNum) throws IOException
    {
        // Stream encrypted with identity filter
        if (COSName.IDENTITY.equals(streamFilterName))
        {
            return;
        }
        
        COSBase type = stream.getCOSName(COSName.TYPE);
        if (!decryptMetadata && COSName.METADATA.equals(type))
        {
            return;
        }
        // "The cross-reference stream shall not be encrypted"
        if (COSName.XREF.equals(type))
        {
            return;
        }
        if (COSName.METADATA.equals(type))
        {
            byte[] buf;
            // PDFBOX-3229 check case where metadata is not encrypted despite /EncryptMetadata missing
            try (InputStream is = stream.createRawInputStream())
            {
                int nBytes = 10;
                buf = is.readNBytes(nBytes);
                int isResult = buf.length;

                if (buf.length != nBytes)
                {
                    LOG.debug("Tried reading " + buf.length + " bytes but only " + isResult + " bytes read");
                }
            }
            if (Arrays.equals(buf, "<?xpacket ".getBytes(StandardCharsets.ISO_8859_1)))
            {
                LOG.warn("Metadata is not encrypted, but was expected to be");
                LOG.warn("Read PDF specification about EncryptMetadata (default value: true)");
                return;
            }
        }
        decryptDictionary(stream, objNum, genNum);
        // the input and the output stream of a still encrypted COSStream aren't no longer based
        // on the same object so that it is safe to omit the intermediate ByteArrayStream
        try (InputStream encryptedStream = stream.createRawInputStream(); //
                OutputStream output = stream.createRawOutputStream())
        {
            encryptData(objNum, genNum, encryptedStream, output, true /* decrypt */);
        }
        catch (IOException ex)
        {
            LOG.error(ex.getClass().getSimpleName() + " thrown when decrypting object " +
                    objNum + " " + genNum + " obj");
            throw ex;
        }
    }

    /**
     * This will encrypt a stream, but not the dictionary as the dictionary is
     * encrypted by visitFromString() in COSWriter and we don't want to encrypt
     * it twice.
     *
     * @param stream The stream to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws IOException If there is an error getting the stream data.
     */
    public void encryptStream(COSStream stream, long objNum, int genNum) throws IOException
    {
        // empty streams don't need to be encrypted
        if (!stream.hasData())
        {
            return;
        }
        InputStream in = stream.createRawInputStream();
        byte[] rawData = in.readAllBytes();
        ByteArrayInputStream encryptedStream = new ByteArrayInputStream(rawData);
        try (OutputStream output = stream.createRawOutputStream())
        {
            encryptData(objNum, genNum, encryptedStream, output, false /* encrypt */);
        }
    }

    /**
     * This will decrypt a dictionary.
     *
     * @param dictionary The dictionary to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws IOException If there is an error creating a new string.
     */
    private void decryptDictionary(COSDictionary dictionary, long objNum, long genNum) throws IOException
    {
        if (dictionary.getItem(COSName.CF) != null)
        {
            // PDFBOX-2936: avoid orphan /CF dictionaries found in US govt "I-" files
            return;
        }
        COSName type = dictionary.getCOSName(COSName.TYPE);
        boolean isSignature = COSName.SIG.equals(type) || COSName.DOC_TIME_STAMP.equals(type) ||
                // PDFBOX-4466: /Type is optional, see
                // https://ec.europa.eu/cefdigital/tracker/browse/DSS-1538
                (dictionary.getDictionaryObject(COSName.CONTENTS) instanceof COSString && 
                 dictionary.getDictionaryObject(COSName.BYTERANGE) instanceof COSArray);
        for (Map.Entry<COSName, COSBase> entry : dictionary.entrySet())
        {
            if (isSignature && COSName.CONTENTS.equals(entry.getKey()))
            {
                // do not decrypt the signature contents string
                continue;
            }
            COSBase value = entry.getValue();
            // within a dictionary only the following kind of COS objects have to be decrypted
            if (value instanceof COSString || value instanceof COSArray || value instanceof COSDictionary)
            {
                decrypt(value, objNum, genNum);
            }
        }
    }

    /**
     * This will decrypt a string.
     *
     * @param string the string to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     */
    private void decryptString(COSString string, long objNum, long genNum)
    {
        // String encrypted with identity filter
        if (COSName.IDENTITY.equals(stringFilterName))
        {
            return;
        }
        
        ByteArrayInputStream data = new ByteArrayInputStream(string.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            encryptData(objNum, genNum, data, outputStream, true /* decrypt */);
            string.setValue(outputStream.toByteArray());
        }
        catch (IOException ex)
        {
            LOG.error("Failed to decrypt COSString of length " + string.getBytes().length + 
                    " in object " + objNum + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * This will encrypt a string.
     *
     * @param string the string to encrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws IOException If an error occurs writing the new string.
     */
    public void encryptString(COSString string, long objNum, int genNum) throws IOException
    {
        ByteArrayInputStream data = new ByteArrayInputStream(string.getBytes());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        encryptData(objNum, genNum, data, buffer, false /* encrypt */);
        string.setValue(buffer.toByteArray());
    }

    /**
     * This will decrypt an array.
     *
     * @param array The array to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws IOException If there is an error accessing the data.
     */
    private void decryptArray(COSArray array, long objNum, long genNum) throws IOException
    {
        for (int i = 0; i < array.size(); i++)
        {
            decrypt(array.get(i), objNum, genNum);
        }
    }

    /**
     * Getter of the property keyLength.
     * 
     * @return Returns the keyLength in bits.
     */
    public int getKeyLength()
    {
        return keyLength;
    }

    /**
     * Setter of the property keyLength.
     *
     * @param keyLen The keyLength to set in bits.
     */
    public void setKeyLength(int keyLen)
    {
        this.keyLength = (short) keyLen;
    }

    /**
     * Sets the access permissions.
     *
     * @param currentAccessPermission The access permissions to be set.
     */
    public void setCurrentAccessPermission(AccessPermission currentAccessPermission)
    {
        this.currentAccessPermission = currentAccessPermission;
    }

    /**
     * Returns the access permissions that were computed during document decryption.
     * The returned object is in read only mode.
     *
     * @return the access permissions or null if the document was not decrypted.
     */
    public AccessPermission getCurrentAccessPermission()
    {
        return currentAccessPermission;
    }

    /**
     * True if AES is used for encryption and decryption.
     *
     * @return true if AEs is used
     */
    public boolean isAES()
    {
        return useAES;
    }

    /**
     * Set to true if AES for encryption and decryption should be used.
     *
     * @param aesValue if true AES will be used
     *
     */
    public void setAES(boolean aesValue)
    {
        useAES = aesValue;
    }

    /**
     * Returns whether a protection policy has been set.
     *
     * @return true if a protection policy has been set.
     */
    public boolean hasProtectionPolicy()
    {
        return protectionPolicy != null;
    }

    /**
     * Returns the set {@link ProtectionPolicy} or null.
     *
     * @return The set {@link ProtectionPolicy}.
     */
    protected T_POLICY getProtectionPolicy()
    {
        return protectionPolicy;
    }

    /**
     * Sets the {@link ProtectionPolicy} to the given value.
     * @param protectionPolicy The {@link ProtectionPolicy}, that shall be set.
     */
    protected void setProtectionPolicy(T_POLICY protectionPolicy)
    {
        this.protectionPolicy = protectionPolicy;
    }

    /**
     * Returns the current encryption key data.
     *
     * @return The current encryption key data.
     */
    public byte[] getEncryptionKey()
    {
        return encryptionKey;
    }

    /**
     * Sets the current encryption key data.
     *
     * @param encryptionKey The encryption key data to set.
     */
    public void setEncryptionKey(byte[] encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Computes the version number of the {@link SecurityHandler} based on the encryption key
     * length. See PDF Spec 1.6 p 93 and
     * <a href="https://www.adobe.com/content/dam/acom/en/devnet/pdf/adobe_supplement_iso32000.pdf">PDF
     * 1.7 Supplement ExtensionLevel: 3</a> and
     * <a href="http://intranet.pdfa.org/wp-content/uploads/2016/08/ISO_DIS_32000-2-DIS4.pdf">PDF
     * Spec 2.0</a>.
     *
     * @return The computed version number.
     */
    protected int computeVersionNumber()
    {
        if (keyLength == 40)
        {
            return 1;
        }
        else if (keyLength == 128 && protectionPolicy.isPreferAES())
        {
            return 4;
        }
        else if (keyLength == 256)
        {
            return 5;
        }
        return 2;
    }
}
