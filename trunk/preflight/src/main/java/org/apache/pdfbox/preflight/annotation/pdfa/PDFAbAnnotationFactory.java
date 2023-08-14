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

import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK;
import static org.apache.pdfbox.preflight.PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFreeText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationInk;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquare;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquiggly;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationStrikeout;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationUnderline;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
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
        this.validatorClasses.put(PDAnnotationText.SUB_TYPE, TextAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationLink.SUB_TYPE, LinkAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationFreeText.SUB_TYPE, FreeTextAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationLine.SUB_TYPE, LineAnnotationValidator.class);

        this.validatorClasses.put(PDAnnotationSquare.SUB_TYPE, SquareAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationCircle.SUB_TYPE, CircleAnnotationValidator.class);

        this.validatorClasses.put(PDAnnotationHighlight.SUB_TYPE,
                HighlightAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationUnderline.SUB_TYPE,
                UnderlineAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationStrikeout.SUB_TYPE,
                StrikeoutAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationSquiggly.SUB_TYPE, SquigglyAnnotationValidator.class);

        this.validatorClasses.put(PDAnnotationRubberStamp.SUB_TYPE,
                RubberStampAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationInk.SUB_TYPE, InkAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationPopup.SUB_TYPE, PopupAnnotationValidator.class);
        this.validatorClasses.put(PDAnnotationWidget.SUB_TYPE, WidgetAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK, PrintMarkAnnotationValidator.class);
        this.validatorClasses.put(ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET, TrapNetAnnotationValidator.class);
    }

}
