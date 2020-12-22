/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmpbox.XMPMetadata;

public abstract class AbstractComplexProperty extends AbstractField
{

    private final ComplexPropertyContainer container;

    private final Map<String, String> namespaceToPrefix;

    public AbstractComplexProperty(final XMPMetadata metadata, final String propertyName)
    {
        super(metadata, propertyName);
        container = new ComplexPropertyContainer();
        this.namespaceToPrefix = new HashMap<>();
    }

    public void addNamespace(final String namespace, final String prefix)
    {
        this.namespaceToPrefix.put(namespace, prefix);
    }

    public String getNamespacePrefix(final String namespace)
    {
        return this.namespaceToPrefix.get(namespace);
    }

    public Map<String, String> getAllNamespacesWithPrefix()
    {
        return this.namespaceToPrefix;
    }

    /**
     * Add a property to the current structure
     * 
     * @param obj
     *            the property to add
     */
    public final void addProperty(final AbstractField obj)
    {
        // https://www.adobe.com/content/dam/Adobe/en/devnet/xmp/pdfs/cs6/XMPSpecificationPart1.pdf
        // "Each property name in an XMP packet shall be unique within that packet"
        // "Multiple values are represented using an XMP array value"
        // "The nested element's element content shall consist of zero or more rdf:li elements, 
        // one for each item in the array"
        // thus delete existing elements of a property, except for arrays ("li")
        if (!(this instanceof ArrayProperty))
        {
            container.removePropertiesByName(obj.getPropertyName());
        }
        container.addProperty(obj);
    }

    /**
     * Remove a property
     * 
     * @param property
     *            The property to remove
     */
    public final void removeProperty(final AbstractField property)
    {
        container.removeProperty(property);
    }

    // /**
    // * Return the container of this Array
    // *
    // * @return The complex property container that represents content of this
    // * property
    // */
    public final ComplexPropertyContainer getContainer()
    {
        return container;
    }

    public final List<AbstractField> getAllProperties()
    {
        return container.getAllProperties();
    }

    public final AbstractField getProperty(final String fieldName)
    {
        final List<AbstractField> list = container.getPropertiesByLocalName(fieldName);
        // return null if no property
        if (list == null)
        {
            return null;
        }
        // return the first element of the list
        return list.get(0);
    }

    public final ArrayProperty getArrayProperty(final String fieldName)
    {
        final List<AbstractField> list = container.getPropertiesByLocalName(fieldName);
        // return null if no property
        if (list == null)
        {
            return null;
        }
        // return the first element of the list
        return (ArrayProperty) list.get(0);
    }

    protected final AbstractField getFirstEquivalentProperty(final String localName, final Class<? extends AbstractField> type)
    {
        return container.getFirstEquivalentProperty(localName, type);
    }

}
