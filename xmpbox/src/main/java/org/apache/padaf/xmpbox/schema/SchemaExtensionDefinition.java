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

package org.apache.padaf.xmpbox.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * to be used at runtime
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * Annotation to specify PDF/A extension schema description associated to a schema
 * 
 * property_descriptions design a file where all properties descriptions are described
 * this value is optional. By default, all properties descriptions will be load from
 * their PropertyExtensionDefinition annotation. 
 * However, if Property_descriptions value is changed all properties descriptions
 * will be load from this file. 
 * 
 * valueType_description must be use only to specify an XML File path to describe specific valuetypes.
 * 
 * Note: the 2 files can be created easily with XMLManagers in utils package
 * 
 */
public @interface SchemaExtensionDefinition {
	/**
	 * Get the textual description of this schema
	 * 
	 */
	String schema();

	/**
	 * Get the optional xml file path to have all textual descriptions of
	 * properties defined in this schema
	 */
	String property_descriptions() default "";

	/**
	 * Get the optional xml file path to have all textual descriptions of value
	 * types defined in this schema
	 */
	String valueType_description() default "";
}
