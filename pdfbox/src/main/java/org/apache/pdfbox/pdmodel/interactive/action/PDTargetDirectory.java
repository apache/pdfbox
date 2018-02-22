/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A target dictionary specifying path information to the target document. Each target dictionary
 * specifies one element in the full path to the target and may have nested target dictionaries
 * specifying additional elements.
 *
 * @author Tilman Hausherr
 */
public class PDTargetDirectory implements COSObjectable
{
    private final COSDictionary dict;

    /**
     * Default constructor, creates target directory.
     */
    public PDTargetDirectory()
    {
        dict = new COSDictionary();
    }

    /**
     * Create a target directory from an existing dictionary.
     *
     * @param dictionary The existing graphics state.
     */
    public PDTargetDirectory(COSDictionary dictionary)
    {
        dict = dictionary;
    }

    /**
     * This will get the underlying dictionary that this class acts on.
     *
     * @return The underlying dictionary for this class.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dict;
    }

    /**
     * Get the relationship between the current document and the target (which may be an
     * intermediate target).
     *
     * @return the relationship as a name. Valid values are P (the target is the parent of the
     * current document) and C (the target is a child of the current document). Invalid values or
     * null are also returned.
     */
    public COSName getRelationship()
    {
        COSBase base = dict.getItem(COSName.R);
        if (base instanceof COSName)
        {
            return (COSName) base;
        }
        return null;
    }

    /**
     * Set the relationship between the current document and the target (which may be an
     * intermediate target).
     *
     * @param relationship Valid values are P (the target is the parent of the current document) and
     * C (the target is a child of the current document).
     *
     * throws IllegalArgumentException if the parameter is not P or C.
     */
    public void setRelationship(COSName relationship)
    {
        if (!COSName.P.equals(relationship) && !COSName.P.equals(relationship))
        {
            throw new IllegalArgumentException("The only valid are P or C, not " + relationship.getName());
        }
        dict.setItem(COSName.R, relationship);
    }

    /**
     * Get the name of the file as found in the EmbeddedFiles name tree. This is only to be used if
     * the target is a child of the current document.
     *
     * @return a filename or null if there is none.
     */
    public String getFilename()
    {
        return dict.getString(COSName.N);
    }

    /**
     * Sets the name of the file as found in the EmbeddedFiles name tree. This is only to be used if
     * the target is a child of the current document.
     *
     * @param filename a filename or null if the entry is to be deleted.
     */
    public void setFilename(String filename)
    {
        dict.setString(COSName.N, filename);
    }

    /**
     * Get the target directory. If this entry is absent, the current document is the target file
     * containing the destination.
     *
     * @return the target directory or null if the current document is the target file containing
     * the destination.
     */
    public PDTargetDirectory getTargetDirectory()
    {
        COSBase base = dict.getDictionaryObject(COSName.T);
        if (base instanceof COSDictionary)
        {
            return new PDTargetDirectory((COSDictionary) base);
        }
        return null;
    }

    /**
     * Sets the target directory.
     *
     * @param targetDirectory the target directory or null if the current document is the target
     * file containing the destination.
     */
    public void setTargetDirectory(PDTargetDirectory targetDirectory)
    {
        dict.setItem(COSName.T, targetDirectory);
    }

    // page 420
    //TODO P, A
    // getPageNumber int
    // getNamedDestination PDNamedDestination
    // getAnnotationIndex int
    // getAnnotationName String
}
