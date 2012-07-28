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

package org.apache.padaf.xmpbox.type;

import java.util.Calendar;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.XmpConstants;
import org.apache.padaf.xmpbox.schema.XMPSchema;

public class VersionType extends AbstractStructuredType {

	public static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/sType/Version#";

	public static final String PREFERRED_PREFIX = "stVer";
	
	@PropertyType(propertyType="Text")
	public static final String COMMENTS = "comments";

	@PropertyType(propertyType="ResourceEvent")
	public static final String EVENT = "event";

	@PropertyType(propertyType="ProperName") 
	public static final String MODIFIER = "modifier";
	
	@PropertyType(propertyType="Date")
	public static final String MODIFY_DATE = "modifyDate";
	
	@PropertyType(propertyType="Text")
	public static final String VERSION = "version";


	/**
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param namespace
	 *            the namespace URI to associate to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this thumbnail type
	 */
	public VersionType(XMPMetadata metadata) {
		super(metadata, XmpConstants.RDF_NAMESPACE, PREFERRED_PREFIX);
		setAttribute(new Attribute(XMPSchema.NS_NAMESPACE, "xmlns", PREFERRED_PREFIX, ELEMENT_NS));
	}
	
	
	public String getComments() {
		return getPropertyValueAsString(COMMENTS);
	}

	public void setComments (String value) {
		addSimpleProperty(COMMENTS, value);
	}

	public ResourceEventType getEvent () {
		return (ResourceEventType)getFirstEquivalentProperty(EVENT,ResourceEventType.class);
	}

	public void setEvent (ResourceEventType value) {
		this.addProperty(value);
	}

	public Calendar getModifyDate () {
		return getDatePropertyAsCalendar(MODIFY_DATE);
	}

	public void setModifyDate (Calendar value) {
		addSimpleProperty(MODIFY_DATE, value);
	}

	public String getVersion () {
		return getPropertyValueAsString(VERSION);
	}

	public void setVersion (String value) {
		addSimpleProperty(VERSION, value);
	}

	public String getModifier () {
		return getPropertyValueAsString(MODIFIER);
	}

	public void setModifier (String value) {
		addSimpleProperty(MODIFIER, value);
	}

	@Override
	public String getFieldsNamespace() {
		return ELEMENT_NS;
	}


}
