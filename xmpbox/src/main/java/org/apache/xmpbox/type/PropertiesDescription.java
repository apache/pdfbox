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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents all properties known for a specific namespace Type and attributes associated to each properties are saved
 * If a specific type well declared is used, this class map it to a basic type
 * 
 * @author a183132
 * 
 *         Attribute management pre-implemented in order to give clues to make an attribute management system
 */
public class PropertiesDescription
{

    private Map<String, PropertyType> types;

    /**
     * Build PropMapping for specified namespace
     * 
     * @param namespace
     *            namespace URI concerned by this PropMapping
     */
    public PropertiesDescription()
    {
        types = new HashMap<String, PropertyType>();
    }

    /**
     * Get All Properties Name
     * 
     * @return a list of properties qualifiedName
     */
    public List<String> getPropertiesName()
    {
        return new ArrayList<String>(types.keySet());
    }

    /**
     * Add a new property, an attributes list can be given or can be null
     * 
     * @param name
     *            new property name
     * @param type
     *            Valuetype of the new property
     * @param attr
     *            A list of attribute (put null while attribute management is not implemented)
     */
    public void addNewProperty(String name, PropertyType type)
    {
        types.put(name, type);
    }

    /**
     * Return a type of a property from its qualifiedName
     * 
     * @param name
     *            The name of the property concerned
     * @return Type of property or null
     */
    public PropertyType getPropertyType(String name)
    {
        return types.get(name);
    }

}
