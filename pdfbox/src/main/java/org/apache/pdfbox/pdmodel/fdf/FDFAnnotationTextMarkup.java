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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.w3c.dom.Element;

/**
 * This abstract class is used as a superclass for the different FDF annotations with text markup attributes.
 * 
 * @author Johanneke Lamberink
 */
public abstract class FDFAnnotationTextMarkup extends FDFAnnotation
{
    /**
     * Default constructor.
     */
    public FDFAnnotationTextMarkup()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationTextMarkup(COSDictionary a)
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
    public FDFAnnotationTextMarkup(Element element) throws IOException
    {
        super(element);

        String coords = element.getAttribute("coords");
        if (coords == null || coords.isEmpty())
        {
            throw new IOException("Error: missing attribute 'coords'");
        }
        String[] coordsValues = coords.split(",");
        if (coordsValues.length < 8)
        {
            throw new IOException("Error: too little numbers in attribute 'coords'");
        }
        float[] values = new float[coordsValues.length];
        for (int i = 0; i < coordsValues.length; i++)
        {
            values[i] = Float.parseFloat(coordsValues[i]);
        }
        setCoords(values);
    }

    /**
     * Set the coordinates of individual words or group of words.
     * 
     * The quadliterals shall encompasses a word or group of contiguous words in the text underlying the annotation. The
     * coordinates for each quadrilateral shall be given in the order x1 y1 x2 y2 x3 y3 x4 y4.
     *
     * @param coords an array of 8 􏰍 n numbers specifying the coordinates of n quadrilaterals.
     */
    public void setCoords(float[] coords)
    {
        COSArray newQuadPoints = new COSArray();
        newQuadPoints.setFloatArray(coords);
        annot.setItem(COSName.QUADPOINTS, newQuadPoints);
    }

    /**
     * Get the coordinates of individual words or group of words.
     * 
     * @see #setCoords(float[])
     * @return the array of 8 􏰍 n numbers specifying the coordinates of n quadrilaterals.
     */
    public float[] getCoords()
    {
        COSArray quadPoints = (COSArray) annot.getItem(COSName.QUADPOINTS);
        if (quadPoints != null)
        {
            return quadPoints.toFloatArray();
        }
        else
        {
            return null; // Should never happen as this is a required item
        }
    }

}
