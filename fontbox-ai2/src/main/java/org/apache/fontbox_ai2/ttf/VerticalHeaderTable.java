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
package org.apache.fontbox_ai2.ttf;

import java.io.IOException;

/**
 * A vertical header 'vhea' table in a TrueType or OpenType font.
 *
 * Supports versions 1.0 and 1.1, for which the only difference is changing
 * the specification names and descriptions of the ascender, descender,
 * and lineGap fields to vertTypoAscender, vertTypoDescender, vertTypeLineGap.
 *
 * This table is required by the OpenType CJK Font Guidelines for "all
 * OpenType fonts that are used for vertical writing".
 * 
 * This table is specified in both the TrueType and OpenType specifications.
 * 
 * @author Glenn Adams
 * 
 */
public class VerticalHeaderTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "vhea";
    
    private float version;
    private short ascender;
    private short descender;
    private short lineGap;
    private int advanceHeightMax;
    private short minTopSideBearing;
    private short minBottomSideBearing;
    private short yMaxExtent;
    private short caretSlopeRise;
    private short caretSlopeRun;
    private short caretOffset;
    private short reserved1;
    private short reserved2;
    private short reserved3;
    private short reserved4;
    private short metricDataFormat;
    private int numberOfVMetrics;

    VerticalHeaderTable(TrueTypeFont font)
    {
        super(font);
    }

    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        version = data.read32Fixed();
        ascender = data.readSignedShort();
        descender = data.readSignedShort();
        lineGap = data.readSignedShort();
        advanceHeightMax = data.readUnsignedShort();
        minTopSideBearing = data.readSignedShort();
        minBottomSideBearing = data.readSignedShort();
        yMaxExtent = data.readSignedShort();
        caretSlopeRise = data.readSignedShort();
        caretSlopeRun = data.readSignedShort();
        caretOffset = data.readSignedShort();
        reserved1 = data.readSignedShort();
        reserved2 = data.readSignedShort();
        reserved3 = data.readSignedShort();
        reserved4 = data.readSignedShort();
        metricDataFormat = data.readSignedShort();
        numberOfVMetrics = data.readUnsignedShort();
        initialized = true;
    }
    
    /**
     * @return Returns the advanceHeightMax.
     */
    public int getAdvanceHeightMax()
    {
        return advanceHeightMax;
    }
    /**
     * @return Returns the ascender.
     */
    public short getAscender()
    {
        return ascender;
    }
    /**
     * @return Returns the caretSlopeRise.
     */
    public short getCaretSlopeRise()
    {
        return caretSlopeRise;
    }
    /**
     * @return Returns the caretSlopeRun.
     */
    public short getCaretSlopeRun()
    {
        return caretSlopeRun;
    }
    /**
     * @return Returns the caretOffset.
     */
    public short getCaretOffset()
    {
        return caretOffset;
    }
    /**
     * @return Returns the descender.
     */
    public short getDescender()
    {
        return descender;
    }
    /**
     * @return Returns the lineGap.
     */
    public short getLineGap()
    {
        return lineGap;
    }
    /**
     * @return Returns the metricDataFormat.
     */
    public short getMetricDataFormat()
    {
        return metricDataFormat;
    }
    /**
     * @return Returns the minTopSideBearing.
     */
    public short getMinTopSideBearing()
    {
        return minTopSideBearing;
    }
    /**
     * @return Returns the minBottomSideBearing.
     */
    public short getMinBottomSideBearing()
    {
        return minBottomSideBearing;
    }
    /**
     * @return Returns the numberOfVMetrics.
     */
    public int getNumberOfVMetrics()
    {
        return numberOfVMetrics;
    }
    /**
     * @return Returns the reserved1.
     */
    public short getReserved1()
    {
        return reserved1;
    }
    /**
     * @return Returns the reserved2.
     */
    public short getReserved2()
    {
        return reserved2;
    }
    /**
     * @return Returns the reserved3.
     */
    public short getReserved3()
    {
        return reserved3;
    }
    /**
     * @return Returns the reserved4.
     */
    public short getReserved4()
    {
        return reserved4;
    }
    /**
     * @return Returns the version.
     */
    public float getVersion()
    {
        return version;
    }
    /**
     * @return Returns the yMaxExtent.
     */
    public short getYMaxExtent()
    {
        return yMaxExtent;
    }
}
