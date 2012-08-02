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
package org.apache.padaf.xmpbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.apache.padaf.xmpbox.parser.XmpSchemaException;
import org.apache.padaf.xmpbox.schema.AdobePDFSchema;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.padaf.xmpbox.schema.PhotoshopSchema;
import org.apache.padaf.xmpbox.schema.XMPBasicJobTicketSchema;
import org.apache.padaf.xmpbox.schema.XMPBasicSchema;
import org.apache.padaf.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.Elementable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Object representation of XMPMetaData Be CAREFUL: typically, metadata should
 * contain only one schema for each type (each NSURI). Retrieval of common
 * schemas (like DublinCore) is based on this fact and take the first schema of
 * this type encountered. However, XmpBox allow you to place schemas of same
 * type with different prefix. If you do that, you must retrieve all schemas by
 * yourself with getAllSchemas or with getSchema which use prefix parameter.
 * 
 * @author a183132
 * 
 */
public class XMPMetadata {

    private String xpacketId = "W5M0MpCehiHzreSzNTczkc9d";
    
    private String xpacketBegin = "\uFEFF";

    // DEPRECATED (SHOULD STAY NULL (Default value))
    private String xpacketBytes;
    
    private String xpacketEncoding;

    private String xpacketEndData = "end=\"w\"";

    private SchemasContainer schemas;

    private Document xmpDocument;
    
    private XMPDocumentBuilder builder;

    /**
     * Contructor of an empty default XMPMetaData
     * 
     * @throws CreateXMPMetadataException
     *             If DOM Document associated could not be created
     */
    protected XMPMetadata(XMPDocumentBuilder builder) throws CreateXMPMetadataException {
        try {
        	this.builder = builder;
            xmpDocument = org.apache.padaf.xmpbox.parser.XMLUtil.newDocument();
            schemas = new SchemasContainer();
        } catch (IOException e) {
            throw new CreateXMPMetadataException(
            "Failed to create Dom Document",e);
        }

    }

    /**
     * creates blank XMP doc with specified parameters
     * 
     * @throws CreateXMPMetadataException
     * @param xpacketBegin
     *            Value of xpacketBegin
     * @param xpacketId
     *            Value of xpacketId
     * @param xpacketBytes
     *            Value of xpacketBytes
     * @param xpacketEncoding
     *            Value of xpacket encoding
     * @throws CreateXMPMetadataException
     *             If DOM Document associated could not be created
     */
    protected XMPMetadata(XMPDocumentBuilder builder, String xpacketBegin, String xpacketId,
            String xpacketBytes, String xpacketEncoding)
    throws CreateXMPMetadataException {
    	this.builder = builder;
        this.xpacketBegin = xpacketBegin;
        this.xpacketId = xpacketId;
        this.xpacketBytes = xpacketBytes;
        this.xpacketEncoding = xpacketEncoding;
        try {
            xmpDocument = org.apache.padaf.xmpbox.parser.XMLUtil.newDocument();
            schemas = new SchemasContainer();
        } catch (IOException e) {
            throw new CreateXMPMetadataException(
            "Failed to create Dom Document",e);
        }
    }

    /**
     * Get xpacketBytes
     * 
     * @return value of xpacketBytes field
     */
    public String getXpacketBytes() {
        return xpacketBytes;
    }

    /**
     * Get xpacket encoding
     * 
     * @return value of xpacket Encoding field
     */
    public String getXpacketEncoding() {
        return xpacketEncoding;
    }

    /**
     * Get xpacket Begin
     * 
     * @return value of xpacket Begin field
     */
    public String getXpacketBegin() {
        return xpacketBegin;
    }

    /**
     * Get xpacket Id
     * 
     * @return value of xpacket Id field
     */
    public String getXpacketId() {
        return xpacketId;
    }

    /**
     * Add schema given to this metadata representation
     * 
     * @param schema
     *            The Schema to add
     */
    public void addSchema(XMPSchema schema) {
        schemas.addSchema(schema);
    }

    /**
     * Get All Schemas declared in this metadata representation
     * 
     * @return List of declared schemas
     */
    public List<XMPSchema> getAllSchemas() {
        ArrayList<XMPSchema> schem = new ArrayList<XMPSchema>();
        Iterator<XMPSchema> it = schemas.schemas.iterator();
        while (it.hasNext()) {
            schem.add((XMPSchema) it.next());
        }
        return schem;
    }

    /**
     * Set special XPACKET END PI
     * 
     * @param data
     *            The XPacket End value
     */
    public void setEndXPacket(String data) {
        xpacketEndData = data;
    }

    /**
     * get XPACKET END PI
     * 
     * @return XPACKET END Value
     */
    public String getEndXPacket() {
        return xpacketEndData;
    }

    /**
     * Get element associated to all schemas contained in this Metadata
     * 
     * @return Dom Element representing serialized metadata
     */
    public Element getContainerElement() {
        return schemas.getElement();
    }

    /**
     * Give the DOM Document to build metadata content
     * 
     * @return The XML Document which is serialized metadata
     */
    public Document getFuturOwner() {
        return xmpDocument;
    }

    /**
     * Return the schema corresponding to this nsURI BE CAREFUL: typically,
     * Metadata should contains one schema for each type this method return the
     * first schema encountered corresponding to this NSURI Return null if
     * unknown
     * 
     * @param nsURI
     *            The namespace URI corresponding to the schema wanted
     * @return The Class Schema representation
     */
    public XMPSchema getSchema(String nsURI) {
        Iterator<XMPSchema> it = schemas.schemas.iterator();
        XMPSchema tmp;
        while (it.hasNext()) {
            tmp = it.next();
            if (tmp.getNamespaceValue().equals(nsURI)) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Return the schema corresponding to this nsURI and a prefix This method is
     * here to treat metadata which embed more than one time the same schema It
     * permit to retrieve a specific schema with its prefix
     * 
     * @param prefix
     *            The prefix fixed in the schema wanted
     * @param nsURI
     *            The namespace URI corresponding to the schema wanted
     * @return The Class Schema representation
     */
    public XMPSchema getSchema(String prefix, String nsURI) {
        Iterator<XMPSchema> it = getAllSchemas().iterator();
        XMPSchema tmp;
        while (it.hasNext()) {
            tmp = it.next();
            if (tmp.getNamespaceValue().equals(nsURI)
                    && tmp.getPrefix().equals(prefix)) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Set a unspecialized schema
     * 
     * @param nsPrefix
     *            The prefix wanted for the schema
     * @param nsURI
     *            The namespace URI wanted for the schema
     * @return The schema added in order to work on it
     */
    public XMPSchema createAndAddDefaultSchema(String nsPrefix, String nsURI) {
        XMPSchema schem = new XMPSchema(this, nsPrefix, nsURI);
        schem.setAboutAsSimple("");
        addSchema(schem);
        return schem;
    }

    /**
     * Create and add a default PDFA Extension schema to this metadata This
     * method return the created schema to enter information This PDFAExtension
     * is created with all default namespaces used in PDFAExtensionSchema
     * 
     * @return PDFAExtension schema added in order to work on it
     */
    public PDFAExtensionSchema createAndAddPDFAExtensionSchemaWithDefaultNS() {
        PDFAExtensionSchema pdfAExt = new PDFAExtensionSchema(this);
        pdfAExt.setAboutAsSimple("");
        addSchema(pdfAExt);
        return pdfAExt;
    }

    /**
     * Create and add a default XMPRights schema to this metadata This method
     * return the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public XMPRightsManagementSchema createAndAddXMPRightsManagementSchema() {
        XMPRightsManagementSchema rights = new XMPRightsManagementSchema(this);
        rights.setAboutAsSimple("");
        addSchema(rights);
        return rights;
    }

    /**
     * Create and add a default PDFA Extension schema to this metadata This
     * method return the created schema to enter information This PDFAExtension
     * is created with specified list of namespaces
     * 
     * @param namespaces
     *            Special namespaces list to use
     * @return schema added in order to work on it
     * @throws XmpSchemaException
     *             If namespaces list not contains PDF/A Extension namespace URI
     */
    public PDFAExtensionSchema createAndAddPDFAExtensionSchemaWithNS(
           Map<String, String> namespaces) throws XmpSchemaException {
        PDFAExtensionSchema pdfAExt = new PDFAExtensionSchema(this, namespaces);
        pdfAExt.setAboutAsSimple("");
        addSchema(pdfAExt);
        return pdfAExt;
    }

    /**
     * Get the PDFA Extension schema This method return null if not found
     * 
     * @return The PDFAExtension schema or null if not declared
     */
    public PDFAExtensionSchema getPDFExtensionSchema() {
        return (PDFAExtensionSchema) getSchema(PDFAExtensionSchema.PDFAEXTENSIONURI);
    }

    /**
     * Create and add a default PDFA Identification schema to this metadata This
     * method return the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public PDFAIdentificationSchema createAndAddPFAIdentificationSchema() {
        PDFAIdentificationSchema pdfAId = new PDFAIdentificationSchema(this);
        pdfAId.setAboutAsSimple("");
        addSchema(pdfAId);
        return pdfAId;
    }

    /**
     * Get the PDFA Identification schema This method return null if not found
     * 
     * @return The PDFAIdentificationSchema schema or null if not declared
     */
    public PDFAIdentificationSchema getPDFIdentificationSchema() {
        return (PDFAIdentificationSchema) getSchema(PDFAIdentificationSchema.IDURI);
    }

    /**
     * Create and add a default Dublin Core schema to this metadata This method
     * return the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public DublinCoreSchema createAndAddDublinCoreSchema() {
        DublinCoreSchema dc = new DublinCoreSchema(this);
        dc.setAboutAsSimple("");
        addSchema(dc);
        return dc;
    }
    
    /**
     * Create and add a default Basic Job Ticket schema to this metadata This method
     * return the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public XMPBasicJobTicketSchema createAndAddBasicJobTicketSchema () {
        XMPBasicJobTicketSchema sc = new XMPBasicJobTicketSchema(this);
        sc.setAboutAsSimple("");
        addSchema(sc);
        return sc;
    }
    

    /**
     * Get the Dublin Core schema This method return null if not found
     * 
     * @return The DublinCoreSchema schema or null if not declared
     */
    public DublinCoreSchema getDublinCoreSchema() {
        return (DublinCoreSchema) getSchema(DublinCoreSchema.DCURI);
    }

    /**
     * Get the Basic JOb Ticket Schema schema This method return null if not found
     * 
     * @return The XMPBasicJobTicketSchema schema or null if not declared
     */
    public XMPBasicJobTicketSchema getBasicJobTicketSchema() {
        return (XMPBasicJobTicketSchema) getSchema(XMPBasicJobTicketSchema.JOB_TICKET_URI);
    }

    
    /**
     * Get the XMPRights schema This method return null if not found
     * 
     * @return The XMPRightsManagementSchema schema or null if not declared
     */
    public XMPRightsManagementSchema getXMPRightsManagementSchema() {
        return (XMPRightsManagementSchema) getSchema(XMPRightsManagementSchema.XMPRIGHTSURI);
    }

    /**
     * Get the Photoshop schema This method return null if not found
     * 
     * @return The PhotoshopSchema schema or null if not declared
     */
    public PhotoshopSchema getPhotoshopSchema() {
        return (PhotoshopSchema) getSchema(PhotoshopSchema.PHOTOSHOPURI);
    }

    /**
     * Create and add a XMP Basic schema to this metadata This method return the
     * created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public XMPBasicSchema createAndAddXMPBasicSchema() {
        XMPBasicSchema xmpB = new XMPBasicSchema(this);
        xmpB.setAboutAsSimple("");
        addSchema(xmpB);
        return xmpB;
    }

    /**
     * Get the XMP Basic schema This method return null if not found
     * 
     * @return The XMPBasicSchema schema or null if not declared
     */
    public XMPBasicSchema getXMPBasicSchema() {
        return (XMPBasicSchema) getSchema(XMPBasicSchema.XMPBASICURI);
    }

    /**
     * Create and add a XMP Media Management schema to this metadata This method
     * return the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public XMPMediaManagementSchema createAndAddXMPMediaManagementSchema() {
        XMPMediaManagementSchema xmpMM = new XMPMediaManagementSchema(this);
        xmpMM.setAboutAsSimple("");
        addSchema(xmpMM);
        return xmpMM;
    }

    /***
     * create and add Photoshop Schema to this metadata. This method return 
     * the created schema to enter information
     * @return
     */
    public PhotoshopSchema createAndAddPhotoshopSchema() {
        PhotoshopSchema photoshop = new PhotoshopSchema(this);
        photoshop.setAboutAsSimple("");
        addSchema(photoshop);
        return photoshop;
    }

    /**
     * Get the XMP Media Management schema This method return null if not found
     * 
     * @return The XMPMediaManagementSchema schema or null if not declared
     */
    public XMPMediaManagementSchema getXMPMediaManagementSchema() {
        return (XMPMediaManagementSchema) getSchema(XMPMediaManagementSchema.XMPMMURI);
    }

    /**
     * Create and add an Adobe PDF schema to this metadata This method return
     * the created schema to enter information
     * 
     * @return schema added in order to work on it
     */
    public AdobePDFSchema createAndAddAdobePDFSchema() {
        AdobePDFSchema pdf = new AdobePDFSchema(this);
        pdf.setAboutAsSimple("");
        addSchema(pdf);
        return pdf;
    }

    /**
     * Get the Adobe PDF schema This method return null if not found
     * 
     * @return The AdobePDFSchema schema or null if not declared
     */
    public AdobePDFSchema getAdobePDFSchema() {
        return (AdobePDFSchema) getSchema(AdobePDFSchema.PDFURI);
    }

    public XMPDocumentBuilder getBuilder() {
		return builder;
	}




	/**
     * Class which represent a container for schemas associated to a metadata
     * representation
     * 
     * @author a183132
     * 
     */
    public class SchemasContainer implements Elementable {

        private Element element;
        
        private List<XMPSchema> schemas;

        /**
         * 
         * Schemas Container constructor
         */
        public SchemasContainer() {
            element = xmpDocument.createElement("rdf:RDF");
            element.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:rdf",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            schemas = new ArrayList<XMPSchema>();
        }

        /**
         * Add a schema to the current structure
         * 
         * @param obj
         *            the schema to add
         */
        public void addSchema(XMPSchema obj) {
            schemas.add(obj);
            element.appendChild(obj.getElement());
        }

        /**
         * Remove a schema
         * 
         * @param schema
         *            The schema to remove
         */
        public void removeSchema(XMPSchema schema) {
            if (schemas.remove(schema)) {
                element.removeChild(schema.getElement());
            }
        }

        /**
         * Get The Dom Element in order to build serialized metadata
         * 
         * @return Dom Element
         */
        public Element getElement() {
            return element;
        }

    }

}
