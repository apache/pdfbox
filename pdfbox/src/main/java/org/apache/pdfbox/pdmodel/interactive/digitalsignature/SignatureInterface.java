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

import org.apache.pdfbox.exceptions.SignatureException;

/**
 * Providing an interface for accessing necessary functions for signing a pdf document.
 * 
 * @author <a href="mailto:mail@thomas-chojecki.de">Thomas Chojecki</a>
 * @version $
 */
public interface SignatureInterface
{

  /**
   * Creates a cms signature for the given content
   * 
   * @param content is the content as a (Filter)InputStream
   * @return signature as a byte array
   */
  public byte[] sign (InputStream content) throws SignatureException, IOException;
  
}
