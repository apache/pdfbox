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
import org.apache.padaf.xmpbox.schema.XMPSchema;

public class VersionType extends ComplexPropertyContainer {

	public static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/sType/Version#";

	public static final String PREFERRED_PREFIX = "stVer";
	
	protected XMPMetadata metadata;
	
	public static final String COMMENTS = "comments";

	public static final String EVENT = "event";

	public static final String MODIFIER = "modifier";
	
	public static final String MODIFY_DATE = "modifyDate";
	
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
	public VersionType(XMPMetadata metadata, String namespace, String prefix,
			String propertyName) {
		super(metadata, namespace, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(XMPSchema.NS_NAMESPACE, "xmlns", PREFERRED_PREFIX, ELEMENT_NS));
	}
	
	
	public String getComments() {
		TextType absProp = (TextType)getFirstEquivalentProperty(COMMENTS,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setComments (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, COMMENTS, value));
	}

	public ResourceEventType getEvent () {
//		ResourceEventType event = (ResourceEventType)getPropertiesByLocalName(EVENT);
		return (ResourceEventType)getFirstEquivalentProperty(EVENT,ResourceEventType.class);
	}

	public void setEvent (ResourceEventType value) {
		this.addProperty(value);
	}

	public Calendar getModifyDate () {
		DateType absProp = (DateType)getFirstEquivalentProperty(MODIFY_DATE,DateType.class);
		if (absProp != null) {
			return absProp.getValue();
		} else {
			return null;
		}
	}

	public void setModifyDate (Calendar value) {
		this.addProperty(new DateType(metadata, PREFERRED_PREFIX, MODIFY_DATE, value));
	}

	public String getVersion () {
		TextType absProp = (TextType)getFirstEquivalentProperty(VERSION,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setVersion (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, VERSION, value));
	}

	public String getModifier () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MODIFIER,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setModifier (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MODIFIER, value));
	}


	
}
