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
     * Parse all objects of the stream. This will close the stream when it is finished parsing.
     *
     * @return All of the objects in the stream.
     * @throws IOException If there is an error while parsing the stream.
     */
    public List<COSObject> parse() throws IOException
    {
        List<COSObject> streamObjects = new ArrayList<>();
        try
        {
            List<Long> objectNumbers = new ArrayList<>( numberOfObjects );
            for( int i=0; i<numberOfObjects; i++ )
            {
                long objectNumber = readObjectNumber();
                // skip offset
                readLong();
                objectNumbers.add( objectNumber);
            }
            COSObject object;
            COSBase cosObject;
            int objectCounter = 0;
            while( (cosObject = parseDirObject()) != null )
            {
                if (objectCounter >= objectNumbers.size())
                {
                    LOG.error("/ObjStm (object stream) has more objects than /N " + numberOfObjects);
                    break;
                }
                object = new COSObject(cosObject);
                object.setGenerationNumber(0);
                object.setObjectNumber( objectNumbers.get( objectCounter) );
                streamObjects.add( object );
                // According to the spec objects within an object stream shall not be enclosed 
                // by obj/endobj tags, but there are some pdfs in the wild using those tags 
                // skip endobject marker if present
                if (!isEOF() && seqSource.peek() == 'e')
                {
                    readLine();
                }
                objectCounter++;
            }
        }
        finally
        {
            seqSource.close();
        }
        return streamObjects;
    }

    /**
     * Search for/parse the object with the given object number. This will close the stream when it is finished parsing.
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
            Integer objectOffset = readObjectNumbers().get(objectNumber);
            if (objectOffset != null) 
            {
                // jump to the offset of the first object
                long currentPosition = seqSource.getPosition();
                if (firstObject > 0 && currentPosition < firstObject)
                {
                    seqSource.readFully(firstObject - (int) currentPosition);
                }
                // jump to the offset of the object to be parsed
                seqSource.readFully(objectOffset);
                streamObject = parseDirObject();
            }
        }
        finally
        {
            seqSource.close();
        }
        return streamObject;
    }

    private Map<Long, Integer> readObjectNumbers() throws IOException
    {
        Map<Long, Integer> objectNumbers = new HashMap<>(numberOfObjects);
        for (int i = 0; i < numberOfObjects; i++)
        {
            long objectNumber = readObjectNumber();
            int offset = (int) readLong();
            objectNumbers.put(objectNumber, offset);
        }
        return objectNumbers;
    }

}
