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

package org.apache.xmpbox.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DomHelper
{

    private DomHelper()
    {
    }

    public static Element getUniqueElementChild(Element description) throws XmpParsingException
    {
        NodeList nl = description.getChildNodes();
        int pos = -1;
        for (int i = 0; i < nl.getLength(); i++)
        {
            if (nl.item(i) instanceof Element)
            {
                if (pos >= 0)
                {
                    // invalid : found two child elements
                    throw new XmpParsingException(ErrorType.Undefined, "Found two child elements in " + description);
                }
                else
                {
                    pos = i;
                }
            }
        }
        return (Element) nl.item(pos);
    }

    /**
     * Return the first child element of the element parameter. If there is no child, null is returned
     * 
     * @param description
     * @return
     * @throws XmpParsingException
     */
    public static Element getFirstChildElement(Element description) throws XmpParsingException
    {
        NodeList nl = description.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++)
        {
            if (nl.item(i) instanceof Element)
            {
                return (Element) nl.item(i);
            }
        }
        return null;
    }

    public static List<Element> getElementChildren(Element description) throws XmpParsingException
    {
        NodeList nl = description.getChildNodes();
        List<Element> ret = new ArrayList<Element>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++)
        {
            if (nl.item(i) instanceof Element)
            {
                ret.add((Element) nl.item(i));
            }
        }
        return ret;
    }

    public static QName getQName(Element element)
    {
        return new QName(element.getNamespaceURI(), element.getLocalName(), element.getPrefix());
    }

    public static boolean isRdfDescription(Element element)
    {
        return (XmpConstants.DEFAULT_RDF_PREFIX.equals(element.getPrefix()) && XmpConstants.DESCRIPTION_NAME
                .equals(element.getLocalName()));
    }

    public static boolean isParseTypeResource(Element element)
    {
        Attr parseType = element.getAttributeNodeNS(XmpConstants.RDF_NAMESPACE, XmpConstants.PARSE_TYPE);
        if (parseType != null && XmpConstants.RESOURCE_NAME.equals(parseType.getValue()))
        {
            // parseType resourc
            return true;
        }
        // else
        return false;
    }

}
