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
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

/**
 * This will parse a PDF 1.5 object stream and extract all of the objects from the stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDFObjectStreamParser extends BaseParser
{
    /**
     * Log instance.
     */
    private static final Log log =
        LogFactory.getLog(PDFObjectStreamParser.class);

    private List streamObjects = null;
    private List objectNumbers = null;
    private COSStream stream;

    /**
     * Constructor.
     *
     * @param strm The stream to parse.
     * @param doc The document for the current parsing.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFObjectStreamParser( COSStream strm, COSDocument doc ) throws IOException
    {
       super( strm.getUnfilteredStream() );
       setDocument( doc );
       stream = strm;
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
            objectNumbers = new ArrayList( numberOfObjects );
            streamObjects = new ArrayList( numberOfObjects );
            for( int i=0; i<numberOfObjects; i++ )
            {
                int objectNumber = readInt();
                int offset = readInt();
                objectNumbers.add( new Integer( objectNumber ) );
            }
            COSObject object = null;
            COSBase cosObject = null;
            int objectCounter = 0;
            while( (cosObject = parseDirObject()) != null )
            {
                object = new COSObject(cosObject);
                object.setGenerationNumber( COSInteger.ZERO );
                COSInteger objNum =
                    new COSInteger( ((Integer)objectNumbers.get( objectCounter)).intValue() );
                object.setObjectNumber( objNum );
                streamObjects.add( object );
                if(log.isDebugEnabled())
                {
                    log.debug( "parsed=" + object );
                }
                objectCounter++;
            }
        }
        finally
        {
            pdfSource.close();
        }
    }

    /**
     * This will get the objects that were parsed from the stream.
     *
     * @return All of the objects in the stream.
     */
    public List getObjects()
    {
        return streamObjects;
    }
}
