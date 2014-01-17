/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fontbox.type1;

import org.apache.fontbox.cff.Type1CharString;

import java.io.IOException;

/**
 * A high-level mapping from character codes to glyphs.
 *
 * @author John Hewson
 */
public interface Type1Mapping
{
    /**
     * Returns the Type 1 CharString for the character.
     *
     * @return the Type 1 CharString
     * @throws java.io.IOException if an error occurs during reading
     */
    public Type1CharString getType1CharString() throws IOException;

    /**
     * Gets the value for the code.
     *
     * @return the code
     */
    public int getCode();

    /**
     * Gets the value for the name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Gets the value for the bytes.
     *
     * @return the bytes
     */
    public byte[] getBytes();
}
