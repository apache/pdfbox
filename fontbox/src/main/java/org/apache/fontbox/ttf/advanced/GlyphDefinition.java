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

package org.apache.fontbox.ttf.advanced;

/**
 * <p>The <code>GlyphDefinition</code> interface is a marker interface implemented by a glyph definition
 * subtable.</p>
 *
 * @author Glenn Adams
 */
public interface GlyphDefinition {

    /**
     * Determine if some definition is available for a specific glyph.
     * @param gi a glyph index
     * @return true if some (unspecified) definition is available for the specified glyph
     */
    boolean hasDefinition(int gi);

}
