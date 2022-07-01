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
 * <p>Exception thrown when attempting to decode a truetype font file and a format
 * constraint is violated.</p>
 *
 * @author Glenn Adams
 */
public class AdvancedTypographicTableFormatException extends RuntimeException {
    /**
     * Instantiate ATT format exception.
     */
    public AdvancedTypographicTableFormatException() {
        super();
    }
    /**
     * Instantiate ATT format exception.
     * @param message a message string
     */
    public AdvancedTypographicTableFormatException(String message) {
        super(message);
    }
    /**
     * Instantiate ATT format exception.
     * @param message a message string
     * @param cause a <code>Throwable</code> that caused this exception
     */
    public AdvancedTypographicTableFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
