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

import java.io.IOException;
import java.util.Calendar;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.DateConverter;


/**
 * Object representation of a Date XMP type
 * 
 * @author a183132
 * 
 */
public class DateType extends AbstractSimpleProperty {

	/**
	 * Property Date type constructor (namespaceURI is not given)
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this property
	 * @param value
	 *            The value to set for this property
	 */
	public DateType(XMPMetadata metadata, String prefix, String propertyName,
			Object value) {
		super(metadata, prefix, propertyName, value);

	}

	/**
	 * Property Date type constructor (namespaceURI is given)
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
	 *            The value to set for this property
	 */
	public DateType(XMPMetadata metadata, String namespaceURI, String prefix,
			String propertyName, Object value) {
		super(metadata, namespaceURI, prefix, propertyName, value);
	}

	/**
	 * Set property value
	 * 
	 * @param value
	 *            the new Calendar element value
	 * @throws InappropriateTypeException
	 */
	private void setValueFromCalendar(Calendar value) {
		objValue = value;
		element.setTextContent(DateConverter.toISO8601(value));

	}

	/**
	 * return the property value
	 * 
	 * @return boolean
	 */
	public Calendar getValue() {
		return (Calendar) objValue;
	}

	/**
	 * Check if the value has a type which can be understood
	 * 
	 * @param value
	 *            Object value to check
	 * @return True if types are compatibles
	 */
	public boolean isGoodType(Object value) {
		if (value instanceof Calendar) {
			return true;
		} else if (value instanceof String) {
			try {
				DateConverter.toCalendar((String) value);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Set value of this property
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setValue(Object value) {
		if (!isGoodType(value)) {
			throw new IllegalArgumentException(
					"Value given is not allowed for the Date type.");
		} else {
			// if string object
			if (value instanceof String) {
				setValueFromString((String) value);
			} else {
				// if Calendar
				setValueFromCalendar((Calendar) value);
			}

		}

	}

	/**
	 * Set the property value with a String
	 * 
	 * @param value
	 *            The String value
	 */
	private void setValueFromString(String value) {
		try {
			setValueFromCalendar(DateConverter.toCalendar((String) value));

		} catch (IOException e) {
			// SHOULD NEVER HAPPEN
			// STRING HAS BEEN CHECKED BEFORE
			throw new IllegalArgumentException(e.getCause());
		}

	}

}
