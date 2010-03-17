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