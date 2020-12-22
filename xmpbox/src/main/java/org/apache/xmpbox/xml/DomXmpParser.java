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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Deque;
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
    private final DocumentBuilder dBuilder;

    private final NamespaceFinder nsFinder;

    private boolean strictParsing = true;

    public DomXmpParser() throws XmpParsingException
    {
        try
        {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            dbFactory.setIgnoringComments(true);
            dbFactory.setNamespaceAware(true);
            dBuilder = dbFactory.newDocumentBuilder();
            nsFinder = new NamespaceFinder();
        }
        catch (final ParserConfigurationException e)
        {
            throw new XmpParsingException(ErrorType.Configuration, "Failed to initilalize", e);
        }
    }

    public boolean isStrictParsing()
    {
        return strictParsing;
    }

    public void setStrictParsing(final boolean strictParsing)
    {
        this.strictParsing = strictParsing;
    }

    public XMPMetadata parse(final byte[] xmp) throws XmpParsingException
    {
        final ByteArrayInputStream input = new ByteArrayInputStream(xmp);
        return parse(input);
    }

    public XMPMetadata parse(final InputStream input) throws XmpParsingException
    {
        Document document = null;
        try
        {
            // prevents validation messages polluting the console
            dBuilder.setErrorHandler(null);
            document = dBuilder.parse(input);
        }
        catch (final SAXException | IOException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Failed to parse", e);
        }

        XMPMetadata xmp = null;

        // Start reading
        removeComments(document);
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
        final Element rdfRdf = findDescriptionsParent(root);
        final List<Element> descriptions = DomHelper.getElementChildren(rdfRdf);
        final List<Element> dataDescriptions = new ArrayList<>(descriptions.size());
        for (final Element description : descriptions)
        {
            final Element first = DomHelper.getFirstChildElement(description);
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
        for (final Element description : dataDescriptions)
        {
            parseDescriptionRoot(xmp, description);
        }

        return xmp;
    }

    private void parseDescriptionRoot(final XMPMetadata xmp, final Element description) throws XmpParsingException
    {
        nsFinder.push(description);
        final TypeMapping tm = xmp.getTypeMapping();
        try
        {
            final List<Element> properties = DomHelper.getElementChildren(description);
            // parse attributes as properties
            final NamedNodeMap nnm = description.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++)
            {
                final Attr attr = (Attr) nnm.item(i);
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
                    parseDescriptionRootAttr(xmp, description, attr, tm);
                }
            }
            parseChildrenAsProperties(xmp, properties, tm, description);
        }
        catch (final XmpSchemaException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Parsing failed", e);
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private void parseDescriptionRootAttr(final XMPMetadata xmp, final Element description, final Attr attr, final TypeMapping tm)
            throws XmpSchemaException, XmpParsingException
    {
        final String namespace = attr.getNamespaceURI();
        XMPSchema schema = xmp.getSchema(namespace);
        if (schema == null && tm.getSchemaFactory(namespace) != null)
        {
            schema = tm.getSchemaFactory(namespace).createXMPSchema(xmp, attr.getPrefix());
            loadAttributes(schema, description);
        }
        // Only process when a schema was successfully found
        if( schema != null )
        {
            final ComplexPropertyContainer container = schema.getContainer();
            PropertyType type = checkPropertyDefinition(xmp,
                    new QName(attr.getNamespaceURI(), attr.getLocalName()));
            
            //Default to text if no type is found
            if( type == null)
            {
                type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
            }
            
            try
            {
                final AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, schema.getPrefix(),
                        attr.getLocalName(), attr.getValue(), type.type());
                container.addProperty(sp);
            }
            catch (final IllegalArgumentException e)
            {
                throw new XmpParsingException(ErrorType.Format,
                        e.getMessage() + " in " + schema.getPrefix() + ":" + attr.getLocalName(), e);
            }
        }
    }

    private void parseChildrenAsProperties(final XMPMetadata xmp, final List<Element> properties, final TypeMapping tm, final Element description)
            throws XmpParsingException, XmpSchemaException
    {
        // parse children elements as properties
        for (final Element property : properties)
        {
            final String namespace = property.getNamespaceURI();
            final PropertyType type = checkPropertyDefinition(xmp, DomHelper.getQName(property));
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
            final ComplexPropertyContainer container = schema.getContainer();
            // create property
            createProperty(xmp, property, type, container);
        }
    }

    private void createProperty(final XMPMetadata xmp, final Element property, final PropertyType type, final ComplexPropertyContainer container)
            throws XmpParsingException
    {
        final String prefix = property.getPrefix();
        final String name = property.getLocalName();
        final String namespace = property.getNamespaceURI();
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
                manageStructuredType(xmp, property, prefix, container);
            }
            else if (type.type() == Types.DefinedType)
            {
                manageDefinedType(xmp, property, prefix, container);
            }
        }
        catch (final IllegalArgumentException e)
        {
            throw new XmpParsingException(ErrorType.Format, e.getMessage() + " in " + prefix + ":" + name, e);
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private void manageDefinedType(final XMPMetadata xmp, final Element property, final String prefix, final ComplexPropertyContainer container)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(property))
        {
            final AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
            ast.setPrefix(prefix);
            container.addProperty(ast);
        }
        else
        {
            final Element inner = DomHelper.getFirstChildElement(property);
            if (inner == null)
            {
                throw new XmpParsingException(ErrorType.Format, "property should contain child element : "
                        + property);
            }
            final AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), inner);
            ast.setPrefix(prefix);
            container.addProperty(ast);
        }
    }

    private void manageStructuredType(final XMPMetadata xmp, final Element property, final String prefix, final ComplexPropertyContainer container)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(property))
        {
            final AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
            if (ast != null)
            {
                ast.setPrefix(prefix);
                container.addProperty(ast);
            }
        }
        else
        {
            final Element inner = DomHelper.getFirstChildElement(property);
            if (inner != null)
            {
                nsFinder.push(inner);
                final AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), inner);
                ast.setPrefix(prefix);
                container.addProperty(ast);
            }
        }
    }

    private void manageSimpleType(final XMPMetadata xmp, final Element property, final Types type, final ComplexPropertyContainer container)
    {
        final TypeMapping tm = xmp.getTypeMapping();
        final String prefix = property.getPrefix();
        final String name = property.getLocalName();
        final String namespace = property.getNamespaceURI();
        final AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, prefix, name, property.getTextContent(),
                type);
        loadAttributes(sp, property);
        container.addProperty(sp);
    }

    private void manageArray(final XMPMetadata xmp, final Element property, final PropertyType type, final ComplexPropertyContainer container)
            throws XmpParsingException
    {
        final TypeMapping tm = xmp.getTypeMapping();
        final String prefix = property.getPrefix();
        final String name = property.getLocalName();
        final String namespace = property.getNamespaceURI();
        final Element bagOrSeq = DomHelper.getUniqueElementChild(property);
        // ensure this is the good type of array
        if (bagOrSeq == null)
        {
            // not an array
            String whatFound = "nothing";
            if (property.getFirstChild() != null)
            {
                whatFound = property.getFirstChild().getClass().getName();
            }
            throw new XmpParsingException(ErrorType.Format, "Invalid array definition, expecting " + type.card()
                    + " and found "
                    + whatFound
                    + " [prefix=" + prefix + "; name=" + name + "]");
        }
        if (!bagOrSeq.getLocalName().equals(type.card().name()))
        {
            // not the good array type
            throw new XmpParsingException(ErrorType.Format, "Invalid array type, expecting " + type.card()
                    + " and found " + bagOrSeq.getLocalName() + " [prefix="+prefix+"; name="+name+"]");
        }
        final ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
        container.addProperty(array);
        final List<Element> lis = DomHelper.getElementChildren(bagOrSeq);

        for (final Element element : lis)
        {
            final QName propertyQName = new QName(element.getLocalName());
            final AbstractField ast = parseLiElement(xmp, propertyQName, element, type.type());
            if (ast != null)
            {
                array.addProperty(ast);
            }
        }
    }

    private void manageLangAlt(final XMPMetadata xmp, final Element property, final ComplexPropertyContainer container)
            throws XmpParsingException
    {
        manageArray(xmp, property, TypeMapping.createPropertyType(Types.LangAlt, Cardinality.Alt), container);
    }

    private void parseDescriptionInner(final XMPMetadata xmp, final Element description, final ComplexPropertyContainer parentContainer)
            throws XmpParsingException
    {
        nsFinder.push(description);
        final TypeMapping tm = xmp.getTypeMapping();
        try
        {
            final List<Element> properties = DomHelper.getElementChildren(description);
            for (final Element property : properties)
            {
                final String name = property.getLocalName();
                final PropertyType dtype = checkPropertyDefinition(xmp, DomHelper.getQName(property));
                final PropertyType ptype = tm.getStructuredPropMapping(dtype.type()).getPropertyType(name);
                // create property
                createProperty(xmp, property, ptype, parentContainer);
            }
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private AbstractField parseLiElement(final XMPMetadata xmp, final QName descriptor, final Element liElement, final Types type)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(liElement))
        {
            return parseLiDescription(xmp, descriptor, liElement);
        }
        // will find rdf:Description
        final Element liChild = DomHelper.getUniqueElementChild(liElement);
        if (liChild != null)
        {
            nsFinder.push(liChild);
            return parseLiDescription(xmp, descriptor, liChild);
        }
        else
        {
            // no child
            final String text = liElement.getTextContent();
            final TypeMapping tm = xmp.getTypeMapping();
            if (type.isSimple())
            {
                final AbstractField af = tm.instanciateSimpleProperty(descriptor.getNamespaceURI(),
                        descriptor.getPrefix(), descriptor.getLocalPart(), text, type);
                loadAttributes(af, liElement);
                return af;
            }
            else
            {
                // PDFBOX-4325: assume it is structured
                final AbstractField af;
                try
                {
                    af = tm.instanciateStructuredType(type, descriptor.getLocalPart());
                }
                catch (final BadFieldValueException ex)
                {
                    throw new XmpParsingException(ErrorType.InvalidType, "Parsing of structured type failed", ex);
                }
                loadAttributes(af, liElement);
                return af;
            }
        }
    }

    private void loadAttributes(final AbstractField sp, final Element element)
    {
        final NamedNodeMap nnm = element.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++)
        {
            final Attr attr = (Attr) nnm.item(i);
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
                final Attribute attribute = new Attribute(XMLConstants.XML_NS_URI, attr.getLocalName(), attr.getValue());
                sp.setAttribute(attribute);
            }
        }
    }

    private AbstractStructuredType parseLiDescription(final XMPMetadata xmp, final QName descriptor, final Element liElement)
            throws XmpParsingException
    {
        final TypeMapping tm = xmp.getTypeMapping();
        final List<Element> elements = DomHelper.getElementChildren(liElement);
        if (elements.isEmpty())
        {
            // The list is empty
            return null;
        }
        // Instantiate abstract structured type with hint from first element
        final Element first = elements.get(0);
        final PropertyType ctype = checkPropertyDefinition(xmp, DomHelper.getQName(first));
        final Types tt = ctype.type();
        final AbstractStructuredType ast = instanciateStructured(tm, tt, descriptor.getLocalPart(), first.getNamespaceURI());

        ast.setNamespace(descriptor.getNamespaceURI());
        ast.setPrefix(descriptor.getPrefix());

        final PropertiesDescription pm;
        if (tt.isStructured())
        {
            pm = tm.getStructuredPropMapping(tt);
        }
        else
        {
            pm = tm.getDefinedDescriptionByNamespace(first.getNamespaceURI());
        }
        for (final Element element : elements)
        {
            final String prefix = element.getPrefix();
            final String name = element.getLocalName();
            final String namespace = element.getNamespaceURI();
            final PropertyType type = pm.getPropertyType(name);
            if (type == null)
            {
                // not defined
                throw new XmpParsingException(ErrorType.NoType, "Type '" + name + "' not defined in "
                        + element.getNamespaceURI());
            }
            else if (type.card().isArray())
            {
                final ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
                ast.getContainer().addProperty(array);
                final Element bagOrSeq = DomHelper.getUniqueElementChild(element);
                final List<Element> lis = DomHelper.getElementChildren(bagOrSeq);
                for (final Element element2 : lis)
                {
                    final AbstractField ast2 = parseLiElement(xmp, descriptor, element2, type.type());
                    if (ast2 != null)
                    {
                        array.addProperty(ast2);
                    }
                }
            }
            else if (type.type().isSimple())
            {
                final AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, prefix, name,
                        element.getTextContent(), type.type());
                loadAttributes(sp, element);
                ast.getContainer().addProperty(sp);
            }
            else if (type.type().isStructured())
            {
                // create a new structured type
                final AbstractStructuredType inner = instanciateStructured(tm, type.type(), name, null);
                inner.setNamespace(namespace);
                inner.setPrefix(prefix);
                ast.getContainer().addProperty(inner);
                final ComplexPropertyContainer cpc = inner.getContainer();
                if (DomHelper.isParseTypeResource(element))
                {
                    parseDescriptionInner(xmp, element, cpc);
                }
                else
                {
                    final Element descElement = DomHelper.getFirstChildElement(element);
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

    private XMPMetadata parseInitialXpacket(final ProcessingInstruction pi) throws XmpParsingException
    {
        if (!"xpacket".equals(pi.getNodeName()))
        {
            throw new XmpParsingException(ErrorType.XpacketBadStart, "Bad processing instruction name : "
                    + pi.getNodeName());
        }
        final String data = pi.getData();
        final StringTokenizer tokens = new StringTokenizer(data, " ");
        String id = null;
        String begin = null;
        String bytes = null;
        String encoding = null;
        while (tokens.hasMoreTokens())
        {
            final String token = tokens.nextToken();
            if (!token.endsWith("\"") && !token.endsWith("\'"))
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            final String quote = token.substring(token.length() - 1);
            final int pos = token.indexOf("=" + quote);
            if (pos <= 0)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            final String name = token.substring(0, pos);
            if (token.length() - 1 < pos + 2)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            final String value = token.substring(pos + 2, token.length() - 1);
            switch (name)
            {
                case "id":
                    id = value;
                    break;
                case "begin":
                    begin = value;
                    break;
                case "bytes":
                    bytes = value;
                    break;
                case "encoding":
                    encoding = value;
                    break;
                default:
                    throw new XmpParsingException(ErrorType.XpacketBadStart,
                            "Unknown attribute in xpacket PI : '" + token + "'");
            }
        }
        return XMPMetadata.createXMPMetadata(begin, id, bytes, encoding);
    }

    private void parseEndPacket(final XMPMetadata metadata, final ProcessingInstruction pi) throws XmpParsingException
    {
        final String xpackData = pi.getData();
        // end attribute must be present and placed in first
        // xmp spec says Other unrecognized attributes can follow, but
        // should be ignored
        if (xpackData.startsWith("end="))
        {
            final char end = xpackData.charAt(5);
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

    private Element findDescriptionsParent(final Element root) throws XmpParsingException
    {
        // always <x:xmpmeta xmlns:x="adobe:ns:meta/">
        expectNaming(root, "adobe:ns:meta/", "x", "xmpmeta");
        // should only have one child
        final NodeList nl = root.getChildNodes();
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
        final Element rdfRdf = (Element) root.getFirstChild();
        // always <rdf:RDF
        // xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        expectNaming(rdfRdf, XmpConstants.RDF_NAMESPACE, XmpConstants.DEFAULT_RDF_PREFIX,
                XmpConstants.DEFAULT_RDF_LOCAL_NAME);
        // return description parent
        return rdfRdf;
    }

    private void expectNaming(final Element element, final String ns, final String prefix, final String ln) throws XmpParsingException
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
     * @param root
     *            the first node of an element or document to clear
     */
    private void removeComments(final Node root)
    {
    	// will hold the nodes which are to be deleted
    	final List<Node> forDeletion = new ArrayList<>();
    	
    	final NodeList nl = root.getChildNodes();
    	
        if (nl.getLength()<=1) 
        {
            // There is only one node so we do not remove it
            return;
        }
        
        for (int i = 0; i < nl.getLength(); i++) 
        {
            final Node node = nl.item(i);
            if (node instanceof Comment)
            {
                // comments to be deleted
            	forDeletion.add(node);
            }
            else if (node instanceof Text)
            {
                if (node.getTextContent().trim().isEmpty())
                {
                	// TODO: verify why this is necessary
                	// empty text nodes to be deleted
                	forDeletion.add(node);
                }
            }
            else if (node instanceof Element)
            {
                // clean child
                removeComments(node);
            } // else do nothing
        }

        // now remove the child nodes
        forDeletion.forEach(root::removeChild);
    }

    private AbstractStructuredType instanciateStructured(final TypeMapping tm, final Types type, final String name,
                                                         final String structuredNamespace) throws XmpParsingException
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
        catch (final BadFieldValueException e)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Parsing failed", e);
        }
    }

    private PropertyType checkPropertyDefinition(final XMPMetadata xmp, final QName prop) throws XmpParsingException
    {
        final TypeMapping tm = xmp.getTypeMapping();
        // test if namespace is set in xml
        if (!nsFinder.containsNamespace(prop.getNamespaceURI()))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Schema is not set in this document : "
                    + prop.getNamespaceURI());
        }
        // test if namespace is defined
        final String nsuri = prop.getNamespaceURI();
        if (!tm.isDefinedNamespace(nsuri))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Cannot find a definition for the namespace "
                    + prop.getNamespaceURI());
        }
        try
        {
            return tm.getSpecifiedPropertyType(prop);
        }
        catch (final BadFieldValueException e)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Failed to retrieve property definition", e);
        }
    }

    protected static class NamespaceFinder
    {
        private final Deque<Map<String, String>> stack = new ArrayDeque<>();

        protected void push(final Element description)
        {
            final NamedNodeMap nnm = description.getAttributes();
            final Map<String, String> map = new HashMap<>(nnm.getLength());
            for (int j = 0; j < nnm.getLength(); j++)
            {
                final Attr no = (Attr) nnm.item(j);
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

        protected boolean containsNamespace(final String namespace)
        {
            return stack.stream().anyMatch(map -> map.containsValue(namespace));
        }

    }

}
