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

package org.apache.pdfbox.preflight.process;

import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NOCATALOG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_OUTLINES_INVALID;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class BookmarkValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocumentCatalog catalog = ctx.getDocument().getDocumentCatalog();
        if (catalog != null)
        {
            PDDocumentOutline outlineHierarchy = catalog.getDocumentOutline();
            if (outlineHierarchy != null)
            {
                // Count entry is mandatory if there are childrens
                if (!isCountEntryPresent(outlineHierarchy.getCOSDictionary())
                        && (outlineHierarchy.getFirstChild() != null || outlineHierarchy.getLastChild() != null))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "Outline Hierarchy doesn't have Count entry"));
                }
                else if (isCountEntryPositive(ctx, outlineHierarchy.getCOSDictionary())
                        && (outlineHierarchy.getFirstChild() == null || outlineHierarchy.getLastChild() == null))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "Outline Hierarchy doesn't have First and/or Last entry(ies)"));
                }
                else
                {
                    exploreOutlineLevel(ctx, outlineHierarchy.getFirstChild());
                }
            }
        }
        else
        {
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There are no Catalog entry in the Document."));
        }
    }

    /**
     * Return true if the Count entry is present in the given dictionary.
     * 
     * @param outline
     * @return
     */
    private boolean isCountEntryPresent(COSDictionary outline)
    {
        return outline.getItem(COSName.getPDFName("Count")) != null;
    }

    /**
     * return true if Count entry > 0
     * 
     * @param outline
     * @param doc
     * @return
     */
    private boolean isCountEntryPositive(PreflightContext ctx, COSDictionary outline)
    {
        COSBase countBase = outline.getItem(COSName.getPDFName("Count"));
        COSDocument cosDocument = ctx.getDocument().getDocument();
        return COSUtils.isInteger(countBase, cosDocument) && (COSUtils.getAsInteger(countBase, cosDocument) > 0);
    }

    /**
     * This method explores the Outline Item Level and call a validation method on each Outline Item. If an invalid
     * outline item is found, the result list is updated.
     * 
     * @param inputItem
     *            The first outline item of the level
     * @param ctx
     *            The document handler which provides useful data for the level exploration (ex : access to the
     *            PDDocument)
     * @return true if all items are valid in this level.
     * @throws ValidationException
     */
    protected boolean exploreOutlineLevel(PreflightContext ctx, PDOutlineItem inputItem) throws ValidationException
    {
        PDOutlineItem currentItem = inputItem;
        while (currentItem != null)
        {
            if (!validateItem(ctx, currentItem))
            {
                return false;
            }
            currentItem = currentItem.getNextSibling();
        }
        return true;
    }

    /**
     * This method checks the inputItem dictionary and call the exploreOutlineLevel method on the first child if it is
     * not null.
     * 
     * @param inputItem
     *            outline item to validate
     * @param ctx
     *            The document handler which provides useful data for the level exploration (ex : access to the
     *            PDDocument)
     * @param result
     * @return
     * @throws ValidationException
     */
    protected boolean validateItem(PreflightContext ctx, PDOutlineItem inputItem) throws ValidationException
    {
        boolean isValid = true;
        // Dest entry isn't permitted if the A entry is present
        // A entry isn't permitted if the Dest entry is present
        // If the A enntry is present, the referenced actions is validated
        COSDictionary dictionary = inputItem.getCOSDictionary();
        COSBase dest = dictionary.getItem(COSName.DEST);
        COSBase action = dictionary.getItem(COSName.A);

        if (action != null && dest != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                    "Dest entry isn't permitted if the A entry is present"));
            return false;
        }
        else if (action != null)
        {
            ContextHelper.validateElement(ctx, dictionary, ACTIONS_PROCESS);
        } // else no specific validation

        // check children
        PDOutlineItem fChild = inputItem.getFirstChild();
        if (fChild != null)
        {
            if (!isCountEntryPresent(inputItem.getCOSDictionary()))
            {
                addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                        "Outline item doesn't have Count entry but has at least one descendant."));
                isValid = false;
            }
            else
            {
                // there are some descendants, so dictionary must have a Count entry
                isValid = isValid && exploreOutlineLevel(ctx, fChild);
            }
        }

        return isValid;
    }

}
