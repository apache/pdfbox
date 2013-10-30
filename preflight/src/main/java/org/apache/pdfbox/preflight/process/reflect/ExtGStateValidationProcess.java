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

package org.apache.pdfbox.preflight.process.reflect;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_CA;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_KEY_LOWER_CA;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_KEY_UPPER_CA;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL;
import static org.apache.pdfbox.preflight.PreflightConstants.TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class ExtGStateValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) {
            return;
        }
        else if (!vPath.isExpectedType(COSDictionary.class)) 
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_XOBJECT_INVALID_TYPE, "ExtGState validation required at least a Resource dictionary"));
        }
        else
        {
            COSDictionary extGStatesDict = (COSDictionary) vPath.peek();
            List<COSDictionary> listOfExtGState = extractExtGStateDictionaries(context, extGStatesDict);
            validateTransparencyRules(context, listOfExtGState);
        }
    }

    /**
     * Create a list Of ExtGState dictionaries using the given Resource dictionary and the COSDocument.
     * 
     * @param context
     *            the context which contains the Resource dictionary
     * @param resources
     *            a resource COSDictionary
     * @throws ValidationException
     *             thrown if a the Extended Graphic State isn't valid
     */
    public List<COSDictionary> extractExtGStateDictionaries(PreflightContext context, COSDictionary egsEntry)
            throws ValidationException
            {
        List<COSDictionary> listOfExtGState = new ArrayList<COSDictionary>(0);
        COSDocument cosDocument = context.getDocument().getDocument();
        COSDictionary extGStates = COSUtils.getAsDictionary(egsEntry, cosDocument);

        if (extGStates != null)
        {
            for (Object object : extGStates.keySet())
            {
                COSName key = (COSName) object;
                if (key.getName().matches(TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX))
                {
                    COSBase gsBase = extGStates.getItem(key);
                    COSDictionary gsDict = COSUtils.getAsDictionary(gsBase, cosDocument);
                    if (gsDict == null)
                    {
                        throw new ValidationException("The Extended Graphics State dictionary is invalid");
                    }
                    listOfExtGState.add(gsDict);
                }
            }
        }
        return listOfExtGState;
            }

    /**
     * Validate all ExtGState dictionaries of this container
     * 
     */
    protected void validateTransparencyRules(PreflightContext context, List<COSDictionary> listOfExtGState)
    {
        for (COSDictionary egs : listOfExtGState)
        {
            checkSoftMask(context, egs);
            checkCA(context, egs);
            checkBlendMode(context, egs);
            checkTRKey(context, egs);
            checkTR2Key(context, egs);
        }
    }

    /**
     * This method checks the SMask value of the ExtGState dictionary. The Soft Mask is optional but must be "None" if
     * it is present.
     * 
     * @param egs
     *            the Graphic state to check
     */
    private void checkSoftMask(PreflightContext context, COSDictionary egs)
    {
        COSBase smVal = egs.getItem(COSName.SMASK);
        if (smVal != null)
        {
            // ---- Soft Mask is valid only if it is a COSName equals to None
            if (!(smVal instanceof COSName && TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE.equals(((COSName) smVal)
                    .getName())))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK,
                        "SoftMask must be null or None"));
            }
        }
    }

    /**
     * This method checks the BM value of the ExtGState dictionary. The Blend Mode is optional but must be "Normal" or
     * "Compatible" if it is present.
     * 
     * @param egs
     *            the graphic state to check
     */
    private void checkBlendMode(PreflightContext context, COSDictionary egs)
    {
        COSBase bmVal = egs.getItem(TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE);
        if (bmVal != null)
        {
            // ---- Blend Mode is valid only if it is equals to Normal or Compatible
            if (!(bmVal instanceof COSName && (TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL.equals(((COSName) bmVal)
                    .getName()) || TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE.equals(((COSName) bmVal).getName()))))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE,
                        "BlendMode value isn't valid (only Normal and Compatible are authorized)"));
            }
        }
    }

    /**
     * This method checks the "CA" and "ca" values of the ExtGState dictionary. They are optional but must be 1.0 if
     * they are present.
     * 
     * @param egs
     *            the graphic state to check
     */
    private void checkCA(PreflightContext context, COSDictionary egs)
    {
        COSBase uCA = egs.getItem(TRANSPARENCY_DICTIONARY_KEY_UPPER_CA);
        COSBase lCA = egs.getItem(TRANSPARENCY_DICTIONARY_KEY_LOWER_CA);
        COSDocument cosDocument = context.getDocument().getDocument();

        if (uCA != null)
        {
            // ---- If CA is present only the value 1.0 is authorized
            Float fca = COSUtils.getAsFloat(uCA, cosDocument);
            Integer ica = COSUtils.getAsInteger(uCA, cosDocument);
            if (!(fca != null && fca == 1.0f) && !(ica != null && ica == 1))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,
                        "CA entry in a ExtGState is invalid"));
            }
        }

        if (lCA != null)
        {
            // ---- If ca is present only the value 1.0 is authorized
            Float fca = COSUtils.getAsFloat(lCA, cosDocument);
            Integer ica = COSUtils.getAsInteger(lCA, cosDocument);
            if (!(fca != null && fca == 1.0f) && !(ica != null && ica == 1))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,
                        "ca entry in a ExtGState  is invalid."));
            }
        }
    }

    /**
     * Check the TR entry. A valid ExtGState hasn't TR entry.
     * 
     * @param egs
     *            the graphic state to check
     */
    protected void checkTRKey(PreflightContext context, COSDictionary egs)
    {
        if (egs.getItem("TR") != null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY,
                    "No TR key expected in Extended graphics state"));
        }
    }

    /**
     * Check the TR2 entry. A valid ExtGState hasn't TR2 entry or a TR2 entry equals to "default".
     * 
     * @param egs
     *            the graphic state to check
     */
    protected void checkTR2Key(PreflightContext context, COSDictionary egs)
    {
        if (egs.getItem("TR2") != null)
        {
            String s = egs.getNameAsString("TR2");
            if (!"Default".equals(s))
            {
                context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                        "TR2 key only expect 'Default' value, not '" + s + "'"));
            }
        }
    }
    //
    // /**
    // * Check the RI entry of the Graphic State. If the rendering intent entry is
    // * present, the value must be one of the four values defined in the PDF
    // * reference. (@see net.awl.edoc.pdfa.validation.utils.RenderingIntents)
    // *
    // * @param egs
    // * the graphic state to check
    // * @param error
    // * the list of error to update if the validation fails.
    // * @return true if RI entry is valid, false otherwise.
    // */
    // protected boolean checkRIKey(COSDictionary egs, List<ValidationError> error) {
    // String rendenringIntent = egs.getNameAsString(COSName.getPDFName("RI"));
    // if (rendenringIntent != null && !"".equals(rendenringIntent)
    // && !RenderingIntents.contains(rendenringIntent)) {
    // error.add(new ValidationError(
    // PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
    // "Invalid rendering intent value in Extended graphics state"));
    // return false;
    // }
    // return true;
    // }
}
