/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 *
 * This is the filter used for the LZWDecode filter.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Tilman Hausherr
 */
public class LZWFilter implements Filter
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(LZWFilter.class);

    /**
     * The LZW clear table code.
     */
    public static final long CLEAR_TABLE = 256;

    /**
     * The LZW end of data code.
     */
    public static final long EOD = 257;

    //BEWARE: codeTable must be local to each method, because there is only
    // one instance of each filter

    /**
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex)
            throws IOException
    {
        COSBase baseObj = options.getDictionaryObject(COSName.DECODE_PARMS, COSName.DP);
        COSDictionary decodeParams = null;
        if (baseObj instanceof COSDictionary)
        {
            decodeParams = (COSDictionary) baseObj;
        }
        else if (baseObj instanceof COSArray)
        {
            COSArray paramArray = (COSArray) baseObj;
            if (filterIndex < paramArray.size())
            {
                decodeParams = (COSDictionary) paramArray.getObject(filterIndex);
            }
        }
        else if (baseObj != null)
        {
            throw new IOException("Error: Expected COSArray or COSDictionary and not "
                    + baseObj.getClass().getName());
        }

        int predictor = -1;
        int earlyChange = 1;
        if (decodeParams != null)
        {
            predictor = decodeParams.getInt(COSName.PREDICTOR);
            earlyChange = decodeParams.getInt(COSName.EARLY_CHANGE, 1);
            if (earlyChange != 0 && earlyChange != 1)
            {
                earlyChange = 1;
            }
        }
        if (predictor > 1)
        {
            int colors = Math.min(decodeParams.getInt(COSName.COLORS, 1), 32);
            int bitsPerPixel = decodeParams.getInt(COSName.BITS_PER_COMPONENT, 8);
            int columns = decodeParams.getInt(COSName.COLUMNS, 1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doLZWDecode(compressedData, baos, earlyChange);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Predictor.decodePredictor(predictor, colors, bitsPerPixel, columns, bais, result);
            result.flush();
            baos.reset();
            bais.reset();
        }
        else
        {
            doLZWDecode(compressedData, result, earlyChange);
        }
    }

    private void doLZWDecode(InputStream compressedData, OutputStream result, int earlyChange) throws IOException
    {
        ArrayList<byte[]> codeTable = null;
        int chunk = 9;
        MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(compressedData);
        long nextCommand = 0;
        long prevCommand = -1;

        try
        {
            while ((nextCommand = in.readBits(chunk)) != EOD)
            {
                if (nextCommand == CLEAR_TABLE)
                {
                    chunk = 9;
                    codeTable = createCodeTable();
                    prevCommand = -1;
                }
                else
                {
                    if (nextCommand < codeTable.size())
                    {
                        byte[] data = codeTable.get((int) nextCommand);
                        byte firstByte = data[0];
                        result.write(data);
                        if (prevCommand != -1)
                        {
                            data = codeTable.get((int) prevCommand);
                            byte[] newData = new byte[data.length + 1];
                            for (int i = 0; i < data.length; ++i)
                            {
                                newData[i] = data[i];
                            }
                            newData[data.length] = firstByte;
                            codeTable.add(newData);
                        }
                    }
                    else
                    {
                        byte[] data = codeTable.get((int) prevCommand);
                        byte[] newData = new byte[data.length + 1];
                        for (int i = 0; i < data.length; ++i)
                        {
                            newData[i] = data[i];
                        }
                        newData[data.length] = data[0];
                        result.write(newData);
                        codeTable.add(newData);
                    }

                    chunk = calculateChunk(codeTable.size(), earlyChange);
                    prevCommand = nextCommand;
                }
            }
        }
        catch (EOFException ex)
        {
            LOG.warn("Premature EOF in LZW stream, EOD code missing");
        }
        result.flush();
    }

    /**
     * {@inheritDoc}
     */
    public void encode(InputStream rawData, OutputStream result, COSDictionary options, int filterIndex)
            throws IOException
    {
        ArrayList<byte[]> codeTable = createCodeTable();
        int chunk = 9;

        byte[] inputPattern = null;
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(result);
        out.writeBits(CLEAR_TABLE, chunk);
        int foundCode = -1;
        int r;
        while ((r = rawData.read()) != -1)
        {
            byte by = (byte) r;
            if (inputPattern == null)
            {
                inputPattern = new byte[]
                {
                    by
                };
                foundCode = by & 0xff;
            }
            else
            {
                byte[] inputPatternCopy = new byte[inputPattern.length + 1];
                for (int i = 0; i < inputPattern.length; ++i)
                {
                    inputPatternCopy[i] = inputPattern[i];
                }
                inputPattern = inputPatternCopy;
                inputPattern[inputPattern.length - 1] = by;
                int newFoundCode = findPatternCode(codeTable, inputPattern);
                if (newFoundCode == -1)
                {
                    // use previous
                    chunk = calculateChunk(codeTable.size() - 1, 1);
                    out.writeBits(foundCode, chunk);
                    // create new table entry
                    codeTable.add(inputPattern);

                    if (codeTable.size() == 4096)
                    {
                        // code table is full
                        out.writeBits(CLEAR_TABLE, chunk);
                        chunk = 9;
                        codeTable = createCodeTable();
                    }

                    inputPattern = new byte[]
                    {
                        by
                    };
                    foundCode = by & 0xff;
                }
                else
                {
                    foundCode = newFoundCode;
                }
            }
        }
        if (foundCode != -1)
        {
            chunk = calculateChunk(codeTable.size() - 1, 1);
            out.writeBits(foundCode, chunk);
        }

        // PPDFBOX-1977: the decoder wouldn't know that the encoder would output 
        // an EOD as code, so he would have increased his own code table and 
        // possibly adjusted the chunk. Therefore, the encoder must behave as 
        // if the code table had just grown and thus it must be checked it is
        // needed to adjust the chunk, based on an increased table size parameter
        chunk = calculateChunk(codeTable.size(), 1);

        out.writeBits(EOD, chunk);
        out.writeBits(0, 7); // pad with 0
        out.flush(); // must do or file will be empty :-(
    }

    /**
     * Find the longest matching pattern in the code table.
     *
     * @param codeTable The LZW code table.
     * @param pattern The pattern to be searched for.
     * @return The index of the longest matching pattern or -1 if nothing is
     * found.
     */
    private int findPatternCode(ArrayList<byte[]> codeTable, byte[] pattern)
    {
        int foundCode = -1;
        int foundLen = 0;
        for (int i = codeTable.size() - 1; i >= 0; --i)
        {
            if (i <= EOD)
            {
                // we're in the single byte area
                if (foundCode != -1)
                {
                    return foundCode; // we already found pattern with size > 1
                }
                else if (pattern.length > 1)
                {
                    return -1; // we won't find anything here anyway
                }
            }
            byte[] tryPattern = codeTable.get(i);
            if (foundCode != -1 || tryPattern.length > foundLen)
            {
                if (Arrays.equals(tryPattern, pattern))
                {
                    foundCode = i;
                    foundLen = tryPattern.length;
                }
            }
        }
        return foundCode;
    }

    /**
     * Init the code table with 1 byte entries and the EOD and CLEAR_TABLE
     * markers.
     */
    private ArrayList<byte[]> createCodeTable()
    {
        ArrayList<byte[]> codeTable = new ArrayList<byte[]>(4096);
        for (int i = 0; i < 256; ++i)
        {
            codeTable.add(new byte[]
            {
                (byte) (i & 0xFF)
            });
        }
        codeTable.add(null); // 256 EOD
        codeTable.add(null); // 257 CLEAR_TABLE
        return codeTable;
    }

    /**
     * Calculate the appropriate chunk size
     *
     * @param tabSize the size of the code table
     * @param earlyChange 0 or 1 for early chunk increase
     *
     * @return a value between 9 and 12
     */
    private int calculateChunk(int tabSize, int earlyChange)
    {
        if (tabSize >= 2048 - earlyChange)
        {
            return 12;
        }
        if (tabSize >= 1024 - earlyChange)
        {
            return 11;
        }
        if (tabSize >= 512 - earlyChange)
        {
            return 10;
        }
        return 9;
    }
}
