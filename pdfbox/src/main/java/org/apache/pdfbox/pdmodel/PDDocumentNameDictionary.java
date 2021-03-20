/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class holds all of the name trees that are available at the document level.
 *
 * @author Ben Litchfield
 */
public class PDDocumentNameDictionary implements COSObjectable
{
    private final COSDictionary nameDictionary;
    private final PDDocumentCatalog catalog;

    /**
     * Constructor.
     *
     * @param cat The document catalog that this dictionary is part of.
     */
    public PDDocumentNameDictionary( PDDocumentCatalog cat )
    {
        COSDictionary names = cat.getCOSObject().getCOSDictionary(COSName.NAMES);
        if (names == null)
        {
            names = new COSDictionary();
            cat.getCOSObject().setItem(COSName.NAMES, names);
        }
        nameDictionary = names;
        catalog = cat;
    }

    /**
     * Constructor.
     *
     * @param cat The document that this dictionary is part of.
     * @param names The names dictionary.
     */
    public PDDocumentNameDictionary( PDDocumentCatalog cat, COSDictionary names )
    {
        catalog = cat;
        nameDictionary = names;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos dictionary for this object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return nameDictionary;
    }

    /**
     * Get the destination named tree node. The values in this name tree will be
     * PDPageDestination objects.
     *
     * @return The destination name tree node.
     */
    public PDDestinationNameTreeNode getDests()
    {
        COSDictionary dic = nameDictionary.getCOSDictionary(COSName.DESTS);
        //The document catalog also contains the Dests entry sometimes
        //so check there as well.
        if( dic == null )
        {
            dic = catalog.getCOSObject().getCOSDictionary(COSName.DESTS);
        }
        return dic != null ? new PDDestinationNameTreeNode(dic) : null;
    }

    /**
     * Set the named destinations that are associated with this document.
     *
     * @param dests The destination names.
     */
    public void setDests( PDDestinationNameTreeNode dests )
    {
        nameDictionary.setItem( COSName.DESTS, dests );
        //The dests can either be in the document catalog or in the
        //names dictionary, PDFBox will just maintain the one in the
        //names dictionary for now unless there is a reason to do
        //something else.
        //clear the potentially out of date Dests reference.
        catalog.getCOSObject().setItem( COSName.DESTS, (COSObjectable)null);
    }

    /**
     * Get the embedded files named tree node. The values in this name tree will
     * be PDComplexFileSpecification objects.
     *
     * @return The embedded files name tree node.
     */
    public PDEmbeddedFilesNameTreeNode getEmbeddedFiles()
    {
        COSDictionary dic = nameDictionary.getCOSDictionary(COSName.EMBEDDED_FILES);
        return dic != null ? new PDEmbeddedFilesNameTreeNode(dic) : null;
    }

    /**
     * Set the named embedded files that are associated with this document.
     *
     * @param ef The new embedded files
     */
    public void setEmbeddedFiles( PDEmbeddedFilesNameTreeNode ef )
    {
        nameDictionary.setItem( COSName.EMBEDDED_FILES, ef );
    }

    /**
     * Get the document level JavaScript name tree. When the document is opened, all the JavaScript
     * actions in it shall be executed, defining JavaScript functions for use by other scripts in
     * the document.
     *
     * @return The document level JavaScript name tree.
     */
    public PDJavascriptNameTreeNode getJavaScript()
    {
        COSDictionary dic = nameDictionary.getCOSDictionary(COSName.JAVA_SCRIPT);
        return dic != null ? new PDJavascriptNameTreeNode(dic) : null;
    }

    /**
     * Set the named javascript entries that are associated with this document.
     *
     * @param js The new Javascript entries.
     */
    public void setJavascript( PDJavascriptNameTreeNode js )
    {
        nameDictionary.setItem( COSName.JAVA_SCRIPT, js );
    }
}
