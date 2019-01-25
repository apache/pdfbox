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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.util.Hex;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This represents a Stamp FDF annotation.
 *
 * @author Ben Litchfield
 * @author Andrew Hung
 */
public class FDFAnnotationStamp extends FDFAnnotation
{
    private static final Log LOG = LogFactory.getLog(FDFAnnotationStamp.class);

    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Stamp";

    /**
     * Default constructor.
     */
    public FDFAnnotationStamp()
    {
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationStamp(COSDictionary a)
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
    public FDFAnnotationStamp(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        // PDFBOX-4437: Initialize the Stamp appearance from the XFDF
        // https://www.immagic.com/eLibrary/ARCHIVES/TECH/ADOBE/A070914X.pdf
        // appearance is only defined for stamps
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Set the Appearance to the annotation
        LOG.debug("Get the DOM Document for the stamp appearance");
        String base64EncodedAppearance;
        try
        {
            base64EncodedAppearance = xpath.evaluate("appearance", element);
        }
        catch (XPathExpressionException e)
        {
            // should not happen
            LOG.error("Error while evaluating XPath expression for appearance: " + e);
            return;
        }
        byte[] decodedAppearanceXML;
        try
        {
            decodedAppearanceXML = Hex.decodeBase64(base64EncodedAppearance);
        }
        catch (IllegalArgumentException ex)
        {
            LOG.error("Bad base64 encoded appearance ignored", ex);
            return;
        }
        if (base64EncodedAppearance != null && !base64EncodedAppearance.isEmpty())
        {
            Document stampAppearance = getStampAppearanceDocument(decodedAppearanceXML);

            Element appearanceEl = stampAppearance.getDocumentElement();

            // Is the root node have tag as DICT, error otherwise
            if (!"dict".equalsIgnoreCase(appearanceEl.getNodeName()))
            {
                throw new IOException("Error while reading stamp document, "
                        + "root should be 'dict' and not '" + appearanceEl.getNodeName() + "'");
            }
            LOG.debug("Generate and set the appearance dictionary to the stamp annotation");
            annot.setItem(COSName.AP, new FDFStampAnnotationAppearance(appearanceEl));
        }
    }

    /**
     * Parse the <param>xmlString</param> to DOM Document tree from XML content
     */
    private Document getStampAppearanceDocument(byte[] xml) throws IOException
    {
        try
        {
            // Obtain DOM Document instance and create DocumentBuilder with default configuration
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // Parse the content to Document object
            return builder.parse(new ByteArrayInputStream(xml));
        }
        catch (ParserConfigurationException ex)
        {
            LOG.error("Error while converting appearance xml to document: " + ex);
            throw new IOException(ex);
        }
        catch (SAXException ex)
        {
            LOG.error("Error while converting appearance xml to document: " + ex);
            throw new IOException(ex);
        }
    }    
}
