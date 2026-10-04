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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * The 'gasp' (Grid-fitting And Scan-conversion Procedure) table. It maps ppem ranges to flags that
 * advise whether grid-fitting (hinting) and/or grayscale anti-aliasing should be applied at that size.
 * The ranges are sorted by ascending maximum ppem; the last range always ends at 0xFFFF.
 *
 * @author Apache PDFBox
 */
public class GaspTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "gasp";

    /**
     * Use grid-fitting (i.e. execute the hinting bytecode) at this size.
     */
    public static final int GASP_GRIDFIT = 0x0001;

    /**
     * Use grayscale (anti-aliased) rendering at this size.
     */
    public static final int GASP_DOGRAY = 0x0002;

    /**
     * Use grid-fitting with ClearType symmetric smoothing (gasp version 1).
     */
    public static final int GASP_SYMMETRIC_GRIDFIT = 0x0004;

    /**
     * Use smoothing along multiple axes with ClearType (gasp version 1).
     */
    public static final int GASP_SYMMETRIC_SMOOTHING = 0x0008;

    private int version;
    private int[] rangeMaxPPEM;
    private int[] rangeFlags;

    GaspTable()
    {
    }

    /**
     * This will read the required data from the stream.
     *
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        version = data.readUnsignedShort();
        int numRanges = data.readUnsignedShort();
        rangeMaxPPEM = new int[numRanges];
        rangeFlags = new int[numRanges];
        for (int i = 0; i < numRanges; i++)
        {
            rangeMaxPPEM[i] = data.readUnsignedShort();
            rangeFlags[i] = data.readUnsignedShort();
        }
        initialized = true;
    }

    /**
     * @return the table version (0 or 1)
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Returns the upper ppem bound of each range, in ascending order. The last entry is 0xFFFF.
     *
     * @return the per-range maximum ppem values
     */
    public int[] getRangeMaxPPEM()
    {
        return rangeMaxPPEM;
    }

    /**
     * Returns the flags for each range, parallel to {@link #getRangeMaxPPEM()}.
     *
     * @return the per-range flags
     */
    public int[] getRangeFlags()
    {
        return rangeFlags;
    }

    /**
     * Returns the behavior flags that apply at the given ppem - those of the first range whose
     * maximum ppem is greater than or equal to the requested ppem.
     *
     * @param ppem the pixels-per-em to look up
     * @return the flags for that ppem, or 0 if the table has no ranges
     */
    public int getFlags(int ppem)
    {
        if (rangeMaxPPEM == null)
        {
            return 0;
        }
        for (int i = 0; i < rangeMaxPPEM.length; i++)
        {
            if (ppem <= rangeMaxPPEM[i])
            {
                return rangeFlags[i];
            }
        }
        // beyond the last range (should not happen as the last bound is 0xFFFF)
        return rangeFlags.length > 0 ? rangeFlags[rangeFlags.length - 1] : 0;
    }

    /**
     * Convenience test for whether grid-fitting (hinting) is advised at the given ppem.
     *
     * @param ppem the pixels-per-em to look up
     * @return true if {@link #GASP_GRIDFIT} is set for that ppem
     */
    public boolean isGridFit(int ppem)
    {
        return (getFlags(ppem) & GASP_GRIDFIT) != 0;
    }
}