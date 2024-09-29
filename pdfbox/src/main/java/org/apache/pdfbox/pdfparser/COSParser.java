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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
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
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
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

    private static final char[] STARTXREF = { 's','t','a','r','t','x','r','e','f' };

    private static final byte[] ENDSTREAM = { E, N, D, S, T, R, E, A, M };

    private static final byte[] ENDOBJ = { E, N, D, O, B, J };

    protected static final long MINIMUM_SEARCH_OFFSET = 6;
    
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
    private static final char[] EOF_MARKER = { '%', '%', 'E', 'O', 'F' };
    /**
     * obj-marker.
     */
    private static final char[] OBJ_MARKER = { 'o', 'b', 'j' };

    /**
     * file length.
     */
    private final long fileLen;

    /**
     * is parser using auto healing capacity ?
     */
    private boolean isLenient = true;

    protected boolean initialParseDone = false;

    private boolean trailerWasRebuild = false;
    
    private BruteForceParser bruteForceParser = null;
    private PDEncryption encryption = null;
    private final Map<COSObjectKey, Long> xrefTable = new HashMap<>();
    
    /**
     * Intermediate cache. Contains all objects of already read compressed object streams. Objects are removed after
     * dereferencing them.
     */
    private final Map<Long, Map<COSObjectKey, COSBase>> decompressedObjects = new HashMap<>();

    /**
     * The security handler.
     */
    private SecurityHandler<ProtectionPolicy> securityHandler = null;

    /**
     *  how many trailing bytes to read for EOF marker.
     */
    private int readTrailBytes = DEFAULT_TRAIL_BYTECOUNT; 

    private static final Logger LOG = LogManager.getLogger(COSParser.class);

    /**
     * Default constructor.
     *
     * @param source input representing the pdf.
     * 
     * @throws IOException if something went wrong
     */
    public COSParser(RandomAccessRead source) throws IOException
    {
        this(source, null, null, null);
    }

    /**
     * Constructor for encrypted pdfs.
     * 
     * @param source input representing the pdf.
     * @param password password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security
     * @param keyAlias alias to be used for decryption when using public key security
     *
     * @throws IOException if the source data could not be read
     */
    public COSParser(RandomAccessRead source, String password, InputStream keyStore,
            String keyAlias) throws IOException
    {
        this(source, password, keyStore, keyAlias, null);
    }

    /**
     * Constructor for encrypted pdfs.
     * 
     * @param source input representing the pdf.
     * @param password password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security
     * @param keyAlias alias to be used for decryption when using public key security
     * @param streamCacheCreateFunction a function to create an instance of the stream cache
     *
     * @throws IOException if the source data could not be read
     */
    public COSParser(RandomAccessRead source, String password, InputStream keyStore,
            String keyAlias, StreamCacheCreateFunction streamCacheCreateFunction) throws IOException
    {
        super(source);
        this.password = password;
        this.keyAlias = keyAlias;
        fileLen = source.length();
        keyStoreInputStream = keyStore;
        init(streamCacheCreateFunction);
    }

    private void init(StreamCacheCreateFunction streamCacheCreateFunction)
    {
        String eofLookupRangeStr = System.getProperty(SYSPROP_EOFLOOKUPRANGE);
        if (eofLookupRangeStr != null)
        {
            try
            {
                setEOFLookupRange(Integer.parseInt(eofLookupRangeStr));
            }
            catch (NumberFormatException nfe)
            {
                LOG.warn(
                        "System property " + SYSPROP_EOFLOOKUPRANGE + " does not contain an integer value, but: '{}'",
                        eofLookupRangeStr);
            }
        }
        document = new COSDocument(streamCacheCreateFunction, this);
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
                XrefParser xrefParser = new XrefParser(this);
                trailer = xrefParser.parseXref(document, startXRefOffset);
                xrefTable.putAll(xrefParser.getXrefTable());
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
            // reset cross reference table
            xrefTable.clear();
            trailer = getBruteForceParser().rebuildTrailer(xrefTable);
            trailerWasRebuild = true;
        }
        else
        {
            // prepare decryption if necessary
            prepareDecryption();
            // don't use the getter as it creates an instance of BruteForceParser
            if (bruteForceParser != null && bruteForceParser.bfSearchTriggered())
            {
                getBruteForceParser().bfSearchForObjStreams(xrefTable);
            }
        }
        return trailer;
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
                LOG.debug("Missing end of file marker '{}'", new String(EOF_MARKER));
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
    private int lastIndexOf(final char[] pattern, final byte[] buf, final int endOff)
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
        COSObjectKey key = obj.getKey();
        COSBase parsedObj = parseObjectDynamically(key, false);
        if (parsedObj != null)
        {
            parsedObj.setDirect(false);
            parsedObj.setKey(key);
        }
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
     * Parse the object for the given object key.
     * 
     * @param objKey key of object to be parsed
     * @param requireExistingNotCompressedObj if <code>true</code> the object to be parsed must be defined in xref
     * (comment: null objects may be missing from xref) and it must not be a compressed object within object stream
     * (this is used to circumvent being stuck in a loop in a malicious PDF)
     * 
     * @return the parsed object (which is also added to document object)
     * 
     * @throws IOException If an IO error occurs.
     */
    private synchronized COSBase parseObjectDynamically(COSObjectKey objKey,
            boolean requireExistingNotCompressedObj) throws IOException
    {
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
                referencedObject = parseObjectStreamObject(-offsetOrObjstmObNr, objKey);
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
            offsetOrObjstmObNr =  getBruteForceParser().getBFCOSObjectOffsets().get(objKey);
            if (offsetOrObjstmObNr != null)
            {
                LOG.debug("Set missing offset {} for object {}", offsetOrObjstmObNr, objKey);
                document.getXrefTable().put(objKey, offsetOrObjstmObNr);
            }
        }

        // test to circumvent loops with broken documents
        if (requireExistingNotCompressedObj
                && (offsetOrObjstmObNr == null || offsetOrObjstmObNr <= 0))
        {
            throw new IOException("Object must be defined and must not be compressed object: "
                    + objKey.getNumber() + ":" + objKey.getGeneration());
        }
        return offsetOrObjstmObNr;
    }

    private COSBase parseFileObject(Long objOffset, final COSObjectKey objKey)
            throws IOException
    {
        // jump to the object start
        source.seek(objOffset);

        // an indirect object starts with the object number/generation number
        final long readObjNr = readObjectNumber();
        final int readObjGen = readGenerationNumber();
        readObjectMarker();

        // consistency check
        if (readObjNr != objKey.getNumber() || readObjGen != objKey.getGeneration())
        {
            throw new IOException("XREF for " + objKey.getNumber() + ":"
                    + objKey.getGeneration() + " points to wrong object: " + readObjNr
                    + ":" + readObjGen + " at offset " + objOffset);
        }

        skipSpaces();
        COSBase parsedObject = parseDirObject();
        if (parsedObject != null)
        {
            parsedObject.setDirect(false);
            parsedObject.setKey(objKey);
        }
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
                        + objOffset + ").");
            }
            skipSpaces();
            endObjectKey = readLine();

            // we have case with a second 'endstream' before endobj
            if (!endObjectKey.startsWith(ENDOBJ_STRING) && endObjectKey.startsWith(ENDSTREAM_STRING))
            {
                endObjectKey = endObjectKey.substring(9).trim();
                if (endObjectKey.isEmpty())
                {
                    // no other characters in extra endstream line
                    // read next line
                    endObjectKey = readLine();
                }
            }
        }
        else if (securityHandler != null)
        {
            parsedObject = securityHandler.decrypt(parsedObject, objKey.getNumber(),
                    objKey.getGeneration());
            parsedObject.setKey(objKey);
        }

        if (!endObjectKey.startsWith(ENDOBJ_STRING))
        {
            if (isLenient)
            {
                LOG.warn("Object ({}:{}) at offset {} does not end with 'endobj' but with '{}'",
                        readObjNr, readObjGen, objOffset, endObjectKey);
            }
            else
            {
                throw new IOException("Object (" + readObjNr + ":" + readObjGen
                        + ") at offset " + objOffset
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
    protected COSBase parseObjectStreamObject(long objstmObjNr, COSObjectKey key) throws IOException
    {
        Map<COSObjectKey, COSBase> streamObjects = decompressedObjects.computeIfAbsent(objstmObjNr,
                n -> new HashMap<>());
        // did we already read the compressed object stream?
        COSBase objectStreamObject = streamObjects.remove(key);
        if (objectStreamObject != null)
        {
            return objectStreamObject;
        }
        final COSObjectKey objKey = getObjectKey(objstmObjNr, 0);
        final COSBase objstmBaseObj = document.getObjectFromPool(objKey).getObject();
        if (objstmBaseObj instanceof COSStream)
        {
            try
            {
                PDFObjectStreamParser parser = new PDFObjectStreamParser((COSStream) objstmBaseObj,
                        document);
                Map<COSObjectKey, COSBase> allStreamObjects = parser.parseAllObjects();
                objectStreamObject = allStreamObjects.remove(key);
                allStreamObjects.entrySet().stream()
                        .forEach(e -> streamObjects.putIfAbsent(e.getKey(), e.getValue()));
            }
            catch (IOException ex)
            {
                if (isLenient)
                {
                    LOG.error(() -> "object stream {} could not be parsed due to an exception" +
                            objstmObjNr, ex);
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
                LOG.warn("Length object ({}) not found", lengthObj.getKey());
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
                LOG.warn(
                        "The stream doesn't provide any stream length, using fallback readUntilEnd, at offset {}",
                        source.getPosition());
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
            if (streamLengthObj == null || streamLengthObj.longValue() != streamLength)
            {
                dic.setLong(COSName.LENGTH, streamLength);
            }
        }
        String endStream = readString();
        if (endStream.equals("endobj") && isLenient)
        {
            LOG.warn("stream ends with 'endobj' instead of 'endstream' at offset {}",
                    source.getPosition());
            // avoid follow-up warning about missing endobj
            source.rewind(ENDOBJ.length);
        }
        else if (endStream.length() > 9 && isLenient && endStream.startsWith(ENDSTREAM_STRING))
        {
            LOG.warn("stream ends with '{}' instead of 'endstream' at offset {}", endStream,
                    source.getPosition());
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
        long originOffset = source.getPosition();
        if (streamLength <= 0)
        {
            LOG.warn("Invalid stream length: " + streamLength + ", stream start position: "
                    + originOffset);
            return false;
        }
        long expectedEndOfStream = originOffset + streamLength;
        if (expectedEndOfStream > fileLen)
        {
            LOG.warn(
                    "The end of the stream is out of range, using workaround to read the stream, stream start position: {}, length: {}, expected end position: {}",
                    originOffset, streamLength, expectedEndOfStream);
            return false;
        }
        source.seek(expectedEndOfStream);
        skipSpaces();
        boolean endStreamFound = isString(ENDSTREAM);
        source.seek(originOffset);
        if (!endStreamFound)
        {
            LOG.warn(
                    "The end of the stream doesn't point to the correct offset, using workaround to read the stream, stream start position: {}, length: {}, expected end position: {}",
                    originOffset, streamLength, expectedEndOfStream);
            return false;
        }
        return true;
    }

    protected BruteForceParser getBruteForceParser() throws IOException
    {
        if (bruteForceParser == null)
        {
            bruteForceParser = new BruteForceParser(document, this);
        }
        return bruteForceParser;
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
            COSDictionary pages = root.getCOSDictionary(COSName.PAGES);
            if (pages != null)
            {
                checkPagesDictionary(pages, new HashSet<>());
            }
        }
        if (root.getCOSDictionary(COSName.PAGES) == null)
        {
            throw new IOException("Page tree root must be a dictionary");
        }
    }

    private int checkPagesDictionary(COSDictionary pagesDict, Set<COSObject> set)
    {
        // check for kids
        COSArray kidsArray = pagesDict.getCOSArray(COSName.KIDS);
        int numberOfPages = 0;
        if (kidsArray != null)
        {
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
                    LOG.warn("Removed null object {} from pages dictionary", kid);
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
    protected boolean isString(char[] string) throws IOException
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

    protected void readObjectMarker() throws IOException
    {
        readExpectedString(OBJ_MARKER, true);
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
                if ((!header.isEmpty()) && (Character.isDigit(header.charAt(0))))
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
            header = header.substring(headerStart);
        }
    
        // This is used if there is garbage after the header on the same line
        if (header.startsWith(headerMarker) && !header.matches(headerMarker + "\\d.\\d"))
        {
            if (header.length() < headerMarker.length() + 3)
            {
                // No version number at all, set to 1.4 as default
                header = headerMarker + defaultVersion;
                LOG.debug("No version found, set to {} as default.", defaultVersion);
            }
            else
            {
                String headerGarbage = header.substring(headerMarker.length() + 3) + "\n";
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
     * This will get the encryption dictionary. The document must be parsed before this is called.
     *
     * @return The encryption dictionary of the document that was parsed.
     *
     * @throws IOException If there is an error getting the document.
     */
    protected PDEncryption getEncryption() throws IOException
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
    protected AccessPermission getAccessPermission() throws IOException
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
    protected void prepareDecryption() throws IOException
    {
        if (encryption != null)
        {
            return;
        }
        COSDictionary encryptionDictionary = document.getEncryptionDictionary();
        if (encryptionDictionary == null)
        {
            return;
        }

        try
        {
            encryption = new PDEncryption(encryptionDictionary);
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

    /**
     * This will get the security handler. The document must be parsed before this is called.
     *
     * @return The security handler of the document that was parsed.
     */
    protected SecurityHandler<ProtectionPolicy> getSecurityHandler()
    {
        return securityHandler;
    }
}
