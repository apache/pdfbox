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

@StructuredType(preferedPrefix = "stVer", namespace = "http://ns.adobe.com/xap/1.0/sType/Version#")
public class VersionType extends AbstractStructuredType
{

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String COMMENTS = "comments";

    @PropertyType(type = Types.ResourceEvent, card = Cardinality.Simple)
    public static final String EVENT = "event";

    @PropertyType(type = Types.ProperName, card = Cardinality.Simple)
    public static final String MODIFIER = "modifier";

    @PropertyType(type = Types.Date, card = Cardinality.Simple)
    public static final String MODIFY_DATE = "modifyDate";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String VERSION = "version";

    /**
     * 
     * @param metadata
     *            The metadata to attach to this property
     */
    public VersionType(XMPMetadata metadata)
    {
        super(metadata);
        addNamespace(getNamespace(), getPreferedPrefix());
    }

    public String getComments()
    {
        return getPropertyValueAsString(COMMENTS);
    }

    public void setComments(String value)
    {
        addSimpleProperty(COMMENTS, value);
    }

    public ResourceEventType getEvent()
    {
        return (ResourceEventType) getFirstEquivalentProperty(EVENT, ResourceEventType.class);
    }

    public void setEvent(ResourceEventType value)
    {
        this.addProperty(value);
    }

    public Calendar getModifyDate()
    {
        return getDatePropertyAsCalendar(MODIFY_DATE);
    }

    public void setModifyDate(Calendar value)
    {
        addSimpleProperty(MODIFY_DATE, value);
    }

    public String getVersion()
    {
        return getPropertyValueAsString(VERSION);
    }

    public void setVersion(String value)
    {
        addSimpleProperty(VERSION, value);
    }

    public String getModifier()
    {
        return getPropertyValueAsString(MODIFIER);
    }

    public void setModifier(String value)
    {
        addSimpleProperty(MODIFIER, value);
    }

}
