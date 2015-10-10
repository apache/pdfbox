/*****************************************************************************
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.AbstractComplexProperty;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.AbstractSimpleProperty;
import org.apache.xmpbox.type.AbstractStructuredType;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class XmpSerializer
{

    private DocumentBuilder documentBuilder = null;

    private boolean parseTypeResourceForLi = true;

    public XmpSerializer()
    {
        // xml init
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try
        {
            documentBuilder = builderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            // never happens, because we don't call builderFactory#setAttribute
            throw new RuntimeException(e);
        }
    }

    public void serialize(XMPMetadata metadata, OutputStream os, boolean withXpacket) throws TransformerException
    {
        Document doc = documentBuilder.newDocument();
        // fill document
        Element rdf = createRdfElement(doc, metadata, withXpacket);
        for (XMPSchema schema : metadata.getAllSchemas())
        {
            rdf.appendChild(serializeSchema(doc, schema));
        }
        // save
        save(doc, os, "UTF-8");
    }

    protected Element serializeSchema(Document doc, XMPSchema schema)
    {
        // prepare schema
        Element selem = doc.createElementNS(XmpConstants.RDF_NAMESPACE, "rdf:Description");
        selem.setAttributeNS(XmpConstants.RDF_NAMESPACE, "rdf:about", schema.getAboutValue());
        selem.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + schema.getPrefix(), schema.getNamespace());
        // the other attributes
        fillElementWithAttributes(selem, schema);
        // the content
        List<AbstractField> fields = schema.getAllProperties();
        serializeFields(doc, selem, fields,schema.getPrefix(), null, true);
        // return created schema
        return selem;
    }

    public void serializeFields(Document doc, Element parent, List<AbstractField> fields, String resourceNS, String prefix, boolean wrapWithProperty)
    {
        for (AbstractField field : fields)
        {

            if (field instanceof AbstractSimpleProperty)
            {
                AbstractSimpleProperty simple = (AbstractSimpleProperty) field;
                
                String localPrefix;
                
                if (prefix != null && !prefix.isEmpty())
                {
                    localPrefix = prefix;
                }
                else
                {
                    localPrefix = simple.getPrefix();
                }
                
                Element esimple = doc.createElement(localPrefix + ":" + simple.getPropertyName());
                esimple.setTextContent(simple.getStringValue());
                List<Attribute> attributes = simple.getAllAttributes();
                for (Attribute attribute : attributes)
                {
                    esimple.setAttributeNS(attribute.getNamespace(), attribute.getName(), attribute.getValue());
                }
                parent.appendChild(esimple);
            }
            else if (field instanceof ArrayProperty)
            {
                ArrayProperty array = (ArrayProperty) field;
                // property
                Element asimple = doc.createElement(array.getPrefix() + ":" + array.getPropertyName());
                parent.appendChild(asimple);
                // attributes
                fillElementWithAttributes(asimple, array);
                // the array definition
                Element econtainer = doc.createElement(XmpConstants.DEFAULT_RDF_PREFIX + ":" + array.getArrayType());
                asimple.appendChild(econtainer);
                // for each element of the array
                List<AbstractField> innerFields = array.getAllProperties();
                serializeFields(doc, econtainer, innerFields,resourceNS, XmpConstants.DEFAULT_RDF_PREFIX, false);
            }
            else if (field instanceof AbstractStructuredType)
            {
                AbstractStructuredType structured = (AbstractStructuredType) field;
                List<AbstractField> innerFields = structured.getAllProperties();
                // property name attribute
                Element listParent = parent;
                if (wrapWithProperty)
                {
                    Element nstructured = doc
                            .createElement(resourceNS + ":" + structured.getPropertyName());
                    parent.appendChild(nstructured);
                    listParent = nstructured;
                }

                // element li
                Element estructured = doc.createElement(XmpConstants.DEFAULT_RDF_PREFIX + ":" + XmpConstants.LIST_NAME);
                listParent.appendChild(estructured);
                if (parseTypeResourceForLi)
                {
                    estructured.setAttribute("rdf:parseType", "Resource");
                    // all properties
                    serializeFields(doc, estructured, innerFields,resourceNS, null, true);
                }
                else
                {
                    // element description
                    Element econtainer = doc.createElement(XmpConstants.DEFAULT_RDF_PREFIX + ":" + "Description");
                    estructured.appendChild(econtainer);
                    // all properties
                    serializeFields(doc, econtainer, innerFields,resourceNS, null, true);
                }
            }
            else
            {
                // XXX finish serialization classes
                System.err.println(">> TODO >> " + field.getClass());
            }
        }

    }

    private void fillElementWithAttributes(Element target, AbstractComplexProperty property)
    {
        // normalize the attributes list
        List<Attribute> toSerialize = normalizeAttributes(property);        
        
        for (Attribute attribute : toSerialize)
        {
            if (XmpConstants.RDF_NAMESPACE.equals(attribute.getNamespace()))
            {
                target.setAttribute(XmpConstants.DEFAULT_RDF_PREFIX + ":" + attribute.getName(), attribute.getValue());
            }
            else
            {
                target.setAttribute(attribute.getName(), attribute.getValue());
            }
        }
        
        for (Map.Entry<String, String> ns : property.getAllNamespacesWithPrefix().entrySet())
        {
            target.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + ns.getValue(), ns.getKey());
        }
    }

    /** Normalize the list of attributes.
     * 
     * Attributes which match a schema property are serialized as child elements
     * so only return the ones which do not match a schema property
     * 
     * @param property the property that needs to be inspected
     * @return the list of attributed for serializing
     */
    private List<Attribute> normalizeAttributes(AbstractComplexProperty property)
    {
        List<Attribute> attributes = property.getAllAttributes();
        

        List<Attribute> toSerialize = new ArrayList<Attribute>();
        List<AbstractField> fields = property.getAllProperties();
                
        for (Attribute attribute : attributes)
        {
            boolean matchesField = false;
            for (AbstractField field : fields)
            {
                if (attribute.getName().compareTo(field.getPropertyName()) == 0)
                {
                    matchesField = true;
                    break;
                }
            }
            if (!matchesField)
            {
                toSerialize.add(attribute);
            }
        }
        return toSerialize;
        
    }

    protected Element createRdfElement(Document doc, XMPMetadata metadata, boolean withXpacket)
    {
        // starting xpacket
        if (withXpacket)
        {
            ProcessingInstruction beginXPacket = doc.createProcessingInstruction("xpacket",
                    "begin=\"" + metadata.getXpacketBegin() + "\" id=\"" + metadata.getXpacketId() + "\"");
            doc.appendChild(beginXPacket);
        }
        // meta element
        Element xmpmeta = doc.createElementNS("adobe:ns:meta/", "x:xmpmeta");
        xmpmeta.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:x", "adobe:ns:meta/");
        doc.appendChild(xmpmeta);
        // ending xpacket
        if (withXpacket)
        {
            ProcessingInstruction endXPacket = doc.createProcessingInstruction("xpacket",
                    "end=\"" + metadata.getEndXPacket() + "\"");
            doc.appendChild(endXPacket);
        }
        // rdf element
        Element rdf = doc.createElementNS(XmpConstants.RDF_NAMESPACE, "rdf:RDF");
        // rdf.setAttributeNS(XMPSchema.NS_NAMESPACE, qualifiedName, value)
        xmpmeta.appendChild(rdf);
        // return the rdf element where all will be put
        return rdf;
    }

    /**
     * Save the XML document to an output stream.
     * 
     * @param doc
     *            The XML document to save.
     * @param outStream
     *            The stream to save the document to.
     * @param encoding
     *            The encoding to save the file as.
     * 
     * @throws TransformerException
     *             If there is an error while saving the XML.
     */
    private void save(Node doc, OutputStream outStream, String encoding) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // human readable
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // indent elements
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // encoding
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // initialize StreamResult with File object to save to file
        Result result = new StreamResult(outStream);
        DOMSource source = new DOMSource(doc);
        // save
        transformer.transform(source, result);
    }
}
