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

package org.apache.pdfbox.preflight.xobject;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_TRANSPARENCY_SMASK;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

/**
 * This class processes commons validations of XObjects.
 */
public abstract class AbstractXObjValidator implements XObjectValidator
{
    /**
     * The XObject to validate as a COSStream.
     */
    protected COSStream xobject = null;
    /**
     * The validation context which contains useful information to process validation.
     */
    protected PreflightContext context = null;

    public AbstractXObjValidator(PreflightContext context, COSStream xobj)
    {
        this.xobject = xobj;
        this.context = context;
    }

    /**
     * This method checks the SMask entry in the XObject dictionary. According to the PDF Reference, a SMask in a
     * XObject is a Stream. So if it is not null, it should be an error but a SMask with the name None is authorized in
     * the PDF/A Specification 6.4. If the validation fails (SMask not null and different from None), the error list is
     * updated with the error code ERROR_GRAPHIC_TRANSPARENCY_SMASK (2.2.2).
     * 
     */
    protected void checkSMask()
    {
        COSBase smask = xobject.getDictionaryObject(COSName.SMASK);
        if (smask != null && !COSName.NONE.equals(smask))
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_TRANSPARENCY_SMASK,
                    "Soft Mask must be null or None ["+xobject.toString()+"]"));
        }
    }

    /**
     * According the ISO 190005:1-2005 specification, a XObject can't have an OPI entry in its dictionary. If the
     * XObject has a OPI entry, the error list is updated with the error code ERROR_GRAPHIC_UNEXPECTED_KEY (2.3).
     * 
     */
    protected void checkOPI()
    {
        // 6.2.4 and 6.2.5 no OPI
        if (this.xobject.getItem(COSName.getPDFName("OPI")) != null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY, "Unexpected 'OPI' Key"));
        }
    }

    /**
     * According the ISO 190005:1-2005 specification, a XObject can't have an Ref entry in its dictionary. If the
     * XObject has a Ref entry, the error list is updated with the error code ERROR_GRAPHIC_UNEXPECTED_KEY (2.3).
     * 
     */
    protected void checkReferenceXObject()
    {
        // 6.2.6 No reference XObject
        if (this.xobject.getItem("Ref") != null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_KEY,
                    "No reference XObject allowed in PDF/A"));
        }
    }

    /**
     * According the ISO 190005:1-2005 specification, PostScript XObjects are forbidden. If the XObject is a PostScript
     * XObject, the error list is updated with the error code ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY (2.3.2).
     * 
     * To know whether the object is a Postscript XObject, "Subtype" and "Subtype2" entries are checked.
     */
    protected void checkPostscriptXObject()
    {
        // 6.2.7 No PostScript XObjects
        COSName subtype = this.xobject.getCOSName(COSName.SUBTYPE);
        if (COSName.PS.equals(subtype))
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                    "No Postscript XObject allowed in PDF/A"));
        }
        else if (this.xobject.getItem(COSName.getPDFName("Subtype2")) != null)
        {
            context.addValidationError(new ValidationError(ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                    "No Postscript XObject allowed in PDF/A (Subtype2)"));
        }
    }

    /**
     * This method checks if required fields are present.
     * 
     */
    protected abstract void checkMandatoryFields();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pdfbox.preflight.graphic.XObjectValidator#validate()
     */
    @Override
    public void validate() throws ValidationException
    {
        checkMandatoryFields();
        checkOPI();
        checkSMask();
        checkReferenceXObject();
        checkPostscriptXObject();
    }
}
