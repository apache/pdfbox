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

package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Class to collect all sort of border info about annotations.
 * 
 * @author Tilman Hausherr
 */
class AnnotationBorder
{
    float[] dashArray = null;
    boolean underline = false;
    float width = 0;

    // return border info. BorderStyle must be provided as parameter because
    // method is not available in the base class
    static AnnotationBorder getAnnotationBorder(PDAnnotation annotation,
            PDBorderStyleDictionary borderStyle)
    {
        AnnotationBorder ab = new AnnotationBorder();
        if (borderStyle == null)
        {
            COSArray border = annotation.getBorder();
            if (border.size() >= 3 && border.getObject(2) instanceof COSNumber)
            {
                ab.width = ((COSNumber) border.getObject(2)).floatValue();
            }
            if (border.size() > 3)
            {
                COSBase base3 = border.getObject(3);
                if (base3 instanceof COSArray)
                {
                    ab.dashArray = ((COSArray) base3).toFloatArray();
                }
            }
        }
        else
        {
            ab.width = borderStyle.getWidth();
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_DASHED))
            {
                ab.dashArray = borderStyle.getDashStyle().getDashArray();
            }
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_UNDERLINE))
            {
                ab.underline = true;
            }
        }
        if (ab.dashArray != null)
        {
            boolean allZero = true;
            for (float f : ab.dashArray)
            {
                if (f != 0)
                {
                    allZero = false;
                    break;
                }
            }
            if (allZero)
            {
                ab.dashArray = null;
            }
        }
        return ab;
    }
}
