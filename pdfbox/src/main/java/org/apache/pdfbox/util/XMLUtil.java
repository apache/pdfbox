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
package org.apache.pdfbox.util;

import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class with handle some simple XML operations.
 *
 * @author Ben Litchfield
 */
public final class XMLUtil
{
    /**
     * Utility class, should not be instantiated.
     *
     */
    private XMLUtil()
    {
    }

    /**
     * This will parse an XML stream and create a DOM document.
     *
     * @param is The stream to get the XML from.
     * @return The DOM document.
     * @throws IOException It there is an error creating the dom.
     */
    public static Document parse(final InputStream is) throws IOException
    {
        return parse(is, false);
    }

    /**
     * This will parse an XML stream and create a DOM document.
     *
     * @param is The stream to get the XML from.
     * @param nsAware activates namespace awareness of the parser
     * @return The DOM document.
     * @throws IOException It there is an error creating the dom.
     */
    public static Document parse(final InputStream is, final boolean nsAware) throws IOException
    {
        try
        {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities",
                    false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities",
                    false);
            builderFactory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builderFactory.setXIncludeAware(false);
            builderFactory.setExpandEntityReferences(false);
            builderFactory.setNamespaceAware(nsAware);
            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            return builder.parse(is);
        }
        catch (final FactoryConfigurationError | ParserConfigurationException | SAXException e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * This will get the text value of an element.
     *
     * @param node The node to get the text value for.
     * @return The text of the node.
     */
    public static String getNodeValue(final Element node)
    {
        final StringBuilder sb = new StringBuilder();
        final NodeList children = node.getChildNodes();
        final int numNodes = children.getLength();
        for (int i = 0; i < numNodes; i++)
        {
            final Node next = children.item(i);
            if (next instanceof Text)
            {
                sb.append(next.getNodeValue());
            }
        }
        return sb.toString();
    }

}
