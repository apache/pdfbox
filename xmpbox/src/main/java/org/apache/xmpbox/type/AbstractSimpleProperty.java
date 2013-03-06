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

import org.apache.xmpbox.XMPMetadata;

/**
 * Abstract Class of an Simple XMP Property
 * 
 * @author a183132
 * 
 */
public abstract class AbstractSimpleProperty extends AbstractField
{

    private String namespace;

    private String prefix;

    /**
     * Property specific type constructor (namespaceURI is given)
     * 
     * @param metadata
     *            The metadata to attach to this property
     * @param namespaceURI
     *            the specified namespace URI associated to this property
     * @param prefix
     *            The prefix to set for this property
     * @param propertyName
     *            The local Name of this property
     * @param value
     *            the value to give
     */
    public AbstractSimpleProperty(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName,
            Object value)
    {
        super(metadata, propertyName);
        setValue(value);
        this.namespace = namespaceURI;
        this.prefix = prefix;

    }

    /**
     * Check and set new property value (in Element and in its Object Representation)
     * 
     * @param value
     *            Object value to set
     */
    public abstract void setValue(Object value);

    /**
     * Return the property value
     * 
     * @return a string
     */
    public abstract String getStringValue();

    public abstract Object getValue();

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(this.getClass().getSimpleName()).append(":");
        sb.append(getStringValue()).append("]");
        return sb.toString();
    }

    /**
     * Get the namespace URI of this entity
     * 
     * @return the namespace URI
     */
    public final String getNamespace()
    {
        return namespace;
    }

    /**
     * Get the prefix of this entity
     * 
     * @return the prefix specified
     */
    public String getPrefix()
    {
        return prefix;
    }

}
