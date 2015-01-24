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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * PDF-Parser which first reads startxref and xref tables in order to know valid objects and parse only these objects.
 * Thus it is closer to a conforming parser than the sequential reading of {@link PDFParser}.
 * 
 * First {@link #parse()} must be called before page objects
 * can be retrieved, e.g. {@link #getPDDocument()}.
 * 
 * This class is a much enhanced version of <code>QuickParser</code> presented in <a
 * href="https://issues.apache.org/jira/browse/PDFBOX-1104">PDFBOX-1104</a> by Jeremy Villalobos.
 */
public class NonSequentialPDFParser extends BaseParser
{
    private static final String PDF_HEADER = "%PDF-";
    private static final String FDF_HEADER = "%FDF-";
    
    private static final String PDF_DEFAULT_VERSION = "1.4";
    private static final String FDF_DEFAULT_VERSION = "1.0";

    private static final char[] XREF_TABLE = new char[] { 'x', 'r', 'e', 'f' };
    private static final char[] XREF_STREAM = new char[] { '/', 'X', 'R', 'e', 'f' };
    private static final char[] STARTXREF = new char[] { 's','t','a','r','t','x','r','e','f' };

    private static final long MINIMUM_SEARCH_OFFSET = 6;
    
    private static final int X = 'x';
    
    /**
     * Only parse the PDF file minimally allowing access to basic information.
     */
    public static final String SYSPROP_PARSEMINIMAL = 
            "org.apache.pdfbox.pdfparser.nonSequentialPDFParser.parseMinimal";
    
    /**
     * The range within the %%EOF marker will be searched.
     * Useful if there are additional characters after %%EOF within the PDF. 
     */
    public static final String SYSPROP_EOFLOOKUPRANGE =
            "org.apache.pdfbox.pdfparser.nonSequentialPDFParser.eofLookupRange";

    private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    /**
     * How many trailing bytes to read for EOF marker.
     */
    protected static final int DEFAULT_TRAIL_BYTECOUNT = 2048;
    /**
     * EOF-marker.
     */
    protected static final char[] EOF_MARKER = new char[] { '%', '%', 'E', 'O', 'F' };
    /**
     * StartXRef-marker.
     */
    protected static final char[] STARTXREF_MARKER = new char[] { 's', 't', 'a', 'r', 't', 'x',
            'r', 'e', 'f' };
    /**
     * obj-marker.
     */
    protected static final char[] OBJ_MARKER = new char[] { 'o', 'b', 'j' };

    /**
     * trailer-marker.
     */
    private static final char[] TRAILER_MARKER = new char[] { 't', 'r', 'a', 'i', 'l', 'e', 'r' };
    
    private long trailerOffset;
    private final File pdfFile;
    private long fileLen;
    private final RandomAccessBufferedFileInputStream raStream;

    /**
     * is parser using auto healing capacity ?
     */
    private boolean isLenient = true;

    /**
     * Contains all found objects of a brute force search.
     */
    private Map<String, Long> bfSearchObjectOffsets = null;
    private Map<COSObjectKey, Long> bfSearchCOSObjectKeyOffsets = null;
    private List<Long> bfSearchXRefOffsets = null;

    /**
     * The security handler.
     */
    protected SecurityHandler securityHandler = null;

    private AccessPermission accessPermission;
    private InputStream keyStoreInputStream = null;
    private String keyAlias = null;
    private String password = "";
    
    /**
     *  how many trailing bytes to read for EOF marker.
     */
    private int readTrailBytes = DEFAULT_TRAIL_BYTECOUNT; 
    /**
     * If <code>true</code> object references in catalog are not followed; pro: page objects will be only parsed when
     * needed; cons: some information of catalog might not be available (e.g. outline). Catalog parsing without pages is
     * not an option since a number of entries will also refer to page objects (like OpenAction).
     */
    private final boolean parseMinimalCatalog = "true".equals(System.getProperty(SYSPROP_PARSEMINIMAL));

    private boolean initialParseDone = false;
    private boolean allPagesParsed = false;

    private static final Log LOG = LogFactory.getLog(NonSequentialPDFParser.class);

    private boolean isFDFDocment = false;

    /** 
     * Collects all Xref/trailer objects and resolves them into single
     * object using startxref reference. 
     */
    protected XrefTrailerResolver xrefTrailerResolver = new XrefTrailerResolver();


    /**
     * <code>true</code> if the NonSequentialPDFParser is initialized by a InputStream, in this case a temporary file is
     * created. At the end of the {@linkplain #parse()} method,the temporary file will be deleted.
     */
    private boolean isTmpPDFFile = false;

    /**
     * The prefix for the temp file being used. 
     */
    public static final String TMP_FILE_PREFIX = "tmpPDF";
    
    /**
     * Constructs parser for given file using memory buffer.
     * 
     * @param filename the filename of the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(String filename) throws IOException
    {
        this(new File(filename), null, false);
    }

    /**
     * Constructs parser for given file using memory buffer.
     * 
     * @param filename the filename of the pdf to be parsed.
     * @param useScratchFiles use a buffer for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(String filename, boolean useScratchFiles) throws IOException
    {
        this(new File(filename), null, useScratchFiles);
    }

    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file) throws IOException
    {
        this(file, "", false);
    }

    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * @param useScratchFiles use a buffer for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, boolean useScratchFiles) throws IOException
    {
        this(file, "", useScratchFiles);
    }

    /**
     * Constructs parser for given file using given buffer for temporary storage.
     * 
     * @param file the pdf to be parsed
     * @param decryptionPassword password to be used for decryption
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, String decryptionPassword)
            throws IOException
    {
        this (file, decryptionPassword, false);
    }

    /**
     * Constructs parser for given file using given buffer for temporary storage.
     * 
     * @param file the pdf to be parsed.
     * @param decryptionPassword password to be used for decryption.
     * @param useScratchFiles use a buffer for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, String decryptionPassword, boolean useScratchFiles)
            throws IOException
    {
        this(file, decryptionPassword, null, null, useScratchFiles);
    }

    /**
     * Constructs parser for given file using given buffer for temporary storage.
     * 
     * @param file the pdf to be parsed.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, String decryptionPassword, InputStream keyStore, String alias)
            throws IOException
    {
        this(file, decryptionPassword, keyStore, alias, false);
    }

    /**
     * Constructs parser for given file using given buffer for temporary storage.
     * 
     * @param file the pdf to be parsed.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * @param useScratchFiles use a buffer for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, String decryptionPassword, InputStream keyStore, 
            String alias, boolean useScratchFiles) throws IOException
    {
        super(EMPTY_INPUT_STREAM);
        pdfFile = file;
        raStream = new RandomAccessBufferedFileInputStream(pdfFile);
        password = decryptionPassword;
        keyStoreInputStream = keyStore;
        keyAlias = alias;
        init(useScratchFiles);
    }

    private void init(boolean useScratchFiles) throws IOException
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
                LOG.warn("System property " + SYSPROP_EOFLOOKUPRANGE
                        + " does not contain an integer value, but: '" + eofLookupRangeStr + "'");
            }
        }
        document = new COSDocument(useScratchFiles);
        pdfSource = new PushBackInputStream(raStream, 4096);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input) throws IOException
    {
        this(input, "", false);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param useScratchFiles use a buffer for temporary storage.
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, boolean useScratchFiles) throws IOException
    {
        this(input, "", useScratchFiles);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, String decryptionPassword)
            throws IOException
    {
        this(input, decryptionPassword, false);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param useScratchFiles use a buffer for temporary storage.
     *
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, String decryptionPassword, boolean useScratchFiles)
            throws IOException
    {
        this(input, decryptionPassword, null, null, useScratchFiles);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     *
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, String decryptionPassword, InputStream keyStore, String alias)
            throws IOException
    {
        this(input, decryptionPassword, keyStore, alias, false);
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param decryptionPassword password to be used for decryption.
     * @param keyStore key store to be used for decryption when using public key security 
     * @param alias alias to be used for decryption when using public key security
     * @param useScratchFiles use a buffer for temporary storage.
     *
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, String decryptionPassword, InputStream keyStore,
            String alias, boolean useScratchFiles) throws IOException
    {
        super(EMPTY_INPUT_STREAM);
        pdfFile = createTmpFile(input);
        raStream = new RandomAccessBufferedFileInputStream(pdfFile);
        password = decryptionPassword;
        keyStoreInputStream = keyStore;
        keyAlias = alias;
        init(useScratchFiles);
    }

    /**
     * Create a temporary file with the input stream. If the creation succeed, the {@linkplain #isTmpPDFFile} is set to
     * true. This Temporary file will be deleted at end of the parse method
     * 
     * @param input
     * @return the temporary file
     * @throws IOException If something went wrong.
     */
    private File createTmpFile(InputStream input) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            File tmpFile = File.createTempFile(TMP_FILE_PREFIX, ".pdf");
            fos = new FileOutputStream(tmpFile);
            IOUtils.copy(input, fos);
            isTmpPDFFile = true;
            return tmpFile;
        }
        finally
        {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * This will get the PD document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public PDDocument getPDDocument() throws IOException
    {
        return new PDDocument( getDocument(), this, accessPermission );
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
     * The initial parse will first parse only the trailer, the xrefstart and all xref tables to have a pointer (offset)
     * to all the pdf's objects. It can handle linearized pdfs, which will have an xref at the end pointing to an xref
     * at the beginning of the file. Last the root object is parsed.
     * 
     * @throws IOException If something went wrong.
     */
    protected void initialParse() throws IOException
    {
        COSDictionary trailer = null;
        // parse startxref
        long startXRefOffset = getStartxrefOffset();
        if (startXRefOffset > 0)
        {
            trailer = parseXref(startXRefOffset);
        }
        else if (isFDFDocment || isLenient)
        {
            // signal start of new XRef
            xrefTrailerResolver.nextXrefObj( startXRefOffset, XRefType.TABLE );
            bfSearchForObjects();
            for (COSObjectKey objectKey : bfSearchCOSObjectKeyOffsets.keySet())
            {
                xrefTrailerResolver.setXRef(objectKey, bfSearchCOSObjectKeyOffsets.get(objectKey));
            }
            // parse the last trailer.
            pdfSource.seek(trailerOffset);
            if (!parseTrailer())
            {
                throw new IOException("Expected trailer object at position: "
                        + pdfSource.getOffset());
            }
            xrefTrailerResolver.setStartxref(startXRefOffset);
            trailer = xrefTrailerResolver.getCurrentTrailer();
            document.setTrailer(trailer);
            document.setIsXRefStream(false);
        }
        // ---- prepare decryption if necessary
        prepareDecryption();

        // PDFBOX-1557 - ensure that all COSObject are loaded in the trailer
        // PDFBOX-1606 - after securityHandler has been instantiated
        for (COSBase trailerEntry : trailer.getValues())
        {
            if (trailerEntry instanceof COSObject)
            {
                COSObject tmpObj = (COSObject) trailerEntry;
                parseObjectDynamically(tmpObj, false);
            }
        }
        // parse catalog or root object
        COSObject root = (COSObject) xrefTrailerResolver.getTrailer().getItem(COSName.ROOT);

        if (root == null)
        {
            throw new IOException("Missing root object specification in trailer.");
        }

        COSBase rootObject = parseObjectDynamically(root, false);

        // ---- resolve all objects
        if (isFDFDocment)
        {
            // A FDF doesn't have a catalog, all FDF fields are within the root object
            if (rootObject instanceof COSDictionary)
            {
                parseDictObjects((COSDictionary) rootObject, (COSName[]) null);
                allPagesParsed = true;
                document.setDecrypted();
            }
        }
        else if(!parseMinimalCatalog)
        {
            COSObject catalogObj = document.getCatalog();
            if (catalogObj != null && catalogObj.getObject() instanceof COSDictionary)
            {
                parseDictObjects((COSDictionary) catalogObj.getObject(), (COSName[]) null);
                allPagesParsed = true;
                document.setDecrypted();
            }
        }

        // PDFBOX-1922: read the version again now that all objects have been resolved
        readVersionInTrailer(trailer);
        getDocument().addXRefTable(xrefTrailerResolver.getXrefTable());
        initialParseDone = true;
    }

    /**
     * Resolves all not already parsed objects of a dictionary recursively.
     * 
     * @param dictionaryObject dictionary to be parsed
     * @throws IOException if something went wrong
     * 
     */
    private void parseDictionaryRecursive(COSObject dictionaryObject) throws IOException
    {
        parseObjectDynamically(dictionaryObject, true);
        COSDictionary dictionary = (COSDictionary)dictionaryObject.getObject();
        for(COSBase value : dictionary.getValues())
        {
            if (value instanceof COSObject)
            {
                COSObject object = (COSObject)value;
                if (object.getObject() == null)
                {
                    parseDictionaryRecursive(object);
                }
            }
        }
    }
    /**
     * Prepare for decryption.
     * 
     * @throws IOException if something went wrong
     */
    private void prepareDecryption() throws IOException
    {
        COSBase trailerEncryptItem = document.getTrailer().getItem(COSName.ENCRYPT);
        if (trailerEncryptItem != null && !(trailerEncryptItem instanceof COSNull))
        {
            if (trailerEncryptItem instanceof COSObject)
            {
                COSObject trailerEncryptObj = (COSObject) trailerEncryptItem;
                parseDictionaryRecursive(trailerEncryptObj);
            }
            try
            {
                PDEncryption encryption = new PDEncryption(document.getEncryptionDictionary());

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
            catch (Exception e)
            {
                throw new IOException("Error (" + e.getClass().getSimpleName()
                        + ") while creating security handler for decryption", e);
            }
        }
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
        pdfSource.seek(startXRefOffset);
        long startXrefOffset = parseStartXref();
        // check the startxref offset
        long fixedOffset = checkXRefOffset(startXrefOffset);
        if (fixedOffset > -1)
        {
            startXrefOffset = fixedOffset;
        }
        document.setStartXref(startXrefOffset);
        long prev = startXrefOffset;
        // ---- parse whole chain of xref tables/object streams using PREV reference
        while (prev > -1)
        {
            // seek to xref table
            pdfSource.seek(prev);

            // skip white spaces
            skipSpaces();
            // -- parse xref
            if (pdfSource.peek() == X)
            {
                // xref table and trailer
                // use existing parser to parse xref table
                parseXrefTable(prev);
                // parse the last trailer.
                trailerOffset = pdfSource.getOffset();
                // PDFBOX-1739 skip extra xref entries in RegisSTAR documents
                while (isLenient && pdfSource.peek() != 't')
                {
                    if (pdfSource.getOffset() == trailerOffset)
                    {
                        // warn only the first time
                        LOG.warn("Expected trailer object at position " + trailerOffset
                                + ", keep trying");
                    }
                    readLine();
                }
                if (!parseTrailer())
                {
                    throw new IOException("Expected trailer object at position: "
                            + pdfSource.getOffset());
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
                        streamOffset = (int)fixedOffset;
                        trailer.setInt(COSName.XREF_STM, streamOffset);
                    }
                    pdfSource.seek(streamOffset);
                    skipSpaces();
                    parseXrefObjStream(prev, false); 
                }
                prev = trailer.getInt(COSName.PREV);
                if (prev > -1)
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
                if (prev > -1)
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
        }
        // ---- build valid xrefs out of the xref chain
        xrefTrailerResolver.setStartxref(startXrefOffset);
        COSDictionary trailer = xrefTrailerResolver.getTrailer();
        document.setTrailer(trailer);
        document.setIsXRefStream(XRefType.STREAM == xrefTrailerResolver.getXrefType());
        // check the offsets of all referenced objects
        checkXrefOffsets();
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
        COSStream xrefStream = parseCOSStream(dict);
        parseXrefStream(xrefStream, (int) objByteOffset, isStandalone);

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
    protected final long getStartxrefOffset() throws IOException
    {
        byte[] buf;
        long skipBytes;
        // read trailing bytes into buffer
        fileLen = pdfFile.length();

        FileInputStream fileInputstream = null;
        try
        {
            fileInputstream = new FileInputStream(pdfFile);

            final int trailByteCount = (fileLen < readTrailBytes) ? (int) fileLen : readTrailBytes;
            buf = new byte[trailByteCount];
            fileInputstream.skip(skipBytes = fileLen - trailByteCount);

            int off = 0;
            int readBytes;
            while (off < trailByteCount)
            {
                readBytes = fileInputstream.read(buf, off, trailByteCount - off);
                // in order to not get stuck in a loop we check readBytes (this
                // should never happen)
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
            IOUtils.closeQuietly(fileInputstream);
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
        bufOff = lastIndexOf(STARTXREF_MARKER, buf, bufOff);

        if (bufOff < 0)
        {
            if (isLenient) 
            {
                trailerOffset = lastIndexOf(TRAILER_MARKER, buf, buf.length);
                if (trailerOffset > 0)
                {
                    trailerOffset += skipBytes;
                }
                return -1;
            }
            else
            {
                throw new IOException("Missing 'startxref' marker.");
            }
        }
        return skipBytes + bufOff;
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
    
    private COSDictionary pagesDictionary = null;

    /**
     * Returns PAGES {@link COSDictionary} object or throws {@link IOException} if PAGES dictionary does not exist.
     */
    private COSDictionary getPagesObject() throws IOException
    {
        if (pagesDictionary != null)
        {
            return pagesDictionary;
        }
        COSObject pages = (COSObject) document.getCatalog().getItem(COSName.PAGES);

        if (pages == null)
        {
            throw new IOException("Missing PAGES entry in document catalog.");
        }

        COSBase object = parseObjectDynamically(pages, false);

        if (!(object instanceof COSDictionary))
        {
            throw new IOException("PAGES not a dictionary object, but: "
                    + object.getClass().getSimpleName());
        }

        pagesDictionary = (COSDictionary) object;

        return pagesDictionary;
    }

    /**
     * This will parse the stream and populate the COSDocument object.  This will close
     * the stream when it is done parsing.
     *
     * @throws IOException If there is an error reading from the stream or corrupt data
     * is found.
     */
     public void parse() throws IOException
     {
         // set to false if all is processed
         boolean exceptionOccurred = true; 
         try
         {
            // PDFBOX-1922 read the version header and rewind
            if (!parseHeader(PDF_HEADER, PDF_DEFAULT_VERSION) && !parseHeader(FDF_HEADER, FDF_DEFAULT_VERSION))
            {
                throw new IOException( "Error: Header doesn't contain versioninfo" );
            }

            if (!initialParseDone)
            {
                initialParse();
            }

            // a FDF doesn't have any pages
            if (!isFDFDocment)
            {
                if (!allPagesParsed)
                {
                    final int pageCount = getPageNumber();
                    for (int pNr = 0; pNr < pageCount; pNr++)
                    {
                        getPage(pNr);
                    }
                    allPagesParsed = true;
                    document.setDecrypted();
                }
            }
            exceptionOccurred = false;
        }
        finally
        {
            IOUtils.closeQuietly(pdfSource);
            IOUtils.closeQuietly(keyStoreInputStream);

            deleteTempFile();

            if (exceptionOccurred && document != null)
            {
                try
                {
                    document.close();
                    document = null;
                }
                catch (IOException ioe)
                {
                }
            }
        }
    }

    /**
     * Return the pdf file.
     * 
     * @return the pdf file
     */
    protected File getPdfFile()
    {
        return this.pdfFile;
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
    public void setLenient(boolean lenient)
    {
        if (initialParseDone)
        {
            throw new IllegalArgumentException("Cannot change leniency after parsing");
        }
        this.isLenient = lenient;
    }

    /**
     * Remove the temporary file. A temporary file is created if this class is instantiated with an InputStream
     */
    private void deleteTempFile()
    {
        if (isTmpPDFFile)
        {
            try
            {
                if (!pdfFile.delete())
                {
                    LOG.warn("Temporary file '" + pdfFile.getName() + "' can't be deleted");
                }
            }
            catch (SecurityException e)
            {
                LOG.warn("Temporary file '" + pdfFile.getName() + "' can't be deleted", e);
            }
        }
    }
    
    /**
     * Returns the number of pages in a document.
     * 
     * @return the number of pages.
     * 
     * @throws IOException if PAGES or other needed object is missing
     */
    public int getPageNumber() throws IOException
    {
        int pageCount = getPagesObject().getInt(COSName.COUNT);

        if (pageCount < 0)
        {
            throw new IOException("No page number specified.");
        }
        return pageCount;
    }
    
    /**
     * Returns the page requested with all the objects loaded into it.
     * 
     * @param pageNr starts from 0 to the number of pages.
     * @return the page with the given pagenumber.
     * @throws IOException If something went wrong.
     */
    public PDPage getPage(int pageNr) throws IOException
    {
        getPagesObject();

        // get list of top level pages
        COSArray kids = (COSArray) pagesDictionary.getDictionaryObject(COSName.KIDS);

        if (kids == null)
        {
            throw new IOException("Missing 'Kids' entry in pages dictionary.");
        }

        // get page we are looking for (possibly going recursively into subpages)
        COSObject pageObj = getPageObject(pageNr, kids, 0);

        if (pageObj == null)
        {
            throw new IOException("Page " + pageNr + " not found.");
        }

        COSDictionary pageDict = (COSDictionary) pageObj.getObject();

        // parse all objects necessary to load page.
        if (parseMinimalCatalog && (!allPagesParsed))
        {
            parseDictObjects(pageDict);
        }
        return new PDPage(pageDict);
    }

    /**
     * Returns the object for a specific page. The page tree is made up of kids. The kids have COSArray with COSObjects
     * inside of them. The COSObject can be parsed using the dynamic parsing method We want to only parse the minimum
     * COSObjects and still return a complete page. ready to be used.
     * 
     * @param num the requested page number; numbering starts with 0
     * @param startKids Kids array to start with looking up page number
     * @param startPageCount
     * 
     * @return page object or <code>null</code> if no such page exists
     * 
     * @throws IOException
     */
    private COSObject getPageObject(int num, COSArray startKids, int startPageCount)
            throws IOException
    {
        int curPageCount = startPageCount;
        Iterator<COSBase> kidsIter = startKids.iterator();

        while (kidsIter.hasNext())
        {
            COSObject obj = (COSObject) kidsIter.next();
            COSBase base = obj.getObject();
            if (base == null)
            {
                base = parseObjectDynamically(obj, false);
                obj.setObject(base);
            }

            COSDictionary dic = (COSDictionary) base;
            int count = dic.getInt(COSName.COUNT);
            
            // skip this branch if requested page comes later
            if (count >= 0 && (curPageCount + count) <= num)
            {
                curPageCount += count;
                continue;
            }

            COSArray kids = (COSArray) dic.getDictionaryObject(COSName.KIDS);
            if (kids != null)
            {
                // recursively scan subpages
                COSObject ans = getPageObject(num, kids, curPageCount);
                // if ans is not null, we got what we were looking for
                if (ans != null)
                {
                    return ans;
                }
            }
            else
            {
                // found page?
                if (curPageCount == num)
                {
                    return obj;
                }
                // page has no kids and it is not the page we are looking for
                curPageCount++;
            }
        }
        return null;
    }

    /**
     * Creates a unique object id using object number and object generation
     * number. (requires object number &lt; 2^31))
     */
    private long getObjectId(final COSObject obj)
    {
        return (obj.getObjectNumber().longValue() << 32) | obj.getGenerationNumber().longValue();
    }

    /**
     * Adds all from newObjects to toBeParsedList if it is not an COSObject or
     * we didn't add this COSObject already (checked via addedObjects).
     */
    private void addNewToList(final Queue<COSBase> toBeParsedList,
            final Collection<COSBase> newObjects, final Set<Long> addedObjects)
    {
        for (COSBase newObject : newObjects)
        {
            addNewToList(toBeParsedList, newObject, addedObjects);
        }
    }

    /**
     * Adds newObject to toBeParsedList if it is not an COSObject or we didn't
     * add this COSObject already (checked via addedObjects).
     */
    private void addNewToList(final Queue<COSBase> toBeParsedList, final COSBase newObject,
            final Set<Long> addedObjects)
    {
        if (newObject instanceof COSObject)
        {
            final long objId = getObjectId((COSObject) newObject);
            if (!addedObjects.add(objId))
            {
                return;
            }
        }
        toBeParsedList.add(newObject);
    }

    /**
     * Will parse every object necessary to load a single page from the pdf document. We try our best to order objects
     * according to offset in file before reading to minimize seek operations.
     * 
     * @param dict the COSObject from the parent pages.
     * @param excludeObjects dictionary object reference entries with these names will not be parsed
     * 
     * @throws IOException
     */
    private void parseDictObjects(COSDictionary dict, COSName... excludeObjects) throws IOException
    {
        // ---- create queue for objects waiting for further parsing
        final Queue<COSBase> toBeParsedList = new LinkedList<COSBase>();
        // offset ordered object map
        final TreeMap<Long, List<COSObject>> objToBeParsed = new TreeMap<Long, List<COSObject>>();
        // in case of compressed objects offset points to stmObj
        final Set<Long> parsedObjects = new HashSet<Long>();
        final Set<Long> addedObjects = new HashSet<Long>();

        addExcludedToList(excludeObjects, dict, parsedObjects);
        addNewToList(toBeParsedList, dict.getValues(), addedObjects);

        // ---- go through objects to be parsed
        while (!(toBeParsedList.isEmpty() && objToBeParsed.isEmpty()))
        {
            // -- first get all COSObject from other kind of objects and
            // put them in objToBeParsed; afterwards toBeParsedList is empty
            COSBase baseObj;
            while ((baseObj = toBeParsedList.poll()) != null)
            {
                if (baseObj instanceof COSDictionary)
                {
                    addNewToList(toBeParsedList, ((COSDictionary) baseObj).getValues(), addedObjects);
                }
                else if (baseObj instanceof COSArray)
                {
                    final Iterator<COSBase> arrIter = ((COSArray) baseObj).iterator();
                    while (arrIter.hasNext())
                    {
                        addNewToList(toBeParsedList, arrIter.next(), addedObjects);
                    }
                }
                else if (baseObj instanceof COSObject)
                {
                    COSObject obj = (COSObject) baseObj;
                    long objId = getObjectId(obj);
                    COSObjectKey objKey = new COSObjectKey(obj.getObjectNumber().intValue(), obj
                            .getGenerationNumber().intValue());

                    if (!(parsedObjects.contains(objId) /*
                                                         * || document.hasObjectInPool ( objKey )
                                                         */))
                    {
                        Long fileOffset = xrefTrailerResolver.getXrefTable().get(objKey);
                        // it is allowed that object references point to null,
                        // thus we have to test
                        if (fileOffset != null && fileOffset != 0)
                        {
                            if (fileOffset > 0)
                            {
                                objToBeParsed.put(fileOffset, Collections.singletonList(obj));
                            }
                            else
                            {
                                // negative offset means we have a compressed
                                // object within object stream;
                                // get offset of object stream
                                fileOffset = xrefTrailerResolver.getXrefTable().get(
                                        new COSObjectKey(-fileOffset, 0));
                                if ((fileOffset == null) || (fileOffset <= 0))
                                {
                                    throw new IOException(
                                            "Invalid object stream xref object reference for key '" + objKey + "': "
                                                    + fileOffset);
                                }

                                List<COSObject> stmObjects = objToBeParsed.get(fileOffset);
                                if (stmObjects == null)
                                {
                                    stmObjects = new ArrayList<COSObject>();
                                    objToBeParsed.put(fileOffset, stmObjects);
                                }
                                stmObjects.add(obj);
                            }
                        }
                        else
                        {
                            // NULL object
                            COSObject pdfObject = document.getObjectFromPool(objKey);
                            pdfObject.setObject(COSNull.NULL);
                        }
                    }
                }
            }

            // ---- read first COSObject with smallest offset;
            // resulting object will be added to toBeParsedList
            if (objToBeParsed.isEmpty())
            {
                break;
            }

            for (COSObject obj : objToBeParsed.remove(objToBeParsed.firstKey()))
            {
                COSBase parsedObj = parseObjectDynamically(obj, false);

                obj.setObject(parsedObj);
                addNewToList(toBeParsedList, parsedObj, addedObjects);

                parsedObjects.add(getObjectId(obj));
            }
        }
    }

    // add objects not to be parsed to list of already parsed objects
    private void addExcludedToList(COSName[] excludeObjects, COSDictionary dict, final Set<Long> parsedObjects)
    {
        if (excludeObjects != null)
        {
            for (COSName objName : excludeObjects)
            {
                COSBase baseObj = dict.getItem(objName);
                if (baseObj instanceof COSObject)
                {
                    parsedObjects.add(getObjectId((COSObject) baseObj));
                }
            }
        }
    }

    /**
     * This will parse the next object from the stream and add it to the local state. This is taken from
     * {@link PDFParser} and reduced to parsing an indirect object.
     * 
     * @param obj object to be parsed (we only take object number and generation number for lookup start offset)
     * @param requireExistingNotCompressedObj if <code>true</code> object to be parsed must not be contained within
     * compressed stream
     * @return the parsed object (which is also added to document object)
     * 
     * @throws IOException If an IO error occurs.
     */
    protected final COSBase parseObjectDynamically(COSObject obj,
            boolean requireExistingNotCompressedObj) throws IOException
    {
        return parseObjectDynamically(obj.getObjectNumber().intValue(), obj.getGenerationNumber()
                .intValue(), requireExistingNotCompressedObj);
    }

    /**
     * This will parse the next object from the stream and add it to the local state. 
     * It's reduced to parsing an indirect object.
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
    protected COSBase parseObjectDynamically(int objNr, int objGenNr,
            boolean requireExistingNotCompressedObj) throws IOException
    {
        // ---- create object key and get object (container) from pool
        final COSObjectKey objKey = new COSObjectKey(objNr, objGenNr);
        final COSObject pdfObject = document.getObjectFromPool(objKey);

        if (pdfObject.getObject() == null)
        {
            // not previously parsed
            // ---- read offset or object stream object number from xref table
            Long offsetOrObjstmObNr = xrefTrailerResolver.getXrefTable().get(objKey);

            // sanity test to circumvent loops with broken documents
            if (requireExistingNotCompressedObj
                    && ((offsetOrObjstmObNr == null) || (offsetOrObjstmObNr <= 0)))
            {
                throw new IOException("Object must be defined and must not be compressed object: "
                        + objKey.getNumber() + ":" + objKey.getGeneration());
            }

            if (offsetOrObjstmObNr == null)
            {
                // not defined object -> NULL object (Spec. 1.7, chap. 3.2.9)
                pdfObject.setObject(COSNull.NULL);
            }
            else if (offsetOrObjstmObNr > 0)
            {
                // offset of indirect object in file
                // ---- go to object start
                pdfSource.seek(offsetOrObjstmObNr);

                // ---- we must have an indirect object
                final long readObjNr = readObjectNumber();
                final long readObjGen = readGenerationNumber();
                readExpectedString(OBJ_MARKER, true);

                // ---- consistency check
                if ((readObjNr != objKey.getNumber()) || (readObjGen != objKey.getGeneration()))
                {
                    throw new IOException("XREF for " + objKey.getNumber() + ":"
                            + objKey.getGeneration() + " points to wrong object: " + readObjNr
                            + ":" + readObjGen);
                }

                skipSpaces();
                COSBase pb = parseDirObject();
                String endObjectKey = readString();

                if (endObjectKey.equals(STREAM_STRING))
                {
                    pdfSource.unread(endObjectKey.getBytes(ISO_8859_1));
                    pdfSource.unread(' ');
                    if (pb instanceof COSDictionary)
                    {
                        COSStream stream = parseCOSStream((COSDictionary) pb);

                        if (securityHandler != null)
                        {
                            securityHandler.decryptStream(stream, objNr, objGenNr);
                        }
                        pb = stream;
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
                            endObjectKey = readLine(); // read next line
                        }
                    }
                }
                else if (securityHandler != null)
                {
                    securityHandler.decrypt(pb, objNr, objGenNr);
                }

                pdfObject.setObject(pb);

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
            }
            else
            {
                // xref value is object nr of object stream containing object to
                // be parsed;
                // since our object was not found it means object stream was not
                // parsed so far
                final int objstmObjNr = (int) (-offsetOrObjstmObNr);
                final COSBase objstmBaseObj = parseObjectDynamically(objstmObjNr, 0, true);
                if (objstmBaseObj instanceof COSStream)
                {
                    // parse object stream
                    PDFObjectStreamParser parser = new PDFObjectStreamParser(
                            (COSStream) objstmBaseObj, document);
                    parser.parse();

                    // get set of object numbers referenced for this object
                    // stream
                    final Set<Long> refObjNrs = xrefTrailerResolver
                            .getContainedObjectNumbers(objstmObjNr);

                    // register all objects which are referenced to be contained
                    // in object stream
                    for (COSObject next : parser.getObjects())
                    {
                        COSObjectKey stmObjKey = new COSObjectKey(next);
                        if (refObjNrs.contains(stmObjKey.getNumber()))
                        {
                            COSObject stmObj = document.getObjectFromPool(stmObjKey);
                            stmObj.setObject(next.getObject());
                        }
                    }
                }
            }
        }
        return pdfObject.getObject();
    }
    
    private boolean inGetLength = false;

    /** Returns length value referred to or defined in given object. */
    private COSNumber getLength(final COSBase lengthBaseObj) throws IOException
    {
        if (lengthBaseObj == null)
        {
            return null;
        }

        if (inGetLength)
        {
            throw new IOException("Loop while reading length from " + lengthBaseObj);
        }

        COSNumber retVal = null;

        try
        {
            inGetLength = true;

            // ---- maybe length was given directly
            if (lengthBaseObj instanceof COSNumber)
            {
                retVal = (COSNumber) lengthBaseObj;
            }
            // ---- length in referenced object
            else if (lengthBaseObj instanceof COSObject)
            {
                COSObject lengthObj = (COSObject) lengthBaseObj;

                if (lengthObj.getObject() == null)
                {
                    // not read so far

                    // keep current stream position
                    final long curFileOffset = pdfSource.getOffset();

                    parseObjectDynamically(lengthObj, true);

                    // reset current stream position
                    pdfSource.seek(curFileOffset);

                    if (lengthObj.getObject() == null)
                    {
                        throw new IOException("Length object content was not read.");
                    }
                }

                if (!(lengthObj.getObject() instanceof COSNumber))
                {
                    throw new IOException("Wrong type of referenced length object " + lengthObj
                            + ": " + lengthObj.getObject().getClass().getSimpleName());
                }

                retVal = (COSNumber) lengthObj.getObject();

            }
            else
            {
                throw new IOException("Wrong type of length object: "
                        + lengthBaseObj.getClass().getSimpleName());
            }
        }
        finally
        {
            inGetLength = false;
        }
        return retVal;
    }
    
    private static final int STREAMCOPYBUFLEN = 8192;
    private final byte[] streamCopyBuf = new byte[STREAMCOPYBUFLEN];

    /**
     * This will read a COSStream from the input stream using length attribute within dictionary. If length attribute is
     * a indirect reference it is first resolved to get the stream length. This means we copy stream data without
     * testing for 'endstream' or 'endobj' and thus it is no problem if these keywords occur within stream. We require
     * 'endstream' to be found after stream data is read.
     * 
     * @param dic dictionary that goes with this stream.
     * 
     * @return parsed pdf stream.
     * 
     * @throws IOException if an error occurred reading the stream, like problems with reading length attribute, stream
     * does not end with 'endstream' after data read, stream too short etc.
     */
    @Override
    protected COSStream parseCOSStream(COSDictionary dic) throws IOException
    {
        final COSStream stream = createCOSStream(dic);
        OutputStream out = null;
        try
        {
            readString(); // read 'stream'; this was already tested in
                          // parseObjectsDynamically()

            // ---- skip whitespaces before start of data
            // PDF Ref 1.7, chap. 3.2.7:
            // 'stream' should be followed by either a CRLF (0x0d 0x0a) or LF
            // but nothing else.
            int whitespace = pdfSource.read();
            
            // see brother_scan_cover.pdf, it adds whitespaces
            // after the stream but before the start of the
            // data, so just read those first
            while (whitespace == 0x20)
            {
                whitespace = pdfSource.read();
            }

            if (whitespace == 0x0D)
            {
                whitespace = pdfSource.read();
                if (whitespace != 0x0A)
                {
                    // the spec says this is invalid but it happens in the
                    // real world so we must support it
                    pdfSource.unread(whitespace);
                }
            }
            else if (whitespace != 0x0A)
            {
                // no whitespace after 'stream'; PDF ref. says 'should' so
                // that is ok
                pdfSource.unread(whitespace);
            }

            /*
             * This needs to be dic.getItem because when we are parsing, the underlying object might still be null.
             */
            COSNumber streamLengthObj = getLength(dic.getItem(COSName.LENGTH));
            if (streamLengthObj == null)
            {
                if (isLenient)
                {
                   LOG.warn("The stream doesn't provide any stream length, using fallback readUntilEnd"); 
                }
                else
                {
                    throw new IOException("Missing length for stream.");
                }
            }

            boolean useReadUntilEnd = false;
            // ---- get output stream to copy data to
            if (streamLengthObj != null && validateStreamLength(streamLengthObj.longValue()))
            {
                out = stream.createFilteredStream(streamLengthObj);
                long remainBytes = streamLengthObj.longValue();
                int bytesRead = 0;
                while (remainBytes > 0)
                {
                    final int readBytes = pdfSource
                            .read(streamCopyBuf,
                                    0,
                                    (remainBytes > STREAMCOPYBUFLEN) ? STREAMCOPYBUFLEN : (int) remainBytes);
                    if (readBytes <= 0)
                    {
                        useReadUntilEnd = true;
                        out.close();
                        pdfSource.unread(bytesRead);
                        break;
                    }
                    out.write(streamCopyBuf, 0, readBytes);
                    remainBytes -= readBytes;
                    bytesRead += readBytes;
                }
            }
            else
            {
                useReadUntilEnd = true;
            }
            if (useReadUntilEnd)
            {
                out = stream.createFilteredStream();
                readUntilEndStream(new EndstreamOutputStream(out));
            }
            String endStream = readString();
            if (endStream.equals("endobj") && isLenient)
            {
                LOG.warn("stream ends with 'endobj' instead of 'endstream' at offset "
                        + pdfSource.getOffset());
                // avoid follow-up warning about missing endobj
                pdfSource.unread(ENDOBJ);
            }
            else if (endStream.length() > 9 && isLenient && endStream.substring(0,9).equals(ENDSTREAM_STRING))
            {
                LOG.warn("stream ends with '" + endStream + "' instead of 'endstream' at offset "
                        + pdfSource.getOffset());
                // unread the "extra" bytes
                pdfSource.unread(endStream.substring(9).getBytes(ISO_8859_1));
            }
            else if (!endStream.equals(ENDSTREAM_STRING))
            {
                throw new IOException(
                        "Error reading stream, expected='endstream' actual='"
                        + endStream + "' at offset " + pdfSource.getOffset());
            }
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
        return stream;
    }

    private boolean validateStreamLength(long streamLength) throws IOException
    {
        boolean streamLengthIsValid = true;
        long originOffset = pdfSource.getOffset();
        long expectedEndOfStream = originOffset + streamLength;
        if (expectedEndOfStream > fileLen)
        {
            streamLengthIsValid = false;
            LOG.error("The end of the stream is out of range, using workaround to read the stream");
            LOG.error("Stream start offset: " + originOffset);
            LOG.error("Expected endofstream offset: " + expectedEndOfStream);
        }
        else
        {
            pdfSource.seek(expectedEndOfStream);
            skipSpaces();
            if (!isString(ENDSTREAM))
            {
                streamLengthIsValid = false;
                LOG.error("The end of the stream doesn't point to the correct offset, using workaround to read the stream");
                LOG.error("Stream start offset: " + originOffset);
                LOG.error("Expected endofstream offset: " + expectedEndOfStream);
            }
            pdfSource.seek(originOffset);
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
        pdfSource.seek(startXRefOffset-1);
        // save the previous character
        int previous = pdfSource.read();
        if (pdfSource.peek() == X && isString(XREF_TABLE))
        {
            return startXRefOffset;
        }
        // the previous character has to be a whitespace
        if (isWhitespace(previous))
        {
            int nextValue = pdfSource.peek();
            // maybe there isn't a xref table but a xref stream
            // is the next character a digit?
            if (nextValue > 47 && nextValue < 58)
            {
                try
                {
                    // Maybe it's a XRef stream
                    readObjectNumber();
                    readGenerationNumber();
                    readExpectedString(OBJ_MARKER, true);
                    pdfSource.seek(startXRefOffset);
                    return startXRefOffset;
                }
                catch (IOException exception)
                {
                    // there wasn't an object of a xref stream
                    // try to repair the offset
                    pdfSource.seek(startXRefOffset);
                }
            }
        }
        // try to find a fixed offset
        return calculateXRefFixedOffset(startXRefOffset);
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
        LOG.error("Can't find the object axref table/stream at offset " + objectOffset);
        return 0;
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
        if (xrefOffset != null)
        {
            for (Entry<COSObjectKey, Long> objectEntry : xrefOffset.entrySet())
            {
                COSObjectKey objectKey = objectEntry.getKey();
                Long objectOffset = objectEntry.getValue();
                // a negative offset number represents a object number itself
                // see type 2 entry in xref stream
                if (objectOffset != null && objectOffset >= 0)
                {
                    long objectNr = objectKey.getNumber();
                    long objectGen = objectKey.getGeneration();
                    String objectString = createObjectString(objectNr, objectGen);
                    if (!checkObjectId(objectString, objectOffset))
                    {
                        long newOffset = bfSearchForObject(objectString);
                        if (newOffset > -1)
                        {
                            xrefOffset.put(objectKey, newOffset);
                            LOG.debug("Fixed reference for object " + objectNr + " " + objectGen
                                    + " " + objectOffset + " -> " + newOffset);
                        }
                        else
                        {
                            LOG.error("Can't find the object " + objectNr + " " + objectGen
                                    + " (origin offset " + objectOffset + ")");
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if the given string can be found at the given offset.
     * 
     * @param objectString the string we are looking for
     * @param offset the given where to look
     * @return returns true if the given string can be found at the givwen offset
     * @throws IOException if something went wrong
     */
    private boolean checkObjectId(String objectString, long offset) throws IOException
    {
        long originOffset = pdfSource.getOffset();
        pdfSource.seek(offset);
        boolean objectFound = isString(objectString.getBytes(ISO_8859_1));
        pdfSource.seek(originOffset);
        return objectFound;
    }

    /**
     * Create a string for the given object id.
     * 
     * @param objectID the object id
     * @param genID the generation id
     * @return the generated string
     */
    private String createObjectString(long objectID, long genID)
    {
        return Long.toString(objectID) + " " + Long.toString(genID) + " obj";
    }

    /**
     * Search for the offset of the given object among the objects found by a brute force search.
     * 
     * @param objectString the object we are looking for
     * @return the offset of the object
     * @throws IOException if something went wrong
     */
    private long bfSearchForObject(String objectString) throws IOException
    {
        long newOffset = -1;
        bfSearchForObjects();
        if (bfSearchObjectOffsets.containsKey(objectString))
        {
            newOffset = bfSearchObjectOffsets.get(objectString);
        }
        return newOffset;
    }

    /**
     * Brute force search for every object in the pdf.
     *   
     * @throws IOException if something went wrong
     */
    private void bfSearchForObjects() throws IOException
    {
        if (bfSearchObjectOffsets == null)
        {
            bfSearchObjectOffsets = new HashMap<String, Long>();
            bfSearchCOSObjectKeyOffsets = new HashMap<COSObjectKey, Long>();
            long originOffset = pdfSource.getOffset();
            long currentOffset = MINIMUM_SEARCH_OFFSET;
            String objString = " obj";
            char[] string = objString.toCharArray();
            do
            {
                pdfSource.seek(currentOffset);
                if (isString(string))
                {
                    long tempOffset = currentOffset - 1;
                    pdfSource.seek(tempOffset);
                    int genID = pdfSource.peek();
                    // is the next char a digit?
                    if (genID > 47 && genID < 58)
                    {
                        genID -= 48;
                        tempOffset--;
                        pdfSource.seek(tempOffset);
                        if (isSpace())
                        {
                            while (tempOffset > MINIMUM_SEARCH_OFFSET && isSpace())
                            {
                                pdfSource.seek(--tempOffset);
                            }
                            int length = 0;
                            while (tempOffset > MINIMUM_SEARCH_OFFSET && isDigit())
                            {
                                pdfSource.seek(--tempOffset);
                                length++;
                            }
                            if (length > 0)
                            {
                                pdfSource.read();
                                byte[] objIDBytes = pdfSource.readFully(length);
                                String objIdString = new String(objIDBytes, 0,
                                        objIDBytes.length, ISO_8859_1);
                                Long objectID;
                                try
                                {
                                    objectID = Long.valueOf(objIdString);
                                }
                                catch (NumberFormatException exception)
                                {
                                    objectID = null;
                                }
                                if (objectID != null)
                                {
                                    bfSearchObjectOffsets.put(
                                            createObjectString(objectID, genID), ++tempOffset);
                                    bfSearchCOSObjectKeyOffsets.put(new COSObjectKey(objectID, genID), tempOffset);
                                }
                            }
                        }
                    }
                }
                currentOffset++;
            }
            while (!pdfSource.isEOF());
            // reestablish origin position
            pdfSource.seek(originOffset);
        }
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
        bfSearchForXRefs();
        if (bfSearchXRefOffsets != null)
        {
            long currentDifference = -1;
            int currentOffsetIndex = -1;
            int numberOfOffsets = bfSearchXRefOffsets.size();
            // find the most likely value
            // TODO to be optimized, this won't work in every case
            for (int i = 0; i < numberOfOffsets; i++)
            {
                long newDifference = xrefOffset - bfSearchXRefOffsets.get(i);
                // find the nearest offset
                if (currentDifference == -1
                        || (Math.abs(currentDifference) > Math.abs(newDifference)))
                {
                    currentDifference = newDifference;
                    currentOffsetIndex = i;
                }
            }
            if (currentOffsetIndex > -1)
            {
                newOffset = bfSearchXRefOffsets.remove(currentOffsetIndex);
            }
        }
        return newOffset;
    }

    /**
     * Brute force search for all xref entries.
     * 
     * @throws IOException if something went wrong
     */
    private void bfSearchForXRefs() throws IOException
    {
        if (bfSearchXRefOffsets == null)
        {
            // a pdf may contain more than one xref entry
            bfSearchXRefOffsets = new Vector<Long>();
            long originOffset = pdfSource.getOffset();
            pdfSource.seek(MINIMUM_SEARCH_OFFSET);
            // search for xref tables
            while (!pdfSource.isEOF())
            {
                if (isString(XREF_TABLE))
                {
                    long newOffset = pdfSource.getOffset();
                    pdfSource.seek(newOffset - 1);
                    // ensure that we don't read "startxref" instead of "xref"
                    if (isWhitespace())
                    {
                        bfSearchXRefOffsets.add(newOffset);
                    }
                    pdfSource.seek(newOffset + 4);
                }
                pdfSource.read();
            }
            pdfSource.seek(MINIMUM_SEARCH_OFFSET);
            // search for XRef streams
            String objString = " obj";
            char[] string = objString.toCharArray();
            while (!pdfSource.isEOF())
            {
                if (isString(XREF_STREAM))
                {
                    // search backwards for the beginning of the stream
                    long newOffset = -1;
                    long xrefOffset = pdfSource.getOffset();
                    boolean objFound = false;
                    for (int i = 1; i < 30 && !objFound; i++)
                    {
                        long currentOffset = xrefOffset - (i * 10);
                        if (currentOffset > 0)
                        {
                            pdfSource.seek(currentOffset);
                            for (int j = 0; j < 10; j++)
                            {
                                if (isString(string))
                                {
                                    long tempOffset = currentOffset - 1;
                                    pdfSource.seek(tempOffset);
                                    int genID = pdfSource.peek();
                                    // is the next char a digit?
                                    if (isDigit(genID))
                                    {
                                        genID -= 48;
                                        tempOffset--;
                                        pdfSource.seek(tempOffset);
                                        if (isSpace())
                                        {
                                            int length = 0;
                                            pdfSource.seek(--tempOffset);
                                            while (tempOffset > MINIMUM_SEARCH_OFFSET && isDigit())
                                            {
                                                pdfSource.seek(--tempOffset);
                                                length++;
                                            }
                                            if (length > 0)
                                            {
                                                pdfSource.read();
                                                newOffset = pdfSource.getOffset();
                                            }
                                        }
                                    }
                                    LOG.debug("Fixed reference for xref stream " + xrefOffset
                                            + " -> " + newOffset);
                                    objFound = true;
                                    break;
                                }
                                else
                                {
                                    currentOffset++;
                                    pdfSource.read();
                                }
                            }
                        }
                    }
                    if (newOffset > -1)
                    {
                        bfSearchXRefOffsets.add(newOffset);
                    }
                    pdfSource.seek(xrefOffset + 5);
                }
                pdfSource.read();
            }
            pdfSource.seek(originOffset);
        }
    }

    /**
     * This will parse the startxref section from the stream.
     * The startxref value is ignored.
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
     * This will parse the trailer from the stream and add it to the state.
     *
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseTrailer() throws IOException
    {
        if(pdfSource.peek() != 't')
        {
            return false;
        }
        //read "trailer"
        long currentOffset = pdfSource.getOffset();
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
                pdfSource.seek(currentOffset + len);
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
    
        // The version can also be specified within the document /Catalog
        readVersionInTrailer(parsedTrailer);
    
        skipSpaces();
        return true;
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
            pdfSource.seek(0);
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
        if (header.startsWith(headerMarker))
        {
            if (!header.matches(headerMarker + "\\d.\\d"))
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
                    pdfSource.unread(headerGarbage.getBytes(ISO_8859_1));
                }
            }
        }
        document.setHeaderString(header);
        try
        {
            if (header.startsWith( headerMarker ))
            {
                float pdfVersion = Float. parseFloat(
                        header.substring( headerMarker.length(), Math.min( header.length(), headerMarker.length()+3) ) );
                document.setVersion( pdfVersion );
            }
        }
        catch ( NumberFormatException e )
        {
            throw new IOException( "Error getting version: " + e.getMessage(), e );
        }
        // rewind
        pdfSource.seek(0);
        isFDFDocment = FDF_HEADER.equals(headerMarker);
        return true;
    }

    /**
     * The document catalog can also have a /Version parameter which overrides the version specified
     * in the header if, and only if it is greater.
     *
     * @param parsedTrailer the parsed catalog in the trailer
     */
    private void readVersionInTrailer(COSDictionary parsedTrailer)
    {
        COSObject root = (COSObject) parsedTrailer.getItem(COSName.ROOT);
        if (root != null)
        {
            COSBase item = root.getItem(COSName.VERSION);
            if (item instanceof COSName)
            {
                COSName version = (COSName) item;
                float trailerVersion = Float.valueOf(version.getName());
                if (trailerVersion > document.getVersion())
                {
                    document.setVersion(trailerVersion);
                }
            }
            else if (item != null)
            {
                LOG.warn("Incorrect /Version entry is ignored: " + item);
            }
        }
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
        if(pdfSource.peek() != 'x')
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
        byte[] b = str.getBytes(ISO_8859_1);
        pdfSource.unread(b, 0, b.length);
        
        // signal start of new XRef
        xrefTrailerResolver.nextXrefObj( startByteOffset, XRefType.TABLE );
    
        if (str.startsWith("trailer"))
        {
            LOG.warn("skipping empty xref table");
            return false;
        }
        
        /**
         * Xref tables can have multiple sections.
         * Each starts with a starting object id and a count.
         */
        while(true)
        {
            long currObjID = readObjectNumber(); // first obj id
            long count = readLong(); // the number of objects in the xref table
            skipSpaces();
            for(int i = 0; i < count; i++)
            {
                if(pdfSource.isEOF() || isEndOfName((char)pdfSource.peek()))
                {
                    break;
                }
                if(pdfSource.peek() == 't')
                {
                    break;
                }
                //Ignore table contents
                String currentLine = readLine();
                String[] splitString = currentLine.split("\\s");
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
                        int currGenID = Integer.parseInt(splitString[1]);
                        COSObjectKey objKey = new COSObjectKey(currObjID, currGenID);
                        xrefTrailerResolver.setXRef(objKey, currOffset);
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
        PDFXrefStreamParser parser =
                new PDFXrefStreamParser( stream, document, xrefTrailerResolver );
        parser.parse();
    }

    /**
     * This will get the document that was parsed.  parse() must be called before this is called.
     * When you are done with this document you must call close() on it to release
     * resources.
     *
     * @return The document that was parsed.
     *
     * @throws IOException If there is an error getting the document.
     */
    public COSDocument getDocument() throws IOException
    {
        if( document == null )
        {
            throw new IOException( "You must call parse() before calling getDocument()" );
        }
        return document;
    }

    /**
     * This will get the FDF document that was parsed.  When you are done with
     * this document you must call close() on it to release resources.
     *
     * @return The document at the PD layer.
     *
     * @throws IOException If there is an error getting the document.
     */
    public FDFDocument getFDFDocument() throws IOException
    {
        return new FDFDocument( getDocument() );
    }
}
