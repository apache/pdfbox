/*
 * Copyright 2018 The Apache Software Foundation.
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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDCaretAppearanceHandler;

/**
 *
 * @author Paul King
 */
public class PDAnnotationCaret extends PDAnnotationMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Caret";

    private PDAppearanceHandler customAppearanceHandler;

    public PDAnnotationCaret()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Creates a Caret annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    public PDAnnotationCaret(COSDictionary field)
    {
        super(field);
    }
    
    /**
     * This will set the difference between the annotations "outer" rectangle defined by
     * /Rect and boundaries of the underlying.
     * 
     * <p>This will set an equal difference for all sides</p>
     * 
     * @param difference from the annotations /Rect entry
     */
    public void setRectDifferences(float difference) {
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
     * This will get the margin between the annotations "outer" rectangle defined by
     * /Rect and the boundaries of the underlying caret.
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
    
    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        if (customAppearanceHandler == null)
        {
            PDCaretAppearanceHandler appearanceHandler = new PDCaretAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
