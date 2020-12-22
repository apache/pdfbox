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
    public PDFXrefStreamParser(final COSStream stream, final COSDocument document)
            throws IOException
    {
        super(stream.createView());
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

    private void initParserValues(final COSStream stream) throws IOException
    {
        final COSArray wArray = stream.getCOSArray(COSName.W);
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
        final Iterator<COSBase> indexIter = indexArray.iterator();
        while (indexIter.hasNext())
        {
            COSBase base = indexIter.next();
            if (!(base instanceof COSInteger))
            {
                throw new IOException("Xref stream must have integer in /Index array");
            }
            final long objID = ((COSInteger) base).longValue();
            if (!indexIter.hasNext())
            {
                break;
            }
            base = indexIter.next();
            if (!(base instanceof COSInteger))
            {
                throw new IOException("Xref stream must have integer in /Index array");
            }
            final int size = ((COSInteger) base).intValue();
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
    public void parse(final XrefTrailerResolver resolver) throws IOException
    {
        final byte[] currLine = new byte[w[0] + w[1] + w[2]];
        final Iterator<Long> objIter = objNums.iterator();
        while (!isEOF() && objIter.hasNext())
        {
            source.read(currLine);

            // get the current objID
            final Long objID = objIter.next();

            // default value is 1 if w[0] == 0, otherwise parse first field
            final int type = w[0] == 0 ? 1 : (int) parseValue(currLine, 0, w[0]);
            // Skip free objects (type 0) and invalid types
            if (type == 0)
            {
                continue;
            }
            // second field holds the offset (type 1) or the object stream number (type 2)
            final long offset = parseValue(currLine, w[0], w[1]);
            // third field holds the generation number for type 1 entries
            final int genNum = type == 1 ? (int) parseValue(currLine, w[0] + w[1], w[2]) : 0;
            final COSObjectKey objKey = new COSObjectKey(objID, genNum);
            if (type == 1)
            {
                resolver.setXRef(objKey, offset);
            }
            else
            {
                // For XRef aware parsers we have to know which objects contain object streams. We will store this
                // information in normal xref mapping table but add object stream number with minus sign in order to
                // distinguish from file offsets
                resolver.setXRef(objKey, -offset);
            }
        }
        close();
    }

    private long parseValue(final byte[] data, final int start, final int length)
    {
        long value = 0;
        for (int i = 0; i < length; i++)
        {
            value += ((long) data[i + start] & 0x00ff) << ((length - i - 1) * 8);
        }
        return value;
    }

}