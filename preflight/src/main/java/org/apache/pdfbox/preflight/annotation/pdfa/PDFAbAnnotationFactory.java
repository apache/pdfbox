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

package org.apache.pdfbox.preflight.annotation.pdfa;

import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_INK;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUIGGLY;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET;

import org.apache.pdfbox.preflight.annotation.AnnotationValidatorFactory;
import org.apache.pdfbox.preflight.annotation.CircleAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.FreeTextAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.HighlightAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.InkAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.LineAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.LinkAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.PopupAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.PrintMarkAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.RubberStampAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.SquareAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.SquigglyAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.StrikeoutAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.TextAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.TrapNetAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.UnderlineAnnotationValidator;
import org.apache.pdfbox.preflight.annotation.WidgetAnnotationValidator;


/**
 * Factory to instantiate AnnotationValidator for a PDF/A-1b validation.
 */
public class PDFAbAnnotationFactory extends AnnotationValidatorFactory
{

    @Override
    protected void initializeClasses()
    {
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT, TextAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK, LinkAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT, FreeTextAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE, LineAnnotationValidator.class);

        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE, SquareAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE, CircleAnnotationValidator.class);

        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT, HighlightAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE, UnderlineAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT, StrikeoutAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUIGGLY, SquigglyAnnotationValidator.class);

        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP, RubberStampAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_INK, InkAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP, PopupAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET, WidgetAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK, PrintMarkAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET, TrapNetAnnotationValidator.class);
    }

}
