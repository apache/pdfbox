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

/**
 * A name object specifying how the document shall be displayed when opened.
 *
 * @author John Hewson
 */
public enum PageMode
{
    /** Neither the outline nor the thumbnails are displayed. */
    USE_NONE("UseNone"),

    /** Show bookmarks when pdf is opened. */
    USE_OUTLINES("UseOutlines"),

    /** Show thumbnails when pdf is opened. */
    USE_THUMBS("UseThumbs"),

    /** Full screen mode with no menu bar, window controls. */
    FULL_SCREEN("FullScreen"),

    /** Optional content group panel is visible when opened. */
    USE_OPTIONAL_CONTENT("UseOC"),

    /** Attachments panel is visible. */
    USE_ATTACHMENTS("UseAttachments");

    public static PageMode fromString(String value)
    {
        if (value.equals("UseNone"))
        {
            return USE_NONE;
        }
        else if (value.equals("UseOutlines"))
        {
            return USE_OUTLINES;
        }
        else if (value.equals("UseThumbs"))
        {
            return USE_THUMBS;
        }
        else if (value.equals("FullScreen"))
        {
            return FULL_SCREEN;
        }
        else if (value.equals("UseOC"))
        {
            return USE_OPTIONAL_CONTENT;
        }
        else if (value.equals("UseAttachments"))
        {
            return USE_ATTACHMENTS;
        }
        throw new IllegalArgumentException(value);
    }

    private final String value;

    PageMode(String value)
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
