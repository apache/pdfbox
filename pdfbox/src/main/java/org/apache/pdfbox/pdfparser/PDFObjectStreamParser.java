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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
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
    private final COSStream stream;

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
        this.stream = stream;
        this.document = document;
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
            //need to first parse the header.
            int numberOfObjects = stream.getInt( "N" );
            List<Long> objectNumbers = new ArrayList<Long>( numberOfObjects );
            streamObjects = new ArrayList<COSObject>( numberOfObjects );
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
                object = new COSObject(cosObject);
                object.setGenerationNumber(0);
                if (objectCounter >= objectNumbers.size())
                {
                    LOG.error("/ObjStm (object stream) has more objects than /N " + numberOfObjects);
                    break;
                }
                object.setObjectNumber( objectNumbers.get( objectCounter) );
                streamObjects.add( object );
                if(LOG.isDebugEnabled())
                {
                    LOG.debug( "parsed=" + object );
                }
                // According to the spec objects within an object stream shall not be enclosed 
                // by obj/endobj tags, but there are some pdfs in the wild using those tags 
                // skip endobject marker if present
                if (!seqSource.isEOF() && seqSource.peek() == 'e')
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
}
