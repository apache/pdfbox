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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

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
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandlersManager;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * PDFParser which first reads startxref and xref tables in order to know valid
 * objects and parse only these objects. Thus it is closer to a conforming
 * parser than the sequential reading of {@link PDFParser}.
 * 
 * This class can be used as a {@link PDFParser} replacement. First
 * {@link #parse()} must be called before page objects can be retrieved, e.g.
 * {@link #getPDDocument()}.
 * 
 * This class is a much enhanced version of <code>QuickParser</code> presented
 * in <a
 * href="https://issues.apache.org/jira/browse/PDFBOX-1104">PDFBOX-1104</a> by
 * Jeremy Villalobos.
 */
public class NonSequentialPDFParser extends PDFParser
{

	private static final byte[] XREF = new byte[] { 'x', 'r', 'e', 'f' };

	private static final int E = 'e';
    private static final int N = 'n';
    private static final int X = 'x';

    public static final String SYSPROP_PARSEMINIMAL = "org.apache.pdfbox.pdfparser.nonSequentialPDFParser.parseMinimal";
    public static final String SYSPROP_EOFLOOKUPRANGE = "org.apache.pdfbox.pdfparser.nonSequentialPDFParser.eofLookupRange";

    private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    protected static final int DEFAULT_TRAIL_BYTECOUNT = 2048;
    /**
     * EOF-marker.
     */
    protected static final char[] EOF_MARKER = new char[] { '%', '%', 'E', 'O', 'F' };
    /**
     * StartXRef-marker.
     */
    protected static final char[] STARTXREF_MARKER = new char[] { 's', 't', 'a', 'r', 't', 'x', 'r', 'e', 'f' };
    /**
     * obj-marker.
     */
    protected static final char[] OBJ_MARKER = new char[] { 'o', 'b', 'j' };

    private final File pdfFile;
    private long fileLen;
    private final RandomAccessBufferedFileInputStream raStream;

    /**
     * The security handler.
     */
    protected SecurityHandler securityHandler = null;

    private String keyStoreFilename = null;
    private String alias = null;
    private String password = "";
    private int readTrailBytes = DEFAULT_TRAIL_BYTECOUNT; // how many trailing
                                                          // bytes to read for
                                                          // EOF marker

    /**
     * If <code>true</code> object references in catalog are not followed; pro:
     * page objects will be only parsed when needed; cons: some information of
     * catalog might not be available (e.g. outline). Catalog parsing without
     * pages is not an option since a number of entries will also refer to page
     * objects (like OpenAction).
     */
    private boolean parseMinimalCatalog = "true".equals(System.getProperty(SYSPROP_PARSEMINIMAL));

    private boolean initialParseDone = false;
    private boolean allPagesParsed = false;

    private static final Log LOG = LogFactory.getLog(NonSequentialPDFParser.class);

    /**
     * <code>true</code> if the NonSequentialPDFParser is initialized by a
     * InputStream, in this case a temporary file is created. At the end of the
     * {@linkplain #parse()} method,the temporary file will be deleted.
     */
    private boolean isTmpPDFFile = false;

    public static final String TMP_FILE_PREFIX = "tmpPDF";

    // ------------------------------------------------------------------------
    /**
     * Constructs parser for given file using memory buffer.
     * 
     * @param filename the filename of the pdf to be parsed
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(String filename) throws IOException
    {
        this(new File(filename), null);
    }

    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * @param raBuf the buffer to be used for parsing
     * 
     * @throws IOException If something went wrong.
     */
    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * @param raBuf the buffer to be used for parsing
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, RandomAccess raBuf) throws IOException
    {
        this(file, raBuf, "");
    }

    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * @param raBuf the buffer to be used for parsing
     * 
     * @throws IOException If something went wrong.
     */
    /**
     * Constructs parser for given file using given buffer for temporary
     * storage.
     * 
     * @param file the pdf to be parsed
     * @param raBuf the buffer to be used for parsing
     * @param decryptionPassword password to be used for decryption
     * 
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(File file, RandomAccess raBuf, String decryptionPassword) throws IOException
    {
        super(EMPTY_INPUT_STREAM, null, false);
        pdfFile = file;
        raStream = new RandomAccessBufferedFileInputStream(pdfFile);
        init(file, raBuf, decryptionPassword);
    }

    private void init(File file, RandomAccess raBuf, String decryptionPassword) throws IOException
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
                LOG.warn("System property " + SYSPROP_EOFLOOKUPRANGE + " does not contain an integer value, but: '"
                        + eofLookupRangeStr + "'");
            }
        }

        setDocument((raBuf == null) ? new COSDocument(new RandomAccessBuffer(), false) : new COSDocument(raBuf, false));

        pdfSource = new PushBackInputStream(raStream, 4096);

        password = decryptionPassword;
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input) throws IOException
    {
        this(input, null, "");
    }

    /**
     * Constructor.
     * 
     * @param input input stream representing the pdf.
     * @param raBuf the buffer to be used for parsing
     * @param decryptionPassword password to be used for decryption.
     * @throws IOException If something went wrong.
     */
    public NonSequentialPDFParser(InputStream input, RandomAccess raBuf, String decryptionPassword) throws IOException
    {
        super(EMPTY_INPUT_STREAM, null, false);
        pdfFile = createTmpFile(input);
        raStream = new RandomAccessBufferedFileInputStream(pdfFile);
        init(pdfFile, raBuf, decryptionPassword);
    }

    /**
     * Create a temporary file with the input stream. If the creation succeed,
     * the {@linkplain #isTmpPDFFile} is set to true. This Temporary file will
     * be deleted at end of the parse method
     * 
     * @param input
     * @return
     * @throws IOException If something went wrong.
     */
    private File createTmpFile(InputStream input) throws IOException
    {
        File tmpFile = null;
        FileOutputStream fos = null;
        try
        {
            tmpFile = File.createTempFile(TMP_FILE_PREFIX, ".pdf");
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

    // ------------------------------------------------------------------------
    /**
     * Sets how many trailing bytes of PDF file are searched for EOF marker and
     * 'startxref' marker. If not set we use default value
     * {@link #DEFAULT_TRAIL_BYTECOUNT}.
     * 
     * <p<We check that new value is at least 16. However for practical use
     * cases this value should not be lower than 1000; even 2000 was found to
     * not be enough in some cases where some trailing garbage like HTML
     * snippets followed the EOF marker.</p>
     * 
     * <p>In case system property {@link #SYSPROP_EOFLOOKUPRANGE} is defined
     * this value will be set on initialization but can be overwritten
     * later.</p>
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

    // ------------------------------------------------------------------------
    /**
     * The initial parse will first parse only the trailer, the xrefstart and
     * all xref tables to have a pointer (offset) to all the pdf's objects. It
     * can handle linearized pdfs, which will have an xref at the end pointing
     * to an xref at the beginning of the file. Last the root object is parsed.
     * 
     * @throws IOException If something went wrong.
     */
    protected void initialParse() throws IOException
    {
        // ---- parse startxref
        setPdfSource(getStartxrefOffset());
        parseStartXref();

        long startXrefOffset = document.getStartXref();
        // check the startxref offset
        startXrefOffset -= calculateFixingOffset(startXrefOffset);
        document.setStartXref(startXrefOffset);
        long prev = startXrefOffset;
        // ---- parse whole chain of xref tables/object streams using PREV
        // reference
        while (prev > -1)
        {
            // seek to xref table
            setPdfSource(prev);

            // skip white spaces
            skipSpaces();
            // -- parse xref
            if (pdfSource.peek() == X)
            {
                // xref table and trailer
                // use existing parser to parse xref table
                parseXrefTable(prev);
                // parse the last trailer.
                if (!parseTrailer())
                {
                    throw new IOException("Expected trailer object at position: " + pdfSource.getOffset());
                }
                COSDictionary trailer = xrefTrailerResolver.getCurrentTrailer();
                prev = trailer.getInt(COSName.PREV);
                if (prev > -1)
                {
                	// check the xref table reference
                	long fixingOffset = calculateFixingOffset(prev);
	            	if (fixingOffset != 0)
	            	{
	            		prev -= fixingOffset;
	            		trailer.setLong(COSName.PREV, prev);
	            	} 
                }            	
            }
            else
            {
                // parse xref stream
                prev = parseXrefObjStream(prev);
                if (prev > -1)
                {
                	// check the xref table reference
                	long fixingOffset = calculateFixingOffset(prev);
	            	if (fixingOffset != 0)
	            	{
	            		prev -= fixingOffset;
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

        // check the offsets of all referenced objects 
        checkXrefOffsets();
        
        // ---- prepare encryption if necessary
        COSBase trailerEncryptItem = document.getTrailer().getItem(COSName.ENCRYPT);
        if (trailerEncryptItem != null)
        {
            if (trailerEncryptItem instanceof COSObject)
            {
                COSObject trailerEncryptObj = (COSObject) trailerEncryptItem;
                parseObjectDynamically(trailerEncryptObj, true);
            }
            try
            {
                PDEncryptionDictionary encParameters = new PDEncryptionDictionary(document.getEncryptionDictionary());

                DecryptionMaterial decryptionMaterial = null;
                if (keyStoreFilename != null)
                {
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(new FileInputStream(keyStoreFilename), password.toCharArray());

                    decryptionMaterial = new PublicKeyDecryptionMaterial(ks, alias, password);
                }
                else
                {
                    decryptionMaterial = new StandardDecryptionMaterial(password);
                }

                securityHandler = SecurityHandlersManager.getInstance().getSecurityHandler(encParameters.getFilter());
                securityHandler.prepareForDecryption(encParameters, document.getDocumentID(), decryptionMaterial);

                AccessPermission permission = securityHandler.getCurrentAccessPermission();
                if (!permission.canExtractContent())
                {
                    LOG.warn("PDF file '" + pdfFile.getPath() + "' does not allow extracting content.");
                }

            }
            catch (Exception e)
            {
                throw new IOException("Error (" + e.getClass().getSimpleName()
                        + ") while creating security handler for decryption: " + e.getMessage() /*
                                                                                                 * , e TODO: remove
                                                                                                 * remark with Java 1.6
                                                                                                 */);
            }
        }

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
        // ---- parse catalog or root object
        COSObject root = (COSObject) xrefTrailerResolver.getTrailer().getItem(COSName.ROOT);

        if (root == null)
        {
            throw new IOException("Missing root object specification in trailer.");
        }

        parseObjectDynamically(root, false);

        // ---- resolve all objects (including pages)
        if (!parseMinimalCatalog)
        {
            COSObject catalogObj = document.getCatalog();
            if (catalogObj != null)
            {
                if (catalogObj.getObject() instanceof COSDictionary)
                {
                    parseDictObjects((COSDictionary) catalogObj.getObject(), (COSName[]) null);
                    allPagesParsed = true;
                    document.setDecrypted();
                }
            }
        }
        initialParseDone = true;
    }

    // ------------------------------------------------------------------------
    /**
     * Parses an xref object stream starting with indirect object id.
     * 
     * @return value of PREV item in dictionary or <code>-1</code> if no such
     *         item exists
     */
    private long parseXrefObjStream(long objByteOffset) throws IOException
    {
        // ---- parse indirect object head
        readObjectNumber();
        readGenerationNumber();
        readPattern(OBJ_MARKER);

        COSDictionary dict = parseCOSDictionary();
        COSStream xrefStream = parseCOSStream(dict, getDocument().getScratchFile());
        parseXrefStream(xrefStream, (int) objByteOffset);

        return dict.getLong(COSName.PREV);
    }

    // ------------------------------------------------------------------------
    /** Get current offset in file at which next byte would be read. */
    private final long getPdfSourceOffset()
    {
        return pdfSource.getOffset();
    }

    /**
     * Sets {@link #pdfSource} to start next parsing at given file offset.
     * 
     * @param fileOffset file offset
     * @throws IOException If something went wrong.
     */
    protected final void setPdfSource(long fileOffset) throws IOException
    {

        pdfSource.seek(fileOffset);

        // alternative using 'old fashioned' input stream
        // if ( pdfSource != null )
        // pdfSource.close();
        //
        // pdfSource = new PushBackInputStream(
        // new BufferedInputStream(
        // new FileInputStream( file ), 16384), 4096);
        // pdfSource.skip( _fileOffset );
    }

    /**
     * Enable handling of alternative pdfSource implementation.
     * @throws IOException If something went wrong.
     */
    protected final void releasePdfSourceInputStream() throws IOException
    {
        // if ( pdfSource != null )
        // pdfSource.close();
    }

    private final void closeFileStream() throws IOException
    {
        if (pdfSource != null)
        {
            pdfSource.close();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Looks for and parses startxref. We first look for last '%%EOF' marker
     * (within last {@link #DEFAULT_TRAIL_BYTECOUNT} bytes (or range set via
     * {@link #setEOFLookupRange(int)}) and go back to find
     * <code>startxref</code>.
     * 
     * @return the offset of StartXref 
     * @throws IOException If something went wrong.
     */
    protected final long getStartxrefOffset() throws IOException
    {
        byte[] buf;
        long skipBytes;

        // ---- read trailing bytes into buffer
        fileLen = pdfFile.length();

        FileInputStream fIn = null;
        try
        {
            fIn = new FileInputStream(pdfFile);

            final int trailByteCount = (fileLen < readTrailBytes) ? (int) fileLen : readTrailBytes;
            buf = new byte[trailByteCount];
            fIn.skip(skipBytes = fileLen - trailByteCount);

            int off = 0;
            int readBytes;
            while (off < trailByteCount)
            {
                readBytes = fIn.read(buf, off, trailByteCount - off);
                // in order to not get stuck in a loop we check readBytes (this
                // should never happen)
                if (readBytes < 1)
                {
                    throw new IOException("No more bytes to read for trailing buffer, but expected: "
                            + (trailByteCount - off));
                }
                off += readBytes;
            }
        }
        finally
        {
            if (fIn != null)
            {
                try
                {
                    fIn.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }

        // ---- find last '%%EOF'
        int bufOff = lastIndexOf(EOF_MARKER, buf, buf.length);

        if (bufOff < 0)
        {
            throw new IOException("Missing end of file marker '" + (new String(EOF_MARKER)) + "'");
        }
        // ---- find last startxref preceding EOF marker
        bufOff = lastIndexOf(STARTXREF_MARKER, buf, bufOff);

        if (bufOff < 0)
        {
            throw new IOException("Missing 'startxref' marker.");
        }
        return skipBytes + bufOff;
    }

    // ------------------------------------------------------------------------
    /**
     * Searches last appearance of pattern within buffer. Lookup before _lastOff
     * and goes back until 0.
     * 
     * @param pattern pattern to search for
     * @param buf buffer to search pattern in
     * @param endOff offset (exclusive) where lookup starts at
     * 
     * @return start offset of pattern within buffer or <code>-1</code> if
     *         pattern could not be found
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
                lookupCh = pattern[patOff = lastPatternChOff];
            }
        }

        return -1;
    }

    // ------------------------------------------------------------------------
    /**
     * Reads given pattern from {@link #pdfSource}. Skipping whitespace at start
     * and end.
     * 
     * @param pattern pattern to be skipped
     * @throws IOException if pattern could not be read
     */
    protected final void readPattern(final char[] pattern) throws IOException
    {
        skipSpaces();

        for (char c : pattern)
        {
            if (pdfSource.read() != c)
            {
                throw new IOException("Expected pattern '" + new String(pattern) + " but missed at character '" + c
                        + "'");
            }
        }

        skipSpaces();
    }

    // ------------------------------------------------------------------------
    private COSDictionary pagesDictionary = null;

    /**
     * Returns PAGES {@link COSDictionary} object or throws {@link IOException}
     * if PAGES dictionary does not exist.
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
            throw new IOException("PAGES not a dictionary object, but: " + object.getClass().getSimpleName());
        }

        pagesDictionary = (COSDictionary) object;

        return pagesDictionary;
    }

    // ------------------------------------------------------------------------
    /** Parses all objects needed by pages and closes input stream. */
    /**
     * {@inheritDoc}
     */
    @Override
    public void parse() throws IOException
    {
        boolean exceptionOccurred = true; // set to false if all is processed

        try
        {
            if (!initialParseDone)
            {
                initialParse();
            }

            final int pageCount = getPageNumber();

            if (!allPagesParsed)
            {
                for (int pNr = 0; pNr < pageCount; pNr++)
                {
                    getPage(pNr);
                }
                allPagesParsed = true;
                document.setDecrypted();
            }

            exceptionOccurred = false;
        }
        finally
        {
            try
            {
                closeFileStream();
            }
            catch (IOException ioe)
            {
            }

            deleteTempFile();

            if (exceptionOccurred && (document != null))
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
     * Remove the temporary file. A temporary file is created if this class is
     * instantiated with an InputStream
     */
    protected void deleteTempFile()
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

    // ------------------------------------------------------------------------
    /**
     * Returns security handler of the document or <code>null</code> if document
     * is not encrypted or {@link #parse()} wasn't called before.
     * 
     * @return the security handler.
     */
    public SecurityHandler getSecurityHandler()
    {
        return securityHandler;
    }

    // ------------------------------------------------------------------------
    /**
     * This will get the PD document that was parsed. When you are done with
     * this document you must call close() on it to release resources.
     * 
     * Overwriting super method was necessary in order to set security handler.
     * 
     * @return The document at the PD layer.
     * 
     * @throws IOException If there is an error getting the document.
     */
    @Override
    public PDDocument getPDDocument() throws IOException
    {
        PDDocument pdDocument = super.getPDDocument();
        if (securityHandler != null)
        {
            pdDocument.setSecurityHandler(securityHandler);
        }
        return pdDocument;
    }

    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
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

        // ---- get list of top level pages
        COSArray kids = (COSArray) pagesDictionary.getDictionaryObject(COSName.KIDS);

        if (kids == null)
        {
            throw new IOException("Missing 'Kids' entry in pages dictionary.");
        }

        // ---- get page we are looking for (possibly going recursively into
        // subpages)
        COSObject pageObj = getPageObject(pageNr, kids, 0);

        if (pageObj == null)
        {
            throw new IOException("Page " + pageNr + " not found.");
        }

        // ---- parse all objects necessary to load page.
        COSDictionary pageDict = (COSDictionary) pageObj.getObject();

        if (parseMinimalCatalog && (!allPagesParsed))
        {
            // parse page resources since we did not do this on start
            COSDictionary resDict = (COSDictionary) pageDict.getDictionaryObject(COSName.RESOURCES);
            parseDictObjects(resDict);
        }

        return new PDPage(pageDict);
    }

    /**
     * Returns the object for a specific page. The page tree is made up of kids.
     * The kids have COSArray with COSObjects inside of them. The COSObject can
     * be parsed using the dynamic parsing method We want to only parse the
     * minimum COSObjects and still return a complete page. ready to be used.
     * 
     * @param num the requested page number; numbering starts with 0
     * @param startKids Kids array to start with looking up page number
     * @param startPageCount
     * 
     * @return page object or <code>null</code> if no such page exists
     * 
     * @throws IOException
     */
    private COSObject getPageObject(int num, COSArray startKids, int startPageCount) throws IOException
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
            if (count >= 0)
            {
                // skip this branch if requested page comes later
                if ((curPageCount + count) <= num)
                {
                    curPageCount += count;
                    continue;
                }
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
     * number. (requires object number < 2^31))
     */
    private final long getObjectId(final COSObject obj)
    {
        return (obj.getObjectNumber().longValue() << 32) | obj.getGenerationNumber().longValue();
    }

    /**
     * Adds all from newObjects to toBeParsedList if it is not an COSObject or
     * we didn't add this COSObject already (checked via addedObjects).
     */
    private final void addNewToList(final Queue<COSBase> toBeParsedList, final Collection<COSBase> newObjects,
            final Set<Long> addedObjects)
    {
        for (COSBase newObject : newObjects)
        {
            if (newObject instanceof COSObject)
            {
                final long objId = getObjectId((COSObject) newObject);
                if (!addedObjects.add(objId))
                {
                    continue;
                }
            }
            toBeParsedList.add(newObject);
        }
    }

    /**
     * Adds newObject to toBeParsedList if it is not an COSObject or we didn't
     * add this COSObject already (checked via addedObjects).
     */
    private final void addNewToList(final Queue<COSBase> toBeParsedList, final COSBase newObject,
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
     * Will parse every object necessary to load a single page from the pdf
     * document. We try our best to order objects according to offset in file
     * before reading to minimize seek operations.
     * 
     * @param dict the COSObject from the parent pages.
     * @param excludeObjects dictionary object reference entries with these
     *            names will not be parsed
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

        // ---- add objects not to be parsed to list of already parsed objects
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

        addNewToList(toBeParsedList, dict.getValues(), addedObjects);

        // ---- go through objects to be parsed
        while (!(toBeParsedList.isEmpty() && objToBeParsed.isEmpty()))
        {
            // -- first get all COSObject from other kind of objects and
            // put them in objToBeParsed; afterwards toBeParsedList is empty
            COSBase baseObj;
            while ((baseObj = toBeParsedList.poll()) != null)
            {
                if (baseObj instanceof COSStream)
                {
                    addNewToList(toBeParsedList, ((COSStream) baseObj).getValues(), addedObjects);
                }
                else if (baseObj instanceof COSDictionary)
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
                    COSObjectKey objKey = new COSObjectKey(obj.getObjectNumber().intValue(), obj.getGenerationNumber()
                            .intValue());

                    if (!(parsedObjects.contains(objId) /*
                                                         * || document.hasObjectInPool ( objKey )
                                                         */))
                    {
                        Long fileOffset = xrefTrailerResolver.getXrefTable().get(objKey);
                        // it is allowed that object references point to null,
                        // thus we have to test
                        if (fileOffset != null)
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
                                fileOffset = xrefTrailerResolver.getXrefTable().get(new COSObjectKey(-fileOffset, 0));
                                if ((fileOffset == null) || (fileOffset <= 0))
                                {
                                    throw new IOException("Invalid object stream xref object reference: " + fileOffset);
                                }

                                List<COSObject> stmObjects = objToBeParsed.get(fileOffset);
                                if (stmObjects == null)
                                {
                                    objToBeParsed.put(fileOffset, stmObjects = new ArrayList<COSObject>());
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

    /**
     * This will parse the next object from the stream and add it to the local
     * state. This is taken from {@link PDFParser} and reduced to parsing an
     * indirect object.
     * 
     * @param obj object to be parsed (we only take object number and generation
     *            number for lookup start offset)
     * @param requireExistingNotCompressedObj if <code>true</code> object to be
     *            parsed must not be contained within compressed stream
     * @return the parsed object (which is also added to document object)
     * 
     * @throws IOException If an IO error occurs.
     */
    protected final COSBase parseObjectDynamically(COSObject obj, boolean requireExistingNotCompressedObj)
            throws IOException
    {
        return parseObjectDynamically(obj.getObjectNumber().intValue(), obj.getGenerationNumber().intValue(),
                requireExistingNotCompressedObj);
    }

    /**
     * This will parse the next object from the stream and add it to the local
     * state. This is taken from {@link PDFParser} and reduced to parsing an
     * indirect object.
     * 
     * @param objNr object number of object to be parsed
     * @param objGenNr object generation number of object to be parsed
     * @param requireExistingNotCompressedObj if <code>true</code> the object to
     *            be parsed must be defined in xref (comment: null objects may
     *            be missing from xref) and it must not be a compressed object
     *            within object stream (this is used to circumvent being stuck
     *            in a loop in a malicious PDF)
     * 
     * @return the parsed object (which is also added to document object)
     * 
     * @throws IOException If an IO error occurs.
     */
    protected COSBase parseObjectDynamically(int objNr, int objGenNr, boolean requireExistingNotCompressedObj)
            throws IOException
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
            if (requireExistingNotCompressedObj && ((offsetOrObjstmObNr == null) || (offsetOrObjstmObNr <= 0)))
            {
                throw new IOException("Object must be defined and must not be compressed object: " + objKey.getNumber()
                        + ":" + objKey.getGeneration());
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
                setPdfSource(offsetOrObjstmObNr);

                // ---- we must have an indirect object
                final long readObjNr = readObjectNumber();
                final long readObjGen = readGenerationNumber();
                readPattern(OBJ_MARKER);

                // ---- consistency check
                if ((readObjNr != objKey.getNumber()) || (readObjGen != objKey.getGeneration()))
                {
                    throw new IOException("XREF for " + objKey.getNumber() + ":" + objKey.getGeneration()
                            + " points to wrong object: " + readObjNr + ":" + readObjGen);
                }

                skipSpaces();
                COSBase pb = parseDirObject();
                String endObjectKey = readString();

                if (endObjectKey.equals("stream"))
                {
                    pdfSource.unread(endObjectKey.getBytes("ISO-8859-1"));
                    pdfSource.unread(' ');
                    if (pb instanceof COSDictionary)
                    {
                        COSStream stream = parseCOSStream((COSDictionary) pb, getDocument().getScratchFile());

                        if (securityHandler != null)
                        {
                            try
                            {
                                securityHandler.decryptStream(stream, objNr, objGenNr);
                            }
                            catch (CryptographyException ce)
                            {
                                throw new IOException("Error decrypting stream object " + objNr + ": "
                                        + ce.getMessage()
                                /* , ce // TODO: remove remark with Java 1.6 */);
                            }
                        }
                        pb = stream;
                    }
                    else
                    {
                        // this is not legal
                        // the combination of a dict and the stream/endstream
                        // forms a complete stream object
                        throw new IOException("Stream not preceded by dictionary (offset: " + offsetOrObjstmObNr + ").");
                    }
                    skipSpaces();
                    endObjectKey = readLine();

                    // we have case with a second 'endstream' before endobj
                    if (!endObjectKey.startsWith("endobj"))
                    {
                        if (endObjectKey.startsWith("endstream"))
                        {
                            endObjectKey = endObjectKey.substring(9).trim();
                            if (endObjectKey.length() == 0)
                            {
                                // no other characters in extra endstream line
                                endObjectKey = readLine(); // read next line
                            }
                        }
                    }
                }
                else if (securityHandler != null)
                {
                    // decrypt
                    if (pb instanceof COSString)
                    {
                        decrypt((COSString) pb, objNr, objGenNr);
                    }
                    else if (pb instanceof COSDictionary)
                    {
                        for (Entry<COSName, COSBase> entry : ((COSDictionary) pb).entrySet())
                        {
                            // TODO: specially handle 'Contents' entry of
                            // signature dictionary like in
                            // SecurityHandler#decryptDictionary
                            if (entry.getValue() instanceof COSString)
                            {
                                decrypt((COSString) entry.getValue(), objNr, objGenNr);
                            }
                        }
                    }
                    else if (pb instanceof COSArray)
                    {
                        final COSArray array = (COSArray) pb;
                        for (int aIdx = 0, len = array.size(); aIdx < len; aIdx++)
                        {
                            if (array.get(aIdx) instanceof COSString)
                            {
                                decrypt((COSString) array.get(aIdx), objNr, objGenNr);
                            }
                        }
                    }
                }

                pdfObject.setObject(pb);

                if (!endObjectKey.startsWith("endobj"))
                {
                    throw new IOException("Object (" + readObjNr + ":" + readObjGen + ") at offset "
                            + offsetOrObjstmObNr + " does not end with 'endobj'.");
                }

                releasePdfSourceInputStream();

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
                    PDFObjectStreamParser parser = new PDFObjectStreamParser((COSStream) objstmBaseObj, document,
                            forceParsing);
                    parser.parse();

                    // get set of object numbers referenced for this object
                    // stream
                    final Set<Long> refObjNrs = xrefTrailerResolver.getContainedObjectNumbers(objstmObjNr);

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

    // ------------------------------------------------------------------------
    /**
     * Decrypts given COSString.
     * 
     * @param str the string to be decrypted
     * @param objNr the object number
     * @param objGenNr the object generation number
     * @throws IOException ff something went wrong
     */
    protected final void decrypt(COSString str, long objNr, long objGenNr) throws IOException
    {
        try
        {
            securityHandler.decryptString(str, objNr, objGenNr);
        }
        catch (CryptographyException ce)
        {
            throw new IOException("Error decrypting string: " + ce.getMessage()
            /* , ce // TODO: remove remark with Java 1.6 */);
        }
    }

    // ------------------------------------------------------------------------
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
                    final long curFileOffset = getPdfSourceOffset();
                    releasePdfSourceInputStream();

                    parseObjectDynamically(lengthObj, true);

                    // reset current stream position
                    setPdfSource(curFileOffset);

                    if (lengthObj.getObject() == null)
                    {
                        throw new IOException("Length object content was not read.");
                    }
                }

                if (!(lengthObj.getObject() instanceof COSNumber))
                {
                    throw new IOException("Wrong type of referenced length object " + lengthObj + ": "
                            + lengthObj.getObject().getClass().getSimpleName());
                }

                retVal = (COSNumber) lengthObj.getObject();

            }
            else
            {
                throw new IOException("Wrong type of length object: " + lengthBaseObj.getClass().getSimpleName());
            }
        }
        finally
        {
            inGetLength = false;
        }
        return retVal;
    }

    // ------------------------------------------------------------------------
    private final int streamCopyBufLen = 8192;
    private final byte[] streamCopyBuf = new byte[streamCopyBufLen];

    /**
     * This will read a COSStream from the input stream using length attribute
     * within dictionary. If length attribute is a indirect reference it is
     * first resolved to get the stream length. This means we copy stream data
     * without testing for 'endstream' or 'endobj' and thus it is no problem if
     * these keywords occur within stream. We require 'endstream' to be found
     * after stream data is read.
     * 
     * @param dic dictionary that goes with this stream.
     * @param file file to write the stream to when reading.
     * 
     * @return parsed pdf stream.
     * 
     * @throws IOException if an error occurred reading the stream, like
     *             problems with reading length attribute, stream does not end
     *             with 'endstream' after data read, stream too short etc.
     */
    @Override
    protected COSStream parseCOSStream(COSDictionary dic, RandomAccess file) throws IOException
    {
        final COSStream stream = new COSStream(dic, file);
        OutputStream out = null;
        try
        {
            readString(); // read 'stream'; this was already tested in
                          // parseObjectsDynamically()

            // ---- skip whitespaces before start of data
            // PDF Ref 1.7, chap. 3.2.7:
            // 'stream' should be followed by either a CRLF (0x0d 0x0a) or LF
            // but nothing else.
            {
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
                        // real
                        // world so we must support it
                        pdfSource.unread(whitespace);
                    }
                }
                else if (whitespace != 0x0A)
                {
                    // no whitespace after 'stream'; PDF ref. says 'should' so
                    // that is ok
                    pdfSource.unread(whitespace);
                }
            }

            /*
             * This needs to be dic.getItem because when we are parsing, the underlying object might still be null.
             */
            COSNumber streamLengthObj = getLength(dic.getItem(COSName.LENGTH));
            if (streamLengthObj == null)
            {
                throw new IOException("Missing length for stream.");
            }

            boolean useReadUntilEnd = false;
            // ---- get output stream to copy data to
            if (validateStreamLength(streamLengthObj.longValue()))
            {
                out = stream.createFilteredStream(streamLengthObj);
	            long remainBytes = streamLengthObj.longValue();
	            int bytesRead = 0;
	            while (remainBytes > 0)
	            {
	                final int readBytes = pdfSource.read(streamCopyBuf, 0,
	                        (remainBytes > streamCopyBufLen) ? streamCopyBufLen : (int) remainBytes);
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
                out = stream.createFilteredStream(streamLengthObj);
                readUntilEndStream(out);
            }
            String endStream = readString();
            if (!endStream.equals("endstream"))
            {
                throw new IOException("Error reading stream using length value. Expected='endstream' actual='"
                        + endStream + "' ");
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
    	}
    	else
    	{
			pdfSource.seek(expectedEndOfStream);
			skipSpaces();
	    	if (!checkBytesAtOffset("endstream".getBytes("ISO-8859-1")))
	    	{
	    		streamLengthIsValid = false;
	    		LOG.error("The end of the stream doesn't point to the correct offset, using workaround to read the stream");
	    	}
    		pdfSource.seek(originOffset);
    	}
    	return streamLengthIsValid;
    }
    private void readUntilEndStream(final OutputStream out) throws IOException
    {

        int bufSize;
        int charMatchCount = 0;
        byte[] keyw = ENDSTREAM;

        final int quickTestOffset = 5; // last character position of shortest
                                       // keyword ('endobj')

        // read next chunk into buffer; already matched chars are added to
        // beginning of buffer
        while ((bufSize = pdfSource.read(streamCopyBuf, charMatchCount, streamCopyBufLen - charMatchCount)) > 0)
        {
        	// number of already matching chars
            int startingMatchCount = charMatchCount;
            int bIdx = charMatchCount;
            int quickTestIdx;

            // iterate over buffer, trying to find keyword match
            for (int maxQuicktestIdx = bufSize - quickTestOffset; bIdx < bufSize; bIdx++)
            {
                // reduce compare operations by first test last character we
                // would have to
                // match if current one matches; if it is not a character from
                // keywords
                // we can move behind the test character;
                // this shortcut is inspired by Boyer–Moore string search
                // algorithm
                // and can reduce parsing time by approx. 20%
                if ((charMatchCount == 0) && ((quickTestIdx = bIdx + quickTestOffset) < maxQuicktestIdx))
                {

                    final byte ch = streamCopyBuf[quickTestIdx];
                    if ((ch > 't') || (ch < 'a'))
                    {
                        // last character we would have to match if current
                        // character would match
                        // is not a character from keywords -> jump behind and
                        // start over
                        bIdx = quickTestIdx;
                        continue;
                    }
                }

                final byte ch = streamCopyBuf[bIdx]; // could be negative - but
                                                     // we only compare to ASCII

                if (ch == keyw[charMatchCount])
                {
                    if (++charMatchCount == keyw.length)
                    {
                        // match found
                        bIdx++;
                        break;
                    }
                }
                else
                {
                    if ((charMatchCount == 3) && (ch == ENDOBJ[charMatchCount]))
                    {
                        // maybe ENDSTREAM is missing but we could have ENDOBJ
                        keyw = ENDOBJ;
                        charMatchCount++;

                    }
                    else
                    {
                        // no match; incrementing match start by 1 would be dumb
                        // since we already know matched chars
                        // depending on current char read we may already have
                        // beginning of a new match:
                        // 'e': first char matched;
                        // 'n': if we are at match position idx 7 we already
                        // read 'e' thus 2 chars matched
                        // for each other char we have to start matching first
                        // keyword char beginning with next
                        // read position
                        charMatchCount = (ch == E) ? 1 : ((ch == N) && (charMatchCount == 7)) ? 2 : 0;
                        // search again for 'endstream'
                        keyw = ENDSTREAM;
                    }
                }
            } // for

            int contentBytes = Math.max(0, bIdx - charMatchCount);

            // write buffer content until first matched char to output stream
            if (contentBytes > 0)
            {
                out.write(streamCopyBuf, 0, contentBytes);
            }
            if (charMatchCount == keyw.length)
            {
                // keyword matched; 
            	// unread matched keyword (endstream/endobj) and following buffered content
           		pdfSource.unread(streamCopyBuf, contentBytes, bufSize - contentBytes - keyw.length + startingMatchCount);
                break;

            }
            else
            {
                // copy matched chars at start of buffer
                System.arraycopy(keyw, 0, streamCopyBuf, 0, charMatchCount);
            }

        } // while
    }
    
    /**
     * 
     * @param startXRefOffset
     * @return
     * @throws IOException
     */
    private long calculateFixingOffset(long startXRefOffset) throws IOException
    {
    	// TODO check offset for XRef stream objects
    	setPdfSource(startXRefOffset);
    	if (pdfSource.peek() == X && calculateFixingOffset(startXRefOffset, XREF) == 0)
    	{
    		return 0;
    	}
    	long fixingOffset = calculateFixingOffset(startXRefOffset, XREF);
   		return fixingOffset;
    }

    /**
     * Try to dereference the given object at the given offset and calculate a new
     * offset if necessary.
     * 
     * @param objectOffset the offset where to look at
     * @param objectID the object ID
     * @param genID the generation number
     * @return the difference to the origin offset
     * @throws IOException if something went wrong
     */
    private long calculateFixingOffset(long objectOffset, long objectID, long genID) throws IOException
    {
    	String objString = Long.toString(objectID) + " " + Long.toString(genID)+ " obj";
    	return calculateFixingOffset(objectOffset, objString.getBytes("ISO-8859-1"));
    }
    
    /**
     * Check if the given bytes can be found at the current offset.
     * 
     * @param string the bytes to look for
     * @return true if the bytes are in place, false if not
     * @throws IOException if something went wrong
     */
    private boolean checkBytesAtOffset(byte[] string) throws IOException
    {
    	boolean bytesMatching = false;
		if (pdfSource.peek() == string[0])
		{
	    	int length = string.length;
	    	byte[] bytesRead = new byte[length];
			int numberOfBytes = pdfSource.read(bytesRead, 0, length);
			while (numberOfBytes < length)
			{
				int readMore =  pdfSource.read(bytesRead, numberOfBytes, length-numberOfBytes);
				if (readMore < 0)
				{
					break;
				}
				numberOfBytes += readMore;
			}
			if (Arrays.equals(string, bytesRead))
			{
				bytesMatching = true;
			}
			pdfSource.unread(bytesRead, 0, numberOfBytes);
		}
		return bytesMatching;
    }
    
    /**
     * Check if the given bytes can be found at the given offset.
     * The method seeks 200 bytes backward/forward if the given string
     * can't be found at the given offset and returns the difference 
     * of the new offset to the origin one.
     * 
     * @param objectOffset the given offset where to look at
     * @param string the bytes to look for
     * @return the difference to the origin one
     * @throws IOException if something went wrong
     */
    private long calculateFixingOffset(long objectOffset, byte[] string) throws IOException
    {
    	if (objectOffset < 0)
    	{
    		LOG.error("Invalid object offset " + objectOffset + " for object " + new String(string));
    		return 0;
    	}
    	long originOffset = pdfSource.getOffset();
    	pdfSource.seek(objectOffset);
    	// most likely the object can be found at the given offset
    	if (checkBytesAtOffset(string))
    	{
        	pdfSource.seek(originOffset);
			return 0;
		}
    	// the offset seems to be wrong -> seek backward to find the object we are looking for
    	long currentOffset = objectOffset;
    	for (int i=1; i<20;i++)
    	{
    		currentOffset = objectOffset - (i*10);
    		if (currentOffset > 0)
    		{
	    		pdfSource.seek(currentOffset);
	    		for (int j=0; j<10;j++)
	    		{
	    			if (checkBytesAtOffset(string))
	    			{
	    				pdfSource.seek(originOffset);
						LOG.debug("Fixed reference for object "+new String(string)+" "+objectOffset + " -> "+(objectOffset - currentOffset));
	    				return objectOffset - currentOffset;
	    			}
	    			else
	    			{
	    				currentOffset++;
	    				pdfSource.read();
	    			}
	    		}
    		}
    	}
    	// no luck by seeking backward -> seek forward to find the object we are looking for
		pdfSource.seek(objectOffset);
		currentOffset = objectOffset;
		do
		{
			if (checkBytesAtOffset(string))
			{
				pdfSource.seek(originOffset);
				if (currentOffset != 0)
				{
					LOG.debug("Fixed reference for object "+new String(string)+" "+objectOffset + " -> "+(objectOffset - currentOffset));
				}
				return objectOffset - currentOffset;
			}
			else
			{
				// next byte
				currentOffset++;
				if (pdfSource.read() == -1)
				{
					throw new IOException("Premature end of file while dereferencing object "+ new String(string) + " at offset " + objectOffset);
				}
			}
		}
		while(currentOffset < objectOffset+200);
		pdfSource.seek(originOffset);
		LOG.error("Can't find the object " + new String(string) + " at offset " + objectOffset);
    	return 0;
    }

    /**
     * Check the XRef table by dereferencing all objects and fixing 
     * the offset if necessary.
     * 
     * @throws IOException if something went wrong.
     */
    private void checkXrefOffsets() throws IOException
    {
    	Map<COSObjectKey, Long>xrefOffset = xrefTrailerResolver.getXrefTable();
    	if (xrefOffset != null)
    	{
    		for (COSObjectKey objectKey : xrefOffset.keySet())
    		{
    			Long objectOffset = xrefOffset.get(objectKey);
    			// a negative offset number represents a object number itself
    			// see type 2 entry in xref stream
    			if (objectOffset != null && objectOffset > 0)
    			{
        			long objectNr = objectKey.getNumber();
        			long objectGen = objectKey.getGeneration();
    				long fixingOffset = calculateFixingOffset(objectOffset, objectNr, objectGen);
    				if (fixingOffset != 0)
    				{
    					long newOffset = objectOffset - fixingOffset;
    					xrefOffset.put(objectKey, newOffset);
    					LOG.debug("Fixed reference for object "+objectNr+" "+objectGen+" "+objectOffset + " -> "+newOffset);
    				}
    			}
    		}
    	}
    }

}
