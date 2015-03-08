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
 * A table in a true type font.
 * 
 * @author Ben Litchfield
 */
public class HorizontalHeaderTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "hhea";
    
    private float version;
    private short ascender;
    private short descender;
    private short lineGap;
    private int advanceWidthMax;
    private short minLeftSideBearing;
    private short minRightSideBearing;
    private short xMaxExtent;
    private short caretSlopeRise;
    private short caretSlopeRun;
    private short reserved1;
    private short reserved2;
    private short reserved3;
    private short reserved4;
    private short reserved5;
    private short metricDataFormat;
    private int numberOfHMetrics;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        version = data.read32Fixed();
        ascender = data.readSignedShort();
        descender = data.readSignedShort();
        lineGap = data.readSignedShort();
        advanceWidthMax = data.readUnsignedShort();
        minLeftSideBearing = data.readSignedShort();
        minRightSideBearing = data.readSignedShort();
        xMaxExtent = data.readSignedShort();
        caretSlopeRise = data.readSignedShort();
        caretSlopeRun = data.readSignedShort();
        reserved1 = data.readSignedShort();
        reserved2 = data.readSignedShort();
        reserved3 = data.readSignedShort();
        reserved4 = data.readSignedShort();
        reserved5 = data.readSignedShort();
        metricDataFormat = data.readSignedShort();
        numberOfHMetrics = data.readUnsignedShort();
        initialized = true;
    }
    
    /**
     * @return Returns the advanceWidthMax.
     */
    public int getAdvanceWidthMax()
    {
        return advanceWidthMax;
    }
    /**
     * @param advanceWidthMaxValue The advanceWidthMax to set.
     */
    public void setAdvanceWidthMax(int advanceWidthMaxValue)
    {
        this.advanceWidthMax = advanceWidthMaxValue;
    }
    /**
     * @return Returns the ascender.
     */
    public short getAscender()
    {
        return ascender;
    }
    /**
     * @param ascenderValue The ascender to set.
     */
    public void setAscender(short ascenderValue)
    {
        this.ascender = ascenderValue;
    }
    /**
     * @return Returns the caretSlopeRise.
     */
    public short getCaretSlopeRise()
    {
        return caretSlopeRise;
    }
    /**
     * @param caretSlopeRiseValue The caretSlopeRise to set.
     */
    public void setCaretSlopeRise(short caretSlopeRiseValue)
    {
        this.caretSlopeRise = caretSlopeRiseValue;
    }
    /**
     * @return Returns the caretSlopeRun.
     */
    public short getCaretSlopeRun()
    {
        return caretSlopeRun;
    }
    /**
     * @param caretSlopeRunValue The caretSlopeRun to set.
     */
    public void setCaretSlopeRun(short caretSlopeRunValue)
    {
        this.caretSlopeRun = caretSlopeRunValue;
    }
    /**
     * @return Returns the descender.
     */
    public short getDescender()
    {
        return descender;
    }
    /**
     * @param descenderValue The descender to set.
     */
    public void setDescender(short descenderValue)
    {
        this.descender = descenderValue;
    }
    /**
     * @return Returns the lineGap.
     */
    public short getLineGap()
    {
        return lineGap;
    }
    /**
     * @param lineGapValue The lineGap to set.
     */
    public void setLineGap(short lineGapValue)
    {
        this.lineGap = lineGapValue;
    }
    /**
     * @return Returns the metricDataFormat.
     */
    public short getMetricDataFormat()
    {
        return metricDataFormat;
    }
    /**
     * @param metricDataFormatValue The metricDataFormat to set.
     */
    public void setMetricDataFormat(short metricDataFormatValue)
    {
        this.metricDataFormat = metricDataFormatValue;
    }
    /**
     * @return Returns the minLeftSideBearing.
     */
    public short getMinLeftSideBearing()
    {
        return minLeftSideBearing;
    }
    /**
     * @param minLeftSideBearingValue The minLeftSideBearing to set.
     */
    public void setMinLeftSideBearing(short minLeftSideBearingValue)
    {
        this.minLeftSideBearing = minLeftSideBearingValue;
    }
    /**
     * @return Returns the minRightSideBearing.
     */
    public short getMinRightSideBearing()
    {
        return minRightSideBearing;
    }
    /**
     * @param minRightSideBearingValue The minRightSideBearing to set.
     */
    public void setMinRightSideBearing(short minRightSideBearingValue)
    {
        this.minRightSideBearing = minRightSideBearingValue;
    }
    /**
     * @return Returns the numberOfHMetrics.
     */
    public int getNumberOfHMetrics()
    {
        return numberOfHMetrics;
    }
    /**
     * @param numberOfHMetricsValue The numberOfHMetrics to set.
     */
    public void setNumberOfHMetrics(int numberOfHMetricsValue)
    {
        this.numberOfHMetrics = numberOfHMetricsValue;
    }
    /**
     * @return Returns the reserved1.
     */
    public short getReserved1()
    {
        return reserved1;
    }
    /**
     * @param reserved1Value The reserved1 to set.
     */
    public void setReserved1(short reserved1Value)
    {
        this.reserved1 = reserved1Value;
    }
    /**
     * @return Returns the reserved2.
     */
    public short getReserved2()
    {
        return reserved2;
    }
    /**
     * @param reserved2Value The reserved2 to set.
     */
    public void setReserved2(short reserved2Value)
    {
        this.reserved2 = reserved2Value;
    }
    /**
     * @return Returns the reserved3.
     */
    public short getReserved3()
    {
        return reserved3;
    }
    /**
     * @param reserved3Value The reserved3 to set.
     */
    public void setReserved3(short reserved3Value)
    {
        this.reserved3 = reserved3Value;
    }
    /**
     * @return Returns the reserved4.
     */
    public short getReserved4()
    {
        return reserved4;
    }
    /**
     * @param reserved4Value The reserved4 to set.
     */
    public void setReserved4(short reserved4Value)
    {
        this.reserved4 = reserved4Value;
    }
    /**
     * @return Returns the reserved5.
     */
    public short getReserved5()
    {
        return reserved5;
    }
    /**
     * @param reserved5Value The reserved5 to set.
     */
    public void setReserved5(short reserved5Value)
    {
        this.reserved5 = reserved5Value;
    }
    /**
     * @return Returns the version.
     */
    public float getVersion()
    {
        return version;
    }
    /**
     * @param versionValue The version to set.
     */
    public void setVersion(float versionValue)
    {
        this.version = versionValue;
    }
    /**
     * @return Returns the xMaxExtent.
     */
    public short getXMaxExtent()
    {
        return xMaxExtent;
    }
    /**
     * @param maxExtentValue The xMaxExtent to set.
     */
    public void setXMaxExtent(short maxExtentValue)
    {
        xMaxExtent = maxExtentValue;
    }
}
