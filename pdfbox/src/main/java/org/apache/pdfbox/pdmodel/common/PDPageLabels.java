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
package org.apache.pdfbox.pdmodel.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Represents the page label dictionary of a document.
 * 
 * @author Igor Podolskiy
 */
public class PDPageLabels implements COSObjectable
{

    private final Map<Integer, PDPageLabelRange> labels;

    private final PDDocument doc;

    /**
     * Creates an empty page label dictionary for the given document.
     * 
     * <p>
     * Note that the page label dictionary won't be automatically added to the
     * document; you will still need to do it manually (see
     * {@link org.apache.pdfbox.pdmodel.PDDocumentCatalog#setPageLabels(PDPageLabels)}.
     * </p>
     * 
     * @param document
     *            The document the page label dictionary is created for.
     * @see org.apache.pdfbox.pdmodel.PDDocumentCatalog#setPageLabels(PDPageLabels)
     */
    public PDPageLabels(PDDocument document)
    {
        labels = new TreeMap<>();
        this.doc = document;
        PDPageLabelRange defaultRange = new PDPageLabelRange();
        defaultRange.setStyle(PDPageLabelRange.STYLE_DECIMAL);
        labels.put(0, defaultRange);
    }

    /**
     * Creates an page label dictionary for a document using the information in
     * the given COS dictionary.
     * 
     * <p>
     * Note that the page label dictionary won't be automatically added to the
     * document; you will still need to do it manually (see
     * {@link org.apache.pdfbox.pdmodel.PDDocumentCatalog#setPageLabels(PDPageLabels)}.
     * </p>
     * 
     * @param document
     *            The document the page label dictionary is created for.
     * @param dict
     *            an existing page label dictionary
     * @see org.apache.pdfbox.pdmodel.PDDocumentCatalog#setPageLabels(PDPageLabels)
     * @throws IOException
     *             If something goes wrong during the number tree conversion.
     */
    public PDPageLabels(PDDocument document, COSDictionary dict) throws IOException
    {
        this(document);
        if (dict == null)
        {
            return;
        }
        PDNumberTreeNode root = new PDNumberTreeNode(dict, PDPageLabelRange.class);
        findLabels(root);
    }
    
    private void findLabels(PDNumberTreeNode node) throws IOException 
    {
        List<PDNumberTreeNode> kids = node.getKids();
        if (node.getKids() != null) 
        {
            for (PDNumberTreeNode kid : kids) 
            {
                findLabels(kid);
            }
        }
        else
        {
            Map<Integer,COSObjectable> numbers = node.getNumbers();
            if (numbers != null)
            {
                numbers.forEach((key, pageLabelRange) ->
                {
                    if (key >= 0)
                    {
                        labels.put(key, (PDPageLabelRange) pageLabelRange);
                    }
                });
            }
        }
    }


    /**
     * Returns the number of page label ranges.
     * 
     * <p>
     * This will be always &gt;= 1, as the required default entry for the page
     * range starting at the first page is added automatically by this
     * implementation (see PDF32000-1:2008, p. 375).
     * </p>
     * 
     * @return the number of page label ranges.
     */
    public int getPageRangeCount()
    {
        return labels.size();
    }

    /**
     * Returns the page label range starting at the given page, or {@code null}
     * if no such range is defined.
     * 
     * @param startPage
     *            the 0-based page index representing the start page of the page
     *            range the item is defined for.
     * @return the page label range or {@code null} if no label range is defined
     *         for the given start page.
     */
    public PDPageLabelRange getPageLabelRange(int startPage)
    {
        return labels.get(startPage);
    }

    /**
     * Sets the page label range beginning at the specified start page.
     * 
     * @param startPage
     *            the 0-based index of the page representing the start of the
     *            page label range.
     * @param item
     *            the page label item to set.
     * @throws IllegalArgumentException if the startPage parameter is &lt; 0.
     */
    public void setLabelItem(int startPage, PDPageLabelRange item)
    {
        if (startPage < 0)
        {
            throw new IllegalArgumentException("startPage parameter of setLabelItem may not be < 0");
        }
        labels.put(startPage, item);
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public COSBase getCOSObject()
    {
        COSArray arr = new COSArray();
        labels.forEach((key, value) ->
        {
            arr.add(COSInteger.get(key));
            arr.add(value);
        });
        COSDictionary dict = new COSDictionary();
        dict.setItem(COSName.NUMS, arr);
        return dict;
    }

    /**
     * Returns a mapping with computed page labels as keys and corresponding
     * 0-based page indices as values. The returned map will contain at most as
     * much entries as the document has pages.
     * 
     * <p>
     * <strong>NOTE:</strong> If the document contains duplicate page labels,
     * the returned map will contain <em>less</em> entries than the document has
     * pages. The page index returned in this case is the <em>highest</em> index
     * among all pages sharing the same label.
     * </p>
     * 
     * @return a mapping from labels to 0-based page indices.
     */
    public Map<String, Integer> getPageIndicesByLabels()
    {
        int numberOfPages = doc.getNumberOfPages();
        final Map<String, Integer> labelMap = new HashMap<>(numberOfPages);
        computeLabels((pageIndex, label) -> labelMap.put(label, pageIndex), numberOfPages);
        return labelMap;
    }

    /**
     * Returns a mapping with 0-based page indices as keys and corresponding
     * page labels as values as an array. The array will have exactly as much
     * entries as the document has pages.
     * 
     * @return an array mapping from 0-based page indices to labels.
     */
    public String[] getLabelsByPageIndices()
    {
        final int numberOfPages = doc.getNumberOfPages();
        final String[] map = new String[numberOfPages];
        computeLabels((pageIndex, label) ->
        {
            if (pageIndex < numberOfPages)
            {
                map[pageIndex] = label;
            }
        }, numberOfPages);
        return map;
    }

    /**
     * Get an ordered set of page indices having a page label range.
     *
     * @return set of page indices.
     */
    public NavigableSet<Integer> getPageIndices()
    {
        return new TreeSet<>(labels.keySet());
    }

    /**
     * Internal interface for the control flow support.
     * 
     * @author Igor Podolskiy
     */
    private interface LabelHandler
    {
        void newLabel(int pageIndex, String label);
    }

    private void computeLabels(LabelHandler handler, int numberOfPages)
    {
        Iterator<Entry<Integer, PDPageLabelRange>> iterator = 
            labels.entrySet().iterator();
        if (!iterator.hasNext())
        {
            return;
        }
        int pageIndex = 0;
        Entry<Integer, PDPageLabelRange> lastEntry = iterator.next();
        while (iterator.hasNext())
        {
            Entry<Integer, PDPageLabelRange> entry = iterator.next();
            int numPages = entry.getKey() - lastEntry.getKey();
            LabelGenerator gen = new LabelGenerator(lastEntry.getValue(),
                    numPages);
            while (gen.hasNext())
            {
                handler.newLabel(pageIndex, gen.next());
                pageIndex++;
            }
            lastEntry = entry;
        }
        LabelGenerator gen = new LabelGenerator(lastEntry.getValue(), 
                numberOfPages - lastEntry.getKey());
        while (gen.hasNext())
        {
            handler.newLabel(pageIndex, gen.next());
            pageIndex++;
        }
    }

    /**
     * Generates the labels in a page range.
     * 
     * @author Igor Podolskiy
     * 
     */
    private static class LabelGenerator implements Iterator<String>
    {
        private final PDPageLabelRange labelInfo;
        private final int numPages;
        private int currentPage;

        LabelGenerator(PDPageLabelRange label, int pages)
        {
            this.labelInfo = label;
            this.numPages = pages;
            this.currentPage = 0;
        }

        @Override
        public boolean hasNext()
        {
            return currentPage < numPages;
        }

        @Override
        public String next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            StringBuilder buf = new StringBuilder();
            String label = labelInfo.getPrefix();
            if (label != null)
            {
                // there may be some labels with some null bytes at the end
                // which will lead to an incomplete output, see PDFBOX-1047
                int index = label.indexOf(0);
                if (index > -1)
                {
                    label = label.substring(0, index);
                }
                buf.append(label);
            }
            String style = labelInfo.getStyle();
            if (style != null)
            {
                buf.append(getNumber(labelInfo.getStart() + currentPage, style));
            }
            currentPage++;
            return buf.toString();
        }

        private String getNumber(int pageIndex, String style)
        {
            if (style != null)
            {
                switch (style)
                {
                    case PDPageLabelRange.STYLE_DECIMAL:
                        return Integer.toString(pageIndex);
                    case PDPageLabelRange.STYLE_LETTERS_LOWER:
                        return makeLetterLabel(pageIndex);
                    case PDPageLabelRange.STYLE_LETTERS_UPPER:
                        return makeLetterLabel(pageIndex).toUpperCase();
                    case PDPageLabelRange.STYLE_ROMAN_LOWER:
                        return makeRomanLabel(pageIndex);
                    case PDPageLabelRange.STYLE_ROMAN_UPPER:
                        return makeRomanLabel(pageIndex).toUpperCase();
                    default:
                        break;
                }
            }
            // Fall back to decimals.
            return Integer.toString(pageIndex);
        }

        /**
         * Lookup table used by the {@link #makeRomanLabel(int)} method.
         */
        private static final String[][] ROMANS = new String[][] {
            { "", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix" },
            { "", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc" },
            { "", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm" }
        };

        private static String makeRomanLabel(int pageIndex)
        {
            StringBuilder buf = new StringBuilder();
            int power = 0;
            while (power < 3 && pageIndex > 0)
            {
                buf.insert(0, ROMANS[power][pageIndex % 10]);
                pageIndex /= 10;
                power++;
            }
            // Prepend as many m as there are thousands (which is
            // incorrect by the roman numeral rules for numbers > 3999,
            // but is unbounded and Adobe Acrobat does it this way).
            // This code is somewhat inefficient for really big numbers,
            // but those don't occur too often (and the numbers in those cases
            // would be incomprehensible even if we and Adobe
            // used strict Roman rules).
            for (int i = 0; i < pageIndex; i++)
            {
                buf.insert(0, 'm');
            }
            return buf.toString();
        }

        /**
         * a..z, aa..zz, aaa..zzz ... labeling as described in PDF32000-1:2008,
         * Table 159, Page 375.
         */
        private static String makeLetterLabel(int num)
        {
            StringBuilder buf = new StringBuilder();
            int numLetters = num / 26 + Integer.signum(num % 26);
            int letter = num % 26 + 26 * (1 - Integer.signum(num % 26)) + 'a' - 1;
            for (int i = 0; i < numLetters; i++)
            {
                buf.appendCodePoint(letter);
            }
            return buf.toString();
        }

        @Override
        public void remove()
        {
            // This is a generator, no removing allowed.
            throw new UnsupportedOperationException();
        }
    }
}
