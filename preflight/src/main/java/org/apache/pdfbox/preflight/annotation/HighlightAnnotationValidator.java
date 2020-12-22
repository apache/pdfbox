/* ***************************************************************************

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.

  */
package org.apache.pdfbox.preflight.annotation;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.preflight.PreflightContext;

/**
 * Validation class for the Highlight Annotation
 */
public class HighlightAnnotationValidator extends MarkupAnnotationValidator
{
    protected HighlightAnnotationValidator(final PreflightContext ctx, final COSDictionary annotDictionary)
    {
        super(ctx, annotDictionary);
        this.pdMarkup = new PDAnnotationHighlight(annotDictionary);
        this.pdAnnot = this.pdMarkup;
    }
}
