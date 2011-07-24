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

package org.apache.padaf.preflight.annotation;

import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_INK;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE;
import static org.apache.padaf.preflight.ValidationConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET;
import static org.apache.padaf.preflight.ValidationConstants.DICTIONARY_KEY_SUBTYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ANNOT_FORBIDDEN_SUBTYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ANNOT_MISSING_SUBTYPE;

import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * Factory to instantiate AnnotationValidator for a PDF/A-1b valdiation.
 *
 */
public class PDFAbAnnotationFactory extends AnnotationValidatorFactory {

	/**
	 * Return an instance of AnnotationValidator if the annotation subtype is
	 * authorized for a PDF/A. Otherwise, returns null and the given list is
	 * updated with the right error code.
	 * 
	 * If the subtype isn't mentioned in the PDF/A specification and if it doesn't
	 * exist in the PDF Reference 1.4, it will be considered as an invalid
	 * annotation. Here is the list of Annotations which appear after the PDF 1.4
	 * :
	 * <UL>
	 * <li>Polygon (1.5)
	 * <li>Polyline (1.5)
	 * <li>Caret (1.5)
	 * <li>Screen (1.5)
	 * <li>Watermark (1.6)
	 * <li>3D (1.6)
	 * </UL>
	 * 
	 * @param annotDic
	 * @param handler
	 * @param errors
	 * @return
	 */
	@Override
	public AnnotationValidator instantiateAnnotationValidator(COSDictionary annotDic,
			DocumentHandler handler, List<ValidationError> errors) {
		AnnotationValidator av = null;

		String subtype = annotDic.getNameAsString(COSName.getPDFName(DICTIONARY_KEY_SUBTYPE));

		if (subtype == null || "".equals(subtype)) {
			errors.add(new ValidationError(ERROR_ANNOT_MISSING_SUBTYPE));
		} else {
			if (ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT.equals(subtype)) {
				av = new TextAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK.equals(subtype)) {
				av = new LinkAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT.equals(subtype)) {
				av = new FreeTextAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE.equals(subtype)) {
				av = new LineAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE.equals(subtype)
					|| ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE.equals(subtype)) {
				av = new SquareCircleAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT.equals(subtype)
					|| ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE.equals(subtype)
					|| ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT.equals(subtype)
					|| ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY.equals(subtype)) {
				av = new MarkupAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP.equals(subtype)) {
				av = new RubberStampAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_INK.equals(subtype)) {
				av = new InkAnnotationValdiator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP.equals(subtype)) {
				av = new PopupAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET.equals(subtype)) {
				av = new WidgetAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK.equals(subtype)) {
				av = new PrintMarkAnnotationValidator(handler, annotDic);
			} else if (ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET.equals(subtype)) {
				av = new TrapNetAnnotationValidator(handler, annotDic);
			} else {
				errors.add(new ValidationError(ERROR_ANNOT_FORBIDDEN_SUBTYPE,
						"The subtype isn't authorized : " + subtype));
			}
		}

		if ( av != null ) {
			// initialize the factory if the Validator has been created
			av.setFactory(this);
		}

		return av;
	}
}