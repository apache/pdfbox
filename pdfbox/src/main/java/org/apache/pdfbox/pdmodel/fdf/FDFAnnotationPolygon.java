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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.w3c.dom.Element;

/**
 * This represents a Polygon FDF annotation.
 *
 * @author Ben Litchfield
 * @author Johanneke Lamberink
 */
public class FDFAnnotationPolygon extends FDFAnnotation
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotationPolygon.class);
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Polygon";

    /**
     * Default constructor.
     */
    public FDFAnnotationPolygon()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationPolygon(COSDictionary a)
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
    public FDFAnnotationPolygon(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        initVertices(element);
        String color = element.getAttribute("interior-color");
        if (color != null && color.length() == 7 && color.charAt(0) == '#')
        {
            int colorValue = Integer.parseInt(color.substring(1, 7), 16);
            setInteriorColor(new Color(colorValue));
        }
    }

    private void initVertices(Element element) throws IOException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            String vertices = xpath.evaluate("vertices", element);
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
            LOG.debug("Error while evaluating XPath expression for polygon vertices");
        }
    }

    /**
     * This will set the coordinates of the the vertices.
     *
     * @param vertices array of floats [x1, y1, x2, y2, ...] vertex coordinates in default user space.
     */
    public final void setVertices(float[] vertices)
    {
        COSArray newVertices = new COSArray();
        newVertices.setFloatArray(vertices);
        annot.setItem(COSName.VERTICES, newVertices);
    }

    /**
     * This will get the coordinates of the the vertices.
     *
     * @return array of floats [x1, y1, x2, y2, ...] vertex coordinates in default user space.
     */
    public float[] getVertices()
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.VERTICES);
        if (array != null)
        {
            return array.toFloatArray();
        }
        else
        {
            return null; // Should never happen as this is a required item
        }
    }

    /**
     * This will set interior color of the drawn area.
     *
     * @param color The interior color of the drawn area.
     */
    public final void setInteriorColor(Color color)
    {
        COSArray array = null;
        if (color != null)
        {
            float[] colors = color.getRGBColorComponents(null);
            array = new COSArray();
            array.setFloatArray(colors);
        }
        annot.setItem(COSName.IC, array);
    }

    /**
     * This will get interior color of the drawn area.
     *
     * @return object representing the color.
     */
    public Color getInteriorColor()
    {
        Color retval = null;
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.IC);
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
