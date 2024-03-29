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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver.XRefType;
import org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandler;

/**
 * Brute force parser to be used as last resort if a malformed pdf can't be read.
 */
public class BruteForceParser extends COSParser
{
    private static final char[] XREF_TABLE = new char[] { 'x', 'r', 'e', 'f' };
    private static final char[] XREF_STREAM = new char[] { '/', 'X', 'R', 'e', 'f' };

    private static final long MINIMUM_SEARCH_OFFSET = 6;

    /**
     * EOF-marker.
     */
    private static final char[] EOF_MARKER = new char[] { '%', '%', 'E', 'O', 'F' };
    /**
     * obj-marker.
     */
    private static final char[] OBJ_MARKER = new char[] { 'o', 'b', 'j' };

    /**
     * trailer-marker.
     */
    private static final char[] TRAILER_MARKER = new char[] { 't', 'r', 'a', 'i', 'l', 'e', 'r' };

    /**
     * ObjStream-marker.
     */
    private static final char[] OBJ_STREAM = new char[] { '/', 'O', 'b', 'j', 'S', 't', 'm' };

    private static final Log LOG = LogFactory.getLog(BruteForceParser.class);

    /**
     * Contains all found objects of a brute force search.
     */
    private final Map<COSObjectKey, Long> bfSearchCOSObjectKeyOffsets = new HashMap<>();

    private boolean bfSearchTriggered = false;

    /**
     * Constructor. Triggers a brute force search for all objects of the document.
     *
     * @param source input representing the pdf.
     * @param document the corresponding COS document
     * 
     * @throws IOException if the source data could not be read
     */
    public BruteForceParser(RandomAccessRead source, COSDocument document) throws IOException
    {
        super(source);
        this.document = document;
    }

    /**
     * Indicates wether the brute force search for objects was triggered.
     * 
     * @return true if the search was triggered
     */
    public boolean bfSearchTriggered()
    {
        return bfSearchTriggered;
    }

    /**
     * Returns all found objects of a brute force search.
     * 
     * @return map containing all found objects of a brute force search
     * 
     * @throws IOException if something went wrong
     * 
     */
    protected Map<COSObjectKey, Long> getBFCOSObjectOffsets() throws IOException
    {
        if (!bfSearchTriggered)
        {
            bfSearchTriggered = true;
            bfSearchForObjects();
        }
        return bfSearchCOSObjectKeyOffsets;
    }

    /**
     * Brute force search for every object in the pdf.
     * 
     * @throws IOException if something went wrong
     */
    private void bfSearchForObjects() throws IOException
    {
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
                                bfSearchCOSObjectKeyOffsets.put(
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
            bfSearchCOSObjectKeyOffsets.put(new COSObjectKey(lastObjectId, lastGenID),
                    lastObjOffset);
        }
        // reestablish origin position
        source.seek(originOffset);
    }

    /**
     * Search for the offset of the given xref table/stream among those found by a brute force search.
     * 
     * @param xrefOffset the given offset to be searched for
     * 
     * @return the offset of the xref entry
     * @throws IOException if something went wrong
     */
    protected long bfSearchForXRef(long xrefOffset) throws IOException
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
     * Brute force search for all objects streams of a pdf.
     * 
     * @param trailerResolver the trailer resolver of the document
     * @param securityHandler security handler to be used to decrypt encrypted documents
     * @throws IOException if something went wrong
     */
    protected void bfSearchForObjStreams(XrefTrailerResolver trailerResolver,
            SecurityHandler<? extends ProtectionPolicy> securityHandler) throws IOException
    {
        // update security handler
        this.securityHandler = securityHandler;
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
                COSDictionary dict = parseCOSDictionary(false);
                stream = parseCOSStream(dict);
                if (securityHandler != null)
                {
                    securityHandler.decryptStream(stream, stmObjNumber, stmGenNumber);
                }
                PDFObjectStreamParser objStreamParser = new PDFObjectStreamParser(stream, document);
                Map<Long, Integer> objectNumbers = objStreamParser.readObjectNumbers();
                Map<COSObjectKey, Long> xrefOffset = trailerResolver.getXrefTable();
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
     * Brute force search for all trailer marker.
     * 
     * @param trailer dictionary to be used as trailer dictionary
     * 
     * @throws IOException if something went wrong
     */
    private boolean bfSearchForTrailer(COSDictionary trailer) throws IOException
    {
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
                COSDictionary trailerDict = parseCOSDictionary(true);
                COSObject rootObj = trailerDict.getCOSObject(COSName.ROOT);
                if (rootObj != null)
                {
                    // check if the dictionary can be dereferenced and is the one we are looking for
                    COSBase rootDict = rootObj.getObject();
                    if (rootDict instanceof COSDictionary && isCatalog((COSDictionary) rootDict))
                    {
                        rootFound = true;
                    }
                }
                COSObject infoObj = trailerDict.getCOSObject(COSName.INFO);
                if (infoObj != null)
                {
                    // check if the dictionary can be dereferenced and is the one we are looking for
                    COSBase infoDict = infoObj.getObject();
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
                        COSObject encObj = trailerDict.getCOSObject(COSName.ENCRYPT);
                        // check if the dictionary can be dereferenced
                        // TODO check if the dictionary is an encryption dictionary?
                        if (encObj != null && encObj.getObject() instanceof COSDictionary)
                        {
                            trailer.setItem(COSName.ENCRYPT, encObj);
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
            catch (IOException exception)
            {
                LOG.debug("An exception occurred during brute force search for trailer - ignoring",
                        exception);
            }
            trailerOffset = findString(TRAILER_MARKER);
        }
        source.seek(originOffset);
        return false;
    }

    /**
     * Search for the different parts of the trailer dictionary.
     *
     * @param trailer dictionary to be used as trailer dictionary
     * @return true if the root was found, false if not.
     * 
     * @throws IOException if something went wrong
     */
    private boolean searchForTrailerItems(COSDictionary trailer) throws IOException
    {
        COSObject rootObject = null;
        COSObject infoObject = null;
        for (Entry<COSObjectKey, Long> entrySet : getBFCOSObjectOffsets().entrySet())
        {
            COSObjectKey currentKey = entrySet.getKey();
            COSObject cosObject = document.getObjectFromPool(currentKey);
            COSBase baseObject = cosObject.getObject();

            if (!(baseObject instanceof COSDictionary))
            {
                continue;
            }
            COSDictionary dictionary = (COSDictionary) baseObject;
            // document catalog
            if (isCatalog(dictionary))
            {
                rootObject = compareCOSObjects(cosObject, entrySet.getValue(), rootObject);
            }
            // info dictionary
            else if (isInfo(dictionary))
            {
                infoObject = compareCOSObjects(cosObject, entrySet.getValue(), infoObject);
            }
            // encryption dictionary, if existing, is lost
            // We can't run "Algorithm 2" from PDF specification because of missing ID
        }
        if (rootObject != null)
        {
            trailer.setItem(COSName.ROOT, rootObject);
        }
        if (infoObject != null)
        {
            trailer.setItem(COSName.INFO, infoObject);
        }
        return rootObject != null;
    }

    private COSObject compareCOSObjects(COSObject newObject, Long newOffset,
            COSObject currentObject)
    {
        if (currentObject != null && currentObject.getKey() != null)
        {
            COSObjectKey currentKey = currentObject.getKey();
            COSObjectKey newKey = newObject.getKey();
            // check if the current object is an updated version of the previous found object
            if (currentKey.getNumber() == newKey.getNumber())
            {
                return currentKey.getGeneration() < newKey.getGeneration() ? newObject
                        : currentObject;
            }
            // most likely the object with the bigger offset is the newer one
            Long currentOffset = document.getXrefTable().get(currentKey);
            return currentOffset != null && newOffset > currentOffset ? newObject : currentObject;
        }
        return newObject;
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
     * Tell if the dictionary is an info dictionary.
     * 
     * @param dictionary the dictionary to be checked
     * @return true if the given dictionary is an info dictionary
     */
    private boolean isInfo(COSDictionary dictionary)
    {
        if (dictionary.containsKey(COSName.PARENT) || dictionary.containsKey(COSName.A)
                || dictionary.containsKey(COSName.DEST))
        {
            return false;
        }
        return dictionary.containsKey(COSName.MOD_DATE) || dictionary.containsKey(COSName.TITLE)
                || dictionary.containsKey(COSName.AUTHOR)
                || dictionary.containsKey(COSName.SUBJECT)
                || dictionary.containsKey(COSName.KEYWORDS)
                || dictionary.containsKey(COSName.CREATOR)
                || dictionary.containsKey(COSName.PRODUCER)
                || dictionary.containsKey(COSName.CREATION_DATE);
    }

    /**
     * Tell if the dictionary is a PDF or FDF catalog.
     * 
     * @param dictionary
     * @return true if the given dictionary is a root dictionary
     */
    private boolean isCatalog(COSDictionary dictionary)
    {
        return COSName.CATALOG.equals(dictionary.getCOSName(COSName.TYPE))
                || dictionary.containsKey(COSName.FDF);
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
                    position = source.getPosition() - 1;
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
     * Rebuild the trailer dictionary if startxref can't be found.
     * 
     * @param trailerResolver the trailer resolver of the document
     * @param securityHandler security handler to be used to decrypt encrypted documents
     * @return the rebuild trailer dictionary
     * 
     * @throws IOException if something went wrong
     */
    protected COSDictionary rebuildTrailer(XrefTrailerResolver trailerResolver,
            SecurityHandler<? extends ProtectionPolicy> securityHandler) throws IOException
    {
        // update security handler
        this.securityHandler = securityHandler;
        // reset trailer resolver
        trailerResolver.reset();
        // use the found objects to rebuild the trailer resolver
        trailerResolver.nextXrefObj(0, XRefType.TABLE);
        getBFCOSObjectOffsets().forEach(trailerResolver::setXRef);
        trailerResolver.setStartxref(0);
        // transfer xref-table to document
        document.getXrefTable().clear();
        document.addXRefTable(trailerResolver.getXrefTable());
        // remember the highest XRef object number to avoid it being reused in incremental saving
        Long maxValue = document.getXrefTable().keySet().stream() //
                .map(COSObjectKey::getNumber) //
                .reduce(Long::max) //
                .orElse(0L);
        document.setHighestXRefObjectNumber(maxValue);

        COSDictionary trailer = trailerResolver.getTrailer();
        document.setTrailer(trailer);
        boolean searchForObjStreamsDone = false;
        if (!bfSearchForTrailer(trailer) && !searchForTrailerItems(trailer))
        {
            // root entry wasn't found, maybe it is part of an object stream
            // brute force search for all object streams.
            bfSearchForObjStreams(trailerResolver, securityHandler);
            searchForObjStreamsDone = true;
            // search again for the root entry
            searchForTrailerItems(trailer);
        }
        // prepare decryption if necessary
        prepareDecryption();
        if (!searchForObjStreamsDone)
        {
            // brute force search for all object streams.
            bfSearchForObjStreams(trailerResolver, securityHandler);
        }
        return trailer;
    }

}
