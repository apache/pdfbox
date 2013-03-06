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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;

/**
 * Object representation of a Complex XMP Property (Represents Ordered, Unordered and Alternative Arrays builder)
 * 
 * @author a183132
 * 
 */
public class ArrayProperty extends AbstractComplexProperty
{

    private Cardinality arrayType;

    private String namespace;

    private String prefix;

    /**
     * Contructor of a complex property
     * 
     * @param metadata
     *            The metadata to attach to this property
     * @param namespace
     *            The namespace URI to associate to this property
     * @param prefix
     *            The prefix to set for this property
     * @param propertyName
     *            The local Name of this property
     * @param type
     *            type of complexProperty (Bag, Seq, Alt)
     */
    public ArrayProperty(XMPMetadata metadata, String namespace, String prefix, String propertyName, Cardinality type)
    {
        super(metadata, propertyName);
        this.arrayType = type;
        this.namespace = namespace;
        this.prefix = prefix;
    }

    public Cardinality getArrayType()
    {
        return arrayType;
    }

    public List<String> getElementsAsString()
    {
        List<String> retval = null;
        retval = new ArrayList<String>();
        Iterator<AbstractField> it = getContainer().getAllProperties().iterator();
        AbstractSimpleProperty tmp;
        while (it.hasNext())
        {
            tmp = (AbstractSimpleProperty) it.next();
            retval.add(tmp.getStringValue());
        }
        retval = Collections.unmodifiableList(retval);
        return retval;
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
