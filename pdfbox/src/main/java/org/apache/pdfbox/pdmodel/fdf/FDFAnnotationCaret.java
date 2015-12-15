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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Element;

/**
 * This represents a Caret FDF annotation.
 *
 * @author Ben Litchfield
 */
public class FDFAnnotationCaret extends FDFAnnotation
{
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Caret";

    /**
     * Default constructor.
     */
    public FDFAnnotationCaret()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationCaret(COSDictionary a)
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
    public FDFAnnotationCaret(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        initFringe(element);

        String symbol = element.getAttribute("symbol");
        if (symbol != null && !symbol.isEmpty())
        {
            setSymbol(element.getAttribute("symbol"));
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

    /**
     * This will set the fringe rectangle. Giving the difference between the annotations rectangle and where the drawing
     * occurs.
     *
     * @param fringe the fringe
     */
    public final void setFringe(PDRectangle fringe)
    {
        annot.setItem(COSName.RD, fringe);
    }

    /**
     * This will retrieve the fringe. Giving the difference between the annotations rectangle and where the drawing
     * occurs.
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
     * This will set the symbol that shall be associated with the caret.
     * 
     * @param symbol the symbol
     */
    public final void setSymbol(String symbol)
    {
        String newSymbol = "None";
        if ("paragraph".equals(symbol))
        {
            newSymbol = "P";
        }
        annot.setString(COSName.SY, newSymbol);
    }

    /**
     * This will retrieve the symbol that shall be associated with the caret.
     * 
     * @return the symbol
     */
    public String getSymbol()
    {
        return annot.getString(COSName.SY);
    }
}
