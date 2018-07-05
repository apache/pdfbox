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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDFreeTextAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 *
 * @author Paul King
 */
public class PDAnnotationFreeText extends PDAnnotationMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "FreeText";

    private PDAppearanceHandler customAppearanceHandler;

    public PDAnnotationFreeText()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Creates a FreeText annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    public PDAnnotationFreeText(COSDictionary field)
    {
        super(field);
    }

    /**
     * Get the default appearance.
     * 
     * @return a string describing the default appearance.
     */
    public String getDefaultAppearance()
    {
        return getCOSObject().getString(COSName.DA);
    }

    /**
     * Set the default appearance.
     *
     * @param daValue a string describing the default appearance.
     */
    public void setDefaultAppearance(String daValue)
    {
        getCOSObject().setString(COSName.DA, daValue);
    }

    /**
     * Get the default style string.
     *
     * The default style string defines the default style for rich text fields.
     *
     * @return the DS element of the dictionary object
     */
    public String getDefaultStyleString()
    {
        return getCOSObject().getString(COSName.DS);
    }

    /**
     * Set the default style string.
     *
     * Providing null as the value will remove the default style string.
     *
     * @param defaultStyleString a string describing the default style.
     */
    public void setDefaultStyleString(String defaultStyleString)
    {
        getCOSObject().setString(COSName.DS, defaultStyleString);
    }

    /**
     * This will get the 'quadding' or justification of the text to be displayed.
     * <br>
     * 0 - Left (default)<br>
     * 1 - Centered<br>
     * 2 - Right<br>
     * Please see the QUADDING_CONSTANTS in {@link PDVariableText }.
     *
     * @return The justification of the text strings.
     */
    public int getQ()
    {
        return getCOSObject().getInt(COSName.Q, 0);
    }

    /**
     * This will set the quadding/justification of the text. Please see the QUADDING_CONSTANTS
     * in {@link PDVariableText }.
     *
     * @param q The new text justification.
     */
    public void setQ(int q)
    {
        getCOSObject().setInt(COSName.Q, q);
    }

    /**
     * This will set the difference between the annotations "outer" rectangle defined by
     * /Rect and the border.
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

    /**
     * This will set the coordinates of the callout line.
     *
     * @param callout An array of four or six numbers specifying a callout line attached to the free
     * text annotation. Six numbers [ x1 y1 x2 y2 x3 y3 ] represent the starting, knee point, and
     * ending coordinates of the line in default user space, four numbers [ x1 y1 x2 y2 ] represent
     * the starting and ending coordinates of the line.
     */
    public final void setCallout(float[] callout)
    {
        COSArray newCallout = new COSArray();
        newCallout.setFloatArray(callout);
        getCOSObject().setItem(COSName.CL, newCallout);
    }

    /**
     * This will get the coordinates of the callout line.
     *
     * @return An array of four or six numbers specifying a callout line attached to the free text
     * annotation. Six numbers [ x1 y1 x2 y2 x3 y3 ] represent the starting, knee point, and ending
     * coordinates of the line in default user space, four numbers [ x1 y1 x2 y2 ] represent the
     * starting and ending coordinates of the line.
     */
    public float[] getCallout()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.CL);
        if (base instanceof COSArray)
        {
            return ((COSArray) base).toFloatArray();
        }
        return null;
    }

    /**
     * This will set the line ending style.
     *
     * @param style The new style.
     */
    public final void setLineEndingStyle(String style)
    {
        getCOSObject().setName(COSName.LE, style);
    }

    /**
     * This will retrieve the line ending style.
     *
     * @return The line ending style, possible values shown in the LE_ constants section, LE_NONE if
     * missing, never null.
     */
    public String getLineEndingStyle()
    {
        return getCOSObject().getNameAsString(COSName.LE, PDAnnotationLine.LE_NONE);
    }    

    /**
     * This will set the border effect dictionary, specifying effects to be applied when drawing the
     * line. This is supported by PDF 1.6 and higher.
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
            PDFreeTextAppearanceHandler appearanceHandler = new PDFreeTextAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
