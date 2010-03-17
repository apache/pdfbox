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
package org.apache.pdfbox.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.pdfbox.exceptions.CryptographyException;

/**
 * This class will deal with PDF encryption algorithms.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.15 $
 *
 * @deprecated use the new security layer instead
 *
 * @see org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler
 */
public final class PDFEncryption
{
    private ARCFour rc4 = new ARCFour();
    /**
     * The encryption padding defined in the PDF 1.4 Spec algorithm 3.2.
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
     * This will encrypt a piece of data.
     *
     * @param objectNumber The id for the object.
     * @param genNumber The generation id for the object.
     * @param key The key used to encrypt the data.
     * @param data The data to encrypt/decrypt.
     * @param output The stream to write to.
     *
     * @throws CryptographyException If there is an error encrypting the data.
     * @throws IOException If there is an io error.
     */
    public final void encryptData(
        long objectNumber,
        long genNumber,
        byte[] key,
        InputStream data,
        OutputStream output )
        throws CryptographyException, IOException
    {
        byte[] newKey = new byte[ key.length + 5 ];
        System.arraycopy( key, 0, newKey, 0, key.length );
        //PDF 1.4 reference pg 73
        //step 1
        //we have the reference

        //step 2
        newKey[newKey.length -5] = (byte)(objectNumber & 0xff);
        newKey[newKey.length -4] = (byte)((objectNumber >> 8) & 0xff);
        newKey[newKey.length -3] = (byte)((objectNumber >> 16) & 0xff);
        newKey[newKey.length -2] = (byte)(genNumber & 0xff);
        newKey[newKey.length -1] = (byte)((genNumber >> 8) & 0xff);


        //step 3
        byte[] digestedKey = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            digestedKey = md.digest( newKey );
        }
        catch( NoSuchAlgorithmException e )
        {
            throw new CryptographyException( e );
        }

        //step 4
        int length = Math.min( newKey.length, 16 );
        byte[] finalKey = new byte[ length ];
        System.arraycopy( digestedKey, 0, finalKey, 0, length );

        rc4.setKey( finalKey );
        rc4.write( data, output );
        output.flush();
    }

    /**
     * This will get the user password from the owner password and the documents o value.
     *
     * @param ownerPassword The plaintext owner password.
     * @param o The document's o entry.
     * @param revision The document revision number.
     * @param length The length of the encryption.
     *
     * @return The plaintext padded user password.
     *
     * @throws CryptographyException If there is an error getting the user password.
     * @throws IOException If there is an error reading data.
     */
    public final byte[] getUserPassword(
        byte[] ownerPassword,
        byte[] o,
        int revision,
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
            if( revision == 3 || revision == 4 )
            {
                for( int i=0; i<50; i++ )
                {
                    md.reset();
                    md.update( digest );
                    digest = md.digest();
                }
            }
            if( revision == 2 && length != 5 )
            {
                throw new CryptographyException(
                    "Error: Expected length=5 actual=" + length );
            }

            //3.3 STEP 4
            byte[] rc4Key = new byte[ (int)length ];
            System.arraycopy( digest, 0, rc4Key, 0, (int)length );

            //3.7 step 2
            if( revision == 2 )
            {
                rc4.setKey( rc4Key );
                rc4.write( o, result );
            }
            else if( revision == 3 || revision == 4)
            {
                /**
                byte[] iterationKey = new byte[ rc4Key.length ];
                byte[] dataToEncrypt = o;
                for( int i=19; i>=0; i-- )
                {
                    System.arraycopy( rc4Key, 0, iterationKey, 0, rc4Key.length );
                    for( int j=0; j< iterationKey.length; j++ )
                    {
                        iterationKey[j] = (byte)(iterationKey[j] ^ (byte)i);
                    }
                    rc4.setKey( iterationKey );
                    rc4.write( dataToEncrypt, result );
                    dataToEncrypt = result.toByteArray();
                    result.reset();
                }
                result.write( dataToEncrypt, 0, dataToEncrypt.length );
                */
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
     * This will tell if this is the owner password or not.
     *
     * @param ownerPassword The plaintext owner password.
     * @param u The U value from the PDF Document.
     * @param o The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param revision The revision of the encryption.
     * @param length The length of the encryption key.
     *
     * @return true if the owner password matches the one from the document.
     *
     * @throws CryptographyException If there is an error while executing crypt functions.
     * @throws IOException If there is an error while checking owner password.
     */
    public final boolean isOwnerPassword(
        byte[] ownerPassword,
        byte[] u,
        byte[] o,
        int permissions,
        byte[] id,
        int revision,
        int length)
        throws CryptographyException, IOException
    {
        byte[] userPassword = getUserPassword( ownerPassword, o, revision, length );
        return isUserPassword( userPassword, u, o, permissions, id, revision, length );
    }

    /**
     * This will tell if this is a valid user password.
     *
     * Algorithm 3.6 pg 80
     *
     * @param password The password to test.
     * @param u The U value from the PDF Document.
     * @param o The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param revision The revision of the encryption.
     * @param length The length of the encryption key.
     *
     * @return true If this is the correct user password.
     *
     * @throws CryptographyException If there is an error computing the value.
     * @throws IOException If there is an IO error while computing the owners password.
     */
    public final boolean isUserPassword(
        byte[] password,
        byte[] u,
        byte[] o,
        int permissions,
        byte[] id,
        int revision,
        int length)
        throws CryptographyException, IOException
    {
        boolean matches = false;
        //STEP 1
        byte[] computedValue = computeUserPassword( password, o, permissions, id, revision, length );
        if( revision == 2 )
        {
            //STEP 2
            matches = arraysEqual( u, computedValue );
        }
        else if( revision == 3 || revision == 4 )
        {
            //STEP 2
            matches = arraysEqual( u, computedValue, 16 );
        }
        return matches;
    }

    /**
     * This will compare two byte[] for equality for count number of bytes.
     *
     * @param first The first byte array.
     * @param second The second byte array.
     * @param count The number of bytes to compare.
     *
     * @return true If the arrays contain the exact same data.
     */
    private final boolean arraysEqual( byte[] first, byte[] second, int count )
    {
        boolean equal = first.length >= count && second.length >= count;
        for( int i=0; i<count && equal; i++ )
        {
            equal = first[i] == second[i];
        }
        return equal;
    }

    /**
     * This will compare two byte[] for equality.
     *
     * @param first The first byte array.
     * @param second The second byte array.
     *
     * @return true If the arrays contain the exact same data.
     */
    private final boolean arraysEqual( byte[] first, byte[] second )
    {
        boolean equal = first.length == second.length;
        for( int i=0; i<first.length && equal; i++ )
        {
            equal = first[i] == second[i];
        }
        return equal;
    }

    /**
     * This will compute the user password hash.
     *
     * @param password The plain text password.
     * @param o The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param revision The revision of the encryption.
     * @param length The length of the encryption key.
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
        int revision,
        int length )
        throws CryptographyException, IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        //STEP 1
        byte[] encryptionKey = computeEncryptedKey( password, o, permissions, id, revision, length );

        if( revision == 2 )
        {
            //STEP 2
            rc4.setKey( encryptionKey );
            rc4.write( ENCRYPT_PADDING, result );
        }
        else if( revision == 3 || revision == 4 )
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
     * This will compute the encrypted key.
     *
     * @param password The password used to compute the encrypted key.
     * @param o The owner password hash.
     * @param permissions The permissions for the document.
     * @param id The document id.
     * @param revision The security revision.
     * @param length The length of the encryption key.
     *
     * @return The encryption key.
     *
     * @throws CryptographyException If there is an error computing the key.
     */
    public final byte[] computeEncryptedKey(
        byte[] password,
        byte[] o,
        int permissions,
        byte[] id,
        int revision,
        int length )
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
            byte[] digest = md.digest();

            //step 6
            if( revision == 3 || revision == 4)
            {
                for( int i=0; i<50; i++ )
                {
                    md.reset();
                    md.update( digest, 0, length );
                    digest = md.digest();
                }
            }

            //step 7
            if( revision == 2 && length != 5 )
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
     * This algorithm is taked from PDF Reference 1.4 Algorithm 3.3 Page 79.
     *
     * @param ownerPassword The plain owner password.
     * @param userPassword The plain user password.
     * @param revision The version of the security.
     * @param length The length of the document.
     *
     * @return The computed owner password.
     *
     * @throws CryptographyException If there is an error computing O.
     * @throws IOException If there is an error computing O.
     */
    public final byte[] computeOwnerPassword(
        byte[] ownerPassword,
        byte[] userPassword,
        int revision,
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
            if( revision == 3 || revision == 4)
            {
                for( int i=0; i<50; i++ )
                {
                    md.reset();
                    md.update( digest, 0, length );
                    digest = md.digest();
                }
            }
            if( revision == 2 && length != 5 )
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
            if( revision == 3 || revision == 4 )
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
}
