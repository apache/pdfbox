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
package org.jempbox.xmp;

import org.w3c.dom.Element;

/**
 * Define XMP properties used with Adobe PDF documents.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class XMPSchemaPDF extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/pdf/1.3/";
    
    /**
     * Construct a new blank PDF schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaPDF( XMPMetadata parent )
    {
        super( parent, "pdf", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaPDF( Element element, String prefix )
    {
        super( element, prefix );
    }
    
    /**
     * PDF Keywords.
     *
     * @param keywords The PDF keywords.
     */
    public void setKeywords( String keywords )
    {
        setTextProperty( prefix + ":Keywords", keywords );
    }
    
    /**
     * Get the PDF keywords.
     *
     * @return The PDF keywords.
     */
    public String getKeywords()
    {
        return getTextProperty( prefix + ":Keywords" );
    }
    
    /**
     * Set the PDF file version.  1.2,1.3,...
     *
     * @param pdfVersion The version of the PDF file format.
     */
    public void setPDFVersion( String pdfVersion )
    {
        setTextProperty( prefix + ":PDFVersion", pdfVersion );
    }
    
    /**
     * Get the PDF version.
     *
     * @return The value of the PDF version property.
     */
    public String getPDFVersion()
    {
        return getTextProperty( prefix + ":PDFVersion" );
    }
    
    /**
     * Set the PDF producer.
     *
     * @param producer The tool that created the PDF.
     */
    public void setProducer( String producer )
    {
        setTextProperty( prefix + ":Producer", producer );
    }
    
    /**
     * Get the value of the producer property.
     *
     * @return The producer property.
     */
    public String getProducer()
    {
        return getTextProperty( prefix + ":Producer" );
    }
}