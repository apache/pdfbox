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

package org.apache.pdfbox.preflight.annotation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.ContextHelper;

import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConfiguration.GRAPHIC_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_COLOR;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_FLAG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_INVALID_AP_CONTENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_INVALID_CA;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_MISSING_AP_N_CONTENT;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_MISSING_FIELDS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_DICT_INVALID;

public abstract class AnnotationValidator
{

    private AnnotationValidatorFactory annotFact = null;

    protected PreflightContext ctx = null;
    /**
     * COSDictionary of the annotation
     */
    protected COSDictionary annotDictionary = null;
    /**
     * Instance of PDAnnotation built using the annotDictionary
     */
    protected PDAnnotation pdAnnot = null;

    public AnnotationValidator(final PreflightContext context, final COSDictionary annotDictionary)
    {
        this.ctx = context;
        this.annotDictionary = annotDictionary;
    }

    /**
     * Checks if flags of the annotation are authorized.
     * <UL>
     * <li>Print flag must be 1
     * <li>NoView flag must be 0
     * <li>Hidden flag must be 0
     * <li>Invisible flag must be 0
     * </UL>
     * If one of these flags is invalid, the errors list is updated with the ERROR_ANNOT_FORBIDDEN_FLAG ValidationError
     * code.
     * 
     * @return false if a flag is invalid, true otherwise.
     */
    protected boolean checkFlags()
    {
        boolean result = this.pdAnnot.isPrinted();
        result = result && !this.pdAnnot.isHidden();
        result = result && !this.pdAnnot.isInvisible();
        result = result && !this.pdAnnot.isNoView();
        if (!result)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_FLAG, "Flags of " + pdAnnot.getSubtype()
                    + " annotation are invalid"));
        }

        return result;
    }

    /**
     * Check if the CA value is 1.0. Return true if the CA element is missing or if the value is 1.0. Return false
     * otherwise and update the list of errors present in the DocumentHandler.
     * 
     * @return true if the CA element is missing or if the value is 1.0.
     */
    protected boolean checkCA()
    {
        final COSBase ca = this.pdAnnot.getCOSObject().getDictionaryObject(COSName.CA);
        if (ca instanceof COSFloat)
        {
            final float caf = ((COSFloat) ca).floatValue();
            if (Float.compare(caf, 1.0f) != 0)
            { // ---- Only 1.0 is authorized as value
                ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_CA,
                        "CA entry is invalid. Expected 1.0 / Read " + caf));
                return false;
            }
        } // else optional field, ok
        return true;
    }

    /**
     * Return true if the C field is present in the Annotation dictionary and if the RGB profile is used in the
     * DestOutputProfile of the OutputIntent dictionary.
     * 
     * @return true if the C field is present and the RGB profile is used.
     * @throws org.apache.pdfbox.preflight.exception.ValidationException
     */
    protected boolean checkColors() throws ValidationException
    {
        if (this.pdAnnot.getColor() != null && !searchRGBProfile())
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_COLOR,
                    "Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent"));
            return false;
        }
        return true;
    }

    /**
     * Search the RGB Profile in OutputIntents dictionaries
     * 
     * @return true if a rgb profile is found, false otherwise.
     * @throws org.apache.pdfbox.preflight.exception.ValidationException
     */
    protected boolean searchRGBProfile() throws ValidationException
    {
        final ICCProfileWrapper iccpw = ICCProfileWrapper.getOrSearchICCProfile(ctx);
        if (iccpw != null)
        {
            return iccpw.isRGBColorSpace();
        }
        return false;
    }

    /**
     * This method checks the AP entry of the Annotation Dictionary. If the AP key is missing, this method returns true.
     * If the AP key exists, only the N entry is authorized and must be a Stream which define the appearance of the
     * annotation. (Currently, only the type of the N entry is checked because of the Appearance stream is a Form
     * XObject, so it will be checked by the Graphics Helper)
     * 
     * If the AP content isn't valid, this method return false and updates the errors list.
     * 
     * @return the validation state of the AP content.
     * @throws org.apache.pdfbox.preflight.exception.ValidationException
     */
    protected boolean checkAP() throws ValidationException
    {
        final PDAppearanceDictionary ap = this.pdAnnot.getAppearance();
        if (ap != null)
        {
            final COSDictionary apDict = ap.getCOSObject();
            // Only N entry is authorized
            if (apDict.getItem(COSName.D) != null || apDict.getItem(COSName.R) != null)
            {
                ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                        "Only the N Appearance is authorized"));
                return false;
            }
            else if (apDict.getItem(COSName.N) == null)
            {
                // N entry is required
                ctx.addValidationError(new ValidationError(ERROR_ANNOT_MISSING_AP_N_CONTENT,
                        "The N Appearance must be present"));
                return false;
            }
            else
            {
                final COSBase apn = apDict.getDictionaryObject(COSName.N);
                final COSName subtype = annotDictionary.getCOSName(COSName.SUBTYPE);
                final COSBase ft = getFieldType();
                if (COSName.WIDGET.equals(subtype) && COSName.BTN.equals(ft))
                {
                    // TECHNICAL CORRIGENDUM 2 for ISO 19005-1:2005 (PDF/A-1) 
                    // added a clause for Widget Annotations:
                    // the value of the N key shall be an appearance subdictionary
                    if (apn instanceof COSStream)
                    {
                        ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                                "The N Appearance of a Btn widget must not be a stream, but an appearance subdictionary"));
                        // But validate it anyway, for isartor-6-3-4-t01-fail-f.pdf
                        // Appearance stream is a XObjectForm, check it.
                        ContextHelper.validateElement(ctx, new PDFormXObject((COSStream) apn),
                                GRAPHIC_PROCESS);
                        return false;
                    }
                    if (apn instanceof COSDictionary)
                    {
                        if (this.pdAnnot.getAppearanceState() == null)
                        {
                            ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                                "The appearance state is required if the appearance contains a subdictionary"));
                            return false;
                        }
                        if (((COSDictionary) apn).size() == 0)
                        {
                            // PDFBOX-4712 / https://github.com/veraPDF/veraPDF-library/issues/900
                            ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                                "The N Appearance of a Btn widget must not be empty"));
                            return false;
                        }
                        for (COSBase val : ((COSDictionary) apn).getValues())
                        {
                            // Appearance stream is a XObjectForm, check it.
                            if (val instanceof COSObject)
                            {
                                val = ((COSObject) val).getObject();
                            }
                            ContextHelper.validateElement(ctx,
                                    new PDFormXObject((COSStream) val), GRAPHIC_PROCESS);
                        }
                    }
                    else
                    {
                        ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                                "The N Appearance must be an appearance subdictionary"));
                        return false;
                    }
                }
                else
                {
                    // the N entry must be a stream (Dictionaries are forbidden)
                    if (apn instanceof COSStream)
                    {
                        // Appearance stream is a XObjectForm, check it.
                        ContextHelper.validateElement(ctx,
                                new PDFormXObject((COSStream) apn), GRAPHIC_PROCESS);
                    }
                    else
                    {
                        ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                                "The N Appearance must be a Stream"));
                        return false;
                    }
                }
            }
        }
        else if (this.pdAnnot instanceof PDAnnotationWidget && this.pdAnnot.getAppearance() == null)
        {
            // https://www.pdfa.org/wp-content/uploads/2017/07/TechNote0010.pdf
            // "An ISO 19005-1 validator shall FAIL otherwise conforming files in which a
            //  widget annotation lacks an appearance dictionary." (page 17)
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_INVALID_AP_CONTENT,
                    "widget annotation lacks an appearance dictionary"));
            return false;
        }
        // else ok, nothing to check, this field is optional
        return true;
    }

    /**
     * Extract a list of ActionManager from the Annotation dictionary and valid them. If an action is invalid, the
     * errors list is updated and the method returns false. Otherwise, the method returns true and the errors list
     * doesn't change.
     * 
     * @return the validation state of the annotations actions.
     * @throws ValidationException
     */
    protected boolean checkActions() throws ValidationException
    {
        ContextHelper.validateElement(ctx, annotDictionary, ACTIONS_PROCESS);
        return true;
    }

    /**
     * This method validates the Popup entry. This entry shall contain an other Annotation. This annotation is validated
     * with the right AnnotationValidator.
     *
     * @return true if the popup entry is valid, false if not.
     * @throws ValidationException
     */
    protected boolean checkPopup() throws ValidationException
    {
        final COSBase cosPopup = this.annotDictionary
                .getDictionaryObject(COSName.getPDFName(PDAnnotationPopup.SUB_TYPE));
        if (cosPopup != null)
        {
            if (!(cosPopup instanceof COSDictionary))
            {
                ctx.addValidationError(new ValidationError(ERROR_SYNTAX_DICT_INVALID,
                        "An Annotation has a Popup entry, but the value is missing or isn't a dictionary"));
                return false;
            }
            final AnnotationValidator popupVal = this.annotFact.getAnnotationValidator(ctx,
                    (COSDictionary) cosPopup);
            return popupVal.validate();
        }
        return true;
    }

    /**
     * Execute validation of the Annotation dictionary.
     * 
     * @return true if validation succeed, false otherwise.
     * @throws ValidationException
     */
    public boolean validate() throws ValidationException
    {
        boolean isValid = checkMandatoryFields();
        isValid = checkFlags() && isValid;
        isValid = checkColors() && isValid;
        isValid = checkAP() && isValid;
        isValid = checkCA() && isValid;
        isValid = checkActions() && isValid;
        isValid = checkPopup() && isValid;
        return isValid;
    }

    /**
     * Checks if all mandatory fields of an annotation are present. If some fields are missing, the method returns false
     * and the errors list is updated.
     *
     * @return true if validation succeed, false otherwise.
     */
    protected boolean checkMandatoryFields()
    {
        final boolean subtype = this.annotDictionary.containsKey(COSName.SUBTYPE);
        final boolean rect = this.annotDictionary.containsKey(COSName.RECT);

        final boolean result = (subtype && rect && checkSpecificMandatoryFields());
        if (!result)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_MISSING_FIELDS, "A mandatory field for the "
                    + this.pdAnnot.getSubtype() + " annotation is missing"));
        }
        return result;
    }

    /**
     * Override this method to check the presence of specific fields
     * @return the presence of specific fields.
     */
    protected boolean checkSpecificMandatoryFields()
    {
        return true;
    }
    
    
    /**
     * Initialize the annotFact attribute of this object. This method must be called by the Factory at the creation of
     * this object. Only the Factory should call this method.
     * 
     * @param fact
     */
    public final void setFactory(final AnnotationValidatorFactory fact)
    {
        this.annotFact = fact;
    }

    private COSBase getFieldType()
    {
        COSBase ft = annotDictionary.getDictionaryObject(COSName.FT);
        COSDictionary parent = annotDictionary;
        while (ft == null)
        {
            // /FT could be in parent, so look upwards
            final COSBase parentBase = parent.getDictionaryObject(COSName.PARENT);
            if (parentBase instanceof COSDictionary)
            {
                parent = (COSDictionary) parentBase;
                ft = parent.getDictionaryObject(COSName.FT);
            }
            else
            {
                break;
            }
        }
        return ft;
    }
}
