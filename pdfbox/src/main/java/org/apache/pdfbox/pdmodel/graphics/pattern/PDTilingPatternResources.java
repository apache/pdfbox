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
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

/**
 * This represents the resources for a tiling pattern.
 *
 * @version $Revision: 1.0 $
 */
public class PDTilingPatternResources extends PDPatternResources
{
    
    /**
     * Default constructor.
     */
    public PDTilingPatternResources()
    {
        super();
        getCOSDictionary().setInt(COSName.PATTERN_TYPE, PDPatternResources.TILING_PATTERN);
    }

    /**
     * Prepopulated pattern resources.
     *
     * @param resourceDictionary The COSDictionary for this pattern resource.
     */
    public PDTilingPatternResources( COSDictionary resourceDictionary )
    {
        super(resourceDictionary);
    }

    /**
     * {@inheritDoc}
     */
    public int getPatternType()
    {
        return PDPatternResources.TILING_PATTERN;
    }

    /**
     * This will set the length of the content stream.
     *
     * @param length The new stream length.
     */
    public void setLength(int length)
    {
        getCOSDictionary().setInt(COSName.LENGTH, length);
    }

    /**
     * This will return the length of the content stream.
     *
     * @return The length of the content stream
     */
    public int getLength()
    {
        return getCOSDictionary().getInt( COSName.LENGTH, 0 );
    }

    /**
     * This will set the paint type.
     *
     * @param paintType The new paint type.
     */
    public void setPaintType(int paintType)
    {
        getCOSDictionary().setInt(COSName.PAINT_TYPE, paintType);
    }

    /**
     * This will return the paint type.
     *
     * @return The paint type
     */
    public int getPaintType()
    {
        return getCOSDictionary().getInt( COSName.PAINT_TYPE, 0 );
    }

    /**
     * This will set the tiling type.
     *
     * @param tilingType The new tiling type.
     */
    public void setTilingType(int tilingType)
    {
        getCOSDictionary().setInt(COSName.TILING_TYPE, tilingType);
    }

    /**
     * This will return the tiling type.
     *
     * @return The tiling type
     */
    public int getTilingType()
    {
        return getCOSDictionary().getInt( COSName.TILING_TYPE, 0 );
    }

    /**
     * This will set the XStep value.
     *
     * @param xStep The new XStep value.
     */
    public void setXStep(int xStep)
    {
        getCOSDictionary().setInt(COSName.X_STEP, xStep);
    }

    /**
     * This will return the XStep value.
     *
     * @return The XStep value
     */
    public int getXStep()
    {
        return getCOSDictionary().getInt( COSName.X_STEP, 0 );
    }

    /**
     * This will set the YStep value.
     *
     * @param yStep The new YStep value.
     */
    public void setYStep(int yStep)
    {
        getCOSDictionary().setInt(COSName.Y_STEP, yStep);
    }

    /**
     * This will return the YStep value.
     *
     * @return The YStep value
     */
    public int getYStep()
    {
        return getCOSDictionary().getInt( COSName.Y_STEP, 0 );
    }

    /**
     * This will get the resources for this pattern.
     * This will return null if no resources are available at this level.
     *
     * @return The resources for this pattern.
     */
    public PDResources getResources()
    {
        PDResources retval = null;
        COSDictionary resources = (COSDictionary)getCOSDictionary().getDictionaryObject( COSName.RESOURCES );
        if( resources != null )
        {
            retval = new PDResources( resources );
        }
        return retval;
    }

    /**
     * This will set the resources for this pattern.
     *
     * @param resources The new resources for this pattern.
     */
    public void setResources( PDResources resources )
    {
        if (resources != null)
        {
            getCOSDictionary().setItem( COSName.RESOURCES, resources );
        }
        else
        {
            getCOSDictionary().removeItem( COSName.RESOURCES );
        }
    }

    /**
     * An array of four numbers in the form coordinate system (see
     * below), giving the coordinates of the left, bottom, right, and top edges,
     * respectively, of the pattern's bounding box.
     *
     * @return The BBox of the form.
     */
    public PDRectangle getBBox()
    {
        PDRectangle retval = null;
        COSArray array = (COSArray)getCOSDictionary().getDictionaryObject( COSName.BBOX );
        if( array != null )
        {
            retval = new PDRectangle( array );
        }
        return retval;
    }

    /**
     * This will set the BBox (bounding box) for this Pattern.
     *
     * @param bbox The new BBox for this Pattern.
     */
    public void setBBox(PDRectangle bbox)
    {
        if( bbox == null )
        {
            getCOSDictionary().removeItem( COSName.BBOX );
        }
        else
        {
            getCOSDictionary().setItem( COSName.BBOX, bbox.getCOSArray() );
        }
    }

    /**
     * This will get the optional Matrix of a Pattern.
     * It maps the form space into the user space
     * @return the form matrix
     */
    public Matrix getMatrix()
    {
        Matrix retval = null;
        COSArray array = (COSArray)getCOSDictionary().getDictionaryObject( COSName.MATRIX );
        if( array != null )
        {
            retval = new Matrix();
            retval.setValue(0, 0, ((COSNumber) array.get(0)).floatValue());
            retval.setValue(0, 1, ((COSNumber) array.get(1)).floatValue());
            retval.setValue(1, 0, ((COSNumber) array.get(2)).floatValue());
            retval.setValue(1, 1, ((COSNumber) array.get(3)).floatValue());
            retval.setValue(2, 0, ((COSNumber) array.get(4)).floatValue());
            retval.setValue(2, 1, ((COSNumber) array.get(5)).floatValue());
        }
        return retval;
    }

    /**
     * Sets the optional Matrix entry for the Pattern.
     * @param transform the transformation matrix
     */
    public void setMatrix(AffineTransform transform)
    {
        COSArray matrix = new COSArray();
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            matrix.add(new COSFloat((float)v));
        }
        getCOSDictionary().setItem(COSName.MATRIX, matrix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Paint getPaint(int pageHeight) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
