/*****************************************************************************
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 ****************************************************************************/
package org.apache.pdfbox.preflight.parser;


import org.apache.pdfbox.preflight.ValidationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class TestXmlResultParser
{

    public static final String ERROR_CODE = "000";

    protected XmlResultParser parser = new XmlResultParser();

    protected Document document;

    protected Element preflight;

    protected XPath xpath;

    @Before
    public void before() throws Exception
    {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        preflight = parser.generateResponseSkeleton(document, "myname", 14);
        xpath = XPathFactory.newInstance().newXPath();
    }

    @Test
    public void testOneError() throws Exception
    {
        ValidationResult result = new ValidationResult(false);
        result.addError(new ValidationResult.ValidationError("7"));
        parser.createResponseWithError(document, "pdftype", result, preflight);
        Assert.assertNotNull(xpath.evaluate("errors[@count='1']", preflight, XPathConstants.NODE));
        NodeList nl = (NodeList)xpath.evaluate("errors/error[@count='1']", preflight, XPathConstants.NODESET);
        Assert.assertEquals(1,nl.getLength());
    }

    @Test
    public void testTwoError() throws Exception
    {
        ValidationResult result = new ValidationResult(false);
        result.addError(new ValidationResult.ValidationError("7"));
        result.addError(new ValidationResult.ValidationError(ERROR_CODE));
        parser.createResponseWithError(document, "pdftype", result, preflight);
        Assert.assertNotNull(xpath.evaluate("errors[@count='2']", preflight, XPathConstants.NODE));
        NodeList nl = (NodeList)xpath.evaluate("errors/error[@count='1']", preflight, XPathConstants.NODESET);
        Assert.assertEquals(2,nl.getLength());
    }

    @Test
    public void testSameErrorTwice() throws Exception
    {
        ValidationResult result = new ValidationResult(false);
        result.addError(new ValidationResult.ValidationError(ERROR_CODE));
        result.addError(new ValidationResult.ValidationError(ERROR_CODE));
        parser.createResponseWithError(document,"pdftype",result,preflight);
        Assert.assertNotNull(xpath.evaluate("errors[@count='2']", preflight, XPathConstants.NODE));
        Assert.assertNotNull(xpath.evaluate("errors/error[@count='2']", preflight, XPathConstants.NODE));
        Element code = (Element)xpath.evaluate("errors/error[@count='2']/code", preflight, XPathConstants.NODE);
        Assert.assertNotNull(code);
        Assert.assertEquals(ERROR_CODE,code.getTextContent());
    }

    @Test
    public void testSameCodeWithDifferentMessages() throws Exception
    {
        ValidationResult result = new ValidationResult(false);
        result.addError(new ValidationResult.ValidationError(ERROR_CODE,"message 1"));
        result.addError(new ValidationResult.ValidationError(ERROR_CODE,"message 2"));
        parser.createResponseWithError(document, "pdftype", result, preflight);
        Assert.assertNotNull(xpath.evaluate("errors[@count='2']", preflight, XPathConstants.NODE));
        NodeList nl = (NodeList)xpath.evaluate("errors/error[@count='1']", preflight, XPathConstants.NODESET);
        Assert.assertEquals(2,nl.getLength());
    }


//    private void dump (Element element) throws Exception {
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        transformer.transform(new DOMSource(element), new StreamResult(System.out));
//        System.out.flush();
//    }

}
