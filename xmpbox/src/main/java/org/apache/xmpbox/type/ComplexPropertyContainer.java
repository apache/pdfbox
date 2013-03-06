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
import java.util.Iterator;
import java.util.List;

/**
 * Object representation for arrays content This Class could be used to define directly a property with more than one
 * field (structure) and also schemas
 * 
 * @author a183132
 * 
 */
public class ComplexPropertyContainer /* extends AbstractField */
{

    private List<AbstractField> properties;

    // private Map<String, Attribute> attributes;

    /**
     * Complex Property type constructor (namespaceURI is given)
     * 
     * @param metadata
     *            The metadata to attach to this property
     * @param namespaceURI
     *            The namespace URI to associate to this property
     * @param prefix
     *            The prefix to set for this property
     * @param propertyName
     *            The local Name of this property
     */
    public ComplexPropertyContainer()
    {
        properties = new ArrayList<AbstractField>();
        // attributes = new HashMap<String, Attribute>();
    }

    // /**
    // * Get an attribute with its name in this entity
    // *
    // * @param qualifiedName
    // * the full qualified name of the attribute wanted
    // * @return The attribute property
    // */
    // public Attribute getAttribute(String qualifiedName) {
    // return attributes.get(qualifiedName);
    // }

    // /**
    // * Get attributes list defined for this entity
    // *
    // * @return Attributes list
    // */
    // public List<Attribute> getAllAttributes() {
    // return new ArrayList<Attribute>(attributes.values());
    // }

    // /**
    // * Set a new attribute for this entity
    // *
    // * @param value
    // * The Attribute property to add
    // */
    // public void setAttribute(Attribute value) {
    // if (attributes.containsKey(value.getQualifiedName())) {
    // // if same name in element, attribute will be replaced
    // attributes.remove(value.getQualifiedName());
    // }
    // if (value.getNamespace() == null) {
    // attributes.put(value.getQualifiedName(), value);
    // } else {
    // attributes.put(value.getQualifiedName(), value);
    // }
    // }

    // /**
    // * Remove an attribute of this entity
    // *
    // * @param qualifiedName
    // * the full qualified name of the attribute wanted
    // */
    // public void removeAttribute(String qualifiedName) {
    // if (containsAttribute(qualifiedName)) {
    // attributes.remove(qualifiedName);
    // }
    //
    // }

    // /**
    // * Check if an attribute is declared for this entity
    // *
    // * @param qualifiedName
    // * the full qualified name of the attribute concerned
    // * @return true if attribute is present
    // */
    // public boolean containsAttribute(String qualifiedName) {
    // return attributes.containsKey(qualifiedName);
    // }

    /**
     * Give the first property found in this container with type and localname expected
     * 
     * @param localName
     *            the localname of property wanted
     * @param type
     *            the property type of property wanted
     * @return the property wanted
     */
    protected AbstractField getFirstEquivalentProperty(String localName, Class<? extends AbstractField> type)
    {
        List<AbstractField> list = getPropertiesByLocalName(localName);
        if (list != null)
        {
            for (AbstractField abstractField : list)
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
    public void addProperty(AbstractField obj)
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
     * @return All Properties contained in this container
     */
    public List<AbstractField> getAllProperties()
    {
        return properties;
    }

    /**
     * Return all properties with this specified localName
     * 
     * @param localName
     *            the local name wanted
     * @return All properties with local name which match with localName given
     */
    public List<AbstractField> getPropertiesByLocalName(String localName)
    {
        List<AbstractField> absFields = getAllProperties();
        if (absFields != null)
        {
            List<AbstractField> list = new ArrayList<AbstractField>();
            for (AbstractField abstractField : absFields)
            {
                if (abstractField.getPropertyName().equals(localName))
                {
                    list.add(abstractField);
                }
            }
            if (list.size() == 0)
            {
                return null;
            }
            else
            {
                return list;
            }
        }
        return null;

    }

    /**
     * Check if two property are similar
     * 
     * @param prop1
     *            First property
     * @param prop2
     *            Second property
     * @return True if these properties are equals
     */
    public boolean isSameProperty(AbstractField prop1, AbstractField prop2)
    {

        if (prop1.getClass().equals(prop2.getClass()))
        {
            String pn1 = prop1.getPropertyName();
            String pn2 = prop2.getPropertyName();
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
    public boolean containsProperty(AbstractField property)
    {
        Iterator<AbstractField> it = getAllProperties().iterator();
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
    public void removeProperty(AbstractField property)
    {
        if (containsProperty(property))
        {
            properties.remove(property);
        }
    }

}
