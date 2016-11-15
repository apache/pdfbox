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
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Charsets;

/**
 * A security handler as described in the PDF specifications.
 * A security handler is responsible of documents protection.
 *
 * @author Ben Litchfield
 * @author Benoit Guillon
 * @author Manuel Kasper
 */
public abstract class SecurityHandler
{
    private static final Log LOG = LogFactory.getLog(SecurityHandler.class);

    private static final int DEFAULT_KEY_LENGTH = 40;

    // see 7.6.2, page 58, PDF 32000-1:2008
    private static final byte[] AES_SALT = { (byte) 0x73, (byte) 0x41, (byte) 0x6c, (byte) 0x54 };

    /** The length in bits of the secret key used to encrypt the document. */
    protected int keyLength = DEFAULT_KEY_LENGTH;

    /** The encryption key that will used to encrypt / decrypt.*/
    protected byte[] encryptionKey;

    /** The RC4 implementation used for cryptographic functions. */
    private final RC4Cipher rc4 = new RC4Cipher();

    /** indicates if the Metadata have to be decrypted of not. */
    private boolean decryptMetadata;

    private final Set<COSBase> objects = new HashSet<COSBase>();

    private boolean useAES;

    /**
     * The access permission granted to the current user for the document. These
     * permissions are computed during decryption and are in read only mode.
     */
    private AccessPermission currentAccessPermission = null;

    /**
     * Set wether to decrypt meta data.
     *
     * @param decryptMetadata true if meta data has to be decrypted.
     */
    protected void setDecryptMetadata(boolean decryptMetadata)
    {
        this.decryptMetadata = decryptMetadata;
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
            DecryptionMaterial decryptionMaterial) throws InvalidPasswordException, IOException;

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
            Cipher decryptCipher;
            try
            {
                decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            }
            catch (NoSuchAlgorithmException e)
            {
                // should never happen
                throw new RuntimeException(e);
            }

            SecretKey aesKey = new SecretKeySpec(finalKey, "AES");
            IvParameterSpec ips = new IvParameterSpec(iv);
            decryptCipher.init(decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, aesKey, ips);
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
        catch (InvalidKeyException e)
        {
            throw new IOException(e);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new IOException(e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new IOException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new IOException(e);
        }
        catch (BadPaddingException e)
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
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException(e);
        }

        CipherInputStream cis = new CipherInputStream(data, cipher);
        try
        {
            IOUtils.copy(cis, output);
        }
        catch(IOException exception)
        {
            // starting with java 8 the JVM wraps an IOException around a GeneralSecurityException
            // it should be safe to swallow a GeneralSecurityException
            if (!(exception.getCause() instanceof GeneralSecurityException))
            {
                throw exception;
            }
            LOG.debug("A GeneralSecurityException occured when decrypting some stream data", exception);
        }
        finally
        {
            cis.close();
        }
    }

    private boolean prepareAESInitializationVector(boolean decrypt, byte[] iv, InputStream data, OutputStream output) throws IOException
    {
        if (decrypt)
        {
            // read IV from stream
            int ivSize = data.read(iv);
            if (ivSize == -1)
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
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(iv);
            output.write(iv);
        }
        return true;
    }

    /**
     * This will dispatch to the correct method.
     *
     * @param obj The object to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation Number.
     *
     * @throws IOException If there is an error getting the stream data.
     */
    public void decrypt(COSBase obj, long objNum, long genNum) throws IOException
    {
        if (!objects.contains(obj))
        {
            objects.add(obj);

            if (obj instanceof COSString)
            {
                decryptString((COSString) obj, objNum, genNum);
            }
            else if (obj instanceof COSStream)
            {
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
            // PDFBOX-3229 check case where metadata is not encrypted despite /EncryptMetadata missing
            InputStream is = stream.createRawInputStream();
            byte buf[] = new byte[10];
            is.read(buf);
            is.close();
            if (Arrays.equals(buf, "<?xpacket ".getBytes(Charsets.ISO_8859_1)))
            {
                LOG.warn("Metadata is not encrypted, but was expected to be");
                LOG.warn("Read PDF specification about EncryptMetadata (default value: true)");
                return;
            }
        }
        decryptDictionary(stream, objNum, genNum);
        byte[] encrypted = IOUtils.toByteArray(stream.createRawInputStream());
        ByteArrayInputStream encryptedStream = new ByteArrayInputStream(encrypted);
        OutputStream output = stream.createRawOutputStream();
        try
        {
           encryptData(objNum, genNum, encryptedStream, output, true /* decrypt */);
        }
        finally
        {
            output.close();
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
        byte[] rawData = IOUtils.toByteArray(stream.createRawInputStream());
        ByteArrayInputStream encryptedStream = new ByteArrayInputStream(rawData);
        OutputStream output = stream.createRawOutputStream();
        try
        {
            encryptData(objNum, genNum, encryptedStream, output, false /* encrypt */);
        }
        finally
        {
            output.close();
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
        COSBase type = dictionary.getDictionaryObject(COSName.TYPE);
        boolean isSignature = COSName.SIG.equals(type) || COSName.DOC_TIME_STAMP.equals(type);
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
     * @throws IOException If an error occurs writing the new string.
     */
    private void decryptString(COSString string, long objNum, long genNum) throws IOException
    {
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
                    " in object " + objNum + ": " + ex.getMessage());
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
        encryptData(objNum, genNum, data, buffer, false /* decrypt */);
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
     * Getter of the property <tt>keyLength</tt>.
     * @return  Returns the keyLength.
     */
    public int getKeyLength()
    {
        return keyLength;
    }

    /**
     * Setter of the property <tt>keyLength</tt>.
     *
     * @param keyLen  The keyLength to set.
     */
    public void setKeyLength(int keyLen)
    {
        this.keyLength = keyLen;
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
    public abstract boolean hasProtectionPolicy();
}
