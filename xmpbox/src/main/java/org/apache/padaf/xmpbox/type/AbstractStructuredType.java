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

public abstract class AbstractStructuredType extends AbstractComplexProperty {

	
	protected static final  String STRUCTURE_ARRAY_NAME = "li"; 

	public AbstractStructuredType(XMPMetadata metadata, String namespaceURI,
			String fieldPrefix) {
		super(metadata, namespaceURI, fieldPrefix, STRUCTURE_ARRAY_NAME);
	}

	protected void addSimpleProperty (String propertyName, Object value) {
		TypeMapping tm = getMetadata().getTypeMapping();
		AbstractSimpleProperty asp = tm.instanciateSimpleField(getClass(), null,getPrefix(),propertyName, value);
		addProperty(asp);
	}

	protected String getPropertyValueAsString (String fieldName) {
		AbstractSimpleProperty absProp = (AbstractSimpleProperty)getProperty(fieldName);
		if (absProp == null) {
			return null;
		} else {
			return absProp.getStringValue();
		}
	}

	protected Calendar getDatePropertyAsCalendar(String fieldName) {
		DateType absProp = (DateType)getFirstEquivalentProperty(fieldName,DateType.class);
		if (absProp != null) {
			return absProp.getValue();
		} else {
			return null;
		}
	}


}
