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

package org.apache.padaf.preflight.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.annotation.AnnotationValidator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * This helper validates AcroFrom (Interactive Form)
 */
public class AcroFormValidationHelper extends AbstractValidationHelper {

  /**
   * 
   * @param cfg
   * @throws ValidationException
   */
  public AcroFormValidationHelper(ValidatorConfig cfg) throws ValidationException {
    super(cfg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * net.awl.edoc.pdfa.validation.helpers.AbstractValidationHelper#innerValidate
   * (net.awl.edoc.pdfa.validation.DocumentHandler)
   */
  @Override
  public List<ValidationError> innerValidate(DocumentHandler handler)
      throws ValidationException {

    List<ValidationError> result = new ArrayList<ValidationError>(0);

    PDDocumentCatalog catalog = handler.getDocument().getDocumentCatalog();
    if (catalog != null) {
      PDAcroForm acroForm = catalog.getAcroForm();
      if (acroForm != null) {
        checkNeedAppearences(handler, acroForm, result);
        try {
          exploreFields(handler, acroForm.getFields(), result);
        } catch (IOException e) {
          throw new ValidationException("Unable to get the list of fields : "
              + e.getMessage(), e);
        }
      }
    } else {
      throw new ValidationException(
          "There are no Catalog entry in the Document.");
    }

    return result;
  }

  /**
   * This method checks if the NeedAppearances entry is present. If it is, the
   * value must be false.
   * 
   * If the entry is invalid, the ERROR_SYNTAX_DICT_INVALID (1.2.3) error is
   * return.
   * 
   * @param handler
   * @param acroForm
   * @param result
   */
  protected void checkNeedAppearences(DocumentHandler handler,
      PDAcroForm acroForm, List<ValidationError> error) {
    if (acroForm.getDictionary().getBoolean(
        ACROFORM_DICTIONARY_KEY_NEED_APPEARANCES, false)) {
      error.add(new ValidationError(ERROR_SYNTAX_DICT_INVALID,
          "NeedAppearance is present with the value \"true\""));
    }
  }

  /**
   * This function explores all fields and their children to check if the A or
   * AA entry is present.
   * 
   * @param handler
   * @param acroForm
   * @param result
   * @throws IOException
   */
  protected boolean exploreFields(DocumentHandler handler, List<?> lFields,
      List<ValidationError> error) throws IOException, ValidationException {
    if (lFields != null) {
      // ---- the list can be null is the Field doesn't have child
      for (Object obj : lFields) {
        if (!valideField((PDField) obj, handler, error)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A and AA field are forbidden, this method checks if they are present and
   * checks all child of this field. If the an Additional Action is present the
   * error code ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD (6.2.3) is added
   * to the error list If the an Action is present (in the Widget Annotation)
   * the error ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD (6.2.4) is added to
   * the error list. (Remark : The widget validation will be done by the
   * AnnotationValidationHelper, but some actions are authorized in a standard
   * Widget)
   * 
   * @param aField
   * @param handler
   * @param result
   * @return
   * @throws IOException
   */
  protected boolean valideField(PDField aField, DocumentHandler handler,
      List<ValidationError> error) throws IOException, ValidationException {
    boolean res = true;
    PDFormFieldAdditionalActions aa = aField.getActions();
    if (aa != null) {
      error.add(new ValidationError(ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD, "\"AA\" must not be used in a Field dictionary"));
      res = false;
    }

    // ---- The widget validation will be done by the widget annotation, a
    // widget contained in a Field can't have action.
    PDAnnotationWidget widget = aField.getWidget();
    if (res && widget != null) {
      AnnotationValidator widgetVal = annotFact.getAnnotationValidator( widget.getDictionary(), handler, error);
      widgetVal.validate(error);

      COSBase act = widget.getDictionary().getDictionaryObject(DICTIONARY_KEY_ACTION);
      if (act != null) {
        error.add(new ValidationError(
            ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD, "\"A\" must not be used in a Field dictionary"));
        res = false;
      }
    }

    res = res && exploreFields(handler, aField.getKids(), error);
    return res;
  }
}
