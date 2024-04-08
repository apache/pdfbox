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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * This will parse a PDF 1.5 (or better) Xref stream and
 * extract the xref information from the stream.
 *
 *  @author Justin LeFebvre
 */
public class PDFXrefStreamParser
{
    private final int[] w = new int[3];
    private ObjectNumbers objectNumbers = null;
    private final RandomAccessRead source;

    /**
     * Constructor.
     *
     * @param stream The stream to parse.
     *
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFXrefStreamParser(COSStream stream) throws IOException
    {
        source = stream.createView();
        try
        {
            initParserValues(stream);
        }
        catch (IOException exception)
        {
            close();
            throw exception;
        }
    }

    private void initParserValues(COSStream stream) throws IOException
    {
        COSArray wArray = stream.getCOSArray(COSName.W);
        if (wArray == null)
        {
            throw new IOException("/W array is missing in Xref stream");
        }
        if (wArray.size() != 3)
        {
            throw new IOException(
                    "Wrong number of values for /W array in XRef: " + Arrays.toString(w));
        }
        for (int i = 0; i < 3; i++)
        {
            w[i] = wArray.getInt(i, 0);
        }
        if (w[0] < 0 || w[1] < 0 || w[2] < 0)
        {
            throw new IOException("Incorrect /W array in XRef: " + Arrays.toString(w));
        }

        COSArray indexArray = stream.getCOSArray(COSName.INDEX);
        if (indexArray == null)
        {
            // If /Index doesn't exist, we will use the default values.
            indexArray = new COSArray();
            indexArray.add(COSInteger.ZERO);
            indexArray.add(COSInteger.get(stream.getInt(COSName.SIZE, 0)));
        }
        if (indexArray.isEmpty() || indexArray.size() % 2 == 1)
        {
            throw new IOException(
                    "Wrong number of values for /Index array in XRef: " + Arrays.toString(w));
        }
        // create an Iterator for all object numbers using the index array
        objectNumbers = new ObjectNumbers(indexArray);
    }

    private void close() throws IOException
    {
        if (source != null)
        {
            source.close();
        }
        objectNumbers = null;
    }

    /**
     * Parses through the unfiltered stream and populates the xrefTable HashMap.
     * 
     * @param resolver resolver to read the xref/trailer information
     * @throws IOException If there is an error while parsing the stream.
     */
    public void parse(XrefTrailerResolver resolver) throws IOException
    {
        byte[] currLine = new byte[w[0] + w[1] + w[2]];
        while (!source.isEOF() && objectNumbers.hasNext())
        {
            readNextValue(currLine);
            // get the current objID
            long objID = objectNumbers.next();
            // default value is 1 if w[0] == 0, otherwise parse first field
            int type = w[0] == 0 ? 1 : (int) parseValue(currLine, 0, w[0]);
            // Skip free objects (type 0) and invalid types
            if (type == 0)
            {
                continue;
            }
            // second field holds the offset (type 1) or the object stream number (type 2)
            long offset = parseValue(currLine, w[0], w[1]);
            // third filed may hold the generation number (type1) or the index within a object stream (type2)
            int thirdValue = (int) parseValue(currLine, w[0] + w[1], w[2]);
            if (type == 1)
            {
                // third field holds the generation number for type 1 entries
                resolver.setXRef(new COSObjectKey(objID, thirdValue), offset);
            }
            else
            {
                // For XRef aware parsers we have to know which objects contain object streams. We will store this
                // information in normal xref mapping table but add object stream number with minus sign in order to
                // distinguish from file offsets
                resolver.setXRef(new COSObjectKey(objID, 0, thirdValue), -offset);
            }
        }
        close();
    }

    private void readNextValue(byte[] value) throws IOException
    {
        int remainingBytes = value.length;
        int amountRead;
        while ((amountRead = source.read(value, value.length - remainingBytes, remainingBytes)) > 0)
        {
            remainingBytes -= amountRead;
        }
    }

    private long parseValue(byte[] data, int start, int length)
    {
        long value = 0;
        for (int i = 0; i < length; i++)
        {
            value += ((long) data[i + start] & 0x00ff) << ((length - i - 1) * 8);
        }
        return value;
    }

    private static class ObjectNumbers implements Iterator<Long>
    {
        private final long[] start;
        private final long[] end;
        private int currentRange = 0;
        private long currentEnd = 0;
        private long currentNumber = 0;

        private ObjectNumbers(COSArray indexArray) throws IOException
        {
            start = new long[indexArray.size() / 2];
            end = new long[start.length];
            int counter = 0;
            Iterator<COSBase> indexIter = indexArray.iterator();
            while (indexIter.hasNext())
            {
                COSBase base = indexIter.next();
                if (!(base instanceof COSInteger))
                {
                    throw new IOException("Xref stream must have integer in /Index array");
                }
                long startValue = ((COSInteger) base).longValue();
                if (!indexIter.hasNext())
                {
                    break;
                }
                base = indexIter.next();
                if (!(base instanceof COSInteger))
                {
                    throw new IOException("Xref stream must have integer in /Index array");
                }
                long sizeValue = ((COSInteger) base).longValue();
                start[counter] = startValue;
                end[counter] = startValue + sizeValue;
                counter++;
            }
            currentNumber = start[0];
            currentEnd = end[0];
        }

        @Override
        public boolean hasNext()
        {
            if (start.length == 1)
            {
                return currentNumber < currentEnd;
            }
            return currentRange < start.length - 1 || currentNumber < currentEnd;
        }

        @Override
        public Long next()
        {
            if (currentNumber < currentEnd)
            {
                return currentNumber++;
            }
            if (currentRange >= start.length - 1)
            {
                throw new NoSuchElementException();
            }
            currentNumber = start[++currentRange];
            currentEnd = end[currentRange];
            return currentNumber++;
        }
    }

}
