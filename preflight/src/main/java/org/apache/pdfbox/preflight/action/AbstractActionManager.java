/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.action;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION;

import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

public abstract class AbstractActionManager
{
    /**
     * ActionManager factory used to create new ActionManager
     */
    private ActionManagerFactory actionFact = null;
    /**
     * Action name in a Additional Action dictionary
     */
    private String aaKey = null;
    /**
     * The action dictionary checked by this class
     */
    protected COSDictionary actionDictionary = null;
    /**
     * The validation context
     */
    protected PreflightContext context = null;

    /**
     * 
     * @param amFact
     *            Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict
     *            the COSDictionary of the action wrapped by this class.
     * @param ctx
     *            the validation context .
     * @param aaKey
     *            The name of the key which identify the action in a additional action dictionary.
     */
    AbstractActionManager(final ActionManagerFactory amFact, final COSDictionary adict, final PreflightContext ctx, final String aaKey)
    {
        this.actionFact = amFact;
        this.actionDictionary = adict;
        this.aaKey = aaKey;
        this.context = ctx;
    }

    /**
     * @return the isAdditionalAction
     */
    public boolean isAdditionalAction()
    {
        return this.aaKey != null;
    }

    /**
     * @return the actionDictionary
     */
    public COSDictionary getActionDictionary()
    {
        return actionDictionary;
    }

    /**
     * @return the aaKey
     */
    public String getAdditionalActionKey()
    {
        return aaKey;
    }

    /**
     * This method create a list of Action Managers which represent actions in the Next entry of the current action
     * dictionary. For each Next Action, the innerValid is called and the method returns false if a validation fails.
     * 
     * @return True if all Next Action are valid, false otherwise.
     * @throws ValidationException
     */
    protected boolean validNextActions() throws ValidationException
    {
        final List<AbstractActionManager> lActions = this.actionFact.getNextActions(this.context, this.actionDictionary);
        for (final AbstractActionManager nAction : lActions)
        {
            if (!nAction.innerValid())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Call the {@link AbstractActionManager#valid(boolean)} method with the additionalActionAuth set to false.
     * 
     * @return the validation state.
     * @throws ValidationException
     */
    public boolean valid() throws ValidationException
    {
        return valid(false);
    }

    /**
     * Validate an Action dictionary.
     * 
     * Return false if the dictionary is invalid (ex : missing key). If the ActionManager represents an
     * AdditionalAction, this method returns false and updates the error list when the additionalActionAuth parameter is
     * set to false.
     * 
     * This method call the innerValid method to process specific checks according to the action type.
     * 
     * If innerValid successes, all actions contained in the Next entry of the Action dictionary are validated.
     * 
     * @param additionalActionAuth
     *            boolean to know if an additional action is authorized.
     * @return the validation state of the Action dictionary.
     * @throws ValidationException
     */
    public boolean valid(final boolean additionalActionAuth) throws ValidationException
    {
        if (isAdditionalAction() && !additionalActionAuth)
        {
            context.addValidationError(new ValidationError(ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION,
                    "Additional Action are forbidden"));
            return false;
        }

        if (innerValid())
        {
            return validNextActions();
        }

        return true;
    }

    /**
     * This method must be implemented by inherited classes to process specific validation.
     * 
     * @return True if the action is valid, false otherwise.
     */
    protected abstract boolean innerValid() throws ValidationException;
}
