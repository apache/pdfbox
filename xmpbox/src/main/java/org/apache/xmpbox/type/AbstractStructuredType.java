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

import java.util.Calendar;

import org.apache.xmpbox.XMPMetadata;

public abstract class AbstractStructuredType extends AbstractComplexProperty
{

    protected static final String STRUCTURE_ARRAY_NAME = "li";

    private String namespace;

    private String preferedPrefix;

    private String prefix;

    public AbstractStructuredType(XMPMetadata metadata)
    {
        this(metadata, null, null, null);
    }

    public AbstractStructuredType(XMPMetadata metadata, String namespaceURI)
    {
        this(metadata, namespaceURI, null, null);
        StructuredType st = this.getClass().getAnnotation(StructuredType.class);
        if (st != null)
        {
            // init with annotation
            this.namespace = st.namespace();
            this.preferedPrefix = st.preferedPrefix();
        }
        else
        {
            throw new IllegalArgumentException(" StructuredType annotation cannot be null");
        }
        this.prefix = this.preferedPrefix;
    }

    public AbstractStructuredType(XMPMetadata metadata, String namespaceURI, String fieldPrefix, String propertyName)
    {
        super(metadata, propertyName);
        StructuredType st = this.getClass().getAnnotation(StructuredType.class);
        if (st != null)
        {
            // init with annotation
            this.namespace = st.namespace();
            this.preferedPrefix = st.preferedPrefix();
        }
        else
        {
            // init with parameters
            if (namespaceURI == null)
            {
                throw new IllegalArgumentException(
                        "Both StructuredType annotation and namespace parameter cannot be null");
            }
            this.namespace = namespaceURI;
            this.preferedPrefix = fieldPrefix;
        }
        this.prefix = fieldPrefix == null ? this.preferedPrefix : fieldPrefix;
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

    public final void setNamespace(String ns)
    {
        this.namespace = ns;
    }

    /**
     * Get the prefix of this entity
     * 
     * @return the prefix specified
     */
    public final String getPrefix()
    {
        return prefix;
    }

    public final void setPrefix(String pf)
    {
        this.prefix = pf;
    }

    public final String getPreferedPrefix()
    {
        return preferedPrefix;
    }

    protected void addSimpleProperty(String propertyName, Object value)
    {
        TypeMapping tm = getMetadata().getTypeMapping();
        AbstractSimpleProperty asp = tm.instanciateSimpleField(getClass(), null, getPrefix(), propertyName, value);
        addProperty(asp);
    }

    protected String getPropertyValueAsString(String fieldName)
    {
        AbstractSimpleProperty absProp = (AbstractSimpleProperty) getProperty(fieldName);
        if (absProp == null)
        {
            return null;
        }
        else
        {
            return absProp.getStringValue();
        }
    }

    protected Calendar getDatePropertyAsCalendar(String fieldName)
    {
        DateType absProp = (DateType) getFirstEquivalentProperty(fieldName, DateType.class);
        if (absProp != null)
        {
            return absProp.getValue();
        }
        else
        {
            return null;
        }
    }

    public TextType createTextType(String propertyName, String value)
    {
        return getMetadata().getTypeMapping().createText(getNamespace(), getPrefix(), propertyName, value);
    }

    public ArrayProperty createArrayProperty(String propertyName, Cardinality type)
    {
        return getMetadata().getTypeMapping().createArrayProperty(getNamespace(), getPrefix(), propertyName, type);
    }

}
