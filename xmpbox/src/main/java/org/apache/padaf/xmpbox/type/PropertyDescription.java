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

/**
 * Represents one Property Description described in xml file in order to be use
 * in automatic SchemaDescriptionBulding
 * 
 * @author a183132
 * 
 */
public class PropertyDescription {
	protected String propName;
	protected String propDesc;

	/**
	 * Constructor of a propertyDescription in order to be use in automatic
	 * SchemaDescriptionBulding
	 * 
	 * @param propName
	 *            the local Name of the property to describe
	 * @param propDesc
	 *            the description of the property to describe
	 */
	public PropertyDescription(String propName, String propDesc) {
		this.propName = propName;
		this.propDesc = propDesc;
	}

	/**
	 * Get description declared
	 * 
	 * @return description declared
	 */
	public String getDescription() {
		return propDesc;
	}

	/**
	 * Get property name declared
	 * 
	 * @return property name declared
	 */
	public String getPropertyName() {
		return propName;
	}
}
