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
package org.apache.jempbox.xmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jempbox.impl.DateConverter;
import org.apache.jempbox.impl.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a metadata schema that can be stored in an XMP
 * document. It handles all generic properties that are available. See
 * subclasses for access to specific properties.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class XMPSchema
{
    /**
     * The standard xmlns namespace.
     */
    public static final String NS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    /**
     * The XML schema prefix.
     */
    protected String prefix;

    /**
     * The DOM representation of this object.
     */
    protected Element schema = null;

    /**
     * Create a new blank schema that can be populated.
     * 
     * @param parent
     *            The parent XMP document that this schema will be part of.
     * @param namespaceName
     *            The name of the namespace, ie pdf,dc,...
     * @param namespaceURI
     *            The URI of the namespace, ie "http://ns.adobe.com/pdf/1.3/"
     */
    public XMPSchema(XMPMetadata parent, String namespaceName,
            String namespaceURI)
    {
        schema = parent.xmpDocument.createElementNS(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                "rdf:Description");
        prefix = namespaceName;
        schema.setAttributeNS(NS_NAMESPACE, "xmlns:" + namespaceName,
                namespaceURI);
    }

    /**
     * Create schema from an existing XML element.
     * 
     * @param element
     *            The existing XML element.
     * @param aPrefix
     *            The XML prefix.
     */
    public XMPSchema(Element element, String aPrefix)
    {
        schema = element;
        if (aPrefix != null)
        {
            prefix = aPrefix;
        }
        else
        {
            prefix = "";
        }
    }

    /**
     * Get the XML element that is represented by this schema.
     * 
     * @return The root XML element of this schema.
     */
    public Element getElement()
    {
        return schema;
    }

    /**
     * Get the RDF about attribute.
     * 
     * @return The RDF 'about' attribute.
     */
    public String getAbout()
    {
        return getTextProperty("rdf:about");
    }

    /**
     * Set the RDF 'about' attribute. Passing in null will clear this attribute.
     * 
     * @param about
     *            The new RFD about value.
     */
    public void setAbout(String about)
    {
        if (about == null)
        {
            schema.removeAttribute("rdf:about");
        }
        else
        {
            schema.setAttribute("rdf:about", about);
        }
    }

    /**
     * Set a simple text property on the schema.
     * 
     * @param propertyName
     *            The name of the property, it must contain the namespace
     *            prefix, ie "pdf:Keywords"
     * @param propertyValue
     *            The value for the property, can be any string. Passing null
     *            will remove the property.
     */
    public void setTextProperty(String propertyName, String propertyValue)
    {
        if (propertyValue == null)
        {
            schema.removeAttribute(propertyName);
            NodeList keywordList = schema.getElementsByTagName(propertyName);
            for (int i = 0; i < keywordList.getLength(); i++)
            {
                schema.removeChild(keywordList.item(i));
            }

        }
        else
        {
            if (schema.hasAttribute(propertyName))
            {
                schema.setAttribute(propertyName, propertyValue);
            }
            else
            {
                if (schema.hasChildNodes())
                {
                    NodeList nodeList = schema
                            .getElementsByTagName(propertyName);
                    if (nodeList.getLength() > 0)
                    {
                        Element node = (Element) nodeList.item(0);
                        node.setNodeValue(propertyValue);
                    }
                    else
                    {
                        Element textNode = schema.getOwnerDocument()
                                .createElement(propertyName);
                        XMLUtil.setStringValue(textNode, propertyValue);
                        schema.appendChild(textNode);
                    }
                }
                else
                {
                    schema.setAttribute(propertyName, propertyValue);
                }
            }
        }
    }

    /**
     * Get the value of a simple text property.
     * 
     * @param propertyName
     *            The name of the property to get, it must include the namespace
     *            prefix. ie "pdf:Keywords".
     * 
     * @return The value of the text property or the null if there is no value.
     */
    public String getTextProperty(String propertyName)
    {
        // propertyValue == null does not work, since getAttribute returns the
        // empty string if the attribute is not found

        if (schema.hasAttribute(propertyName))
        {
            return schema.getAttribute(propertyName);
        }
        else
        {
            NodeList nodes = schema.getElementsByTagName(propertyName);
            if (nodes.getLength() > 0)
            {
                Element node = (Element) nodes.item(0);
                return XMLUtil.getStringValue(node);
            }
            return null;
        }
    }

    /**
     * Get the value of the property as a date.
     * 
     * @param propertyName
     *            The fully qualified property name for the date.
     * 
     * @return The value of the property as a date.
     * 
     * @throws IOException
     *             If there is an error converting the value to a date.
     */
    public Calendar getDateProperty(String propertyName) throws IOException
    {
        return DateConverter.toCalendar(getTextProperty(propertyName));
    }

    /**
     * Set the value of the property as a date.
     * 
     * @param propertyName
     *            The fully qualified property name for the date.
     * @param date
     *            The date to set, or null to clear.
     */
    public void setDateProperty(String propertyName, Calendar date)
    {
        setTextProperty(propertyName, DateConverter.toISO8601(date));
    }

    /**
     * Get the value of the property as a boolean.
     * 
     * @param propertyName
     *            The fully qualified property name for the boolean.
     * 
     * @return The value of the property as a boolean.
     */
    public Boolean getBooleanProperty(String propertyName)
    {
        Boolean value = null;
        String stringValue = getTextProperty(propertyName);
        if (stringValue != null)
        {
            value = stringValue.equals("True") ? Boolean.TRUE : Boolean.FALSE;
        }
        return value;
    }

    /**
     * Set the value of the property as a boolean.
     * 
     * @param propertyName
     *            The fully qualified property name for the boolean.
     * @param bool
     *            The boolean to set, or null to clear.
     */
    public void setBooleanProperty(String propertyName, Boolean bool)
    {
        String value = null;
        if (bool != null)
        {
            value = bool.booleanValue() ? "True" : "False";
        }
        setTextProperty(propertyName, value);
    }

    /**
     * Get the value of the property as an integer.
     * 
     * @param propertyName
     *            The fully qualified property name for the integer.
     * 
     * @return The value of the property as an integer.
     */
    public Integer getIntegerProperty(String propertyName)
    {
        Integer retval = null;
        String intProperty = getTextProperty(propertyName);
        if (intProperty != null && intProperty.length() > 0)
        {
            retval = new Integer(intProperty);
        }
        return retval;
    }

    /**
     * Set the value of the property as an integer.
     * 
     * @param propertyName
     *            The fully qualified property name for the integer.
     * @param intValue
     *            The int to set, or null to clear.
     */
    public void setIntegerProperty(String propertyName, Integer intValue)
    {
        String textValue = null;
        if (intValue != null)
        {
            textValue = intValue.toString();
        }
        setTextProperty(propertyName, textValue);
    }

    /**
     * Remove all matching entries with the given value from the bag.
     * 
     * @param bagName
     *            The name of the bag, it must include the namespace prefix. ie
     *            "pdf:Keywords".
     * @param bagValue
     *            The value to remove from the bagList.
     */
    public void removeBagValue(String bagName, String bagValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(bagName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Bag");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
                NodeList items = bagElement.getElementsByTagName("rdf:li");
                for (int i = items.getLength() - 1; i >= 0; i--)
                {
                    Element li = (Element) items.item(i);
                    String value = XMLUtil.getStringValue(li);
                    if (value.equals(bagValue))
                    {
                        bagElement.removeChild(li);
                    }
                }
            }
        }
    }

    /**
     * Add an entry to a bag property.
     * 
     * @param bagName
     *            The name of the bag, it must include the namespace prefix. ie
     *            "pdf:Keywords".
     * @param bagValue
     *            The value to add to the bagList.
     */
    public void addBagValue(String bagName, String bagValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(bagName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Bag");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
            }
        }
        else
        {
            Element contElement = schema.getOwnerDocument().createElement(
                    bagName);
            schema.appendChild(contElement);
            bagElement = schema.getOwnerDocument().createElement("rdf:Bag");
            contElement.appendChild(bagElement);
        }
        Element liElement = schema.getOwnerDocument().createElement("rdf:li");
        XMLUtil.setStringValue(liElement, bagValue);
        bagElement.appendChild(liElement);
    }

    /**
     * Get all the values of the bag property. This will return a list of
     * java.lang.String objects, this is a read-only list.
     * 
     * @param bagName
     *            The name of the bag property to get, it must include the
     *            namespace prefix. ie "pdf:Keywords"
     * 
     * @return All of the values of the bag property in a list.
     */
    public List<String> getBagList(String bagName)
    {
        List<String> retval = null;
        NodeList nodes = schema.getElementsByTagName(bagName);
        if (nodes.getLength() > 0)
        {
            Element contributor = (Element) nodes.item(0);
            NodeList bagList = contributor.getElementsByTagName("rdf:Bag");
            if (bagList.getLength() > 0)
            {
                Element bag = (Element) bagList.item(0);
                retval = new ArrayList<String>();
                NodeList items = bag.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength(); i++)
                {
                    Element li = (Element) items.item(i);
                    retval.add(XMLUtil.getStringValue(li));
                }
                retval = Collections.unmodifiableList(retval);
            }
        }

        return retval;
    }

    /**
     * Remove all matching values from a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property. It must include the
     *            namespace prefix. ie "pdf:Keywords".
     * @param seqValue
     *            The value to remove from the list.
     */
    public void removeSequenceValue(String seqName, String seqValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
                NodeList items = bagElement.getElementsByTagName("rdf:li");
                for (int i = items.getLength() - 1; i >= 0; i--)
                {
                    Element li = (Element) items.item(i);
                    String value = XMLUtil.getStringValue(li);
                    if (value.equals(seqValue))
                    {
                        bagElement.removeChild(li);
                    }
                }
            }
        }
    }

    /**
     * Remove a value from a sequence property. This will remove all entries
     * from the list.
     * 
     * @param seqName
     *            The name of the sequence property. It must include the
     *            namespace prefix. ie "pdf:Keywords".
     * @param seqValue
     *            The value to remove from the list.
     */
    public void removeSequenceValue(String seqName, Elementable seqValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
                NodeList items = bagElement.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength(); i++)
                {
                    Element li = (Element) items.item(i);
                    if (li == seqValue.getElement())
                    {
                        bagElement.removeChild(li);
                    }
                }
            }
        }
    }

    /**
     * Add a new value to a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords"
     * @param seqValue
     *            The value to add to the sequence.
     */
    public void addSequenceValue(String seqName, String seqValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
            }
            else
            {
                // xml is crap discard it
                schema.removeChild(nodes.item(0));
            }
        }
        if (bagElement == null)
        {
            Element contElement = schema.getOwnerDocument().createElement(
                    seqName);
            schema.appendChild(contElement);
            bagElement = schema.getOwnerDocument().createElement("rdf:Seq");
            contElement.appendChild(bagElement);
        }
        Element liElement = schema.getOwnerDocument().createElement("rdf:li");
        liElement.appendChild(schema.getOwnerDocument()
                .createTextNode(seqValue));
        bagElement.appendChild(liElement);
    }

    /**
     * Add a new value to a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords"
     * @param seqValue
     *            The value to add to the sequence.
     */
    public void addSequenceValue(String seqName, Elementable seqValue)
    {
        Element bagElement = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contElement = (Element) nodes.item(0);
            NodeList bagList = contElement.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                bagElement = (Element) bagList.item(0);
            }
        }
        else
        {
            Element contElement = schema.getOwnerDocument().createElement(
                    seqName);
            schema.appendChild(contElement);
            bagElement = schema.getOwnerDocument().createElement("rdf:Seq");
            contElement.appendChild(bagElement);
        }
        bagElement.appendChild(seqValue.getElement());
    }

    /**
     * Get all the values in a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords".
     * 
     * @return A read-only list of java.lang.String objects or null if the
     *         property does not exist.
     */
    public List<String> getSequenceList(String seqName)
    {
        List<String> retval = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contributor = (Element) nodes.item(0);
            NodeList bagList = contributor.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                Element bag = (Element) bagList.item(0);
                retval = new ArrayList<String>();
                NodeList items = bag.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength(); i++)
                {
                    Element li = (Element) items.item(i);
                    retval.add(XMLUtil.getStringValue(li));
                }
                retval = Collections.unmodifiableList(retval);
            }
        }

        return retval;
    }

    /**
     * Get a list of ResourceEvent objects.
     * 
     * @param seqName
     *            The name of the sequence to retrieve.
     * 
     * @return A list of ResourceEvent objects or null if they do not exist.
     */
    public List<ResourceEvent> getEventSequenceList(String seqName)
    {
        List<ResourceEvent> retval = null;
        NodeList nodes = schema.getElementsByTagName(seqName);
        if (nodes.getLength() > 0)
        {
            Element contributor = (Element) nodes.item(0);
            NodeList bagList = contributor.getElementsByTagName("rdf:Seq");
            if (bagList.getLength() > 0)
            {
                Element bag = (Element) bagList.item(0);
                retval = new ArrayList<ResourceEvent>();
                NodeList items = bag.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength(); i++)
                {
                    Element li = (Element) items.item(i);
                    retval.add(new ResourceEvent(li));
                }
                retval = Collections.unmodifiableList(retval);
            }
        }

        return retval;
    }

    /**
     * Remove a date sequence value from the list.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords"
     * @param date
     *            The date to remove from the sequence property.
     */
    public void removeSequenceDateValue(String seqName, Calendar date)
    {
        String dateAsString = DateConverter.toISO8601(date);
        removeSequenceValue(seqName, dateAsString);
    }

    /**
     * Add a date sequence value to the list.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords"
     * @param date
     *            The date to add to the sequence property.
     */
    public void addSequenceDateValue(String seqName, Calendar date)
    {
        String dateAsString = DateConverter.toISO8601(date);
        addSequenceValue(seqName, dateAsString);
    }

    /**
     * Get all the date values in a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the
     *            namespace prefix. ie "pdf:Keywords".
     * 
     * @return A read-only list of java.util.Calendar objects or null if the
     *         property does not exist.
     * 
     * @throws IOException
     *             If there is an error converting the value to a date.
     */
    public List<Calendar> getSequenceDateList(String seqName) throws IOException
    {
        List<String> strings = getSequenceList(seqName);
        List<Calendar> retval = null;
        if (strings != null)
        {
            retval = new ArrayList<Calendar>();
            for (int i = 0; i < strings.size(); i++)
            {
                retval.add(DateConverter.toCalendar(strings.get(i)));
            }
        }
        return retval;
    }

    /**
     * Set the value of a multi-lingual property.
     * 
     * @param propertyName
     *            The name of the property, it must include the namespace
     *            prefix. ie "pdf:Keywords"
     * @param language
     *            The language code of the value. If null then "x-default" is
     *            assumed.
     * @param value
     *            The value of the property in the specified language.
     */
    public void setLanguageProperty(String propertyName, String language,
            String value)
    {
        NodeList nodes = schema.getElementsByTagName(propertyName);
        Element property = null;
        if (nodes.getLength() == 0)
        {
            if (value == null)
            {
                // value is null, it doesn't already exist so there
                // is nothing to do.
                return;
            }
            property = schema.getOwnerDocument().createElement(propertyName);
            schema.appendChild(property);
        }
        else
        {
            property = (Element) nodes.item(0);
        }
        Element alt = null;
        NodeList altList = property.getElementsByTagName("rdf:Alt");
        if (altList.getLength() == 0)
        {
            if (value == null)
            {
                // value is null, it doesn't already exist so there
                // is nothing to do.
                return;
            }
            alt = schema.getOwnerDocument().createElement("rdf:Alt");
            property.appendChild(alt);
        }
        else
        {
            alt = (Element) altList.item(0);
        }
        NodeList items = alt.getElementsByTagName("rdf:li");
        if (language == null)
        {
            language = "x-default";
        }
        boolean foundValue = false;
        for (int i = 0; i < items.getLength(); i++)
        {
            Element li = (Element) items.item(i);
            if (value == null)
            {
                alt.removeChild(li);
            }
            else if (language.equals(li.getAttribute("xml:lang")))
            {
                foundValue = true;
                XMLUtil.setStringValue(li, value);
            }
        }
        if (value != null && !foundValue)
        {
            Element li = schema.getOwnerDocument().createElement("rdf:li");
            li.setAttribute("xml:lang", language);
            XMLUtil.setStringValue(li, value);
            if (language.equals("x-default"))
            {
                // default should be first element, see XMP spec
                alt.insertBefore(li, alt.getFirstChild());
            }
            else
            {
                alt.appendChild(li);
            }

        }
    }

    /**
     * Get the value of a multi-lingual property.
     * 
     * @param propertyName
     *            The name of the property, it must include the namespace
     *            prefix. ie "pdf:Keywords"
     * @param language
     *            The language code of the value. If null then "x-default" is
     *            assumed.
     * 
     * @return The value of the language property.
     */
    public String getLanguageProperty(String propertyName, String language)
    {
        String retval = null;
        if (language == null)
        {
            language = "x-default";
        }

        NodeList nodes = schema.getElementsByTagName(propertyName);
        if (nodes.getLength() > 0)
        {
            Element property = (Element) nodes.item(0);
            NodeList altList = property.getElementsByTagName("rdf:Alt");
            if (altList.getLength() > 0)
            {
                Element alt = (Element) altList.item(0);
                NodeList items = alt.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength() && retval == null; i++)
                {
                    Element li = (Element) items.item(i);
                    String elementLanguage = li.getAttribute("xml:lang");
                    if (language.equals(elementLanguage))
                    {
                        retval = XMLUtil.getStringValue(li);
                    }
                }
            }
            else if (property.getChildNodes().getLength() == 1 && Node.TEXT_NODE == property.getFirstChild().getNodeType())
            {
                retval = property.getFirstChild().getNodeValue();
            }
        }
        return retval;
    }

    /**
     * Set the value of a multi-lingual property.
     * 
     * @param propertyName
     *            The name of the property, it must include the namespace
     *            prefix. ie "pdf:Keywords"
     * @param language
     *            The language code of the value. If null then "x-default" is
     *            assumed.
     * @param value
     *            The value of the property in the specified language.
     */
    public void setThumbnailProperty(String propertyName, String language,
            Thumbnail value)
    {
        NodeList nodes = schema.getElementsByTagName(propertyName);
        Element property = null;
        if (nodes.getLength() == 0)
        {
            if (value == null)
            {
                // value is null, it doesn't already exist so there
                // is nothing to do.
                return;
            }
            property = schema.getOwnerDocument().createElement(propertyName);
            schema.appendChild(property);
        }
        else
        {
            property = (Element) nodes.item(0);
        }
        Element alt = null;
        NodeList altList = property.getElementsByTagName("rdf:Alt");
        if (altList.getLength() == 0)
        {
            if (value == null)
            {
                // value is null, it doesn't already exist so there
                // is nothing to do.
                return;
            }
            alt = schema.getOwnerDocument().createElement("rdf:Alt");
            property.appendChild(alt);
        }
        else
        {
            alt = (Element) altList.item(0);
        }
        NodeList items = alt.getElementsByTagName("rdf:li");
        if (language == null)
        {
            language = "x-default";
        }
        boolean foundValue = false;
        for (int i = 0; i < items.getLength(); i++)
        {
            Element li = (Element) items.item(i);
            if (value == null)
            {
                alt.removeChild(li);
            }
            else if (language.equals(li.getAttribute("xml:lang")))
            {
                foundValue = true;
                alt.replaceChild(li, value.getElement());
            }
        }
        if (value != null && !foundValue)
        {
            Element li = value.getElement();
            li.setAttribute("xml:lang", language);
            if (language.equals("x-default"))
            {
                // default should be first element, see XMP spec
                alt.insertBefore(li, alt.getFirstChild());
            }
            else
            {
                alt.appendChild(li);
            }

        }
    }

    /**
     * Get the value of a multi-lingual property.
     * 
     * @param propertyName
     *            The name of the property, it must include the namespace
     *            prefix. ie "pdf:Keywords"
     * @param language
     *            The language code of the value. If null then "x-default" is
     *            assumed.
     * 
     * @return The value of the language property.
     */
    public Thumbnail getThumbnailProperty(String propertyName, String language)
    {
        Thumbnail retval = null;
        if (language == null)
        {
            language = "x-default";
        }

        NodeList nodes = schema.getElementsByTagName(propertyName);
        if (nodes.getLength() > 0)
        {
            Element property = (Element) nodes.item(0);
            NodeList altList = property.getElementsByTagName("rdf:Alt");
            if (altList.getLength() > 0)
            {
                Element alt = (Element) altList.item(0);
                NodeList items = alt.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength() && retval == null; i++)
                {
                    Element li = (Element) items.item(i);
                    String elementLanguage = li.getAttribute("xml:lang");
                    if (language.equals(elementLanguage))
                    {
                        retval = new Thumbnail(li);
                    }
                }
            }
        }
        return retval;
    }

    /**
     * Get a list of all languages that are currently defined for a specific
     * property.
     * 
     * @param propertyName
     *            The name of the property, it must include the namespace
     *            prefix. ie "pdf:Keywords"
     * 
     * @return A list of all languages, this will return an non-null empty list
     *         if none have been defined.
     */
    public List<String> getLanguagePropertyLanguages(String propertyName)
    {
        List<String> retval = new ArrayList<String>();

        NodeList nodes = schema.getElementsByTagName(propertyName);
        if (nodes.getLength() > 0)
        {
            Element property = (Element) nodes.item(0);
            NodeList altList = property.getElementsByTagName("rdf:Alt");
            if (altList.getLength() > 0)
            {
                Element alt = (Element) altList.item(0);
                NodeList items = alt.getElementsByTagName("rdf:li");
                for (int i = 0; i < items.getLength(); i++)
                {
                    Element li = (Element) items.item(i);
                    String elementLanguage = li.getAttribute("xml:lang");
                    if (elementLanguage == null)
                    {
                        retval.add("x-default");
                    }
                    else
                    {
                        retval.add(elementLanguage);
                    }
                }
            }
        }
        return retval;
    }

    /**
     * A basic schema merge, it merges bags and sequences and replace everything
     * else.
     * 
     * @param xmpSchema The schema to merge.
     * @throws IOException If there is an error during the merge.
     */
    public void merge(XMPSchema xmpSchema) throws IOException
    {
        if (!xmpSchema.getClass().equals(this.getClass()))
        {
            throw new IOException("Can only merge schemas of the same type.");
        }

        NamedNodeMap attributes = xmpSchema.getElement().getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node a = attributes.item(i);
            String name = a.getNodeName();
            if (name.startsWith(prefix))
            {
                String newValue = xmpSchema.getTextProperty(name);
                setTextProperty(name, newValue);
            }
        }
        NodeList nodes = xmpSchema.getElement().getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node a = nodes.item(i);
            String name = a.getNodeName();
            if (name.startsWith(prefix))
            {
                if (a instanceof Element)
                {
                    Element e = (Element) a;
                    if (nodes.getLength() > 0)
                    {
                        NodeList seqList = e.getElementsByTagName("rdf:Seq");
                        if (seqList.getLength() > 0)
                        {
                            List<String> newList = xmpSchema.getSequenceList(name);
                            List<String> oldList = getSequenceList(name);

                            Iterator<String> it = newList.iterator();

                            while (it.hasNext())
                            {
                                String object = it.next();
                                if (oldList == null
                                        || !oldList.contains(object))
                                {
                                    addSequenceValue(name, object);
                                }
                            }
                            continue;
                        }
                        NodeList bagList = e.getElementsByTagName("rdf:Bag");
                        if (bagList.getLength() > 0)
                        {
                            List<String> newList = xmpSchema.getBagList(name);
                            List<String> oldList = getBagList(name);

                            Iterator<String> it = newList.iterator();

                            while (it.hasNext())
                            {
                                String object = it.next();
                                if (oldList == null
                                        || !oldList.contains(object))
                                {
                                    addBagValue(name, object);
                                }
                            }
                            continue;
                        }
                    }
                }
                String newValue = xmpSchema.getTextProperty(name);
                setTextProperty(name, newValue);
            }
        }
    }
}