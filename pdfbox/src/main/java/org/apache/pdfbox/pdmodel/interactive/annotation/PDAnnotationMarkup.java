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
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

/**
 * This class represents the additonal fields of a Markup type Annotation. See section 12.5.6 of ISO32000-1:2008
 * (starting with page 390) for details on annotation types.
 *
 * @author Paul King
 */
public class PDAnnotationMarkup extends PDAnnotation
{
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

}
