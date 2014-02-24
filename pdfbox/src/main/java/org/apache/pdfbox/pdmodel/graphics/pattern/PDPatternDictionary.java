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
package org.apache.pdfbox.pdmodel.graphics.pattern;

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A Pattern dictionary from a page's resources.
 * @author Andreas Lehmkühler
 */
public abstract class PDPatternDictionary implements COSObjectable
{
    /** Tiling pattern type. */
    public static final int TYPE_TILING_PATTERN = 1;

    /** Shading pattern type. */
    public static final int TYPE_SHADING_PATTERN = 2;

    /**
     * Create the correct PD Model pattern based on the COS base pattern.
     * @param resourceDictionary the COS pattern dictionary
     * @return the newly created pattern resources object
     * @throws IOException If we are unable to create the PDPattern object.
     */
    public static PDPatternDictionary create(COSDictionary resourceDictionary) throws IOException
    {
        PDPatternDictionary pattern;
        int patternType = resourceDictionary.getInt(COSName.PATTERN_TYPE, 0);
        switch (patternType)
        {
            case TYPE_TILING_PATTERN:
                pattern = new PDTilingPattern(resourceDictionary);
                break;
            case TYPE_SHADING_PATTERN:
                pattern = new PDShadingPattern(resourceDictionary);
                break;
            default:
                throw new IOException("Error: Unknown pattern type " + patternType);
        }
        return pattern;
    }

    private COSDictionary patternDictionary;

    /**
     * Creates a new Pattern dictionary.
     */
    public PDPatternDictionary()
    {
        patternDictionary = new COSDictionary();
        patternDictionary.setName(COSName.TYPE, COSName.PATTERN.getName());
    }

    /**
     * Creates a new Pattern dictionary from the given COS dictionary.
     * @param resourceDictionary The COSDictionary for this pattern resource.
     */
    public PDPatternDictionary(COSDictionary resourceDictionary)
    {
        patternDictionary = resourceDictionary;
    }

    /**
     * This will get the underlying dictionary.
     * @return The dictionary for these pattern resources.
     */
    public COSDictionary getCOSDictionary()
    {
        return patternDictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return patternDictionary;
    }

    /**
     * Sets the filter entry of the encryption dictionary.
     * @param filter The filter name.
     */
    public void setFilter(String filter)
    {
        patternDictionary.setItem(COSName.FILTER, COSName.getPDFName(filter));
    }

    /**
     * Get the name of the filter.
     * @return The filter name contained in this encryption dictionary.
     */
    public String getFilter()
    {
        return patternDictionary.getNameAsString(COSName.FILTER);
    }

    /**
     * This will set the length of the content stream.
     * @param length The new stream length.
     */
    public void setLength(int length)
    {
        patternDictionary.setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the length of the content stream.
     * @return The length of the content stream
     */
    public int getLength()
    {
        return patternDictionary.getInt(COSName.LENGTH, 0);
    }

    /**
     * This will set the paint type.
     * @param paintType The new paint type.
     */
    public void setPaintType(int paintType)
    {
        patternDictionary.setInt(COSName.PAINT_TYPE, paintType);
    }

    /**
     * This will return the paint type.
     * @return The type of object that this is.
     */
    public String getType()
    {
        return COSName.PATTERN.getName();
    }

    /**
     * This will set the pattern type.
     * @param patternType The new pattern type.
     */
    public void setPatternType(int patternType)
    {
        patternDictionary.setInt(COSName.PATTERN_TYPE, patternType);
    }

    /**
     * This will return the pattern type.
     * @return The pattern type
     */
    public abstract int getPatternType();
}
