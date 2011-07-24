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

import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;


/**
 * Object representation of an Thumbnail XMP type
 * 
 * @author eric
 */
public class ThumbnailType extends ComplexPropertyContainer {
	protected static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/g/img/";
	protected XMPMetadata metadata;

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
	public ThumbnailType(XMPMetadata metadata, String namespace, String prefix,
			String propertyName) {
		super(metadata, namespace, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}

	/**
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this thumbnail type
	 */
	public ThumbnailType(XMPMetadata metadata, String prefix,
			String propertyName) {
		super(metadata, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}

	/**
	 * Give the first property found in this container with type and localname
	 * expected
	 * 
	 * @param localName
	 *            the localname of property wanted
	 * @param type
	 *            the property type of property wanted
	 * @return the property wanted
	 */
	protected AbstractField getFirstEquivalentProperty(String localName,
			Class<? extends AbstractField> type) {
		List<AbstractField> list = getPropertiesByLocalName(localName);
		if (list != null) {
			for (AbstractField abstractField : list) {
				// System.out.println(abstractField.getQualifiedName());
				if (abstractField.getClass().equals(type)) {
					return abstractField;
				}
			}
		}
		return null;
	}

	/**
	 * Get Height
	 * 
	 * @return the height
	 */
	public Integer getHeight() {
		AbstractField absProp = getFirstEquivalentProperty("height",
				IntegerType.class);
		if (absProp != null) {
			return ((IntegerType) absProp).getValue();
		}
		return null;
	}

	/**
	 * Set Height
	 * 
	 * @param prefix
	 *            the prefix of Height property to set
	 * @param name
	 *            the name of Height property to set
	 * @param height
	 *            the value of Height property to set
	 */
	public void setHeight(String prefix, String name, Integer height) {
		this.addProperty(new IntegerType(metadata, prefix, name, height));
	}

	/**
	 * Get Width
	 * 
	 * @return the width
	 */
	public Integer getWidth() {
		AbstractField absProp = getFirstEquivalentProperty("width",
				IntegerType.class);
		if (absProp != null) {

			return ((IntegerType) absProp).getValue();
		}
		return null;
	}

	/**
	 * Set Width
	 * 
	 * @param prefix
	 *            the prefix of width property to set
	 * @param name
	 *            the name of width property to set
	 * @param width
	 *            the value of width property to set
	 */
	public void setWidth(String prefix, String name, Integer width) {
		this.addProperty(new IntegerType(metadata, prefix, name, width));
	}

	/**
	 * Get The img data
	 * 
	 * @return the img
	 */
	public String getImg() {
		AbstractField absProp = getFirstEquivalentProperty("image",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set Image data
	 * 
	 * @param prefix
	 *            the prefix of image property to set
	 * @param name
	 *            the name of image property to set
	 * @param image
	 *            the value of image property to set
	 */
	public void setImg(String prefix, String name, String image) {
		this.addProperty(new TextType(metadata, prefix, name, image));
	}

	/**
	 * Get Format
	 * 
	 * @return the format
	 */
	public String getFormat() {
		AbstractField absProp = getFirstEquivalentProperty("format",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set Format
	 * 
	 * @param prefix
	 *            the prefix of format property to set
	 * @param name
	 *            the name of format property to set
	 * @param format
	 *            the value of format property to set
	 */
	public void setFormat(String prefix, String name, String format) {
		this.addProperty(new TextType(metadata, prefix, name, format));
	}

}
