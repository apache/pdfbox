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
 * Object representation of an Boolean XMP type
 * 
 * @author a183132
 * 
 */
public class BooleanType extends AbstractSimpleProperty {

	private static final String TRUE = "True";
	private static final String FALSE = "False";

	/**
	 * Property Boolean type constructor (namespaceURI is not given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            the value to give
	 */
	public BooleanType(XMPMetadata metadata, String prefix,
			String propertyName, Object value) {
		super(metadata, prefix, propertyName, value);
	}

	/**
	 * Property Boolean type constructor (namespaceURI is given)
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
	 *            the value to give
	 */
	public BooleanType(XMPMetadata metadata, String namespaceURI,
			String prefix, String propertyName, Object value) {
		super(metadata, namespaceURI, prefix, propertyName, value);
	}

	/**
	 * Check if object value type is compatible with this property type
	 * 
	 * @param value
	 *            Object value to check
	 * @return true if types are compatibles
	 */
	public boolean isGoodType(Object value) {
		if (value instanceof Boolean) {
			return true;
		} else if (value instanceof String) {
			return value.equals(TRUE) || value.equals(FALSE);
		}
		return false;
	}

	/**
	 * return the property value
	 * 
	 * @return boolean the property value
	 */
	public boolean getValue() {
		return (Boolean) objValue;
	}

	/**
	 * Set value of this property BooleanTypeObject accept String value or a
	 * boolean
	 * 
	 * @param value
	 *            The value to set
	 * 
	 */
	public void setValue(Object value) {
		if (!isGoodType(value)) {
			throw new IllegalArgumentException(
					"Value given is not allowed for the boolean type.");
		} else {
			// if string object
			if (value instanceof String) {
				setValueFromString((String) value);
			} else {
				// if boolean
				setValueFromBool((Boolean) value);
			}

		}
	}

	/**
	 * Set property value
	 * 
	 * @param value
	 *            the new boolean element value
	 */
	private void setValueFromBool(boolean value) {
		objValue = value;
		if (value) {
			element.setTextContent(TRUE);
		} else {
			element.setTextContent(FALSE);
		}
	}

	/**
	 * Set the value of this property
	 * 
	 * @param value
	 *            The String value to set
	 */
	private void setValueFromString(String value) {
		if (value.equals(TRUE)) {
			setValueFromBool(true);
		} else {
			setValueFromBool(false);
		}
	}

}
