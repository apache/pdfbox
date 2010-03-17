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
package org.apache.jempbox.xmp.pdfa;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;
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