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
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_BODY;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class AcroFormValidationProcess extends AbstractProcess
{

    @Override
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
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There is no Catalog entry in the Document."));
        }
    }

    /**
     * This method checks if the NeedAppearances entry is present. If it is, the value must be false.
     * 
     * If the entry is invalid, the ERROR_SYNTAX_DICT_INVALID (1.2.3) error is return.
     * 
     * @param ctx the preflight context.
     * @param acroForm the AcroForm.
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
     * This function explores all fields and their children to validate them.
     * 
     * @see #validateField(PreflightContext, PDField) 
     * 
     * @param ctx the preflight context.
     * @param lFields the list of fields, can be null.
     * @return the result of the validation.
     * @throws IOException
     */
    protected boolean exploreFields(PreflightContext ctx, List<?> lFields) throws IOException
    {
        if (lFields != null)
        {
            // the list can be null if the Field doesn't have children
            for (Object obj : lFields)
            {
                if (obj instanceof PDFieldTreeNode)
                {
                    if (!validateField(ctx, (PDFieldTreeNode) obj))
                    {
                        return false;
                    }
                }
                else if (obj instanceof PDAnnotationWidget)
                {
                    // "A field?s children in the hierarchy may also include widget annotations"
                    ContextHelper.validateElement(ctx, ((PDAnnotationWidget) obj).getDictionary(), ANNOTATIONS_PROCESS);
                }
                else
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_BODY,
                            "Field can only have fields or widget annotations as KIDS"));
                }
            }
        }
        return true;
    }

    /**
     * A and AA field are forbidden, this method checks if they are present and checks all children of this field. If the
     * an Additional Action is present the error code ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD (6.2.3) is added
     * to the error list If the an Action is present (in the Widget Annotation) the error
     * ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD (6.2.4) is added to the error list. (Remark : The widget validation
     * will be done by the AnnotationValidationHelper, but some actions are authorized in a standard Widget)
     * 
     * @param ctx the preflight context.
     * @param aField an acro forms field.
     * @return the result of the check for A or AA entries.
     * @throws IOException
     */
    protected boolean validateField(PreflightContext ctx, PDFieldTreeNode aField) throws IOException
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
