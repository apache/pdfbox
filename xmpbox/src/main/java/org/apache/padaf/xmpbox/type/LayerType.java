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
import org.apache.padaf.xmpbox.XmpConstants;

public class LayerType extends AbstractStructuredType {

	public static final String PREFERED_PREFIX = "photoshop";
	
	public static final String ELEMENT_NS = "http://ns.adobe.com/photoshop/1.0/";
	
	@PropertyType(propertyType="Text")
	public static final String LAYER_NAME = "LayerName";

	@PropertyType(propertyType="Text")
	public static final String LAYER_TEXT = "LayerText";

	
	public LayerType(XMPMetadata metadata) {
		super(metadata, XmpConstants.RDF_NAMESPACE, PREFERED_PREFIX);
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}
	
	/**
	 * Get The LayerName data
	 * 
	 * @return the LayerName
	 */
	public String getLayerName() {
		AbstractField absProp = getFirstEquivalentProperty(LAYER_NAME,
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
	public void setLayerName(String image) {
		this.addProperty(new TextType(getMetadata(), null,getFieldPrefix(), LAYER_NAME, image));
	}
	
	/**
	 * Get The LayerText data
	 * 
	 * @return the LayerText
	 */
	public String getLayerText() {
		AbstractField absProp = getFirstEquivalentProperty(LAYER_TEXT,
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
	public void setLayerText(String image) {
		this.addProperty(new TextType(getMetadata(), null,getFieldPrefix(), LAYER_TEXT, image));
	}

	@Override
	public String getFieldsNamespace() {
		return ELEMENT_NS;
	}


}
