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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;

/**
 * Parser to be used to read the cross reference table of a pdf. It is either a simple table or a stream.
 */
public class XrefParser
{
    private static final Logger LOG = LogManager.getLogger(XrefParser.class);

    private static final int X = 'x';
    private static final char[] XREF_TABLE = { 'x', 'r', 'e', 'f' };
    private static final char[] STARTXREF = { 's', 't', 'a', 'r', 't', 'x', 'r', 'e', 'f' };

    /** 
     * Collects all Xref/trailer objects and resolves them into single
     * object using startxref reference. 
     */
    private XrefTrailerResolver xrefTrailerResolver = new XrefTrailerResolver();

    private final COSParser parser;
    private final RandomAccessRead source;

    /**
     * Default constructor.
     *
     * @param cosParser the parser to be used to read the pdf.
     * 
     */
    public XrefParser(COSParser cosParser)
    {
        parser = cosParser;
        source = parser.source;
    }

    /**
     * Returns the resulting cross reference table.
     * 
     * @return
     */
    public Map<COSObjectKey, Long> getXrefTable()
    {
        return xrefTrailerResolver.getXrefTable();
    }

    /**
     * Parses cross reference tables.
     * 
     * @param document the corresponding COS document of the pdf.
     * @param startXRefOffset start offset of the first table
     * 
     * @return the trailer dictionary
     * 
     * @throws IOException if something went wrong
     */
    public COSDictionary parseXref(COSDocument document, long startXRefOffset) throws IOException
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
        COSDictionary trailer = null;
        while (prev > 0)
        {
            // save expected position for loop detection
            prevSet.add(prev);
            // seek to xref table
            source.seek(prev);
            // skip white spaces
            parser.skipSpaces();
            // save current position as well due to skipped spaces
            prevSet.add(source.getPosition());
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
                trailer = xrefTrailerResolver.getCurrentTrailer();
                // check for a XRef stream, it may contain some object ids of compressed objects 
                if(trailer.containsKey(COSName.XREF_STM))
                {
                    int streamOffset = trailer.getInt(COSName.XREF_STM);
                    // check the xref stream reference
                    fixedOffset = checkXRefOffset(streamOffset);
                    if (fixedOffset > -1 && fixedOffset != streamOffset)
                    {
                        LOG.warn("/XRefStm offset {} is incorrect, corrected to {}", streamOffset,
                                fixedOffset);
                        streamOffset = (int)fixedOffset;
                        trailer.setInt(COSName.XREF_STM, streamOffset);
                    }
                    if (streamOffset > 0)
                    {
                        source.seek(streamOffset);
                        parser.skipSpaces();
                        try
                        {
                            parseXrefObjStream(prev, false);
                            document.setHasHybridXRef();
                        }
                        catch (IOException ex)
                        {
                            LOG.error("Failed to parse /XRefStm at offset " + streamOffset, ex);
                        }
                    }
                    else
                    {
                        LOG.error("Skipped XRef stream due to a corrupt offset: {}", streamOffset);
                    }
                }
                prev = trailer.getLong(COSName.PREV);
            }
            else
            {
                // parse xref stream
                prev = parseXrefObjStream(prev, true);
                trailer = xrefTrailerResolver.getCurrentTrailer();
            }
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
            if (prevSet.contains(prev))
            {
                throw new IOException("/Prev loop at offset " + prev);
            }
        }
        // ---- build valid xrefs out of the xref chain
        xrefTrailerResolver.setStartxref(startXrefOffset);
        trailer = xrefTrailerResolver.getTrailer();
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
        int nextCharacter = source.peek();
        while (nextCharacter != 't' && BaseParser.isDigit(nextCharacter))
        {
            if (source.getPosition() == trailerOffset)
            {
                // warn only the first time
                LOG.warn("Expected trailer object at offset {}, keep trying", trailerOffset);
            }
            parser.readLine();
            nextCharacter = source.peek();
        }
        if (source.peek() != 't')
        {
            return false;
        }
        // read "trailer"
        long currentOffset = source.getPosition();
        String nextLine = parser.readLine();
        if (!nextLine.trim().equals("trailer"))
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
        parser.skipSpaces();

        COSDictionary parsedTrailer = parser.parseCOSDictionary(true);
        xrefTrailerResolver.setTrailer(parsedTrailer);

        parser.skipSpaces();
        return true;
    }

    /**
     * Parses an xref object stream starting with indirect object id.
     * 
     * @return value of PREV item in dictionary or <code>-1</code> if no such item exists
     */
    private long parseXrefObjStream(long objByteOffset, boolean isStandalone) throws IOException
    {
        // ---- parse indirect object head
        parser.readObjectNumber();
        parser.readGenerationNumber();
        parser.readObjectMarker();

        COSDictionary dict = parser.parseCOSDictionary(false);
        try (COSStream xrefStream = parser.parseCOSStream(dict))
        {
            // the cross reference stream of a hybrid xref table will be added to the existing one
            // and we must not override the offset and the trailer
            if ( isStandalone )
            {
                xrefTrailerResolver.nextXrefObj( objByteOffset, XRefType.STREAM );
                xrefTrailerResolver.setTrailer(xrefStream);
            }
            new PDFXrefStreamParser(xrefStream).parse(xrefTrailerResolver);
        }

        return dict.getLong(COSName.PREV);
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
        source.seek(startXRefOffset);
        parser.skipSpaces();
        if (parser.isString(XREF_TABLE))
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
            LOG.error("Invalid object offset {} when searching for a xref table/stream",
                    objectOffset);
            return 0;
        }
        // search for the offset of the given xref table/stream among those found by a brute force search.
        long newOffset = parser.getBruteForceParser().bfSearchForXRef(objectOffset);
        if (newOffset > -1)
        {
            LOG.debug("Fixed reference for xref table/stream {} -> {}", objectOffset, newOffset);
            return newOffset;
        }
        LOG.error("Can't find the object xref table/stream at offset {}", objectOffset);
        return 0;
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
        if (startXRefOffset == 0)
        {
            return true;
        }
        // seek to offset-1 
        source.seek(startXRefOffset - 1);
        int nextValue = source.read();
        // the first character has to be a whitespace, and then a digit
        if (BaseParser.isWhitespace(nextValue))
        {
            parser.skipSpaces();
            if (parser.isDigit())
            {
                try
                {
                    // it's a XRef stream
                    parser.readObjectNumber();
                    parser.readGenerationNumber();
                    parser.readObjectMarker();
                    // check the dictionary to avoid false positives
                    COSDictionary dict = parser.parseCOSDictionary(false);
                    source.seek(startXRefOffset);
                    if ("XRef".equals(dict.getNameAsString(COSName.TYPE)))
                    {
                        return true;
                    }
                }
                catch (IOException exception)
                {
                    // there wasn't an object of a xref stream
                    LOG.debug("No Xref stream at given location {}", startXRefOffset, exception);
                    source.seek(startXRefOffset);
                }
            }
        }
        return false;
    }
    
    private boolean validateXrefOffsets(Map<COSObjectKey, Long> xrefOffset) throws IOException
    {
        if (xrefOffset == null)
        {
            return true;
        }
        Map<COSObjectKey, COSObjectKey> correctedKeys = new HashMap<>();
        HashSet<COSObjectKey> validKeys = new HashSet<>();
        for (Entry<COSObjectKey, Long> objectEntry : xrefOffset.entrySet())
        {
            COSObjectKey objectKey = objectEntry.getKey();
            Long objectOffset = objectEntry.getValue();
            // a negative offset number represents an object number itself
            // see type 2 entry in xref stream
            if (objectOffset != null && objectOffset >= 0)
            {
                COSObjectKey foundObjectKey = findObjectKey(objectKey, objectOffset, xrefOffset);
                if (foundObjectKey == null)
                {
                    LOG.debug(
                            "Stop checking xref offsets as at least one ({}) couldn't be dereferenced",
                            objectKey);
                    return false;
                }
                else if (foundObjectKey != objectKey)
                {
                    // Generation was fixed - need to update map later, after iteration
                    correctedKeys.put(objectKey, foundObjectKey);
                }
                else
                {
                    validKeys.add(objectKey);
                }
            }
        }
        Map<COSObjectKey, Long> correctedPointers = new HashMap<>();
        for (Entry<COSObjectKey, COSObjectKey> correctedKeyEntry : correctedKeys.entrySet())
        {
            if (!validKeys.contains(correctedKeyEntry.getValue()))
            {
                // Only replace entries, if the original entry does not point to a valid object
                correctedPointers.put(correctedKeyEntry.getValue(),
                        xrefOffset.get(correctedKeyEntry.getKey()));
            }
        }
        // remove old invalid, as some might not be replaced
        correctedKeys.forEach((key, value) -> xrefOffset.remove(key));
        xrefOffset.putAll(correctedPointers);
        return true;
    }

    /**
     * Check the XRef table by dereferencing all objects and fixing the offset if necessary.
     * 
     * @throws IOException if something went wrong.
     */
    private void checkXrefOffsets() throws IOException
    {
        Map<COSObjectKey, Long> xrefOffset = xrefTrailerResolver.getXrefTable();
        if (!validateXrefOffsets(xrefOffset))
        {
            Map<COSObjectKey, Long> bfCOSObjectKeyOffsets = parser.getBruteForceParser()
                    .getBFCOSObjectOffsets();
            if (!bfCOSObjectKeyOffsets.isEmpty())
            {
                LOG.debug("Replaced read xref table with the results of a brute force search");
                xrefOffset.clear();
                xrefOffset.putAll(bfCOSObjectKeyOffsets);
            }
        }
    }

    /**
     * Check if the given object can be found at the given offset. Returns the provided object key if everything is ok.
     * If the generation number differs it will be fixed and a new object key is returned.
     * 
     * @param objectKey the key of object we are looking for
     * @param offset the offset where to look
     * @param xrefOffset a map with with all known xref entries
     * @return returns the found/fixed object key
     * 
     * @throws IOException if something went wrong
     */
    private COSObjectKey findObjectKey(COSObjectKey objectKey, long offset,
            Map<COSObjectKey, Long> xrefOffset) throws IOException
    {
        // there can't be any object at the very beginning of a pdf
        if (offset < COSParser.MINIMUM_SEARCH_OFFSET)
        {
            return null;
        }
        try 
        {
            source.seek(offset);
            parser.skipWhiteSpaces();
            if (source.getPosition() == offset)
            {
                // ensure that at least one whitespace is skipped in front of the object number
                source.seek(offset - 1);
                if (source.getPosition() < offset)
                {
                    if (!parser.isDigit())
                    {
                        // anything else but a digit may be some garbage of the previous object -> just ignore it
                        source.read();
                    }
                    else
                    {
                        long current = source.getPosition();
                        source.seek(--current);
                        while (parser.isDigit())
                            source.seek(--current);
                        long newObjNr = parser.readObjectNumber();
                        int newGenNr = parser.readGenerationNumber();
                        COSObjectKey newObjKey = new COSObjectKey(newObjNr, newGenNr);
                        Long existingOffset = xrefOffset.get(newObjKey);
                        // the found object number belongs to another uncompressed object at the same or nearby offset
                        // something has to be wrong
                        if (existingOffset != null && existingOffset > 0
                                && Math.abs(offset - existingOffset) < 10)
                        {
                            LOG.debug("Found the object {} instead of {} at offset {} - ignoring",
                                    newObjKey, objectKey, offset);
                            return null;
                        }
                        // something seems to be wrong but it's hard to determine what exactly -> simply continue
                        source.seek(offset);
                    }
                }
            }
            // try to read the given object/generation number
            long foundObjectNumber = parser.readObjectNumber();
            if (objectKey.getNumber() != foundObjectNumber)
            {
                LOG.warn("found wrong object number. expected [{}] found [{}]",
                        objectKey.getNumber(), foundObjectNumber);
                objectKey = new COSObjectKey(foundObjectNumber, objectKey.getGeneration());
            }

            int genNumber = parser.readGenerationNumber();
            // finally try to read the object marker
            parser.readObjectMarker();
            if (genNumber == objectKey.getGeneration())
            {
                return objectKey;
            }
            else if (genNumber > objectKey.getGeneration())
            {
                return new COSObjectKey(objectKey.getNumber(), genNumber);
            }
        }
        catch (IOException exception)
        {
            // Swallow the exception, obviously there isn't any valid object number
            LOG.debug("No valid object at given location {} - ignoring", offset, exception);
        }
        return null;
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
        if (parser.isString(STARTXREF))
        {
            parser.readString();
            parser.skipSpaces();
            // This integer is the byte offset of the first object referenced by the xref or xref stream
            startXref = parser.readLong();
        }
        return startXref;
    }
    
    /**
     * This will parse the xref table from the stream and add it to the state
     * The XrefTable contents are ignored.
     * @param startByteOffset the offset to start at
     * @return false on parsing error
     * @throws IOException If an IO error occurs.
     */
    private boolean parseXrefTable(long startByteOffset) throws IOException
    {
        if (source.peek() != 'x')
        {
            return false;
        }
        String xref = parser.readString();
        if( !xref.trim().equals( "xref" ) )
        {
            return false;
        }
        
        // check for trailer after xref
        String str = parser.readString();
        byte[] b = str.getBytes(StandardCharsets.ISO_8859_1);
        source.seek(source.getPosition() - b.length);
        
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
            String currentLine = parser.readLine();
            String[] splitString = currentLine.split("\\s");
            if (splitString.length != 2)
            {
                LOG.warn("Unexpected XRefTable Entry: {}", currentLine);
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
                LOG.warn("XRefTable: invalid ID for the first object: {}", currentLine);
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
                LOG.warn("XRefTable: invalid number of objects: {}", currentLine);
                return false;
            }
            
            parser.skipSpaces();
            for(int i = 0; i < count; i++)
            {
                if (parser.isEOF() )
                {
                    break;
                }
                int nextChar = source.peek();
                if (nextChar == 't' || BaseParser.isEndOfName(nextChar))
                {
                    break;
                }
                //Ignore table contents
                currentLine = parser.readLine();
                splitString = currentLine.split("\\s");
                if (splitString.length < 3)
                {
                    LOG.warn("invalid xref line: {}", currentLine);
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
                    catch (IllegalArgumentException e)
                    {
                        throw new IOException(e);
                    }
                }
                else if(!splitString[2].equals("f"))
                {
                    throw new IOException("Corrupt XRefTable Entry - ObjID:" + currObjID);
                }
                currObjID++;
                parser.skipSpaces();
            }
            parser.skipSpaces();
            if (!parser.isDigit())
            {
                break;
            }
        }
        return true;
    }

}
