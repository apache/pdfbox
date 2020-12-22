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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * ActionManager for InvalidAction. An invalid action is an action which isn't authorized in a PDF/A file but should be
 * valid in a standard PDF file.
 */
public class InvalidAction extends AbstractActionManager
{
    private String actionName = null;

    /**
     * 
     * @param amFact
     *            Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict
     *            the COSDictionary of the action wrapped by this class.
     * @param ctx
     *            the DocumentHandler from which the action comes from.
     * @param aaKey
     *            The name of the key which identify the action in a additional action dictionary.
     * @param name
     *            the action type
     */
    public InvalidAction(final ActionManagerFactory amFact, final COSDictionary adict, final PreflightContext ctx, final String aaKey,
                         final String name)
    {
        super(amFact, adict, ctx, aaKey);
        this.actionName = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.pdfbox.preflight.action.AbstractActionManager#valid(java.util .List)
     */
    @Override
    protected boolean innerValid()
    {
        context.addValidationError(new ValidationError(ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN,
                "The action " + actionName + " is forbidden"));
        return false;
    }
}
