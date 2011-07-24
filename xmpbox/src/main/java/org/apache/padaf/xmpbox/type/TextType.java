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

/**
 * Object representation of a Text XMP type
 * 
 * @author a183132
 * 
 */
public class TextType extends AbstractSimpleProperty {

	/**
	 * Property Text type constructor (namespaceURI is not given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            The value to set
	 */
	public TextType(XMPMetadata metadata, String prefix, String propertyName,
			Object value) {
		super(metadata, prefix, propertyName, value);

	}

	/**
	 * Property Text type constructor (namespaceURI is given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param namespaceURI
	 *            the namespace URI to associate to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            The value to set
	 */
	public TextType(XMPMetadata metadata, String namespaceURI, String prefix,
			String propertyName, Object value) {
		super(metadata, namespaceURI, prefix, propertyName, value);

	}

	/**
	 * Check if the value can be treated
	 * 
	 * @param value
	 *            The object to check
	 * @return True if types are compatibles
	 */
	public boolean isGoodType(Object value) {
		return value instanceof String;
	}

	/**
	 * Set the property value
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setValue(Object value) {
		if (!isGoodType(value)) {
			throw new IllegalArgumentException(
					"Value given is not allowed for the Text type : '" + value
							+ "'");
		} else {
			objValue = (String) value;
			element.setTextContent((String) value);
		}

	}

}
