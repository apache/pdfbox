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

/**
 * This will specify whether to open the destination document in a new window.
 *
 * @author Tilman Hausherr
 */
public enum OpenMode
{
    /**
     * The viewer application should behave in accordance with the current user preference.
     */
    USER_PREFERENCE,

    /**
     * Destination document will replace the current document in the same window.
     */
    SAME_WINDOW,

    /**
     * Open the destination document in a new window.
     */
    NEW_WINDOW
}
