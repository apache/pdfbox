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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.util.BitFlagHelper;

/**
 * This class represents a PDF annotation.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public abstract class PDAnnotation implements COSObjectable
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDAnnotation.class);

    /**
     * An annotation flag.
     */
    public static final int FLAG_INVISIBLE = 1 << 0;
    /**
     * An annotation flag.
     */
    public static final int FLAG_HIDDEN = 1 << 1;
    /**
     * An annotation flag.
     */
    public static final int FLAG_PRINTED = 1 << 2;
    /**
     * An annotation flag.
     */
    public static final int FLAG_NO_ZOOM = 1 << 3;
    /**
     * An annotation flag.
     */
    public static final int FLAG_NO_ROTATE = 1 << 4;
    /**
     * An annotation flag.
     */
    public static final int FLAG_NO_VIEW = 1 << 5;
    /**
     * An annotation flag.
     */
    public static final int FLAG_READ_ONLY = 1 << 6;
    /**
     * An annotation flag.
     */
    public static final int FLAG_LOCKED = 1 << 7;
    /**
     * An annotation flag.
     */
    public static final int FLAG_TOGGLE_NO_VIEW = 1 << 8;

    private COSDictionary dictionary;

    /**
     * Create the correct annotation from the base COS object.
     * 
     * @param base The COS object that is the annotation.
     * @return The correctly typed annotation object.
     * @throws IOException If there is an error while creating the annotation.
     */
    public static PDAnnotation createAnnotation(COSBase base) throws IOException
    {
        PDAnnotation annot = null;
        if (base instanceof COSDictionary)
        {
            COSDictionary annotDic = (COSDictionary) base;
            String subtype = annotDic.getNameAsString(COSName.SUBTYPE);
            if (PDAnnotationFileAttachment.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationFileAttachment(annotDic);
            }
            else if (PDAnnotationLine.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationLine(annotDic);
            }
            else if (PDAnnotationLink.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationLink(annotDic);
            }
            else if (PDAnnotationPopup.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationPopup(annotDic);
            }
            else if (PDAnnotationRubberStamp.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationRubberStamp(annotDic);
            }
            else if (PDAnnotationSquareCircle.SUB_TYPE_SQUARE.equals(subtype)
                    || PDAnnotationSquareCircle.SUB_TYPE_CIRCLE.equals(subtype))
            {
                annot = new PDAnnotationSquareCircle(annotDic);
            }
            else if (PDAnnotationText.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationText(annotDic);
            }
            else if (PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(subtype)
                    || PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE.equals(subtype)
                    || PDAnnotationTextMarkup.SUB_TYPE_SQUIGGLY.equals(subtype)
                    || PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT.equals(subtype))
            {
                annot = new PDAnnotationTextMarkup(annotDic);
            }
            else if (PDAnnotationLink.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationLink(annotDic);
            }
            else if (PDAnnotationWidget.SUB_TYPE.equals(subtype))
            {
                annot = new PDAnnotationWidget(annotDic);
            }
            else if (PDAnnotationMarkup.SUB_TYPE_FREETEXT.equals(subtype)
                    || PDAnnotationMarkup.SUB_TYPE_POLYGON.equals(subtype)
                    || PDAnnotationMarkup.SUB_TYPE_POLYLINE.equals(subtype)
                    || PDAnnotationMarkup.SUB_TYPE_CARET.equals(subtype)
                    || PDAnnotationMarkup.SUB_TYPE_INK.equals(subtype)
                    || PDAnnotationMarkup.SUB_TYPE_SOUND.equals(subtype))
            {
                annot = new PDAnnotationMarkup(annotDic);
            }
            else
            {
                // TODO not yet implemented:
                // Movie, Screen, PrinterMark, TrapNet, Watermark, 3D, Redact
                annot = new PDAnnotationUnknown(annotDic);
                LOG.debug("Unknown or unsupported annotation subtype " + subtype);
            }
        }
        else
        {
            throw new IOException("Error: Unknown annotation type " + base);
        }

        return annot;
    }

    /**
     * Constructor.
     */
    public PDAnnotation()
    {
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.TYPE, COSName.ANNOT);
    }

    /**
     * Constructor.
     * 
     * @param dict The annotations dictionary.
     */
    public PDAnnotation(COSDictionary dict)
    {
        dictionary = dict;
    }

    /**
     * returns the dictionary.
     * 
     * @return the dictionary
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * The annotation rectangle, defining the location of the annotation on the page in default user space units. This
     * is usually required and should not return null on valid PDF documents. But where this is a parent form field with
     * children, such as radio button collections then the rectangle will be null.
     * 
     * @return The Rect value of this annotation.
     */
    public PDRectangle getRectangle()
    {
        COSArray rectArray = (COSArray) dictionary.getDictionaryObject(COSName.RECT);
        PDRectangle rectangle = null;
        if (rectArray != null)
        {
            rectangle = new PDRectangle(rectArray);
        }
        return rectangle;
    }

    /**
     * This will set the rectangle for this annotation.
     * 
     * @param rectangle The new rectangle values.
     */
    public void setRectangle(PDRectangle rectangle)
    {
        dictionary.setItem(COSName.RECT, rectangle.getCOSArray());
    }

    /**
     * This will get the flags for this field.
     * 
     * @return flags The set of flags.
     */
    public int getAnnotationFlags()
    {
        return getDictionary().getInt(COSName.F, 0);
    }

    /**
     * This will set the flags for this field.
     * 
     * @param flags The new flags.
     */
    public void setAnnotationFlags(int flags)
    {
        getDictionary().setInt(COSName.F, flags);
    }

    /**
     * Interface method for COSObjectable.
     * 
     * @return This object as a standard COS object.
     */
    public COSBase getCOSObject()
    {
        return getDictionary();
    }

    /**
     * This will get the name of the current appearance stream if any.
     * 
     * @return The name of the appearance stream.
     */
    public String getAppearanceStream()
    {
        String retval = null;
        COSName name = (COSName) getDictionary().getDictionaryObject(COSName.AS);
        if (name != null)
        {
            retval = name.getName();
        }
        return retval;
    }

    /**
     * This will set the annotations appearance stream name.
     * 
     * @param as The name of the appearance stream.
     */
    public void setAppearanceStream(String as)
    {
        if (as == null)
        {
            getDictionary().removeItem(COSName.AS);
        }
        else
        {
            getDictionary().setItem(COSName.AS, COSName.getPDFName(as));
        }
    }

    /**
     * This will get the appearance dictionary associated with this annotation. This may return null.
     * 
     * @return This annotations appearance.
     */
    public PDAppearanceDictionary getAppearance()
    {
        PDAppearanceDictionary ap = null;
        COSDictionary apDic = (COSDictionary) dictionary.getDictionaryObject(COSName.AP);
        if (apDic != null)
        {
            ap = new PDAppearanceDictionary(apDic);
        }
        return ap;
    }

    /**
     * This will set the appearance associated with this annotation.
     * 
     * @param appearance The appearance dictionary for this annotation.
     */
    public void setAppearance(PDAppearanceDictionary appearance)
    {
        COSDictionary ap = null;
        if (appearance != null)
        {
            ap = appearance.getDictionary();
        }
        dictionary.setItem(COSName.AP, ap);
    }

    /**
     * Get the invisible flag.
     * 
     * @return The invisible flag.
     */
    public boolean isInvisible()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_INVISIBLE);
    }

    /**
     * Set the invisible flag.
     * 
     * @param invisible The new invisible flag.
     */
    public void setInvisible(boolean invisible)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_INVISIBLE, invisible);
    }

    /**
     * Get the hidden flag.
     * 
     * @return The hidden flag.
     */
    public boolean isHidden()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_HIDDEN);
    }

    /**
     * Set the hidden flag.
     * 
     * @param hidden The new hidden flag.
     */
    public void setHidden(boolean hidden)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_HIDDEN, hidden);
    }

    /**
     * Get the printed flag.
     * 
     * @return The printed flag.
     */
    public boolean isPrinted()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_PRINTED);
    }

    /**
     * Set the printed flag.
     * 
     * @param printed The new printed flag.
     */
    public void setPrinted(boolean printed)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_PRINTED, printed);
    }

    /**
     * Get the noZoom flag.
     * 
     * @return The noZoom flag.
     */
    public boolean isNoZoom()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_NO_ZOOM);
    }

    /**
     * Set the noZoom flag.
     * 
     * @param noZoom The new noZoom flag.
     */
    public void setNoZoom(boolean noZoom)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_NO_ZOOM, noZoom);
    }

    /**
     * Get the noRotate flag.
     * 
     * @return The noRotate flag.
     */
    public boolean isNoRotate()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_NO_ROTATE);
    }

    /**
     * Set the noRotate flag.
     * 
     * @param noRotate The new noRotate flag.
     */
    public void setNoRotate(boolean noRotate)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_NO_ROTATE, noRotate);
    }

    /**
     * Get the noView flag.
     * 
     * @return The noView flag.
     */
    public boolean isNoView()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_NO_VIEW);
    }

    /**
     * Set the noView flag.
     * 
     * @param noView The new noView flag.
     */
    public void setNoView(boolean noView)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_NO_VIEW, noView);
    }

    /**
     * Get the readOnly flag.
     * 
     * @return The readOnly flag.
     */
    public boolean isReadOnly()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_READ_ONLY);
    }

    /**
     * Set the readOnly flag.
     * 
     * @param readOnly The new readOnly flag.
     */
    public void setReadOnly(boolean readOnly)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_READ_ONLY, readOnly);
    }

    /**
     * Get the locked flag.
     * 
     * @return The locked flag.
     */
    public boolean isLocked()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_LOCKED);
    }

    /**
     * Set the locked flag.
     * 
     * @param locked The new locked flag.
     */
    public void setLocked(boolean locked)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_LOCKED, locked);
    }

    /**
     * Get the toggleNoView flag.
     * 
     * @return The toggleNoView flag.
     */
    public boolean isToggleNoView()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.F, FLAG_TOGGLE_NO_VIEW);
    }

    /**
     * Set the toggleNoView flag.
     * 
     * @param toggleNoView The new toggleNoView flag.
     */
    public void setToggleNoView(boolean toggleNoView)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.F, FLAG_TOGGLE_NO_VIEW, toggleNoView);
    }

    /**
     * Get the "contents" of the field.
     * 
     * @return the value of the contents.
     */
    public String getContents()
    {
        return dictionary.getString(COSName.CONTENTS);
    }

    /**
     * Set the "contents" of the field.
     * 
     * @param value the value of the contents.
     */
    public void setContents(String value)
    {
        dictionary.setString(COSName.CONTENTS, value);
    }

    /**
     * This will retrieve the date and time the annotation was modified.
     * 
     * @return the modified date/time (often in date format, but can be an arbitary string).
     */
    public String getModifiedDate()
    {
        return getDictionary().getString(COSName.M);
    }

    /**
     * This will set the the date and time the annotation was modified.
     * 
     * @param m the date and time the annotation was created.
     */
    public void setModifiedDate(String m)
    {
        getDictionary().setString(COSName.M, m);
    }

    /**
     * This will get the name, a string intended to uniquely identify each annotation within a page. Not to be confused
     * with some annotations Name entry which impact the default image drawn for them.
     * 
     * @return The identifying name for the Annotation.
     */
    public String getAnnotationName()
    {
        return getDictionary().getString(COSName.NM);
    }

    /**
     * This will set the name, a string intended to uniquely identify each annotation within a page. Not to be confused
     * with some annotations Name entry which impact the default image drawn for them.
     * 
     * @param nm The identifying name for the annotation.
     */
    public void setAnnotationName(String nm)
    {
        getDictionary().setString(COSName.NM, nm);
    }

    /**
     * This will get the key of this annotation in the structural parent tree.
     * 
     * @return the integer key of the annotation's entry in the structural parent tree
     */
    public int getStructParent()
    {
        return getDictionary().getInt(COSName.STRUCT_PARENT, 0);
    }

    /**
     * This will set the key for this annotation in the structural parent tree.
     * 
     * @param structParent The new key for this annotation.
     */
    public void setStructParent(int structParent)
    {
        getDictionary().setInt(COSName.STRUCT_PARENT, structParent);
    }

    /**
     * This will set the colour used in drawing various elements. As of PDF 1.6 these are : Background of icon when
     * closed Title bar of popup window Border of a link annotation
     * 
     * Colour is in DeviceRGB colourspace
     * 
     * @param c colour in the DeviceRGB colourspace
     * 
     */
    public void setColour(PDGamma c)
    {
        getDictionary().setItem(COSName.C, c);
    }

    /**
     * This will retrieve the colour used in drawing various elements. As of PDF 1.6 these are : Background of icon when
     * closed Title bar of popup window Border of a link annotation
     * 
     * Colour is in DeviceRGB colourspace
     * 
     * @return PDGamma object representing the colour
     * 
     */
    public PDGamma getColour()
    {
        COSArray c = (COSArray) getDictionary().getItem(COSName.C);
        if (c != null)
        {
            return new PDGamma(c);
        }
        else
        {
            return null;
        }
    }

    /**
     * This will retrieve the subtype of the annotation.
     * 
     * @return the subtype
     */
    public String getSubtype()
    {
        return this.getDictionary().getNameAsString(COSName.SUBTYPE);
    }

    /**
     * This will set the corresponding page for this annotation.
     * 
     * @param page is the corresponding page
     */
    public void setPage(PDPage page)
    {
        this.getDictionary().setItem(COSName.P, page);
    }

    /**
     * This will retrieve the corresponding page of this annotation.
     * 
     * @return the corresponding page
     */
    public PDPage getPage()
    {
        COSDictionary p = (COSDictionary) this.getDictionary().getDictionaryObject(COSName.P);
        if (p != null)
        {
            return new PDPage(p);
        }
        return null;
    }

}
