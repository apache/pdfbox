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

import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_KEY_D;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_INVALID_TYPE;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_MISING_KEY;

import java.util.List;


import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.utils.COSUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;

/**
 * ActionManager for the GoTo action GoToAction is valid if the D entry is
 * present.
 */
public class GoToAction extends AbstractActionManager {

  /**
   * 
   * @param amFact
   *          Instance of ActionManagerFactory used to create ActionManager to
   *          check Next actions.
   * @param adict
   *          the COSDictionary of the action wrapped by this class.
   * @param cDoc
   *          the COSDocument from which the action comes from.
   * @param aa
   *          The name of the key which identify the action in a additional
   *          action dictionary.
   */
  public GoToAction(ActionManagerFactory amFact, COSDictionary adict,
      COSDocument cDoc, String aa) {
    super(amFact, adict, cDoc, aa);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * net.awl.edoc.pdfa.validation.actions.AbstractActionManager#valid(java.util
   * .List)
   */
  @Override
  protected boolean innerValid(List<ValidationError> error) {
    COSBase d = this.actionDictionnary.getItem(COSName
        .getPDFName(ACTION_DICTIONARY_KEY_D));

    // ---- D entry is mandatory
    if (d == null) {
      error.add(new ValidationError(ERROR_ACTION_MISING_KEY,
          "D entry is mandatory for the GoToActions"));
      return false;
    }

    if (!(d instanceof COSName || COSUtils.isString(d, cDoc) || COSUtils
        .isArray(d, cDoc))) {
      error.add(new ValidationError(ERROR_ACTION_INVALID_TYPE, "Type of D entry is invalid"));
      return false;
    }

    return true;
  }

}
