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

import java.util.HashSet;
import java.util.Set;

import static org.apache.pdfbox.preflight.PreflightConfiguration.ACTIONS_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_NOCATALOG;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER_OUTLINES_INVALID;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import static org.apache.pdfbox.preflight.PreflightConfiguration.DESTINATION_PROCESS;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class BookmarkValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocumentCatalog catalog = ctx.getDocument().getDocumentCatalog();
        if (catalog != null)
        {
            PDDocumentOutline outlineHierarchy = catalog.getDocumentOutline();
            if (outlineHierarchy != null)
            {
                COSDictionary dict = outlineHierarchy.getCOSObject();
                if (!checkIndirectObjects(ctx, dict))
                {
                    return;
                }
                COSObject firstObj = toCOSObject(dict.getItem(COSName.FIRST));
                COSObject lastObj = toCOSObject(dict.getItem(COSName.LAST));

                // Count entry is mandatory if there are childrens
                if (!isCountEntryPresent(dict)
                        && (outlineHierarchy.getFirstChild() != null || outlineHierarchy.getLastChild() != null))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "Outline Hierarchy doesn't have Count entry"));
                }
                else if (isCountEntryPositive(ctx, dict)
                        && (outlineHierarchy.getFirstChild() == null || outlineHierarchy.getLastChild() == null))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "Outline Hierarchy doesn't have First and/or Last entry(ies)"));
                }
                else
                {
                    exploreOutlineLevel(ctx, outlineHierarchy.getFirstChild(), firstObj, lastObj);
                }
            }
        }
        else
        {
            ctx.addValidationError(new ValidationError(ERROR_SYNTAX_NOCATALOG, "There is no /Catalog entry in the Document"));
        }
    }

    /**
     * Return true if the Count entry is present in the given dictionary.
     * 
     * @param outline the dictionary representing the document outline.
     * @return true if the Count entry is present.
     */
    private boolean isCountEntryPresent(COSDictionary outline)
    {
        return outline.getItem(COSName.COUNT) != null;
    }

    /**
     * return true if Count entry &gt; 0
     * 
     * @param ctx the preflight context.
     * @param outline the dictionary representing the document outline.
     * @return true if the Count entry &gt; 0.
     */
    private boolean isCountEntryPositive(PreflightContext ctx, COSDictionary outline)
    {
        COSBase countBase = outline.getItem(COSName.COUNT);
        COSDocument cosDocument = ctx.getDocument().getDocument();
        return COSUtils.isInteger(countBase, cosDocument) && (COSUtils.getAsInteger(countBase, cosDocument) > 0);
    }

    /**
     * This method explores the Outline Item Level and calls a validation method on each Outline Item. If an invalid
     * outline item is found, the result list is updated.
     * 
     * @param ctx the preflight context.
     * @param inputItem The first outline item of the level.
     * @param firstObj The first PDF object of the level.
     * @param lastObj The last PDF object of the level.
     * @return true if all items are valid in this level.
     * @throws ValidationException
     */
    protected boolean exploreOutlineLevel(PreflightContext ctx, PDOutlineItem inputItem, 
            COSObject firstObj, COSObject lastObj) throws ValidationException
    {
        PDOutlineItem currentItem = inputItem;
        COSObject currentObj = firstObj;
        Set<COSObject> levelObjects = new HashSet<COSObject>();
        levelObjects.add(firstObj);
        boolean result = true;

        if (currentItem != null && inputItem.getPreviousSibling() != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                    "The value of /Prev of first object " + firstObj + " on a level is " 
                            + inputItem.getCOSObject().getItem(COSName.PREV) 
                            + ", but shouldn't exist"));
            result = false;
        }
        
        while (currentItem != null)
        {
            COSObject realPrevObject = currentObj;
            if (!validateItem(ctx, currentItem))
            {
                result = false;
            }
            currentObj = toCOSObject(currentItem.getCOSObject().getItem(COSName.NEXT));
            if (levelObjects.contains(currentObj))
            {
                addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                        "Loop detected: /Next " + currentObj + " is already in the list"));
                return false;
            }
            levelObjects.add(currentObj);
            currentItem = currentItem.getNextSibling();
            if (currentItem == null)
            {
                if (!realPrevObject.equals(lastObj))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "Last object on a level isn't the expected /Last: " + lastObj
                                    + ", but is " + currentObj));
                    result = false;
                }
            }
            else 
            {
                COSObject prevObject = toCOSObject(currentItem.getCOSObject().getItem(COSName.PREV));
                if (!realPrevObject.equals(prevObject))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "The value of /Prev at " + currentObj
                                    + " doesn't point to previous object " + realPrevObject
                                    + ", but to " + prevObject));
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * This method checks the inputItem dictionary and call the exploreOutlineLevel method on the first child if it is
     * not null.
     * 
     * @param ctx the preflight context.
     * @param inputItem outline item to validate
     * @return the validation result.
     * @throws ValidationException
     */
    protected boolean validateItem(PreflightContext ctx, PDOutlineItem inputItem) throws ValidationException
    {
        boolean isValid = true;
        // Dest entry isn't permitted if the A entry is present
        // A entry isn't permitted if the Dest entry is present
        // If the A entry is present, the referenced actions is validated
        COSDictionary dictionary = inputItem.getCOSObject();
        COSBase dest = dictionary.getItem(COSName.DEST);
        COSBase action = dictionary.getItem(COSName.A);
        
        if (!checkIndirectObjects(ctx, dictionary))
        {
            return false;
        }
        if (action != null && dest != null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                    "Dest entry isn't permitted if the A entry is present"));
            return false;
        }
        else if (action != null)
        {
            ContextHelper.validateElement(ctx, dictionary, ACTIONS_PROCESS);
        }
        else if (dest != null)
        {
            ContextHelper.validateElement(ctx, dest, DESTINATION_PROCESS);
        }
        // else no specific validation

        // check children
        PDOutlineItem fChild = inputItem.getFirstChild();
        if (fChild != null)
        {
            if (!isCountEntryPresent(inputItem.getCOSObject()))
            {
                addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                        "Outline item doesn't have Count entry but has at least one descendant"));
                isValid = false;
            }
            else
            {
                COSObject firstObj = toCOSObject(dictionary.getItem(COSName.FIRST));
                COSObject lastObj = toCOSObject(dictionary.getItem(COSName.LAST));
                if ((firstObj == null && lastObj != null) || (firstObj != null && lastObj == null))
                {
                    addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                            "/First and /Last are both required if there are outline entries"));
                    isValid = false;
                }
                
                // there are some descendants, so dictionary must have a Count entry
                isValid = isValid && exploreOutlineLevel(ctx, fChild, firstObj, lastObj);
            }
        }

        return isValid;
    }

    // verify that if certain named items exist, that they are indirect objects
    private boolean checkIndirectObjects(PreflightContext ctx, COSDictionary dictionary)
    {
        // Parent, Prev, Next, First and Last must be indirect objects
        if (!checkIndirectObject(ctx, dictionary, COSName.PARENT))
        {
            return false;
        }
        if (!checkIndirectObject(ctx, dictionary, COSName.PREV))
        {
            return false;
        }
        if (!checkIndirectObject(ctx, dictionary, COSName.NEXT))
        {
            return false;
        }
        if (!checkIndirectObject(ctx, dictionary, COSName.FIRST))
        {
            return false;
        }
        return checkIndirectObject(ctx, dictionary, COSName.LAST);
    }

    // verify that if the named item exists, that it is is an indirect object
    private boolean checkIndirectObject(PreflightContext ctx, COSDictionary dictionary, COSName name)
    {
        COSBase item = dictionary.getItem(name);
        if (item == null || item instanceof COSNull || item instanceof COSObject)
        {
            return true;
        }
        addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER_OUTLINES_INVALID,
                "/" + name.getName() + " entry must be an indirect object"));
        return false;
    }
    
    /**
     * Returns a COSBase as a COSObject or null if null or COSNull. To avoid
     * trouble, this method is to be called only after having called
     * {@link #checkIndirectObjects(PreflightContext, COSDictionary)}.
     *
     * @param base should be null, COSNull or a COSObject.
     * @return null if the parameter is COSNull or null; or else a COSObject.
     * @throws IllegalArgumentException if the parameter is not null, COSNull or
     * a COSObject.
     */
    private COSObject toCOSObject(COSBase base)
    {
        if (base == null || base instanceof COSNull)
        {
            return null;
        }
        if (!(base instanceof COSObject))
        {
            throw new IllegalArgumentException("Paremater " + base + " should be null, COSNull or a COSObject");
        }
        return (COSObject) base;
    }

}
