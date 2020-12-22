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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.ScratchFile;

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
    private static final Log LOG = LogFactory.getLog(COSDocument.class);

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
    
    private boolean warnMissingClose = true;
    
    /** 
     * Signal that document is already decrypted. 
     */
    private boolean isDecrypted = false;
    
    private long startXref;
    
    private boolean closed = false;

    private boolean isXRefStream;

    private ScratchFile scratchFile;

    /**
     * Used for incremental saving, to avoid XRef object numbers from being reused.
     */
    private long highestXRefObjectNumber;

    private final ICOSParser parser;

    /**
     * Constructor. Uses main memory to buffer PDF streams.
     */
    public COSDocument()
    {
        this(MemoryUsageSetting.setupMainMemoryOnly());
    }

    /**
     * Constructor. Uses main memory to buffer PDF streams.
     * 
     * @param parser Parser to be used to parse the document on demand
     */
    public COSDocument(ICOSParser parser)
    {
        this(MemoryUsageSetting.setupMainMemoryOnly(), parser);
    }

    /**
     * Constructor that will use the provided memory settings for storage of the PDF streams.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * 
     */
    public COSDocument(MemoryUsageSetting memUsageSetting)
    {
        this(memUsageSetting, null);
    }

    /**
     * Constructor that will use the provided memory settings for storage of the PDF streams.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams
     * @param parser Parser to be used to parse the document on demand
     * 
     */
    public COSDocument(MemoryUsageSetting memUsageSetting, ICOSParser parser)
    {
        try
        {
            if (memUsageSetting != null)
            {
                scratchFile = new ScratchFile(memUsageSetting);
            }
            else
            {
                scratchFile = ScratchFile.getMainMemoryOnlyInstance();
            }
        }
        catch (IOException ioe)
        {
            LOG.warn("Error initializing scratch file: " + ioe.getMessage()
                    + ". Fall back to main memory usage only.", ioe);

            scratchFile = ScratchFile.getMainMemoryOnlyInstance();
        }
        this.parser = parser;
    }

    /**
     * Creates a new COSStream using the current configuration for scratch files.
     * 
     * @return the new COSStream
     */
    public COSStream createCOSStream()
    {
        COSStream stream = new COSStream(scratchFile);
        // collect all COSStreams so that they can be closed when closing the COSDocument.
        // This is limited to newly created pdfs as all COSStreams of an existing pdf are
        // collected within the map objectPool
        streams.add(stream);
        return stream;
    }

    /**
     * Creates a new COSStream using the current configuration for scratch files. Not for public use. Only COSParser should
     * call this method.
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
        COSStream stream = new COSStream(scratchFile,
                parser.createRandomAccessReadView(startPosition, streamLength));
        dictionary.forEach(stream::setItem);
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
                .sorted(Comparator.comparing(Entry::getValue)) //
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
     * @param type2 The second possible type of the object, usally an abreviation, optional.
     *
     * @return This will return all objects with the specified type(s).
     */
    public List<COSObject> getObjectsByType(COSName type1, COSName type2)
    {
        List<COSObject> retval = new ArrayList<>();
        for (COSObjectKey objectKey : xrefTable.keySet())
        {
            COSObject objectFromPool = getObjectFromPool(objectKey);
            COSBase realObject = objectFromPool.getObject();
            if( realObject instanceof COSDictionary )
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
        boolean encrypted = false;
        if (trailer != null)
        {
            encrypted = trailer.getDictionaryObject(COSName.ENCRYPT) instanceof COSDictionary;
        }
        return encrypted;
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
     * This will set the document ID.
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
     * @return any object, depending on the visitor implementation, or null
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public Object accept(ICOSVisitor visitor) throws IOException
    {
        return visitor.visitFromDocument( this );
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
        // - ScratchFile is closed
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

        if (scratchFile != null)
        {
            firstException = IOUtils.closeAndLogException(scratchFile, LOG, "ScratchFile", firstException);
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
     */
    public boolean isClosed()
    {
        return closed;
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
}
