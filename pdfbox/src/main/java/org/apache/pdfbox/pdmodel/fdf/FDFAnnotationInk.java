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
import java.util.ArrayList;
import java.util.List;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents a Ink FDF annotation.
 *
 * @author Ben Litchfield
 */
public class FDFAnnotationInk extends FDFAnnotation
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotationInk.class);
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Ink";

    /**
     * Default constructor.
     */
    public FDFAnnotationInk()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationInk(COSDictionary a)
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
    public FDFAnnotationInk(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            NodeList gestures = (NodeList) xpath.evaluate("inklist/gesture", element,
                    XPathConstants.NODESET);
            if (gestures.getLength() == 0)
            {
                throw new IOException("Error: missing element 'gesture'");
            }
            List<float[]> inklist = new ArrayList<>();
            for (int i = 0; i < gestures.getLength(); i++)
            {
                Node node = gestures.item(i);
                if (node instanceof Element)
                {
                    String gesture = node.getFirstChild().getNodeValue();
                    String[] gestureValues = gesture.split(",|;");
                    float[] values = new float[gestureValues.length];
                    for (int j = 0; j < gestureValues.length; j++)
                    {
                        values[j] = Float.parseFloat(gestureValues[j]);
                    }
                    inklist.add(values);
                }
            }
            setInkList(inklist);
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression for inklist gestures");
        }
    }

    /**
     * Set the paths making up the freehand "scribble".
     * 
     * The ink annotation is made up of one ore more disjoint paths. Each array entry is an array representing a stroked
     * path, being a series of alternating horizontal and vertical coordinates in default user space.
     * 
     * @param inklist the List of arrays representing the paths.
     */
    public final void setInkList(List<float[]> inklist)
    {
        COSArray newInklist = new COSArray();
        for (float[] array : inklist)
        {
            COSArray newArray = new COSArray();
            newArray.setFloatArray(array);
            newInklist.add(newArray);
        }
        annot.setItem(COSName.INKLIST, newInklist);
    }

    /**
     * Get the paths making up the freehand "scribble".
     *
     * @see #setInkList(List)
     * @return the List of arrays representing the paths.
     */
    public List<float[]> getInkList()
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.INKLIST);
        if (array != null)
        {
            List<float[]> retval = new ArrayList<>();
            for (COSBase entry : array)
            {
                retval.add(((COSArray) entry).toFloatArray());
            }
            return retval;
        }
        else
        {
            return null; // Should never happen as this is a required item
        }
    }
}
