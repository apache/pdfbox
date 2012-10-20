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


import java.awt.Paint;
import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDShadingPatternResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPatternResources;

/**
 * This represents the resources for a pattern colorspace.
 *
 * @version $Revision: 1.0 $
 */
public abstract class PDPatternResources implements COSObjectable
{
    private COSDictionary patternDictionary;

    public static final int TILING_PATTERN = 1;
    public static final int SHADING_PATTERN = 2;
    
    /**
     * Default constructor.
     */
    public PDPatternResources()
    {
        patternDictionary = new COSDictionary();
        patternDictionary.setName(COSName.TYPE, COSName.PATTERN.getName());
    }

    /**
     * Prepopulated pattern resources.
     *
     * @param resourceDictionary The COSDictionary for this pattern resource.
     */
    public PDPatternResources( COSDictionary resourceDictionary )
    {
        patternDictionary = resourceDictionary;
    }

    /**
     * This will get the underlying dictionary.
     *
     * @return The dictionary for these pattern resources.
     */
    public COSDictionary getCOSDictionary()
    {
        return patternDictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return patternDictionary;
    }

    /**
     * Sets the filter entry of the encryption dictionary.
     *
     * @param filter The filter name.
     */
    public void setFilter(String filter)
    {
        patternDictionary.setItem( COSName.FILTER, COSName.getPDFName( filter ) );
    }

    /**
     * Get the name of the filter.
     *
     * @return The filter name contained in this encryption dictionary.
     */
    public String getFilter()
    {
        return patternDictionary.getNameAsString( COSName.FILTER );
    }

    /**
     * This will set the length of the content stream.
     *
     * @param length The new stream length.
     */
    public void setLength(int length)
    {
        patternDictionary.setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the length of the content stream.
     *
     * @return The length of the content stream
     */
    public int getLength()
    {
        return patternDictionary.getInt( COSName.LENGTH, 0 );
    }

    /**
     * This will set the paint type.
     *
     * @param paintType The new paint type.
     */
    public void setPaintType(int paintType)
    {
        patternDictionary.setInt(COSName.PAINT_TYPE, paintType);
    }

    /**
     * This will return the paint type.
     *
     * @return The type of object that this is.
     */
    public String getType()
    {
        return COSName.PATTERN.getName();
    }

    /**
     * This will set the pattern type.
     *
     * @param patternType The new pattern type.
     */
    public void setPatternType(int patternType)
    {
        patternDictionary.setInt(COSName.PATTERN_TYPE, patternType);
    }

    /**
     * This will return the pattern type.
     *
     * @return The pattern type
     */
    public abstract int getPatternType();
    
    /**
     * Create the correct PD Model pattern based on the COS base pattern.
     * 
     * @param resourceDictionary the COS pattern dictionary
     * 
     * @return the newly created pattern resources object
     * 
     * @throws IOException If we are unable to create the PDPattern object.
     */
    public static PDPatternResources create(COSDictionary resourceDictionary) throws IOException
    {
        PDPatternResources pattern = null;
        int patternType = resourceDictionary.getInt( COSName.PATTERN_TYPE, 0 );
        switch (patternType) 
        {
            case TILING_PATTERN: 
                pattern = new PDTilingPatternResources(resourceDictionary);
                break;
            case SHADING_PATTERN: 
                pattern = new PDShadingPatternResources(resourceDictionary);
                break;
            default:
                throw new IOException( "Error: Unknown pattern type " + patternType );
        }
        return pattern;
    }
    
    /**
     * This will return the paint of the pattern.
     * 
     * @param the height of the current page
     * 
     * @return the paint of the pattern
     */
    public abstract Paint getPaint(int pageHeight) throws IOException;
    
}
