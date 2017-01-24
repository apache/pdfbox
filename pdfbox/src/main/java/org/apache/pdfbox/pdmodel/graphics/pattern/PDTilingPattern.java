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
import java.io.InputStream;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * A tiling pattern dictionary.
 *
 */
public class PDTilingPattern extends PDAbstractPattern implements PDContentStream
{
    /** paint type 1 = colored tiling pattern. */
    public static final int PAINT_COLORED = 1;

    /** paint type 2 = uncolored tiling pattern. */
    public static final int PAINT_UNCOLORED = 2;

    /** tiling type 1 = constant spacing.*/
    public static final int TILING_CONSTANT_SPACING = 1;

    /** tiling type 2 = no distortion. */
    public static final int TILING_NO_DISTORTION = 2;

    /** tiling type 3 = constant spacing and faster tiling. */
    public static final int TILING_CONSTANT_SPACING_FASTER_TILING = 3;

    /**
     * Creates a new tiling pattern.
     */
    public PDTilingPattern()
    {
        super(new COSStream());
        getCOSObject().setName(COSName.TYPE, COSName.PATTERN.getName());
        getCOSObject().setInt(COSName.PATTERN_TYPE, PDAbstractPattern.TYPE_TILING_PATTERN);
        
        // Resources required per PDF specification; when missing, pattern is not displayed in Adobe Reader
        setResources(new PDResources());
    }

    /**
     * Creates a new tiling pattern from the given COS dictionary.
     * @param dictionary The COSDictionary for this pattern.
     */
    public PDTilingPattern(COSDictionary dictionary)
    {
        super(dictionary);
    }

    @Override
    public int getPatternType()
    {
        return PDAbstractPattern.TYPE_TILING_PATTERN;
    }

    /**
     * This will set the paint type.
     * @param paintType The new paint type.
     */
    @Override
    public void setPaintType(int paintType)
    {
        getCOSObject().setInt(COSName.PAINT_TYPE, paintType);
    }

    /**
     * This will return the paint type.
     * @return The paint type
     */
    public int getPaintType()
    {
        return getCOSObject().getInt( COSName.PAINT_TYPE, 0 );
    }

    /**
     * This will set the tiling type.
     * @param tilingType The new tiling type.
     */
    public void setTilingType(int tilingType)
    {
        getCOSObject().setInt(COSName.TILING_TYPE, tilingType);
    }

    /**
     * This will return the tiling type.
     * @return The tiling type
     */
    public int getTilingType()
    {
        return getCOSObject().getInt( COSName.TILING_TYPE, 0 );
    }

    /**
     * This will set the XStep value.
     * @param xStep The new XStep value.
     */
    public void setXStep(float xStep)
    {
        getCOSObject().setFloat(COSName.X_STEP, xStep);
    }

    /**
     * This will return the XStep value.
     * @return The XStep value
     */
    public float getXStep()
    {
        return getCOSObject().getFloat(COSName.X_STEP, 0);
    }

    /**
     * This will set the YStep value.
     * @param yStep The new YStep value.
     */
    public void setYStep(float yStep)
    {
        getCOSObject().setFloat(COSName.Y_STEP, yStep);
    }

    /**
     * This will return the YStep value.
     * @return The YStep value
     */
    public float getYStep()
    {
        return getCOSObject().getFloat(COSName.Y_STEP, 0);
    }
    
    public PDStream getContentStream()
    {
        return new PDStream((COSStream)getCOSObject());
    }

    @Override
    public InputStream getContents() throws IOException
    {
        COSDictionary dict = getCOSObject();
        if (dict instanceof COSStream)
        {
            return ((COSStream) getCOSObject()).createInputStream();
        }
        return null;
    }

    /**
     * This will get the resources for this pattern.
     * This will return null if no resources are available at this level.
     * @return The resources for this pattern.
     */
    @Override
    public PDResources getResources()
    {
        PDResources retval = null;
        COSBase base = getCOSObject().getDictionaryObject(COSName.RESOURCES);
        if (base instanceof COSDictionary)
        {
            retval = new PDResources((COSDictionary) base);
        }
        return retval;
    }

    /**
     * This will set the resources for this pattern.
     * @param resources The new resources for this pattern.
     */
    public final void setResources( PDResources resources )
    {
        getCOSObject().setItem(COSName.RESOURCES, resources);
    }

    /**
     * An array of four numbers in the form coordinate system (see
     * below), giving the coordinates of the left, bottom, right, and top edges,
     * respectively, of the pattern's bounding box.
     *
     * @return The BBox of the pattern.
     */
    @Override
    public PDRectangle getBBox()
    {
        PDRectangle retval = null;
        COSBase base = getCOSObject().getDictionaryObject(COSName.BBOX);
        if (base instanceof COSArray)
        {
            retval = new PDRectangle((COSArray) base);
        }
        return retval;
    }

    /**
     * This will set the BBox (bounding box) for this Pattern.
     * @param bbox The new BBox for this Pattern.
     */
    public void setBBox(PDRectangle bbox)
    {
        if( bbox == null )
        {
            getCOSObject().removeItem( COSName.BBOX );
        }
        else
        {
            getCOSObject().setItem( COSName.BBOX, bbox.getCOSArray() );
        }
    }
}
