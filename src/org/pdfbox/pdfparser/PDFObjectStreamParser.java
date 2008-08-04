/**
 * Copyright (c) 2003-2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdfparser;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;

/**
 * This will parse a PDF 1.5 object stream and extract all of the objects from the stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDFObjectStreamParser extends BaseParser
{
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