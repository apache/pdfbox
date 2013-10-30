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

package org.apache.pdfbox.preflight.process;

import static org.apache.pdfbox.preflight.PreflightConfiguration.ANNOTATIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ACROFORM_DICTIONARY_KEY_NEED_APPEARANCES;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_DICT_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NOCATALOG;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class AcroFormValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocumentCatalog catalog = ctx.getDocument().getDocumentCatalog();
        if (catalog != null)
        {
            PDAcroForm acroForm = catalog.getAcroForm();
            if (acroForm != null)
            {
                checkNeedAppearences(ctx, acroForm);
                try
                {
                    exploreFields(ctx, acroForm.getFields());
                }
                catch (IOException e)
                {
                    throw new ValidationException("Unable to get the list of fields : " + e.getMessage(), e);
                }
            }
        }
        else
        {
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There are no Catalog entry in the Document."));
        }
    }

    /**
     * This method checks if the NeedAppearances entry is present. If it is, the value must be false.
     * 
     * If the entry is invalid, the ERROR_SYNTAX_DICT_INVALID (1.2.3) error is return.
     * 
     * @param ctx
     * @param acroForm
     * @param result
     */
    protected void checkNeedAppearences(PreflightContext ctx, PDAcroForm acroForm)
    {
        if (acroForm.getDictionary().getBoolean(ACROFORM_DICTIONARY_KEY_NEED_APPEARANCES, false))
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_DICT_INVALID,
                    "NeedAppearance is present with the value \"true\""));
        }
    }

    /**
     * This function explores all fields and their children to check if the A or AA entry is present.
     * 
     * @param ctx
     * @param acroForm
     * @param result
     * @throws IOException
     */
    protected boolean exploreFields(PreflightContext ctx, List<?> lFields) throws IOException
    {
        if (lFields != null)
        { // the list can be null is the Field doesn't have child
            for (Object obj : lFields)
            {
                if (!valideField(ctx, (PDField) obj))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * A and AA field are forbidden, this method checks if they are present and checks all child of this field. If the
     * an Additional Action is present the error code ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD (6.2.3) is added
     * to the error list If the an Action is present (in the Widget Annotation) the error
     * ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD (6.2.4) is added to the error list. (Remark : The widget validation
     * will be done by the AnnotationValidationHelper, but some actions are authorized in a standard Widget)
     * 
     * @param ctx
     * @param aField
     * @return
     * @throws IOException
     */
    protected boolean valideField(PreflightContext ctx, PDField aField) throws IOException
    {
        boolean res = true;
        PDFormFieldAdditionalActions aa = aField.getActions();
        if (aa != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD,
                    "\"AA\" must not be used in a Field dictionary"));
            res = false;
        }

        /*
         * The widget validation will be done by the widget annotation, a widget contained in a Field can't have action.
         */
        PDAnnotationWidget widget = aField.getWidget();
        if (res && widget != null)
        {
            ContextHelper.validateElement(ctx, widget.getDictionary(), ANNOTATIONS_PROCESS);
            COSBase act = widget.getDictionary().getDictionaryObject(COSName.A);
            if (act != null)
            {
                addValidationError(ctx, new ValidationError(ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD,
                        "\"A\" must not be used in a widget annotation"));
                res = false;
            }
        }

        res = res && exploreFields(ctx, aField.getKids());
        return res;
    }

}
