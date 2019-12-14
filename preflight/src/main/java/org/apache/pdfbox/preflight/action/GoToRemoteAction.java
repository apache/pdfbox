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

package org.apache.pdfbox.preflight.action;

import java.io.IOException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.preflight.PreflightConstants;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_INVALID_TYPE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_MISING_KEY;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_DICT_INVALID;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;

/**
 * ActionManager for the GoToRemote action. GoToRemoteAction is valid if the F entry is present.
 */
public class GoToRemoteAction extends GoToAction
{

    /**
     * 
     * @param amFact Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict the COSDictionary of the action wrapped by this class.
     * @param ctx the preflight context.
     * @param aaKey the name of the key which identify the action in a additional action dictionary.
     */
    public GoToRemoteAction(ActionManagerFactory amFact, COSDictionary adict, PreflightContext ctx, String aaKey)
    {
        super(amFact, adict, ctx, aaKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see AbstractActionManager#valid(java.util.List)
     */
    @Override
    protected boolean innerValid() throws ValidationException
    {
        COSBase dest = this.actionDictionnary.getItem(COSName.D);

        // ---- D entry is mandatory
        if (dest == null)
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_MISING_KEY,
                    "/D entry is mandatory for the GoToActions"));
            return false;
        }

        COSDocument cosDocument = this.context.getDocument().getDocument();
        if (!(dest instanceof COSName || COSUtils.isString(dest, cosDocument) || COSUtils.isArray(dest, cosDocument)))
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_INVALID_TYPE, 
                    "Type " + dest.getClass().getSimpleName() + " of /D entry is invalid"));
            return false;
        }

        COSBase f = this.actionDictionnary.getItem(COSName.F);
        if (f == null)
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_MISING_KEY,
                    "/F entry is mandatory for the GoToRemoteActions"));
            return false;
        }
        
        if (dest instanceof COSArray)
        {
            COSArray ar = (COSArray) dest;
            if (ar.size() < 2)
            {
                context.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                        "Destination array must have at least 2 elements"));
                return false;
            }
            if (!(ar.get(1) instanceof COSName))
            {
                context.addValidationError(new ValidationResult.ValidationError(ERROR_SYNTAX_DICT_INVALID,
                        "Second element of destination array must be a name"));
                return false;
            }
            validateExplicitDestination(ar);
        }
        try
        {
            PDDestination.create(dest);
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_SYNTAX_DICT_INVALID,
                    e.getMessage(), e));
            return false;
        }

        return true;
    }

    private boolean validateExplicitDestination(COSArray ar)
    {
        if (!(ar.get(0) instanceof COSNumber))
        {
            // "its first element shall be a page number"
            context.addValidationError(new ValidationError(ERROR_ACTION_INVALID_TYPE,
                    "First element in /D array entry of GoToRemoteAction must be a page number, but is "
                    + ar.get(0)));
            return false;
        }
        return true;
    }

}
