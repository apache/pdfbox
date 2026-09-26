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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * Parser to be used to read the trailer of a pdf.
 */
public class TrailerParser
{
    private static final Logger LOG = LogManager.getLogger(TrailerParser.class);

    private final COSParser parser;
    private final COSDocument document;

    private static final char[] EOF_MARKER = { '%', '%', 'E', 'O', 'F' };

    private static final char[] STARTXREF = { 's', 't', 'a', 'r', 't', 'x', 'r', 'e', 'f' };

    /**
     * how many trailing bytes to read for EOF marker.
     */
    private final int readTrailBytes;

    private final Map<COSObjectKey, Long> xrefTable = new HashMap<>();

    /**
     * file length.
     */
    private final long fileLen;

    /**
     * Indicates if the trailer was rebuilt using the brute force parser due to a malformed pdf.
     */
    private boolean trailerWasRebuild = false;

    /**
     * Constructor.
     * 
     * @param cosDocument the underlying COS-document of the pdf to be read
     * @param cosParser the parser to be used for reading
     * @throws IOException if something went wrong
     */
    public TrailerParser(COSDocument cosDocument, COSParser cosParser)
            throws IOException
    {
        document = cosDocument;
        parser = cosParser;
        fileLen = parser.length();
        readTrailBytes = cosParser.getEOFLookupRange();
    }

    public boolean trailerWasRebuild()
    {
        return trailerWasRebuild;
    }

    public Map<COSObjectKey, Long> getXrefTable()
    {
        return xrefTable;
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
                XrefParser xrefParser = new XrefParser(parser);
                trailer = xrefParser.parseXref(document, startXRefOffset);
                xrefTable.putAll(xrefParser.getXrefTable());
            }
            else
            {
                rebuildTrailer = parser.isLenient();
            }
        }
        catch (IOException exception)
        {
            if (parser.isLenient())
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
            rebuildTrailer = parser.isLenient();
        }
        if (rebuildTrailer)
        {
            // reset cross reference table
            xrefTable.clear();
            trailer = parser.getBruteForceParser().rebuildTrailer(xrefTable);
            trailerWasRebuild = true;
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
            parser.seek(skipBytes);
            int off = 0;
            int readBytes;
            while (off < trailByteCount)
            {
                readBytes = parser.read(buf, off, trailByteCount - off);
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
            parser.seek(0);
        }
        // find last '%%EOF'
        int bufOff = lastIndexOf(EOF_MARKER, buf, buf.length);
        if (bufOff < 0)
        {
            if (parser.isLenient())
            {
                // in lenient mode the '%%EOF' isn't needed
                bufOff = buf.length;
                LOG.debug("Missing end of file marker '{}'", new String(EOF_MARKER));
            }
            else
            {
                throw new IOException(
                        "Missing end of file marker '" + new String(EOF_MARKER) + "'");
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

}
