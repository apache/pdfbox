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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.AbstractSimpleProperty;
import org.apache.xmpbox.type.AbstractStructuredType;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Attribute;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.ComplexPropertyContainer;
import org.apache.xmpbox.type.PropertiesDescription;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class DomXmpParser
{

    private DocumentBuilder dBuilder;

    private NamespaceFinder nsFinder;

    private boolean strictParsing = true;

    public DomXmpParser() throws XmpParsingException
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dBuilder = dbFactory.newDocumentBuilder();
            nsFinder = new NamespaceFinder();
        }
        catch (ParserConfigurationException e)
        {
            throw new XmpParsingException(ErrorType.Configuration, "Failed to initilalize", e);
        }

    }

    public boolean isStrictParsing()
    {
        return strictParsing;
    }

    public void setStrictParsing(boolean strictParsing)
    {
        this.strictParsing = strictParsing;
    }

    public XMPMetadata parse(byte[] xmp) throws XmpParsingException
    {
        ByteArrayInputStream input = new ByteArrayInputStream(xmp);
        return parse(input);
    }

    public XMPMetadata parse(InputStream input) throws XmpParsingException
    {
        Document document = null;
        try
        {
            document = dBuilder.parse(input);
        }
        catch (SAXException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Failed to parse", e);
        }
        catch (IOException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Failed to parse", e);
        }
        // document.normalizeDocument();
        XMPMetadata xmp = null;

        // Start reading
        removeComments(document.getFirstChild());
        Node node = document.getFirstChild();

        // expect xpacket processing instruction
        if (!(node instanceof ProcessingInstruction))
        {
            throw new XmpParsingException(ErrorType.XpacketBadStart, "xmp should start with a processing instruction");
        }
        else
        {
            xmp = parseInitialXpacket((ProcessingInstruction) node);
            node = node.getNextSibling();
        }
        // forget other processing instruction
        while (node instanceof ProcessingInstruction)
        {
            node = node.getNextSibling();
        }
        // expect root element
        Element root = null;
        if (!(node instanceof Element))
        {
            throw new XmpParsingException(ErrorType.NoRootElement, "xmp should contain a root element");
        }
        else
        {
            // use this element as root
            root = (Element) node;
            node = node.getNextSibling();
        }
        // expect xpacket end
        if (!(node instanceof ProcessingInstruction))
        {
            throw new XmpParsingException(ErrorType.XpacketBadEnd, "xmp should end with a processing instruction");
        }
        else
        {
            parseEndPacket(xmp, (ProcessingInstruction) node);
            node = node.getNextSibling();
        }
        // should be null
        if (node != null)
        {
            throw new XmpParsingException(ErrorType.XpacketBadEnd,
                    "xmp should end after xpacket end processing instruction");
        }
        // xpacket is OK and the is no more nodes
        // Now, parse the content of root
        Element rdfRdf = findDescriptionsParent(root);
        List<Element> descriptions = DomHelper.getElementChildren(rdfRdf);
        List<Element> dataDescriptions = new ArrayList<Element>(descriptions.size());
        for (Element description : descriptions)
        {
            Element first = DomHelper.getFirstChildElement(description);
            if (first != null && "pdfaExtension".equals(first.getPrefix()))
            {
                PdfaExtensionHelper.validateNaming(xmp, description);
                parseDescriptionRoot(xmp, description);
            }
            else
            {
                dataDescriptions.add(description);
            }
        }
        // find schema description
        PdfaExtensionHelper.populateSchemaMapping(xmp);
        // parse data description
        for (Element description : dataDescriptions)
        {
            parseDescriptionRoot(xmp, description);
        }

        return xmp;
    }

    private void parseDescriptionRoot(XMPMetadata xmp, Element description) throws XmpParsingException
    {
        nsFinder.push(description);
        TypeMapping tm = xmp.getTypeMapping();
        try
        {
            List<Element> properties = DomHelper.getElementChildren(description);
            // parse attributes as properties
            NamedNodeMap nnm = description.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++)
            {
                Attr attr = (Attr) nnm.item(i);
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
                {
                    // do nothing
                }
                else if (XmpConstants.DEFAULT_RDF_PREFIX.equals(attr.getPrefix())
                        && XmpConstants.ABOUT_NAME.equals(attr.getLocalName()))
                {
                    // do nothing
                }
                else if (attr.getPrefix() == null && XmpConstants.ABOUT_NAME.equals(attr.getLocalName()))
                {
                    // do nothing
                }
                else
                {
                    String namespace = attr.getNamespaceURI();
                    XMPSchema schema = xmp.getSchema(namespace);
                    if (schema == null)
                    {
                        schema = tm.getSchemaFactory(namespace).createXMPSchema(xmp, attr.getPrefix());
                        loadAttributes(schema, description);
                    }
                    ComplexPropertyContainer container = schema.getContainer();
                    PropertyType type = checkPropertyDefinition(xmp,
                            new QName(attr.getNamespaceURI(), attr.getLocalName()));
                    AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, schema.getPrefix(),
                            attr.getLocalName(), attr.getValue(), type.type());
                    container.addProperty(sp);
                }

            }
            // parse children elements as properties
            for (Element property : properties)
            {
                String namespace = property.getNamespaceURI();
                PropertyType type = checkPropertyDefinition(xmp, DomHelper.getQName(property));
                // create the container
                if (!tm.isDefinedSchema(namespace))
                {
                    throw new XmpParsingException(ErrorType.NoSchema,
                            "This namespace is not a schema or a structured type : " + namespace);
                }
                XMPSchema schema = xmp.getSchema(namespace);
                if (schema == null)
                {
                    schema = tm.getSchemaFactory(namespace).createXMPSchema(xmp, property.getPrefix());
                    loadAttributes(schema, description);
                }
                ComplexPropertyContainer container = schema.getContainer();
                // create property
                createProperty(xmp, property, type, container);
            }
        }
        catch (XmpSchemaException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Parsing failed", e);
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private void createProperty(XMPMetadata xmp, Element property, PropertyType type, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        String prefix = property.getPrefix();
        String name = property.getLocalName();
        String namespace = property.getNamespaceURI();
        // create property
        nsFinder.push(property);
        try
        {
            if (type == null)
            {
                if (strictParsing)
                {
                    throw new XmpParsingException(ErrorType.InvalidType, "No type defined for {" + namespace + "}"
                            + name);
                }
                else
                {
                    // use it as string
                    manageSimpleType(xmp, property, Types.Text, container);
                }
            }
            else if (type.type() == Types.LangAlt)
            {
                manageLangAlt(xmp, property, container);
            }
            else if (type.card().isArray())
            {
                manageArray(xmp, property, type, container);
            }
            else if (type.type().isSimple())
            {
                manageSimpleType(xmp, property, type.type(), container);
            }
            else if (type.type().isStructured())
            {
                if (DomHelper.isParseTypeResource(property))
                {
                    AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
                    ast.setPrefix(prefix);
                    container.addProperty(ast);
                }
                else
                {
                    Element inner = DomHelper.getFirstChildElement(property);
                    if (inner != null)
                    {
                        AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), inner);
                        ast.setPrefix(prefix);
                        container.addProperty(ast);
                    }
                }
            }
            else if (type.type() == Types.DefinedType)
            {
                if (DomHelper.isParseTypeResource(property))
                {
                    AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
                    ast.setPrefix(prefix);
                    container.addProperty(ast);
                }
                else
                {
                    Element inner = DomHelper.getFirstChildElement(property);
                    if (inner == null)
                    {
                        throw new XmpParsingException(ErrorType.Format, "property should contain child element : "
                                + property);
                    }
                    AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), inner);
                    ast.setPrefix(prefix);
                    container.addProperty(ast);
                }
            }
        }
        finally
        {
            nsFinder.pop();
        }

    }

    private void manageSimpleType(XMPMetadata xmp, Element property, Types type, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        TypeMapping tm = xmp.getTypeMapping();
        String prefix = property.getPrefix();
        String name = property.getLocalName();
        String namespace = property.getNamespaceURI();
        AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, prefix, name, property.getTextContent(),
                type);
        loadAttributes(sp, property);
        container.addProperty(sp);
    }

    private void manageArray(XMPMetadata xmp, Element property, PropertyType type, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        TypeMapping tm = xmp.getTypeMapping();
        String prefix = property.getPrefix();
        String name = property.getLocalName();
        String namespace = property.getNamespaceURI();
        Element bagOrSeq = DomHelper.getUniqueElementChild(property);
        // ensure this is the good type of array
        if (bagOrSeq == null)
        {
            // not an array
            throw new XmpParsingException(ErrorType.Format, "Invalid array definition, expecting " + type.card()
                    + " and found nothing");
        }
        if (!bagOrSeq.getLocalName().equals(type.card().name()))
        {
            // not the good array type
            throw new XmpParsingException(ErrorType.Format, "Invalid array type, expecting " + type.card()
                    + " and found " + bagOrSeq.getLocalName());
        }
        ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
        container.addProperty(array);
        List<Element> lis = DomHelper.getElementChildren(bagOrSeq);

        for (Element element : lis)
        {
            QName propertyQName = DomHelper.getQName(property);
            AbstractField ast = parseLiElement(xmp, propertyQName, element);
            if (ast != null)
            {
                array.addProperty(ast);
            }
        }
    }

    private void manageLangAlt(XMPMetadata xmp, Element property, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        manageArray(xmp, property, TypeMapping.createPropertyType(Types.LangAlt, Cardinality.Alt), container);
    }

    private void parseDescriptionInner(XMPMetadata xmp, Element description, ComplexPropertyContainer parentContainer)
            throws XmpParsingException
    {
        nsFinder.push(description);
        TypeMapping tm = xmp.getTypeMapping();
        try
        {
            List<Element> properties = DomHelper.getElementChildren(description);
            for (Element property : properties)
            {
                String name = property.getLocalName();
                PropertyType dtype = checkPropertyDefinition(xmp, DomHelper.getQName(property));
                PropertyType ptype = tm.getStructuredPropMapping(dtype.type()).getPropertyType(name);
                // create property
                createProperty(xmp, property, ptype, parentContainer);
            }
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private AbstractField parseLiElement(XMPMetadata xmp, QName descriptor, Element liElement)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(liElement))
        {
            return parseLiDescription(xmp, descriptor, liElement);
        }
        // will find rdf:Description
        Element liChild = DomHelper.getUniqueElementChild(liElement);
        if (liChild != null)
        {
            return parseLiDescription(xmp, descriptor, liChild);
        }
        else
        {
            // no child, so consider as simple text
            String text = liElement.getTextContent();
            TypeMapping tm = xmp.getTypeMapping();
            AbstractSimpleProperty sp = tm.instanciateSimpleProperty(descriptor.getNamespaceURI(),
                    descriptor.getPrefix(), descriptor.getLocalPart(), text, Types.Text);
            loadAttributes(sp, liElement);
            return sp;
        }
    }

    private void loadAttributes(AbstractField sp, Element element)
    {
        NamedNodeMap nnm = element.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++)
        {
            Attr attr = (Attr) nnm.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
            {
                // do nothing
            }
            else if (XmpConstants.DEFAULT_RDF_PREFIX.equals(attr.getPrefix())
                    && XmpConstants.ABOUT_NAME.equals(attr.getLocalName()))
            {
                // set about
                if (sp instanceof XMPSchema)
                {
                    ((XMPSchema) sp).setAboutAsSimple(attr.getValue());
                }
            }
            else
            {
                Attribute attribute = new Attribute(XMLConstants.XML_NS_URI, attr.getLocalName(), attr.getValue());
                sp.setAttribute(attribute);
            }
        }
    }

    private AbstractStructuredType parseLiDescription(XMPMetadata xmp, QName descriptor, Element liElement)
            throws XmpParsingException
    {
        TypeMapping tm = xmp.getTypeMapping();
        List<Element> elements = DomHelper.getElementChildren(liElement);
        if (elements.size() == 0)
        {
            // The list is empty
            return null;
        }
        // Instantiate abstract structured type with hint from first element
        Element first = elements.get(0);
        PropertyType ctype = checkPropertyDefinition(xmp, DomHelper.getQName(first));
        Types tt = ctype.type();
        AbstractStructuredType ast = instanciateStructured(tm, tt, descriptor.getLocalPart(), first.getNamespaceURI());

        ast.setNamespace(descriptor.getNamespaceURI());
        ast.setPrefix(descriptor.getPrefix());

        PropertiesDescription pm;
        if (tt.isStructured())
        {
            pm = tm.getStructuredPropMapping(tt);
        }
        else
        {
            pm = tm.getDefinedDescriptionByNamespace(first.getNamespaceURI());
        }
        for (Element element : elements)
        {
            String prefix = element.getPrefix();
            String name = element.getLocalName();
            String namespace = element.getNamespaceURI();
            PropertyType type = pm.getPropertyType(name);
            if (type == null)
            {
                // not defined
                throw new XmpParsingException(ErrorType.NoType, "Type '" + name + "' not defined in "
                        + element.getNamespaceURI());
            }
            else if (type.card().isArray())
            {
                ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
                ast.getContainer().addProperty(array);
                Element bagOrSeq = DomHelper.getUniqueElementChild(element);
                List<Element> lis = DomHelper.getElementChildren(bagOrSeq);
                for (Element element2 : lis)
                {
                    AbstractField ast2 = parseLiElement(xmp, descriptor, element2);
                    if (ast2 != null)
                    {
                        array.addProperty(ast2);
                    }
                }
            }
            else if (type.type().isSimple())
            {
                AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, prefix, name,
                        element.getTextContent(), type.type());
                loadAttributes(sp, element);
                ast.getContainer().addProperty(sp);
            }
            else if (type.type().isStructured())
            {
                // create a new structured type
                AbstractStructuredType inner = instanciateStructured(tm, type.type(), name, null);
                inner.setNamespace(namespace);
                inner.setPrefix(prefix);
                ast.getContainer().addProperty(inner);
                ComplexPropertyContainer cpc = inner.getContainer();
                if (DomHelper.isParseTypeResource(element))
                {
                    parseDescriptionInner(xmp, element, cpc);
                }
                else
                {
                    Element descElement = DomHelper.getFirstChildElement(element);
                    if (descElement != null)
                    {
                        parseDescriptionInner(xmp, descElement, cpc);
                    }
                }
            }
            else
            {
                throw new XmpParsingException(ErrorType.NoType, "Unidentified element to parse " + element + " (type="
                        + type + ")");
            }

        }
        return ast;
    }

    private XMPMetadata parseInitialXpacket(ProcessingInstruction pi) throws XmpParsingException
    {
        if (!"xpacket".equals(pi.getNodeName()))
        {
            throw new XmpParsingException(ErrorType.XpacketBadStart, "Bad processing instruction name : "
                    + pi.getNodeName());
        }
        String data = pi.getData();
        StringTokenizer tokens = new StringTokenizer(data, " ");
        String id = null;
        String begin = null;
        String bytes = null;
        String encoding = null;
        while (tokens.hasMoreTokens())
        {
            String token = tokens.nextToken();
            if (!token.endsWith("\"") && !token.endsWith("\'"))
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "'");
            }
            String quote = token.substring(token.length() - 1);
            int pos = token.indexOf("=" + quote);
            if (pos <= 0)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "'");
            }
            String name = token.substring(0, pos);
            String value = token.substring(pos + 2, token.length() - 1);
            if ("id".equals(name))
            {
                id = value;
            }
            else if ("begin".equals(name))
            {
                begin = value;
            }
            else if ("bytes".equals(name))
            {
                bytes = value;
            }
            else if ("encoding".equals(name))
            {
                encoding = value;
            }
            else
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Unknown attribute in xpacket PI : '" + token
                        + "'");
            }
        }
        return XMPMetadata.createXMPMetadata(begin, id, bytes, encoding);
    }

    private void parseEndPacket(XMPMetadata metadata, ProcessingInstruction pi) throws XmpParsingException
    {
        String xpackData = pi.getData();
        // end attribute must be present and placed in first
        // xmp spec says Other unrecognized attributes can follow, but
        // should be ignored
        if (xpackData.startsWith("end="))
        {
            char end = xpackData.charAt(5);
            // check value (5 for end='X')
            if (end != 'r' && end != 'w')
            {
                throw new XmpParsingException(ErrorType.XpacketBadEnd,
                        "Excepted xpacket 'end' attribute with value 'r' or 'w' ");
            }
            else
            {
                metadata.setEndXPacket(Character.toString(end));
            }
        }
        else
        {
            // should find end='r/w'
            throw new XmpParsingException(ErrorType.XpacketBadEnd,
                    "Excepted xpacket 'end' attribute (must be present and placed in first)");
        }
    }

    private Element findDescriptionsParent(Element root) throws XmpParsingException
    {
        // always <x:xmpmeta xmlns:x="adobe:ns:meta/">
        expectNaming(root, "adobe:ns:meta/", "x", "xmpmeta");
        // should only have one child
        NodeList nl = root.getChildNodes();
        if (nl.getLength() == 0)
        {
            // empty description
            throw new XmpParsingException(ErrorType.Format, "No rdf description found in xmp");
        }
        else if (nl.getLength() > 1)
        {
            // only expect one element
            throw new XmpParsingException(ErrorType.Format, "More than one element found in x:xmpmeta");
        }
        else if (!(root.getFirstChild() instanceof Element))
        {
            // should be an element
            throw new XmpParsingException(ErrorType.Format, "x:xmpmeta does not contains rdf:RDF element");
        } // else let's parse
        Element rdfRdf = (Element) root.getFirstChild();
        // always <rdf:RDF
        // xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        expectNaming(rdfRdf, XmpConstants.RDF_NAMESPACE, XmpConstants.DEFAULT_RDF_PREFIX,
                XmpConstants.DEFAULT_RDF_LOCAL_NAME);
        // return description parent
        return rdfRdf;
    }

    private void expectNaming(Element element, String ns, String prefix, String ln) throws XmpParsingException
    {
        if ((ns != null) && !(ns.equals(element.getNamespaceURI())))
        {
            throw new XmpParsingException(ErrorType.Format, "Expecting namespace '" + ns + "' and found '"
                    + element.getNamespaceURI() + "'");
        }
        else if ((prefix != null) && !(prefix.equals(element.getPrefix())))
        {
            throw new XmpParsingException(ErrorType.Format, "Expecting prefix '" + prefix + "' and found '"
                    + element.getPrefix() + "'");
        }
        else if ((ln != null) && !(ln.equals(element.getLocalName())))
        {
            throw new XmpParsingException(ErrorType.Format, "Expecting local name '" + ln + "' and found '"
                    + element.getLocalName() + "'");
        } // else OK
    }

    /**
     * Remove all the comments node in the parent element of the parameter
     * 
     * @param node
     *            the first node of an element or document to clear
     */
    private void removeComments(Node root)
    {
        Node node = root;
        while (node != null)
        {
            Node next = node.getNextSibling();
            if (node instanceof Comment)
            {
                // remove the comment
                node.getParentNode().removeChild(node);
            }
            else if (node instanceof Text)
            {
                Text t = (Text) node;
                if (t.getTextContent().trim().length() == 0)
                {
                    // XXX is there a better way to remove useless Text ?
                    node.getParentNode().removeChild(node);
                }
            }
            else if (node instanceof Element)
            {
                // clean child
                removeComments(node.getFirstChild());
            } // else do nothing
            node = next;
        }
        // end of document
    }

    private AbstractStructuredType instanciateStructured(TypeMapping tm, Types type, String name,
            String structuredNamespace) throws XmpParsingException
    {
        try
        {
            if (type.isStructured())
            {
                return tm.instanciateStructuredType(type, name);
            }
            else if (type.isDefined())
            {
                return tm.instanciateDefinedType(name, structuredNamespace);
            }
            else
            {
                throw new XmpParsingException(ErrorType.InvalidType, "Type not structured : " + type);
            }
        }
        catch (BadFieldValueException e)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Parsing failed", e);
        }
    }

    private PropertyType checkPropertyDefinition(XMPMetadata xmp, QName prop) throws XmpParsingException
    {
        TypeMapping tm = xmp.getTypeMapping();
        // test if namespace is set in xml
        if (!nsFinder.containsNamespace(prop.getNamespaceURI()))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Schema is not set in this document : "
                    + prop.getNamespaceURI());
        }
        // test if namespace is defined
        String nsuri = prop.getNamespaceURI();
        if (!tm.isDefinedNamespace(nsuri))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Cannot find a definition for the namespace "
                    + prop.getNamespaceURI());
        }
        try
        {
            return tm.getSpecifiedPropertyType(prop);
        }
        catch (BadFieldValueException e)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Failed to retreive property definition", e);
        }
    }

    protected class NamespaceFinder
    {

        private Stack<Map<String, String>> stack = new Stack<Map<String, String>>();

        protected void push(Element description)
        {
            NamedNodeMap nnm = description.getAttributes();
            Map<String, String> map = new HashMap<String, String>(nnm.getLength());
            for (int j = 0; j < nnm.getLength(); j++)
            {
                Attr no = (Attr) nnm.item(j);
                // if ns definition add it
                if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(no.getNamespaceURI()))
                {
                    map.put(no.getLocalName(), no.getValue());
                }
            }
            stack.push(map);
        }

        protected Map<String, String> pop()
        {
            return stack.pop();
        }

        protected boolean containsNamespace(String namespace)
        {
            for (int i = stack.size() - 1; i >= 0; i--)
            {
                Map<String, String> map = stack.get(i);
                if (map.containsValue(namespace))
                {
                    return true;
                }
            }
            // else namespace not found
            return false;
        }

    }

}
