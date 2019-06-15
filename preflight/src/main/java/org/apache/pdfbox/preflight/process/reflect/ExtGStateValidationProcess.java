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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.pdfbox.preflight.PreflightConfiguration.FONT_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_CA;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_COMMON;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NUMERIC_RANGE;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_NEGATIVE_FLOAT;
import static org.apache.pdfbox.preflight.PreflightConstants.MAX_POSITIVE_FLOAT;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.AbstractProcess;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class ExtGStateValidationProcess extends AbstractProcess
{

    /**
     * Validate the ExtGState dictionaries.
     * 
     * @param context the context which contains the Resource dictionary.
     * @throws ValidationException thrown if an Extended Graphic State isn't valid.
     */
    @Override
    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty())
        {
            return;
        }
        
        if (!vPath.isExpectedType(COSDictionary.class)) 
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_XOBJECT_INVALID_TYPE, "ExtGState validation required at least a Resource dictionary"));
        }
        else
        {
            COSDictionary extGStatesDict = (COSDictionary) vPath.peek();
            List<COSDictionary> listOfExtGState = extractExtGStateDictionaries(extGStatesDict);
            validateTransparencyRules(context, listOfExtGState);
            validateFonts(context, listOfExtGState);
        }
    }

    /**
     * Create a list of ExtGState dictionaries using the given Resource dictionary and the COSDocument.
     * 
     * @param extGStates a resource COSDictionary.
     * @return the list of ExtGState dictionaries.
     * @throws ValidationException thrown if an Extended Graphic State isn't valid.
     */
    public List<COSDictionary> extractExtGStateDictionaries(
            COSDictionary extGStates)
            throws ValidationException
    {
        List<COSDictionary> listOfExtGState = new ArrayList<>(0);
        if (extGStates != null)
        {
            for (COSName key : extGStates.keySet())
            {
                COSDictionary gsDict = extGStates.getCOSDictionary(key);
                if (gsDict == null)
                {
                    throw new ValidationException("The Extended Graphics State dictionary is invalid");
                }
                listOfExtGState.add(gsDict);
            }
        }
        return listOfExtGState;
    }

    /**
     * Validate transparency rules in all ExtGState dictionaries of this container.
     * 
     * @param context the preflight context.
     * @param listOfExtGState a list of ExtGState COSDictionaries.
     * 
     */
    protected void validateTransparencyRules(PreflightContext context, List<COSDictionary> listOfExtGState)
    {
        for (COSDictionary egs : listOfExtGState)
        {
            checkSoftMask(context, egs);
            checkUpperCA(context, egs);
            checkLowerCA(context, egs);
            checkBlendMode(context, egs);
            checkTRKey(context, egs);
            checkTR2Key(context, egs);
        }
    }

    /**
     * Validate fonts in all ExtGState dictionaries of this container.
     * 
     * @param context the preflight context.
     * @param listOfExtGState a list of ExtGState COSDictionaries.
     * @throws ValidationException
     * 
     */
    protected void validateFonts(PreflightContext context, List<COSDictionary> listOfExtGState)
            throws ValidationException
    {
        for (COSDictionary egs : listOfExtGState)
        {
            checkFont(context, egs);
        }
    }

    /**
     * This method checks a Font array in the ExtGState dictionary.
     * 
     * @param context the preflight context.
     * @param egs the Graphic state to check
     * @throws ValidationException
     */
    private void checkFont(PreflightContext context, COSDictionary egs) throws ValidationException
    {
        COSBase base = egs.getItem(COSName.FONT);
        if (base == null)
        {
            return;
        }
        if (!(base instanceof COSArray) || ((COSArray) base).size() != 2)
        {
            context.addValidationError(new ValidationError(ERROR_SYNTAX_COMMON,
                    "/Font entry in /ExtGState must be an array with 2 elements"));
            return;
        }
        COSArray ar = (COSArray) base;
        COSBase base0 = ar.get(0);
        if (!(base0 instanceof COSObject))
        {
            context.addValidationError(new ValidationError(ERROR_SYNTAX_COMMON,
                    "1st element in /Font entry in /ExtGState must be an indirect object"));
            return;
        }
        COSBase base1 = ar.getObject(1);
        if (!(base1 instanceof COSNumber))
        {
            context.addValidationError(new ValidationError(ERROR_SYNTAX_COMMON,
                    "2nd element in /Font entry in /ExtGState must be a number"));
            return;
        }
        COSNumber fontSize = (COSNumber) ar.getObject(1);
        if (fontSize.floatValue() > MAX_POSITIVE_FLOAT || fontSize.floatValue() < MAX_NEGATIVE_FLOAT)
        {
            context.addValidationError(new ValidationError(ERROR_SYNTAX_NUMERIC_RANGE,
                    "invalid float range in 2nd element in /Font entry in /ExtGState"));
        }
        if (ar.getObject(0) instanceof COSDictionary)
        {
            COSDictionary fontDict = (COSDictionary) ar.getObject(0);
            try
            {
                PDFont newFont = PDFontFactory.createFont(fontDict);
                ContextHelper.validateElement(context, newFont, FONT_PROCESS);
            }
            catch (IOException e)
            {
                addFontError(fontDict, context, e);
            }
        }

    }

    /**
     * This method checks the SMask value of the ExtGState dictionary. The Soft Mask is optional but must be "None" if
     * it is present.
     * 
     * @param context the preflight context.
     * @param egs the Graphic state to check
     */
    private void checkSoftMask(PreflightContext context, COSDictionary egs)
    {
        COSBase smVal = egs.getDictionaryObject(COSName.SMASK);
        if (smVal != null && 
                !(smVal instanceof COSName && COSName.NONE.equals(smVal)))
        {
            // ---- Soft Mask is valid only if it is a COSName equals to None
            context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK,
                    "SoftMask must be null or None"));
        }
    }

    /**
     * This method checks the BM value of the ExtGState dictionary. The Blend Mode is optional but must be "Normal" or
     * "Compatible" if it is present.
     * 
     * @param context the preflight context     * @param egs the graphic state to check
     */
    private void checkBlendMode(PreflightContext context, COSDictionary egs)
    {
        COSName bmVal = egs.getCOSName(COSName.BM);
        // ---- Blend Mode is valid only if it is equals to Normal or Compatible
        if (bmVal != null && !(COSName.NORMAL.equals(bmVal) || COSName.COMPATIBLE.equals(bmVal)))
        {
            context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE,
                    "BlendMode value isn't valid (only Normal and Compatible are authorized)"));
        }
    }

    /**
     * This method checks the "CA" value of the ExtGState dictionary. It is optional but must be 1.0
     * if present.
     *
     * @param context the preflight context.
     * @param egs the graphic state to check
     */
    private void checkUpperCA(PreflightContext context, COSDictionary egs)
    {
        COSBase uCA = egs.getDictionaryObject(COSName.CA);
        if (uCA != null)
        {
            // ---- If CA is present only the value 1.0 is authorized
            Float fca = uCA instanceof COSFloat ? ((COSFloat) uCA).floatValue() : null;
            Integer ica = uCA instanceof COSInteger ? ((COSInteger) uCA).intValue() : null;
            if (!(fca != null && Float.compare(fca, 1.0f) == 0) && !(ica != null && ica == 1))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,
                        "CA entry in a ExtGState is invalid"));
            }
        }
    }

    /**
     * This method checks the "ca" value of the ExtGState dictionary. It is optional but must be 1.0
     * if present.
     *
     * @param context the preflight context.
     * @param egs the graphic state to check
     */
    private void checkLowerCA(PreflightContext context, COSDictionary egs)
    {
        COSBase lCA = egs.getDictionaryObject(COSName.CA_NS);
        if (lCA != null)
        {
            // ---- If ca is present only the value 1.0 is authorized
            Float fca = lCA instanceof COSFloat ? ((COSFloat) lCA).floatValue() : null;
            Integer ica = lCA instanceof COSInteger ? ((COSInteger) lCA).intValue() : null;
            if (!(fca != null && Float.compare(fca, 1.0f) == 0) && !(ica != null && ica == 1))
            {
                context.addValidationError(new ValidationError(ERROR_TRANSPARENCY_EXT_GS_CA,
                        "ca entry in a ExtGState is invalid"));
            }
        }
    }

    /**
     * Check the TR entry. A valid ExtGState hasn't TR entry.
     * 
     * @param context the preflight context
     * @param egs the graphic state to check
     */
    protected void checkTRKey(PreflightContext context, COSDictionary egs)
    {
        if (egs.getItem(COSName.TR) != null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY,
                    "No TR key expected in Extended graphics state"));
        }
    }

    /**
     * Check the TR2 entry. A valid ExtGState hasn't TR2 entry or a TR2 entry equals to "default".
     * 
     * @param context the preflight context
     * @param egs the graphic state to check
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
}
