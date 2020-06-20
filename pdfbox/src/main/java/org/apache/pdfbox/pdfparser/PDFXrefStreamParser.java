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
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.InputStreamRandomAccessRead;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * This will parse a PDF 1.5 (or better) Xref stream and
 * extract the xref information from the stream.
 *
 *  @author Justin LeFebvre
 */
public class PDFXrefStreamParser extends BaseParser
{
    private final int[] w = new int[3];
    private final List<Long> objNums = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param stream The stream to parse.
     * @param document The document for the current parsing.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFXrefStreamParser(COSStream stream, COSDocument document)
            throws IOException
    {
        super(new InputStreamRandomAccessRead(stream.createInputStream()));
        this.document = document;
        try
        {
            initParserValues(stream);
        }
        catch (IOException exception)
        {
            close();
        }
    }

    private void initParserValues(COSStream stream) throws IOException
    {
        COSArray wArray = stream.getCOSArray(COSName.W);
        if (wArray == null)
        {
            throw new IOException("/W array is missing in Xref stream");
        }
        for (int i = 0; i < 3; i++)
        {
            w[i] = wArray.getInt(i, 0);
        }

        COSArray indexArray = stream.getCOSArray(COSName.INDEX);
        if (indexArray == null)
        {
            // If /Index doesn't exist, we will use the default values.
            indexArray = new COSArray();
            indexArray.add(COSInteger.ZERO);
            indexArray.add(COSInteger.get(stream.getInt(COSName.SIZE, 0)));
        }

        /*
         * Populates objNums with all object numbers available
         */
        Iterator<COSBase> indexIter = indexArray.iterator();
        while (indexIter.hasNext())
        {
            COSBase base = indexIter.next();
            if (!(base instanceof COSInteger))
            {
                throw new IOException("Xref stream must have integer in /Index array");
            }
            long objID = ((COSInteger) base).longValue();
            if (!indexIter.hasNext())
            {
                break;
            }
            base = indexIter.next();
            if (!(base instanceof COSInteger))
            {
                throw new IOException("Xref stream must have integer in /Index array");
            }
            int size = ((COSInteger) base).intValue();
            for (int i = 0; i < size; i++)
            {
                objNums.add(objID + i);
            }
        }
    }

    private void close() throws IOException
    {
        if (source != null)
        {
            source.close();
        }
        document = null;
        objNums.clear();
    }

    /**
     * Parses through the unfiltered stream and populates the xrefTable HashMap.
     * 
     * @param resolver resolver to read the xref/trailer information
     * @throws IOException If there is an error while parsing the stream.
     */
    public void parse(XrefTrailerResolver resolver) throws IOException
    {
        /*
         * Calculating the size of the line in bytes
         */
        int lineSize = w[0] + w[1] + w[2];

        Iterator<Long> objIter = objNums.iterator();
        while (!isEOF() && objIter.hasNext())
        {
            byte[] currLine = new byte[lineSize];
            source.read(currLine);

            int type;            
            if (w[0] == 0)
            {
                // "If the first element is zero, 
                // the type field shall not be present, and shall default to type 1"
                type = 1;
            }
            else
            {
                type = 0;
                /*
                 * Grabs the number of bytes specified for the first column in
                 * the W array and stores it.
                 */
                for (int i = 0; i < w[0]; i++)
                {
                    type += (currLine[i] & 0x00ff) << ((w[0] - i - 1) * 8);
                }
            }
            //Need to remember the current objID
            Long objID = objIter.next();
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
                    long offset = 0;
                    for (int i = 0; i < w[1]; i++)
                    {
                        offset += ((long) currLine[i + w[0]] & 0x00ff) << ((w[1] - i - 1) * 8);
                    }
                    int genNum = 0;
                    for (int i = 0; i < w[2]; i++)
                    {
                        genNum += (currLine[i + w[0] + w[1]] & 0x00ff) << ((w[2] - i - 1) * 8);
                    }
                    COSObjectKey objKey = new COSObjectKey(objID, genNum);
                    resolver.setXRef(objKey, offset);
                    break;
                case 2:
                    /*
                     * object stored in object stream: 
                     * 2nd argument is object number of object stream
                     * 3rd argument is index of object within object stream
                     * 
                     * For XRef aware parsers we have to know which objects contain
                     * object streams. We will store this information in normal xref mapping
                     * table but add object stream number with minus sign in order to
                     * distinguish from file offsets
                     */
                    long objstmObjNr = 0;
                    for (int i = 0; i < w[1]; i++)
                    {
                        objstmObjNr += ((long) currLine[i + w[0]] & 0x00ff) << ((w[1] - i - 1) * 8);
                    }    
                    objKey = new COSObjectKey( objID, 0 );
                    resolver.setXRef(objKey, -objstmObjNr);
                    break;
                default:
                    break;
            }
        }
        close();
    }

}
