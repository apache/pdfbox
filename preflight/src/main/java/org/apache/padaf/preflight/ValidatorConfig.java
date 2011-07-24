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

package org.apache.padaf.preflight;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.padaf.preflight.actions.ActionManagerFactory;
import org.apache.padaf.preflight.annotation.AnnotationValidatorFactory;
import org.apache.padaf.preflight.annotation.PDFAbAnnotationFactory;
import org.apache.padaf.preflight.helpers.AbstractValidationHelper;


/**
 * This object contains the configuration of a PdfValidator.
 * In this object, it is possible to define :
 * <ul>
 * <li> a list of priority Helpers that must be executed just after the syntactic validation
 * <li> a list of standard Helpers that must be executed after priority helpers
 * <li> a class which extends of AnnotationManagerFactory to allow the override the annotation validator creation
 * <li> a class which extends of ActionManagerFactory to allow the override the action manager creation
 * <li> a java.util.Properties object to authorize future configuration adds whitout change of the object interface.
 * </ul>
 */
public class ValidatorConfig {
	public static final String FONT_FILTER = "font-filter";
	public static final String STREAM_FILTER = "stream-filter";
	public static final String CATALOG_FILTER = "catalog-filter";
	public static final String GRAPHIC_FILTER = "graphic-filter";

	public static final String TRAILER_FILTER = "trailer-filter";
	public static final String XREF_FILTER = "xref-filter";
	public static final String BOOKMARK_FILTER = "bookmark-filter";
	public static final String ACRO_FORM_FILTER = "acro-form-filter";
	public static final String FILE_SPECIF_FILTER = "file-specification-filter";
	public static final String PAGE_FILTER = "page-filter";
	public static final String META_DATA_FILTER = "metadata-filter";

	/**
	 * Container for future properties values
	 */
	private Properties properties = new Properties();
	/**
	 * List of Helpers which have to be executed first. Helpers are called in the order ofa
	 * appearance. 
	 */
	private LinkedHashMap<String, Class<? extends AbstractValidationHelper>> priorHelpers = new LinkedHashMap<String, Class<? extends AbstractValidationHelper>>();
	/**
	 * List of Helpers which have to be executed after priorHelpers. Helpers are called in the order ofa
	 * appearance. 
	 */
	private LinkedHashMap<String, Class<? extends AbstractValidationHelper>> standHelpers = new LinkedHashMap<String, Class<? extends AbstractValidationHelper>>();
	/**
	 * Define the AnnotationFactory used by helpers
	 * Default value is PDFAbAnnotationFactory.class
	 */
	private Class<? extends AnnotationValidatorFactory> annotFact = PDFAbAnnotationFactory.class;
	/**
	 * Define the ActionManagerFactory used by helpers
	 * Default value is ActionManagerFactory.class
	 */
	private Class<? extends ActionManagerFactory> actionFact = ActionManagerFactory.class;

	public void setAnnotationFactory(Class<? extends AnnotationValidatorFactory> _annotFact) {
		this.annotFact = _annotFact;
	}

	public void setActionFactory(Class<? extends ActionManagerFactory> _actionFact) {
		this.actionFact = _actionFact;
	}

	public Class<? extends AnnotationValidatorFactory> getAnnotFact() {
		return annotFact;
	}

	public Class<? extends ActionManagerFactory> getActionFact() {
		return actionFact;
	}

	public void addProperty(Object key, Object value) {
		this.properties.put(key, value);
	}

	public Object getProperty(Object key) {
		return this.properties.get(key);
	}

	public void addPriorHelpers(String key, Class<? extends AbstractValidationHelper> filter) {
		priorHelpers.put(key, filter);
	}

	public void addStandHelpers(String key, Class<? extends AbstractValidationHelper> filter) {
		standHelpers.put(key, filter);
	}
	
	public Collection<Class<? extends AbstractValidationHelper>> getPriorHelpers() {
		return priorHelpers.values();
	}

	public Collection<Class<? extends AbstractValidationHelper>> getStandHelpers() {
		return standHelpers.values();
	}
}