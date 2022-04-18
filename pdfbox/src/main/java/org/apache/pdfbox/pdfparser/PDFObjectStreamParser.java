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
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

/**
 * This will parse a PDF 1.5 object stream and extract the object with given object number from the stream.
 *
 * @author Ben Litchfield
 * 
 */
public class PDFObjectStreamParser extends BaseParser
{
    private final int numberOfObjects;
    private final int firstObject;

    /**
     * Constructor.
     *
     * @param stream The stream to parse.
     * @param document The document for the current parsing.
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFObjectStreamParser(COSStream stream, COSDocument document) throws IOException
    {
        super(stream.createView());
        this.document = document;
        // get mandatory number of objects
        numberOfObjects = stream.getInt(COSName.N);
        if (numberOfObjects == -1)
        {
            throw new IOException("/N entry missing in object stream");
        }
        if (numberOfObjects < 0)
        {
            throw new IOException("Illegal /N entry in object stream: " + numberOfObjects);
        }
        // get mandatory stream offset of the first object
        firstObject = stream.getInt(COSName.FIRST);
        if (firstObject == -1)
        {
            throw new IOException("/First entry missing in object stream");
        }
        if (firstObject < 0)
        {
            throw new IOException("Illegal /First entry in object stream: " + firstObject);
        }
    }

    /**
     * Search for/parse the object with the given object number. The stream is closed after parsing the object with the
     * given number.
     * 
     * @param objectNumber the number of the object to b e parsed
     * @return the parsed object or null if the object with the given number can't be found
     * @throws IOException if there is an error while parsing the stream
     */
    public COSBase parseObject(long objectNumber) throws IOException
    {
        COSBase streamObject = null;
        try
        {
            Integer objectOffset = privateReadObjectNumbers().get(objectNumber);
            if (objectOffset != null)
            {
                // jump to the offset of the first object
                long currentPosition = source.getPosition();
                if (firstObject > 0 && currentPosition < firstObject)
                {
                    source.skip(firstObject - (int) currentPosition);
                }
                // jump to the offset of the object to be parsed
                source.skip(objectOffset);
                streamObject = parseDirObject();
                if (streamObject != null)
                {
                    streamObject.setDirect(false);
                }
            }
        }
        finally
        {
            source.close();
            document = null;
        }
        return streamObject;
    }

    /**
     * Parse all compressed objects. The stream is closed after parsing.
     * 
     * @return a map containing all parsed objects using the object number as key
     * @throws IOException if there is an error while parsing the stream
     */
    public Map<Long, COSBase> parseAllObjects() throws IOException
    {
        Map<Long, COSBase> allObjects = new HashMap<>();
        try
        {
            Map<Integer, Long> objectNumbers = privateReadObjectOffsets();
            long currentPosition = source.getPosition();
            if (firstObject > 0 && currentPosition < firstObject)
            {
                source.skip(firstObject - (int) currentPosition);
            }
            for (Entry<Integer, Long> entry : objectNumbers.entrySet())
            {
                int finalPosition = firstObject + entry.getKey();
                currentPosition = source.getPosition();
                if (finalPosition > 0 && currentPosition < finalPosition)
                {
                    // jump to the offset of the object to be parsed
                    source.skip(finalPosition - (int) currentPosition);
                }
                COSBase streamObject = parseDirObject();
                if (streamObject != null)
                {
                    streamObject.setDirect(false);
                }
                allObjects.put(entry.getValue(), streamObject);
            }
        }
        finally
        {
            source.close();
            document = null;
        }
        return allObjects;
    }

    private Map<Long, Integer> privateReadObjectNumbers() throws IOException
    {
        // don't initialize map using numberOfObjects as there might by less object numbers than expected
        Map<Long, Integer> objectNumbers = new HashMap<>();
        long firstObjectPosition = source.getPosition() + firstObject - 1;
        for (int i = 0; i < numberOfObjects; i++)
        {
            // don't read beyond the part of the stream reserved for the object numbers
            if (source.getPosition() >= firstObjectPosition)
            {
                break;
            }
            long objectNumber = readObjectNumber();
            int offset = (int) readLong();
            objectNumbers.put(objectNumber, offset);
        }
        return objectNumbers;
    }

    private Map<Integer, Long> privateReadObjectOffsets() throws IOException
    {
        // according to the pdf spec the offsets shall be sorted ascending
        // but we can't rely on that, so that we have to sort the offsets
        // as the sequential parsers relies on it, see PDFBOX-4927
        Map<Integer, Long> objectOffsets = new TreeMap<>();
        long firstObjectPosition = source.getPosition() + firstObject - 1;
        for (int i = 0; i < numberOfObjects; i++)
        {
            // don't read beyond the part of the stream reserved for the object numbers
            if (source.getPosition() >= firstObjectPosition)
            {
                break;
            }
            long objectNumber = readObjectNumber();
            int offset = (int) readLong();
            objectOffsets.put(offset, objectNumber);
        }
        return objectOffsets;
    }

    /**
     * Read all object numbers from the compressed object stream. The stream is closed after reading the object numbers.
     * 
     * @return a map off all object numbers and the corresponding offset within the object stream.
     * @throws IOException if there is an error while parsing the stream
     */
    public Map<Long, Integer> readObjectNumbers() throws IOException
    {
        Map<Long, Integer> objectNumbers = null;
        try
        {
            objectNumbers = privateReadObjectNumbers();
        }
        finally
        {
            source.close();
            document = null;
        }
        return objectNumbers;
    }
}
