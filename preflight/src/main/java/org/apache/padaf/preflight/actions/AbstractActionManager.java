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

package org.apache.padaf.preflight.actions;

import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION;

import java.util.List;


import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;

public abstract class AbstractActionManager {
  /**
   * ActionManager factory used to create new ActionManager
   */
  protected ActionManagerFactory actionFact = null;
  /**
   * Action name in a Additional Action dictionary
   */
  protected String aaKey = null;
  /**
   * The action dictionary checked by this class
   */
  protected COSDictionary actionDictionnary = null;
  /**
   * The COSDocument from which the action comes from
   */
  protected COSDocument cDoc = null;

  /**
   * 
   * @param amFact
   *          Instance of ActionManagerFactory used to create ActionManager to
   *          check Next actions.
   * @param adict
   *          the COSDictionary of the action wrapped by this class.
   * @param cDoc
   *          the COSDocument from which the action comes from.
   * @param aaKey
   *          The name of the key which identify the action in a additional
   *          action dictionary.
   */
  AbstractActionManager(ActionManagerFactory amFact, COSDictionary adict,
      COSDocument cDoc, String aaKey) {
    this.actionFact = amFact;
    this.actionDictionnary = adict;
    this.aaKey = aaKey;
    this.cDoc = cDoc;
  }

  /**
   * @return the isAdditionalAction
   */
  public boolean isAdditionalAction() {
    return this.aaKey != null;
  }

  /**
   * @return the actionDictionnary
   */
  public COSDictionary getActionDictionnary() {
    return actionDictionnary;
  }

  /**
   * @return the aaKey
   */
  public String getAdditionalActionKey() {
    return aaKey;
  }

  /**
   * This method create a list of Action Managers which represent actions in the
   * Next entry of the current action dictionary. For each Next Action, the
   * innerValid is called and the method returns false if a validation fails.
   * 
   * @param error
   *          the validation error list to updated if the validation fails.
   * @return True if all Next Action are valid, false otherwise.
   * @throws ValidationException
   */
  protected boolean validNextActions(List<ValidationError> error)
      throws ValidationException {
    List<AbstractActionManager> lActions = this.actionFact.getNextActions(
        this.actionDictionnary, this.cDoc);
    for (AbstractActionManager nAction : lActions) {
      if (!nAction.innerValid(error)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Call the valid(boolean, List) method with the additonalActionAuth set to
   * false.
   * 
   * @param error
   *          the validation error list to updated if the validation fails.
   * @return
   * @throws ValidationException
   */
  public boolean valid(List<ValidationError> error) throws ValidationException {
    return valid(false, error);
  }

  /**
   * Validate an Action dictionary.
   * 
   * Return false if the dictionary is invalid (ex : missing key). If the
   * ActionManager represents an AdditionalAction, this method returns false and
   * updates the error list when the additonalActionAuth parameter is set to
   * false.
   * 
   * This method call the innerValid method to process specific checks according
   * to the action type.
   * 
   * If innerValid successes, all actions contained in the Next entry of the
   * Action dictionary are validated.
   * 
   * @param additonalActionAuth
   *          boolean to know if an additional action is authorized.
   * @param error
   *          the validation error list to updated if the validation fails.
   * @return 
   * @throws ValidationException
   */
  public boolean valid(boolean additonalActionAuth, List<ValidationError> error)
      throws ValidationException {
    if (isAdditionalAction() && !additonalActionAuth) {
      error.add(new ValidationError(ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION, "Additional Action are forbidden" ));
      return false;
    }

    if (innerValid(error)) {
      return validNextActions(error);
    }

    return true;
  }

  /**
   * This method must be implemented by inherited classes to process specific
   * validation.
   * 
   * @param error
   *          the validation error list to updated if the validation fails.
   * @return True if the action is valid, false otherwise.
   */
  protected abstract boolean innerValid(List<ValidationError> error);
}
