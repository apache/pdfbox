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

import java.lang.reflect.Field;

public final class ReflectHelper {

	private ReflectHelper () {
	}

	/**
	 * Initialize the Property Mapping for a given schema
	 * 
	 * @param ns
	 *            Namespace URI
	 * @param classSchem
	 *            The class representation of the schema linked to the namespace
	 * @return Construct expected properties types representation
	 * @throws XmpSchemaException
	 *             When could not read property name in field with properties
	 *             annotations
	 */
	public static PropMapping initializePropMapping(String ns,
			Class<?> classSchem) {
		PropMapping propMap = new PropMapping(ns);
		Field [] fields = classSchem.getFields();
		String propName = null;
		for (Field field : fields) {
			if (field.isAnnotationPresent(PropertyType.class)) {
				try {
					propName = (String) field.get(propName);
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"couldn't read one type declaration, please check accessibility and declaration of fields annoted in "
									+ classSchem.getName(), e);
				}
				PropertyType propType = field.getAnnotation(PropertyType.class);
				propMap.addNewProperty(propName, propType.propertyType());
			}
		}
		return propMap;
	}


	
	
}
