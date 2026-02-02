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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents a Polygon FDF annotation.
 */
public class FDFAnnotationLink extends FDFAnnotation
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotationLink.class);

    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Link";

    /**
     * Default constructor.
     */
    public FDFAnnotationLink()
    {
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationLink(COSDictionary a)
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
    public FDFAnnotationLink(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);
        XPath xpath = XPathFactory.newInstance().newXPath();

        try
        {
            NodeList uri = (NodeList) xpath.evaluate("OnActivation/Action/URI", element,
                    XPathConstants.NODESET);
            if (uri.getLength() > 0)
            {
                Node namedItem = uri.item(0).getAttributes().getNamedItem("Name");
                if (namedItem != null && namedItem.getNodeValue() != null)
                {
                    PDActionURI actionURI = new PDActionURI();
                    actionURI.setURI(namedItem.getNodeValue());
                    annot.setItem(COSName.A, actionURI);
                }
            }
            // GoTo is more tricky, because because page destination needs page tree
            // to convert number into PDPage object
        }
        catch (XPathExpressionException e)
        {
            LOG.debug("Error while evaluating XPath expression", e);
        }
    }
}
