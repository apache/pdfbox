/*****************************************************************************
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

package org.apache.xmpbox;

/**
 * Several constants used in XMP.
 */
public final class XmpConstants
{

    /**
     * The RDF namespace URI reference.
     */
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * The default xpacket header begin attribute.
     */
    public static final String DEFAULT_XPACKET_BEGIN = "\uFEFF";

    /**
     * The default xpacket header id attribute.
     */    
    public static final String DEFAULT_XPACKET_ID = "W5M0MpCehiHzreSzNTczkc9d";

    /**
     * The default xpacket header encoding attribute.
     */
    public static final String DEFAULT_XPACKET_ENCODING = "UTF-8";

    /**
     * The default xpacket data (XMP Data).
     */
    public static final String DEFAULT_XPACKET_BYTES = null;

    /**
     * The default xpacket trailer end attribute.
     */
    public static final String DEFAULT_XPACKET_END = "w";

    /**
     * The default namespace prefix for RDF.
     */
    public static final String DEFAULT_RDF_PREFIX = "rdf";

    /**
     * The default local name for RDF.
     */
    public static final String DEFAULT_RDF_LOCAL_NAME = "RDF";

    /**
     * The list element name.
     */
    public static final String LIST_NAME = "li";

    /**
     * The language attribute name.
     */
    public static final String LANG_NAME = "lang";

    /**
     * The about attribute name.
     */
    public static final String ABOUT_NAME = "about";

    /**
     * The Description element name.
     */
    public static final String DESCRIPTION_NAME = "Description";

    /**
     * The resource attribute name.
     */
    public static final String RESOURCE_NAME = "Resource";

    /**
     * The parse type attribute name.
     */
    public static final String PARSE_TYPE = "parseType";

    /**
     * The default language code.
     */
    public static final String X_DEFAULT = "x-default";

    private XmpConstants()
    {
        // hide constructor
    }

}
