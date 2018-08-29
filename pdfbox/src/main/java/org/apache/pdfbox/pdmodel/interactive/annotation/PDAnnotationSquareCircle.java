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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;

/**
 * This is the class that represents a rectangular or elliptical annotation introduced in PDF 1.3
 * specification .
 *
 * @author Paul King
 */
public abstract class PDAnnotationSquareCircle extends PDAnnotationMarkup
{
    /**
     * Creates a Circle or Square annotation of the specified sub type.
     *
     * @param subType the subtype the annotation represents.
     */
    protected PDAnnotationSquareCircle(String subType)
    {
        setSubtype(subType);
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    protected PDAnnotationSquareCircle(COSDictionary dict)
    {
        super(dict);
    }

    @Override
    public abstract void constructAppearances();

    /**
     * This will set interior color of the drawn area color is in DeviceRGB colorspace.
     *
     * @param ic color in the DeviceRGB color space.
     *
     */
    public void setInteriorColor(PDColor ic)
    {
        getCOSObject().setItem(COSName.IC, ic.toCOSArray());
    }

    /**
     * This will retrieve the interior color of the drawn area color is in DeviceRGB color space.
     *
     * @return object representing the color.
     */
    public PDColor getInteriorColor()
    {
        return getColor(COSName.IC);
    }

    /**
     * This will set the border effect dictionary, specifying effects to be applied when drawing the
     * line. This is supported by PDF 1.5 and higher.
     *
     * @param be The border effect dictionary to set.
     *
     */
    public void setBorderEffect(PDBorderEffectDictionary be)
    {
        getCOSObject().setItem(COSName.BE, be);
    }

    /**
     * This will retrieve the border effect dictionary, specifying effects to be applied used in
     * drawing the line.
     *
     * @return The border effect dictionary
     */
    public PDBorderEffectDictionary getBorderEffect()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.BE);
        if (base instanceof COSDictionary)
        {
            return new PDBorderEffectDictionary((COSDictionary) base);
        }
        return null;
    }

    /**
     * This will set the rectangle difference rectangle. Giving the difference between the
     * annotations rectangle and where the drawing occurs. (To take account of any effects applied
     * through the BE entry for example)
     *
     * @param rd the rectangle difference
     *
     */
    public void setRectDifference(PDRectangle rd)
    {
        getCOSObject().setItem(COSName.RD, rd);
    }

    /**
     * This will get the rectangle difference rectangle. Giving the difference between the
     * annotations rectangle and where the drawing occurs. (To take account of any effects applied
     * through the BE entry for example)
     *
     * @return the rectangle difference
     */
    public PDRectangle getRectDifference()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.RD);
        if (base instanceof COSArray)
        {
            return new PDRectangle((COSArray) base);
        }
        return null;
    }

    /**
     * This will set the difference between the annotations "outer" rectangle defined by /Rect and
     * the border.
     *
     * <p>
     * This will set an equal difference for all sides</p>
     *
     * @param difference from the annotations /Rect entry
     */
    public void setRectDifferences(float difference)
    {
        setRectDifferences(difference, difference, difference, difference);
    }
    
    /**
     * This will set the difference between the annotations "outer" rectangle defined by
     * /Rect and the border.
     * 
     * @param differenceLeft left difference from the annotations /Rect entry
     * @param differenceTop top difference from the annotations /Rect entry
     * @param differenceRight right difference from  the annotations /Rect entry
     * @param differenceBottom bottom difference from the annotations /Rect entry
     * 
     */
    public void setRectDifferences(float differenceLeft, float differenceTop, float differenceRight, float differenceBottom)
    {
        COSArray margins = new COSArray();
        margins.add(new COSFloat(differenceLeft));
        margins.add(new COSFloat(differenceTop));
        margins.add(new COSFloat(differenceRight));
        margins.add(new COSFloat(differenceBottom));
        getCOSObject().setItem(COSName.RD, margins);    
    }
    
    /**
     * This will get the differences between the annotations "outer" rectangle defined by
     * /Rect and the border.
     * 
     * @return the differences. If the entry hasn't been set am empty array is returned.
     */
    public float[] getRectDifferences()
    {
        COSBase margin = getCOSObject().getItem(COSName.RD);
        if (margin instanceof COSArray)
        {
            return ((COSArray) margin).toFloatArray();
        }
        return new float[]{};
    }

}
