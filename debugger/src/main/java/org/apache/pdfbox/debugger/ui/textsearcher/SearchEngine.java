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

package org.apache.pdfbox.debugger.ui.textsearcher;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Khyrul Bashar
 * A class that search a word in the JTextComponent and if find highlights them.
 */
class SearchEngine
{
    private static final Log LOG = LogFactory.getLog(SearchEngine.class);

    private final Document document;
    private final Highlighter highlighter;
    private final Highlighter.HighlightPainter painter;

    /**
     * Constructor.
     * @param textComponent JTextComponent that is to be searched.
     * @param painter Highlighter.HighlightPainter instance to paint the highlights.
     */
    SearchEngine(JTextComponent textComponent, Highlighter.HighlightPainter painter)
    {
        this.document = textComponent.getDocument();
        this.highlighter = textComponent.getHighlighter();
        this.painter = painter;
    }

    /**
     * Search the word.
     * @param searchKey String. Search word.
     * @param isCaseSensitive boolean. If search is case sensitive.
     * @return ArrayList<Highlighter.Highlight>.
     */
    public List<Highlighter.Highlight> search(String searchKey, boolean isCaseSensitive)
    {
        List<Highlighter.Highlight> highlights = new ArrayList<>();

        if (searchKey != null)
        {
            highlighter.removeAllHighlights();

            if (searchKey.isEmpty())
            {
                return highlights;
            }

            String textContent;

            try
            {
                textContent = document.getText(0, document.getLength());
            }
            catch (BadLocationException e)
            {
                LOG.error(e.getMessage(), e);
                return highlights;
            }
            if (!isCaseSensitive)
            {
                textContent = textContent.toLowerCase();
                searchKey = searchKey.toLowerCase();
            }

            int searchKeyLength = searchKey.length();
            int startAt = 0;
            int resultantOffset;
            int indexOfHighLight = 0;

            while ((resultantOffset = textContent.indexOf(searchKey, startAt)) != -1)
            {
                try
                {
                    highlighter.addHighlight(resultantOffset, resultantOffset + searchKeyLength, painter);
                    highlights.add(highlighter.getHighlights()[indexOfHighLight++]);
                    startAt = resultantOffset + searchKeyLength;
                }
                catch (BadLocationException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return highlights;
    }
}
