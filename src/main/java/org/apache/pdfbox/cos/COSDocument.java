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
package org.apache.pdfbox.cos;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;

import org.apache.pdfbox.pdfparser.PDFObjectStreamParser;
import org.apache.pdfbox.pdfparser.PDFXrefStreamParser;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.28 $
 */
public class COSDocument extends COSBase
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(COSDocument.class);

    private float version;

    /**
     * Maps ObjectKeys to a COSObject. Note that references to these objects
     * are also stored in COSDictionary objects that map a name to a specific object.
     */
    private Map objectPool = new HashMap();

    /**
     * Maps object and generation ids to object byte offsets.
     */
    private Map xrefTable = new HashMap();

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

    private boolean warnMissingClose = true;

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
     * @throws IOException If there is an error getting the object
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
     * @throws IOException If there is an error getting the object
     */
    public COSObject getObjectByType( COSName type ) throws IOException
    {
        COSObject retval = null;
        Iterator iter = objectPool.values().iterator();
        while( iter.hasNext() && retval == null)
        {
            COSObject object = (COSObject)iter.next();

            COSBase realObject = object.getObject();
            if( realObject instanceof COSDictionary )
            {
                try
                {
                    COSDictionary dic = (COSDictionary)realObject;
                    COSName objectType = (COSName)dic.getItem( COSName.TYPE );
                    if( objectType != null && objectType.equals( type ) )
                    {
                        retval = object;
                    }
                }
                catch (ClassCastException e)
                {
                    log.warn(e, e);
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
     * @throws IOException If there is an error getting the object
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
     * @throws IOException If there is an error getting the object
     */
    public List getObjectsByType( COSName type ) throws IOException
    {
        List retval = new ArrayList();
        Iterator iter = objectPool.values().iterator();
        while( iter.hasNext() )
        {
            COSObject object = (COSObject)iter.next();

            COSBase realObject = object.getObject();
            if( realObject instanceof COSDictionary )
            {
                try
                {
                    COSDictionary dic = (COSDictionary)realObject;
                    COSName objectType = (COSName)dic.getItem( COSName.TYPE );
                    if( objectType != null && objectType.equals( type ) )
                    {
                        retval.add( object );
                    }
                }
                catch (ClassCastException e)
                {
                    log.warn(e, e);
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
        Iterator iter = objectPool.values().iterator();
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
        return new ArrayList(objectPool.values());
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
     * Warn the user in the finalizer if he didn't close the PDF document. The method also
     * closes the document just in case, to avoid abandoned temporary files. It's still a good
     * idea for the user to close the PDF document at the earliest possible to conserve resources.
     * @throws IOException if an error occurs while closing the temporary files
     */
    protected void finalize() throws IOException
    {
        if( this.warnMissingClose && ( tmpFile != null || scratchFile != null ) )
        {
            Throwable t = new Throwable( "Warning: You did not close the PDF Document" );
            t.printStackTrace();
        }
        close();
    }

    /**
     * Controls whether this instance shall issue a warning if the PDF document wasn't closed
     * properly through a call to the {@link #close()} method. If the PDF document is held in
     * a cache governed by soft references it is impossible to reliably close the document
     * before the warning is raised. By default, the warning is enabled.
     * @param warn true enables the warning, false disables it.
     */
    public void setWarnMissingClose(boolean warn)
    {
        this.warnMissingClose = warn;
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
        }
        return obj;
    }

    /**
     * Used to populate the XRef HashMap. Will add an Xreftable entry
     * that maps ObjectKeys to byte offsets in the file.
     * @param objKey The objkey, with id and gen numbers
     * @param offset The byte offset in this file
     */
    public void setXRef(COSObjectKey objKey, int offset)
    {
        xrefTable.put(objKey, new Integer(offset));
    }

    /**
     * Returns the xrefTable which is a mapping of ObjectKeys
     * to byte offsets in the file.
     * @return mapping of ObjectsKeys to byte offsets
     */
    public Map getXrefTable()
    {
        return xrefTable;
    }

    /**
     * This method will search the list of objects for types of XRef and
     * uses the parsed data to populate the trailer information as well as
     * the xref Map.
     *
     * @throws IOException if there is an error parsing the stream
     */
    public void parseXrefStreams() throws IOException
    {
        COSDictionary trailerDict = new COSDictionary();
        Iterator xrefIter = getObjectsByType( "XRef" ).iterator();
        while( xrefIter.hasNext() )
        {
            COSObject xrefStream = (COSObject)xrefIter.next();
            COSStream stream = (COSStream)xrefStream.getObject();
            trailerDict.addAll(stream);
            PDFXrefStreamParser parser = new PDFXrefStreamParser(stream, this);
            parser.parse();
        }
        setTrailer( trailerDict );
    }

}
