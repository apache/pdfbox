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
package org.pdfbox.cos;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.io.RandomAccess;
import org.pdfbox.io.RandomAccessFile;

import org.pdfbox.pdfparser.PDFObjectStreamParser;
import org.pdfbox.persistence.util.COSObjectKey;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.28 $
 */
public class COSDocument extends COSBase
{
    private float version;

    /**
     * added objects (actually preserving original sequence).
     */
    private List objects = new ArrayList();

    /**
     * a pool of objects read/referenced so far
     * used to resolve indirect object references.
     */
    private Map objectPool = new HashMap();

    /**
     * Document trailer dictionary.
     */
    private COSDictionary trailer;

    /**
     * This file will store the streams in order to conserve memory.
     */
    private RandomAccess scratchFile = null;

    private File tmpFile = null;

    private String headerString = "%PDF-1.4";

    /**
     * Constructor.  Uses the java.io.tmpdir value to create a file
     * to store the streams.
     *
     *  @throws IOException If there is an error creating the tmp file.
     */
    public COSDocument() throws IOException
    {
        this( new File( System.getProperty( "java.io.tmpdir" ) ) );
    }

    /**
     * Constructor that will create a create a scratch file in the
     * following directory.
     *
     * @param scratchDir The directory to store a scratch file.
     *
     *  @throws IOException If there is an error creating the tmp file.
     */
    public COSDocument( File scratchDir ) throws IOException
    {
        tmpFile = File.createTempFile( "pdfbox", "tmp", scratchDir );
        scratchFile = new RandomAccessFile( tmpFile, "rw" );
    }

    /**
     * Constructor that will use the following random access file for storage
     * of the PDF streams.  The client of this method is responsible for deleting
     * the storage if necessary that this file will write to.  The close method
     * will close the file though.
     *
     * @param file The random access file to use for storage.
     */
    public COSDocument( RandomAccess file )
    {
        scratchFile = file;
    }

    /**
     * This will get the scratch file for this document.
     *
     * @return The scratch file.
     */
    public RandomAccess getScratchFile()
    {
        return scratchFile;
    }

    /**
     * This will get the first dictionary object by type.
     *
     * @param type The type of the object.
     *
     * @return This will return an object with the specified type.
     */
    public COSObject getObjectByType( String type ) throws IOException
    {
        return getObjectByType( COSName.getPDFName( type ) );
    }

    /**
     * This will get the first dictionary object by type.
     *
     * @param type The type of the object.
     *
     * @return This will return an object with the specified type.
     */
    public COSObject getObjectByType( COSName type ) throws IOException
    {
        COSObject retval = null;
        Iterator iter = objects.iterator();
        while( iter.hasNext() && retval == null)
        {
            COSObject object = (COSObject)iter.next();

            COSBase realObject = object.getObject();
            if( realObject instanceof COSDictionary )
            {
		try{
			COSDictionary dic = (COSDictionary)realObject;
			COSName objectType = (COSName)dic.getItem( COSName.TYPE );
			if( objectType != null && objectType.equals( type ) )
			{
			    retval = object;
			}
		}catch (ClassCastException e){
			logger().warning(e.toString() + "\n at\n" + FullStackTrace(e));
		}

            }
        }
        return retval;
    }

    /**
     * This will get all dictionary objects by type.
     *
     * @param type The type of the object.
     *
     * @return This will return an object with the specified type.
     */
    public List getObjectsByType( String type ) throws IOException
    {
        return getObjectsByType( COSName.getPDFName( type ) );
    }

    /**
     * This will get a dictionary object by type.
     *
     * @param type The type of the object.
     *
     * @return This will return an object with the specified type.
     */
    public List getObjectsByType( COSName type ) throws IOException
    {
        List retval = new ArrayList();
        Iterator iter = objects.iterator();
        while( iter.hasNext() )
        {
            COSObject object = (COSObject)iter.next();

            COSBase realObject = object.getObject();
            if( realObject instanceof COSDictionary )
            {
		try{
			COSDictionary dic = (COSDictionary)realObject;
			COSName objectType = (COSName)dic.getItem( COSName.TYPE );
			if( objectType != null && objectType.equals( type ) )
			{
			    retval.add( object );
			}
		}catch (ClassCastException e){
			logger().warning(e.toString() + "\n at\n" + FullStackTrace(e));
		}
            }
        }
        return retval;
    }

    /**
     * This will print contents to stdout.
     */
    public void print()
    {
        Iterator iter = objects.iterator();
        while( iter.hasNext() )
        {
            COSObject object = (COSObject)iter.next();
            System.out.println( object);
        }
    }

    /**
     * This will set the version of this PDF document.
     *
     * @param versionValue The version of the PDF document.
     */
    public void setVersion( float versionValue )
    {
        version = versionValue;
    }

    /**
     * This will get the version of this PDF document.
     *
     * @return This documents version.
     */
    public float getVersion()
    {
        return version;
    }

    /**
     * This will tell if this is an encrypted document.
     *
     * @return true If this document is encrypted.
     */
    public boolean isEncrypted()
    {
        boolean encrypted = false;
        if( trailer != null )
        {
            encrypted = trailer.getDictionaryObject( "Encrypt" ) != null;
        }
        return encrypted;
    }

    /**
     * This will get the encryption dictionary if the document is encrypted or null
     * if the document is not encrypted.
     *
     * @return The encryption dictionary.
     */
    public COSDictionary getEncryptionDictionary()
    {
        return (COSDictionary)trailer.getDictionaryObject( COSName.getPDFName( "Encrypt" ) );
    }

    /**
     * This will set the encryption dictionary, this should only be called when
     * encypting the document.
     *
     * @param encDictionary The encryption dictionary.
     */
    public void setEncryptionDictionary( COSDictionary encDictionary )
    {
        trailer.setItem( COSName.getPDFName( "Encrypt" ), encDictionary );
    }

    /**
     * This will get the document ID.
     *
     * @return The document id.
     */
    public COSArray getDocumentID()
    {
        return (COSArray) getTrailer().getItem(COSName.getPDFName("ID"));
    }

    /**
     * This will set the document ID.
     *
     * @param id The document id.
     */
    public void setDocumentID( COSArray id )
    {
        getTrailer().setItem(COSName.getPDFName("ID"), id);
    }

    /**
     * This will create an object for this document.
     *
     * Create an indirect object out of the direct type and include in the document
     * for later lookup via document a map from direct object to indirect object
     * is maintained. this provides better support for manual PDF construction.
     *
     * @param base the base object to wrap in an indirect object.
     *
     * @return The pdf object that wraps the base, or creates a new one.
     */
    /**
    public COSObject createObject( COSBase base )
    {
        COSObject obj = (COSObject)objectMap.get(base);
        if (obj == null)
        {
            obj = new COSObject( base );
            obj.addTo(this);
        }
        return obj;
    }**/

    /**
     * This will get the document catalog.
     *
     * Maybe this should move to an object at PDFEdit level
     *
     * @return catalog is the root of all document activities
     *
     * @throws IOException If no catalog can be found.
     */
    public COSObject getCatalog() throws IOException
    {
        COSObject catalog = getObjectByType( COSName.CATALOG );
        if( catalog == null )
        {
            throw new IOException( "Catalog cannot be found" );
        }
        return catalog;
    }

    /**
     * This will get a list of all available objects.
     *
     * @return A list of all objects.
     */
    public List getObjects()
    {
        return new ArrayList(objects);
    }

    /**
     * This will get the document trailer.
     *
     * @return the document trailer dict
     */
    public COSDictionary getTrailer()
    {
        return trailer;
    }

    /**
     * // MIT added, maybe this should not be supported as trailer is a persistence construct.
     * This will set the document trailer.
     *
     * @param newTrailer the document trailer dictionary
     */
    public void setTrailer(COSDictionary newTrailer)
    {
        trailer = newTrailer;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public Object accept(ICOSVisitor visitor) throws COSVisitorException
    {
        return visitor.visitFromDocument( this );
    }

    /**
     * This will close all storage and delete the tmp files.
     *
     *  @throws IOException If there is an error close resources.
     */
    public void close() throws IOException
    {
        if( scratchFile != null )
        {
            scratchFile.close();
            scratchFile = null;
        }
        if( tmpFile != null )
        {
            tmpFile.delete();
            tmpFile = null;
        }
    }

    /**
     * The sole purpose of this is to inform a client of PDFBox that they
     * did not close the document.
     */
    protected void finalize()
    {
        if( tmpFile != null || scratchFile != null )
        {
            Throwable t = new Throwable( "Warning: You did not close the PDF Document" );
            t.printStackTrace();
        }
    }
    /**
     * @return Returns the headerString.
     */
    public String getHeaderString()
    {
        return headerString;
    }
    /**
     * @param header The headerString to set.
     */
    public void setHeaderString(String header)
    {
        headerString = header;
    }

    /**
     * This method will search the list of objects for types of ObjStm.  If it finds
     * them then it will parse out all of the objects from the stream that is contains.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    public void dereferenceObjectStreams() throws IOException
    {
        Iterator objStm = getObjectsByType( "ObjStm" ).iterator();
        while( objStm.hasNext() )
        {
            COSObject objStream = (COSObject)objStm.next();
            COSStream stream = (COSStream)objStream.getObject();
            PDFObjectStreamParser parser = new PDFObjectStreamParser( stream, this );
            parser.parse();
            Iterator compressedObjects = parser.getObjects().iterator();
            while( compressedObjects.hasNext() )
            {
                COSObject next = (COSObject)compressedObjects.next();
                COSObjectKey key = new COSObjectKey( next );
                COSObject obj = getObjectFromPool( key );
                obj.setObject( next.getObject() );
            }
        }
    }

    /**
     * This will add an object to this document.
     * the method checks if obj is already present as there may be cyclic dependencies
     *
     * @param obj The object to add to the document.
     * @return The object that was actually added to this document, if an object reference already
     * existed then that will be returned.
     *
     * @throws IOException If there is an error adding the object.
     */
    public COSObject addObject(COSObject obj) throws IOException
    {
        COSObjectKey key = null;
        if( obj.getObjectNumber() != null )
        {
            key = new COSObjectKey( obj );
        }
        COSObject fromPool = getObjectFromPool( key );
        fromPool.setObject( obj.getObject() );
        return fromPool;
    }

    /**
     * This will get an object from the pool.
     *
     * @param key The object key.
     *
     * @return The object in the pool or a new one if it has not been parsed yet.
     *
     * @throws IOException If there is an error getting the proxy object.
     */
    public COSObject getObjectFromPool(COSObjectKey key) throws IOException
    {
        COSObject obj = null;
        if( key != null )
        {
            obj = (COSObject) objectPool.get(key);
        }
        if (obj == null)
        {
            // this was a forward reference, make "proxy" object
            obj = new COSObject(null);
            if( key != null )
            {
                obj.setObjectNumber( new COSInteger( key.getNumber() ) );
                obj.setGenerationNumber( new COSInteger( key.getGeneration() ) );
                objectPool.put(key, obj);
            }
            objects.add( obj );
        }

        return obj;
    }
}
