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
 * TODO 2 naked so far, implement
 * 
 * @author Karsten Krieg (kkrieg@intarsys.de)
 * @version $Revision: 1.1 $
 */
public class XMPSchemaPDFAId extends XMPSchema
{
    /**
     * The namespace for this schema.
     * This is the future amendment of the PDFA Spec with the trailing slash at end
     */    
    public static final String NAMESPACE = "http://www.aiim.org/pdfa/ns/id/";
    
    /**
     * Construct a new blank PDFA schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaPDFAId( XMPMetadata parent )
    {
        super( parent, "pdfaid", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaPDFAId( Element element , String prefix)
    {
        super( element , prefix);
    }   
    
    /**
     * Get the ISO19005 part number.
     * 
     * @return The ISO 19005 part number.
     */
    public Integer getPart()
    {
        return getIntegerProperty( prefix+":part" );
    }
    
    /**
     * Set the ISO19005 part number.
     * 
     * @param part The ISO 19005 part number.
     */
    public void setPart( Integer part )
    {
        setIntegerProperty( prefix+":part", part);
    }
    
    /**
     * Set the amendment idenitifier.
     *
     * @param amd The amendment idenitifier.
     */
    public void setAmd( String amd )
    {
        setTextProperty( prefix+":amd", amd);
    }
    
    /**
     * Get the amendment idenitifier.
     *
     * @return The amendment idenitifier.
     */
    public String getAmd()
    {
        return getTextProperty( prefix+":amd" );
    }

    /**
     * Set the conformance level.
     *
     * @param conformance The conformance level.
     */
    public void setConformance( String conformance )
    {
        setTextProperty( prefix+":conformance", conformance);
    }
    
    /**
     * Get the conformance level.
     *
     * @return The conformance level.
     */
    public String getConformance()
    {
        return getTextProperty( prefix+":conformance" );
    }

}