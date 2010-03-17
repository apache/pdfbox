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
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class MaximumProfileTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "maxp";
    
    private float version;
    private int numGlyphs;
    private int maxPoints;
    private int maxContours;
    private int maxCompositePoints;
    private int maxCompositeContours;
    private int maxZones;
    private int maxTwilightPoints;
    private int maxStorage;
    private int maxFunctionDefs;
    private int maxInstructionDefs;
    private int maxStackElements;
    private int maxSizeOfInstructions;
    private int maxComponentElements;
    private int maxComponentDepth;
    
    /**
     * @return Returns the maxComponentDepth.
     */
    public int getMaxComponentDepth()
    {
        return maxComponentDepth;
    }
    /**
     * @param maxComponentDepthValue The maxComponentDepth to set.
     */
    public void setMaxComponentDepth(int maxComponentDepthValue)
    {
        this.maxComponentDepth = maxComponentDepthValue;
    }
    /**
     * @return Returns the maxComponentElements.
     */
    public int getMaxComponentElements()
    {
        return maxComponentElements;
    }
    /**
     * @param maxComponentElementsValue The maxComponentElements to set.
     */
    public void setMaxComponentElements(int maxComponentElementsValue)
    {
        this.maxComponentElements = maxComponentElementsValue;
    }
    /**
     * @return Returns the maxCompositeContours.
     */
    public int getMaxCompositeContours()
    {
        return maxCompositeContours;
    }
    /**
     * @param maxCompositeContoursValue The maxCompositeContours to set.
     */
    public void setMaxCompositeContours(int maxCompositeContoursValue)
    {
        this.maxCompositeContours = maxCompositeContoursValue;
    }
    /**
     * @return Returns the maxCompositePoints.
     */
    public int getMaxCompositePoints()
    {
        return maxCompositePoints;
    }
    /**
     * @param maxCompositePointsValue The maxCompositePoints to set.
     */
    public void setMaxCompositePoints(int maxCompositePointsValue)
    {
        this.maxCompositePoints = maxCompositePointsValue;
    }
    /**
     * @return Returns the maxContours.
     */
    public int getMaxContours()
    {
        return maxContours;
    }
    /**
     * @param maxContoursValue The maxContours to set.
     */
    public void setMaxContours(int maxContoursValue)
    {
        this.maxContours = maxContoursValue;
    }
    /**
     * @return Returns the maxFunctionDefs.
     */
    public int getMaxFunctionDefs()
    {
        return maxFunctionDefs;
    }
    /**
     * @param maxFunctionDefsValue The maxFunctionDefs to set.
     */
    public void setMaxFunctionDefs(int maxFunctionDefsValue)
    {
        this.maxFunctionDefs = maxFunctionDefsValue;
    }
    /**
     * @return Returns the maxInstructionDefs.
     */
    public int getMaxInstructionDefs()
    {
        return maxInstructionDefs;
    }
    /**
     * @param maxInstructionDefsValue The maxInstructionDefs to set.
     */
    public void setMaxInstructionDefs(int maxInstructionDefsValue)
    {
        this.maxInstructionDefs = maxInstructionDefsValue;
    }
    /**
     * @return Returns the maxPoints.
     */
    public int getMaxPoints()
    {
        return maxPoints;
    }
    /**
     * @param maxPointsValue The maxPoints to set.
     */
    public void setMaxPoints(int maxPointsValue)
    {
        this.maxPoints = maxPointsValue;
    }
    /**
     * @return Returns the maxSizeOfInstructions.
     */
    public int getMaxSizeOfInstructions()
    {
        return maxSizeOfInstructions;
    }
    /**
     * @param maxSizeOfInstructionsValue The maxSizeOfInstructions to set.
     */
    public void setMaxSizeOfInstructions(int maxSizeOfInstructionsValue)
    {
        this.maxSizeOfInstructions = maxSizeOfInstructionsValue;
    }
    /**
     * @return Returns the maxStackElements.
     */
    public int getMaxStackElements()
    {
        return maxStackElements;
    }
    /**
     * @param maxStackElementsValue The maxStackElements to set.
     */
    public void setMaxStackElements(int maxStackElementsValue)
    {
        this.maxStackElements = maxStackElementsValue;
    }
    /**
     * @return Returns the maxStorage.
     */
    public int getMaxStorage()
    {
        return maxStorage;
    }
    /**
     * @param maxStorageValue The maxStorage to set.
     */
    public void setMaxStorage(int maxStorageValue)
    {
        this.maxStorage = maxStorageValue;
    }
    /**
     * @return Returns the maxTwilightPoints.
     */
    public int getMaxTwilightPoints()
    {
        return maxTwilightPoints;
    }
    /**
     * @param maxTwilightPointsValue The maxTwilightPoints to set.
     */
    public void setMaxTwilightPoints(int maxTwilightPointsValue)
    {
        this.maxTwilightPoints = maxTwilightPointsValue;
    }
    /**
     * @return Returns the maxZones.
     */
    public int getMaxZones()
    {
        return maxZones;
    }
    /**
     * @param maxZonesValue The maxZones to set.
     */
    public void setMaxZones(int maxZonesValue)
    {
        this.maxZones = maxZonesValue;
    }
    /**
     * @return Returns the numGlyphs.
     */
    public int getNumGlyphs()
    {
        return numGlyphs;
    }
    /**
     * @param numGlyphsValue The numGlyphs to set.
     */
    public void setNumGlyphs(int numGlyphsValue)
    {
        this.numGlyphs = numGlyphsValue;
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
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        version = data.read32Fixed();
        numGlyphs = data.readUnsignedShort();
        maxPoints = data.readUnsignedShort();
        maxContours = data.readUnsignedShort();
        maxCompositePoints = data.readUnsignedShort();
        maxCompositeContours = data.readUnsignedShort();
        maxZones = data.readUnsignedShort();
        maxTwilightPoints = data.readUnsignedShort();
        maxStorage = data.readUnsignedShort();
        maxFunctionDefs = data.readUnsignedShort();
        maxInstructionDefs = data.readUnsignedShort();
        maxStackElements = data.readUnsignedShort();
        maxSizeOfInstructions = data.readUnsignedShort();
        maxComponentElements = data.readUnsignedShort();
        maxComponentDepth = data.readUnsignedShort();
    }
}
