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

@StructuredType(preferedPrefix = "stJob", namespace = "http://ns.adobe.com/xap/1.0/sType/Job#")
public class JobType extends AbstractStructuredType
{

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String ID = "id";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String NAME = "name";

    @PropertyType(type = Types.URL, card = Cardinality.Simple)
    public static final String URL = "url";

    public JobType(XMPMetadata metadata)
    {
        this(metadata, null);
    }

    public JobType(XMPMetadata metadata, String fieldPrefix)
    {
        super(metadata, fieldPrefix);
        addNamespace(getNamespace(), getPrefix());
    }

    public void setId(String id)
    {
        addSimpleProperty(ID, id);
    }

    public void setName(String name)
    {
        addSimpleProperty(NAME, name);
    }

    public void setUrl(String name)
    {
        addSimpleProperty(URL, name);
    }

    public String getId()
    {
        return getPropertyValueAsString(ID);
    }

    public String getName()
    {
        return getPropertyValueAsString(NAME);
    }

    public String getUrl()
    {
        return getPropertyValueAsString(URL);
    }

}
