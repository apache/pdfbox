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

package org.apache.padaf.preflight.contentstream;

import java.io.IOException;

/**
 * This exception inherits from the IOException to be thrown by classes which
 * inherit from org.apache.pdfbox.util.PDFStreamEngine.
 * 
 * This exception contains a validationError code.
 */
public class ContentStreamException extends IOException {
  private String validationError = "";

  public ContentStreamException() {
    super();
  }

  public ContentStreamException(String arg0, Throwable arg1) {
    super(arg0);
  }

  public ContentStreamException(String arg0) {
    super(arg0);
  }

  public ContentStreamException(Throwable arg0) {
    super(arg0.getMessage());
  }

  /**
   * @return the validationError
   */
  public String getValidationError() {
    return validationError;
  }

  /**
   * @param validationError
   *          the validationError to set
   */
  public void setValidationError(String validationError) {
    this.validationError = validationError;
  }

}
