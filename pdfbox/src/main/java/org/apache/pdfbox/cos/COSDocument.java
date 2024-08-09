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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessStreamCache;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author Ben Litchfield
 * 
 */
public class COSDocument extends COSBase implements Closeable
{

    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(COSDocument.class);
    
    private float version = 1.4f;

    /**
     * Maps ObjectKeys to a COSObject. Note that references to these objects
     * are also stored in COSDictionary objects that map a name to a specific object.
     */
    private final Map<COSObjectKey, COSObject> objectPool =
        new HashMap<>();

    /**
     * Maps object and generation id to object byte offsets.
     */
    private final Map<COSObjectKey, Long> xrefTable =
        new HashMap<>();

    /**
     * List containing all streams which are created when creating a new pdf.
     */
    private final List<COSStream> streams = new ArrayList<>();
    
    /**
     * Document trailer dictionary.
     */
    private COSDictionary trailer;
    
    /**
     * Signal that document is already decrypted.
     */
    private boolean isDecrypted = false;
    
    private long startXref;
    
    private boolean closed = false;

    private boolean isXRefStream;

    private boolean hasHybridXRef = false;

    private final RandomAccessStreamCache streamCache;

    /**
     * Used for incremental saving, to avoid XRef object numbers from being reused.
     */
    private long highestXRefObjectNumber;

    private final ICOSParser parser;
    
    private final COSDocumentState documentState = new COSDocumentState();

    /**
     * Constructor. Uses main memory to buffer PDF streams.
     */
    public COSDocument()
    {
        this(IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Constructor. Uses main memory to buffer PDF streams.
     * 
     * @param parser Parser to be used to parse the document on demand
     */
    public COSDocument(ICOSParser parser)
    {
        this(IOUtils.createMemoryOnlyStreamCache(), parser);
    }

    /**
     * Constructor that will use the provided function to create a stream cache for the storage of the PDF streams.
     *
     * @param streamCacheCreateFunction a function to create an instance of a stream cache
     * 
     */
    public COSDocument(StreamCacheCreateFunction streamCacheCreateFunction)
    {
        this(streamCacheCreateFunction, null);
    }

    /**
     * Constructor that will use the provided function to create a stream cache for the storage of the PDF streams.
     *
     * @param streamCacheCreateFunction a function to create an instance of a stream cache
     * @param parser Parser to be used to parse the document on demand
     * 
     */
    public COSDocument(StreamCacheCreateFunction streamCacheCreateFunction, ICOSParser parser)
    {
        streamCache = getStreamCache(streamCacheCreateFunction);
        this.parser = parser;
    }

    private RandomAccessStreamCache getStreamCache(StreamCacheCreateFunction streamCacheCreateFunction)
    {
        if (streamCacheCreateFunction == null)
        {
            return null;
        }
        try
        {
            return streamCacheCreateFunction.create();
        }
        catch (IOException exception1)
        {
            LOG.warn(
                    "An error occured when creating stream cache. Using memory only cache as fallback.",
                    exception1);
        }
        try
        {
            return IOUtils.createMemoryOnlyStreamCache().create();
        }
        catch (IOException exception2)
        {
            LOG.warn("An error occured when creating stream cache for fallback.", exception2);
        }
        return null;
    }

    /**
     * Creates a new COSStream using the current configuration for scratch files.
     * 
     * @return the new COSStream
     */
    public COSStream createCOSStream()
    {
        COSStream stream = new COSStream(streamCache);
        // collect all COSStreams so that they can be closed when closing the COSDocument.
        // This is limited to newly created pdfs as all COSStreams of an existing pdf are
        // collected within the map objectPool
        streams.add(stream);
        return stream;
    }

    /**
     * Creates a new COSStream using the current configuration for scratch files. Not for public use.
     * Only COSParser should call this method.
     * 
     * @param dictionary    the corresponding dictionary
     * @param startPosition the start position within the source
     * @param streamLength  the stream length
     * @return the new COSStream
     * @throws IOException if the random access view can't be read
     */
    public COSStream createCOSStream(COSDictionary dictionary, long startPosition,
            long streamLength) throws IOException
    {
        COSStream stream = new COSStream(streamCache,
                parser.createRandomAccessReadView(startPosition, streamLength));
        dictionary.forEach(stream::setItem);
        stream.setKey(dictionary.getKey());
        return stream;
    }

    /**
     * Get the dictionary containing the linearization information if the pdf is linearized.
     * 
     * @return the dictionary containing the linearization information
     */
    public COSDictionary getLinearizedDictionary()
    {
        // get all keys with a positive offset in ascending order, as the linearization dictionary shall be the first
        // within the pdf
        List<COSObjectKey> objectKeys = xrefTable.entrySet().stream() //
                .filter(e -> e.getValue() > 0L) //
                .sorted(Entry.comparingByValue()) //
                .map(Entry::getKey) //
                .collect(Collectors.toList());
        for (COSObjectKey objectKey : objectKeys)
        {
            COSObject objectFromPool = getObjectFromPool(objectKey);
            COSBase realObject = objectFromPool.getObject();
            if (realObject instanceof COSDictionary)
            {
                COSDictionary dic = (COSDictionary) realObject;
                if (dic.getItem(COSName.LINEARIZED) != null)
                {
                    return dic;
                }
            }
        }
        return null;
    }

    /**
     * This will get all dictionaries objects by type.
     *
     * @param type The type of the object.
     *
     * @return This will return all objects with the specified type.
     */
    public List<COSObject> getObjectsByType(COSName type)
    {
        return getObjectsByType(type, null);
    }

    /**
     * This will get all dictionaries objects by type.
     *
     * @param type1 The first possible type of the object, mandatory.
     * @param type2 The second possible type of the object, usually an abbreviation, optional.
     *
     * @return This will return all objects with the specified type(s).
     */
    public List<COSObject> getObjectsByType(COSName type1, COSName type2)
    {
        List<COSObjectKey> originKeys = new ArrayList<>(xrefTable.keySet());
        List<COSObject> retval = getObjectsByType(originKeys, type1, type2);
        // there might be some additional objects if the brute force parser was triggered
        // due to a broken cross reference table/stream
        if (originKeys.size() < xrefTable.size())
        {
            List<COSObjectKey> additionalKeys = new ArrayList<>(xrefTable.keySet());
            additionalKeys.removeAll(originKeys);
            retval.addAll(getObjectsByType(additionalKeys, type1, type2));
        }
        return retval;
    }

    private List<COSObject> getObjectsByType(List<COSObjectKey> keys, COSName type1, COSName type2)
    {
        List<COSObject> retval = new ArrayList<>();
        for (COSObjectKey objectKey : keys)
        {
            COSObject objectFromPool = getObjectFromPool(objectKey);
            COSBase realObject = objectFromPool.getObject();
            if (realObject instanceof COSDictionary)
            {
                COSName dictType = ((COSDictionary) realObject).getCOSName(COSName.TYPE);
                if (type1.equals(dictType) || (type2 != null && type2.equals(dictType)))
                {
                    retval.add(objectFromPool);
                }
            }
        }
        return retval;
    }

    /**
     * This will set the header version of this PDF document.
     *
     * @param versionValue The version of the PDF document.
     */
    public void setVersion( float versionValue )
    {
        version = versionValue;
    }

    /**
     * This will get the version extracted from the header of this PDF document.
     *
     * @return The header version.
     */
    public float getVersion()
    {
        return version;
    }

    /** 
     * Signals that the document is decrypted completely.
     */
    public void setDecrypted()
    {
        isDecrypted = true;
    }

    /** 
     * Indicates if a encrypted pdf is already decrypted after parsing.
     * 
     *  @return true indicates that the pdf is decrypted.
     */
    public boolean isDecrypted()
    {
        return isDecrypted;
    }
    
    /**
     * This will tell if this is an encrypted document.
     *
     * @return true If this document is encrypted.
     */
    public boolean isEncrypted()
    {
        return trailer != null && trailer.getCOSDictionary(COSName.ENCRYPT) != null;
    }

    /**
     * This will get the encryption dictionary if the document is encrypted or null if the document
     * is not encrypted.
     *
     * @return The encryption dictionary.
     */
    public COSDictionary getEncryptionDictionary()
    {
        return trailer.getCOSDictionary(COSName.ENCRYPT);
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
     * This will get the document ID.
     *
     * @return The document id.
     */
    public COSArray getDocumentID()
    {
        return getTrailer().getCOSArray(COSName.ID);
    }

    /**
     * This will set the document ID. This should be an array of two strings. This method cannot be
     * used to remove the document id by passing null or an empty array; it will be recreated. Only
     * the first existing string is used when writing, the second one is always recreated. If you
     * don't want this, you'll have to modify the {@code COSWriter} class, look for {@link COSName#ID}.
     *
     * @param id The document id.
     */
    public void setDocumentID( COSArray id )
    {
        getTrailer().setItem(COSName.ID, id);
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
        trailer.getUpdateState().setOriginDocumentState(documentState);
    }

    /**
     * Internal PDFBox use only. Get the object number of the highest XRef stream. This is needed to
     * avoid reusing such a number in incremental saving.
     *
     * @return The object number of the highest XRef stream, or 0 if there was no XRef stream.
     */
    public long getHighestXRefObjectNumber()
    {
        return highestXRefObjectNumber;
    }

    /**
     * Internal PDFBox use only. Sets the object number of the highest XRef stream. This is needed
     * to avoid reusing such a number in incremental saving.
     *
     * @param highestXRefObjectNumber The object number of the highest XRef stream.
     */
    public void setHighestXRefObjectNumber(long highestXRefObjectNumber)
    {
        this.highestXRefObjectNumber = highestXRefObjectNumber;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromDocument(this);
    }

    /**
     * This will close all storage and delete the tmp files.
     *
     * @throws IOException If there is an error close resources.
     */
    @Override
    public void close() throws IOException
    {
        if (closed)
        {
            return;
        }

        // Make sure that:
        // - first Exception is kept
        // - all COSStreams are closed
        // - stream cache is closed
        // - there's a way to see which errors occurred
        IOException firstException = null;

        // close all open I/O streams
        for (COSObject object : objectPool.values())
        {
            if (!object.isObjectNull())
            {
                COSBase cosObject = object.getObject();
                if (cosObject instanceof COSStream)
                {
                    firstException = IOUtils.closeAndLogException((COSStream) cosObject, LOG,
                            "COSStream", firstException);
                }
            }
        }

        for (COSStream stream : streams)
        {
            firstException = IOUtils.closeAndLogException(stream, LOG, "COSStream", firstException);
        }

        if (streamCache != null)
        {
            firstException = IOUtils.closeAndLogException(streamCache, LOG, "Stream Cache",
                    firstException);
        }
        closed = true;

        // rethrow first exception to keep method contract
        if (firstException != null)
        {
            throw firstException;
        }
    }

    /**
     * Returns true if this document has been closed.
     * 
     * @return true if the document is already closed, false otherwise
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * This will get an object from the pool.
     *
     * @param key The object key.
     *
     * @return The object in the pool or a new one if it has not been parsed yet.
     */
    public COSObject getObjectFromPool(COSObjectKey key)
    {
        COSObject obj = null;
        if( key != null )
        {
            // make "proxy" object if this was a forward reference
            obj = objectPool.computeIfAbsent(key, k -> new COSObject(k, parser));
        }
        return obj;
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
     * Determines if the trailer is a XRef stream or not.
     * 
     * @return true if the trailer is a XRef stream
     */
    public boolean isXRefStream()
    {
        return isXRefStream;
    }
    
    /**
     * Sets isXRefStream to the given value. You need to take care that the version of your PDF is
     * 1.5 or higher.
     *
     * @param isXRefStreamValue the new value for isXRefStream
     */
    public void setIsXRefStream(boolean isXRefStreamValue)
    {
        isXRefStream = isXRefStreamValue;
    }
    
    /**
     * Determines if the pdf has hybrid cross references, both plain tables and streams.
     * 
     * @return true if the pdf has hybrid cross references
     */
    public boolean hasHybridXRef()
    {
        return hasHybridXRef;
    }

    /**
     * Marks the pdf as document using hybrid cross references.
     */
    public void setHasHybridXRef()
    {
        hasHybridXRef = true;
    }

    /**
     * Returns the {@link COSDocumentState} of this {@link COSDocument}.
     *
     * @return The {@link COSDocumentState} of this {@link COSDocument}.
     * @see COSDocumentState
     */
    public COSDocumentState getDocumentState()
    {
        return documentState;
    }
    
}
