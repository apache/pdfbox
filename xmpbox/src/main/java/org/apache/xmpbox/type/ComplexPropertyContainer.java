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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Object representation for arrays content This Class could be used to define directly a property with more than one
 * field (structure) and also schemas
 * 
 * @author a183132
 * 
 */
public class ComplexPropertyContainer
{

    private final List<AbstractField> properties;

    /**
     * Complex Property type constructor (namespaceURI is given)
     * 
     */
    public ComplexPropertyContainer()
    {
        properties = new ArrayList<>();
    }

    /**
     * Give the first property found in this container with type and localname expected
     * 
     * @param localName
     *            the localname of property wanted
     * @param type
     *            the property type of property wanted
     * @return the property wanted
     */
    protected AbstractField getFirstEquivalentProperty(final String localName, final Class<? extends AbstractField> type)
    {
        final List<AbstractField> list = getPropertiesByLocalName(localName);
        if (list != null)
        {
            for (final AbstractField abstractField : list)
            {
                if (abstractField.getClass().equals(type))
                {
                    return abstractField;
                }
            }
        }
        return null;
    }

    /**
     * Add a property to the current structure
     * 
     * @param obj
     *            the property to add
     */
    public void addProperty(final AbstractField obj)
    {
        if (containsProperty(obj))
        {
            removeProperty(obj);
        }
        properties.add(obj);
    }

    /**
     * Return all children associated to this property
     * 
     * @return All Properties contained in this container, never null.
     */
    public List<AbstractField> getAllProperties()
    {
        return properties;
    }

    /**
     * Return all properties with this specified localName.
     *
     * @param localName the local name wanted
     * @return All properties with local name which match with localName given, or null if there are
     * none.
     */
    public List<AbstractField> getPropertiesByLocalName(final String localName)
    {
        final List<AbstractField> list =
                getAllProperties().stream().
                filter(abstractField -> (abstractField.getPropertyName().equals(localName))).
                collect(Collectors.toList());
        if (list.isEmpty())
        {
            return null;
        }
        else
        {
            return list;
        }
    }

    /**
     * Check if two properties are equal.
     * 
     * @param prop1
     *            First property
     * @param prop2
     *            Second property
     * @return True if these properties are equal.
     */
    public boolean isSameProperty(final AbstractField prop1, final AbstractField prop2)
    {

        if (prop1.getClass().equals(prop2.getClass()))
        {
            final String pn1 = prop1.getPropertyName();
            final String pn2 = prop2.getPropertyName();
            if (pn1 == null)
            {
                return pn2 == null;
            }
            else
            {
                if (pn1.equals(pn2))
                {
                    return prop1.equals(prop2);
                }
            }
        }
        return false;
    }

    /**
     * Check if a XMPFieldObject is in the complex property
     * 
     * @param property
     *            The property to check
     * @return True if property is present in this container
     */
    public boolean containsProperty(final AbstractField property)
    {
        final Iterator<AbstractField> it = getAllProperties().iterator();
        AbstractField tmp;
        while (it.hasNext())
        {
            tmp = it.next();
            if (isSameProperty(tmp, property))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a property
     * 
     * @param property
     *            The property to remove
     */
    public void removeProperty(final AbstractField property)
    {
        if (containsProperty(property))
        {
            properties.remove(property);
        }
    }

    /**
     * Remove all properties with a specified LocalName.
     * 
     * @param localName The name for which to remove all.
     */
    public void removePropertiesByName(final String localName)
    {
        if (properties.isEmpty())
        {
            return;
        }
        final List<AbstractField> propList = getPropertiesByLocalName(localName);
        if (propList == null)
        {
            return;
        }
        propList.forEach(properties::remove);
    }
}
