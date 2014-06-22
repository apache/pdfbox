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

@StructuredType(preferedPrefix = "stEvt", namespace = "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#")
public class ResourceEventType extends AbstractStructuredType
{

    @PropertyType(type = Types.Choice, card = Cardinality.Simple)
    public static final String ACTION = "action";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CHANGED = "changed";

    @PropertyType(type = Types.GUID, card = Cardinality.Simple)
    public static final String INSTANCE_ID = "instanceID";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String PARAMETERS = "parameters";

    @PropertyType(type = Types.AgentName, card = Cardinality.Simple)
    public static final String SOFTWARE_AGENT = "softwareAgent";

    @PropertyType(type = Types.Date, card = Cardinality.Simple)
    public static final String WHEN = "when";

    /**
     * 
     * @param metadata
     *            The metadata to attach to this property
     */
    public ResourceEventType(XMPMetadata metadata)
    {
        super(metadata);
        addNamespace(getNamespace(), getPreferedPrefix());
    }

    public String getInstanceID()
    {
        return getPropertyValueAsString(INSTANCE_ID);
    }

    public void setInstanceID(String value)
    {
        addSimpleProperty(INSTANCE_ID, value);
    }

    public String getSoftwareAgent()
    {
        return getPropertyValueAsString(SOFTWARE_AGENT);
    }

    public void setSoftwareAgent(String value)
    {
        addSimpleProperty(SOFTWARE_AGENT, value);
    }

    public Calendar getWhen()
    {
        return getDatePropertyAsCalendar(WHEN);
    }

    public void setWhen(Calendar value)
    {
        addSimpleProperty(WHEN, value);
    }

    public String getAction()
    {
        return getPropertyValueAsString(ACTION);
    }

    public void setAction(String value)
    {
        addSimpleProperty(ACTION, value);
    }

    public String getChanged()
    {
        return getPropertyValueAsString(CHANGED);
    }

    public void setChanged(String value)
    {
        addSimpleProperty(CHANGED, value);
    }

    public String getParameters()
    {
        return getPropertyValueAsString(PARAMETERS);
    }

    public void setParameters(String value)
    {
        addSimpleProperty(PARAMETERS, value);
    }

}
