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
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;

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
        if (!COSName.P.equals(relationship) && !COSName.C.equals(relationship))
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

    /**
     * If the value in the /P entry is an integer, this will get the page number (zero-based) in the
     * current document containing the file attachment annotation.
     *
     * @return the zero based page number or -1 if the /P entry value is missing or not a number.
     */
    public int getPageNumber()
    {
        COSBase base = dict.getDictionaryObject(COSName.P);
        if (base instanceof COSInteger)
        {
            return ((COSInteger) base).intValue();
        }
        return -1;
    }

    /**
     * Set the page number (zero-based) in the current document containing the file attachment
     * annotation.
     *
     * @param pageNumber the zero based page number. If this is &lt; 0 then the entry is removed.
     */
    public void setPageNumber(int pageNumber)
    {
        if (pageNumber < 0)
        {
            dict.removeItem(COSName.P);
        }
        else
        {
            dict.setInt(COSName.P, pageNumber);
        }
    }

    /**
     * If the value in the /P entry is a string, this will get a named destination in the current
     * document that provides the page number of the file attachment annotation.
     *
     * @return a named destination or null if the /P entry value is missing or not a string.
     */
    public PDNamedDestination getNamedDestination()
    {
        COSBase base = dict.getDictionaryObject(COSName.P);
        if (base instanceof COSString)
        {
            return new PDNamedDestination((COSString) base);
        }
        return null;
    }

    /**
     * This will set a named destination in the current document that provides the page number of
     * the file attachment annotation.
     *
     * @param dest a named destination or null if the entry is to be removed.
     */
    public void setNamedDestination(PDNamedDestination dest)
    {
        if (dest == null)
        {
            dict.removeItem(COSName.P);
        }
        else
        {
            dict.setItem(COSName.P, dest);
        }
    }

    /**
     * If the value in the /A entry is an integer, this will get the index (zero-based) of the
     * annotation in the /Annots array of the page specified by the /P entry.
     *
     * @return the zero based page number or -1 if the /P entry value is missing or not a number.
     */
    public int getAnnotationIndex()
    {
        COSBase base = dict.getDictionaryObject(COSName.A);
        if (base instanceof COSInteger)
        {
            return ((COSInteger) base).intValue();
        }
        return -1;
    }
    
    /**
     * This will set the index (zero-based) of the annotation in the /Annots array of the page
     * specified by the /P entry.
     *
     * @param index the zero based index. If this is &lt; 0 then the entry is removed.
     */
    public void setAnnotationIndex(int index)
    {
        if (index < 0)
        {
            dict.removeItem(COSName.A);
        }
        else
        {
            dict.setInt(COSName.A, index);
        }
    }

    /**
     * If the value in the /A entry is a string, this will get the value of the /NM entry in the
     * annotation dictionary.
     *
     * @return the /NM value of an annotation dictionary or null if the /A entry value is missing or
     * not a string.
     */
    public String getAnnotationName()
    {
        COSBase base = dict.getDictionaryObject(COSName.A);
        if (base instanceof COSString)
        {
            return ((COSString) base).getString();
        }
        return null;
    }

    /**
     * This will get the value of the /NM entry in the annotation dictionary.
     *
     * @param name the /NM value of an annotation dictionary or null if the entry is to be removed.
     */
    public void setAnnotationName(String name)
    {
        dict.setString(COSName.A, name);
    }
}
