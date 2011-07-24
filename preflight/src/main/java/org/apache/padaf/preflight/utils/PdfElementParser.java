/*****************************************************************************
 * 
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
 * 
 ****************************************************************************/

package org.apache.padaf.preflight.utils;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.BaseParser;

/**
 * This class is a tool to parse a byte array as a COS object (COSDIctionary)
 */
public class PdfElementParser extends BaseParser {

  /**
   * Create the PDFElementParser object.
   * 
   * @param cd
   *          a COSDocument which will be used to parse the byte array
   * @param input
   *          the byte array to parse
   * @throws IOException
   */
  public PdfElementParser(COSDocument cd, byte[] input) throws IOException {
    super(input);
    this.document = cd;
  }

  /**
   * Parse the input byte array of the constructor call as a COSDictionary.
   * 
   * @return a COSDictionary if the parsing succeed.
   * @throws IOException
   *           If the byte array isn't a COSDictionary or if there are an error
   *           on the stream parsing
   */
  public COSDictionary parseAsDictionary() throws IOException {
    return parseCOSDictionary();
  }

  /**
   * Return the COSDocument used to create this object.
   * 
   * @return
   */
  public COSDocument getDocument() {
    return this.document;
  }

}
