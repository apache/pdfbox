/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.util.appearance;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.interactive.form.PDAppearanceString;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * (Re-) Generate the appearance for a field.
 * 
 * The fields appearance defines the 'look' the field has when it's rendered
 * for display or printing.
 * 
 */

public class AppearanceGenerator {
	
	private static final Log LOG = LogFactory.getLog(AppearanceGenerator.class);
	
	private AppearanceGenerator() {
	}
	
	
	/**
	 * Generate the appearances for a single field.
	 * 
	 * @param field The field which appearances need to be generated.
	 */
	public static void generateFieldAppearances(PDField field) {
		// TODO: handle appearance generation for other field types
		if (field instanceof PDVariableText) {
			
			PDAppearanceString pdAppearance = new PDAppearanceString(field.getAcroForm(), (PDVariableText) field);
			
			Object fieldValue = field.getValue();
			
			// in case there is no value being set generate the visual 
			// appearance with an empty String
			if (fieldValue == null) {
				fieldValue = "";
			}
			
			// TODO: implement the handling for additional values.
			if (fieldValue instanceof String) {
				try {
					pdAppearance.setAppearanceValue((String) fieldValue);
				} catch (IOException e) {
					LOG.error("Unable to generate the field appearance.", e);
				}
			} else {
				LOG.warn("Can't generate the appearance for values typed " + fieldValue.getClass().getName() + ".");
			}
		}
	}
}
