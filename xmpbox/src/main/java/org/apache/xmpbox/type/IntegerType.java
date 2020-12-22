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

import org.apache.xmpbox.XMPMetadata;

/**
 * Object representation of an Integer XMP type
 * 
 * @author a183132
 * 
 */
public class IntegerType extends AbstractSimpleProperty
{

    private int integerValue;

    /**
     * Property Integer type constructor (namespaceURI is given)
     * 
     * @param metadata
     *            The metadata to attach to this property
     * @param namespaceURI
     *            the namespace URI to associate to this property
     * @param prefix
     *            The prefix to set for this property
     * @param propertyName
     *            The local Name of this property
     * @param value
     *            The value to set
     */
    public IntegerType(final XMPMetadata metadata, final String namespaceURI, final String prefix, final String propertyName, final Object value)
    {
        super(metadata, namespaceURI, prefix, propertyName, value);

    }

    /**
     * return the property value
     * 
     * @return the property value
     */
    @Override
    public Integer getValue()
    {
        return integerValue;
    }

    /**
     * Set the property value
     * 
     * @param value
     *            The value to set
     */
    @Override
    public void setValue(final Object value)
    {
        if (value instanceof Integer)
        {
            integerValue = (Integer) value;
        }
        else if (value instanceof String)
        {
            integerValue = Integer.parseInt((String) value);
            // NumberFormatException is thrown (sub of InvalidArgumentException)
        }
        else
        {
            // invalid type of value
            throw new IllegalArgumentException("Value given is not allowed for the Integer type: " + value);
        }
    }

    @Override
    public String getStringValue()
    {
        return Integer.toString(integerValue);
    }

}
