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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.w3c.dom.Element;

/**
 * This represents a Polyline FDF annotation.
 *
 * @author Ben Litchfield
 * @author Johanneke Lamberink
 */
public class FDFAnnotationPolyline extends FDFAnnotation
{
    private static final Logger LOG = LogManager.getLogger(FDFAnnotationPolyline.class);
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Polyline";

    /**
     * Default constructor.
     */
    public FDFAnnotationPolyline()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationPolyline(COSDictionary a)
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
    public FDFAnnotationPolyline(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        initVertices(element);
        initStyles(element);
    }

    private void initVertices(Element element) throws IOException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            String vertices = xpath.evaluate("vertices[1]", element);
            if (vertices == null || vertices.isEmpty())
            {
                throw new IOException("Error: missing element 'vertices'");
            }
            String[] verticesValues = vertices.split(",|;");
            float[] values = new float[verticesValues.length];
            for (int i = 0; i < verticesValues.length; i++)
            {
                values[i] = Float.parseFloat(verticesValues[i]);
            }
            setVertices(values);
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression for polyline vertices", e);
        }
    }

    private void initStyles(Element element)
    {
        String startStyle = element.getAttribute("head");
        if (startStyle != null && !startStyle.isEmpty())
        {
            setStartPointEndingStyle(startStyle);
        }
        String endStyle = element.getAttribute("tail");
        if (endStyle != null && !endStyle.isEmpty())
        {
            setEndPointEndingStyle(endStyle);
        }

        String color = element.getAttribute("interior-color");
        if (color != null && color.length() == 7 && color.charAt(0) == '#')
        {
            int colorValue = Integer.parseInt(color.substring(1, 7), 16);
            setInteriorColor(new Color(colorValue));
        }
    }

    /**
     * This will set the coordinates of the the vertices.
     *
     * @param vertices array of floats [x1, y1, x2, y2, ...] vertex coordinates in default user space.
     */
    public void setVertices(float[] vertices)
    {
        annot.setItem(COSName.VERTICES, COSArray.of(vertices));
    }

    /**
     * This will get the coordinates of the the vertices.
     *
     * @return array of floats [x1, y1, x2, y2, ...] vertex coordinates in default user space.
     */
    public float[] getVertices()
    {
        COSArray array = annot.getCOSArray(COSName.VERTICES);
        return array != null ? array.toFloatArray() : null;
    }

    /**
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setStartPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSArray array = annot.getCOSArray(COSName.LE);
        if (array == null)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(actualStyle));
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            annot.setItem(COSName.LE, array);
        }
        else
        {
            array.setName(0, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the start point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the start point.
     */
    public String getStartPointEndingStyle()
    {
        COSArray array = annot.getCOSArray(COSName.LE);
        return array != null ? array.getName(0) : PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set the line ending style for the end point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setEndPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSArray array = annot.getCOSArray(COSName.LE);
        if (array == null)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            array.add(COSName.getPDFName(actualStyle));
            annot.setItem(COSName.LE, array);
        }
        else
        {
            array.setName(1, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the end point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the end point.
     */
    public String getEndPointEndingStyle()
    {
        COSArray array = annot.getCOSArray(COSName.LE);
        return array != null ? array.getName(1) : PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set interior color of the line endings defined in the LE entry.
     *
     * @param color The interior color of the line endings.
     */
    public void setInteriorColor(Color color)
    {
        COSArray array = null;
        if (color != null)
        {
            array = COSArray.of(color.getRGBColorComponents(null));
        }
        annot.setItem(COSName.IC, array);
    }

    /**
     * This will retrieve the interior color of the line endings defined in the LE entry.
     *
     * @return object representing the color.
     */
    public Color getInteriorColor()
    {
        Color retval = null;
        COSArray array = annot.getCOSArray(COSName.IC);
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
}
