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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.PDStandardEncryption;

/**
 * This class will deal with encrypting/decrypting a document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.13 $
 *
 * @deprecated use the new security API instead.
 *
 * @see org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler
 */
public class DocumentEncryption
{
    private PDDocument pdDocument = null;
    private COSDocument document = null;

    private byte[] encryptionKey = null;
    private PDFEncryption encryption = new PDFEncryption();

    private Set objects = new HashSet();

    /**
     * A set that contains potential signature dictionaries.  This is used
     * because the Contents entry of the signature is not encrypted.
     */
    private Set potentialSignatures = new HashSet();

    /**
     * Constructor.
     *
     * @param doc The document to decrypt.
     */
    public DocumentEncryption( PDDocument doc )
    {
        pdDocument = doc;
        document = doc.getDocument();
    }

    /**
     * Constructor.
     *
     * @param doc The document to decrypt.
     */
    public DocumentEncryption( COSDocument doc )
    {
        pdDocument = new PDDocument( doc );
        document = doc;
    }

    /**
     * This will encrypt the given document, given the owner password and user password.
     * The encryption method used is the standard filter.
     *
     * @throws CryptographyException If an error occurs during encryption.
     * @throws IOException If there is an error accessing the data.
     */
    public void initForEncryption()
        throws CryptographyException, IOException
    {
        String ownerPassword = pdDocument.getOwnerPasswordForEncryption();
        String userPassword = pdDocument.getUserPasswordForEncryption();
        if( ownerPassword == null )
        {
            ownerPassword = "";
        }
        if( userPassword == null )
        {
            userPassword = "";
        }
        PDStandardEncryption encParameters = (PDStandardEncryption)pdDocument.getEncryptionDictionary();
        int permissionInt = encParameters.getPermissions();
        int revision = encParameters.getRevision();
        int length = encParameters.getLength()/8;
        COSArray idArray = document.getDocumentID();

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
                md.update( document.toString().getBytes() );
                byte[] id = md.digest( this.toString().getBytes("ISO-8859-1") );
                COSString idString = new COSString();
                idString.append( id );
                idArray.add( idString );
                idArray.add( idString );
                document.setDocumentID( idArray );
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new CryptographyException( e );
            }

        }
        COSString id = (COSString)idArray.getObject( 0 );
        encryption = new PDFEncryption();

        byte[] o = encryption.computeOwnerPassword(
            ownerPassword.getBytes("ISO-8859-1"),
            userPassword.getBytes("ISO-8859-1"), revision, length);

        byte[] u = encryption.computeUserPassword(
            userPassword.getBytes("ISO-8859-1"),
            o, permissionInt, id.getBytes(), revision, length);

        encryptionKey = encryption.computeEncryptedKey(
            userPassword.getBytes("ISO-8859-1"), o, permissionInt, id.getBytes(), revision, length);

        encParameters.setOwnerKey( o );
        encParameters.setUserKey( u );

        document.setEncryptionDictionary( encParameters.getCOSDictionary() );
    }



    /**
     * This will decrypt the document.
     *
     * @param password The password for the document.
     *
     * @throws CryptographyException If there is an error decrypting the document.
     * @throws IOException If there is an error getting the stream data.
     * @throws InvalidPasswordException If the password is not a user or owner password.
     */
    public void decryptDocument( String password )
        throws CryptographyException, IOException, InvalidPasswordException
    {
        if( password == null )
        {
            password = "";
        }

        PDStandardEncryption encParameters = (PDStandardEncryption)pdDocument.getEncryptionDictionary();


        int permissions = encParameters.getPermissions();
        int revision = encParameters.getRevision();
        int length = encParameters.getLength()/8;

        COSString id = (COSString)document.getDocumentID().getObject( 0 );
        byte[] u = encParameters.getUserKey();
        byte[] o = encParameters.getOwnerKey();

        boolean isUserPassword =
            encryption.isUserPassword( password.getBytes("ISO-8859-1"), u,
                o, permissions, id.getBytes(), revision, length );
        boolean isOwnerPassword =
            encryption.isOwnerPassword( password.getBytes("ISO-8859-1"), u,
                o, permissions, id.getBytes(), revision, length );

        if( isUserPassword )
        {
            encryptionKey =
                encryption.computeEncryptedKey(
                    password.getBytes("ISO-8859-1"), o,
                    permissions, id.getBytes(), revision, length );
        }
        else if( isOwnerPassword )
        {
            byte[] computedUserPassword =
                encryption.getUserPassword(
                    password.getBytes("ISO-8859-1"),
                    o,
                    revision,
                    length );
            encryptionKey =
                encryption.computeEncryptedKey(
                    computedUserPassword, o,
                    permissions, id.getBytes(), revision, length );
        }
        else
        {
            throw new InvalidPasswordException( "Error: The supplied password does not match " +
                                                "either the owner or user password in the document." );
        }

        COSDictionary trailer = document.getTrailer();
        COSArray fields = (COSArray)trailer.getObjectFromPath( "Root/AcroForm/Fields" );

        //We need to collect all the signature dictionaries, for some
        //reason the 'Contents' entry of signatures is not really encrypted
        if( fields != null )
        {
            for( int i=0; i<fields.size(); i++ )
            {
                COSDictionary field = (COSDictionary)fields.getObject( i );
                addDictionaryAndSubDictionary( potentialSignatures, field );
            }
        }

        List allObjects = document.getObjects();
        Iterator objectIter = allObjects.iterator();
        while( objectIter.hasNext() )
        {
            decryptObject( (COSObject)objectIter.next() );
        }
        document.setEncryptionDictionary( null );
    }

    private void addDictionaryAndSubDictionary( Set set, COSDictionary dic )
    {
        set.add( dic );
        COSArray kids = (COSArray)dic.getDictionaryObject( COSName.KIDS );
        for( int i=0; kids != null && i<kids.size(); i++ )
        {
            addDictionaryAndSubDictionary( set, (COSDictionary)kids.getObject( i ) );
        }
        COSBase value = dic.getDictionaryObject( COSName.V );
        if( value instanceof COSDictionary )
        {
            addDictionaryAndSubDictionary( set, (COSDictionary)value );
        }
    }

    /**
     * This will decrypt an object in the document.
     *
     * @param object The object to decrypt.
     *
     * @throws CryptographyException If there is an error decrypting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    private void decryptObject( COSObject object )
        throws CryptographyException, IOException
    {
        long objNum = object.getObjectNumber().intValue();
        long genNum = object.getGenerationNumber().intValue();
        COSBase base = object.getObject();
        decrypt( base, objNum, genNum );
    }

    /**
     * This will dispatch to the correct method.
     *
     * @param obj The object to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation Number.
     *
     * @throws CryptographyException If there is an error decrypting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    public void decrypt( Object obj, long objNum, long genNum )
        throws CryptographyException, IOException
    {
        if( !objects.contains( obj ) )
        {
            objects.add( obj );

            if( obj instanceof COSString )
            {
                decryptString( (COSString)obj, objNum, genNum );
            }
            else if( obj instanceof COSStream )
            {
                decryptStream( (COSStream)obj, objNum, genNum );
            }
            else if( obj instanceof COSDictionary )
            {
                decryptDictionary( (COSDictionary)obj, objNum, genNum );
            }
            else if( obj instanceof COSArray )
            {
                decryptArray( (COSArray)obj, objNum, genNum );
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
     * @throws CryptographyException If there is an error getting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    private void decryptStream( COSStream stream, long objNum, long genNum )
        throws CryptographyException, IOException
    {
        decryptDictionary( stream, objNum, genNum );
        InputStream encryptedStream = stream.getFilteredStream();
        encryption.encryptData( objNum,
                                genNum,
                                encryptionKey,
                                encryptedStream,
                                stream.createFilteredStream() );
    }

    /**
     * This will decrypt a dictionary.
     *
     * @param dictionary The dictionary to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If there is an error decrypting the document.
     * @throws IOException If there is an error creating a new string.
     */
    private void decryptDictionary( COSDictionary dictionary, long objNum, long genNum )
        throws CryptographyException, IOException
    {
        for( Map.Entry<COSName, COSBase> entry : dictionary.entrySet() )
        {
            //if we are a signature dictionary and contain a Contents entry then
            //we don't decrypt it.
            if( !(entry.getKey().getName().equals( "Contents" ) &&
                  entry.getValue() instanceof COSString &&
                  potentialSignatures.contains( dictionary )))
            {
                decrypt( entry.getValue(), objNum, genNum );
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
     * @throws CryptographyException If an error occurs during decryption.
     * @throws IOException If an error occurs writing the new string.
     */
    private void decryptString( COSString string, long objNum, long genNum )
        throws CryptographyException, IOException
    {
        ByteArrayInputStream data = new ByteArrayInputStream( string.getBytes() );
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        encryption.encryptData( objNum,
                                genNum,
                                encryptionKey,
                                data,
                                buffer );
        string.reset();
        string.append( buffer.toByteArray() );
    }

    /**
     * This will decrypt an array.
     *
     * @param array The array to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If an error occurs during decryption.
     * @throws IOException If there is an error accessing the data.
     */
    private void decryptArray( COSArray array, long objNum, long genNum )
        throws CryptographyException, IOException
    {
        for( int i=0; i<array.size(); i++ )
        {
            decrypt( array.get( i ), objNum, genNum );
        }
    }
}
