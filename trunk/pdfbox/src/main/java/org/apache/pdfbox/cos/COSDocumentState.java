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
package org.apache.pdfbox.cos;

/**
 * An instance of {@link COSDocumentState} collects all known states a {@link COSDocument} may have and shall allow
 * their evaluation.
 *
 * @author Christian Appl
 * @see COSDocument
 */
public class COSDocumentState
{
    
    /**
     * The parsing state of the document.
     * <ul>
     * <li>{@code true}, if the document is currently being parsed. (initial state)</li>
     * <li>{@code false}, if the document's parsing completed and it may be edited and updated.</li>
     * </ul>
     */
    private boolean parsing = true;
    
    /**
     * Sets the {@link #parsing} state of the document.
     *
     * @param parsing The {@link #parsing} state to set.
     */
    public void setParsing(boolean parsing)
    {
        this.parsing = parsing;
    }
    
    /**
     * Returns {@code true}, if the document´s {@link #parsing} is completed and it may be updated.
     *
     * @return {@code true}, if the document´s {@link #parsing} is completed and it may be updated.
     */
    public boolean isAcceptingUpdates()
    {
        return !parsing;
    }
    
}
