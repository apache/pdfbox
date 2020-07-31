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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

/**
 * This will parse a PDF 1.5 object stream and extract all of the objects from the stream.
 *
 * @author Ben Litchfield
 * 
 */
public class PDFObjectStreamParser extends BaseParser
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFObjectStreamParser.class);

    private List<COSObject> streamObjects = null;
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
        super(new InputStreamSource(stream.createInputStream()));
        this.document = document;
        // get mandatory number of objects
        numberOfObjects = stream.getInt(COSName.N);
        if (numberOfObjects == -1)
        {
            throw new IOException("/N entry missing in object stream");
        }
        // get mandatory stream offset of the first object
        firstObject = stream.getInt(COSName.FIRST);
        if (firstObject == -1)
        {
            throw new IOException("/First entry missing in object stream");
        }
    }

    /**
     * This will parse the tokens in the stream.  This will close the
     * stream when it is finished parsing.
     *
     * @throws IOException If there is an error while parsing the stream.
     */
    public void parse() throws IOException
    {
        try
        {
            Map<Integer, Long> offsets = readOffsets();
            streamObjects = new ArrayList<COSObject>( numberOfObjects );
            for (Entry<Integer, Long> offset : offsets.entrySet())
            {
                COSBase cosObject = parseObject(offset.getKey());
                COSObject object = new COSObject(cosObject);
                object.setGenerationNumber(0);
                object.setObjectNumber(offset.getValue());
                streamObjects.add(object);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("parsed=" + object);
                }
            }
        }
        finally
        {
            seqSource.close();
        }
    }

    /**
     * This will get the objects that were parsed from the stream.
     *
     * @return All of the objects in the stream.
     */
    public List<COSObject> getObjects()
    {
        return streamObjects;
    }

    private Map<Integer, Long> readOffsets() throws IOException
    {
        // according to the pdf spec the offsets shall be sorted ascending
        // but we can't rely on that, so that we have to sort the offsets
        // as the sequential parsers relies on it, see PDFBOX-4927
        Map<Integer, Long> objectNumbers = new TreeMap<Integer, Long>();
        for (int i = 0; i < numberOfObjects; i++)
        {
            long objectNumber = readObjectNumber();
            int offset = (int) readLong();
            objectNumbers.put(offset, objectNumber);
        }
        return objectNumbers;
    }

    private COSBase parseObject(int offset) throws IOException
    {
        long currentPosition = seqSource.getPosition();
        int finalPosition = firstObject + offset;
        if (finalPosition > 0 && currentPosition < finalPosition)
        {
            // jump to the offset of the object to be parsed
            seqSource.readFully(finalPosition - (int) currentPosition);
        }
        return parseDirObject();
    }

}
