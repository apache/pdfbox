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

/**
 * Simple representation of an attribute
 * 
 * @author a183132
 * 
 */
public class Attribute
{

    private String nsURI;

    private String name;

    private String value;

    /**
     * Constructor of a new Attribute
     * 
     * @param nsURI
     *            namespaceURI of this attribute (could be null)
     * @param prefix
     *            prefix of this attribute
     * @param localName
     *            localName of this attribute
     * @param value
     *            value given to this attribute
     */
    public Attribute(String nsURI, String localName, String value)
    {
        this.nsURI = nsURI;
        this.name = localName;
        this.value = value;
    }

    /**
     * Get the localName of this attribute
     * 
     * @return local name of this attribute
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the localName of this attribute
     * 
     * @param lname
     *            the local name to set
     */
    public void setName(String lname)
    {
        name = lname;
    }

    /**
     * Get the namespace URI of this attribute
     * 
     * @return the namespace URI associated to this attribute (could be null)
     */
    public String getNamespace()
    {
        return nsURI;
    }

    /**
     * Set the namespace URI of this attribute
     * 
     * @param nsURI
     *            the namespace URI to set
     */
    public void setNsURI(String nsURI)
    {
        this.nsURI = nsURI;
    }

    /**
     * Get value of this attribute
     * 
     * @return value of this attribute
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Set value of this attribute
     * 
     * @param value
     *            the value to set for this attribute
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("[attr:{").append(nsURI).append("}").append(name).append("=").append(value).append("]");
        return sb.toString();
    }

}
