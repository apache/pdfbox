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
import org.apache.pdfbox.io.ScratchFile;
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

    private PDAppearanceHandler caretAppearanceHandler;

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
     * This will set the margin between the annotations "outer" rectangle defined by
     * /Rect and the boundaries of the underlying caret.
     * 
     * @param margin
     */
    public void setMargins(float margin) {
        setMargins(margin, margin, margin, margin);
    }
    
    /**
     * This will set the margin between the annotations "outer" rectangle defined by
     * /Rect and the boundaries of the underlying caret.
     * 
     * @param margin
     */
    public void setMargins(float marginLeft, float marginTop, float marginRight, float marginBottom)
    {
        COSArray margins = new COSArray();
        margins.add(new COSFloat(marginLeft));
        margins.add(new COSFloat(marginTop));
        margins.add(new COSFloat(marginRight));
        margins.add(new COSFloat(marginBottom));
        getCOSObject().setItem(COSName.RD, margins);    
    }
    
    /**
     * This will get the margin between the annotations "outer" rectangle defined by
     * /Rect and the boundaries of the underlying caret.
     * 
     * @return the margins. If the entry hasn't been set am empty array is returned.
     */
    public float[] getMargins()
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
     * @param caretAppearanceHandler
     */
    public void setCustomCaretAppearanceHandler(PDAppearanceHandler caretAppearanceHandler)
    {
        this.caretAppearanceHandler = caretAppearanceHandler;
    }

    @Override
    public void constructAppearances(ScratchFile scratchFile)
    {
        if (caretAppearanceHandler == null)
        {
            PDCaretAppearanceHandler appearanceHandler = new PDCaretAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            caretAppearanceHandler.generateAppearanceStreams();
        }
    }
}
