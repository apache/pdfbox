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
package org.apache.fontbox.ttf;

/**
 * Thrown when the TrueType bytecode interpreter encounters a malformed or unsupported program. It is
 * unchecked so that opcode handlers stay terse; the caller catches it per glyph and falls back to raw,
 * unhinted coordinates rather than letting one bad glyph disable hinting for the whole font.
 *
 * @author Apache PDFBox
 */
class HintingException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * @param message describes the failure
     */
    public HintingException(String message)
    {
        super(message);
    }

    /**
     * @param message describes the failure
     * @param cause the underlying cause
     */
    public HintingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
