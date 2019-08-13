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

import java.io.IOException;
import java.util.Calendar;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDCaretAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDFreeTextAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDInkAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDPolygonAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDPolylineAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDSoundAppearanceHandler;

// needed for the javadoc generation
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * This class represents the additional fields of a Markup type Annotation. See section 12.5.6 of ISO32000-1:2008
 * (starting with page 390) for details on annotation types.
 *
 * @author Paul King
 */
public class PDAnnotationMarkup extends PDAnnotation
{

    private PDAppearanceHandler customAppearanceHandler;
    
    /**
     * Constant for a FreeText type of annotation.
     */
    public static final String SUB_TYPE_FREETEXT = "FreeText";
    /**
     * Constant for an Polygon type of annotation.
     */
    public static final String SUB_TYPE_POLYGON = "Polygon";
    /**
     * Constant for an PolyLine type of annotation.
     */
    public static final String SUB_TYPE_POLYLINE = "PolyLine";
    /**
     * Constant for an Caret type of annotation.
     */
    public static final String SUB_TYPE_CARET = "Caret";
    /**
     * Constant for an Ink type of annotation.
     */
    public static final String SUB_TYPE_INK = "Ink";
    /**
     * Constant for an Sound type of annotation.
     */
    public static final String SUB_TYPE_SOUND = "Sound";

    /*
     * The various values of the free text annotation as defined in the PDF 1.7 reference Table 170
     */
    
    /**
     * A plain free-text annotation, also known as a text box comment.
     */
    public static final String IT_FREE_TEXT = "FreeText";

    /**
     * A callout, associated with an area on the page through the callout line specified.
     */
    public static final String IT_FREE_TEXT_CALLOUT = "FreeTextCallout";

    /**
     * The annotation is intended to function as a click-to-type or typewriter object.
     */
    public static final String IT_FREE_TEXT_TYPE_WRITER = "FreeTextTypeWriter";

    /*
     * The various values of the reply type as defined in the PDF 1.7 reference Table 170
     */

    /**
     * Constant for an annotation reply type.
     */
    public static final String RT_REPLY = "R";

    /**
     * Constant for an annotation reply type.
     */
    public static final String RT_GROUP = "Group";

    /**
     * Constructor.
     */
    public PDAnnotationMarkup()
    {
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAnnotationMarkup(COSDictionary dict)
    {
        super(dict);
    }

    /**
     * Retrieve the string used as the title of the popup window shown when open and active (by convention this
     * identifies who added the annotation).
     *
     * @return The title of the popup.
     */
    public String getTitlePopup()
    {
        return getCOSObject().getString(COSName.T);
    }

    /**
     * Set the string used as the title of the popup window shown when open and active (by convention this identifies
     * who added the annotation).
     *
     * @param t The title of the popup.
     */
    public void setTitlePopup(String t)
    {
        getCOSObject().setString(COSName.T, t);
    }

    /**
     * This will retrieve the popup annotation used for entering/editing the text for this annotation.
     *
     * @return the popup annotation.
     */
    public PDAnnotationPopup getPopup()
    {
        COSDictionary popup = (COSDictionary) getCOSObject().getDictionaryObject("Popup");
        if (popup != null)
        {
            return new PDAnnotationPopup(popup);
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the popup annotation used for entering/editing the text for this annotation.
     *
     * @param popup the popup annotation.
     */
    public void setPopup(PDAnnotationPopup popup)
    {
        getCOSObject().setItem("Popup", popup);
    }

    /**
     * This will retrieve the constant opacity value used when rendering the annotation (excluing any popup).
     *
     * @return the constant opacity value.
     */
    public float getConstantOpacity()
    {
        return getCOSObject().getFloat(COSName.CA, 1);
    }

    /**
     * This will set the constant opacity value used when rendering the annotation (excluing any popup).
     *
     * @param ca the constant opacity value.
     */
    public void setConstantOpacity(float ca)
    {
        getCOSObject().setFloat(COSName.CA, ca);
    }

    /**
     * This will retrieve the rich text stream which is displayed in the popup window.
     *
     * @return the rich text stream.
     */
    public String getRichContents()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.RC);
        if (base instanceof COSString)
        {
            return ((COSString) base).getString();
        }
        else if (base instanceof COSStream)
        {
            return ((COSStream) base).toTextString();
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the rich text stream which is displayed in the popup window.
     *
     * @param rc the rich text stream.
     */
    public void setRichContents(String rc)
    {
        getCOSObject().setItem(COSName.RC, new COSString(rc));
    }

    /**
     * This will retrieve the date and time the annotation was created.
     *
     * @return the creation date/time.
     * @throws IOException if there is a format problem when converting the date.
     */
    public Calendar getCreationDate() throws IOException
    {
        return getCOSObject().getDate(COSName.CREATION_DATE);
    }

    /**
     * This will set the date and time the annotation was created.
     *
     * @param creationDate the date and time the annotation was created.
     */
    public void setCreationDate(Calendar creationDate)
    {
        getCOSObject().setDate(COSName.CREATION_DATE, creationDate);
    }

    /**
     * This will retrieve the annotation to which this one is "In Reply To" the actual relationship
     * is specified by the RT entry.
     *
     * @return the other annotation or null if there is none.
     * @throws IOException if there is an error creating the other annotation.
     */
    public PDAnnotation getInReplyTo() throws IOException
    {
        COSBase base = getCOSObject().getDictionaryObject("IRT");
        if (base instanceof COSDictionary)
        {
            return PDAnnotation.createAnnotation(base);
        }
        return null;
    }

    /**
     * This will set the annotation to which this one is "In Reply To" the actual relationship is specified by the RT
     * entry.
     *
     * @param irt the annotation this one is "In Reply To".
     */
    public void setInReplyTo(PDAnnotation irt)
    {
        getCOSObject().setItem("IRT", irt);
    }

    /**
     * This will retrieve the short description of the subject of the annotation.
     *
     * @return the subject.
     */
    public String getSubject()
    {
        return getCOSObject().getString(COSName.SUBJ);
    }

    /**
     * This will set the short description of the subject of the annotation.
     *
     * @param subj short description of the subject.
     */
    public void setSubject(String subj)
    {
        getCOSObject().setString(COSName.SUBJ, subj);
    }

    /**
     * This will retrieve the Reply Type (relationship) with the annotation in the IRT entry See the RT_* constants for
     * the available values.
     *
     * @return the relationship.
     */
    public String getReplyType()
    {
        return getCOSObject().getNameAsString("RT", RT_REPLY);
    }

    /**
     * This will set the Reply Type (relationship) with the annotation in the IRT entry See the RT_* constants for the
     * available values.
     *
     * @param rt the reply type.
     */
    public void setReplyType(String rt)
    {
        getCOSObject().setName("RT", rt);
    }

    /**
     * This will retrieve the intent of the annotation The values and meanings are specific to the actual annotation See
     * the IT_* constants for the annotation classes.
     *
     * @return the intent
     */
    public String getIntent()
    {
        return getCOSObject().getNameAsString(COSName.IT);
    }

    /**
     * This will set the intent of the annotation The values and meanings are specific to the actual annotation See the
     * IT_* constants for the annotation classes.
     *
     * @param it the intent
     */
    public void setIntent(String it)
    {
        getCOSObject().setName(COSName.IT, it);
    }

    /**
     * This will return the external data dictionary.
     * 
     * @return the external data dictionary
     */
    public PDExternalDataDictionary getExternalData()
    {
        COSBase exData = this.getCOSObject().getDictionaryObject("ExData");
        if (exData instanceof COSDictionary)
        {
            return new PDExternalDataDictionary((COSDictionary) exData);
        }
        return null;
    }

    /**
     * This will set the external data dictionary.
     * 
     * @param externalData the external data dictionary
     */
    public void setExternalData(PDExternalDataDictionary externalData)
    {
        this.getCOSObject().setItem("ExData", externalData);
    }

    /**
     * This will set the border style dictionary, specifying the width and dash pattern used in drawing the line.
     *
     * @param bs the border style dictionary to set.
     *
     */
    public void setBorderStyle(PDBorderStyleDictionary bs)
    {
        this.getCOSObject().setItem(COSName.BS, bs);
    }

    /**
     * This will retrieve the border style dictionary, specifying the width and dash pattern used in drawing the line.
     *
     * @return the border style dictionary.
     */
    public PDBorderStyleDictionary getBorderStyle()
    {
        COSBase bs = getCOSObject().getDictionaryObject(COSName.BS);
        if (bs instanceof COSDictionary)
        {
            return new PDBorderStyleDictionary((COSDictionary) bs);
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

    // PDF 32000 specification has "the interior color with which to fill the annotation’s line endings"
    // but it is the inside of the polygon.
    
    /**
     * This will set interior color.
     *
     * @param ic color.
     */
    public void setInteriorColor(PDColor ic)
    {
        getCOSObject().setItem(COSName.IC, ic.toCOSArray());
    }

    /**
     * This will retrieve the interior color.
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
        COSDictionary be = (COSDictionary) getCOSObject().getDictionaryObject(COSName.BE);
        if (be != null)
        {
            return new PDBorderEffectDictionary(be);
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the paths that make this annotation.
     *
     * @param inkList An array of arrays, each representing a stroked path. Each array shall be a
     * series of alternating horizontal and vertical coordinates. If the parameter is null the entry
     * will be removed.
     */
    public void setInkList(float[][] inkList)
    {
        if (inkList == null)
        {
            getCOSObject().removeItem(COSName.INKLIST);
            return;
        }
        COSArray array = new COSArray();
        for (float[] path : inkList)
        {
            COSArray innerArray = new COSArray();
            innerArray.setFloatArray(path);
            array.add(innerArray);
        }
        getCOSObject().setItem(COSName.INKLIST, array);
    }

    /**
     * Get one or more disjoint paths that make this annotation.
     *
     * @return An array of arrays, each representing a stroked path. Each array shall be a series of
     * alternating horizontal and vertical coordinates.
     */
    public float[][] getInkList()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.INKLIST);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            float[][] inkList = new float[array.size()][];
            for (int i = 0; i < array.size(); ++i)
            {
                COSBase base2 = array.getObject(i);
                if (base2 instanceof COSArray)
                {
                    inkList[i] = ((COSArray) array.getObject(i)).toFloatArray();
                }
                else
                {
                    inkList[i] = new float[0];
                }
            }
            return inkList;
        }
        return new float[0][0];
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
     * This will set the coordinates of the callout line. (PDF 1.6 and higher) Only relevant if the
     * intent is FreeTextCallout.
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
     * This will get the coordinates of the callout line. (PDF 1.6 and higher) Only relevant if the
     * intent is FreeTextCallout.
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
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setStartPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() == 0)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(actualStyle));
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(0, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the start point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the start point, LE_NONE if missing, never null.
     */
    public String getStartPointEndingStyle()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(0, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set the line ending style for the end point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setEndPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() < 2)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            array.add(COSName.getPDFName(actualStyle));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(1, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the end point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the end point, LE_NONE if missing, never null.
     */
    public String getEndPointEndingStyle()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(1, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }


    /**
     * This will retrieve the numbers that shall represent the alternating horizontal and vertical
     * coordinates.
     *
     * @return An array of floats representing the alternating horizontal and vertical coordinates.
     */
    public float[] getVertices()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.VERTICES);
        if (base instanceof COSArray)
        {
            return ((COSArray) base).toFloatArray();
        }
        return null;
    }

    /**
     * This will set the numbers that shall represent the alternating horizontal and vertical
     * coordinates.
     *
     * @param points an array with the numbers that shall represent the alternating horizontal and
     * vertical coordinates.
     */
    public void setVertices(float[] points)
    {
        COSArray ar = new COSArray();
        ar.setFloatArray(points);
        getCOSObject().setItem(COSName.VERTICES, ar);
    }


    /**
     * PDF 2.0: This will retrieve the arrays that shall represent the alternating horizontal
     * and vertical coordinates for path building.
     *
     * @return An array of float arrays, each supplying the operands for a path building operator
     * (m, l or c). The first array should have 2 elements, the others should have 2 or 6 elements.
     */
    public float[][] getPath()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.PATH);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            float[][] pathArray = new float[array.size()][];
            for (int i = 0; i < array.size(); ++i)
            {
                COSBase base2 = array.getObject(i);
                if (base2 instanceof COSArray)
                {
                    pathArray[i] = ((COSArray) array.getObject(i)).toFloatArray();
                }
                else
                {
                    pathArray[i] = new float[0];
                }
            }
            return pathArray;
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
        this.constructAppearances(null);
    }

    @Override
    public void constructAppearances(PDDocument document)
    {
        if (customAppearanceHandler == null)
        {
            PDAppearanceHandler appearanceHandler = null;
            if (SUB_TYPE_CARET.equals(getSubtype()))
            {
                appearanceHandler = new PDCaretAppearanceHandler(this);
            }
            else if (SUB_TYPE_FREETEXT.equals(getSubtype()))
            {
                appearanceHandler = new PDFreeTextAppearanceHandler(this);
            }
            else if (SUB_TYPE_INK.equals(getSubtype()))
            {
                appearanceHandler = new PDInkAppearanceHandler(this);
            }
            else if (SUB_TYPE_POLYGON.equals(getSubtype()))
            {
                appearanceHandler = new PDPolygonAppearanceHandler(this);
            }
            else if (SUB_TYPE_POLYLINE.equals(getSubtype()))
            {
                appearanceHandler = new PDPolylineAppearanceHandler(this);
            }
            else if (SUB_TYPE_SOUND.equals(getSubtype()))
            {
                appearanceHandler = new PDSoundAppearanceHandler(this);
            }

            if (appearanceHandler != null)
            {
                appearanceHandler.generateAppearanceStreams();
            }
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }


}
