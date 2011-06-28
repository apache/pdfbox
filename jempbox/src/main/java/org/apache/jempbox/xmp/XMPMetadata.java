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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.impl.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;

/**
 * This class represents the top level XMP data structure and gives access to
 * the various schemas that are available as part of the XMP specification.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.10 $
 */
public class XMPMetadata
{
    /**
     * Supported encoding for persisted XML.
     */
    public static final String ENCODING_UTF8 = "UTF-8";

    /**
     * Supported encoding for persisted XML.
     */
    public static final String ENCODING_UTF16BE = "UTF-16BE";

    /**
     * Supported encoding for persisted XML.
     */
    public static final String ENCODING_UTF16LE = "UTF-16LE";

    /**
     * The DOM representation of the metadata.
     */
    protected Document xmpDocument;

    /**
     * The encoding of the XMP document. Default is UTF8.
     */
    protected String encoding = ENCODING_UTF8;

    /**
     * A mapping of namespaces.
     */
    protected Map<String,Class<?>> nsMappings = new HashMap<String,Class<?>>();

    /**
     * Default constructor, creates blank XMP doc.
     * 
     * @throws IOException
     *             If there is an error creating the initial document.
     */
    public XMPMetadata() throws IOException
    {
        xmpDocument = XMLUtil.newDocument();
        ProcessingInstruction beginXPacket = xmpDocument
                .createProcessingInstruction("xpacket",
                        "begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"");

        xmpDocument.appendChild(beginXPacket);
        Element xmpMeta = xmpDocument.createElementNS("adobe:ns:meta/",
                "x:xmpmeta");
        xmpMeta.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:x",
                "adobe:ns:meta/");

        xmpDocument.appendChild(xmpMeta);

        Element rdf = xmpDocument.createElement("rdf:RDF");
        rdf.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:rdf",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        xmpMeta.appendChild(rdf);

        ProcessingInstruction endXPacket = xmpDocument
                .createProcessingInstruction("xpacket", "end=\"w\"");
        xmpDocument.appendChild(endXPacket);
        init();
    }

    /**
     * Constructor from an existing XML document.
     * 
     * @param doc
     *            The root XMP document.
     */
    public XMPMetadata(Document doc)
    {
        xmpDocument = doc;
        init();
    }

    private void init()
    {
        nsMappings.put(XMPSchemaPDF.NAMESPACE, XMPSchemaPDF.class);
        nsMappings.put(XMPSchemaBasic.NAMESPACE, XMPSchemaBasic.class);
        nsMappings
                .put(XMPSchemaDublinCore.NAMESPACE, XMPSchemaDublinCore.class);
        nsMappings.put(XMPSchemaMediaManagement.NAMESPACE,
                XMPSchemaMediaManagement.class);
        nsMappings.put(XMPSchemaRightsManagement.NAMESPACE,
                XMPSchemaRightsManagement.class);
        nsMappings.put(XMPSchemaBasicJobTicket.NAMESPACE,
                XMPSchemaBasicJobTicket.class);
        nsMappings.put(XMPSchemaDynamicMedia.NAMESPACE,
                XMPSchemaDynamicMedia.class);
        nsMappings.put(XMPSchemaPagedText.NAMESPACE, XMPSchemaPagedText.class);
        nsMappings.put(XMPSchemaIptc4xmpCore.NAMESPACE,
                XMPSchemaIptc4xmpCore.class);
        nsMappings.put(XMPSchemaPhotoshop.NAMESPACE, XMPSchemaPhotoshop.class);
    }

    /**
     * Will add a XMPSchema to the set of identified schemas.
     * 
     * The class needs to have a constructor with parameter Element
     * 
     * @param namespace
     *            The namespace URI of the schmema for instance
     *            http://purl.org/dc/elements/1.1/.
     * @param xmpSchema
     *            The schema to associated this identifier with.
     */
    public void addXMLNSMapping(String namespace, Class<?> xmpSchema)
    {

        if (!(XMPSchema.class.isAssignableFrom(xmpSchema)))
        {
            throw new IllegalArgumentException(
                    "Only XMPSchemas can be mapped to.");
        }

        nsMappings.put(namespace, xmpSchema);
    }

    /**
     * Get the PDF Schema.
     * 
     * @return The first PDF schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaPDF getPDFSchema() throws IOException
    {
        return (XMPSchemaPDF) getSchemaByClass(XMPSchemaPDF.class);
    }

    /**
     * Get the Basic Schema.
     * 
     * @return The first Basic schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaBasic getBasicSchema() throws IOException
    {
        return (XMPSchemaBasic) getSchemaByClass(XMPSchemaBasic.class);
    }

    /**
     * Get the Dublin Core Schema.
     * 
     * @return The first Dublin schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaDublinCore getDublinCoreSchema() throws IOException
    {
        return (XMPSchemaDublinCore) getSchemaByClass(XMPSchemaDublinCore.class);
    }

    /**
     * Get the Media Management Schema.
     * 
     * @return The first Media Management schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaMediaManagement getMediaManagementSchema()
            throws IOException
    {
        return (XMPSchemaMediaManagement) getSchemaByClass(XMPSchemaMediaManagement.class);
    }

    /**
     * Get the Schema Rights Schema.
     * 
     * @return The first Schema Rights schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaRightsManagement getRightsManagementSchema()
            throws IOException
    {
        return (XMPSchemaRightsManagement) getSchemaByClass(XMPSchemaRightsManagement.class);
    }

    /**
     * Get the Job Ticket Schema.
     * 
     * @return The first Job Ticket schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaBasicJobTicket getBasicJobTicketSchema() throws IOException
    {
        return (XMPSchemaBasicJobTicket) getSchemaByClass(XMPSchemaBasicJobTicket.class);
    }

    /**
     * Get the Dynamic Media Schema.
     * 
     * @return The first Dynamic Media schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaDynamicMedia getDynamicMediaSchema() throws IOException
    {
        return (XMPSchemaDynamicMedia) getSchemaByClass(XMPSchemaDynamicMedia.class);
    }

    /**
     * Get the Paged Text Schema.
     * 
     * @return The first Paged Text schema in the list.
     * 
     * @throws IOException
     *             If there is an error accessing the schema.
     */
    public XMPSchemaPagedText getPagedTextSchema() throws IOException
    {
        return (XMPSchemaPagedText) getSchemaByClass(XMPSchemaPagedText.class);
    }

    /**
     * Add a new Media Management schema.
     * 
     * @return The newly added schema.
     */
    public XMPSchemaMediaManagement addMediaManagementSchema()
    {
        XMPSchemaMediaManagement schema = new XMPSchemaMediaManagement(this);
        return (XMPSchemaMediaManagement) basicAddSchema(schema);
    }

    /**
     * Add a new Rights Managment schema.
     * 
     * @return The newly added schema.
     */
    public XMPSchemaRightsManagement addRightsManagementSchema()
    {
        XMPSchemaRightsManagement schema = new XMPSchemaRightsManagement(this);
        return (XMPSchemaRightsManagement) basicAddSchema(schema);
    }

    /**
     * Add a new Job Ticket schema.
     * 
     * @return The newly added schema.
     */
    public XMPSchemaBasicJobTicket addBasicJobTicketSchema()
    {
        XMPSchemaBasicJobTicket schema = new XMPSchemaBasicJobTicket(this);
        return (XMPSchemaBasicJobTicket) basicAddSchema(schema);
    }

    /**
     * Add a new Dynamic Media schema.
     * 
     * @return The newly added schema.
     */
    public XMPSchemaDynamicMedia addDynamicMediaSchema()
    {
        XMPSchemaDynamicMedia schema = new XMPSchemaDynamicMedia(this);
        return (XMPSchemaDynamicMedia) basicAddSchema(schema);
    }

    /**
     * Add a new Paged Text schema.
     * 
     * @return The newly added schema.
     */
    public XMPSchemaPagedText addPagedTextSchema()
    {
        XMPSchemaPagedText schema = new XMPSchemaPagedText(this);
        return (XMPSchemaPagedText) basicAddSchema(schema);
    }

    /**
     * Add a custom schema to the root rdf. The schema has to have been created
     * as a child of this XMPMetadata.
     * 
     * @param schema
     *            The schema to add.
     */
    public void addSchema(XMPSchema schema)
    {
        Element rdf = getRDFElement();
        rdf.appendChild(schema.getElement());
    }

    /**
     * Save the XMP document to a file.
     * 
     * @param file
     *            The file to save the XMP document to.
     * 
     * @throws Exception
     *             If there is an error while writing to the stream.
     */
    public void save(String file) throws Exception
    {
        XMLUtil.save(xmpDocument, file, encoding);
    }

    /**
     * Save the XMP document to a stream.
     * 
     * @param outStream
     *            The stream to save the XMP document to.
     * 
     * @throws TransformerException
     *             If there is an error while writing to the stream.
     */
    public void save(OutputStream outStream) throws TransformerException
    {
        XMLUtil.save(xmpDocument, outStream, encoding);
    }

    /**
     * Get the XML document as a byte array.
     * 
     * @return The metadata as an XML byte stream.
     * @throws Exception
     *             If there is an error creating the stream.
     */
    public byte[] asByteArray() throws Exception
    {
        return XMLUtil.asByteArray(xmpDocument, encoding);
    }

    /**
     * Get the XML document from this object.
     * 
     * @return This object as an XML document.
     */
    public Document getXMPDocument()
    {
        return xmpDocument;
    }

    /**
     * Generic add schema method.
     * 
     * @param schema
     *            The schema to add.
     * 
     * @return The newly added schema.
     */
    protected XMPSchema basicAddSchema(XMPSchema schema)
    {
        Element rdf = getRDFElement();
        rdf.appendChild(schema.getElement());
        return schema;
    }

    /**
     * Create and add a new PDF Schema to this metadata. Typically a XMP
     * document will only have one PDF schema (but multiple are supported) so it
     * is recommended that you first check the existence of a PDF scheme by
     * using getPDFSchema()
     * 
     * @return A new blank PDF schema that is now part of the metadata.
     */
    public XMPSchemaPDF addPDFSchema()
    {
        XMPSchemaPDF schema = new XMPSchemaPDF(this);
        return (XMPSchemaPDF) basicAddSchema(schema);
    }

    /**
     * Create and add a new Dublin Core Schema to this metadata. Typically a XMP
     * document will only have one schema for each type (but multiple are
     * supported) so it is recommended that you first check the existence of a
     * this scheme by using getDublinCoreSchema()
     * 
     * @return A new blank PDF schema that is now part of the metadata.
     */
    public XMPSchemaDublinCore addDublinCoreSchema()
    {
        XMPSchemaDublinCore schema = new XMPSchemaDublinCore(this);
        return (XMPSchemaDublinCore) basicAddSchema(schema);
    }

    /**
     * Create and add a new Basic Schema to this metadata. Typically a XMP
     * document will only have one schema for each type (but multiple are
     * supported) so it is recommended that you first check the existence of a
     * this scheme by using getDublinCoreSchema()
     * 
     * @return A new blank PDF schema that is now part of the metadata.
     */
    public XMPSchemaBasic addBasicSchema()
    {
        XMPSchemaBasic schema = new XMPSchemaBasic(this);
        return (XMPSchemaBasic) basicAddSchema(schema);
    }

    /**
     * Create and add a new IPTC schema to this metadata.
     * 
     * @return A new blank IPTC schema that is now part of the metadata.
     */
    public XMPSchemaIptc4xmpCore addIptc4xmpCoreSchema()
    {
        XMPSchemaIptc4xmpCore schema = new XMPSchemaIptc4xmpCore(this);
        return (XMPSchemaIptc4xmpCore) basicAddSchema(schema);
    }

    /**
     * Create and add a new Photoshop schema to this metadata.
     * 
     * @return A new blank Photoshop schema that is now part of the metadata.
     */
    public XMPSchemaPhotoshop addPhotoshopSchema()
    {
        XMPSchemaPhotoshop schema = new XMPSchemaPhotoshop(this);
        return (XMPSchemaPhotoshop) basicAddSchema(schema);
    }

    /**
     * The encoding used to write the XML. Default value:UTF-8<br/> See the
     * ENCODING_XXX constants
     * 
     * @param xmlEncoding
     *            The encoding to write the XML as.
     */
    public void setEncoding(String xmlEncoding)
    {
        encoding = xmlEncoding;
    }

    /**
     * Get the current encoding that will be used to write the XML.
     * 
     * @return The current encoding to write the XML to.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Get the root RDF element.
     * 
     * @return The root RDF element.
     */
    private Element getRDFElement()
    {
        Element rdf = null;
        NodeList nodes = xmpDocument.getElementsByTagName("rdf:RDF");
        if (nodes.getLength() > 0)
        {
            rdf = (Element) nodes.item(0);
        }
        return rdf;
    }

    /**
     * Load metadata from the filesystem.
     * 
     * @param file
     *            The file to load the metadata from.
     * 
     * @return The loaded XMP document.
     * 
     * @throws IOException
     *             If there is an error reading the data.
     */
    public static XMPMetadata load(String file) throws IOException
    {
        return new XMPMetadata(XMLUtil.parse(file));
    }

    /**
     * Load a schema from an input source.
     * 
     * @param is
     *            The input source to load the schema from.
     * 
     * @return The loaded/parsed schema.
     * 
     * @throws IOException
     *             If there was an error while loading the schema.
     */
    public static XMPMetadata load(InputSource is) throws IOException
    {
        return new XMPMetadata(XMLUtil.parse(is));
    }

    /**
     * Load metadata from the filesystem.
     * 
     * @param is
     *            The stream to load the data from.
     * 
     * @return The loaded XMP document.
     * 
     * @throws IOException
     *             If there is an error reading the data.
     */
    public static XMPMetadata load(InputStream is) throws IOException
    {
        return new XMPMetadata(XMLUtil.parse(is));
    }

    /**
     * Test main program.
     * 
     * @param args
     *            The command line arguments.
     * @throws Exception
     *             If there is an error.
     */
    public static void main(String[] args) throws Exception
    {
        XMPMetadata metadata = new XMPMetadata();
        XMPSchemaPDF pdf = metadata.addPDFSchema();
        pdf.setAbout("uuid:b8659d3a-369e-11d9-b951-000393c97fd8");
        pdf.setKeywords("ben,bob,pdf");
        pdf.setPDFVersion("1.3");
        pdf.setProducer("Acrobat Distiller 6.0.1 for Macintosh");

        XMPSchemaDublinCore dc = metadata.addDublinCoreSchema();
        dc.addContributor("Ben Litchfield");
        dc.addContributor("Solar Eclipse");
        dc.addContributor("Some Other Guy");

        XMPSchemaBasic basic = metadata.addBasicSchema();
        Thumbnail t = new Thumbnail(metadata);
        t.setFormat(Thumbnail.FORMAT_JPEG);
        t.setImage("IMAGE_DATA");
        t.setHeight(new Integer(100));
        t.setWidth(new Integer(200));
        basic.setThumbnail(t);
        basic.setBaseURL("http://www.pdfbox.org/");

        List<XMPSchema> schemas = metadata.getSchemas();
        System.out.println("schemas=" + schemas);

        metadata.save("test.xmp");
    }

    /**
     * This will get a list of XMPSchema(or subclass) objects.
     * 
     * @return A non null read-only list of schemas that are part of this
     *         metadata.
     * 
     * @throws IOException
     *             If there is an error creating a specific schema.
     */
    public List<XMPSchema> getSchemas() throws IOException
    {
        NodeList schemaList = xmpDocument
                .getElementsByTagName("rdf:Description");
        List<XMPSchema> retval = new ArrayList<XMPSchema>(schemaList.getLength());
        for (int i = 0; i < schemaList.getLength(); i++)
        {
            Element schema = (Element) schemaList.item(i);
            boolean found = false;
            NamedNodeMap attributes = schema.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++)
            {
                Node attribute = attributes.item(j);
                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();
                if (name.startsWith("xmlns:") && nsMappings.containsKey(value))
                {
                    Class<?> schemaClass = nsMappings.get(value);
                    try
                    {
                        Constructor<?> ctor = schemaClass
                                .getConstructor(new Class[] { Element.class,
                                        String.class });
                        retval.add((XMPSchema)ctor.newInstance(new Object[] { schema,
                                name.substring(6) }));
                        found = true;
                    }
                    catch(NoSuchMethodException e)
                    {
                        throw new IOException(
                                "Error: Class "
                                        + schemaClass.getName()
                                        + " must have a constructor with the signature of "
                                        + schemaClass.getName()
                                        + "( org.w3c.dom.Element, java.lang.String )");
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        throw new IOException(e.getMessage());
                    }
                }
            }
            if (!found)
            {
                retval.add(new XMPSchema(schema, null));
            }
        }
        return retval;
    }

    /**
     * Will return all schemas that fit the given namespaceURI. Which is only
     * done by using the namespace mapping (nsMapping) and not by actually
     * checking the xmlns property.
     * 
     * @param namespaceURI
     *            The namespaceURI to filter for.
     * @return A list containing the found schemas or an empty list if non are
     *         found or the namespaceURI could not be found in the namespace
     *         mapping.
     * @throws IOException
     *             If an operation on the document fails.
     */
    public List<XMPSchema> getSchemasByNamespaceURI(String namespaceURI)
            throws IOException
    {

        List<XMPSchema> l = getSchemas();
        List<XMPSchema> result = new LinkedList<XMPSchema>();

        Class<?> schemaClass = nsMappings.get(namespaceURI);
        if (schemaClass == null)
        {
            return result;
        }

        Iterator<XMPSchema> i = l.iterator();
        while (i.hasNext())
        {
            XMPSchema schema = i.next();

            if (schemaClass.isAssignableFrom(schema.getClass()))
            {
                result.add(schema);
            }
        }
        return result;
    }

    /**
     * This will return true if the XMP contains an unknown schema.
     * 
     * @return True if an unknown schema is found, false otherwise
     * 
     * @throws IOException
     *             If there is an error
     */
    public boolean hasUnknownSchema() throws IOException
    {
        NodeList schemaList = xmpDocument
                .getElementsByTagName("rdf:Description");
        for (int i = 0; i < schemaList.getLength(); i++)
        {
            Element schema = (Element) schemaList.item(i);
            NamedNodeMap attributes = schema.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++)
            {
                Node attribute = attributes.item(j);
                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();
                if (name.startsWith("xmlns:") && !nsMappings.containsKey(value)
                        && !value.equals(ResourceEvent.NAMESPACE))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to retrieve a schema from this by classname.
     * 
     * @param targetSchema
     *            Class for targetSchema.
     * 
     * @return XMPSchema or null if no target is found.
     * 
     * @throws IOException
     *             if there was an error creating the schemas of this.
     */
    public XMPSchema getSchemaByClass(Class<?> targetSchema) throws IOException
    {
        Iterator<XMPSchema> iter = getSchemas().iterator();
        while (iter.hasNext())
        {
            XMPSchema element = (XMPSchema) iter.next();
            if (element.getClass().getName().equals(targetSchema.getName()))
            {
                return element;
            }
        }
        // not found
        return null;
    }

    /**
     * Merge this metadata with the given metadata object.
     * 
     * @param metadata The metadata to merge with this document.
     * 
     * @throws IOException If there is an error merging the data.
     */
    public void merge(XMPMetadata metadata) throws IOException
    {
        List<XMPSchema> schemas2 = metadata.getSchemas();
        for (Iterator<XMPSchema> iterator = schemas2.iterator(); iterator.hasNext();)
        {
            XMPSchema schema2 = iterator.next();
            XMPSchema schema1 = getSchemaByClass(schema2.getClass());
            if (schema1 == null)
            {
                Element rdf = getRDFElement();
                rdf.appendChild(xmpDocument.importNode(schema2.getElement(),
                        true));
            }
            else
            {
                schema1.merge(schema2);
            }
        }
    }
}
