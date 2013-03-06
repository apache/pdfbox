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

package org.apache.xmpbox.type;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmpbox.XMPMetadata;

public class DefinedStructuredType extends AbstractStructuredType
{

    private Map<String, PropertyType> definedProperties = null;

    public DefinedStructuredType(XMPMetadata metadata, String namespaceURI, String fieldPrefix, String propertyName)
    {
        super(metadata, namespaceURI, fieldPrefix, propertyName);
        this.definedProperties = new HashMap<String, PropertyType>();
    }

    public DefinedStructuredType(XMPMetadata metadata)
    {
        super(metadata);
        this.definedProperties = new HashMap<String, PropertyType>();
    }

    public void addProperty(String name, PropertyType type)
    {
        definedProperties.put(name, type);
    }

    public Map<String, PropertyType> getDefinedProperties()
    {
        return definedProperties;
    }

}
