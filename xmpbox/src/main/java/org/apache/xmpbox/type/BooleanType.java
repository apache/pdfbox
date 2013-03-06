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
 * Object representation of an Boolean XMP type
 * 
 * @author a183132
 * 
 */
public class BooleanType extends AbstractSimpleProperty
{

    public static final String TRUE = "True";

    public static final String FALSE = "False";

    private boolean booleanValue;

    /**
     * Property Boolean type constructor (namespaceURI is given)
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
     *            the value to give
     */
    public BooleanType(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName, Object value)
    {
        super(metadata, namespaceURI, prefix, propertyName, value);
    }

    /**
     * return the property value
     * 
     * @return boolean the property value
     */
    public Boolean getValue()
    {
        return booleanValue;
    }

    /**
     * Set value of this property BooleanTypeObject accept String value or a boolean
     * 
     * @param value
     *            The value to set
     * 
     */
    public void setValue(Object value)
    {
        if (value instanceof Boolean)
        {
            booleanValue = ((Boolean) value).booleanValue();
        }
        else if (value instanceof String)
        {
            // NumberFormatException is thrown (sub of InvalidArgumentException)
            String s = value.toString().trim().toUpperCase();
            if ("TRUE".equals(s))
            {
                booleanValue = true;
            }
            else if ("FALSE".equals(s))
            {
                booleanValue = false;
            }
            else
            {
                // unknown value
                throw new IllegalArgumentException("Not a valid boolean value : '" + value + "'");
            }
        }
        else
        {
            // invalid type of value
            throw new IllegalArgumentException("Value given is not allowed for the Boolean type.");
        }
    }

    @Override
    public String getStringValue()
    {
        return booleanValue ? TRUE : FALSE;
    }

}
