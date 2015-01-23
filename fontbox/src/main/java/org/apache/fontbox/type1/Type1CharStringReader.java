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

package org.apache.fontbox.type1;

import org.apache.fontbox.cff.Type1CharString;
import java.io.IOException;

/**
 * Something which can read Type 1 CharStrings, namely Type 1 and CFF fonts.
 *
 * @author John Hewson
 */
public interface Type1CharStringReader
{
    /**
     * Returns the Type 1 CharString for the character with the given name.
     *
     * @return Type 1 CharString
     * @throws IOException if something went wrong
     */
    Type1CharString getType1CharString(String name) throws IOException;
}
