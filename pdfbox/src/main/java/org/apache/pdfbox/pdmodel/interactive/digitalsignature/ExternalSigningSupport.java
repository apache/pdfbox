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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for external signature creation scenarios. It contains method for retrieving PDF data
 * to be sign and setting created CMS signature to the PDF.
 *
 */
public interface ExternalSigningSupport
{
    /**
     * Get PDF content to be signed. Obtained InputStream must be closed after use.
     *
     * @return content stream
     */
    InputStream getContent() throws IOException;

    /**
     * Set CMS signature bytes to PDF.
     *
     * @param signature CMS signature as byte array
     *
     * @throws IOException if exception occured during PDF writing
     */
    void setSignature(byte[] signature) throws IOException;
}
