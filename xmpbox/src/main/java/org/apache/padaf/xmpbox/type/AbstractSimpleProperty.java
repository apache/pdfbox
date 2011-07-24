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
 * Abstract Class of an Simple XMP Property
 * 
 * @author a183132
 * 
 */
public abstract class AbstractSimpleProperty extends AbstractField {

	protected Object objValue;

	/**
	 * Property specific type constructor (namespaceURI is not given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            the value to give
	 * 
	 */
	public AbstractSimpleProperty(XMPMetadata metadata, String prefix,
			String propertyName, Object value) {
		super(metadata, prefix, propertyName);
		setValue(value);
	}

	/**
	 * Property specific type constructor (namespaceURI is given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param namespaceURI
	 *            the specified namespace URI associated to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            the value to give
	 */
	public AbstractSimpleProperty(XMPMetadata metadata, String namespaceURI,
			String prefix, String propertyName, Object value) {
		super(metadata, namespaceURI, prefix, propertyName);
		setValue(value);

	}

	/**
	 * Must be rewritten in each special XMP type Class to check if the value
	 * type corresponding to the XMP Type
	 * 
	 * @param value
	 *            Object to analyze
	 * @return true if object type can be treat by the property type
	 */
	public abstract boolean isGoodType(Object value);

	/**
	 * Check and set new property value (in Element and in its Object
	 * Representation)
	 * 
	 * @param value
	 *            Object value to set
	 */
	public abstract void setValue(Object value);

	/**
	 * Return the property value
	 * 
	 * @return a string
	 */
	public String getStringValue() {
		return element.getTextContent();
	}

}
