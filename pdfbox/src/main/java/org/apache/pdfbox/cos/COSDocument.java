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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.NonSequentialPDFParser;
import org.apache.pdfbox.pdfparser.PDFObjectStreamParser;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class COSDocument extends COSBase implements Closeable
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(COSDocument.class);

    private float version = 1.4f;

    /**
     * Maps ObjectKeys to a COSObject. Note that references to these objects
     * are also stored in COSDictionary objects that map a name to a specific object.
     */
    private final Map<COSObjectKey, COSObject> objectPool =
        new HashMap<COSObjectKey, COSObject>();

    /**
     * Maps object and generation id to object byte offsets.
     */
    private final Map<COSObjectKey, Long> xrefTable =
        new HashMap<COSObjectKey, Long>();

    /**
     * Document trailer dictionary.
     */
    private COSDictionary trailer;
    
    /**
     * Signature interface.
     */
    private SignatureInterface signatureInterface;

    /**
     * This file will store the streams in order to conserve memory.
     */
    private final RandomAccess scratchFile;

    private final File tmpFile;

    private String headerString = "%PDF-" + version;

    private boolean warnMissingClose = true;
    
    /** signal that document is already decrypted, e.g. with {@link NonSequentialPDFParser} */
    private boolean isDecrypted = false;
    
    private long startXref;
    
    private boolean closed = false;

    /**
     * Flag to skip malformed or otherwise unparseable input where possible.
     */
    private final boolean forceParsing;

    /**
     * Constructor that will use the given random access file for storage
     * of the PDF streams. The client of this method is responsible for
     * deleting the storage if necessary that this file will write to. The
     * close method will close the file though.
     *
     * @param scratchFileValue the random access file to use for storage
     * @param forceParsingValue flag to skip malformed or otherwise unparseable
     *                     document content where possible
     */
    public COSDocument(RandomAccess scratchFileValue, boolean forceParsingValue) 
    {
        scratchFile = scratchFileValue;
        tmpFile = null;
        forceParsing = forceParsingValue;
    }

    /**
     * Constructor that will use a temporary file in the given directory
     * for storage of the PDF streams. The temporary file is automatically
     * removed when this document gets closed.
     *
     * @param scratchDir directory for the temporary file,
     *                   or <code>null</code> to use the system default
     * @param forceParsingValue flag to skip malformed or otherwise unparseable
     *                     document content where possible
     * @throws IOException if something went wrong
     */
    public COSDocument(File scratchDir, boolean forceParsingValue) throws IOException 
    {
        tmpFile = File.createTempFile("pdfbox-", ".tmp", scratchDir);
        scratchFile = new RandomAccessFile(tmpFile, "rw");
        forceParsing = forceParsingValue;
    }

    /**
     * Constructor.  Uses memory to store stream.
     *
     *  @throws IOException If there is an error creating the tmp file.
     */
    public COSDocument() throws IOException 
    {
        this(new RandomAccessBuffer(), false);
    }

    /**
     * Constructor that will create a create a scratch file in the
     * following directory.
     *
     * @param scratchDir The directory to store a scratch file.
     *
     * @throws IOException If there is an error creating the tmp file.
     */
    public COSDocument(File scratchDir) throws IOException 
    {
        this(scratchDir, false);
    }

    /**
     * Constructor that will use the following random access file for storage
     * of the PDF streams.  The client of this method is responsible for deleting
     * the storage if necessary that this file will write to.  The close method
     * will close the file though.
     *
     * @param file The random access file to use for storage.
     */
    public COSDocument(RandomAccess file) 
    {
        this(file, false);
    }

    /**
     * This will get the scratch file for this document.
     *
     * @return The scratch file.
     * 
     * 
     */
    public RandomAccess getScratchFile()
    {
        // TODO the direct access to the scratch file should be removed.
        if (!closed)
        {
            return scratchFile;
        }
        else
        {
            LOG.error("Can't access the scratch file as it is already closed!");
            return null;
        }
    }

    /**
     * Create a new COSStream using the underlying scratch file.
     * 
     * @return the new COSStream
     */
    public COSStream createCOSStream()
    {
        return new COSStream( getScratchFile() );
    }

    /**
     * Create a new COSStream using the underlying scratch file.
     *
     * @param dictionary the corresponding dictionary
     * 
     * @return the new COSStream
     */
    public COSStream createCOSStream(COSDictionary dictionary)
    {
        return new COSStream( dictionary, getScratchFile() );
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
        for( COSObject object : objectPool.values() )
        {

            COSBase realObject = object.getObject();
            if( realObject instanceof COSDictionary )
            {
                try
                {
                    COSDictionary dic = (COSDictionary)realObject;
                    COSName objectType = (COSName)dic.getItem( COSName.TYPE );
                    if( objectType != null && objectType.equals( type ) )
                    {
                        return object;
                    }
                }
                catch (ClassCastException e)
                {
                    LOG.warn(e, e);
                }
            }
        }
        return null;
    }

    /**
     * This will get all dictionary objects by type.
     *
     * @param type The type of the object.
     *
     * @return This will return an object with the specified type.
     * @throws IOException If there is an error getting the object
     */
    public List<COSObject> getObjectsByType( String type ) throws IOException
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
    public List<COSObject> getObjectsByType( COSName type ) throws IOException
    {
        List<COSObject> retval = new ArrayList<COSObject>();
        for( COSObject object : objectPool.values() )
        {
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
                    LOG.warn(e, e);
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
        for( COSObject object : objectPool.values() )
        {
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
        // update header string
        if (versionValue != version) 
        {
            headerString = headerString.replaceFirst(String.valueOf(version), String.valueOf(versionValue));
        }
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

    /** Signals that the document is decrypted completely.
     *  Needed e.g. by {@link NonSequentialPDFParser} to circumvent
     *  additional decryption later on. */
    public void setDecrypted()
    {
        isDecrypted = true;
    }

    /**
     * This will tell if this is an encrypted document.
     *
     * @return true If this document is encrypted.
     */
    public boolean isEncrypted()
    {
        if ( isDecrypted )
        {
            return false;
        }
        boolean encrypted = false;
        if( trailer != null )
        {
            encrypted = trailer.getDictionaryObject( COSName.ENCRYPT ) != null;
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
        return (COSDictionary)trailer.getDictionaryObject( COSName.ENCRYPT );
    }

    /**
     * This will return the signature interface.
     * @return the signature interface 
     */
    public SignatureInterface getSignatureInterface() 
    {
        return signatureInterface;
    }
    
    /**
     * This will set the encryption dictionary, this should only be called when
     * encrypting the document.
     *
     * @param encDictionary The encryption dictionary.
     */
    public void setEncryptionDictionary( COSDictionary encDictionary )
    {
        trailer.setItem( COSName.ENCRYPT, encDictionary );
    }

    /**
     * This will return a list of signature dictionaries as COSDictionary.
     *
     * @return list of signature dictionaries as COSDictionary
     * @throws IOException if no document catalog can be found
     */
    public List<COSDictionary> getSignatureDictionaries() throws IOException
    {
        List<COSDictionary> signatureFields = getSignatureFields(false);
        List<COSDictionary> signatures = new LinkedList<COSDictionary>();
        for ( COSDictionary dict : signatureFields )
        {
            COSBase dictionaryObject = dict.getDictionaryObject(COSName.V);
            if (dictionaryObject != null)
            {
                signatures.add((COSDictionary)dictionaryObject);
            }
        }
        return signatures;
    }

    /**
     * This will return a list of signature fields.
     *
     * @return list of signature dictionaries as COSDictionary
     * @throws IOException if no document catalog can be found
     */
    public List<COSDictionary> getSignatureFields(boolean onlyEmptyFields) throws IOException
    {
        COSObject documentCatalog = getCatalog();
        if (documentCatalog != null)
        {
            COSDictionary acroForm = (COSDictionary)documentCatalog.getDictionaryObject(COSName.ACRO_FORM);
            if (acroForm != null)
            {
                COSArray fields = (COSArray)acroForm.getDictionaryObject(COSName.FIELDS);
                if (fields != null)
                {
                    // Some fields may contain twice references to a single field. 
                    // This will prevent such double entries.
                    HashMap<COSObjectKey, COSDictionary> signatures = new HashMap<COSObjectKey, COSDictionary>();
                    for ( Object object : fields )
                    {
                        COSObject dict = (COSObject)object;
                        if (COSName.SIG.equals(dict.getItem(COSName.FT)))
                        {
                            COSBase dictionaryObject = dict.getDictionaryObject(COSName.V);
                            if (dictionaryObject == null || (dictionaryObject != null && !onlyEmptyFields))
                            {
                                signatures.put(new COSObjectKey(dict), (COSDictionary)dict.getObject());
                            }
                        }
                    }
                    return new LinkedList<COSDictionary>(signatures.values());
                }
            }
        }
        return Collections.emptyList();
    }
    
    /**
     * This will get the document ID.
     *
     * @return The document id.
     */
    public COSArray getDocumentID()
    {
        return (COSArray) getTrailer().getDictionaryObject(COSName.ID);
    }

    /**
     * This will set the document ID.
     *
     * @param id The document id.
     */
    public void setDocumentID( COSArray id )
    {
        getTrailer().setItem(COSName.ID, id);
    }
    
    /**
     * Set the signature interface to the given value.
     * @param sigInterface the signature interface
     */
    public void setSignatureInterface(SignatureInterface sigInterface) 
    {
        signatureInterface = sigInterface;
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
    public List<COSObject> getObjects()
    {
        return new ArrayList<COSObject>(objectPool.values());
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
    @Override
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
        if (!closed) 
        {
            scratchFile.close();
            if (tmpFile != null) 
            {
                tmpFile.delete();
            }
            if (trailer != null)
            {
            	trailer.clear();
            	trailer = null;
            }
            // Clear object pool
            List<COSObject> list = getObjects();
            if (list != null && !list.isEmpty()) 
            {
                for (COSObject object : list) 
                {
                    COSBase cosObject = object.getObject();
                    // clear the resources of the pooled objects
                    if (cosObject instanceof COSStream)
                    {
                    	((COSStream)cosObject).close();
                    }
                    else if (cosObject instanceof COSDictionary)
                    {
                    	((COSDictionary)cosObject).clear();
                    }
                    else if (cosObject instanceof COSArray)
                    {
                    	((COSArray)cosObject).clear();
                    }
                    // TODO are there other kind of COSObjects to be cleared?
                }
                list.clear();
            }
            closed = true;
        }
    }

    /**
     * Warn the user in the finalizer if he didn't close the PDF document. The method also
     * closes the document just in case, to avoid abandoned temporary files. It's still a good
     * idea for the user to close the PDF document at the earliest possible to conserve resources.
     * @throws IOException if an error occurs while closing the temporary files
     */
    @Override
    protected void finalize() throws IOException
    {
        if (!closed) 
        {
            if (warnMissingClose) 
            {
                LOG.warn( "Warning: You did not close a PDF Document" );
            }
            close();
        }
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
        for( COSObject objStream : getObjectsByType( COSName.OBJ_STM ) )
        {
            COSStream stream = (COSStream)objStream.getObject();
            PDFObjectStreamParser parser =
                new PDFObjectStreamParser(stream, this, forceParsing);
            parser.parse();
            for( COSObject next : parser.getObjects() )
            {
                COSObjectKey key = new COSObjectKey( next );
                if ( objectPool.get(key) == null || objectPool.get(key).getObject() == null ||
                     // xrefTable stores negated objNr of objStream for objects in objStreams
                     (xrefTable.containsKey(key) && xrefTable.get(key) == -objStream.getObjectNumber().longValue()) )
                {
                    COSObject obj = getObjectFromPool(key);
                    obj.setObject(next.getObject());
                }
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
            obj = objectPool.get(key);
        }
        if (obj == null)
        {
            // this was a forward reference, make "proxy" object
            obj = new COSObject(null);
            if( key != null )
            {
                obj.setObjectNumber( COSInteger.get( key.getNumber() ) );
                obj.setGenerationNumber( COSInteger.get( key.getGeneration() ) );
                objectPool.put(key, obj);
            }
        }
        return obj;
    }

    /**
     * Removes an object from the object pool.
     * @param key the object key
     * @return the object that was removed or null if the object was not found
     */
    public COSObject removeObject(COSObjectKey key)
    {
        return objectPool.remove(key);
    }

    /**
     * Populate XRef HashMap with given values.
     * Each entry maps ObjectKeys to byte offsets in the file.
     * @param xrefTableValues  xref table entries to be added
     */
    public void addXRefTable( Map<COSObjectKey, Long> xrefTableValues )
    {
        xrefTable.putAll( xrefTableValues );
    }

    /**
     * Returns the xrefTable which is a mapping of ObjectKeys
     * to byte offsets in the file.
     * @return mapping of ObjectsKeys to byte offsets
     */
    public Map<COSObjectKey, Long> getXrefTable()
    {
        return xrefTable;
    }

    /**
     * This method set the startxref value of the document. This will only 
     * be needed for incremental updates.
     * 
     * @param startXrefValue the value for startXref
     */
    public void setStartXref(long startXrefValue)
    {
        startXref = startXrefValue;
    }

    /**
     * Return the startXref Position of the parsed document. This will only be needed for incremental updates.
     * 
     * @return a long with the old position of the startxref
     */
    public long getStartXref()
    {
      return startXref;
    }

    /**
     * Determines it the trailer is a XRef stream or not.
     * 
     * @return true if the trailer is a XRef stream
     */
    public boolean isXRefStream()
    {
        if (trailer != null)
        {
            return COSName.XREF.equals(trailer.getItem(COSName.TYPE));
        }
        return false;
    }
}
