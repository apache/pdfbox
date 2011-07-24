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

import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_KEY_H;
import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_KEY_T;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_HIDE_H_INVALID;
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
 * ActionManager for the Hide action. The Hide action isn't specifically
 * prohibited by PDF/A-1, but should have been. So this action manager isn't an
 * instance of InvalidAction but authorized only the H entry with the false
 * value.
 */
public class HideAction extends AbstractActionManager {

  /**
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
  public HideAction(ActionManagerFactory amFact, COSDictionary adict,
      COSDocument doc, String aaKey) {
    super(amFact, adict, doc, aaKey);
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
    COSBase t = this.actionDictionnary.getItem(COSName
        .getPDFName(ACTION_DICTIONARY_KEY_T));
    // ---- T entry is mandatory
    if (t == null) {
      error.add(new ValidationError(ERROR_ACTION_MISING_KEY,
          "T entry is mandatory for the NamedActions"));
      return false;
    }

    if (!(COSUtils.isDictionary(t, cDoc) || COSUtils.isArray(t, cDoc) || COSUtils
        .isString(t, cDoc))) {
      error.add(new ValidationError(ERROR_ACTION_INVALID_TYPE,"T entry type is invalid"));
      return false;
    }

    /*
     * ---- H entry is optional but the default value is True (annotations of
     * the T entry will be hidden) according to the aim of a PDF/A it should be
     * false (annotations of the T entry will be shown).
     * 
     * We check the H value and we throw an error if it is true because of the
     * PDF/A Application Notes sentence :
     * 
     * The PDF Reference supports a concept whereby something will happen when
     * the user performs an explicit or implicit action in a PDF viewer - these
     * "things" are called Actions. PDF/A-1 permits a limited set of these
     * Actions, which are detailed in section 6.6.1. Specifically, any action
     * that could change the visual representation of the document or is not
     * documented in the PDF Reference is not permitted. This includes the /Hide
     * action which isn't specifically prohibited by PDF/A-1, but should have
     * been.
     */
    boolean h = this.actionDictionnary.getBoolean(COSName
        .getPDFName(ACTION_DICTIONARY_KEY_H), true);
    if (h) {
      error.add(new ValidationError(ERROR_ACTION_HIDE_H_INVALID,
          "H entry is \"true\""));
      return false;
    }

    return true;
  }
}
