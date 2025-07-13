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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBoolean;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.util.Hex;
import org.apache.pdfbox.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents a Stamp FDF annotation.
 *
 * @author Ben Litchfield
 * @author Andrew Hung
 */
public class FDFAnnotationStamp extends FDFAnnotation
{
    private static final Logger LOG = LogManager.getLogger(FDFAnnotationStamp.class);

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
            LOG.error(() -> "Error while evaluating XPath expression for appearance: " + e.getMessage(), e);
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
            LOG.debug("Decoded XML:\n====\n{}\n====", () -> new String(decodedAppearanceXML, StandardCharsets.UTF_8));

            Document stampAppearance = XMLUtil
                    .parse(new ByteArrayInputStream(decodedAppearanceXML));

            Element appearanceEl = stampAppearance.getDocumentElement();

            // Is the root node have tag as DICT, error otherwise
            if (!"dict".equalsIgnoreCase(appearanceEl.getNodeName()))
            {
                throw new IOException("Error while reading stamp document, "
                        + "root should be 'dict' and not '" + appearanceEl.getNodeName() + "'");
            }
            LOG.debug("Generate and set the appearance dictionary to the stamp annotation");
            annot.setItem(COSName.AP, parseStampAnnotationAppearanceXML(appearanceEl));
        }
    }

    /**
     * This will create an Appearance dictionary from an appearance XML element.
     *
     * @param appearanceXML The XML element that contains the appearance data.
     */
    private COSDictionary parseStampAnnotationAppearanceXML(Element appearanceXML) throws IOException
    {
        COSDictionary dictionary = new COSDictionary();
        // the N entry is required.
        dictionary.setItem(COSName.N, new COSStream());
        LOG.debug("Build dictionary for Appearance based on the appearanceXML");

        NodeList nodeList = appearanceXML.getChildNodes();
        String parentAttrKey = appearanceXML.getAttribute("KEY");
        LOG.debug("Appearance Root - tag: {}, name: {}, key: {}, children: {}",
                appearanceXML::getTagName, appearanceXML::getNodeName, () -> parentAttrKey,
                nodeList::getLength);

        // Currently only handles Appearance dictionary (AP key on the root)
        if (!"AP".equals(appearanceXML.getAttribute("KEY")))
        {
            LOG.warn("{} => Not handling element: {} with key: {}", () -> parentAttrKey,
                    appearanceXML::getTagName, () -> appearanceXML.getAttribute("KEY"));
            return dictionary;
        }
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;
                String childTagName = child.getTagName();
                if ("STREAM".equalsIgnoreCase(childTagName))
                {
                    LOG.debug("{} => Process {} item in the dictionary after processing the {}",
                            () -> parentAttrKey, () -> child.getAttribute("KEY"), () -> childTagName);
                    dictionary.setItem(child.getAttribute("KEY"), parseStreamElement(child));
                    LOG.debug("{} => Set {}", () -> parentAttrKey, () -> child.getAttribute("KEY"));
                }
                else
                {
                    LOG.warn("{} => Not handling element: {}", parentAttrKey, childTagName);
                }
            }
        }
        return dictionary;
    }

    private COSStream parseStreamElement(Element streamEl) throws IOException
    {
        LOG.debug("Parse {} Stream", () -> streamEl.getAttribute("KEY"));
        COSStream stream = new COSStream();

        NodeList nodeList = streamEl.getChildNodes();
        String parentAttrKey = streamEl.getAttribute("KEY");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;
                String childAttrKey = child.getAttribute("KEY");
                String childAttrVal = child.getAttribute("VAL");
                String childTagName = child.getTagName();
                LOG.debug("{} => reading child: {} with key: {}", parentAttrKey, childTagName, childAttrKey);
                if (childTagName == null)
                {
                    LOG.warn("{} => Not handling child element: null", parentAttrKey);
                    continue;
                }
                switch (childTagName.toUpperCase())
                {
                    case "INT":
                        if (!"Length".equals(childAttrKey))
                        {
                            stream.setInt(COSName.getPDFName(childAttrKey), Integer.parseInt(childAttrVal));
                            LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        }
                        break;
                    case "FIXED":
                        stream.setFloat(COSName.getPDFName(childAttrKey), Float.parseFloat(childAttrVal));
                        LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        break;
                    case "NAME":
                        stream.setName(COSName.getPDFName(childAttrKey), childAttrVal);
                        LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        break;
                    case "BOOL":
                        stream.setBoolean(COSName.getPDFName(childAttrKey), Boolean.parseBoolean(childAttrVal));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrVal);
                        break;
                    case "ARRAY":
                        stream.setItem(COSName.getPDFName(childAttrKey), parseArrayElement(child));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrKey);
                        break;
                    case "DICT":
                        stream.setItem(COSName.getPDFName(childAttrKey), parseDictElement(child));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrKey);
                        break;
                    case "STREAM":
                        stream.setItem(COSName.getPDFName(childAttrKey), parseStreamElement(child));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrKey);
                        break;
                    case "DATA":
                        String childEncodingAttr = child.getAttribute("ENCODING");
                        LOG.debug("{} => Handling DATA with encoding: {}", parentAttrKey, childEncodingAttr);
                        if ("HEX".equals(childEncodingAttr))
                        {
                            try (OutputStream os = stream.createRawOutputStream())
                            {
                                os.write(Hex.decodeHex(child.getTextContent()));
                                LOG.debug("{} => Data was streamed", parentAttrKey);
                            }
                        }
                        else if ("ASCII".equals(childEncodingAttr))
                        {
                            try (OutputStream os = stream.createOutputStream())
                            {
                                String encoding = child.getOwnerDocument().getXmlEncoding();
                                if (encoding == null)
                                {
                                    encoding = child.getOwnerDocument().getInputEncoding();
                                }
                                if (encoding == null)
                                {
                                    encoding = "UTF-8";
                                }
                                os.write(child.getTextContent().getBytes(encoding));
                                LOG.debug("{} => Data was streamed", parentAttrKey);
                            }
                        }
                        else
                        {
                            LOG.warn("{} => Not handling element DATA encoding: {}", parentAttrKey,
                                    childEncodingAttr);
                        }
                        break;
                    default:
                        LOG.warn("{} => Not handling child element: {}", parentAttrKey, childTagName);
                        break;
                }
            }
        }

        return stream;
    }

    private COSArray parseArrayElement(Element arrayEl) throws IOException
    {
        LOG.debug("Parse {} Array", () -> arrayEl.getAttribute("KEY"));
        COSArray array = new COSArray();

        NodeList nodeList = arrayEl.getChildNodes();
        String parentAttrKey = arrayEl.getAttribute("KEY");

        if ("BBox".equals(parentAttrKey) && nodeList.getLength() < 4)
        {
            throw new IOException("BBox does not have enough coordinates, only has: " +
                    nodeList.getLength());
        }
        else if ("Matrix".equals(parentAttrKey) && nodeList.getLength() < 6)
        {
            throw new IOException("Matrix does not have enough coordinates, only has: " + 
                    nodeList.getLength());
        }

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;
                String childAttrKey = child.getAttribute("KEY");
                String childAttrVal = child.getAttribute("VAL");
                String childTagName = child.getTagName();
                LOG.debug("{} => reading child: {} with key: {}", parentAttrKey, childTagName,
                        childAttrKey);
                if (null == childTagName)
                {
                    LOG.warn("{} => Not handling child element: null", parentAttrKey);
                    continue;
                }
                switch (childTagName.toUpperCase())
                {
                    case "INT":
                    case "FIXED":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(COSNumber.get(childAttrVal));
                        break;
                    case "NAME":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(COSName.getPDFName(childAttrVal));
                        break;
                    case "BOOL":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(COSBoolean.getBoolean(Boolean.parseBoolean(childAttrVal)));
                        break;
                    case "DICT":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(parseDictElement(child));
                        break;
                    case "STREAM":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(parseStreamElement(child));
                        break;
                    case "ARRAY":
                        LOG.debug("{} value({}): {}", parentAttrKey, i, childAttrVal);
                        array.add(parseArrayElement(child));
                        break;
                    default:
                        LOG.warn("{} => Not handling child element: {}", parentAttrKey, childTagName);
                        break;
                }
            }
        }

        return array;
    }

    private COSDictionary parseDictElement(Element dictEl) throws IOException
    {
        LOG.debug("Parse {} Dictionary", dictEl.getAttribute("KEY"));
        COSDictionary dict = new COSDictionary();

        NodeList nodeList = dictEl.getChildNodes();
        String parentAttrKey = dictEl.getAttribute("KEY");

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;
                String childAttrKey = child.getAttribute("KEY");
                String childAttrVal = child.getAttribute("VAL");
                String childTagName = child.getTagName();

                if (childTagName == null)
                {
                    LOG.warn("{} => NOT handling child element: null", parentAttrKey);
                    continue;
                }
                switch (childTagName)
                {
                    case "DICT":
                        LOG.debug("{} => Handling DICT element with key: {}", parentAttrKey,
                                childAttrKey);
                        dict.setItem(COSName.getPDFName(childAttrKey), parseDictElement(child));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrKey);
                        break;
                    case "STREAM":
                        LOG.debug("{} => Handling STREAM element with key: {}", parentAttrKey,
                                childAttrKey);
                        dict.setItem(COSName.getPDFName(childAttrKey), parseStreamElement(child));
                        break;
                    case "NAME":
                        LOG.debug("{} => Handling NAME element with key: {}", parentAttrKey,
                                childAttrKey);
                        dict.setName(COSName.getPDFName(childAttrKey), childAttrVal);
                        LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        break;
                    case "INT":
                        dict.setInt(COSName.getPDFName(childAttrKey), Integer.parseInt(childAttrVal));
                        LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        break;
                    case "FIXED":
                        dict.setFloat(COSName.getPDFName(childAttrKey), Float.parseFloat(childAttrVal));
                        LOG.debug("{} => Set {}: {}", parentAttrKey, childAttrKey, childAttrVal);
                        break;
                    case "BOOL":
                        dict.setBoolean(COSName.getPDFName(childAttrKey), Boolean.parseBoolean(childAttrVal));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrVal);
                        break;
                    case "ARRAY":
                        dict.setItem(COSName.getPDFName(childAttrKey), parseArrayElement(child));
                        LOG.debug("{} => Set {}", parentAttrKey, childAttrKey);
                        break;
                    default:
                        LOG.warn("{} => NOT handling child element: {}", parentAttrKey, childTagName);
                        break;
                }
            }
        }

        return dict;
    }
}
