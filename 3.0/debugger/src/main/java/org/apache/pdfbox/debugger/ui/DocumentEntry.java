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

package org.apache.pdfbox.debugger.ui;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.debugger.PDFDebugger;

/**
 * Represents an abstract view of a document in the tree view.
 *
 * @author John Hewson
 */
public class DocumentEntry
{
    private final PDDocument doc;
    private final String filename;
    
    public DocumentEntry(PDDocument doc, String filename)
    {
        this.doc = doc;
        this.filename = filename;
    }
    
    public int getPageCount()
    {
        return doc.getPages().getCount();
    }
    
    public PageEntry getPage(int index)
    {
        PDPage page = doc.getPages().get(index);
        String pageLabel = PDFDebugger.getPageLabel(doc, index);
        return new PageEntry(page.getCOSObject(), index + 1, pageLabel);
    }
    
    public int indexOf(PageEntry page)
    {
        return page.getPageNum() - 1;
    }

    @Override
    public String toString()
    {
        return filename;
    }
}
