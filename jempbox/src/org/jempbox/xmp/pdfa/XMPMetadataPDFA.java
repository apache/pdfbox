/**
 * Copyright (c) 2006, www.jempbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.jempbox.org
 *
 */
package org.jempbox.xmp.pdfa;

import java.io.IOException;

import org.jempbox.impl.XMLUtil;
import org.jempbox.xmp.XMPMetadata;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * PDFA Metadata.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class XMPMetadataPDFA extends XMPMetadata 
{

    /**
     * Constructor.
     * 
     * @throws IOException If there is an error creating this metadata.
     */
    public XMPMetadataPDFA() throws IOException 
    {
        super();
        init();
    }

    /**
     * Constructor.
     * 
     * @param doc The XML document that maps to the metadata.
     */
    public XMPMetadataPDFA(Document doc) 
    {
        super(doc);
        init();
    }

    private void init()
    {
        // PDFA specific schemas
        nsMappings.put( XMPSchemaPDFAField.NAMESPACE, XMPSchemaPDFAField.class );
        nsMappings.put( XMPSchemaPDFAId.NAMESPACE, XMPSchemaPDFAId.class );
        nsMappings.put( XMPSchemaPDFAProperty.NAMESPACE, XMPSchemaPDFAProperty.class );
        nsMappings.put( XMPSchemaPDFASchema.NAMESPACE, XMPSchemaPDFASchema.class );
        nsMappings.put( XMPSchemaPDFAType.NAMESPACE, XMPSchemaPDFAType.class );
    }

    /**
     * Load a a PDFA metadata.
     * 
     * @param is An XML input stream
     * @return A PDFA metadata.
     * @throws IOException If there is an error loading the XML document.
     */
    public static XMPMetadata load( InputSource is ) throws IOException
    {
        return new XMPMetadataPDFA( XMLUtil.parse( is ) );
    }

    /**
     * Get the PDFAField schema.
     * 
     * @return A PDFAField schema.
     * 
     * @throws IOException If there is an error finding the scheam.
     */
    public XMPSchemaPDFAField getPDFAFieldSchema() throws IOException 
    {        
        return (XMPSchemaPDFAField) getSchemaByClass(XMPSchemaPDFAField.class);
    }
    
    /**
     * Add a new PDFAField schema.
     * 
     * @return The newly added PDFA schema.
     */
    public XMPSchemaPDFAField addPDFAFieldSchema() 
    {
        XMPSchemaPDFAField schema = new XMPSchemaPDFAField(this);        
        return (XMPSchemaPDFAField) basicAddSchema(schema);
    }
    
    /**
     * Get the PDFA ID schema.
     * @return The PDFA ID schema.
     * @throws IOException If there is an error accessing the PDFA id schema.
     */
    public XMPSchemaPDFAId getPDFAIdSchema() throws IOException 
    {        
        return (XMPSchemaPDFAId) getSchemaByClass(XMPSchemaPDFAId.class);
    }
    
    /**
     * Add a PDFA Id schema and return the result.
     * 
     * @return The newly created PDFA Id schema.
     */
    public XMPSchemaPDFAId addPDFAIdSchema() 
    {
        XMPSchemaPDFAId schema = new XMPSchemaPDFAId(this);        
        return (XMPSchemaPDFAId) basicAddSchema(schema);
    }

    /**
     * Get the PDFA property schema.
     * 
     * @return The PDFA property schema.
     * 
     * @throws IOException If there is an error accessing the PDFA property schema.
     */
    public XMPSchemaPDFAProperty getPDFAPropertySchema() throws IOException 
    {        
        return (XMPSchemaPDFAProperty) getSchemaByClass(XMPSchemaPDFAProperty.class);
    }
    
    /**
     * Create a PDFA property schema.
     * 
     * @return The newly created property schema.
     */
    public XMPSchemaPDFAProperty addPDFAPropertySchema() 
    {
        XMPSchemaPDFAProperty schema = new XMPSchemaPDFAProperty(this);        
        return (XMPSchemaPDFAProperty) basicAddSchema(schema);
    }

    /**
     * Get the PDFA schema.
     * 
     * @return The PDFA schema.
     * 
     * @throws IOException If there is an error getting the PDFA schema.
     */
    public XMPSchemaPDFASchema getPDFASchema() throws IOException 
    {        
        return (XMPSchemaPDFASchema) getSchemaByClass(XMPSchemaPDFASchema.class);
    }

    /**
     * Add a PDFA schema.
     * 
     * @return The newly created PDFA schema.
     */
    public XMPSchemaPDFASchema addPDFASchema() 
    {
        XMPSchemaPDFASchema schema = new XMPSchemaPDFASchema(this);        
        return (XMPSchemaPDFASchema) basicAddSchema(schema);
    }

    /**
     * Get the PDFA type schema.
     * 
     * @return The PDFA type schema.
     * 
     * @throws IOException If there is an error accessing the PDFA type schema.
     */
    public XMPSchemaPDFAType getPDFATypeSchema() throws IOException 
    {        
        return (XMPSchemaPDFAType) getSchemaByClass(XMPSchemaPDFAType.class);
    }
    
    /**
     * Add a new PDFA type schema.
     * 
     * @return The newly created PDFA type schema.
     */
    public XMPSchemaPDFAType addPDFATypeSchema() 
    {
        XMPSchemaPDFAType schema = new XMPSchemaPDFAType(this);        
        return (XMPSchemaPDFAType) basicAddSchema(schema);
    }
}