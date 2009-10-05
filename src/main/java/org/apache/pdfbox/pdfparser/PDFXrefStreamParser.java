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
import java.util.Iterator;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This will parse a PDF 1.5 (or better) Xref stream and 
 * extract the xref information from the stream.
 * 
 *  @author <a href="mailto:justinl@basistech.com">Justin LeFebvre</a>
 *  @version $Revision: 1.0 $
 */
public class PDFXrefStreamParser extends BaseParser 
{
    private COSStream stream;

    /**
     * Constructor.
     *
     * @param strm The stream to parse.
     * @param doc The document for the current parsing.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFXrefStreamParser(COSStream strm, COSDocument doc) throws IOException
    {
        super(strm.getUnfilteredStream());
        setDocument(doc);
        stream = strm;
    }

    /**
     * Parses through the unfiltered stream and populates the xrefTable HashMap.
     * @throws IOException If there is an error while parsing the stream.
     */
    public void parse() throws IOException
    {
        try
        {
            COSArray xrefFormat = (COSArray)stream.getDictionaryObject("W");
            COSArray indexArray = (COSArray)stream.getDictionaryObject("Index");
            /*
             * If Index doesn't exist, we will use the default values. 
             */
            if(indexArray == null)
            {
                indexArray = new COSArray();
                indexArray.add(new COSInteger(0));
                indexArray.add(stream.getDictionaryObject("Size"));
            }
            
            ArrayList objNums = new ArrayList();
            
            /*
             * Populates objNums with all object numbers available
             */
            Iterator indexIter = indexArray.iterator();
            while(indexIter.hasNext())
            {
                int objID = ((COSInteger)indexIter.next()).intValue();
                int size = ((COSInteger)indexIter.next()).intValue();
                for(int i = 0; i < size; i++)
                {
                    objNums.add(new Integer(objID + i));
                }
            }
            Iterator objIter = objNums.iterator();
            /*
             * Calculating the size of the line in bytes
             */
            int w0 = xrefFormat.getInt(0);
            int w1 = xrefFormat.getInt(1);
            int w2 = xrefFormat.getInt(2);
            int lineSize = w0 + w1 + w2;
            
            while(pdfSource.available() > 0 && objIter.hasNext())
            {
                byte[] currLine = new byte[lineSize];
                pdfSource.read(currLine);

                int type = 0;
                /*
                 * Grabs the number of bytes specified for the first column in 
                 * the W array and stores it.
                 */
                for(int i = 0; i < w0; i++)
                {
                    type += (currLine[i] & 0x00ff) << ((w0 - i - 1)* 8);
                }
                //Need to remember the current objID
                Integer objID = (Integer)objIter.next();
                /*
                 * 3 different types of entries. 
                 */
                switch(type)
                {
                    case 0:
                        /*
                         * Skipping free objects
                         */
                        break;
                    case 1:                   
                        int offset = 0;
                        for(int i = 0; i < w1; i++)
                        {
                            offset += (currLine[i + w0] & 0x00ff) << ((w1 - i - 1) * 8);
                        }
                        int genNum = 0;
                        for(int i = 0; i < w2; i++)
                        {
                            genNum += (currLine[i + w0 + w1] & 0x00ff) << ((w2 - i - 1) * 8);
                        }
                        COSObjectKey objKey = new COSObjectKey(objID.intValue(), genNum);
                        document.setXRef(objKey, offset);
                        break;
                    case 2:
                        /*
                         * These objects are handled by the dereferenceObjects() method
                         * since they're only pointing to object numbers
                         */
                        break;
                    default:
                        break;
                }
            }
        }
        finally
        {
            pdfSource.close();
        }
    }
}
