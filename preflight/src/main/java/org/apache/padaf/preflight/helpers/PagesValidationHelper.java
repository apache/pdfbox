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
import org.apache.padaf.preflight.actions.AbstractActionManager;
import org.apache.padaf.preflight.annotation.AnnotationValidator;
import org.apache.padaf.preflight.contentstream.ContentStreamWrapper;
import org.apache.padaf.preflight.graphics.ExtGStateContainer;
import org.apache.padaf.preflight.graphics.ShadingPattern;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;


public class PagesValidationHelper extends AbstractValidationHelper {
 
  public PagesValidationHelper(ValidatorConfig cfg)
  throws ValidationException {
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

      // ---- PDFBox provides a method which returns all pages in a List.
      // Currently, it is useless to explore the Pages hierarchy.
      List<?> pages = catalog.getAllPages();
      for (int i = 0; i < pages.size(); ++i) {
        if (!validatePage((PDPage) pages.get(i), handler, result)) {
          return result;
        }
      }
    } else {
      throw new ValidationException(
          "There are no Catalog entry in the Document.");
    }

    return result;
  }

  /**
   * This method checks the given page. Only a part of element contained by the
   * page will be checked, like :
   * <UL>
   * <li>Presence of mandatory elements
   * <li>The page content when it is possible (ex : text area)
   * <li>The Additional Actions are authorized
   * <li>etc...
   * </UL>
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   */
  protected boolean validatePage(PDPage page, DocumentHandler handler,
      List<ValidationError> result) throws ValidationException {
    boolean isValid = validateActions(page, handler, result);
    isValid = isValid && validateAnnotation(page, handler, result);
    isValid = isValid && validateTransparency(page, handler, result);
    isValid = isValid && validateContent(page, handler, result);
    isValid = isValid && validateShadingPattern(page, handler, result);
    return isValid;
  }

  /**
   * This method checks additional actions contained in the given Page object.
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateActions(PDPage page, DocumentHandler handler,
      List<ValidationError> result) throws ValidationException {
    // ---- get AA (additional actions) entry if it is present
    List<AbstractActionManager> lActions = this.actionFact.getActions(page
        .getCOSDictionary(), handler.getDocument().getDocument());
    for (AbstractActionManager action : lActions) {
      if (!action.valid(true, result)) {
        return false;
      }
    }

    return true;
  }

  /**
   * This method check the ExtGState entry of the resource dictionary.
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateTransparency(PDPage page, DocumentHandler handler,
      List<ValidationError> result) throws ValidationException {
    PDResources resources = page.getResources();
    COSDocument cDoc = handler.getDocument().getDocument();
    ExtGStateContainer extGStates = new ExtGStateContainer(resources
        .getCOSDictionary(), cDoc);
    return extGStates.validateTransparencyRules(result);
    // ---- Even if a Group entry is possible in the Page dictionary, No
    // restrictions are defined by PDF/A
  }

  /**
   * This method check the Shading entry of the resource dictionary if exists.
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateShadingPattern(PDPage page,
      DocumentHandler handler, List<ValidationError> result)
      throws ValidationException {
    PDResources resources = page.getResources();
    COSDictionary shadings = (COSDictionary) resources.getCOSDictionary()
        .getDictionaryObject(PATTERN_KEY_SHADING);
    boolean res = true;
    if (shadings != null) {
      for (Object key : shadings.keySet()) {
        COSDictionary aShading = (COSDictionary) shadings
            .getDictionaryObject((COSName) key);
        ShadingPattern sp = new ShadingPattern(handler, aShading);
        List<ValidationError> lErrors = sp.validate();
        if (lErrors != null && !lErrors.isEmpty()) {
          result.addAll(lErrors);
          res = false;
        }
      }
    }
    return res;
  }

  /**
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateContent(PDPage page, DocumentHandler handler,
      List<ValidationError> result) throws ValidationException {

    ContentStreamWrapper csWrapper = new ContentStreamWrapper(handler);
    List<ValidationError> csParseErrors = csWrapper.validPageContentStream(page);
    if (csParseErrors == null || csParseErrors.isEmpty()) {
      return true;
    }

    result.addAll(csParseErrors);
    return false;
  }

  /**
   * 
   * @param page
   * @param handler
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateAnnotation(PDPage page, DocumentHandler handler,
      List<ValidationError> result) throws ValidationException {
    try {
      List<?> lAnnots = page.getAnnotations();
      for (Object object : lAnnots) {
        if (object instanceof PDAnnotation) {

          COSDictionary cosAnnot = ((PDAnnotation) object).getDictionary();
          AnnotationValidator validator = this.annotFact.getAnnotationValidator(cosAnnot, handler, result);
          if (validator != null) {
            return validator.validate(result);
          }

        }
      }

    } catch (IOException e) {
      throw new ValidationException("Unable to access Annotation", e);
    }
    // --- No annotations, validation OK
    return true;
  }
}
