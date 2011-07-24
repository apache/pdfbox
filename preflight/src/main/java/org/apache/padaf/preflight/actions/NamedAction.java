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

import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_KEY_N;
import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST;
import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST;
import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT;
import static org.apache.padaf.preflight.ValidationConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_ACTION_MISING_KEY;

import java.util.List;


import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;

/**
 * ActionManager for the Named action. Named action is valid if N entry is
 * present with one of the four values :
 * <UL>
 * <li>NextPage
 * <li>PrevPage
 * <li>FirstPage
 * <li>LastPage
 * </UL>
 */
public class NamedAction extends AbstractActionManager {

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
  public NamedAction(ActionManagerFactory amFact, COSDictionary adict,
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
    String n = this.actionDictionnary.getNameAsString(COSName
        .getPDFName(ACTION_DICTIONARY_KEY_N));

    // ---- N entry is mandatory
    if (n == null || "".equals(n)) {
      error.add(new ValidationError(ERROR_ACTION_MISING_KEY,
          "N entry is mandatory for the NamedActions"));
      return false;
    }

    // ---- Only Predefine name actions are authorized
    if (!(ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST.equals(n)
        || ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST.equals(n)
        || ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT.equals(n) || ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV
        .equals(n))) {
      error.add(new ValidationError(ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED, n + " isn't authorized as named action"));
      return false;
    }

    return true;
  }
}
