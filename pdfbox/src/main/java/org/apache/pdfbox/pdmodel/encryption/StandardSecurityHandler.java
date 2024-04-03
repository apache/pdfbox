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
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
 
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * The standard security handler. This security handler protects document with password.
 * @see StandardProtectionPolicy to see how to protect document with this security handler.
 * @author Ben Litchfield
 * @author Benoit Guillon
 * @author Manuel Kasper
 */
public final class StandardSecurityHandler extends SecurityHandler<StandardProtectionPolicy>
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(StandardSecurityHandler.class);

    private static final int REVISION_2 = 2;
    private static final int REVISION_3 = 3;
    private static final int REVISION_4 = 4;
    private static final int REVISION_5 = 5;
    private static final int REVISION_6 = 6;
    
    /** Type of security handler. */
    public static final String FILTER = "Standard";

    /** Protection policy class for this handler. */
    public static final Class<?> PROTECTION_POLICY_CLASS = StandardProtectionPolicy.class;

    /** Standard padding for encryption. */
    private static final byte[] ENCRYPT_PADDING =
    {
        (byte)0x28, (byte)0xBF, (byte)0x4E, (byte)0x5E, (byte)0x4E,
        (byte)0x75, (byte)0x8A, (byte)0x41, (byte)0x64, (byte)0x00,
        (byte)0x4E, (byte)0x56, (byte)0xFF, (byte)0xFA, (byte)0x01,
        (byte)0x08, (byte)0x2E, (byte)0x2E, (byte)0x00, (byte)0xB6,
        (byte)0xD0, (byte)0x68, (byte)0x3E, (byte)0x80, (byte)0x2F,
        (byte)0x0C, (byte)0xA9, (byte)0xFE, (byte)0x64, (byte)0x53,
        (byte)0x69, (byte)0x7A
    };

    // hashes used for Algorithm 2.B, depending on remainder from E modulo 3
    private static final String[] HASHES_2B = new String[] {"SHA-256", "SHA-384", "SHA-512"};

    /**
     * Constructor.
     */
    public StandardSecurityHandler()
    {
    }

    /**
     * Constructor used for encryption.
     *
     * @param standardProtectionPolicy The protection policy.
     */
    public StandardSecurityHandler(StandardProtectionPolicy standardProtectionPolicy)
    {
        super(standardProtectionPolicy);
    }

    /**
     * Computes the revision version of the StandardSecurityHandler to
     * use regarding the version number and the permissions bits set.
     * See PDF Spec 1.6 p98
     * 
     * @param version The version number.
     *
     * @return The computed revision number.
     */
    private int computeRevisionNumber(int version)
    {
        StandardProtectionPolicy protectionPolicy = getProtectionPolicy();
        AccessPermission permissions = protectionPolicy.getPermissions();
        if (version < REVISION_2 && !permissions.hasAnyRevision3PermissionSet())
        {
            return REVISION_2;
        }
        if (version == REVISION_5)
        {
            // note about revision 5: "Shall not be used. This value was used by a deprecated Adobe extension."
            return REVISION_6;    
        }
        if (version == REVISION_4)
        {
            return REVISION_4;
        }
        if (version == REVISION_2 || version == REVISION_3 || permissions.hasAnyRevision3PermissionSet())
        {
            return REVISION_3;
        }
        return REVISION_4;
    }

    /**
     * Prepares everything to decrypt the document.
     *
     * Only if decryption of single objects is needed this should be called.
     *
     * @param encryption  encryption dictionary
     * @param documentIDArray  document id
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException If there is an error accessing data.
     */
    @Override
    public void prepareForDecryption(PDEncryption encryption, COSArray documentIDArray,
                                     DecryptionMaterial decryptionMaterial)
                                     throws IOException
    {
        if(!(decryptionMaterial instanceof StandardDecryptionMaterial))
        {
            throw new IOException("Decryption material is not compatible with the document");
        }
        
        // This is only used with security version 4 and 5.
        if (encryption.getVersion() >= REVISION_4) {
	        setStreamFilterName(encryption.getStreamFilterName());
	        setStringFilterName(encryption.getStringFilterName());
        }
        setDecryptMetadata(encryption.isEncryptMetaData());
        StandardDecryptionMaterial material = (StandardDecryptionMaterial)decryptionMaterial;

        String password = material.getPassword();
        if(password == null)
        {
            password = "";
        }

        int dicPermissions = encryption.getPermissions();
        int dicRevision = encryption.getRevision();
        int dicLength = encryption.getVersion() == 1 ? 5 : encryption.getLength() / 8;
        
        if (encryption.getVersion() == REVISION_4 || encryption.getVersion() == REVISION_5)
        {
            // detect whether AES encryption is used. This assumes that the encryption algo is 
            // stored in the PDCryptFilterDictionary
            // However, crypt filters are used only when V is 4 or 5.
            PDCryptFilterDictionary stdCryptFilterDictionary = encryption.getStdCryptFilterDictionary();
            if (stdCryptFilterDictionary != null)
            {
                COSName cryptFilterMethod = stdCryptFilterDictionary.getCryptFilterMethod();
                if (COSName.AESV2.equals(cryptFilterMethod))
                {
                    dicLength = 128 / 8;
                    setAES(true);
                    if (encryption.getCOSObject().containsKey(COSName.LENGTH))
                    {
                        // PDFBOX-5345
                        int newLength = encryption.getLength() / 8;
                        if (newLength < dicLength)
                        {
                            LOG.warn("Using " + newLength + " bytes key length instead of " +
                                    dicLength + " in AESV2 encryption?!");
                            dicLength = newLength;
                        }
                    }
                }
                if (COSName.AESV3.equals(cryptFilterMethod))
                {
                    dicLength = 256 / 8;
                    setAES(true);
                    if (encryption.getCOSObject().containsKey(COSName.LENGTH))
                    {
                        // PDFBOX-5345
                        int newLength = encryption.getLength() / 8;
                        if (newLength < dicLength)
                        {
                            LOG.warn("Using " + newLength + " bytes key length instead of " +
                                    dicLength + " in AESV3 encryption?!");
                            dicLength = newLength;
                        }
                    }
                }
            }
        }

        byte[] documentIDBytes = getDocumentIDBytes(documentIDArray);

        // we need to know whether the meta data was encrypted for password calculation
        boolean encryptMetadata = encryption.isEncryptMetaData();
        
        byte[] userKey = encryption.getUserKey();
        byte[] ownerKey = encryption.getOwnerKey();
        byte[] ue = null, oe = null;

        Charset passwordCharset = StandardCharsets.ISO_8859_1;
        if (dicRevision == REVISION_5 || dicRevision == REVISION_6)
        {
            passwordCharset = StandardCharsets.UTF_8;
            ue = encryption.getUserEncryptionKey();
            oe = encryption.getOwnerEncryptionKey();
        }

        if (dicRevision == REVISION_6)
        {
            password = SaslPrep.saslPrepQuery(password); // PDFBOX-4155
        }

        AccessPermission currentAccessPermission;

        if( isOwnerPassword(password.getBytes(passwordCharset), userKey, ownerKey,
                                 dicPermissions, documentIDBytes, dicRevision,
                                 dicLength, encryptMetadata) )
        {
            currentAccessPermission = AccessPermission.getOwnerAccessPermission();
            setCurrentAccessPermission(currentAccessPermission);
            
            byte[] computedPassword;
            if (dicRevision == REVISION_5 || dicRevision == REVISION_6)
            {
                computedPassword = password.getBytes(passwordCharset);
            }
            else
            {
                computedPassword = getUserPassword234(password.getBytes(passwordCharset),
                        ownerKey, dicRevision, dicLength );
            }
            
            setEncryptionKey(
                computeEncryptedKey(
                    computedPassword,
                    ownerKey, userKey, oe, ue,
                    dicPermissions,
                    documentIDBytes,
                    dicRevision,
                    dicLength,
                    encryptMetadata, true));
        }
        else if( isUserPassword(password.getBytes(passwordCharset), userKey, ownerKey,
                           dicPermissions, documentIDBytes, dicRevision,
                           dicLength, encryptMetadata) )
        {
            currentAccessPermission = new AccessPermission(dicPermissions);
            currentAccessPermission.setReadOnly();
            setCurrentAccessPermission(currentAccessPermission);
            
            setEncryptionKey(
                computeEncryptedKey(
                    password.getBytes(passwordCharset),
                    ownerKey, userKey, oe, ue,
                    dicPermissions,
                    documentIDBytes,
                    dicRevision,
                    dicLength,
                    encryptMetadata, false));
        }
        else
        {
            throw new InvalidPasswordException("Cannot decrypt PDF, the password is incorrect");
        }

        if (dicRevision == REVISION_5 || dicRevision == REVISION_6)
        {
            validatePerms(encryption, dicPermissions, encryptMetadata);
        }
    }

    private byte[] getDocumentIDBytes(COSArray documentIDArray)
    {
        //some documents may not have document id, see
        //test\encryption\encrypted_doc_no_id.pdf
        byte[] documentIDBytes;
        if( documentIDArray != null && documentIDArray.size() >= 1 )
        {
            COSString id = (COSString)documentIDArray.getObject( 0 );
            documentIDBytes = id.getBytes();
        }
        else
        {
            documentIDBytes = new byte[0];
        }
        return documentIDBytes;
    }

    // Algorithm 13: validate permissions ("Perms" field). Relaxed to accommodate buggy encoders
    // https://www.adobe.com/content/dam/Adobe/en/devnet/acrobat/pdfs/adobe_supplement_iso32000.pdf
    private void validatePerms(PDEncryption encryption, int dicPermissions, boolean encryptMetadata) throws IOException
    {
        try
        {
            // "Decrypt the 16-byte Perms string using AES-256 in ECB mode with an 
            // initialization vector of zero and the file encryption key as the key."
            @SuppressWarnings({"squid:S5542","lgtm [java/weak-cryptographic-algorithm]"})
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getEncryptionKey(), "AES"));
            byte[] perms = cipher.doFinal(encryption.getPerms());
            
            // "Verify that bytes 9-11 of the result are the characters ‘a’, ‘d’, ‘b’."
            if (perms[9] != 'a' || perms[10] != 'd' || perms[11] != 'b')
            {
                LOG.warn("Verification of permissions failed (constant)");
            }
            
            // "Bytes 0-3 of the decrypted Perms entry, treated as a little-endian integer, 
            // are the user permissions. They should match the value in the P key."
            int permsP = perms[0] & 0xFF | (perms[1] & 0xFF) << 8 | (perms[2] & 0xFF) << 16 |
                    (perms[3] & 0xFF) << 24;
            
            if (permsP != dicPermissions)
            {
                LOG.warn("Verification of permissions failed (" + String.format("%08X",permsP) +
                        " != " + String.format("%08X",dicPermissions) + ")");
            }
            
            if (encryptMetadata && perms[8] != 'T' || !encryptMetadata && perms[8] != 'F')
            {
                LOG.warn("Verification of permissions failed (EncryptMetadata)");
            }
        }
        catch (GeneralSecurityException e)
        {
            logIfStrongEncryptionMissing();
            throw new IOException(e);
        }
    }
    
    /**
     * Prepare document for encryption.
     *
     * @param document The document to encrypt.
     *
     * @throws IOException If there is an error accessing data.
     */
    @Override
    public void prepareDocumentForEncryption(PDDocument document) throws IOException
    {
        PDEncryption encryptionDictionary = document.getEncryption();
        if(encryptionDictionary == null)
        {
            encryptionDictionary = new PDEncryption();
        }
        int version = computeVersionNumber();
        int revision = computeRevisionNumber(version);
        encryptionDictionary.setFilter(FILTER);
        encryptionDictionary.setVersion(version);
        if (version != REVISION_4 && version != REVISION_5)
        {
            // remove CF, StmF, and StrF entries that may be left from a previous encryption
            encryptionDictionary.removeV45filters();
        }
        encryptionDictionary.setRevision(revision);
        encryptionDictionary.setLength(getKeyLength());

        StandardProtectionPolicy protectionPolicy = getProtectionPolicy();
        String ownerPassword = protectionPolicy.getOwnerPassword();
        String userPassword = protectionPolicy.getUserPassword();
        if( ownerPassword == null )
        {
            ownerPassword = "";
        }
        if( userPassword == null )
        {
            userPassword = "";
        }
 
        // If no owner password is set, use the user password instead.
        if (ownerPassword.isEmpty())
        {
            ownerPassword = userPassword;
        }

        int permissionInt = protectionPolicy.getPermissions().getPermissionBytes();

        encryptionDictionary.setPermissions(permissionInt);

        int length = getKeyLength()/8;

        if (revision == REVISION_6)
        {
            // PDFBOX-4155
            ownerPassword = SaslPrep.saslPrepStored(ownerPassword);
            userPassword = SaslPrep.saslPrepStored(userPassword);
            prepareEncryptionDictRev6(ownerPassword, userPassword, encryptionDictionary, permissionInt);
        }
        else
        {
            prepareEncryptionDictRev234(ownerPassword, userPassword, encryptionDictionary, permissionInt,
                    document, revision, length);
        }

        document.setEncryptionDictionary( encryptionDictionary );
        document.getDocument().setEncryptionDictionary(encryptionDictionary.getCOSObject());
    }

    private void prepareEncryptionDictRev6(String ownerPassword, String userPassword,
            PDEncryption encryptionDictionary, int permissionInt)
            throws IOException
    {
        try
        {
            SecureRandom rnd = new SecureRandom();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            // make a random 256-bit file encryption key
            setEncryptionKey(new byte[32]);
            rnd.nextBytes(getEncryptionKey());

            // Algorithm 8a: Compute U
            byte[] userPasswordBytes = truncate127(userPassword.getBytes(StandardCharsets.UTF_8));
            byte[] userValidationSalt = new byte[8];
            byte[] userKeySalt = new byte[8];
            rnd.nextBytes(userValidationSalt);
            rnd.nextBytes(userKeySalt);
            byte[] hashU = computeHash2B(concat(userPasswordBytes, userValidationSalt),
                    userPasswordBytes, null);
            byte[] u = concat(hashU, userValidationSalt, userKeySalt);

            // Algorithm 8b: Compute UE
            byte[] hashUE = computeHash2B(concat(userPasswordBytes, userKeySalt),
                    userPasswordBytes, null);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(hashUE, "AES"),
                    // "an initialization vector of zero"
                    new IvParameterSpec(new byte[16]));
            byte[] ue = cipher.doFinal(getEncryptionKey());

            // Algorithm 9a: Compute O
            byte[] ownerPasswordBytes = truncate127(ownerPassword.getBytes(StandardCharsets.UTF_8));
            byte[] ownerValidationSalt = new byte[8];
            byte[] ownerKeySalt = new byte[8];
            rnd.nextBytes(ownerValidationSalt);
            rnd.nextBytes(ownerKeySalt);
            byte[] hashO = computeHash2B(concat(ownerPasswordBytes, ownerValidationSalt, u),
                    ownerPasswordBytes, u);
            byte[] o = concat(hashO, ownerValidationSalt, ownerKeySalt);

            // Algorithm 9b: Compute OE
            byte[] hashOE = computeHash2B(concat(ownerPasswordBytes, ownerKeySalt, u),
                    ownerPasswordBytes, u);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(hashOE, "AES"),
                    // "an initialization vector of zero"
                    new IvParameterSpec(new byte[16]));
            byte[] oe = cipher.doFinal(getEncryptionKey());

            // Set keys and other required constants in encryption dictionary
            encryptionDictionary.setUserKey(u);
            encryptionDictionary.setUserEncryptionKey(ue);
            encryptionDictionary.setOwnerKey(o);
            encryptionDictionary.setOwnerEncryptionKey(oe);

            prepareEncryptionDictAES(encryptionDictionary, COSName.AESV3);

            // Algorithm 10: compute "Perms" value
            byte[] perms = new byte[16];
            perms[0] = (byte) permissionInt;
            perms[1] = (byte) (permissionInt >>> 8);
            perms[2] = (byte) (permissionInt >>> 16);
            perms[3] = (byte) (permissionInt >>> 24);
            perms[4] = (byte) 0xFF;
            perms[5] = (byte) 0xFF;
            perms[6] = (byte) 0xFF;
            perms[7] = (byte) 0xFF;
            perms[8] = 'T';    // we always encrypt Metadata
            perms[9] = 'a';
            perms[10] = 'd';
            perms[11] = 'b';
            for (int i = 12; i <= 15; i++)
            {
                perms[i] = (byte) rnd.nextInt();
            }

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getEncryptionKey(), "AES"),
                    // "an initialization vector of zero"
                    new IvParameterSpec(new byte[16]));

            byte[] permsEnc = cipher.doFinal(perms);

            encryptionDictionary.setPerms(permsEnc);
        }
        catch (GeneralSecurityException e)
        {
            logIfStrongEncryptionMissing();
            throw new IOException(e);
        }
    }

    private void prepareEncryptionDictRev234(String ownerPassword, String userPassword,
            PDEncryption encryptionDictionary, int permissionInt, PDDocument document, 
            int revision, int length)
            throws IOException
    {
        COSArray idArray = document.getDocument().getDocumentID();

        //check if the document has an id yet.  If it does not then generate one
        if (idArray == null || idArray.size() < 2)
        {
            MessageDigest md = MessageDigests.getMD5();
            BigInteger time = BigInteger.valueOf(System.currentTimeMillis());
            md.update(time.toByteArray());
            md.update(ownerPassword.getBytes(StandardCharsets.ISO_8859_1));
            md.update(userPassword.getBytes(StandardCharsets.ISO_8859_1));
            md.update(document.getDocument().toString().getBytes(StandardCharsets.ISO_8859_1));

            byte[] id = md.digest(this.toString().getBytes(StandardCharsets.ISO_8859_1));
            COSString idString = new COSString(id);

            idArray = new COSArray();
            idArray.add(idString);
            idArray.add(idString);
            document.getDocument().setDocumentID(idArray);
        }

        COSString id = (COSString) idArray.getObject(0);

        byte[] ownerBytes = computeOwnerPassword(
                ownerPassword.getBytes(StandardCharsets.ISO_8859_1),
                userPassword.getBytes(StandardCharsets.ISO_8859_1), revision, length);

        byte[] userBytes = computeUserPassword(
                userPassword.getBytes(StandardCharsets.ISO_8859_1),
                ownerBytes, permissionInt, id.getBytes(), revision, length, true);

        setEncryptionKey(computeEncryptedKeyRev234(userPassword.getBytes(StandardCharsets.ISO_8859_1), ownerBytes, permissionInt, id.getBytes(), true, length, revision));

        encryptionDictionary.setOwnerKey(ownerBytes);
        encryptionDictionary.setUserKey(userBytes);
        
        if (revision == REVISION_4)
        {
            prepareEncryptionDictAES(encryptionDictionary, COSName.AESV2);
        }
    }

    private void prepareEncryptionDictAES(PDEncryption encryptionDictionary, COSName aesVName)
    {
        PDCryptFilterDictionary cryptFilterDictionary = new PDCryptFilterDictionary();
        cryptFilterDictionary.setCryptFilterMethod(aesVName);
        cryptFilterDictionary.setLength(getKeyLength());
        encryptionDictionary.setStdCryptFilterDictionary(cryptFilterDictionary);
        encryptionDictionary.setStreamFilterName(COSName.STD_CF);
        encryptionDictionary.setStringFilterName(COSName.STD_CF);
        setAES(true);
    }

    /**
     * Check for owner password.
     *
     * @param ownerPassword The owner password.
     * @param user The u entry of the encryption dictionary.
     * @param owner The o entry of the encryption dictionary.
     * @param permissions The set of permissions on the document.
     * @param id The document id.
     * @param encRevision The encryption algorithm revision.
     * @param keyLengthInBytes The encryption key length in bytes.
     * @param encryptMetadata The encryption metadata
     *
     * @return True If the ownerPassword param is the owner password.
     *
     * @throws IOException If there is an error accessing data.
     */
    public boolean isOwnerPassword(byte[] ownerPassword, byte[] user, byte[] owner,
                                   int permissions, byte[] id, int encRevision, int keyLengthInBytes,
                                   boolean encryptMetadata) throws IOException
    {
        switch (encRevision)
        {
        case REVISION_2:
        case REVISION_3:
        case REVISION_4:
            return isOwnerPassword234(ownerPassword, user, owner, permissions, id, encRevision,
                    keyLengthInBytes, encryptMetadata);
        case REVISION_5:
        case REVISION_6:
            return isOwnerPassword56(ownerPassword, user, owner, encRevision);
        default:
            throw new IOException("Unknown Encryption Revision " + encRevision);
        }
    }

    private boolean isOwnerPassword234(byte[] ownerPassword, byte[] user, byte[] owner,
            int permissions,
            byte[] id, int encRevision, int keyLengthInBytes, boolean encryptMetadata)
            throws IOException
    {
        byte[] userPassword = getUserPassword234(ownerPassword, owner, encRevision,
                keyLengthInBytes);
        return isUserPassword234(userPassword, user, owner, permissions, id, encRevision,
                keyLengthInBytes, encryptMetadata);
    }

    private boolean isOwnerPassword56(byte[] ownerPassword, byte[] user, byte[] owner,
            int encRevision) throws IOException
    {
        if (owner.length < 40)
        {
            // PDFBOX-5104
            throw new IOException("Owner password is too short");
        }
        byte[] truncatedOwnerPassword = truncate127(ownerPassword);
        byte[] oHash = new byte[32];
        byte[] oValidationSalt = new byte[8];
        System.arraycopy(owner, 0, oHash, 0, 32);
        System.arraycopy(owner, 32, oValidationSalt, 0, 8);

        if (encRevision == REVISION_5)
        {
            return MessageDigest.isEqual(computeSHA256(truncatedOwnerPassword, oValidationSalt, user),
                    oHash);
        }
        else
        {
            return MessageDigest.isEqual(computeHash2A(truncatedOwnerPassword, oValidationSalt, user),
                    oHash);
        }
    }

    /**
     * Get the user password based on the owner password.
     *
     * @param ownerPassword The plaintext owner password.
     * @param owner The o entry of the encryption dictionary.
     * @param encRevision The encryption revision number.
     * @param length The key length.
     *
     * @return The u entry of the encryption dictionary.
     *
     * @throws IOException If there is an error accessing data while generating the user password.
     */
    public byte[] getUserPassword( byte[] ownerPassword,  byte[] owner, int encRevision,
                                   int length ) throws IOException
    {
        // TODO ?!?!
        if (encRevision == REVISION_5 || encRevision == REVISION_6)
        {
            return new byte[0];
        }
        else
        {
            return getUserPassword234(ownerPassword, owner, encRevision, length);
        }
    }

    private byte[] getUserPassword234(byte[] ownerPassword, byte[] owner, int encRevision,
            int length)
            throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] rc4Key = computeRC4key(ownerPassword, encRevision, length);

        if (encRevision == REVISION_2)
        {
            encryptDataRC4(rc4Key, owner, result);
        }
        else if (encRevision == REVISION_3 || encRevision == REVISION_4)
        {
            byte[] iterationKey = new byte[rc4Key.length];
            byte[] otemp = new byte[owner.length];
            System.arraycopy(owner, 0, otemp, 0, owner.length);

            for (int i = 19; i >= 0; i--)
            {
                System.arraycopy(rc4Key, 0, iterationKey, 0, rc4Key.length);
                for (int j = 0; j < iterationKey.length; j++)
                {
                    iterationKey[j] = (byte) (iterationKey[j] ^ (byte) i);
                }
                result.reset();
                encryptDataRC4(iterationKey, otemp, result);
                otemp = result.toByteArray();
            }
        }
        return result.toByteArray();
    }

    /**
     * Compute the encryption key.
     *
     * @param password The password to compute the encrypted key.
     * @param o The O entry of the encryption dictionary.
     * @param u The U entry of the encryption dictionary.
     * @param oe The OE entry of the encryption dictionary.
     * @param ue The UE entry of the encryption dictionary.
     * @param permissions The permissions for the document.
     * @param id The document id.
     * @param encRevision The revision of the encryption algorithm.
     * @param keyLengthInBytes The length of the encryption key in bytes.
     * @param encryptMetadata The encryption metadata
     * @param isOwnerPassword whether the password given is the owner password (for revision 6)
     *
     * @return The encrypted key bytes.
     *
     * @throws IOException If there is an error with encryption.
     */
    public byte[] computeEncryptedKey(byte[] password, byte[] o, byte[] u, byte[] oe, byte[] ue,
                                      int permissions, byte[] id, int encRevision, int keyLengthInBytes,
                                      boolean encryptMetadata, boolean isOwnerPassword)
                                      throws IOException
    {
        if (encRevision == REVISION_5 || encRevision == REVISION_6)
        {
            return computeEncryptedKeyRev56(password, isOwnerPassword, o, u, oe, ue, encRevision);
        }
        else
        {
            return computeEncryptedKeyRev234(password, o, permissions, id, encryptMetadata, keyLengthInBytes, encRevision);
        }
    }

    private byte[] computeEncryptedKeyRev234(byte[] password, byte[] o, int permissions, 
            byte[] id, boolean encryptMetadata, int length, int encRevision)
    {
        //Algorithm 2, based on MD5

        //PDFReference 1.4 pg 78
        byte[] padded = truncateOrPad(password);

        MessageDigest md = MessageDigests.getMD5();
        md.update(padded);

        md.update(o);

        md.update((byte) permissions);
        md.update((byte) (permissions >>> 8));
        md.update((byte) (permissions >>> 16));
        md.update((byte) (permissions >>> 24));

        md.update(id);

        //(Security handlers of revision 4 or greater) If document metadata is not being
        // encrypted, pass 4 bytes with the value 0xFFFFFFFF to the MD5 hash function.
        //see 7.6.3.3 Algorithm 2 Step f of PDF 32000-1:2008
        if (encRevision == REVISION_4 && !encryptMetadata)
        {
            md.update(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff });
        }
        byte[] digest = md.digest();

        if (encRevision == REVISION_3 || encRevision == REVISION_4)
        {
            for (int i = 0; i < 50; i++)
            {
                md.update(digest, 0, length);
                digest = md.digest();
            }
        }

        byte[] result = new byte[length];
        System.arraycopy(digest, 0, result, 0, length);
        return result;
    }

    private byte[] computeEncryptedKeyRev56(byte[] password, boolean isOwnerPassword, 
            byte[] o, byte[] u, byte[] oe, byte[] ue, int encRevision) 
            throws IOException
    {
        byte[] hash, fileKeyEnc;

        if (isOwnerPassword)
        {
            if (oe == null)
            {
                throw new IOException("/Encrypt/OE entry is missing");
            }
            byte[] oKeySalt = new byte[8];
            System.arraycopy(o, 40, oKeySalt, 0, 8);

            if (encRevision == REVISION_5)
            {
                hash = computeSHA256(password, oKeySalt, u);
            }
            else
            {
                hash = computeHash2A(password, oKeySalt, u);
            }

            fileKeyEnc = oe;
        }
        else
        {
            if (ue == null)
            {
                throw new IOException("/Encrypt/UE entry is missing");
            }
            byte[] uKeySalt = new byte[8];
            System.arraycopy(u, 40, uKeySalt, 0, 8);

            if (encRevision == REVISION_5)
            {
                hash = computeSHA256(password, uKeySalt, null);
            }
            else
            {
                hash = computeHash2A(password, uKeySalt, null);
            }

            fileKeyEnc = ue;
        }
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(hash, "AES"), new IvParameterSpec(new byte[16]));
            return cipher.doFinal(fileKeyEnc);
        }
        catch (GeneralSecurityException e)
        {
            logIfStrongEncryptionMissing();
            throw new IOException(e);
        }
    }

    /**
     * This will compute the user password hash.
     *
     * @param password The plain text password.
     * @param owner The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param encRevision The revision of the encryption.
     * @param keyLengthInBytes The length of the encryption key in bytes.
     * @param encryptMetadata The encryption metadata
     *
     * @return The user password.
     *
     * @throws IOException if the password could not be computed
     */
    public byte[] computeUserPassword(byte[] password, byte[] owner, int permissions,
                                      byte[] id, int encRevision, int keyLengthInBytes,
                                      boolean encryptMetadata) throws IOException
    {
        // TODO!?!?
        if (encRevision == REVISION_5 || encRevision == REVISION_6)
        {
            return new byte[0];
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] encKey = computeEncryptedKeyRev234(password, owner, permissions,
                id, encryptMetadata, keyLengthInBytes, encRevision);
        
        if (encRevision == REVISION_2)
        {
            encryptDataRC4(encKey, ENCRYPT_PADDING, result );
        }
        else if (encRevision == REVISION_3 || encRevision == REVISION_4)
        {
            MessageDigest md = MessageDigests.getMD5();
            md.update( ENCRYPT_PADDING );

            md.update( id );
            result.write( md.digest() );

            byte[] iterationKey = new byte[ encKey.length ];
            for( int i=0; i<20; i++ )
            {
                System.arraycopy( encKey, 0, iterationKey, 0, iterationKey.length );
                for( int j=0; j< iterationKey.length; j++ )
                {
                    iterationKey[j] = (byte)(iterationKey[j] ^ i);
                }
                ByteArrayInputStream input = new ByteArrayInputStream(result.toByteArray());
                result.reset();
                encryptDataRC4(iterationKey, input, result);
            }

            byte[] finalResult = new byte[32];
            System.arraycopy( result.toByteArray(), 0, finalResult, 0, 16 );
            System.arraycopy( ENCRYPT_PADDING, 0, finalResult, 16, 16 );
            result.reset();
            result.write( finalResult );
        }
        return result.toByteArray();
    }

    /**
     * Compute the owner entry in the encryption dictionary.
     *
     * @param ownerPassword The plaintext owner password.
     * @param userPassword The plaintext user password.
     * @param encRevision The revision number of the encryption algorithm.
     * @param length The length of the encryption key.
     *
     * @return The o entry of the encryption dictionary.
     *
     * @throws IOException if the owner password could not be computed
     */
    public byte[] computeOwnerPassword(byte[] ownerPassword, byte[] userPassword,
                                       int encRevision,  int length ) throws IOException
    {
        if (REVISION_2 == encRevision && length != 5)
        {
            throw new IOException("Expected length=5 actual=" + length );
        }
        
        byte[] rc4Key = computeRC4key(ownerPassword, encRevision, length);
        byte[] paddedUser = truncateOrPad( userPassword );

        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        encryptDataRC4(rc4Key, new ByteArrayInputStream(paddedUser), encrypted);

        if (encRevision == REVISION_3 || encRevision == REVISION_4)
        {
            byte[] iterationKey = new byte[ rc4Key.length ];
            for( int i=1; i<20; i++ )
            {
                System.arraycopy( rc4Key, 0, iterationKey, 0, rc4Key.length );
                for( int j=0; j< iterationKey.length; j++ )
                {
                    iterationKey[j] = (byte)(iterationKey[j] ^ (byte)i);
                }
                ByteArrayInputStream input = new ByteArrayInputStream( encrypted.toByteArray() );
                encrypted.reset();
                encryptDataRC4(iterationKey, input, encrypted );
            }
        }

        return encrypted.toByteArray();
    }

    // steps (a) to (d) of "Algorithm 3: Computing the encryption dictionary’s O (owner password) value".
    private byte[] computeRC4key(byte[] ownerPassword, int encRevision, int length)
    {
        MessageDigest md = MessageDigests.getMD5();
        byte[] digest = md.digest(truncateOrPad(ownerPassword));
        if (encRevision == REVISION_3 || encRevision == REVISION_4)
        {
            for (int i = 0; i < 50; i++)
            {
                // this deviates from the spec - however, omitting the length
                // parameter prevents the file to be opened in Adobe Reader
                // with the owner password when the key length is 40 bit (= 5 bytes)
                md.update(digest, 0, length);
                digest = md.digest();
            }
        }
        byte[] rc4Key = new byte[length];
        System.arraycopy(digest, 0, rc4Key, 0, length);
        return rc4Key;
    }


    /**
     * This will take the password and truncate or pad it as necessary.
     *
     * @param password The password to pad or truncate.
     *
     * @return The padded or truncated password.
     */
    private byte[] truncateOrPad( byte[] password )
    {
        byte[] padded = new byte[ ENCRYPT_PADDING.length ];
        int bytesBeforePad = Math.min( password.length, padded.length );
        System.arraycopy( password, 0, padded, 0, bytesBeforePad );
        System.arraycopy( ENCRYPT_PADDING, 0, padded, bytesBeforePad,
                          ENCRYPT_PADDING.length-bytesBeforePad );
        return padded;
    }

    /**
     * Check if a plaintext password is the user password.
     *
     * @param password The plaintext password.
     * @param user The u entry of the encryption dictionary.
     * @param owner The o entry of the encryption dictionary.
     * @param permissions The permissions set in the PDF.
     * @param id The document id used for encryption.
     * @param encRevision The revision of the encryption algorithm.
     * @param keyLengthInBytes The length of the encryption key in bytes.
     * @param encryptMetadata The encryption metadata.
     *
     * @return true If the plaintext password is the user password.
     *
     * @throws IOException If there is an error accessing data.
     */
    public boolean isUserPassword(byte[] password, byte[] user, byte[] owner, int permissions,
                                  byte[] id, int encRevision, int keyLengthInBytes, boolean encryptMetadata)
                                  throws IOException
    {
        switch (encRevision)
        {
        case REVISION_2:
        case REVISION_3:
        case REVISION_4:
                return isUserPassword234(password, user, owner, permissions, id, encRevision,
                                         keyLengthInBytes, encryptMetadata);
        case REVISION_5:
        case REVISION_6:
                return isUserPassword56(password, user, encRevision);
            default:
                throw new IOException("Unknown Encryption Revision " + encRevision);
        }
    }

    private boolean isUserPassword234(byte[] password, byte[] user, byte[] owner, int permissions,
            byte[] id, int encRevision, int length, boolean encryptMetadata)
            throws IOException
    {
        byte[] passwordBytes = computeUserPassword(password, owner, permissions, id, encRevision,
                                                   length, encryptMetadata);
        if (encRevision == REVISION_2)
        {
            return MessageDigest.isEqual(user, passwordBytes);
        }
        else
        {
            // compare first 16 bytes only
            return MessageDigest.isEqual(Arrays.copyOf(user, 16), Arrays.copyOf(passwordBytes, 16));
        }
    }

    private boolean isUserPassword56(byte[] password, byte[] user, int encRevision) throws IOException
    {
        byte[] truncatedPassword = truncate127(password);
        byte[] uHash = new byte[32];
        byte[] uValidationSalt = new byte[8];
        System.arraycopy(user, 0, uHash, 0, 32);
        System.arraycopy(user, 32, uValidationSalt, 0, 8);

        byte[] hash;
        if (encRevision == REVISION_5)
        {
            hash = computeSHA256(truncatedPassword, uValidationSalt, null);
        }
        else
        {
            hash = computeHash2A(truncatedPassword, uValidationSalt, null);
        }

        return MessageDigest.isEqual(hash, uHash);
    }

    /**
     * Check if a plaintext password is the user password.
     *
     * @param password The plaintext password.
     * @param user The u entry of the encryption dictionary.
     * @param owner The o entry of the encryption dictionary.
     * @param permissions The permissions set in the PDF.
     * @param id The document id used for encryption.
     * @param encRevision The revision of the encryption algorithm.
     * @param keyLengthInBytes The length of the encryption key in bytes.
     * @param encryptMetadata The encryption metadata
     *
     * @return true If the plaintext password is the user password.
     *
     * @throws IOException If there is an error accessing data.
     */
    public boolean isUserPassword(String password, byte[] user, byte[] owner, int permissions,
                                  byte[] id, int encRevision, int keyLengthInBytes, boolean encryptMetadata)
                                  throws IOException
    {
        if (encRevision == REVISION_5 || encRevision == REVISION_6)
        {
            return isUserPassword(password.getBytes(StandardCharsets.UTF_8), user, owner, permissions, id,
                    encRevision, keyLengthInBytes, encryptMetadata);
        }
        else
        {
            return isUserPassword(password.getBytes(StandardCharsets.ISO_8859_1), user, owner, permissions, id,
                    encRevision, keyLengthInBytes, encryptMetadata);
        }
    }

    /**
     * Check for owner password.
     *
     * @param password The owner password.
     * @param user The u entry of the encryption dictionary.
     * @param owner The o entry of the encryption dictionary.
     * @param permissions The set of permissions on the document.
     * @param id The document id.
     * @param encRevision The encryption algorithm revision.
     * @param keyLengthInBytes The encryption key length in bytes.
     * @param encryptMetadata The encryption metadata
     *
     * @return True If the ownerPassword param is the owner password.
     *
     * @throws IOException If there is an error accessing data.
     */
    public boolean isOwnerPassword(String password, byte[] user, byte[] owner, int permissions,
                                   byte[] id, int encRevision, int keyLengthInBytes, boolean encryptMetadata)
                                   throws IOException
    {
        return isOwnerPassword(password.getBytes(StandardCharsets.ISO_8859_1), user,owner,permissions, id,
                               encRevision, keyLengthInBytes, encryptMetadata);
    }

    // Algorithm 2.A from ISO 32000-1
    private byte[] computeHash2A(byte[] password, byte[] salt, byte[] u) throws IOException
    {
        byte[] userKey = adjustUserKey(u);
        byte[] truncatedPassword = truncate127(password);
        byte[] input = concat(truncatedPassword, salt, userKey);
        return computeHash2B(input, truncatedPassword, userKey);
    }
    
    // Algorithm 2.B from ISO 32000-2
    private static byte[] computeHash2B(byte[] input, byte[] password, byte[] userKey)
            throws IOException
    {
        try
        {
            MessageDigest md = MessageDigests.getSHA256();
            byte[] k = md.digest(input);
            
            byte[] e = null;
            for (int round = 0; round < 64 || (e[e.length-1] & 0xFF) > round - 32; round++)
            {                
                byte[] k1;
                if (userKey != null && userKey.length >= 48)
                {
                    k1 = new byte[64*(password.length + k.length + 48)];
                }
                else
                {
                    k1 = new byte[64*(password.length + k.length)];
                }
                
                int pos = 0;
                for (int i = 0; i < 64; i++)
                {
                    System.arraycopy(password, 0, k1, pos, password.length);
                    pos += password.length;
                    System.arraycopy(k, 0, k1, pos, k.length);
                    pos += k.length;
                    if (userKey != null && userKey.length >= 48)
                    {
                        System.arraycopy(userKey, 0, k1, pos, 48);
                        pos += 48;
                    }
                }
                
                byte[] kFirst = new byte[16];
                byte[] kSecond = new byte[16];
                System.arraycopy(k, 0, kFirst, 0, 16);
                System.arraycopy(k, 16, kSecond, 0, 16);
                
                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
                SecretKeySpec keySpec = new SecretKeySpec(kFirst, "AES");
                IvParameterSpec ivSpec = new IvParameterSpec(kSecond);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                e = cipher.doFinal(k1);
                
                byte[] eFirst = new byte[16];
                System.arraycopy(e, 0, eFirst, 0, 16);
                BigInteger bi = new BigInteger(1, eFirst);
                BigInteger remainder = bi.mod(new BigInteger("3"));
                String nextHash = HASHES_2B[remainder.intValue()];
                
                md = MessageDigest.getInstance(nextHash);
                k = md.digest(e);
            }
            
            if (k.length > 32)
            {
                byte[] kTrunc = new byte[32];
                System.arraycopy(k, 0, kTrunc, 0, 32);
                return kTrunc;
            }
            else
            {
                return k;
            }            
        }
        catch (GeneralSecurityException e)
        {
            logIfStrongEncryptionMissing();
            throw new IOException(e);
        }
    }

    private static byte[] computeSHA256(byte[] input, byte[] password, byte[] userKey) throws IOException
    {
        MessageDigest md = MessageDigests.getSHA256();
        md.update(input);
        md.update(password);
        return md.digest(adjustUserKey(userKey));
    }

    private static byte[] adjustUserKey(byte[] u) throws IOException
    {
        if (u == null)
        {
            return new byte[0];
        }
        if (u.length < 48)
        {
            throw new IOException("Bad U length");
        }
        if (u.length > 48)
        {
            // must truncate
            byte[] userKey = new byte[48];
            System.arraycopy(u, 0, userKey, 0, 48);
            return userKey;
        }
        return u;
    }

    private static byte[] concat(byte[] a, byte[] b)
    {
        byte[] o = new byte[a.length + b.length];
        System.arraycopy(a, 0, o, 0, a.length);
        System.arraycopy(b, 0, o, a.length, b.length);
        return o;
    }
    
    private static byte[] concat(byte[] a, byte[] b, byte[] c)
    {
        byte[] o = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, o, 0, a.length);
        System.arraycopy(b, 0, o, a.length, b.length);
        System.arraycopy(c, 0, o, a.length + b.length, c.length);
        return o;
    }
    
    private static byte[] truncate127(byte[] in)
    {
        if (in.length <= 127)
        {
            return in;
        }
        byte[] trunc = new byte[127];
        System.arraycopy(in, 0, trunc, 0, 127);
        return trunc;
    }

    private static void logIfStrongEncryptionMissing()
    {
        try
        {
            if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE)
            {
                LOG.warn("JCE unlimited strength jurisdiction policy files are not installed");
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            LOG.debug("AES Algorithm not available", ex);
        }
    }
}
