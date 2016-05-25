/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License), Version 2.0
 * (the "License")), you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing), software
 * distributed under the License is distributed on an "AS IS" BASIS),
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND), either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel;

/**
 * A name object specifying the page layout shall be used when the document is opened.
 *
 * @author John Hewson
 */
public enum PageLayout
{
    /** Display one page at a time. */
    SINGLE_PAGE("SinglePage"),

    /**  Display the pages in one column. */
    ONE_COLUMN("OneColumn"),

    /** Display the pages in two columns), with odd numbered pages on the left. */
    TWO_COLUMN_LEFT("TwoColumnLeft"),

    /** Display the pages in two columns), with odd numbered pages on the right.  */
    TWO_COLUMN_RIGHT("TwoColumnRight"),

    /** Display the pages two at a time), with odd-numbered pages on the left. */
    TWO_PAGE_LEFT("TwoPageLeft"),

    /** Display the pages two at a time), with odd-numbered pages on the right. */
    TWO_PAGE_RIGHT("TwoPageRight");

    public static PageLayout fromString(String value)
    {
        if (value.equals("SinglePage"))
        {
            return SINGLE_PAGE;
        }
        else if (value.equals("OneColumn"))
        {
            return ONE_COLUMN;
        }
        else if (value.equals("TwoColumnLeft"))
        {
            return TWO_COLUMN_LEFT;
        }
        else if (value.equals("TwoColumnRight"))
        {
            return TWO_COLUMN_RIGHT;
        }
        else if (value.equals("TwoPageLeft"))
        {
            return TWO_PAGE_LEFT;
        }
        else if (value.equals("TwoPageRight"))
        {
            return TWO_PAGE_RIGHT;
        }
        throw new IllegalArgumentException(value);
    }

    private final String value;

    PageLayout(String value)
    {
        this.value = value;
    }

    /**
     * Returns the string value, as used in a PDF file.
     */
    public String stringValue()
    {
        return value;
    }
}
