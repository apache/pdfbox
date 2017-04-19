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
import java.util.List;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 *
 * This is the filter used for the LZWDecode filter.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class LZWFilter extends Filter
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
    @Override
    public DecodeResult decode(InputStream encoded, OutputStream decoded,
            COSDictionary parameters, int index) throws IOException
    {
        int predictor = -1;
        int earlyChange = 1;

        COSDictionary decodeParams = getDecodeParams(parameters, index);
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
            @SuppressWarnings("null")
            int colors = Math.min(decodeParams.getInt(COSName.COLORS, 1), 32);
            int bitsPerPixel = decodeParams.getInt(COSName.BITS_PER_COMPONENT, 8);
            int columns = decodeParams.getInt(COSName.COLUMNS, 1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doLZWDecode(encoded, baos, earlyChange);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Predictor.decodePredictor(predictor, colors, bitsPerPixel, columns, bais, decoded);
            decoded.flush();
            baos.reset();
            bais.reset();
        }
        else
        {
            doLZWDecode(encoded, decoded, earlyChange);
        }
        return new DecodeResult(parameters);
    }

    private void doLZWDecode(InputStream encoded, OutputStream decoded, int earlyChange) throws IOException
    {
        List<byte[]> codeTable = new ArrayList<>();
        int chunk = 9;
        final MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(encoded);
        long nextCommand;
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
                        decoded.write(data);
                        if (prevCommand != -1)
                        {
                            checkIndexBounds(codeTable, prevCommand, in);
                            data = codeTable.get((int) prevCommand);
                            byte[] newData = Arrays.copyOf(data, data.length + 1);
                            newData[data.length] = firstByte;
                            codeTable.add(newData);
                        }
                    }
                    else
                    {
                        checkIndexBounds(codeTable, prevCommand, in);
                        byte[] data = codeTable.get((int) prevCommand);
                        byte[] newData = Arrays.copyOf(data, data.length + 1);
                        newData[data.length] = data[0];
                        decoded.write(newData);
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
        decoded.flush();
    }

    private void checkIndexBounds(List<byte[]> codeTable, long index, MemoryCacheImageInputStream in)
            throws IOException
    {
        if (index < 0)
        {
            throw new IOException("negative array index: " + index + " near offset "
                    + in.getStreamPosition());
        }
        if (index >= codeTable.size())
        {
            throw new IOException("array index overflow: " + index +
                    " >= " + codeTable.size() + " near offset "
                    + in.getStreamPosition());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encode(InputStream rawData, OutputStream encoded, COSDictionary parameters)
            throws IOException
    {
        List<byte[]> codeTable = createCodeTable();
        int chunk = 9;

        byte[] inputPattern = null;
        try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(encoded))
        {
            out.writeBits(CLEAR_TABLE, chunk);
            int foundCode = -1;
            int r;
            while ((r = rawData.read()) != -1)
            {
                byte by = (byte) r;
                if (inputPattern == null)
                {
                    inputPattern = new byte[] { by };
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
                        chunk = calculateChunk(codeTable.size() - 1, 1);
                        out.writeBits(foundCode, chunk);
                        // create new table entry
                        codeTable.add(inputPattern);
                        
                        if (codeTable.size() == 4096)
                        {
                            // code table is full
                            out.writeBits(CLEAR_TABLE, chunk);
                            codeTable = createCodeTable();
                        }
                        
                        inputPattern = new byte[] { by };
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
            
            // pad with 0
            out.writeBits(0, 7);
            
            // must do or file will be empty :-(
            out.flush();
        }
    }

    /**
     * Find the longest matching pattern in the code table.
     *
     * @param codeTable The LZW code table.
     * @param pattern The pattern to be searched for.
     * @return The index of the longest matching pattern or -1 if nothing is
     * found.
     */
    private int findPatternCode(List<byte[]> codeTable, byte[] pattern)
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
                    // we already found pattern with size > 1
                    return foundCode; 
                }
                else if (pattern.length > 1)
                {
                    // we won't find anything here anyway
                    return -1;
                }
            }
            byte[] tryPattern = codeTable.get(i);
            if ((foundCode != -1 || tryPattern.length > foundLen) && Arrays.equals(tryPattern, pattern))
            {
                foundCode = i;
                foundLen = tryPattern.length;
            }
        }
        return foundCode;
    }

    /**
     * Init the code table with 1 byte entries and the EOD and CLEAR_TABLE
     * markers.
     */
    private List<byte[]> createCodeTable()
    {
        List<byte[]> codeTable = new ArrayList<>(4096);
        for (int i = 0; i < 256; ++i)
        {
            codeTable.add(new byte[] { (byte) (i & 0xFF) });
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
