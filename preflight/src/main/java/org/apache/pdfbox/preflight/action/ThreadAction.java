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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_INVALID_TYPE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_MISING_KEY;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.utils.COSUtils;

/**
 * ActionManager for the Thread action ThreadAction is valid if the D entry is present.
 */
public class ThreadAction extends AbstractActionManager
{

    /**
     * @param amFact
     *            Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict
     *            the COSDictionary of the action wrapped by this class.
     * @param cDoc
     *            the COSDocument from which the action comes from.
     * @param aaKey
     *            The name of the key which identify the action in a additional action dictionary.
     */
    public ThreadAction(ActionManagerFactory amFact, COSDictionary adict, PreflightContext ctx, String aaKey)
    {
        super(amFact, adict, ctx, aaKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.actions.AbstractActionManager#valid(java.util .List)
     */
    @Override
    protected boolean innerValid()
    {
        COSBase d = this.actionDictionnary.getItem(COSName.D);

        // ---- D entry is mandatory
        if (d == null)
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_MISING_KEY,
                    "D entry is mandatory for the ThreadAction"));
            return false;
        }

        COSDocument cosDocument = this.context.getDocument().getDocument();
        if (!(COSUtils.isInteger(d, cosDocument) || COSUtils.isString(d, cosDocument) || COSUtils.isDictionary(d,
                cosDocument)))
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_INVALID_TYPE, "D entry type is invalid"));
            return false;
        }

        return true;
    }

}
