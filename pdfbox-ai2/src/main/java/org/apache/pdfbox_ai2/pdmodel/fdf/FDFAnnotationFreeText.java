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
package org.apache.pdfbox_ai2.pdmodel.fdf;

import java.io.IOException;

import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.w3c.dom.Element;

/**
 * This represents a FreeText FDF annotation.
 *
 * @author Ben Litchfield
 */
public class FDFAnnotationFreeText extends FDFAnnotation
{
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
        String rotation = element.getAttribute("rotation");
        if (rotation != null && !rotation.isEmpty())
        {
            setRotation(Integer.parseInt(rotation));
        }
    }

    /**
     * This will set the form of quadding (justification) of the annotation text.
     * 
     * @param justification The quadding of the text.
     */
    public void setJustification(String justification)
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
    public void setRotation(int rotation)
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

}
