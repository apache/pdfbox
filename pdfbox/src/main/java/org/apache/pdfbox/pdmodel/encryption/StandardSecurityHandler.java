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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.encryption.ARCFour;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * The class implements the standard security handler as decribed
 * in the PDF specifications. This security handler protects document
 * with password.
 *
 * @see StandardProtectionPolicy to see how to protect document with this security handler.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 *
 */

public class StandardSecurityHandler extends SecurityHandler
{
    /**
     * Type of security handler.
     */
    public static final String FILTER = "Standard";

    private static final int DEFAULT_VERSION = 1;

    private static final int DEFAULT_REVISION = 3;

    private int revision = DEFAULT_REVISION;

    private StandardProtectionPolicy policy;

    private ARCFour rc4 = new ARCFour();

    /**
     * Protection policy class for this handler.
     */
    public static final Class<?> PROTECTION_POLICY_CLASS = StandardProtectionPolicy.class;

    /**
     * Standard padding for encryption.
     */
    public static final byte[] ENCRYPT_PADDING =
    {
        (byte)0x28, (byte)0xBF, (byte)0x4E, (byte)0x5E, (byte)0x4E,
        (byte)0x75, (byte)0x8A, (byte)0x41, (byte)0x64, (byte)0x00,
        (byte)0x4E, (byte)0x56, (byte)0xFF, (byte)0xFA, (byte)0x01,
        (byte)0x08, (byte)0x2E, (byte)0x2E, (byte)0x00, (byte)0xB6,
        (byte)0xD0, (byte)0x68, (byte)0x3E, (byte)0x80, (byte)0x2F,
        (byte)0x0C, (byte)0xA9, (byte)0xFE, (byte)0x64, (byte)0x53,
        (byte)0x69, (byte)0x7A
    };

    /**
     * Constructor.
     */
    public StandardSecurityHandler()
    {
    }

    /**
     * Constructor used for encryption.
     *
     * @param p The protection policy.
     */
    public StandardSecurityHandler(StandardProtectionPolicy p)
    {
        policy = p;
        keyLength = policy.getEncryptionKeyLength();
    }


    /**
     * Computes the version number of the StandardSecurityHandler
     * regarding the encryption key length.
     * See PDF Spec 1.6 p 93
     *
     * @return The computed cersion number.
     */
    private int computeVersionNumber()
    {
        if(keyLength == 40)
        {
            return DEFAULT_VERSION;
        }
        return 2;
    }

    /**
     * Computes the revision version of the StandardSecurityHandler to
     * use regarding the version number and the permissions bits set.
     * See PDF Spec 1.6 p98
     *
     * @return The computed revision number.
     */
    private int computeRevisionNumber()
    {
        if(version < 2 && !policy.getPermissions().hasAnyRevision3PermissionSet())
        {
            return 2;
        }
        if ( version == 2 || version == 3 || policy.getPermissions().hasAnyRevision3PermissionSet())
        {
            return 3;
        }
        return 4;
    }

    /**
     * Decrypt the document.
     *
     * @param doc The document to be decrypted.
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void decryptDocument(PDDocument doc, DecryptionMaterial decryptionMaterial)
        throws CryptographyException, IOException
    {
        document = doc;

        PDEncryptionDictionary dictionary = document.getEncryptionDictionary();
        COSArray documentIDArray = document.getDocument().getDocumentID();
        
        prepareForDecryption(dictionary, documentIDArray, decryptionMaterial);
        
        this.proceedDecryption();
    }

    /**
     * Prepares everything to decrypt the document.
     *
     * If {@link #decryptDocument(PDDocument, DecryptionMaterial)} is used, this method is
     * called from there. Only if decryption of single objects is needed this should be called instead.
     *
     * @param encDictionary  encryption dictionary, can be retrieved via {@link PDDocument#getEncryptionDictionary()}
     * @param documentIDArray  document id which is returned via {@link COSDocument#getDocumentID()}
     * @param decryptionMaterial Information used to decrypt the document.
     *
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void prepareForDecryption(PDEncryptionDictionary encDictionary, COSArray documentIDArray,
            DecryptionMaterial decryptionMaterial)
        throws CryptographyException, IOException
    {
        if(!(decryptionMaterial instanceof StandardDecryptionMaterial))
        {
            throw new CryptographyException("Provided decryption material is not compatible with the document");
        }

        StandardDecryptionMaterial material = (StandardDecryptionMaterial)decryptionMaterial;

        String password = material.getPassword();
        if(password == null)
        {
            password = "";
        }

        int dicPermissions = encDictionary.getPermissions();
        int dicRevision = encDictionary.getRevision();
        int dicLength = encDictionary.getLength()/8;

        //some documents may have not document id, see
        //test\encryption\encrypted_doc_no_id.pdf
        byte[] documentIDBytes = null;
        if( documentIDArray != null && documentIDArray.size() >= 1 )
        {
            COSString id = (COSString)documentIDArray.getObject( 0 );
            documentIDBytes = id.getBytes();
        }
        else
        {
            documentIDBytes = new byte[0];
        }

        // we need to know whether the meta data was encrypted for password calculation
        boolean encryptMetadata = encDictionary.isEncryptMetaData();
        
        byte[] u = encDictionary.getUserKey();
        byte[] o = encDictionary.getOwnerKey();

        boolean isUserPassword =
            isUserPassword(
                password.getBytes("ISO-8859-1"),
                u,
                o,
                dicPermissions,
                documentIDBytes,
                dicRevision,
                dicLength,
                encryptMetadata);
        boolean isOwnerPassword =
            isOwnerPassword(
                password.getBytes("ISO-8859-1"),
                u,
                o,
                dicPermissions,
                documentIDBytes,
                dicRevision,
                dicLength,
                encryptMetadata);

        if( isUserPassword )
        {
            currentAccessPermission = new AccessPermission( dicPermissions );
            encryptionKey =
                computeEncryptedKey(
                    password.getBytes("ISO-8859-1"),
                    o,
                    dicPermissions,
                    documentIDBytes,
                    dicRevision,
                    dicLength,
                    encryptMetadata );
        }
        else if( isOwnerPassword )
        {
            currentAccessPermission = AccessPermission.getOwnerAccessPermission();
            byte[] computedUserPassword = getUserPassword(password.getBytes("ISO-8859-1"),o,dicRevision,dicLength );
            encryptionKey =
                computeEncryptedKey(
                    computedUserPassword,
                    o,
                    dicPermissions,
                    documentIDBytes,
                    dicRevision,
                    dicLength,
                    encryptMetadata);
        }
        else
        {
            throw new CryptographyException(
                "Error: The supplied password does not match either the owner or user password in the document." );
        }

        // detect whether AES encryption is used. This assumes that the encryption algo is 
        // stored in the PDCryptFilterDictionary
        PDCryptFilterDictionary stdCryptFilterDictionary =  encDictionary.getStdCryptFilterDictionary();

        if (stdCryptFilterDictionary != null)
        {
            COSName cryptFilterMethod = stdCryptFilterDictionary.getCryptFilterMethod();
            if (cryptFilterMethod != null) 
            {
                setAES("AESV2".equalsIgnoreCase(cryptFilterMethod.getName()));
            }
        }
    }
    
    /**
     * Prepare document for encryption.
     *
     * @param doc The documeent to encrypt.
     *
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void prepareDocumentForEncryption(PDDocument doc) throws CryptographyException, IOException
    {
        document = doc;
        PDEncryptionDictionary encryptionDictionary = document.getEncryptionDictionary();
        if(encryptionDictionary == null)
        {
            encryptionDictionary = new PDEncryptionDictionary();
        }
        version = computeVersionNumber();
        revision = computeRevisionNumber();
        encryptionDictionary.setFilter(FILTER);
        encryptionDictionary.setVersion(version);
        encryptionDictionary.setRevision(revision);
        encryptionDictionary.setLength(keyLength);

        String ownerPassword = policy.getOwnerPassword();
        String userPassword = policy.getUserPassword();
        if( ownerPassword == null )
        {
            ownerPassword = "";
        }
        if( userPassword == null )
        {
            userPassword = "";
        }

        int permissionInt = policy.getPermissions().getPermissionBytes();

        encryptionDictionary.setPermissions(permissionInt);

        int length = keyLength/8;

        COSArray idArray = document.getDocument().getDocumentID();

        //check if the document has an id yet.  If it does not then
        //generate one
        if( idArray == null || idArray.size() < 2 )
        {
            idArray = new COSArray();
            try
            {
                MessageDigest md = MessageDigest.getInstance( "MD5" );
                BigInteger time = BigInteger.valueOf( System.currentTimeMillis() );
                md.update( time.toByteArray() );
                md.update( ownerPassword.getBytes("ISO-8859-1") );
                md.update( userPassword.getBytes("ISO-8859-1") );
                md.update( document.getDocument().toString().getBytes() );
                byte[] id = md.digest( this.toString().getBytes("ISO-8859-1") );
                COSString idString = new COSString();
                idString.append( id );
                idArray.add( idString );
                idArray.add( idString );
                document.getDocument().setDocumentID( idArray );
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new CryptographyException( e );
            }
            catch(IOException e )
            {
                throw new CryptographyException( e );
            }
        }

        COSString id = (COSString)idArray.getObject( 0 );

        byte[] o = computeOwnerPassword(
            ownerPassword.getBytes("ISO-8859-1"),
            userPassword.getBytes("ISO-8859-1"), revision, length);

        byte[] u = computeUserPassword(
            userPassword.getBytes("ISO-8859-1"),
            o, permissionInt, id.getBytes(), revision, length, true);

        encryptionKey = computeEncryptedKey(
            userPassword.getBytes("ISO-8859-1"), o, permissionInt, id.getBytes(), revision, length, true);

        encryptionDictionary.setOwnerKey(o);
        encryptionDictionary.setUserKey(u);

        document.setEncryptionDictionary( encryptionDictionary );
        document.getDocument().setEncryptionDictionary(encryptionDictionary.getCOSDictionary());

    }

    /**
     * Check for owner password.
     *
     * @param ownerPassword The owner password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The set of permissions on the document.
     * @param id The document id.
     * @param encRevision The encryption algorithm revision.
     * @param length The encryption key length.
     * @param encryptMetadata The encryption metadata
     *
     * @return True If the ownerPassword param is the owner password.
     *
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isOwnerPassword(
            byte[] ownerPassword,
            byte[] u,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata)
            throws CryptographyException, IOException
    {
        byte[] userPassword = getUserPassword( ownerPassword, o, encRevision, length );
        return isUserPassword( userPassword, u, o, permissions, id, encRevision, length, encryptMetadata );
    }

    /**
     * Get the user password based on the owner password.
     *
     * @param ownerPassword The plaintext owner password.
     * @param o The o entry of the encryption dictionary.
     * @param encRevision The encryption revision number.
     * @param length The key length.
     *
     * @return The u entry of the encryption dictionary.
     *
     * @throws CryptographyException If there is an error generating the user password.
     * @throws IOException If there is an error accessing data while generating the user password.
     */
    public final byte[] getUserPassword(
            byte[] ownerPassword,
            byte[] o,
            int encRevision,
            long length )
            throws CryptographyException, IOException
    {
        try
        {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            //3.3 STEP 1
            byte[] ownerPadded = truncateOrPad( ownerPassword );

            //3.3 STEP 2
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( ownerPadded );
            byte[] digest = md.digest();

            //3.3 STEP 3
            if( encRevision == 3 || encRevision == 4 )
            {
                for( int i=0; i<50; i++ )
                {
                    md.reset();
                    md.update( digest );
                    digest = md.digest();
                }
            }
            if( encRevision == 2 && length != 5 )
            {
                throw new CryptographyException(
                    "Error: Expected length=5 actual=" + length );
            }

            //3.3 STEP 4
            byte[] rc4Key = new byte[ (int)length ];
            System.arraycopy( digest, 0, rc4Key, 0, (int)length );

            //3.7 step 2
            if( encRevision == 2 )
            {
                rc4.setKey( rc4Key );
                rc4.write( o, result );
            }
            else if( encRevision == 3 || encRevision == 4)
            {
                byte[] iterationKey = new byte[ rc4Key.length ];
                byte[] otemp = new byte[ o.length ]; //sm
                System.arraycopy( o, 0, otemp, 0, o.length ); //sm
                rc4.write( o, result);//sm

                for( int i=19; i>=0; i-- )
                {
                    System.arraycopy( rc4Key, 0, iterationKey, 0, rc4Key.length );
                    for( int j=0; j< iterationKey.length; j++ )
                    {
                        iterationKey[j] = (byte)(iterationKey[j] ^ (byte)i);
                    }
                    rc4.setKey( iterationKey );
                    result.reset();  //sm
                    rc4.write( otemp, result ); //sm
                    otemp = result.toByteArray(); //sm
                }
            }
            return result.toByteArray();
        }
        catch( NoSuchAlgorithmException e )
        {
            throw new CryptographyException( e );
        }
    }

    /**
     * Compute the encryption key.
     *
     * @param password The password to compute the encrypted key.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The permissions for the document.
     * @param id The document id.
     * @param encRevision The revision of the encryption algorithm.
     * @param length The length of the encryption key.
     * @param encryptMetadata The encryption metadata
     *
     * @return The encrypted key bytes.
     *
     * @throws CryptographyException If there is an error with encryption.
     */
    public final byte[] computeEncryptedKey(
            byte[] password,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata)
            throws CryptographyException
        {
            byte[] result = new byte[ length ];
            try
            {
                //PDFReference 1.4 pg 78
                //step1
                byte[] padded = truncateOrPad( password );

                //step 2
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update( padded );

                //step 3
                md.update( o );

                //step 4
                byte zero = (byte)(permissions >>> 0);
                byte one = (byte)(permissions >>> 8);
                byte two = (byte)(permissions >>> 16);
                byte three = (byte)(permissions >>> 24);

                md.update( zero );
                md.update( one );
                md.update( two );
                md.update( three );

                //step 5
                md.update( id );
                
                //(Security handlers of revision 4 or greater) If document metadata is not being encrypted, 
                //pass 4 bytes with the value 0xFFFFFFFF to the MD5 hash function.
                //see 7.6.3.3 Algorithm 2 Step f of PDF 32000-1:2008
                if( encRevision == 4 && !encryptMetadata)
                {
                    md.update(new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff});
                }
                
                byte[] digest = md.digest();

                //step 6
                if( encRevision == 3 || encRevision == 4)
                {
                    for( int i=0; i<50; i++ )
                    {
                        md.reset();
                        md.update( digest, 0, length );
                        digest = md.digest();
                    }
                }

                //step 7
                if( encRevision == 2 && length != 5 )
                {
                    throw new CryptographyException(
                        "Error: length should be 5 when revision is two actual=" + length );
                }
                System.arraycopy( digest, 0, result, 0, length );
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new CryptographyException( e );
            }
            return result;
        }

    /**
     * This will compute the user password hash.
     *
     * @param password The plain text password.
     * @param o The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param encRevision The revision of the encryption.
     * @param length The length of the encryption key.
     * @param encryptMetadata The encryption metadata
     *
     * @return The user password.
     *
     * @throws CryptographyException If there is an error computing the user password.
     * @throws IOException If there is an IO error.
     */

    public final byte[] computeUserPassword(
            byte[] password,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata)
            throws CryptographyException, IOException
        {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            //STEP 1
            byte[] encryptionKey = computeEncryptedKey( password, o, permissions, id, encRevision, length, encryptMetadata );

            if( encRevision == 2 )
            {
                //STEP 2
                rc4.setKey( encryptionKey );
                rc4.write( ENCRYPT_PADDING, result );
            }
            else if( encRevision == 3 || encRevision == 4 )
            {
                try
                {
                    //STEP 2
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    //md.update( truncateOrPad( password ) );
                    md.update( ENCRYPT_PADDING );

                    //STEP 3
                    md.update( id );
                    result.write( md.digest() );

                    //STEP 4 and 5
                    byte[] iterationKey = new byte[ encryptionKey.length ];
                    for( int i=0; i<20; i++ )
                    {
                        System.arraycopy( encryptionKey, 0, iterationKey, 0, iterationKey.length );
                        for( int j=0; j< iterationKey.length; j++ )
                        {
                            iterationKey[j] = (byte)(iterationKey[j] ^ i);
                        }
                        rc4.setKey( iterationKey );
                        ByteArrayInputStream input = new ByteArrayInputStream( result.toByteArray() );
                        result.reset();
                        rc4.write( input, result );
                    }

                    //step 6
                    byte[] finalResult = new byte[32];
                    System.arraycopy( result.toByteArray(), 0, finalResult, 0, 16 );
                    System.arraycopy( ENCRYPT_PADDING, 0, finalResult, 16, 16 );
                    result.reset();
                    result.write( finalResult );
                }
                catch( NoSuchAlgorithmException e )
                {
                    throw new CryptographyException( e );
                }
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
     * @throws CryptographyException If there is an error with encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final byte[] computeOwnerPassword(
            byte[] ownerPassword,
            byte[] userPassword,
            int encRevision,
            int length )
            throws CryptographyException, IOException
        {
            try
            {
                //STEP 1
                byte[] ownerPadded = truncateOrPad( ownerPassword );

                //STEP 2
                MessageDigest md = MessageDigest.getInstance( "MD5" );
                md.update( ownerPadded );
                byte[] digest = md.digest();

                //STEP 3
                if( encRevision == 3 || encRevision == 4)
                {
                    for( int i=0; i<50; i++ )
                    {
                        md.reset();
                        md.update( digest, 0, length );
                        digest = md.digest();
                    }
                }
                if( encRevision == 2 && length != 5 )
                {
                    throw new CryptographyException(
                        "Error: Expected length=5 actual=" + length );
                }

                //STEP 4
                byte[] rc4Key = new byte[ length ];
                System.arraycopy( digest, 0, rc4Key, 0, length );

                //STEP 5
                byte[] paddedUser = truncateOrPad( userPassword );


                //STEP 6
                rc4.setKey( rc4Key );
                ByteArrayOutputStream crypted = new ByteArrayOutputStream();
                rc4.write( new ByteArrayInputStream( paddedUser ), crypted );


                //STEP 7
                if( encRevision == 3 || encRevision == 4 )
                {
                    byte[] iterationKey = new byte[ rc4Key.length ];
                    for( int i=1; i<20; i++ )
                    {
                        System.arraycopy( rc4Key, 0, iterationKey, 0, rc4Key.length );
                        for( int j=0; j< iterationKey.length; j++ )
                        {
                            iterationKey[j] = (byte)(iterationKey[j] ^ (byte)i);
                        }
                        rc4.setKey( iterationKey );
                        ByteArrayInputStream input = new ByteArrayInputStream( crypted.toByteArray() );
                        crypted.reset();
                        rc4.write( input, crypted );
                    }
                }

                //STEP 8
                return crypted.toByteArray();
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new CryptographyException( e.getMessage() );
            }
        }


    /**
     * This will take the password and truncate or pad it as necessary.
     *
     * @param password The password to pad or truncate.
     *
     * @return The padded or truncated password.
     */
    private final byte[] truncateOrPad( byte[] password )
    {
        byte[] padded = new byte[ ENCRYPT_PADDING.length ];
        int bytesBeforePad = Math.min( password.length, padded.length );
        System.arraycopy( password, 0, padded, 0, bytesBeforePad );
        System.arraycopy( ENCRYPT_PADDING, 0, padded, bytesBeforePad, ENCRYPT_PADDING.length-bytesBeforePad );
        return padded;
    }

    /**
     * Check if a plaintext password is the user password.
     *
     * @param password The plaintext password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The permissions set in the the PDF.
     * @param id The document id used for encryption.
     * @param encRevision The revision of the encryption algorithm.
     * @param length The length of the encryption key.
     * @param encryptMetadata The encryption metadata
     *
     * @return true If the plaintext password is the user password.
     *
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isUserPassword(
            byte[] password,
            byte[] u,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata)
            throws CryptographyException, IOException
        {
            boolean matches = false;
            //STEP 1
            byte[] computedValue = computeUserPassword( password, o, permissions, id, encRevision, length, 
                    encryptMetadata );
            if( encRevision == 2 )
            {
                //STEP 2
                matches = Arrays.equals(u, computedValue);
            }
            else if( encRevision == 3 || encRevision == 4 )
            {
                //STEP 2
                matches = arraysEqual( u, computedValue, 16 );
            }
            else
            {
                throw new IOException( "Unknown Encryption Revision " + encRevision );
            }
            return matches;
        }
    
    /**
     * Check if a plaintext password is the user password.
     *
     * @param password The plaintext password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The permissions set in the the PDF.
     * @param id The document id used for encryption.
     * @param encRevision The revision of the encryption algorithm.
     * @param length The length of the encryption key.
     * @param encryptMetadata The encryption metadata
     *
     * @return true If the plaintext password is the user password.
     *
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isUserPassword(
            String password,
            byte[] u,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata )
            throws CryptographyException, IOException
            {
                return isUserPassword(password.getBytes("ISO-8859-1"),
                        u,o,permissions, id, encRevision, length, encryptMetadata);
            }

    /**
     * Check for owner password.
     *
     * @param password The owner password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The set of permissions on the document.
     * @param id The document id.
     * @param encRevision The encryption algorithm revision.
     * @param length The encryption key length.
     * @param encryptMetadata The encryption metadata
     *
     * @return True If the ownerPassword param is the owner password.
     *
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isOwnerPassword(
            String password,
            byte[] u,
            byte[] o,
            int permissions,
            byte[] id,
            int encRevision,
            int length,
            boolean encryptMetadata)
            throws CryptographyException, IOException
            {
                return isOwnerPassword(password.getBytes("ISO-8859-1"),
                        u,o,permissions, id, encRevision, length, encryptMetadata);
            }

    private static final boolean arraysEqual( byte[] first, byte[] second, int count )
    {
        // both arrays have to have a minimum length of count
        if (first.length < count || second.length < count)
        {
            return false;
        }
        for( int i=0; i<count; i++ )
        {
            if( first[i] != second[i])
            {
                return false;
            }
        }
        return true;
    }

}
