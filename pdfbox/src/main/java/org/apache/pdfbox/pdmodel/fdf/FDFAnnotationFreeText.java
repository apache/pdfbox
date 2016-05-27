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

import java.io.IOException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Element;

/**
 * This represents a FreeText FDF annotation.
 *
 * @author Ben Litchfield
 */
public class FDFAnnotationFreeText extends FDFAnnotation
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotationFreeText.class);
 
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "FreeText";

    /**
     * Default constructor.
     */
    public FDFAnnotationFreeText()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationFreeText(COSDictionary a)
    {
        super(a);
    }

    /**
     * Constructor.
     *
     * @param element An XFDF element.
     *
     * @throws IOException If there is an error extracting information from the element.
     */
    public FDFAnnotationFreeText(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        setJustification(element.getAttribute("justification"));
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            setDefaultAppearance(xpath.evaluate("defaultappearance", element));
            setDefaultStyle(xpath.evaluate("defaultstyle", element));
        }
        catch (XPathExpressionException ex)
        {
            LOG.debug("Error while evaluating XPath expression");
        }
        initCallout(element);
        String rotation = element.getAttribute("rotation");
        if (rotation != null && !rotation.isEmpty())
        {
            setRotation(Integer.parseInt(rotation));
        }
        initFringe(element);
        String lineEndingStyle = element.getAttribute("head");
        if (lineEndingStyle != null && !lineEndingStyle.isEmpty())
        {
            setLineEndingStyle(lineEndingStyle);
        }
    }

    private void initFringe(Element element) throws IOException
    {
        String fringe = element.getAttribute("fringe");
        if (fringe != null && !fringe.isEmpty())
        {
            String[] fringeValues = fringe.split(",");
            if (fringeValues.length != 4)
            {
                throw new IOException("Error: wrong amount of numbers in attribute 'fringe'");
            }
            PDRectangle rect = new PDRectangle();
            rect.setLowerLeftX(Float.parseFloat(fringeValues[0]));
            rect.setLowerLeftY(Float.parseFloat(fringeValues[1]));
            rect.setUpperRightX(Float.parseFloat(fringeValues[2]));
            rect.setUpperRightY(Float.parseFloat(fringeValues[3]));
            setFringe(rect);
        }
    }

    private void initCallout(Element element) throws IOException
    {
        String callout = element.getAttribute("callout");
        if (callout != null && !callout.isEmpty())
        {
            String[] calloutValues = callout.split(",");
            float[] values = new float[calloutValues.length];
            for (int i = 0; i < calloutValues.length; i++)
            {
                values[i] = Float.parseFloat(calloutValues[i]);
            }
            setCallout(values);
        }
    }

    /**
     * This will set the coordinates of the callout line.
     *
     * @param callout An array of four or six numbers specifying a callout line attached to the free
     * text annotation. Six numbers [ x1 y1 x2 y2 x3 y3 ] represent the starting, knee point, and
     * ending coordinates of the line in default user space, Four numbers [ x1 y1 x2 y2 ] represent
     * the starting and ending coordinates of the line.
     */
    public final void setCallout(float[] callout)
    {
        COSArray newCallout = new COSArray();
        newCallout.setFloatArray(callout);
        annot.setItem(COSName.CL, newCallout);
    }

    /**
     * This will get the coordinates of the the callout line.
     *
     * @return An array of four or six numbers specifying a callout line attached to the free text
     * annotation. Six numbers [ x1 y1 x2 y2 x3 y3 ] represent the starting, knee point, and ending
     * coordinates of the line in default user space, Four numbers [ x1 y1 x2 y2 ] represent the
     * starting and ending coordinates of the line.
     */
    public float[] getCallout()
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.CL);
        if (array != null)
        {
            return array.toFloatArray();
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the form of quadding (justification) of the annotation text.
     * 
     * @param justification The quadding of the text.
     */
    public final void setJustification(String justification)
    {
        int quadding = 0;
        if ("centered".equals(justification))
        {
            quadding = 1;
        }
        else if ("right".equals(justification))
        {
            quadding = 2;
        }
        annot.setInt(COSName.Q, quadding);
    }

    /**
     * This will get the form of quadding (justification) of the annotation text.
     * 
     * @return The quadding of the text.
     */
    public String getJustification()
    {
        return "" + annot.getInt(COSName.Q, 0);
    }

    /**
     * This will set the clockwise rotation in degrees.
     * 
     * @param rotation The number of degrees of clockwise rotation.
     */
    public final void setRotation(int rotation)
    {
        annot.setInt(COSName.ROTATE, rotation);
    }

    /**
     * This will get the clockwise rotation in degrees.
     * 
     * @return The number of degrees of clockwise rotation.
     */
    public String getRotation()
    {
        return annot.getString(COSName.ROTATE);
    }

    /**
     * Set the default appearance string.
     *
     * @param appearance The new default appearance string.
     */
    public final void setDefaultAppearance(String appearance)
    {
        annot.setString(COSName.DA, appearance);
    }

    /**
     * Get the default appearance string.
     *
     * @return The default appearance of the annotation.
     */
    public String getDefaultAppearance()
    {
        return annot.getString(COSName.DA);

    }

    /**
     * Set the default style string.
     *
     * @param style The new default style string.
     */
    public final void setDefaultStyle(String style)
    {
        annot.setString(COSName.DS, style);
    }

    /**
     * Get the default style string.
     *
     * @return The default style of the annotation.
     */
    public String getDefaultStyle()
    {
        return annot.getString(COSName.DS);
    }

    /**
     * This will set the fringe rectangle. Giving the difference between the annotations rectangle
     * and where the drawing occurs. (To take account of any effects applied through the BE entry
     * for example)
     *
     * @param fringe the fringe
     */
    public final void setFringe(PDRectangle fringe)
    {
        annot.setItem(COSName.RD, fringe);
    }

    /**
     * This will get the fringe. Giving the difference between the annotations rectangle and where
     * the drawing occurs. (To take account of any effects applied through the BE entry for example)
     *
     * @return the rectangle difference
     */
    public PDRectangle getFringe()
    {
        COSArray rd = (COSArray) annot.getDictionaryObject(COSName.RD);
        if (rd != null)
        {
            return new PDRectangle(rd);
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the line ending style.
     *
     * @param style The new style.
     */
    public final void setLineEndingStyle(String style)
    {
        annot.setName(COSName.LE, style);
    }

    /**
     * This will retrieve the line ending style.
     *
     * @return The ending style for the start point.
     */
    public String getLineEndingStyle()
    {
        return annot.getNameAsString(COSName.LE);
    }
}
