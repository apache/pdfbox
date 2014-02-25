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
import org.apache.pdfbox.cos.COSDictionary;

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

    /**
     * The LZW code table.
     */
    private ArrayList<byte[]> codeTable = null;

    /**
     * {@inheritDoc}
     */
    public void decode(InputStream compressedData, OutputStream result, COSDictionary options, int filterIndex)
            throws IOException
    {
        codeTable = null;
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
                    initCodeTable();
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
                            byte[] newData = Arrays.copyOf(data, data.length + 1);
                            newData[data.length] = firstByte;
                            codeTable.add(newData);
                        }
                    }
                    else
                    {
                        byte[] data = codeTable.get((int) prevCommand);
                        byte[] newData = Arrays.copyOf(data, data.length + 1);
                        newData[data.length] = data[0];
                        result.write(newData);
                        codeTable.add(newData);
                    }
                    if (codeTable.size() >= 2047)
                    {
                        chunk = 12;
                    }
                    else if (codeTable.size() >= 1023)
                    {
                        chunk = 11;
                    }
                    else if (codeTable.size() >= 511)
                    {
                        chunk = 10;
                    }
                    else
                    {
                        chunk = 9;
                    }
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
        initCodeTable();
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
                inputPattern = Arrays.copyOf(inputPattern, inputPattern.length + 1);
                inputPattern[inputPattern.length - 1] = by;
                int newFoundCode = findPatternCode(codeTable, inputPattern);
                if (newFoundCode == -1)
                {
                    // use previous
                    out.writeBits(foundCode, chunk);
                    // create new table entry
                    codeTable.add(inputPattern);

                    if (codeTable.size() == 4096)
                    {
                        // code table is full
                        out.writeBits(CLEAR_TABLE, chunk);
                        chunk = 9;
                        initCodeTable();
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
            if (codeTable.size() - 1 >= 2047)
            {
                chunk = 12;
            }
            else if (codeTable.size() - 1 >= 1023)
            {
                chunk = 11;
            }
            else if (codeTable.size() - 1 >= 511)
            {
                chunk = 10;
            }
            else
            {
                chunk = 9;
            }
        }
        if (foundCode != -1)
        {
            out.writeBits(foundCode, chunk);
        }
        out.writeBits(EOD, chunk);
        out.writeBits(0, 7);
        out.flush(); // must do or file will be empty :-(
        codeTable.clear();
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
    private void initCodeTable()
    {
        codeTable = new ArrayList<byte[]>(4096);
        for (int i = 0; i < 256; ++i)
        {
            codeTable.add(new byte[]
            {
                (byte) (i & 0xFF)
            });
        }
        codeTable.add(null); // 256 EOD
        codeTable.add(null); // 257 CLEAR_TABLE
    }

}
