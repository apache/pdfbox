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

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.actions.AbstractActionManager;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

/**
 * This helper validates the book mark object (Outline Items)
 */
public class BookmarkValidationHelper extends AbstractValidationHelper {

	/**
	 * 
	 * @param cfg
	 * @throws ValidationException
	 */
	public BookmarkValidationHelper(ValidatorConfig cfg) throws ValidationException {
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
      PDDocumentOutline outlineHierarchy = catalog.getDocumentOutline();
      if (outlineHierarchy != null) {
        if (outlineHierarchy.getFirstChild() == null
            || outlineHierarchy.getLastChild() == null) {
          result.add(new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
              "Outline Hierarchy doesn't have First and/or Last entry(ies)"));
        } else {
          // ---- Count entry is mandatory if there are childrens
          if (!isCountEntryPresent(outlineHierarchy.getCOSDictionary())) {
            result.add(new ValidationError(
                ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                "Outline Hierarchy doesn't have Count entry"));
          } else {
            exploreOutlineLevel(outlineHierarchy.getFirstChild(), handler,
                result);
          }
        }
      }
    } else {
      throw new ValidationException(
          "There are no Catalog entry in the Document.");
    }
    return result;
  }

  /**
   * Return true if the Count entry is present in the given dictionary.
   * 
   * @param outline
   * @return
   */
  private boolean isCountEntryPresent(COSDictionary outline) {
    return outline.getItem(COSName.getPDFName("Count")) != null;
  }

  /**
   * This method explores the Outline Item Level and call a validation method on
   * each Outline Item. If an invalid outline item is found, the result list is
   * updated.
   * 
   * @param inputItem
   *          The first outline item of the level
   * @param handler
   *          The document handler which provides useful data for the level
   *          exploration (ex : access to the PDDocument)
   * @param result
   * @return true if all items are valid in this level.
   * @throws ValidationException
   */
  protected boolean exploreOutlineLevel(PDOutlineItem inputItem,
      DocumentHandler handler, List<ValidationError> result)
      throws ValidationException {
    PDOutlineItem currentItem = inputItem;
    int oiValided = 0;
    while (currentItem != null) {
      if (!validateItem(currentItem, handler, result)) {
        return false;
      }
      oiValided++;
      currentItem = currentItem.getNextSibling();
    }
    return true;
  }

  /**
   * This method checks the inputItem dictionary and call the
   * exploreOutlineLevel method on the first child if it is not null.
   * 
   * @param inputItem
   *          outline item to validate
   * @param handler
   *          The document handler which provides useful data for the level
   *          exploration (ex : access to the PDDocument)
   * @param result
   * @return
   * @throws ValidationException
   */
  protected boolean validateItem(PDOutlineItem inputItem,
      DocumentHandler handler, List<ValidationError> result)
      throws ValidationException {
    boolean isValid = true;
    // ---- Dest entry isn't permitted if the A entry is present
    // A entry isn't permitted if the Dest entry is present
    // If the A enntry is present, the referenced actions is validated
    COSDictionary dictionary = inputItem.getCOSDictionary();
    COSBase dest = dictionary.getItem(COSName
        .getPDFName(DICTIONARY_KEY_DESTINATION));
    COSBase action = dictionary.getItem(COSName
        .getPDFName(DICTIONARY_KEY_ACTION));

    if (action != null && dest != null) {
      result.add(new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
          "Dest entry isn't permitted if the A entry is present"));
      return false;
    } else if (action != null) {
      List<AbstractActionManager> actions = this.actionFact.getActions(dictionary, handler
          .getDocument().getDocument());
      for (AbstractActionManager act : actions) {
        isValid = isValid && act.valid(result);
      }
    } // else no specific validation

    // ---- check children
    PDOutlineItem fChild = inputItem.getFirstChild();
    if (fChild != null) {
      if (!isCountEntryPresent(inputItem.getCOSDictionary())) {
        result
            .add(new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                "Outline item doesn't have Count entry but has at least one descendant."));
        isValid = false;
      } else {
        // ---- there are some descendants, so dictionary must have a Count
        // entry
        isValid = isValid && exploreOutlineLevel(fChild, handler, result);
      }
    }

    return isValid;
  }
}
