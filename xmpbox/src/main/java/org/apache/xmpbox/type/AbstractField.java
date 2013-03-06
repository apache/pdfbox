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

import org.apache.xmpbox.XMPMetadata;

/**
 * Astract Object representation of a XMP 'field' (-> Properties and specific Schemas)
 * 
 * @author a183132
 * 
 */
public abstract class AbstractField
{

    private XMPMetadata metadata;

    private String propertyName;

    private Map<String, Attribute> attributes;

    /**
     * Constructor of a XMP Field
     * 
     * @param metadata
     *            The metadata to attach to this field
     * @param namespaceURI
     *            the namespace URI
     * @param prefix
     *            the prefix to set for this field
     * @param propertyName
     *            the local name to set for this field
     */
    public AbstractField(XMPMetadata metadata, String propertyName)
    {
        this.metadata = metadata;
        this.propertyName = propertyName;
        attributes = new HashMap<String, Attribute>();
    }

    /**
     * Get the propertyName (or localName)
     * 
     * @return the local Name
     */
    public final String getPropertyName()
    {
        return propertyName;
    }

    public final void setPropertyName(String value)
    {
        this.propertyName = value;
    }

    /**
     * Set a new attribute for this entity
     * 
     * @param value
     *            The Attribute property to add
     */
    public final void setAttribute(Attribute value)
    {
        if (attributes.containsKey(value.getName()))
        {
            // if same name in element, attribute will be replaced
            attributes.remove(value.getName());
        }
        if (value.getNamespace() == null)
        {
            attributes.put(value.getName(), value);
        }
        else
        {
            attributes.put(value.getName(), value);
        }
    }

    /**
     * Check if an attribute is declared for this entity
     * 
     * @param qualifiedName
     *            the full qualified name of the attribute concerned
     * @return true if attribute is present
     */
    public final boolean containsAttribute(String qualifiedName)
    {
        return attributes.containsKey(qualifiedName);
    }

    /**
     * Get an attribute with its name in this entity
     * 
     * @param qualifiedName
     *            the full qualified name of the attribute wanted
     * @return The attribute property
     */
    public final Attribute getAttribute(String qualifiedName)
    {
        return attributes.get(qualifiedName);
    }

    /**
     * Get attributes list defined for this entity
     * 
     * @return Attributes list
     */
    public final List<Attribute> getAllAttributes()
    {
        return new ArrayList<Attribute>(attributes.values());
    }

    /**
     * Remove an attribute of this entity
     * 
     * @param qualifiedName
     *            the full qualified name of the attribute wanted
     */
    public final void removeAttribute(String qualifiedName)
    {
        if (containsAttribute(qualifiedName))
        {
            attributes.remove(qualifiedName);
        }

    }

    public final XMPMetadata getMetadata()
    {
        return metadata;
    }

    public abstract String getNamespace();

    /**
     * Get the prefix of this entity
     * 
     * @return the prefix specified
     */
    public abstract String getPrefix();

}
