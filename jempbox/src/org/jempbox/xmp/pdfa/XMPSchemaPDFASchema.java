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

import org.jempbox.xmp.XMPMetadata;
import org.jempbox.xmp.XMPSchema;
import org.w3c.dom.Element;

/**
 * Define XMP properties used PDFA extension schema description schemas.
 * 
 * @author Karsten Krieg (kkrieg@intarsys.de)
 * @version $Revision: 1.1 $
 */
public class XMPSchemaPDFASchema extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://www.aiim.org/pdfa/ns/schema";
    
    /**
     * Construct a new blank PDFA schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaPDFASchema( XMPMetadata parent )
    {
        super( parent, "pdfaSchema", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaPDFASchema( Element element, String prefix )
    {
        super( element , prefix);
    }
    
    /**
     * PDFA schema.
     *
     * @param schema The optional description of schema.
     */
    public void setSchema( String schema )
    {
        setTextProperty( "pdfaSchema:schema", schema);
    }
    
    /**
     * Get the PDFA schema.
     *
     * @return The optional description of schema.
     */
    public String getSchema()
    {
        return getTextProperty( "pdfaSchema:schema" );
    }

    /**
     * PDFA Schema prefix.
     *
     * @param prefix Preferred schema namespace prefix.
     */
    public void setPrefix( String prefix)
    {
        setTextProperty( "pdfaSchema:prefix", prefix);
    }
    
    /**
     * Get the PDFA Schema prefix.
     *
     * @return Preferred schema namespace prefix.
     */
    public String getPrefix()
    {
        return getTextProperty( "pdfaSchema:prefix" );
    }


}