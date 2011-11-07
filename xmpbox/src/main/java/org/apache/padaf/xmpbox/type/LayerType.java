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

import org.apache.padaf.xmpbox.XMPMetadata;

public class LayerType extends ComplexPropertyContainer {
	protected XMPMetadata metadata;
	
	public LayerType(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName) {
		super(metadata, namespaceURI, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}
	
	public LayerType(XMPMetadata metadata, String prefix, String propertyName) {
		super(metadata, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}
	
	/**
	 * Get The LayerName data
	 * 
	 * @return the LayerName
	 */
	public String getLayerName() {
		AbstractField absProp = getFirstEquivalentProperty("LayerName",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set LayerName 
	 * 
	 * @param prefix
	 *            the prefix of LayerName property to set
	 * @param name
	 *            the name of LayerName property to set
	 * @param image
	 *            the value of LayerName property to set
	 */
	public void setLayerName(String prefix, String name, String image) {
		this.addProperty(new TextType(metadata, prefix, name, image));
	}
	
	/**
	 * Get The LayerText data
	 * 
	 * @return the LayerText
	 */
	public String getLayerText() {
		AbstractField absProp = getFirstEquivalentProperty("LayerText",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set LayerText 
	 * 
	 * @param prefix
	 *            the prefix of LayerText property to set
	 * @param name
	 *            the name of LayerText property to set
	 * @param image
	 *            the value of LayerText property to set
	 */
	public void setLayerText(String prefix, String name, String image) {
		this.addProperty(new TextType(metadata, prefix, name, image));
	}

}
