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

public class ResourceEventType extends ComplexPropertyContainer {

	public static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#";

	public static final String PREFERRED_PREFIX = "stEvt";
	
	protected XMPMetadata metadata;
	
	public static final String ACTION = "action";

	public static final String CHANGED = "changed";

	public static final String INSTANCE_ID = "instanceID";
	
	public static final String PARAMETERS = "parameters";
	
	public static final String SOFTWARE_AGENT = "softwareAgent";

	public static final String WHEN = "when";
	

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
	public ResourceEventType(XMPMetadata metadata, String namespace, String prefix,
			String propertyName) {
		super(metadata, namespace, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(XMPSchema.NS_NAMESPACE, "xmlns", PREFERRED_PREFIX, ELEMENT_NS));
	}
	
	
	public String getInstanceID () {
		TextType absProp = (TextType)getFirstEquivalentProperty(INSTANCE_ID,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setInstanceID (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, INSTANCE_ID, value));
	}

	public String getSoftwareAgent () {
		TextType absProp = (TextType)getFirstEquivalentProperty(SOFTWARE_AGENT,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setSoftwareAgent (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, SOFTWARE_AGENT, value));
	}

	public Calendar getWhen () {
		DateType absProp = (DateType)getFirstEquivalentProperty(WHEN,DateType.class);
		if (absProp != null) {
			return absProp.getValue();
		} else {
			return null;
		}
	}

	public void setWhen (Calendar value) {
		this.addProperty(new DateType(metadata, PREFERRED_PREFIX, WHEN, value));
	}

	public String getAction () {
		TextType absProp = (TextType)getFirstEquivalentProperty(ACTION,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setAction (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, ACTION, value));
	}

	
	public String getChanged () {
		TextType absProp = (TextType)getFirstEquivalentProperty(CHANGED,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setChanged (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, CHANGED, value));
	}

	public String getParameters () {
		TextType absProp = (TextType)getFirstEquivalentProperty(PARAMETERS,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setParameters (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, PARAMETERS, value));
	}



	
}
