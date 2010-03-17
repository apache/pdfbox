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
 * Dynamic Media schema.
 * 
 * @author Karsten Krieg (kkrieg@intarsys.de)
 * @version $Revision: 1.2 $
 */
public class XMPSchemaDynamicMedia extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/xmp/1.0/DynamicMedia/";
    
    /**
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaDynamicMedia( XMPMetadata parent )
    {
        super( parent, "xmpDM", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaDynamicMedia( Element element, String prefix)
    {
        super( element , prefix);
    }   
}