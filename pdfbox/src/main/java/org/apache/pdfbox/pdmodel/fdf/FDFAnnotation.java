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
package org.apache.pdfbox.pdmodel.fdf;

import java.awt.Color;
import java.io.IOException;
import java.util.Calendar;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.util.DateConverter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents an FDF annotation that is part of the FDF document.
 *
 * @author Ben Litchfield
 * @author Johanneke Lamberink
 * 
 * */
public abstract class FDFAnnotation implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotation.class);

    /**
     * An annotation flag.
     */
    private static final int FLAG_INVISIBLE = 1 << 0;
    /**
     * An annotation flag.
     */
    private static final int FLAG_HIDDEN = 1 << 1;
    /**
     * An annotation flag.
     */
    private static final int FLAG_PRINTED = 1 << 2;
    /**
     * An annotation flag.
     */
    private static final int FLAG_NO_ZOOM = 1 << 3;
    /**
     * An annotation flag.
     */
    private static final int FLAG_NO_ROTATE = 1 << 4;
    /**
     * An annotation flag.
     */
    private static final int FLAG_NO_VIEW = 1 << 5;
    /**
     * An annotation flag.
     */
    private static final int FLAG_READ_ONLY = 1 << 6;
    /**
     * An annotation flag.
     */
    private static final int FLAG_LOCKED = 1 << 7;
    /**
     * An annotation flag.
     */
    private static final int FLAG_TOGGLE_NO_VIEW = 1 << 8;
    
    /**
     * Annotation dictionary.
     */
    protected COSDictionary annot;

    /**
     * Default constructor.
     */
    public FDFAnnotation()
    {
        annot = new COSDictionary();
        annot.setItem(COSName.TYPE, COSName.ANNOT);
    }

    /**
     * Constructor.
     *
     * @param a The FDF annotation.
     */
    public FDFAnnotation(COSDictionary a)
    {
        annot = a;
    }

    /**
     * Constructor.
     *
     * @param element An XFDF element.
     *
     * @throws IOException If there is an error extracting data from the element.
     */
    public FDFAnnotation(Element element) throws IOException
    {
        this();

        String page = element.getAttribute("page");
        if (page == null || page.isEmpty())
        {
            throw new IOException("Error: missing required attribute 'page'");
        }
        setPage(Integer.parseInt(page));

        String color = element.getAttribute("color");
        if (color != null && color.length() == 7 && color.charAt(0) == '#')
        {
            int colorValue = Integer.parseInt(color.substring(1, 7), 16);
            setColor(new Color(colorValue));
        }

        setDate(element.getAttribute("date"));

        String flags = element.getAttribute("flags");
        if (flags != null)
        {
            String[] flagTokens = flags.split(",");
            for (String flagToken : flagTokens)
            {
                switch (flagToken)
                {
                    case "invisible":
                        setInvisible(true);
                        break;
                    case "hidden":
                        setHidden(true);
                        break;
                    case "print":
                        setPrinted(true);
                        break;
                    case "nozoom":
                        setNoZoom(true);
                        break;
                    case "norotate":
                        setNoRotate(true);
                        break;
                    case "noview":
                        setNoView(true);
                        break;
                    case "readonly":
                        setReadOnly(true);
                        break;
                    case "locked":
                        setLocked(true);
                        break;
                    case "togglenoview":
                        setToggleNoView(true);
                        break;
                    default:
                        break;
                }
            }
        }

        setName(element.getAttribute("name"));

        String rect = element.getAttribute("rect");
        if (rect == null)
        {
            throw new IOException("Error: missing attribute 'rect'");
        }
        String[] rectValues = rect.split(",");
        if (rectValues.length != 4)
        {
            throw new IOException("Error: wrong amount of numbers in attribute 'rect'");
        }
        float[] values = new float[4];
        for (int i = 0; i < 4; i++)
        {
            values[i] = Float.parseFloat(rectValues[i]);
        }
        COSArray array = new COSArray();
        array.setFloatArray(values);
        setRectangle(new PDRectangle(array));

        setTitle(element.getAttribute("title"));

        /*
         * Set the markup annotation attributes
         */
        setCreationDate(DateConverter.toCalendar(element.getAttribute("creationdate")));
        String opac = element.getAttribute("opacity");
        if (opac != null && !opac.isEmpty())
        {
            setOpacity(Float.parseFloat(opac));
        }
        setSubject(element.getAttribute("subject"));

        String intent = element.getAttribute("intent");
        if (intent.isEmpty())
        {
            // not conforming to spec, but qoppa produces it and Adobe accepts it
            intent = element.getAttribute("IT");
        }
        setIntent(intent);

        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            setContents(xpath.evaluate("contents[1]", element));
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression for richtext contents");
        }

        try
        {
            Node richContents = (Node) xpath.evaluate("contents-richtext[1]", element,
                    XPathConstants.NODE);
            if (richContents != null)
            {
                setRichContents(richContentsToString(richContents, true));
                setContents(richContents.getTextContent().trim());
            }
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression for richtext contents");
        }

        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
        String width = element.getAttribute("width");
        if (width != null && !width.isEmpty())
        {
            borderStyle.setWidth(Float.parseFloat(width));
        }
        if (borderStyle.getWidth() > 0)
        {
            String style = element.getAttribute("style");
            if (style != null && !style.isEmpty())
            {
                switch (style)
                {
                    case "dash":
                        borderStyle.setStyle("D");
                        break;
                    case "bevelled":
                        borderStyle.setStyle("B");
                        break;
                    case "inset":
                        borderStyle.setStyle("I");
                        break;
                    case "underline":
                        borderStyle.setStyle("U");
                        break;
                    case "cloudy":
                        borderStyle.setStyle("S");
                        PDBorderEffectDictionary borderEffect = new PDBorderEffectDictionary();
                        borderEffect.setStyle("C");
                        String intensity = element.getAttribute("intensity");
                        if (intensity != null && !intensity.isEmpty())
                        {
                            borderEffect.setIntensity(Float.parseFloat(element
                                    .getAttribute("intensity")));
                        }
                        setBorderEffect(borderEffect);
                        break;
                    default:
                        borderStyle.setStyle("S");
                        break;
                }
            }
            String dashes = element.getAttribute("dashes");
            if (dashes != null && !dashes.isEmpty())
            {
                String[] dashesValues = dashes.split(",");
                COSArray dashPattern = new COSArray();
                for (String dashesValue : dashesValues)
                {
                    dashPattern.add(COSNumber.get(dashesValue));
                }
                borderStyle.setDashStyle(dashPattern);
            }
            setBorderStyle(borderStyle);
        }
    }

    /**
     * Create the correct FDFAnnotation.
     *
     * @param fdfDic The FDF dictionary.
     *
     * @return A newly created FDFAnnotation
     *
     * @throws IOException If there is an error accessing the FDF information.
     */
    public static FDFAnnotation create(COSDictionary fdfDic) throws IOException
    {
        FDFAnnotation retval = null;
        if (fdfDic != null)
        {
            if (FDFAnnotationText.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationText(fdfDic);
            }
            else if (FDFAnnotationCaret.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationCaret(fdfDic);
            }
            else if (FDFAnnotationFreeText.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationFreeText(fdfDic);
            }
            else if (FDFAnnotationFileAttachment.SUBTYPE.equals(fdfDic
                    .getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationFileAttachment(fdfDic);
            }
            else if (FDFAnnotationHighlight.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationHighlight(fdfDic);
            }
            else if (FDFAnnotationInk.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationInk(fdfDic);
            }
            else if (FDFAnnotationLine.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationLine(fdfDic);
            }
            else if (FDFAnnotationLink.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationLink(fdfDic);
            }
            else if (FDFAnnotationCircle.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationCircle(fdfDic);
            }
            else if (FDFAnnotationSquare.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationSquare(fdfDic);
            }
            else if (FDFAnnotationPolygon.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationPolygon(fdfDic);
            }
            else if (FDFAnnotationPolyline.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationPolyline(fdfDic);
            }
            else if (FDFAnnotationSound.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationSound(fdfDic);
            }
            else if (FDFAnnotationSquiggly.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationSquiggly(fdfDic);
            }
            else if (FDFAnnotationStamp.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationStamp(fdfDic);
            }
            else if (FDFAnnotationStrikeOut.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationStrikeOut(fdfDic);
            }
            else if (FDFAnnotationUnderline.SUBTYPE.equals(fdfDic.getNameAsString(COSName.SUBTYPE)))
            {
                retval = new FDFAnnotationUnderline(fdfDic);
            }
            else
            {
                LOG.warn("Unknown or unsupported annotation type '"
                        + fdfDic.getNameAsString(COSName.SUBTYPE) + "'");
            }
        }
        return retval;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return annot;
    }

    /**
     * This will get the page number or null if it does not exist.
     *
     * @return The page number.
     */
    public Integer getPage()
    {
        Integer retval = null;
        COSNumber page = (COSNumber) annot.getDictionaryObject(COSName.PAGE);
        if (page != null)
        {
            retval = page.intValue();
        }
        return retval;
    }

    /**
     * This will set the page.
     *
     * @param page The page number.
     */
    public final void setPage(int page)
    {
        annot.setInt(COSName.PAGE, page);
    }

    /**
     * Get the annotation color.
     *
     * @return The annotation color, or null if there is none.
     */
    public Color getColor()
    {
        Color retval = null;
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.C);
        if (array != null)
        {
            float[] rgb = array.toFloatArray();
            if (rgb.length >= 3)
            {
                retval = new Color(rgb[0], rgb[1], rgb[2]);
            }
        }
        return retval;
    }

    /**
     * Set the annotation color.
     *
     * @param c The annotation color.
     */
    public final void setColor(Color c)
    {
        COSArray color = null;
        if (c != null)
        {
            float[] colors = c.getRGBColorComponents(null);
            color = new COSArray();
            color.setFloatArray(colors);
        }
        annot.setItem(COSName.C, color);
    }

    /**
     * Modification date.
     *
     * @return The date as a string.
     */
    public String getDate()
    {
        return annot.getString(COSName.M);
    }

    /**
     * The annotation modification date.
     *
     * @param date The date to store in the FDF annotation.
     */
    public final void setDate(String date)
    {
        annot.setString(COSName.M, date);
    }

    /**
     * Get the invisible flag.
     *
     * @return The invisible flag.
     */
    public boolean isInvisible()
    {
        return annot.getFlag(COSName.F, FLAG_INVISIBLE);
    }

    /**
     * Set the invisible flag.
     *
     * @param invisible The new invisible flag.
     */
    public final void setInvisible(boolean invisible)
    {
        annot.setFlag(COSName.F, FLAG_INVISIBLE, invisible);
    }

    /**
     * Get the hidden flag.
     *
     * @return The hidden flag.
     */
    public boolean isHidden()
    {
        return annot.getFlag(COSName.F, FLAG_HIDDEN);
    }

    /**
     * Set the hidden flag.
     *
     * @param hidden The new hidden flag.
     */
    public final void setHidden(boolean hidden)
    {
        annot.setFlag(COSName.F, FLAG_HIDDEN, hidden);
    }

    /**
     * Get the printed flag.
     *
     * @return The printed flag.
     */
    public boolean isPrinted()
    {
        return annot.getFlag(COSName.F, FLAG_PRINTED);
    }

    /**
     * Set the printed flag.
     *
     * @param printed The new printed flag.
     */
    public final void setPrinted(boolean printed)
    {
        annot.setFlag(COSName.F, FLAG_PRINTED, printed);
    }

    /**
     * Get the noZoom flag.
     *
     * @return The noZoom flag.
     */
    public boolean isNoZoom()
    {
        return annot.getFlag(COSName.F, FLAG_NO_ZOOM);
    }

    /**
     * Set the noZoom flag.
     *
     * @param noZoom The new noZoom flag.
     */
    public final void setNoZoom(boolean noZoom)
    {
        annot.setFlag(COSName.F, FLAG_NO_ZOOM, noZoom);
    }

    /**
     * Get the noRotate flag.
     *
     * @return The noRotate flag.
     */
    public boolean isNoRotate()
    {
        return annot.getFlag(COSName.F, FLAG_NO_ROTATE);
    }

    /**
     * Set the noRotate flag.
     *
     * @param noRotate The new noRotate flag.
     */
    public final void setNoRotate(boolean noRotate)
    {
        annot.setFlag(COSName.F, FLAG_NO_ROTATE, noRotate);
    }

    /**
     * Get the noView flag.
     *
     * @return The noView flag.
     */
    public boolean isNoView()
    {
        return annot.getFlag(COSName.F, FLAG_NO_VIEW);
    }

    /**
     * Set the noView flag.
     *
     * @param noView The new noView flag.
     */
    public final void setNoView(boolean noView)
    {
        annot.setFlag(COSName.F, FLAG_NO_VIEW, noView);
    }

    /**
     * Get the readOnly flag.
     *
     * @return The readOnly flag.
     */
    public boolean isReadOnly()
    {
        return annot.getFlag(COSName.F, FLAG_READ_ONLY);
    }

    /**
     * Set the readOnly flag.
     *
     * @param readOnly The new readOnly flag.
     */
    public final void setReadOnly(boolean readOnly)
    {
        annot.setFlag(COSName.F, FLAG_READ_ONLY, readOnly);
    }

    /**
     * Get the locked flag.
     *
     * @return The locked flag.
     */
    public boolean isLocked()
    {
        return annot.getFlag(COSName.F, FLAG_LOCKED);
    }

    /**
     * Set the locked flag.
     *
     * @param locked The new locked flag.
     */
    public final void setLocked(boolean locked)
    {
        annot.setFlag(COSName.F, FLAG_LOCKED, locked);
    }

    /**
     * Get the toggleNoView flag.
     *
     * @return The toggleNoView flag.
     */
    public boolean isToggleNoView()
    {
        return annot.getFlag(COSName.F, FLAG_TOGGLE_NO_VIEW);
    }

    /**
     * Set the toggleNoView flag.
     *
     * @param toggleNoView The new toggleNoView flag.
     */
    public final void setToggleNoView(boolean toggleNoView)
    {
        annot.setFlag(COSName.F, FLAG_TOGGLE_NO_VIEW, toggleNoView);
    }

    /**
     * Set a unique name for an annotation.
     *
     * @param name The unique annotation name.
     */
    public final void setName(String name)
    {
        annot.setString(COSName.NM, name);
    }

    /**
     * Get the annotation name.
     *
     * @return The unique name of the annotation.
     */
    public String getName()
    {
        return annot.getString(COSName.NM);
    }

    /**
     * Set the rectangle associated with this annotation.
     *
     * @param rectangle The annotation rectangle.
     */
    public final void setRectangle(PDRectangle rectangle)
    {
        annot.setItem(COSName.RECT, rectangle);
    }

    /**
     * The rectangle associated with this annotation.
     *
     * @return The annotation rectangle.
     */
    public PDRectangle getRectangle()
    {
        PDRectangle retval = null;
        COSArray rectArray = (COSArray) annot.getDictionaryObject(COSName.RECT);
        if (rectArray != null)
        {
            retval = new PDRectangle(rectArray);
        }
        return retval;
    }

    /**
     * Set the contents, or a description, for an annotation.
     *
     * @param contents The annotation contents, or a description.
     */
    public final void setContents(String contents)
    {
        annot.setString(COSName.CONTENTS, contents);
    }

    /**
     * Get the text, or a description, of the annotation.
     *
     * @return The text, or a description, of the annotation.
     */
    public String getContents()
    {
        return annot.getString(COSName.CONTENTS);
    }

    /**
     * Set a unique title for an annotation.
     *
     * @param title The annotation title.
     */
    public final void setTitle(String title)
    {
        annot.setString(COSName.T, title);
    }

    /**
     * Get the annotation title.
     *
     * @return The title of the annotation.
     */
    public String getTitle()
    {
        return annot.getString(COSName.T);
    }

    /**
     * The annotation create date.
     *
     * @return The date of the creation of the annotation date
     *
     * @throws IOException If there is an error converting the string to a Calendar object.
     */
    public Calendar getCreationDate() throws IOException
    {
        return annot.getDate(COSName.CREATION_DATE);
    }

    /**
     * Set the creation date.
     *
     * @param date The date the annotation was created.
     */
    public final void setCreationDate(Calendar date)
    {
        annot.setDate(COSName.CREATION_DATE, date);
    }

    /**
     * Set the annotation opacity.
     *
     * @param opacity The new opacity value.
     */
    public final void setOpacity(float opacity)
    {
        annot.setFloat(COSName.CA, opacity);
    }

    /**
     * Get the opacity value.
     *
     * @return The opacity of the annotation.
     */
    public float getOpacity()
    {
        return annot.getFloat(COSName.CA, 1f);

    }

    /**
     * A short description of the annotation.
     *
     * @param subject The annotation subject.
     */
    public final void setSubject(String subject)
    {
        annot.setString(COSName.SUBJ, subject);
    }

    /**
     * Get the description of the annotation.
     *
     * @return The subject of the annotation.
     */
    public String getSubject()
    {
        return annot.getString(COSName.SUBJ);
    }

    /**
     * The intent of the annotation.
     * 
     * @param intent The annotation's intent.
     */
    public final void setIntent(String intent)
    {
        annot.setName(COSName.IT, intent);
    }

    /**
     * Get the intent of the annotation.
     * 
     * @return The intent of the annotation.
     */
    public String getIntent()
    {
        return annot.getNameAsString(COSName.IT);
    }

    /**
     * This will retrieve the rich text stream which is displayed in the popup window.
     *
     * @return the rich text stream.
     */
    public String getRichContents()
    {
        return getStringOrStream(annot.getDictionaryObject(COSName.RC));
    }

    /**
     * This will set the rich text stream which is displayed in the popup window.
     *
     * @param rc the rich text stream.
     */
    public final void setRichContents(String rc)
    {
        annot.setItem(COSName.RC, new COSString(rc));
    }

    /**
     * This will set the border style dictionary, specifying the width and dash pattern used in drawing the annotation.
     *
     * @param bs the border style dictionary to set.
     *
     */
    public final void setBorderStyle(PDBorderStyleDictionary bs)
    {
        annot.setItem(COSName.BS, bs);
    }

    /**
     * This will retrieve the border style dictionary, specifying the width and dash pattern used in drawing the
     * annotation.
     *
     * @return the border style dictionary.
     */
    public PDBorderStyleDictionary getBorderStyle()
    {
        COSDictionary bs = (COSDictionary) annot.getDictionaryObject(COSName.BS);
        if (bs != null)
        {
            return new PDBorderStyleDictionary(bs);
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the border effect dictionary, describing the effect applied to the border described by the BS
     * entry.
     *
     * @param be the border effect dictionary to set.
     *
     */
    public final void setBorderEffect(PDBorderEffectDictionary be)
    {
        annot.setItem(COSName.BE, be);
    }

    /**
     * This will retrieve the border style dictionary, describing the effect applied to the border described by the BS
     * entry.
     *
     * @return the border effect dictionary.
     */
    public PDBorderEffectDictionary getBorderEffect()
    {
        COSDictionary be = (COSDictionary) annot.getDictionaryObject(COSName.BE);
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
     * Get a text or text stream.
     *
     * Some dictionary entries allow either a text or a text stream.
     *
     * @param base the potential text or text stream
     * @return the text stream
     */
    protected final String getStringOrStream(COSBase base)
    {
        if (base == null)
        {
            return "";
        }
        else if (base instanceof COSString)
        {
            return ((COSString) base).getString();
        }
        else if (base instanceof COSStream)
        {
            return ((COSStream) base).toTextString();
        }
        else
        {
            return "";
        }
    }

    private String richContentsToString(Node node, boolean root)
    {
        String retval = "";
        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            NodeList nodelist = (NodeList) xpath.evaluate("*", node, XPathConstants.NODESET);
            String subString = "";
            if (nodelist.getLength() == 0)
            {
                subString = node.getFirstChild().getNodeValue();
            }
            for (int i = 0; i < nodelist.getLength(); i++)
            {
                Node child = nodelist.item(i);
                if (child instanceof Element)
                {
                    subString += richContentsToString(child, false);
                }
            }
            NamedNodeMap attributes = node.getAttributes();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < attributes.getLength(); i++)
            {
                Node attribute = attributes.item(i);
                builder.append(String.format(" %s=\"%s\"", attribute.getNodeName(),
                        attribute.getNodeValue()));
            }
            if (root)
            {
                return subString;
            }
            retval = String.format("<%s%s>%s</%s>", node.getNodeName(), builder.toString(),
                    subString, node.getNodeName());
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression for richtext contents");
        }
        return retval;
    }
}
