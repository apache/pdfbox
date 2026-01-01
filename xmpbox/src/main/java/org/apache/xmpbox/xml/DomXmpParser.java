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
import java.util.stream.Collectors;

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
        catch (ParserConfigurationException e)
        {
            throw new XmpParsingException(ErrorType.Configuration, "Failed to initialize", e);
        }
    }

    /**
     * Tell if strict parsing mode is enabled.
     *
     * @return Whether strict parsing mode is enabled or not.
     */
    public boolean isStrictParsing()
    {
        return strictParsing;
    }

    /**
     * Enable or disable strict parsing mode.
     *
     * @param strictParsing Whether to be strict or lenient when parsing XMP. True (the default)
     * means that malformed XMP will result in an exception, false (lenient) means that if malformed
     * content is encountered, the parser will continue its work if possible. Use strict mode if you
     * want to work with PDF/A files. Use lenient mode if you care more about getting metadata.
     */
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
            // prevents validation messages polluting the console
            dBuilder.setErrorHandler(null);
            document = dBuilder.parse(input);
        }
        catch (SAXException | IOException e)
        {
            throw new XmpParsingException(ErrorType.Undefined, "Failed to parse: " + e.getMessage(), e);
        }

        XMPMetadata xmp = null;

        // Start reading
        removeCommentsAndBlanks(document);
        Node node = document.getFirstChild();

        // expect xpacket processing instruction
        if (!(node instanceof ProcessingInstruction))
        {
            if (strictParsing)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "xmp should start with a processing instruction");
            }
            xmp = XMPMetadata.createXMPMetadata(XmpConstants.DEFAULT_XPACKET_BEGIN,
                    XmpConstants.DEFAULT_XPACKET_ID, 
                    XmpConstants.DEFAULT_XPACKET_BYTES,
                    XmpConstants.DEFAULT_XPACKET_ENCODING);
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
            if (strictParsing)
            {
                throw new XmpParsingException(ErrorType.XpacketBadEnd, "xmp should end with a processing instruction");
            }
            xmp.setEndXPacket(XmpConstants.DEFAULT_XPACKET_END);
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
        // xpacket is OK and there are no more nodes
        // Now, parse the content of root
        nsFinder.push(root); // PDFBOX-6138: push namespaces in root
        Element rdfRdf = findDescriptionsParent(root);
        nsFinder.push(rdfRdf); // PDFBOX-6099: push namespaces in rdf:RDF

        // PDFBOX-6127: look for non standard namespaces (similar to PDFBOX-2378)
        if (!strictParsing)
        {
            NamedNodeMap nnm = rdfRdf.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++)
            {
                Attr attr = (Attr) nnm.item(i);
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
                {
                    maybeAddNonStandardNamespace(xmp, attr);
                }
            }
        }

        List<Element> descriptions = DomHelper.getElementChildren(rdfRdf);
        for (Element description : descriptions)
        {
            parseSchemaExtensions(xmp, description);
        }

        // find schema description
        PdfaExtensionHelper.populateSchemaMapping(xmp, strictParsing);

        // parse data description
        for (Element description : descriptions)
        {
            parseDescriptionRoot(xmp, description);
        }

        nsFinder.pop();
        nsFinder.pop();

        return xmp;
    }

    private void maybeAddNonStandardNamespace(XMPMetadata xmp, Attr attr)
    {
        // xmlns:prefix="namespace"
        TypeMapping tm = xmp.getTypeMapping();
        String namespace = attr.getValue();
        if (!XmpConstants.RDF_NAMESPACE.equals(namespace) &&
            !tm.isStructuredTypeNamespace(namespace) &&
            xmp.getSchema(namespace) == null && tm.getSchemaFactory(namespace) == null)
        {
            // PDFBOX-5128 / PDFBOX-6127: Add the schema on the fly if it can't be found
            // PDFBOX-5649: But only if the namespace isn't already known
            // because this adds a namespace without property descriptions
            // PDFBOX-6127: never rdf
            tm.addNewNameSpace(namespace, attr.getLocalName());
        }
    }

    private boolean isSchemaExtensionProperty(final Element element)
    {
        return element != null && "pdfaExtension".equals(element.getPrefix());
    }

    private void parseSchemaExtensions(final XMPMetadata xmp, final Element description) throws XmpParsingException
    {
        final TypeMapping tm = xmp.getTypeMapping();
        nsFinder.push(description);
        try
        {
            final List<Element> schemaExtensions = DomHelper.getElementChildren(description)
                    .stream()
                    .filter(this::isSchemaExtensionProperty)
                    .collect(Collectors.toList());
            if (!schemaExtensions.isEmpty())
            {
                PdfaExtensionHelper.validateNaming(xmp, description);
            }
            for (final Element schemaExtension : schemaExtensions)
            {
                final String namespace = schemaExtension.getNamespaceURI();
                if (!tm.isDefinedSchema(namespace))
                {
                    throw new XmpParsingException(ErrorType.NoSchema,
                            "This namespace is not from a schema: " + namespace);
                }
                PropertyType type = checkPropertyDefinition(tm, DomHelper.getQName(schemaExtension), null);
                final XMPSchema schema = tm.getSchemaFactory(namespace).createXMPSchema(xmp, schemaExtension.getPrefix());
                loadAttributes(schema, description);
                ComplexPropertyContainer container = schema.getContainer();
                createProperty(xmp, schemaExtension, type, container);
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
                if (XmpConstants.ABOUT_NAME.equals(attr.getLocalName()) && 
                    attr.getPrefix() == null || XmpConstants.DEFAULT_RDF_PREFIX.equals(attr.getPrefix()))
                {
                    // do nothing
                }
                else if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()))
                {
                    if (!strictParsing)
                    {
                        maybeAddNonStandardNamespace(xmp, attr);
                    }
                }
                else
                {
                    parseDescriptionRootAttr(xmp, description, attr, tm);
                }
            }
            parseChildrenAsProperties(xmp, properties, tm, description);
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

    private void parseDescriptionRootAttr(XMPMetadata xmp, Element description, Attr attr, TypeMapping tm)
            throws XmpSchemaException, XmpParsingException
    {
        String namespace = attr.getNamespaceURI();
        XMPSchema schema = xmp.getSchema(namespace);
        if (schema == null && tm.getSchemaFactory(namespace) != null)
        {
            schema = tm.getSchemaFactory(namespace).createXMPSchema(xmp, attr.getPrefix());
            loadAttributes(schema, description);
        }
        // Only process when a schema was successfully found
        if( schema != null )
        {
            ComplexPropertyContainer container = schema.getContainer();
            PropertyType type = checkPropertyDefinition(tm,
                    new QName(attr.getNamespaceURI(), attr.getLocalName(), attr.getPrefix()), null);

            if (type == null)
            {
                if (strictParsing)
                {
                    throw new XmpParsingException(ErrorType.InvalidType, "No type defined for {" + attr.getNamespaceURI() + "}"
                            + attr.getLocalName());
                }
                // PDFBOX-2318, PDFBOX-6106: Default to text if no type is found
                type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
            }
            else if (!type.type().isSimple() || type.card().isArray() || type.type() == Types.LangAlt)
            {
                if (strictParsing)
                {
                    throw new XmpParsingException(ErrorType.InvalidType, "The type '" +
                            type.type().name() + "' in '" + attr.getPrefix() + ":" + attr.getLocalName() + "=" + attr.getValue()
                            + "' is a structured or array type, but attributes are simple types");
                }
                // PDFBOX-6125: Default to text or skip
                if (attr.getValue().isEmpty())
                {
                    schema.removeAttribute(attr.getLocalName());
                    return;
                }
                type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
            }

            try
            {
                AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, schema.getPrefix(),
                        attr.getLocalName(), attr.getValue(), type.type());
                container.addProperty(sp);
            }
            catch (IllegalArgumentException e)
            {
                throw new XmpParsingException(ErrorType.Format,
                        e.getMessage() + " in " + schema.getPrefix() + ":" + attr.getLocalName(), e);
            }
        }
    }

    private void parseChildrenAsProperties(XMPMetadata xmp, List<Element> properties, TypeMapping tm, Element description)
            throws XmpParsingException, XmpSchemaException
    {
        // parse children elements as properties
        for (Element property : properties)
        {
            nsFinder.push(property);
            String namespace = property.getNamespaceURI();
            PropertyType type = checkPropertyDefinition(tm, DomHelper.getQName(property), null);
            // create the container
            if (!tm.isDefinedSchema(namespace))
            {
                throw new XmpParsingException(ErrorType.NoSchema,
                        "This namespace is not from a schema: " + namespace);
            }
            if (isSchemaExtensionProperty(property))
            {
                continue;
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
                // use it as string
                manageSimpleType(xmp, property, Types.Text, container);
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
        catch (IllegalArgumentException e)
        {
            throw new XmpParsingException(ErrorType.Format, e.getMessage() + " in " + prefix + ":" + name, e);
        }
        finally
        {
            nsFinder.pop();
        }
    }

    private void manageDefinedType(XMPMetadata xmp, Element property, String prefix, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(property))
        {
            AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
            if (ast == null)
            {
                throw new XmpParsingException(ErrorType.Format, "property should contain child elements : "
                        + property);
            }
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
            if (ast == null)
            {
                throw new XmpParsingException(ErrorType.Format, "inner element should contain child elements : "
                        + inner);
            }
            ast.setPrefix(prefix);
            container.addProperty(ast);
        }
    }

    private void manageStructuredType(XMPMetadata xmp, Element property, String prefix, ComplexPropertyContainer container)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(property))
        {
            AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), property);
            if (ast != null)
            {
                ast.setPrefix(prefix);
                container.addProperty(ast);
            }
        }
        else
        {
            Element inner = DomHelper.getFirstChildElement(property);
            if (inner != null)
            {
                try
                {
                    nsFinder.push(inner);
                    AbstractStructuredType ast = parseLiDescription(xmp, DomHelper.getQName(property), inner);
                    if (ast == null)
                    {
                        throw new XmpParsingException(ErrorType.Format, "inner element should contain child elements : "
                                + inner);
                    }
                    ast.setPrefix(prefix);
                    container.addProperty(ast);
                }
                finally
                {
                    nsFinder.pop();
                }
            }
        }
    }

    private void manageSimpleType(XMPMetadata xmp, Element property, Types type, ComplexPropertyContainer container)
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
            Node firstChild = property.getFirstChild();
            if (!strictParsing)
            {
                if (firstChild == null)
                {
                    // PDFBOX-6125: ignore
                    return;
                }
                if (firstChild instanceof Text)
                {
                    // PDFBOX-6125: Default to text in lenient mode
                    // Improvement idea in the future: create an array and add the text item.
                    manageSimpleType(xmp, property, Types.Text, container);
                    return;
                }
            }
            String whatFound = "nothing";
            if (firstChild != null)
            {
                whatFound = firstChild instanceof Text ? "Text" : firstChild.getClass().getName();
            }
            throw new XmpParsingException(ErrorType.Format, "Invalid array definition, expecting " + type.card()
                    + " and found "
                    + whatFound
                    + " [prefix=" + prefix + "; name=" + name + "]");
        }
        if (strictParsing && !bagOrSeq.getLocalName().equals(type.card().name()))
        {
            // not the good array type
            throw new XmpParsingException(ErrorType.Format, "Invalid array type, expecting " + type.card()
                    + " and found " + bagOrSeq.getLocalName() + " [prefix="+prefix+"; name="+name+"]");
        }
        ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
        container.addProperty(array);
        List<Element> lis = DomHelper.getElementChildren(bagOrSeq);

        for (Element element : lis)
        {
            QName propertyQName = new QName(element.getLocalName());
            AbstractField ast = parseLiElement(xmp, propertyQName, element, type.type());
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
                PropertyType dtype = checkPropertyDefinition(tm, DomHelper.getQName(property), null);
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

    private AbstractField parseLiElement(XMPMetadata xmp, QName descriptor, Element liElement, Types type)
            throws XmpParsingException
    {
        if (DomHelper.isParseTypeResource(liElement))
        {
            try
            {
                nsFinder.push(liElement);
                return parseLiDescription(xmp, descriptor, liElement);
            }
            finally
            {
                nsFinder.pop();
            }
        }
        // will find rdf:Description
        Element liChild = DomHelper.getUniqueElementChild(liElement);
        if (liChild != null)
        {
            try
            {
                nsFinder.push(liElement);
                nsFinder.push(liChild);
                return parseLiDescription(xmp, descriptor, liChild);
            }
            finally
            {
                nsFinder.pop();
                nsFinder.pop();
            }
        }
        // no child
        String text = liElement.getTextContent();
        TypeMapping tm = xmp.getTypeMapping();
        if (type.isSimple())
        {
            AbstractField af = tm.instanciateSimpleProperty(descriptor.getNamespaceURI(),
                    descriptor.getPrefix(), descriptor.getLocalPart(), text, type);
            loadAttributes(af, liElement);
            return af;
        }
        // PDFBOX-4325: assume it is structured
        AbstractStructuredType af;
        try
        {
            af = tm.instanciateStructuredType(type, descriptor.getLocalPart());
        }
        catch (BadFieldValueException ex)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Parsing of structured type failed", ex);
        }
        loadAttributes(af, liElement);
        PropertiesDescription pm;
        if (type.isStructured())
        {
            pm = tm.getStructuredPropMapping(type);
        }
        else
        {
            pm = tm.getDefinedDescriptionByNamespace(liElement.getNamespaceURI(), liElement.getLocalName());
        }
        return tryParseAttributesAsProperties(tm, liElement, af, pm, null);
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
            else if (XMLConstants.XML_NS_URI.equals(attr.getNamespaceURI()))
            {
                // This part was the fallback before PDFBOX-6130, now restricted:
                // Do not load "ordinary" attributes here because these will be handled by
                // tryParseAttributesAsProperties() and parseDescriptionRootAttr()
                Attribute attribute = new Attribute(XMLConstants.XML_NS_URI, attr.getLocalName(), attr.getValue());
                sp.setAttribute(attribute);
            }
        }
    }

    private AbstractStructuredType parseLiDescription(XMPMetadata xmp, QName parentQName, Element liDescriptionElement)
            throws XmpParsingException
    {
        TypeMapping tm = xmp.getTypeMapping();
        List<Element> liDescriptionElementChildren = DomHelper.getElementChildren(liDescriptionElement);
        if (liDescriptionElementChildren.isEmpty())
        {
            // The list is empty
            return tryParseAttributesAsProperties(tm, liDescriptionElement, null, null, parentQName);
        }
        Element firstLiDescriptionElementChild = liDescriptionElementChildren.get(0);
        if ("rdf:Description".equals(firstLiDescriptionElementChild.getTagName()))
        {
            // PDFBOX-6126: "<rdf:Description" as child of "<rdf:li"
            return parseLiDescription(xmp, parentQName, firstLiDescriptionElementChild);
        }
        // Instantiate abstract structured type with hint from first element
        nsFinder.push(firstLiDescriptionElementChild);
        QName firstChildQName = DomHelper.getQName(firstLiDescriptionElementChild);
        PropertyType ctype = checkPropertyDefinition(tm, firstChildQName, parentQName.getLocalPart());
        if (ctype == null)
        {
            // PDFBOX-5649
            throw new XmpParsingException(ErrorType.NoType,
                    "Property '" + firstChildQName.getPrefix() + ":" + firstChildQName.getLocalPart() +
                            "' not defined in " + firstChildQName.getNamespaceURI());
        }
        Types tt = ctype.type();
        AbstractStructuredType ast = instanciateStructured(tm, tt, parentQName.getLocalPart(), firstLiDescriptionElementChild.getNamespaceURI());

        ast.setNamespace(firstLiDescriptionElementChild.getNamespaceURI());
        ast.setPrefix(firstLiDescriptionElementChild.getPrefix());

        PropertiesDescription pm;
        if (tt.isStructured())
        {
            pm = tm.getStructuredPropMapping(tt);
        }
        else
        {
            pm = tm.getDefinedDescriptionByNamespace(firstLiDescriptionElementChild.getNamespaceURI(), firstLiDescriptionElementChild.getLocalName());
        }
        for (Element liDescriptionElementChild : liDescriptionElementChildren)
        {
            String prefix = liDescriptionElementChild.getPrefix();
            String name = liDescriptionElementChild.getLocalName();
            String namespace = liDescriptionElementChild.getNamespaceURI();
            PropertyType type = pm.getPropertyType(name);
            if (type == null)
            {
                if (strictParsing)
                {
                    throw new XmpParsingException(ErrorType.NoType, "Type '" + prefix + ":" + name + "' not defined in "
                            + liDescriptionElementChild.getNamespaceURI());
                }
                // PDFBOX-6135: Default to text if no type is found
                type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
            }
            if (type.card().isArray())
            {
                ArrayProperty array = tm.createArrayProperty(namespace, prefix, name, type.card());
                ast.getContainer().addProperty(array);
                Element bagOrSeq = DomHelper.getUniqueElementChild(liDescriptionElementChild);
                List<Element> lis = DomHelper.getElementChildren(bagOrSeq);
                for (Element element2 : lis)
                {
                    AbstractField ast2 = parseLiElement(xmp, parentQName, element2, type.type());
                    if (ast2 != null)
                    {
                        array.addProperty(ast2);
                    }
                }
            }
            else if (type.type().isSimple())
            {
                AbstractSimpleProperty sp = tm.instanciateSimpleProperty(namespace, prefix, name,
                        liDescriptionElementChild.getTextContent(), type.type());
                loadAttributes(sp, liDescriptionElementChild);
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
                if (DomHelper.isParseTypeResource(liDescriptionElementChild))
                {
                    parseDescriptionInner(xmp, liDescriptionElementChild, cpc);
                }
                else
                {
                    Element descElement = DomHelper.getFirstChildElement(liDescriptionElementChild);
                    if (descElement != null)
                    {
                        parseDescriptionInner(xmp, descElement, cpc);
                    }
                }
            }
            else
            {
                throw new XmpParsingException(ErrorType.NoType, "Unidentified element to parse " + liDescriptionElementChild + " (type="
                        + type + ")");
            }

        }
        ast = tryParseAttributesAsProperties(tm, liDescriptionElement, ast, pm, parentQName);
        nsFinder.pop();
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
            if (!token.endsWith("\"") && !token.endsWith("'"))
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            String quote = token.substring(token.length() - 1);
            int pos = token.indexOf("=" + quote);
            if (pos <= 0)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            String name = token.substring(0, pos);
            if (token.length() - 1 < pos + 2)
            {
                throw new XmpParsingException(ErrorType.XpacketBadStart, "Cannot understand PI data part : '" + token
                        + "' in '" + data + "'");
            }
            String value = token.substring(pos + 2, token.length() - 1);
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
                        "Expected xpacket 'end' attribute with value 'r' or 'w' ");
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
                    "Expected xpacket 'end' attribute (must be present and placed in first)");
        }
    }

    private Element findDescriptionsParent(Element root) throws XmpParsingException
    {
        Element rdfRdf = null;
        // check if already rdf element, as xmpmeta wrapper can be optional
        if (!XmpConstants.RDF_NAMESPACE.equals(root.getNamespaceURI()))
        {
            // always <x:xmpmeta xmlns:x="adobe:ns:meta/">
            if (!strictParsing && "xapmeta".equals(root.getLocalName()))
            {
                // older XMP content
                expectNaming(root, "adobe:ns:meta/", "x", "xapmeta");
            }
            else
            {
                expectNaming(root, "adobe:ns:meta/", "x", "xmpmeta");
            }
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
                throw new XmpParsingException(ErrorType.Format, "x:xmpmeta does not contains rdf:RDF element but " + root.getFirstChild());
            } // else let's parse
            rdfRdf = (Element) root.getFirstChild();
        }
        else
        {
            rdfRdf = root;
        }
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
     * Remove all the comments and blank nodes in the parent element of the parameter
     *
     * @param root the first node of an element or document to clear
     */
    private void removeCommentsAndBlanks(Node root)
    {
        // will hold the nodes which are to be deleted
        List<Node> forDeletion = new ArrayList<>();

        NodeList nl = root.getChildNodes();

        if (!(root instanceof Document) && nl.getLength() <= 1)
        {
            // There is only one node so we're done, except when Document
            return;
        }

        for (int i = 0; i < nl.getLength(); i++)
        {
            Node node = nl.item(i);
            if (node instanceof Comment)
            {
                // comments to be deleted
                forDeletion.add(node);
            }
            else if (node instanceof Text)
            {
                if (node.getTextContent().isBlank())
                {
                    // empty text nodes to be deleted
                    forDeletion.add(node);
                }
            }
            else if (node instanceof Element)
            {
                // clean child
                removeCommentsAndBlanks(node);
            } // else do nothing
        }

        // now remove the child nodes
        forDeletion.forEach(root::removeChild);
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

    private PropertyType checkPropertyDefinition(TypeMapping tm, QName qName, String parentTypeName) throws XmpParsingException
    {
        // test if namespace is set in xml
        String nsuri = qName.getNamespaceURI();
        if (!nsFinder.containsNamespace(nsuri))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Schema is not set in this document : "
                    + nsuri + ", property: " + qName.getPrefix() + ":" + qName.getLocalPart());
        }
        // test if namespace is defined
        if (!tm.isDefinedNamespace(nsuri))
        {
            throw new XmpParsingException(ErrorType.NoSchema, "Cannot find a definition for the namespace "
                    + nsuri + ", property: " + qName.getPrefix() + ":" + qName.getLocalPart());
        }
        try
        {
            return tm.getSpecifiedPropertyType(qName, parentTypeName);
        }
        catch (BadFieldValueException e)
        {
            throw new XmpParsingException(ErrorType.InvalidType, "Failed to retrieve property definition for " + qName, e);
        }
    }

    /**
     * This attempts to run the same logic as in parseLiDescription() but with simple attributes
     * that will be treated like children. This is inspired by loadAttributes() and
     * parseDescriptionRootAttr(). This solves the problem in PDFBOX-3882 where properties appear as
     * attributes in places lower than the descriptor root.
     *
     * @param tm
     * @param liElement
     * @param ast An AbstractStructuredType object, can be null.
     * @param pm A PropertiesDescription object, must be set if ast is not null.
     * @param qName QName of the parent, will be used if instantiating an AbstractStructuredType
     * object, must be set if ast is not null.
     * @return An AbstractStructuredType, possibly created here if it was null as parameter.
     * @throws XmpParsingException
     */
    private AbstractStructuredType tryParseAttributesAsProperties(
            TypeMapping tm, Element liElement, AbstractStructuredType ast,
            PropertiesDescription pm, QName qName) throws XmpParsingException
    {
        NamedNodeMap attributes = liElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Attr attr = (Attr) attributes.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()) ||
                XMLConstants.XML_NS_URI.equals(attr.getNamespaceURI()) ||
                XmpConstants.DEFAULT_RDF_PREFIX.equals(attr.getPrefix()))
            {
                // do nothing
                continue;
            }
            if (ast == null && attr.getNamespaceURI() != null) // What to do if attr.getNamespaceURI() is null?
            {
                // like in parseLiDescription():
                // Instantiate abstract structured type with hint from first element
                QName attrQName = new QName(attr.getNamespaceURI(), attr.getLocalName(), attr.getPrefix());
                PropertyType ctype = checkPropertyDefinition(tm, attrQName, null);
                // this is the type of the AbstractStructuredType, not of the element(s)
                if (ctype == null)
                {
                    throw new XmpParsingException(ErrorType.NoType,
                        "Property '" + attrQName.getLocalPart() + "' not defined in " + attrQName.getNamespaceURI());
                }
                Types tt = ctype.type();
                ast = instanciateStructured(tm, tt, qName.getLocalPart(), attr.getNamespaceURI());
                if (tt.isStructured())
                {
                    pm = tm.getStructuredPropMapping(tt);
                }
                else
                {
                    pm = tm.getDefinedDescriptionByNamespace(attr.getNamespaceURI(), attr.getLocalName());
                }
            }
            if (ast != null && pm != null && attr.getNamespaceURI() != null)
            {
                PropertyType type = pm.getPropertyType(attr.getLocalName());
                if (type == null)
                {
                    if (strictParsing)
                    {
                        throw new XmpParsingException(ErrorType.InvalidType, "No type defined for {" + attr.getNamespaceURI() + "}"
                                + attr.getLocalName());
                    }
                    // PDFBOX-2318, PDFBOX-6106: Default to text if no type is found
                    type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
                }
                else if (!type.type().isSimple() || type.card().isArray() || type.type() == Types.LangAlt)
                {
                    if (strictParsing)
                    {
                        throw new XmpParsingException(ErrorType.InvalidType, "The type '" +
                                type.type().name() + "' in '" + attr.getPrefix() + ":" + attr.getLocalName() + "=" + attr.getValue()
                                + "' is a structured or array type, but attributes are simple types");
                    }
                    // PDFBOX-6125: Default to text or skip
                    if (attr.getValue().isEmpty())
                    {
                        continue;
                    }
                    type = TypeMapping.createPropertyType(Types.Text, Cardinality.Simple);
                }
                AbstractSimpleProperty asp = tm.instanciateSimpleProperty(
                        attr.getNamespaceURI(), attr.getPrefix(), attr.getLocalName(),
                        attr.getValue(), type.type());
                ast.getContainer().addProperty(asp);
            }
        }
        return ast;
    }

    protected static class NamespaceFinder
    {
        private final Deque<Map<String, String>> stack = new ArrayDeque<>();

        protected void push(Element description)
        {
            NamedNodeMap nnm = description.getAttributes();
            Map<String, String> map = new HashMap<>(nnm.getLength());
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
            return stack.stream().anyMatch(map -> map.containsValue(namespace));
        }

    }

}
