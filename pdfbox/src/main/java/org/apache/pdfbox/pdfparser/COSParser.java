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
package org.apache.pdfbox.pdfparser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.ICOSParser;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadView;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

/**
 * COS-Parser which first reads startxref and xref tables in order to know valid objects and parse only these objects.
 * 
 * This class is a much enhanced version of <code>QuickParser</code> presented in
 * <a href="https://issues.apache.org/jira/browse/PDFBOX-1104">PDFBOX-1104</a> by Jeremy Villalobos.
 */
public class COSParser extends BaseParser implements ICOSParser
{
    private static final String PDF_HEADER = "%PDF-";
    private static final String FDF_HEADER = "%FDF-";
    
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final String FDF_DEFAULT_VERSION = "1.0";

    private static final char[] XREF_TABLE = new char[] { 'x', 'r', 'e', 'f' };
    private static final char[] XREF_STREAM = new char[] { '/', 'X', 'R', 'e', 'f' };
    private static final char[] STARTXREF = new char[] { 's','t','a','r','t','x','r','e','f' };

    private static final byte[] ENDSTREAM = new byte[] { E, N, D, S, T, R, E, A, M };

    private static final byte[] ENDOBJ = new byte[] { E, N, D, O, B, J };

    private static final long MINIMUM_SEARCH_OFFSET = 6;
    
    private static final int X = 'x';

    private static final int STRMBUFLEN = 2048;
    private final byte[] strmBuf = new byte[ STRMBUFLEN ];

    private AccessPermission accessPermission;
    private InputStream keyStoreInputStream = null;
    @SuppressWarnings({"squid:S2068"})
    private String password = "";
    private String keyAlias = null;

    /**
     * The range within the %%EOF marker will be searched.
     * Useful if there are additional characters after %%EOF within the PDF. 
     */
    public static final String SYSPROP_EOFLOOKUPRANGE =
            "org.apache.pdfbox.pdfparser.nonSequentialPDFParser.eofLookupRange";

    /**
     * How many trailing bytes to read for EOF marker.
     */
    private static final int DEFAULT_TRAIL_BYTECOUNT = 2048;
    /**
     * EOF-marker.
     */
    protected static final char[] EOF_MARKER = new char[] { '%', '%', 'E', 'O', 'F' };
    /**
     * obj-marker.
     */
    protected static final char[] OBJ_MARKER = new char[] { 'o', 'b', 'j' };

    /**
     * trailer-marker.
     */
    private static final char[] TRAILER_MARKER = new char[] { 't', 'r', 'a', 'i', 'l', 'e', 'r' };

    /**
     * ObjStream-marker.
     */
    private static final char[] OBJ_STREAM = new char[] { '/', 'O', 'b', 'j', 'S', 't', 'm' };

    /**
     * file length.
     */
    protected long fileLen;

    /**
     * is parser using auto healing capacity ?
     */
    private boolean isLenient = true;

    protected boolean initialParseDone = false;

    private boolean trailerWasRebuild = false;
    /**
     * Contains all found objects of a brute force search.
     */
    private Map<COSObjectKey, Long> bfSearchCOSObjectKeyOffsets = null;
    boolean bruteForceSearchTriggered = false;
    private PDEncryption encryption = null;

    /**
     * The security handler.
     */
    protected SecurityHandler<? extends ProtectionPolicy> securityHandler = null;

    /**
     *  how many trailing bytes to read for EOF marker.
     */
    private int readTrailBytes = DEFAULT_TRAIL_BYTECOUNT; 

    private static final Log LOG = LogFactory.getLog(COSParser.class);

    /** 
     * Collects all Xref/trailer objects and resolves them into single
     * object using startxref reference. 
     */
    protected XrefTrailerResolver xrefTrailerResolver = new XrefTrailerResolver();


    /**
     * The prefix for the temp file being used. 
     */
    public static final String TMP_FILE_PREFIX = "tmpPDF";
    
    /**
     * Default constructor.
     *
     * @param source input representing the pdf.
     */
    public COSParser(RandomAccessRead source) throws IOException
    {
        super(source);
        fileLen = source.length();
    }

    /**
     * Constructor for encrypted pdfs.
     * 
     * @param source input representing the pdf.
     * @param password password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security
     * @param keyAlias alias to be used for decryption when using public key security
     * 
     */
    public COSParser(RandomAccessRead source, String password, InputStream keyStore,
            String keyAlias) throws IOException
    {
        super(source);
        this.password = password;
        this.keyAlias = keyAlias;
        fileLen = source.length();
        keyStoreInputStream = keyStore;
    }

    /**
     * Sets how many trailing bytes of PDF file are searched for EOF marker and 'startxref' marker. If not set we use
     * default value {@link #DEFAULT_TRAIL_BYTECOUNT}.
     * 
     * <p>We check that new value is at least 16. However for practical use cases this value should not be lower than
     * 1000; even 2000 was found to not be enough in some cases where some trailing garbage like HTML snippets followed
     * the EOF marker.</p>
     * 
     * <p>
     * In case system property {@link #SYSPROP_EOFLOOKUPRANGE} is defined this value will be set on initialization but
     * can be overwritten later.
     * </p>
     * 
     * @param byteCount number of trailing bytes
     */
    public void setEOFLookupRange(int byteCount)
    {
        if (byteCount > 15)
        {
            readTrailBytes = byteCount;
        }
    }

    /**
     * Read the trailer information and provide a COSDictionary containing the trailer information.
     * 
     * @return a COSDictionary containing the trailer information
     * @throws IOException if something went wrong
     */
    protected COSDictionary retrieveTrailer() throws IOException
    {
        COSDictionary trailer = null;
        boolean rebuildTrailer = false;
        try
        {
            // parse startxref
            // TODO FDF files don't have a startxref value, so that rebuildTrailer is triggered
            long startXRefOffset = getStartxrefOffset();
            if (startXRefOffset > -1)
            {
                trailer = parseXref(startXRefOffset);
            }
            else
            {
                rebuildTrailer = isLenient();
            }
        }
        catch (IOException exception)
        {
            if (isLenient())
            {
                rebuildTrailer = true;
            }
            else
            {
                throw exception;
            }
        }
        // check if the trailer contains a Root object
        if (trailer != null && trailer.getItem(COSName.ROOT) == null)
        {
            rebuildTrailer = isLenient();
        }
        if (rebuildTrailer)
        {
            trailer = rebuildTrailer();
        }
        else
        {
            // prepare decryption if necessary
            prepareDecryption();
            if (bruteForceSearchTriggered && !getBFCOSObjectOffsets().isEmpty())
            {
                bfSearchForObjStreams();
            }
        }
        if (resetTrailerResolver())
        {
            xrefTrailerResolver.reset();
            xrefTrailerResolver = null;
        }
        return trailer;
    }

    /**
     * Indicates whether the xref trailer resolver should be reseted or not. Should be overwritten if the xref trailer
     * resolver is needed after the initial parsing.
     * 
     * @return true if the xref trailer resolver should be reseted
     */
    protected boolean resetTrailerResolver()
    {
        return true;
    }

    /**
     * Parses cross reference tables.
     * 
     * @param startXRefOffset start offset of the first table
     * @return the trailer dictionary
     * @throws IOException if something went wrong
     */
    private COSDictionary parseXref(long startXRefOffset) throws IOException
    {
        source.seek(startXRefOffset);
        long startXrefOffset = Math.max(0, parseStartXref());
        // check the startxref offset
        long fixedOffset = checkXRefOffset(startXrefOffset);
        if (fixedOffset > -1)
        {
            startXrefOffset = fixedOffset;
        }
        document.setStartXref(startXrefOffset);
        long prev = startXrefOffset;
        // ---- parse whole chain of xref tables/object streams using PREV reference
        Set<Long> prevSet = new HashSet<>();
        while (prev > 0)
        {
            // seek to xref table
            source.seek(prev);

            // skip white spaces
            skipSpaces();
            // -- parse xref
            if (source.peek() == X)
            {
                // xref table and trailer
                // use existing parser to parse xref table
                if (!parseXrefTable(prev) || !parseTrailer())
                {
                    throw new IOException("Expected trailer object at offset "
                            + source.getPosition());
                }
                COSDictionary trailer = xrefTrailerResolver.getCurrentTrailer();
                // check for a XRef stream, it may contain some object ids of compressed objects 
                if(trailer.containsKey(COSName.XREF_STM))
                {
                    int streamOffset = trailer.getInt(COSName.XREF_STM);
                    // check the xref stream reference
                    fixedOffset = checkXRefOffset(streamOffset);
                    if (fixedOffset > -1 && fixedOffset != streamOffset)
                    {
                        LOG.warn("/XRefStm offset " + streamOffset + " is incorrect, corrected to " + fixedOffset);
                        streamOffset = (int)fixedOffset;
                        trailer.setInt(COSName.XREF_STM, streamOffset);
                    }
                    if (streamOffset > 0)
                    {
                        source.seek(streamOffset);
                        skipSpaces();
                        try
                        {
                            parseXrefObjStream(prev, false);
                        }
                        catch (IOException ex)
                        {
                            if (isLenient)
                            {
                                LOG.error("Failed to parse /XRefStm at offset " + streamOffset, ex);
                            }
                            else
                            {
                                throw ex;
                            }
                        }
                    }
                    else
                    {
                        if(isLenient)
                        {
                            LOG.error("Skipped XRef stream due to a corrupt offset:"+streamOffset);
                        }
                        else
                        {
                            throw new IOException("Skipped XRef stream due to a corrupt offset:"+streamOffset);
                        }
                    }
                }
                prev = trailer.getLong(COSName.PREV);
                if (prev > 0)
                {
                    // check the xref table reference
                    fixedOffset = checkXRefOffset(prev);
                    if (fixedOffset > -1 && fixedOffset != prev)
                    {
                        prev = fixedOffset;
                        trailer.setLong(COSName.PREV, prev);
                    }
                }
            }
            else
            {
                // parse xref stream
                prev = parseXrefObjStream(prev, true);
                if (prev > 0)
                {
                    // check the xref table reference
                    fixedOffset = checkXRefOffset(prev);
                    if (fixedOffset > -1 && fixedOffset != prev)
                    {
                        prev = fixedOffset;
                        COSDictionary trailer = xrefTrailerResolver.getCurrentTrailer();
                        trailer.setLong(COSName.PREV, prev);
                    }
                }
            }
            if (prevSet.contains(prev))
            {
                throw new IOException("/Prev loop at offset " + prev);
            }
            prevSet.add(prev);
        }
        // ---- build valid xrefs out of the xref chain
        xrefTrailerResolver.setStartxref(startXrefOffset);
        COSDictionary trailer = xrefTrailerResolver.getTrailer();
        document.setTrailer(trailer);
        document.setIsXRefStream(XRefType.STREAM == xrefTrailerResolver.getXrefType());
        // check the offsets of all referenced objects
        checkXrefOffsets();
        // copy xref table
        document.addXRefTable(xrefTrailerResolver.getXrefTable());

        // remember the highest XRef object number to avoid it being reused in incremental saving
        Optional<Long> maxValue = document.getXrefTable().keySet().stream() //
                .map(COSObjectKey::getNumber) //
                .reduce(Long::max);
        document.setHighestXRefObjectNumber(maxValue.isPresent() ? maxValue.get() : 0);

        return trailer;
    }

    /**
     * Parses an xref object stream starting with indirect object id.
     * 
     * @return value of PREV item in dictionary or <code>-1</code> if no such item exists
     */
    private long parseXrefObjStream(long objByteOffset, boolean isStandalone) throws IOException
    {
        // ---- parse indirect object head
        readObjectNumber();
        readGenerationNumber();
        readExpectedString(OBJ_MARKER, true);

        COSDictionary dict = parseCOSDictionary();
        try (COSStream xrefStream = parseCOSStream(dict))
        {
            parseXrefStream(xrefStream, objByteOffset, isStandalone);
        }

        return dict.getLong(COSName.PREV);
    }
    
    /**
     * Looks for and parses startxref. We first look for last '%%EOF' marker (within last
     * {@link #DEFAULT_TRAIL_BYTECOUNT} bytes (or range set via {@link #setEOFLookupRange(int)}) and go back to find
     * <code>startxref</code>.
     * 
     * @return the offset of StartXref
     * @throws IOException If something went wrong.
     */
    private long getStartxrefOffset() throws IOException
    {
        byte[] buf;
        long skipBytes;
        // read trailing bytes into buffer
        try
        {
            final int trailByteCount = (fileLen < readTrailBytes) ? (int) fileLen : readTrailBytes;
            buf = new byte[trailByteCount];
            skipBytes = fileLen - trailByteCount;
            source.seek(skipBytes);
            int off = 0;
            int readBytes;
            while (off < trailByteCount)
            {
                readBytes = source.read(buf, off, trailByteCount - off);
                // in order to not get stuck in a loop we check readBytes (this should never happen)
                if (readBytes < 1)
                {
                    throw new IOException(
                            "No more bytes to read for trailing buffer, but expected: "
                                    + (trailByteCount - off));
                }
                off += readBytes;
            }
        }
        finally
        {
            source.seek(0);
        }
        // find last '%%EOF'
        int bufOff = lastIndexOf(EOF_MARKER, buf, buf.length);
        if (bufOff < 0)
        {
            if (isLenient) 
            {
                // in lenient mode the '%%EOF' isn't needed
                bufOff = buf.length;
                LOG.debug("Missing end of file marker '" + new String(EOF_MARKER) + "'");
            } 
            else 
            {
                throw new IOException("Missing end of file marker '" + new String(EOF_MARKER) + "'");
            }
        }
        // find last startxref preceding EOF marker
        bufOff = lastIndexOf(STARTXREF, buf, bufOff);
        if (bufOff < 0)
        {
            throw new IOException("Missing 'startxref' marker.");
        }
        else
        {
            return skipBytes + bufOff;
        }
    }
    
    /**
     * Searches last appearance of pattern within buffer. Lookup before _lastOff and goes back until 0.
     * 
     * @param pattern pattern to search for
     * @param buf buffer to search pattern in
     * @param endOff offset (exclusive) where lookup starts at
     * 
     * @return start offset of pattern within buffer or <code>-1</code> if pattern could not be found
     */
    protected int lastIndexOf(final char[] pattern, final byte[] buf, final int endOff)
    {
        final int lastPatternChOff = pattern.length - 1;

        int bufOff = endOff;
        int patOff = lastPatternChOff;
        char lookupCh = pattern[patOff];

        while (--bufOff >= 0)
        {
            if (buf[bufOff] == lookupCh)
            {
                if (--patOff < 0)
                {
                    // whole pattern matched
                    return bufOff;
                }
                // matched current char, advance to preceding one
                lookupCh = pattern[patOff];
            }
            else if (patOff < lastPatternChOff)
            {
                // no char match but already matched some chars; reset
                patOff = lastPatternChOff;
                lookupCh = pattern[patOff];
            }
        }
        return -1;
    }
    
    /**
     * Return true if parser is lenient. Meaning auto healing capacity of the parser are used.
     *
     * @return true if parser is lenient
     */
    public boolean isLenient()
    {
        return isLenient;
    }

    /**
     * Change the parser leniency flag.
     *
     * This method can only be called before the parsing of the file.
     *
     * @param lenient try to handle malformed PDFs.
     *
     */
    protected void setLenient(boolean lenient)
    {
        if (initialParseDone)
        {
            throw new IllegalArgumentException("Cannot change leniency after parsing");
        }
        this.isLenient = lenient;
    }

    @Override
    public COSBase dereferenceCOSObject(COSObject obj) throws IOException
    {
        long currentPos = source.getPosition();
        COSBase parsedObj = parseObjectDynamically(obj.getObjectNumber(), obj.getGenerationNumber(),
                false);
        if (currentPos > 0)
        {
            source.seek(currentPos);
        }
        return parsedObj;
    }

    @Override
    public RandomAccessReadView createRandomAccessReadView(long startPosition, long streamLength)
            throws IOException
    {
        return source.createView(startPosition, streamLength);
    }

    /**
     * Parse the object for the given object number.  
     * 
     * @param objNr object number of object to be parsed
     * @param objGenNr object generation number of object to be parsed
     * @param requireExistingNotCompressedObj if <code>true</code> the object to be parsed must be defined in xref
     * (comment: null objects may be missing from xref) and it must not be a compressed object within object stream
     * (this is used to circumvent being stuck in a loop in a malicious PDF)
     * 
     * @return the parsed object (which is also added to document object)
     * 
     * @throws IOException If an IO error occurs.
     */
    protected synchronized COSBase parseObjectDynamically(long objNr, int objGenNr,
            boolean requireExistingNotCompressedObj) throws IOException
    {
        final COSObjectKey objKey = new COSObjectKey(objNr, objGenNr);
        COSObject pdfObject = document.getObjectFromPool(objKey);
        if (!pdfObject.isObjectNull())
        {
            return pdfObject.getObject();
        }
        Long offsetOrObjstmObNr = getObjectOffset(objKey, requireExistingNotCompressedObj);
        COSBase referencedObject = null;
        if (offsetOrObjstmObNr != null)
        {

            if (offsetOrObjstmObNr > 0)
            {
                referencedObject = parseFileObject(offsetOrObjstmObNr, objKey);
            }
            else
            {
                // xref value is object nr of object stream containing object to be parsed
                // since our object was not found it means object stream was not parsed so far
                referencedObject = parseObjectStreamObject((int) -offsetOrObjstmObNr, objKey);
            }
        }
        if (referencedObject == null || referencedObject instanceof COSNull)
        {
            // not defined object -> NULL object (Spec. 1.7, chap. 3.2.9)
            // or some other issue with dereferencing
            // remove parser to avoid endless recursion
            pdfObject.setToNull();
        }
        return referencedObject;
    }

    private Long getObjectOffset(COSObjectKey objKey, boolean requireExistingNotCompressedObj)
            throws IOException
    {
        // read offset or object stream object number from xref table
        Long offsetOrObjstmObNr = document.getXrefTable().get(objKey);

        // maybe something is wrong with the xref table -> perform brute force search for all objects
        if (offsetOrObjstmObNr == null && isLenient)
        {
            Map<COSObjectKey, Long> bfCOSObjectKeyOffsets = getBFCOSObjectOffsets();
            offsetOrObjstmObNr = bfCOSObjectKeyOffsets.get(objKey);
            if (offsetOrObjstmObNr != null)
            {
                LOG.debug("Set missing offset " + offsetOrObjstmObNr + " for object " + objKey);
                document.getXrefTable().put(objKey, offsetOrObjstmObNr);
            }
        }

        // sanity test to circumvent loops with broken documents
        if (requireExistingNotCompressedObj
                && ((offsetOrObjstmObNr == null) || (offsetOrObjstmObNr <= 0)))
        {
            throw new IOException("Object must be defined and must not be compressed object: "
                    + objKey.getNumber() + ":" + objKey.getGeneration());
        }
        return offsetOrObjstmObNr;
    }

    private COSBase parseFileObject(Long offsetOrObjstmObNr, final COSObjectKey objKey)
            throws IOException
    {
        // ---- go to object start
        source.seek(offsetOrObjstmObNr);

        // ---- we must have an indirect object
        final long readObjNr = readObjectNumber();
        final int readObjGen = readGenerationNumber();
        readExpectedString(OBJ_MARKER, true);

        // ---- consistency check
        if ((readObjNr != objKey.getNumber()) || (readObjGen != objKey.getGeneration()))
        {
            throw new IOException("XREF for " + objKey.getNumber() + ":"
                    + objKey.getGeneration() + " points to wrong object: " + readObjNr
                    + ":" + readObjGen + " at offset " + offsetOrObjstmObNr);
        }

        skipSpaces();
        COSBase parsedObject = parseDirObject();
        String endObjectKey = readString();

        if (endObjectKey.equals(STREAM_STRING))
        {
            source.rewind(endObjectKey.getBytes(StandardCharsets.ISO_8859_1).length);
            if (parsedObject instanceof COSDictionary)
            {
                COSStream stream = parseCOSStream((COSDictionary) parsedObject);

                if (securityHandler != null)
                {
                    securityHandler.decryptStream(stream, objKey.getNumber(), objKey.getGeneration());
                }
                parsedObject = stream;
            }
            else
            {
                // this is not legal
                // the combination of a dict and the stream/endstream
                // forms a complete stream object
                throw new IOException("Stream not preceded by dictionary (offset: "
                        + offsetOrObjstmObNr + ").");
            }
            skipSpaces();
            endObjectKey = readLine();

            // we have case with a second 'endstream' before endobj
            if (!endObjectKey.startsWith(ENDOBJ_STRING) && endObjectKey.startsWith(ENDSTREAM_STRING))
            {
                endObjectKey = endObjectKey.substring(9).trim();
                if (endObjectKey.length() == 0)
                {
                    // no other characters in extra endstream line
                    // read next line
                    endObjectKey = readLine();
                }
            }
        }
        else if (securityHandler != null)
        {
            securityHandler.decrypt(parsedObject, objKey.getNumber(), objKey.getGeneration());
        }

        if (!endObjectKey.startsWith(ENDOBJ_STRING))
        {
            if (isLenient)
            {
                LOG.warn("Object (" + readObjNr + ":" + readObjGen + ") at offset "
                        + offsetOrObjstmObNr + " does not end with 'endobj' but with '"
                        + endObjectKey + "'");
            }
            else
            {
                throw new IOException("Object (" + readObjNr + ":" + readObjGen
                        + ") at offset " + offsetOrObjstmObNr
                        + " does not end with 'endobj' but with '" + endObjectKey + "'");
            }
        }
        return parsedObject;
    }

    /**
     * Parse the object with the given key from the object stream with the given number.
     * 
     * @param objstmObjNr the number of the offset stream
     * @param key the key of the object to be parsed
     * @return the parsed object
     * @throws IOException if something went wrong when parsing the object
     */
    protected COSBase parseObjectStreamObject(int objstmObjNr, COSObjectKey key) throws IOException
    {
        final COSBase objstmBaseObj = parseObjectDynamically(objstmObjNr, 0, true);
        COSBase objectStreamObject = null;
        if (objstmBaseObj instanceof COSStream)
        {
            // parse object stream
            PDFObjectStreamParser parser = null;
            try
            {
                parser = new PDFObjectStreamParser((COSStream) objstmBaseObj, document);
                objectStreamObject = parser.parseObject(key.getNumber());
                if (objectStreamObject != null)
                {
                    objectStreamObject.setKey(key);
                }
            }
            catch (IOException ex)
            {
                if (isLenient)
                {
                    LOG.error("object stream " + objstmObjNr
                            + " could not be parsed due to an exception", ex);
                }
                else
                {
                    throw ex;
                }
            }
        }
        return objectStreamObject;
    }
    
    /** 
     * Returns length value referred to or defined in given object. 
     */
    private COSNumber getLength(final COSBase lengthBaseObj) throws IOException
    {
        if (lengthBaseObj == null)
        {
            return null;
        }
        // maybe length was given directly
        if (lengthBaseObj instanceof COSNumber)
        {
            return (COSNumber) lengthBaseObj;
        }
        // length in referenced object
        if (lengthBaseObj instanceof COSObject)
        {
            COSObject lengthObj = (COSObject) lengthBaseObj;
            COSBase length = lengthObj.getObject();
            if (length == null)
            {
                throw new IOException("Length object content was not read.");
            }
            if (COSNull.NULL == length)
            {
                LOG.warn("Length object (" + lengthObj.getObjectNumber() + " "
                        + lengthObj.getGenerationNumber() + ") not found");
                return null;
            }
            if (length instanceof COSNumber)
            {
                return (COSNumber) length;
            }
            throw new IOException("Wrong type of referenced length object " + lengthObj + ": "
                    + length.getClass().getSimpleName());
        }
        throw new IOException(
                "Wrong type of length object: " + lengthBaseObj.getClass().getSimpleName());
    }
    
    /**
     * This will read a COSStream from the input stream using length attribute within dictionary. If
     * length attribute is a indirect reference it is first resolved to get the stream length. This
     * means we copy stream data without testing for 'endstream' or 'endobj' and thus it is no
     * problem if these keywords occur within stream. We require 'endstream' to be found after
     * stream data is read.
     *
     * @param dic dictionary that goes with this stream.
     *
     * @return parsed pdf stream.
     *
     * @throws IOException if an error occurred reading the stream, like problems with reading
     * length attribute, stream does not end with 'endstream' after data read, stream too short etc.
     */
    protected COSStream parseCOSStream(COSDictionary dic) throws IOException
    {
        // read 'stream'; this was already tested in parseObjectsDynamically()
        readString(); 
        
        skipWhiteSpaces();

        /*
         * This needs to be dic.getItem because when we are parsing, the underlying object might still be null.
         */
        COSNumber streamLengthObj = getLength(dic.getItem(COSName.LENGTH));
        if (streamLengthObj == null)
        {
            if (isLenient)
            {
               LOG.warn("The stream doesn't provide any stream length, using fallback readUntilEnd, at offset "
                    + source.getPosition());
            }
            else
            {
                throw new IOException("Missing length for stream.");
            }
        }


        long streamStartPosition = source.getPosition();
        long streamLength;
        if (streamLengthObj != null && validateStreamLength(streamLengthObj.longValue()))
        {
            streamLength = streamLengthObj.longValue();
            // skip stream
            source.seek(source.getPosition() + streamLengthObj.intValue());
        }
        else
        {
            streamLength = readUntilEndStream(new EndstreamFilterStream());
        }
        String endStream = readString();
        if (endStream.equals("endobj") && isLenient)
        {
            LOG.warn("stream ends with 'endobj' instead of 'endstream' at offset "
                    + source.getPosition());
            // avoid follow-up warning about missing endobj
            source.rewind(ENDOBJ.length);
        }
        else if (endStream.length() > 9 && isLenient && endStream.substring(0,9).equals(ENDSTREAM_STRING))
        {
            LOG.warn("stream ends with '" + endStream + "' instead of 'endstream' at offset "
                    + source.getPosition());
            // unread the "extra" bytes
            source.rewind(endStream.substring(9).getBytes(StandardCharsets.ISO_8859_1).length);
        }
        else if (!endStream.equals(ENDSTREAM_STRING))
        {
            throw new IOException(
                    "Error reading stream, expected='endstream' actual='"
                    + endStream + "' at offset " + source.getPosition());
        }
        return document.createCOSStream(dic, streamStartPosition, streamLength);
    }

    /**
     * This method will read through the current stream object until
     * we find the keyword "endstream" meaning we're at the end of this
     * object. Some pdf files, however, forget to write some endstream tags
     * and just close off objects with an "endobj" tag so we have to handle
     * this case as well.
     * 
     * This method is optimized using buffered IO and reduced number of
     * byte compare operations.
     * 
     * @param out  stream we write out to.
     * 
     * @throws IOException if something went wrong
     */
    private long readUntilEndStream(final EndstreamFilterStream out) throws IOException
    {
        int bufSize;
        int charMatchCount = 0;
        byte[] keyw = ENDSTREAM;
        
        // last character position of shortest keyword ('endobj')
        final int quickTestOffset = 5;
        
        // read next chunk into buffer; already matched chars are added to beginning of buffer
        while ( ( bufSize = source.read( strmBuf, charMatchCount, STRMBUFLEN - charMatchCount ) ) > 0 ) 
        {
            bufSize += charMatchCount;
            
            int bIdx = charMatchCount;
            int quickTestIdx;
        
            // iterate over buffer, trying to find keyword match
            for ( int maxQuicktestIdx = bufSize - quickTestOffset; bIdx < bufSize; bIdx++ ) 
            {
                // reduce compare operations by first test last character we would have to
                // match if current one matches; if it is not a character from keywords
                // we can move behind the test character; this shortcut is inspired by the 
                // Boyer-Moore string search algorithm and can reduce parsing time by approx. 20%
                quickTestIdx = bIdx + quickTestOffset;
                if (charMatchCount == 0 && quickTestIdx < maxQuicktestIdx)
                {                    
                    final byte ch = strmBuf[quickTestIdx];
                    if ( ( ch > 't' ) || ( ch < 'a' ) ) 
                    {
                        // last character we would have to match if current character would match
                        // is not a character from keywords -> jump behind and start over
                        bIdx = quickTestIdx;
                        continue;
                    }
                }
                
                // could be negative - but we only compare to ASCII
                final byte ch = strmBuf[bIdx];
            
                if ( ch == keyw[ charMatchCount ] ) 
                {
                    if ( ++charMatchCount == keyw.length ) 
                    {
                        // match found
                        bIdx++;
                        break;
                    }
                } 
                else 
                {
                    if ( ( charMatchCount == 3 ) && ( ch == ENDOBJ[ charMatchCount ] ) ) 
                    {
                        // maybe ENDSTREAM is missing but we could have ENDOBJ
                        keyw = ENDOBJ;
                        charMatchCount++;
                    } 
                    else 
                    {
                        // no match; incrementing match start by 1 would be dumb since we already know 
                        // matched chars depending on current char read we may already have beginning 
                        // of a new match: 'e': first char matched; 'n': if we are at match position 
                        // idx 7 we already read 'e' thus 2 chars matched for each other char we have 
                        // to start matching first keyword char beginning with next read position
                        charMatchCount = ( ch == E ) ? 1 : ( ( ch == N ) && ( charMatchCount == 7 ) ) ? 2 : 0;
                        // search again for 'endstream'
                        keyw = ENDSTREAM;
                    }
                } 
            }
            
            int contentBytes = Math.max( 0, bIdx - charMatchCount );
            
            // write buffer content until first matched char to output stream
            if ( contentBytes > 0 )
            {
                out.filter(strmBuf, 0, contentBytes);
            }
            if ( charMatchCount == keyw.length ) 
            {
                // keyword matched; unread matched keyword (endstream/endobj) and following buffered content
                source.rewind( bufSize - contentBytes );
                break;
            } 
            else 
            {
                // copy matched chars at start of buffer
                System.arraycopy( keyw, 0, strmBuf, 0, charMatchCount );
            }            
        }
        // this writes a lonely CR or drops trailing CR LF and LF
        return out.calculateLength();
    }

    private boolean validateStreamLength(long streamLength) throws IOException
    {
        boolean streamLengthIsValid = true;
        long originOffset = source.getPosition();
        long expectedEndOfStream = originOffset + streamLength;
        if (expectedEndOfStream > fileLen)
        {
            streamLengthIsValid = false;
            LOG.warn("The end of the stream is out of range, using workaround to read the stream, "
                    + "stream start position: " + originOffset + ", length: " + streamLength
                    + ", expected end position: " + expectedEndOfStream);
        }
        else
        {
            source.seek(expectedEndOfStream);
            skipSpaces();
            if (!isString(ENDSTREAM))
            {
                streamLengthIsValid = false;
                LOG.warn("The end of the stream doesn't point to the correct offset, using workaround to read the stream, "
                        + "stream start position: " + originOffset + ", length: " + streamLength
                        + ", expected end position: " + expectedEndOfStream);
            }
            source.seek(originOffset);
        }
        return streamLengthIsValid;
    }

    /**
     * Check if the cross reference table/stream can be found at the current offset.
     * 
     * @param startXRefOffset
     * @return the revised offset
     * @throws IOException
     */
    private long checkXRefOffset(long startXRefOffset) throws IOException
    {
        // repair mode isn't available in non-lenient mode
        if (!isLenient)
        {
            return startXRefOffset;
        }
        source.seek(startXRefOffset);
        skipSpaces();
        if (isString(XREF_TABLE))
        {
            return startXRefOffset;
        }
        if (startXRefOffset > 0)
        {
            if (checkXRefStreamOffset(startXRefOffset))
            {
                return startXRefOffset;
            }
            else
            {
                return calculateXRefFixedOffset(startXRefOffset);
            }
        }
        // can't find a valid offset
        return -1;
    }

    /**
     * Check if the cross reference stream can be found at the current offset.
     * 
     * @param startXRefOffset the expected start offset of the XRef stream
     * @return the revised offset
     * @throws IOException if something went wrong
     */
    private boolean checkXRefStreamOffset(long startXRefOffset) throws IOException
    {
        // repair mode isn't available in non-lenient mode
        if (!isLenient || startXRefOffset == 0)
        {
            return true;
        }
        // seek to offset-1 
        source.seek(startXRefOffset-1);
        int nextValue = source.read();
        // the first character has to be a whitespace, and then a digit
        if (isWhitespace(nextValue))
        {
            skipSpaces();
            if (isDigit())
            {
                try
                {
                    // it's a XRef stream
                    readObjectNumber();
                    readGenerationNumber();
                    readExpectedString(OBJ_MARKER, true);
                    // check the dictionary to avoid false positives
                    COSDictionary dict = parseCOSDictionary();
                    source.seek(startXRefOffset);
                    if ("XRef".equals(dict.getNameAsString(COSName.TYPE)))
                    {
                        return true;
                    }
                }
                catch (IOException exception)
                {
                    // there wasn't an object of a xref stream
                    LOG.debug("No Xref stream at given location " + startXRefOffset, exception);
                    source.seek(startXRefOffset);
                }
            }
        }
        return false;
    }
    
    /**
     * Try to find a fixed offset for the given xref table/stream.
     * 
     * @param objectOffset the given offset where to look at
     * @return the fixed offset
     * 
     * @throws IOException if something went wrong
     */
    private long calculateXRefFixedOffset(long objectOffset) throws IOException
    {
        if (objectOffset < 0)
        {
            LOG.error("Invalid object offset " + objectOffset + " when searching for a xref table/stream");
            return 0;
        }
        // start a brute force search for all xref tables and try to find the offset we are looking for
        long newOffset = bfSearchForXRef(objectOffset);
        if (newOffset > -1)
        {
            LOG.debug("Fixed reference for xref table/stream " + objectOffset + " -> " + newOffset);
            return newOffset;
        }
        LOG.error("Can't find the object xref table/stream at offset " + objectOffset);
        return 0;
    }

    private boolean validateXrefOffsets(Map<COSObjectKey, Long> xrefOffset) throws IOException
    {
        if (xrefOffset == null)
        {
            return true;
        }
        for (Entry<COSObjectKey, Long> objectEntry : xrefOffset.entrySet())
        {
            COSObjectKey objectKey = objectEntry.getKey();
            Long objectOffset = objectEntry.getValue();
            // a negative offset number represents an object number itself
            // see type 2 entry in xref stream
            if (objectOffset != null && objectOffset >= 0
                    && !checkObjectKey(objectKey, objectOffset))
            {
                LOG.debug("Stop checking xref offsets as at least one (" + objectKey
                        + ") couldn't be dereferenced");
                return false;
            }
        }
        return true;
    }

    /**
     * Check the XRef table by dereferencing all objects and fixing the offset if necessary.
     * 
     * @throws IOException if something went wrong.
     */
    private void checkXrefOffsets() throws IOException
    {
        // repair mode isn't available in non-lenient mode
        if (!isLenient)
        {
            return;
        }
        Map<COSObjectKey, Long> xrefOffset = xrefTrailerResolver.getXrefTable();
        if (!validateXrefOffsets(xrefOffset))
        {
            Map<COSObjectKey, Long> bfCOSObjectKeyOffsets = getBFCOSObjectOffsets();
            if (!bfCOSObjectKeyOffsets.isEmpty())
            {
                LOG.debug("Replaced read xref table with the results of a brute force search");
                xrefOffset.clear();
                xrefOffset.putAll(bfCOSObjectKeyOffsets);
            }
        }
    }

    /**
     * Check if the given object can be found at the given offset.
     * 
     * @param objectKey the object we are looking for
     * @param offset the offset where to look
     * @return returns true if the given object can be dereferenced at the given offset
     * @throws IOException if something went wrong
     */
    private boolean checkObjectKey(COSObjectKey objectKey, long offset) throws IOException
    {
        // there can't be any object at the very beginning of a pdf
        if (offset < MINIMUM_SEARCH_OFFSET)
        {
            return false;
        }
        boolean objectKeyFound = false;
        try 
        {
            source.seek(offset);
            // try to read the given object/generation number
            if (objectKey.getNumber() == readObjectNumber())
            {
                int genNumber = readGenerationNumber();
                if (genNumber == objectKey.getGeneration())
                {
                    // finally try to read the object marker
                    readExpectedString(OBJ_MARKER, true);
                    objectKeyFound = true;
                }
                else if (isLenient && genNumber > objectKey.getGeneration())
                {
                    // finally try to read the object marker
                    readExpectedString(OBJ_MARKER, true);
                    objectKeyFound = true;
                    objectKey.fixGeneration(genNumber);
                }
            }
        }
        catch (IOException exception)
        {
            // Swallow the exception, obviously there isn't any valid object number
            LOG.debug("No valid object at given location " + offset + " - ignoring", exception);
        }
        // return resulting value
        return objectKeyFound;
    }

    private Map<COSObjectKey, Long> getBFCOSObjectOffsets() throws IOException
    {
        if (bfSearchCOSObjectKeyOffsets == null)
        {
            bfSearchCOSObjectKeyOffsets = bfSearchForObjects();
            bruteForceSearchTriggered = true;
        }
        return bfSearchCOSObjectKeyOffsets;
    }

    /**
     * Brute force search for every object in the pdf.
     *   
     * @throws IOException if something went wrong
     */
    private Map<COSObjectKey, Long> bfSearchForObjects() throws IOException
    {
        Map<COSObjectKey, Long> bfCOSObjectKeyOffsets = new HashMap<>();
        long lastEOFMarker = bfSearchForLastEOFMarker();
        long originOffset = source.getPosition();
        long currentOffset = MINIMUM_SEARCH_OFFSET;
        long lastObjectId = Long.MIN_VALUE;
        int lastGenID = Integer.MIN_VALUE;
        long lastObjOffset = Long.MIN_VALUE;
        char[] endobjString = "ndo".toCharArray();
        char[] endobjRemainingString = "bj".toCharArray();
        boolean endOfObjFound = false;
        do
        {
            source.seek(currentOffset);
            int nextChar = source.read();
            currentOffset++;
            if (isWhitespace(nextChar) && isString(OBJ_MARKER))
            {
                long tempOffset = currentOffset - 2;
                source.seek(tempOffset);
                int genID = source.peek();
                // is the next char a digit?
                if (isDigit(genID))
                {
                    genID -= 48;
                    tempOffset--;
                    source.seek(tempOffset);
                    if (isWhitespace())
                    {
                        while (tempOffset > MINIMUM_SEARCH_OFFSET && isWhitespace())
                        {
                            source.seek(--tempOffset);
                        }
                        boolean objectIDFound = false;
                        while (tempOffset > MINIMUM_SEARCH_OFFSET && isDigit())
                        {
                            source.seek(--tempOffset);
                            objectIDFound = true;
                        }
                        if (objectIDFound)
                        {
                            source.read();
                            long objectId = readObjectNumber();
                            if (lastObjOffset > 0)
                            {
                                // add the former object ID only if there was a subsequent object ID
                                bfCOSObjectKeyOffsets.put(
                                        new COSObjectKey(lastObjectId, lastGenID), lastObjOffset);
                            }
                            lastObjectId = objectId;
                            lastGenID = genID;
                            lastObjOffset = tempOffset + 1;
                            currentOffset += OBJ_MARKER.length - 1;
                            endOfObjFound = false;
                        }
                    }
                }
            }
            // check for "endo" as abbreviation for "endobj", as the pdf may be cut off
            // in the middle of the keyword, see PDFBOX-3936.
            // We could possibly implement a more intelligent algorithm if necessary
            else if (nextChar == 'e' && isString(endobjString))
            {
                currentOffset += endobjString.length;
                source.seek(currentOffset);
                if (source.isEOF())
                {
                    endOfObjFound = true;
                }
                else if (isString(endobjRemainingString))
                {
                    currentOffset += endobjRemainingString.length;
                    endOfObjFound = true;
                }
            }
        } while (currentOffset < lastEOFMarker && !source.isEOF());
        if ((lastEOFMarker < Long.MAX_VALUE || endOfObjFound) && lastObjOffset > 0)
        {
            // if the pdf wasn't cut off in the middle or if the last object ends with a "endobj" marker
            // the last object id has to be added here so that it can't get lost as there isn't any subsequent object id
            bfCOSObjectKeyOffsets.put(new COSObjectKey(lastObjectId, lastGenID),
                    lastObjOffset);
        }
        // reestablish origin position
        source.seek(originOffset);
        return bfCOSObjectKeyOffsets;
    }

    /**
     * Search for the offset of the given xref table/stream among those found by a brute force search.
     * 
     * @return the offset of the xref entry
     * @throws IOException if something went wrong
     */
    private long bfSearchForXRef(long xrefOffset) throws IOException
    {
        long newOffset = -1;

        // initialize bfSearchXRefTablesOffsets -> not null
        List<Long> bfSearchXRefTablesOffsets = bfSearchForXRefTables();
        // initialize bfSearchXRefStreamsOffsets -> not null
        List<Long> bfSearchXRefStreamsOffsets = bfSearchForXRefStreams();

        // TODO to be optimized, this won't work in every case
        long newOffsetTable = searchNearestValue(bfSearchXRefTablesOffsets, xrefOffset);
        
        // TODO to be optimized, this won't work in every case
        long newOffsetStream = searchNearestValue(bfSearchXRefStreamsOffsets, xrefOffset);

        // choose the nearest value
        if (newOffsetTable > -1 && newOffsetStream > -1)
        {
            long differenceTable = xrefOffset - newOffsetTable;
            long differenceStream = xrefOffset - newOffsetStream;
            if (Math.abs(differenceTable) > Math.abs(differenceStream))
            {
                newOffset = newOffsetStream;
                bfSearchXRefStreamsOffsets.remove(newOffsetStream);
            }
            else
            {
                newOffset = newOffsetTable;
                bfSearchXRefTablesOffsets.remove(newOffsetTable);
            }
        }
        else if (newOffsetTable > -1)
        {
            newOffset = newOffsetTable;
            bfSearchXRefTablesOffsets.remove(newOffsetTable);
        }
        else if (newOffsetStream > -1)
        {
            newOffset = newOffsetStream;
            bfSearchXRefStreamsOffsets.remove(newOffsetStream);
        }
        return newOffset;
    }

    private long searchNearestValue(List<Long> values, long offset)
    {
        long newValue = -1;
        Long currentDifference = null;
        int currentOffsetIndex = -1;
        int numberOfOffsets = values.size();
        // find the nearest value
        for (int i = 0; i < numberOfOffsets; i++)
        {
            long newDifference = offset - values.get(i);
            // find the nearest offset
            if (currentDifference == null
                    || (Math.abs(currentDifference) > Math.abs(newDifference)))
            {
                currentDifference = newDifference;
                currentOffsetIndex = i;
            }
        }
        if (currentOffsetIndex > -1)
        {
            newValue = values.get(currentOffsetIndex);
        }
        return newValue;
    }

    /**
     * Brute force search for all trailer marker.
     * 
     * @throws IOException if something went wrong
     */
    private boolean bfSearchForTrailer(COSDictionary trailer) throws IOException
    {
        Map<String, COSDictionary> trailerDicts = new HashMap<>();
        long originOffset = source.getPosition();
        source.seek(MINIMUM_SEARCH_OFFSET);
        // search for trailer marker
        long trailerOffset = findString(TRAILER_MARKER);
        while (trailerOffset != -1)
        {
            try
            {
                boolean rootFound = false;
                boolean infoFound = false;
                skipSpaces();
                COSDictionary trailerDict = parseCOSDictionary();
                StringBuilder trailerKeys = new StringBuilder();
                COSObject rootObj = trailerDict.getCOSObject(COSName.ROOT);
                if (rootObj != null)
                {
                    long objNumber = rootObj.getObjectNumber();
                    int genNumber = rootObj.getGenerationNumber();
                    trailerKeys.append(objNumber).append(" ");
                    trailerKeys.append(genNumber).append(" ");
                    rootFound = true;
                }
                COSObject infoObj = trailerDict.getCOSObject(COSName.INFO);
                if (infoObj != null)
                {
                    long objNumber = infoObj.getObjectNumber();
                    int genNumber = infoObj.getGenerationNumber();
                    trailerKeys.append(objNumber).append(" ");
                    trailerKeys.append(genNumber).append(" ");
                    infoFound = true;
                }
                if (rootFound && infoFound)
                {
                    trailerDicts.put(trailerKeys.toString(), trailerDict);
                }
            }
            catch (IOException exception)
            {
                LOG.debug("An exception occurred during brute force search for trailer - ignoring",
                        exception);
            }
            trailerOffset = findString(TRAILER_MARKER);
        }
        source.seek(originOffset);
        // eliminate double entries
        int trailerdictsSize = trailerDicts.size();
        String firstEntry = null;
        if (trailerdictsSize > 0)
        {
            String[] keys = new String[trailerdictsSize];
            trailerDicts.keySet().toArray(keys);
            firstEntry = keys[0];
            for (int i = 1; i < trailerdictsSize; i++)
            {
                if (firstEntry.equals(keys[i]))
                {
                    trailerDicts.remove(keys[i]);
                }
            }
        }
        // continue if one entry is left only
        if (trailerDicts.size() == 1)
        {
            boolean rootFound = false;
            boolean infoFound = false;
            COSDictionary trailerDict = trailerDicts.get(firstEntry);
            COSBase rootObj = trailerDict.getItem(COSName.ROOT);
            if (rootObj instanceof COSObject)
            {
                // check if the dictionary can be dereferenced and is the one we are looking for
                COSBase rootDict = ((COSObject) rootObj).getObject();
                if (rootDict instanceof COSDictionary && isCatalog((COSDictionary) rootDict))
                {
                    rootFound = true;
                }
            }
            COSBase infoObj = trailerDict.getItem(COSName.INFO);
            if (infoObj instanceof COSObject)
            {
                // check if the dictionary can be dereferenced and is the one we are looking for
                COSBase infoDict = ((COSObject) infoObj).getObject();
                if (infoDict instanceof COSDictionary && isInfo((COSDictionary) infoDict))
                {
                    infoFound = true;
                }
            }
            if (rootFound && infoFound)
            {
                trailer.setItem(COSName.ROOT, rootObj);
                trailer.setItem(COSName.INFO, infoObj);
                if (trailerDict.containsKey(COSName.ENCRYPT))
                {
                    COSBase encObj = trailerDict.getItem(COSName.ENCRYPT);
                    if (encObj instanceof COSObject)
                    {
                        // check if the dictionary can be dereferenced
                        // TODO check if the dictionary is an encryption dictionary?
                        COSBase encDict = ((COSObject) encObj).getObject();
                        if (encDict instanceof COSDictionary)
                        {
                            trailer.setItem(COSName.ENCRYPT, encObj);
                        }
                    }
                }
                if (trailerDict.containsKey(COSName.ID))
                {
                    COSBase idObj = trailerDict.getItem(COSName.ID);
                    if (idObj instanceof COSArray)
                    {
                        trailer.setItem(COSName.ID, idObj);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Brute force search for the last EOF marker.
     * 
     * @throws IOException if something went wrong
     */
    private long bfSearchForLastEOFMarker() throws IOException
    {
        long lastEOFMarker = -1;
        long originOffset = source.getPosition();
        source.seek(MINIMUM_SEARCH_OFFSET);
        long tempMarker = findString(EOF_MARKER);
        while (tempMarker != -1)
        {
            try
            {
                // check if the following data is some valid pdf content
                // which most likely indicates that the pdf is linearized,
                // updated or just cut off somewhere in the middle
                skipSpaces();
                if (!isString(XREF_TABLE))
                {
                    readObjectNumber();
                    readGenerationNumber();
                }
            }
            catch (IOException exception)
            {
                // save the EOF marker as the following data is most likely some garbage
                LOG.debug("An exception occurred during brute force for last EOF - ignoring",
                        exception);
                lastEOFMarker = tempMarker;
            }
            tempMarker = findString(EOF_MARKER);
        }
        source.seek(originOffset);
        // no EOF marker found
        if (lastEOFMarker == -1)
        {
            lastEOFMarker = Long.MAX_VALUE;
        }
        return lastEOFMarker;
    }

    /**
     * Brute force search for all object streams.
     * 
     * @throws IOException if something went wrong
     */
    private void bfSearchForObjStreams() throws IOException
    {
        // save origin offset
        long originOffset = source.getPosition();

        Map<Long, COSObjectKey> bfSearchForObjStreamOffsets = bfSearchForObjStreamOffsets();
        Map<COSObjectKey, Long> bfCOSObjectOffsets = getBFCOSObjectOffsets();
        // log warning about skipped stream
        bfSearchForObjStreamOffsets.entrySet().stream() //
                .filter(o -> bfCOSObjectOffsets.get(o.getValue()) == null) //
                .forEach(o -> LOG.warn(
                        "Skipped incomplete object stream:" + o.getValue() + " at " + o.getKey()));

        // collect all stream offsets
        List<Long> objStreamOffsets = bfSearchForObjStreamOffsets.entrySet().stream() //
                .filter(o -> bfCOSObjectOffsets.get(o.getValue()) != null) //
                .filter(o -> o.getKey().equals(bfCOSObjectOffsets.get(o.getValue()))) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toList());
        // add all found compressed objects to the brute force search result
        for (Long offset : objStreamOffsets)
        {
            source.seek(offset);
            long stmObjNumber = readObjectNumber();
            int stmGenNumber = readGenerationNumber();
            readExpectedString(OBJ_MARKER, true);
            COSStream stream = null;
            try
            {
                COSDictionary dict = parseCOSDictionary();
                stream = parseCOSStream(dict);
                if (securityHandler != null)
                {
                    securityHandler.decryptStream(stream, stmObjNumber, stmGenNumber);
                }
                PDFObjectStreamParser objStreamParser = new PDFObjectStreamParser(stream, document);
                Map<Long, Integer> objectNumbers = objStreamParser.readObjectNumbers();
                Map<COSObjectKey, Long> xrefOffset = xrefTrailerResolver.getXrefTable();
                for (Long objNumber : objectNumbers.keySet())
                {
                    COSObjectKey objKey = new COSObjectKey(objNumber, 0);
                    Long existingOffset = bfCOSObjectOffsets.get(objKey);
                    if (existingOffset != null && existingOffset < 0)
                    {
                        // translate stream object key to its offset
                        COSObjectKey objStmKey = new COSObjectKey(Math.abs(existingOffset), 0);
                        existingOffset = bfCOSObjectOffsets.get(objStmKey);
                    }
                    if (existingOffset == null || offset > existingOffset)
                    {
                        bfCOSObjectOffsets.put(objKey, -stmObjNumber);
                        xrefOffset.put(objKey, -stmObjNumber);
                    }
                }
            }
            catch (IOException exception)
            {
                LOG.debug("Skipped corrupt stream: (" + stmObjNumber + " 0 at offset " + offset,
                        exception);
            }
            finally
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
        }
        // restore origin offset
        source.seek(originOffset);
    }

    /**
     * Search for all offsets of object streams within the given pdf
     * 
     * @return a map of all offsets for object streams
     * @throws IOException if something went wrong
     */
    private Map<Long, COSObjectKey> bfSearchForObjStreamOffsets() throws IOException
    {
        HashMap<Long, COSObjectKey> bfSearchObjStreamsOffsets = new HashMap<>();
        source.seek(MINIMUM_SEARCH_OFFSET);
        char[] string = " obj".toCharArray();
        // search for object stream marker
        long positionObjStream = findString(OBJ_STREAM);
        while (positionObjStream != -1)
        {
            // search backwards for the beginning of the object
            long newOffset = -1;
            boolean objFound = false;
            for (int i = 1; i < 40 && !objFound; i++)
            {
                long currentOffset = positionObjStream - (i * 10);
                if (currentOffset > 0)
                {
                    source.seek(currentOffset);
                    for (int j = 0; j < 10; j++)
                    {
                        if (isString(string))
                        {
                            long tempOffset = currentOffset - 1;
                            source.seek(tempOffset);
                            int genID = source.peek();
                            // is the next char a digit?
                            if (isDigit(genID))
                            {
                                tempOffset--;
                                source.seek(tempOffset);
                                if (isSpace())
                                {
                                    int length = 0;
                                    source.seek(--tempOffset);
                                    while (tempOffset > MINIMUM_SEARCH_OFFSET && isDigit())
                                    {
                                        source.seek(--tempOffset);
                                        length++;
                                    }
                                    if (length > 0)
                                    {
                                        source.read();
                                        newOffset = source.getPosition();
                                        long objNumber = readObjectNumber();
                                        int genNumber = readGenerationNumber();
                                        COSObjectKey streamObjectKey = new COSObjectKey(objNumber,
                                                genNumber);
                                        bfSearchObjStreamsOffsets.put(newOffset, streamObjectKey);
                                    }
                                }
                            }
                            LOG.debug("Dictionary start for object stream -> " + newOffset);
                            objFound = true;
                            break;
                        }
                        else
                        {
                            currentOffset++;
                            source.read();
                        }
                    }
                }
            }
            source.seek(positionObjStream + OBJ_STREAM.length);
            positionObjStream = findString(OBJ_STREAM);
        }
        return bfSearchObjStreamsOffsets;
    }

    /**
     * Brute force search for all xref entries (tables).
     * 
     * @throws IOException if something went wrong
     */
    private List<Long> bfSearchForXRefTables() throws IOException
    {
        List<Long> bfSearchXRefTablesOffsets = new ArrayList<>();
        // a pdf may contain more than one xref entry
        source.seek(MINIMUM_SEARCH_OFFSET);
        // search for xref tables
        long newOffset = findString(XREF_TABLE);
        while (newOffset != -1)
        {
            source.seek(newOffset - 1);
            // ensure that we don't read "startxref" instead of "xref"
            if (isWhitespace())
            {
                bfSearchXRefTablesOffsets.add(newOffset);
            }
            source.seek(newOffset + 4);
            newOffset = findString(XREF_TABLE);
        }
        return bfSearchXRefTablesOffsets;
    }

    /**
     * Brute force search for all /XRef entries (streams).
     * 
     * @throws IOException if something went wrong
     */
    private List<Long> bfSearchForXRefStreams() throws IOException
    {
        List<Long> bfSearchXRefStreamsOffsets = new ArrayList<>();
        // a pdf may contain more than one /XRef entry
        source.seek(MINIMUM_SEARCH_OFFSET);
        // search for XRef streams
        String objString = " obj";
        char[] string = objString.toCharArray();
        long xrefOffset = findString(XREF_STREAM);
        while (xrefOffset != -1)
        {
            // search backwards for the beginning of the stream
            long newOffset = -1;
            boolean objFound = false;
            for (int i = 1; i < 40 && !objFound; i++)
            {
                long currentOffset = xrefOffset - (i * 10);
                if (currentOffset > 0)
                {
                    source.seek(currentOffset);
                    for (int j = 0; j < 10; j++)
                    {
                        if (isString(string))
                        {
                            long tempOffset = currentOffset - 1;
                            source.seek(tempOffset);
                            int genID = source.peek();
                            // is the next char a digit?
                            if (isDigit(genID))
                            {
                                tempOffset--;
                                source.seek(tempOffset);
                                if (isSpace())
                                {
                                    int length = 0;
                                    source.seek(--tempOffset);
                                    while (tempOffset > MINIMUM_SEARCH_OFFSET && isDigit())
                                    {
                                        source.seek(--tempOffset);
                                        length++;
                                    }
                                    if (length > 0)
                                    {
                                        source.read();
                                        newOffset = source.getPosition();
                                    }
                                }
                            }
                            LOG.debug("Fixed reference for xref stream " + xrefOffset + " -> "
                                    + newOffset);
                            objFound = true;
                            break;
                        }
                        else
                        {
                            currentOffset++;
                            source.read();
                        }
                    }
                }
            }
            if (newOffset > -1)
            {
                bfSearchXRefStreamsOffsets.add(newOffset);
            }
            source.seek(xrefOffset + 5);
            xrefOffset = findString(XREF_STREAM);
        }
        return bfSearchXRefStreamsOffsets;
    }
    
    /**
     * Rebuild the trailer dictionary if startxref can't be found.
     *  
     * @return the rebuild trailer dictionary
     * 
     * @throws IOException if something went wrong
     */
    private COSDictionary rebuildTrailer() throws IOException
    {
        Map<COSObjectKey, Long> bfCOSObjectOffsets = getBFCOSObjectOffsets();
        // reset trailer resolver
        xrefTrailerResolver.reset();
        // use the found objects to rebuild the trailer resolver
        xrefTrailerResolver.nextXrefObj(0, XRefType.TABLE);
        bfCOSObjectOffsets.forEach(xrefTrailerResolver::setXRef);
        xrefTrailerResolver.setStartxref(0);
        COSDictionary trailer = xrefTrailerResolver.getTrailer();
        document.setTrailer(trailer);
        boolean searchForObjStreamsDone = false;
        if (!bfSearchForTrailer(trailer) && !searchForTrailerItems(trailer))
        {
            // root entry wasn't found, maybe it is part of an object stream
            bfSearchForObjStreams();
            searchForObjStreamsDone = true;
            // search again for the root entry
            searchForTrailerItems(trailer);
        }
        // prepare decryption if necessary
        prepareDecryption();
        if (!searchForObjStreamsDone)
        {
            bfSearchForObjStreams();
        }
        trailerWasRebuild = true;
        return trailer;
    }

    /**
     * Search for the different parts of the trailer dictionary.
     *
     * @param trailer
     * @return true if the root was found, false if not.
     * @throws java.io.IOException if the page tree root is null
     */
    private boolean searchForTrailerItems(COSDictionary trailer) throws IOException
    {
        boolean rootFound = false;
        for (COSObjectKey key : getBFCOSObjectOffsets().keySet())
        {
            COSObject cosObject = document.getObjectFromPool(key);
            COSBase baseObject = cosObject.getObject();

            if (!(baseObject instanceof COSDictionary))
            {
                continue;
            }
            COSDictionary dictionary = (COSDictionary) baseObject;
            // document catalog
            if (isCatalog(dictionary))
            {
                trailer.setItem(COSName.ROOT, cosObject);
                rootFound = true;
            }
            // info dictionary
            else if (isInfo(dictionary))
            {
                trailer.setItem(COSName.INFO, cosObject);
            }
            // encryption dictionary, if existing, is lost
            // We can't run "Algorithm 2" from PDF specification because of missing ID
        }
        return rootFound;
    }

    /**
     * Check if all entries of the pages dictionary are present. Those which can't be dereferenced are removed.
     * 
     * @param root the root dictionary of the pdf
     * @throws java.io.IOException if the page tree root is null
     */
    protected void checkPages(COSDictionary root) throws IOException
    {
        if (trailerWasRebuild)
        {
            // check if all page objects are dereferenced
            COSBase pages = root.getDictionaryObject(COSName.PAGES);
            if (pages instanceof COSDictionary)
            {
                checkPagesDictionary((COSDictionary) pages, new HashSet<>());
            }
        }
        if (!(root.getDictionaryObject(COSName.PAGES) instanceof COSDictionary))
        {
            throw new IOException("Page tree root must be a dictionary");
        }
    }

    private int checkPagesDictionary(COSDictionary pagesDict, Set<COSObject> set)
    {
        // check for kids
        COSBase kids = pagesDict.getDictionaryObject(COSName.KIDS);
        int numberOfPages = 0;
        if (kids instanceof COSArray)
        {
            COSArray kidsArray = (COSArray) kids;
            List<? extends COSBase> kidsList = kidsArray.toList();
            for (COSBase kid : kidsList)
            {
                if (!(kid instanceof COSObject) || set.contains((COSObject) kid))
                {
                    kidsArray.remove(kid);
                    continue;
                }
                COSObject kidObject = (COSObject) kid;
                COSBase kidBaseobject = kidObject.getObject();
                // object wasn't dereferenced -> remove it
                if (kidBaseobject == null || kidBaseobject.equals(COSNull.NULL))
                {
                    LOG.warn("Removed null object " + kid + " from pages dictionary");
                    kidsArray.remove(kid);
                }
                else if (kidBaseobject instanceof COSDictionary)
                {
                    COSDictionary kidDictionary = (COSDictionary) kidBaseobject;
                    COSName type = kidDictionary.getCOSName(COSName.TYPE);
                    if (COSName.PAGES.equals(type))
                    {
                        // process nested pages dictionaries
                        set.add(kidObject);
                        numberOfPages += checkPagesDictionary(kidDictionary, set);
                    }
                    else if (COSName.PAGE.equals(type))
                    {
                        // count pages
                        numberOfPages++;
                    }
                }
            }
        }
        // fix counter
        pagesDict.setInt(COSName.COUNT, numberOfPages);
        return numberOfPages;
    }

    /**
     * Tell if the dictionary is a PDF catalog. Override this for an FDF catalog.
     * 
     * @param dictionary
     * @return true if the given dictionary is a root dictionary
     */
    protected boolean isCatalog(COSDictionary dictionary)
    {
        return COSName.CATALOG.equals(dictionary.getCOSName(COSName.TYPE));
    }

    /**
     * Tell if the dictionary is an info dictionary.
     * 
     * @param dictionary
     * @return true if the given dictionary is an info dictionary
     */
    private boolean isInfo(COSDictionary dictionary)
    {
        if (dictionary.containsKey(COSName.PARENT) || dictionary.containsKey(COSName.A)
                || dictionary.containsKey(COSName.DEST))
        {
            return false;
        }
        if (!dictionary.containsKey(COSName.MOD_DATE) && !dictionary.containsKey(COSName.TITLE)
                && !dictionary.containsKey(COSName.AUTHOR)
                && !dictionary.containsKey(COSName.SUBJECT)
                && !dictionary.containsKey(COSName.KEYWORDS)
                && !dictionary.containsKey(COSName.CREATOR)
                && !dictionary.containsKey(COSName.PRODUCER)
                && !dictionary.containsKey(COSName.CREATION_DATE))
        {
            return false;
        }
        return true;
    }

    /**
     * This will parse the startxref section from the stream. The startxref value is ignored.
     *
     * @return the startxref value or -1 on parsing error
     * @throws IOException If an IO error occurs.
     */
    private long parseStartXref() throws IOException
    {
        long startXref = -1;
        if (isString(STARTXREF))
        {
            readString();
            skipSpaces();
            // This integer is the byte offset of the first object referenced by the xref or xref stream
            startXref = readLong();
        }
        return startXref;
    }
    
    /**
     * Checks if the given string can be found at the current offset.
     * 
     * @param string the bytes of the string to look for
     * @return true if the bytes are in place, false if not
     * @throws IOException if something went wrong
     */
    private boolean isString(byte[] string) throws IOException
    {
        boolean bytesMatching = true;
        long originOffset = source.getPosition();
        for (byte c : string)
        {
            if (source.read() != c)
            {
                bytesMatching = false;
                break;
            }
        }
        source.seek(originOffset);
        return bytesMatching;
    }

    /**
     * Checks if the given string can be found at the current offset.
     * 
     * @param string the bytes of the string to look for
     * @return true if the bytes are in place, false if not
     * @throws IOException if something went wrong
     */
    private boolean isString(char[] string) throws IOException
    {
        boolean bytesMatching = true;
        long originOffset = source.getPosition();
        for (char c : string)
        {
            if (source.read() != c)
            {
                bytesMatching = false;
                break;
            }
        }
        source.seek(originOffset);
        return bytesMatching;
    }

    /**
     * Search for the given string. The search starts at the current position and returns the start position if the
     * string was found. -1 is returned if there isn't any further occurrence of the given string. After returning the
     * current position is either the end of the string or the end of the input.
     * 
     * @param string the string to be searched
     * @return the start position of the found string
     * @throws IOException if something went wrong
     */
    private long findString(char[] string) throws IOException
    {
        long position = -1L;
        int stringLength = string.length;
        int counter = 0;
        int readChar = source.read();
        while (readChar != -1)
        {
            if (readChar == string[counter])
            {
                if (counter == 0)
                {
                    position = source.getPosition();
                }
                counter++;
                if (counter == stringLength)
                {
                    return position;
                }
            }
            else if (counter > 0)
            {
                counter = 0;
                position = -1L;
                continue;
            }
            readChar = source.read();
        }
        return position;
    }
    /**
     * This will parse the trailer from the stream and add it to the state.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseTrailer() throws IOException
    {
        // parse the last trailer.
        long trailerOffset = source.getPosition();
        // PDFBOX-1739 skip extra xref entries in RegisSTAR documents
        if (isLenient)
        {
            int nextCharacter = source.peek();
            while (nextCharacter != 't' && isDigit(nextCharacter))
            {
                if (source.getPosition() == trailerOffset)
                {
                    // warn only the first time
                    LOG.warn("Expected trailer object at offset " + trailerOffset
                            + ", keep trying");
                }
                readLine();
                nextCharacter = source.peek();
            }
        }
        if(source.peek() != 't')
        {
            return false;
        }
        //read "trailer"
        long currentOffset = source.getPosition();
        String nextLine = readLine();
        if( !nextLine.trim().equals( "trailer" ) )
        {
            // in some cases the EOL is missing and the trailer immediately
            // continues with "<<" or with a blank character
            // even if this does not comply with PDF reference we want to support as many PDFs as possible
            // Acrobat reader can also deal with this.
            if (nextLine.startsWith("trailer"))
            {
                // we can't just unread a portion of the read data as we don't know if the EOL consist of 1 or 2 bytes
                int len = "trailer".length();
                // jump back right after "trailer"
                source.seek(currentOffset + len);
            }
            else
            {
                return false;
            }
        }
    
        // in some cases the EOL is missing and the trailer continues with " <<"
        // even if this does not comply with PDF reference we want to support as many PDFs as possible
        // Acrobat reader can also deal with this.
        skipSpaces();
    
        COSDictionary parsedTrailer = parseCOSDictionary();
        xrefTrailerResolver.setTrailer( parsedTrailer );
    
        skipSpaces();
        return true;
    }

    /**
     * Parse the header of a pdf.
     * 
     * @return true if a PDF header was found
     * @throws IOException if something went wrong
     */
    protected boolean parsePDFHeader() throws IOException
    {
        return parseHeader(PDF_HEADER, PDF_DEFAULT_VERSION);
    }

    /**
     * Parse the header of a fdf.
     * 
     * @return true if a FDF header was found
     * @throws IOException if something went wrong
     */
    protected boolean parseFDFHeader() throws IOException
    {
        return parseHeader(FDF_HEADER, FDF_DEFAULT_VERSION);
    }

    private boolean parseHeader(String headerMarker, String defaultVersion) throws IOException
    {
        // read first line
        String header = readLine();
        // some pdf-documents are broken and the pdf-version is in one of the following lines
        if (!header.contains(headerMarker))
        {
            header = readLine();
            while (!header.contains(headerMarker))
            {
                // if a line starts with a digit, it has to be the first one with data in it
                if ((header.length() > 0) && (Character.isDigit(header.charAt(0))))
                {
                    break;
                }
                header = readLine();
            }
        }
    
        // nothing found
        if (!header.contains(headerMarker))
        {
            source.seek(0);
            return false;
        }
    
        //sometimes there is some garbage in the header before the header
        //actually starts, so lets try to find the header first.
        int headerStart = header.indexOf( headerMarker );
    
        // greater than zero because if it is zero then there is no point of trimming
        if ( headerStart > 0 )
        {
            //trim off any leading characters
            header = header.substring( headerStart, header.length() );
        }
    
        // This is used if there is garbage after the header on the same line
        if (header.startsWith(headerMarker) && !header.matches(headerMarker + "\\d.\\d"))
        {
            if (header.length() < headerMarker.length() + 3)
            {
                // No version number at all, set to 1.4 as default
                header = headerMarker + defaultVersion;
                LOG.debug("No version found, set to " + defaultVersion + " as default.");
            }
            else
            {
                String headerGarbage = header.substring(headerMarker.length() + 3, header.length()) + "\n";
                header = header.substring(0, headerMarker.length() + 3);
                source.rewind(headerGarbage.getBytes(StandardCharsets.ISO_8859_1).length);
            }
        }
        float headerVersion = -1;
        try
        {
            String[] headerParts = header.split("-");
            if (headerParts.length == 2)
            {
                headerVersion = Float.parseFloat(headerParts[1]);
            }
        }
        catch (NumberFormatException exception)
        {
            LOG.debug("Can't parse the header version.", exception);
        }
        if (headerVersion < 0)
        {
            if (isLenient)
            {
                headerVersion = 1.7f;
            }
            else
            {
                throw new IOException("Error getting header version: " + header);
            }
        }
        document.setVersion(headerVersion);
        // rewind
        source.seek(0);
        return true;
    }

    /**
     * This will parse the xref table from the stream and add it to the state
     * The XrefTable contents are ignored.
     * @param startByteOffset the offset to start at
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    protected boolean parseXrefTable(long startByteOffset) throws IOException
    {
        if(source.peek() != 'x')
        {
            return false;
        }
        String xref = readString();
        if( !xref.trim().equals( "xref" ) )
        {
            return false;
        }
        
        // check for trailer after xref
        String str = readString();
        byte[] b = str.getBytes(StandardCharsets.ISO_8859_1);
        source.rewind(b.length);
        
        // signal start of new XRef
        xrefTrailerResolver.nextXrefObj( startByteOffset, XRefType.TABLE );
    
        if (str.startsWith("trailer"))
        {
            LOG.warn("skipping empty xref table");
            return false;
        }
        
        // Xref tables can have multiple sections. Each starts with a starting object id and a count.
        while(true)
        {
            String currentLine = readLine();
            String[] splitString = currentLine.split("\\s");
            if (splitString.length != 2)
            {
                LOG.warn("Unexpected XRefTable Entry: " + currentLine);
                return false;
            }
            // first obj id
            long currObjID;
            try
            {
                currObjID = Long.parseLong(splitString[0]);
            }
            catch (NumberFormatException exception)
            {
                LOG.warn("XRefTable: invalid ID for the first object: " + currentLine);
                return false;
            }

            // the number of objects in the xref table
            int count = 0;
            try
            {
                count = Integer.parseInt(splitString[1]);
            }
            catch (NumberFormatException exception)
            {
                LOG.warn("XRefTable: invalid number of objects: " + currentLine);
                return false;
            }
            
            skipSpaces();
            for(int i = 0; i < count; i++)
            {
                if(source.isEOF() || isEndOfName((char)source.peek()))
                {
                    break;
                }
                if(source.peek() == 't')
                {
                    break;
                }
                //Ignore table contents
                currentLine = readLine();
                splitString = currentLine.split("\\s");
                if (splitString.length < 3)
                {
                    LOG.warn("invalid xref line: " + currentLine);
                    break;
                }
                /* This supports the corrupt table as reported in
                 * PDFBOX-474 (XXXX XXX XX n) */
                if(splitString[splitString.length-1].equals("n"))
                {
                    try
                    {
                        long currOffset = Long.parseLong(splitString[0]);
                        // skip 0 offsets
                        if (currOffset > 0)
                        {
                            int currGenID = Integer.parseInt(splitString[1]);
                            COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                            xrefTrailerResolver.setXRef(objKey, currOffset);
                        }
                    }
                    catch(NumberFormatException e)
                    {
                        throw new IOException(e);
                    }
                }
                else if(!splitString[2].equals("f"))
                {
                    throw new IOException("Corrupt XRefTable Entry - ObjID:" + currObjID);
                }
                currObjID++;
                skipSpaces();
            }
            skipSpaces();
            if (!isDigit())
            {
                break;
            }
        }
        return true;
    }

    /**
     * Fills XRefTrailerResolver with data of given stream.
     * Stream must be of type XRef.
     * @param stream the stream to be read
     * @param objByteOffset the offset to start at
     * @param isStandalone should be set to true if the stream is not part of a hybrid xref table
     * @throws IOException if there is an error parsing the stream
     */
    private void parseXrefStream(COSStream stream, long objByteOffset, boolean isStandalone) throws IOException
    {
        // the cross reference stream of a hybrid xref table will be added to the existing one
        // and we must not override the offset and the trailer
        if ( isStandalone )
        {
            xrefTrailerResolver.nextXrefObj( objByteOffset, XRefType.STREAM );
            xrefTrailerResolver.setTrailer( stream );
        }        
        PDFXrefStreamParser parser = new PDFXrefStreamParser(stream, document);
        parser.parse(xrefTrailerResolver);
    }

    /**
     * This will get the encryption dictionary. The document must be parsed before this is called.
     *
     * @return The encryption dictionary of the document that was parsed.
     *
     * @throws IOException If there is an error getting the document.
     */
    public PDEncryption getEncryption() throws IOException
    {
        if (document == null)
        {
            throw new IOException(
                    "You must parse the document first before calling getEncryption()");
        }
        return encryption;
    }

    /**
     * This will get the AccessPermission. The document must be parsed before this is called.
     *
     * @return The access permission of document that was parsed.
     *
     * @throws IOException If there is an error getting the document.
     */
    public AccessPermission getAccessPermission() throws IOException
    {
        if (document == null)
        {
            throw new IOException(
                    "You must parse the document first before calling getAccessPermission()");
        }
        return accessPermission;
    }

    /**
     * Prepare for decryption.
     * 
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException if something went wrong
     */
    private void prepareDecryption() throws IOException
    {
        if (encryption != null)
        {
            return;
        }
        COSBase trailerEncryptItem = document.getTrailer().getItem(COSName.ENCRYPT);
        if (trailerEncryptItem == null || trailerEncryptItem instanceof COSNull)
        {
            return;
        }

        try
        {
            encryption = new PDEncryption(document.getEncryptionDictionary());
            DecryptionMaterial decryptionMaterial;
            if (keyStoreInputStream != null)
            {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(keyStoreInputStream, password.toCharArray());
                decryptionMaterial = new PublicKeyDecryptionMaterial(ks, keyAlias, password);
            }
            else
            {
                decryptionMaterial = new StandardDecryptionMaterial(password);
            }

            securityHandler = encryption.getSecurityHandler();
            securityHandler.prepareForDecryption(encryption, document.getDocumentID(),
                    decryptionMaterial);
            accessPermission = securityHandler.getCurrentAccessPermission();
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException("Error (" + e.getClass().getSimpleName()
                    + ") while creating security handler for decryption", e);
        }
        finally
        {
            if (keyStoreInputStream != null)
            {
                IOUtils.closeQuietly(keyStoreInputStream);
            }
        }
    }

}
