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

package org.apache.padaf.preflight.font;

import java.awt.Image;
import java.io.IOException;
import java.util.List;


import org.apache.fontbox.util.BoundingBox;
import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.utils.ContentStreamEngine;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDInlinedImage;
import org.apache.pdfbox.util.ImageParameters;
import org.apache.pdfbox.util.PDFOperator;

/**
 * This class is used to parse a glyph of a Type3 font program. If the glyph is
 * parsed without error, the width of the glyph is accessible through the
 * getWidth method.
 */
public class PDFAType3StreamParser extends ContentStreamEngine {
  private boolean firstOperator = true;
  private float width = 0;

  private PDInlinedImage image = null;
  private BoundingBox box = null;

  public PDFAType3StreamParser(DocumentHandler handler) {
    super(handler);
  }

  /**
   * This will parse a type3 stream and create an image from it.
   * 
   * @param type3Stream
   *          The stream containing the operators to draw the image.
   * 
   * @return The image that was created.
   * 
   * @throws IOException
   *           If there is an error processing the stream.
   */
  public Image createImage(COSStream type3Stream) throws IOException {
  	resetEngine();
    processSubStream(null, null, type3Stream);
    return image.createImage();
  }

  /**
   * This is used to handle an operation.
   * 
   * @param operator
   *          The operation to perform.
   * @param arguments
   *          The list of arguments.
   * 
   * @throws IOException
   *           If there is an error processing the operation.
   */
  protected void processOperator(PDFOperator operator, List arguments)
      throws IOException {
    super.processOperator(operator, arguments);
    String operation = operator.getOperation();

    if (operation.equals("BI")) {
      ImageParameters params = operator.getImageParameters();
      image = new PDInlinedImage();
      image.setImageParameters(params);
      image.setImageData(operator.getImageData());

      validImageFilter(operator);
      validImageColorSpace(operator);
    }

    if (operation.equals("d0")) {
      // set glyph with for a type3 font
      // COSNumber horizontalWidth = (COSNumber)arguments.get( 0 );
      // COSNumber verticalWidth = (COSNumber)arguments.get( 1 );
      // width = horizontalWidth.intValue();
      // height = verticalWidth.intValue();

      checkType3FirstOperator(arguments);

    } else if (operation.equals("d1")) {
      // set glyph with and bounding box for type 3 font
      // COSNumber horizontalWidth = (COSNumber)arguments.get( 0 );
      // COSNumber verticalWidth = (COSNumber)arguments.get( 1 );
      COSNumber llx = (COSNumber) arguments.get(2);
      COSNumber lly = (COSNumber) arguments.get(3);
      COSNumber urx = (COSNumber) arguments.get(4);
      COSNumber ury = (COSNumber) arguments.get(5);

      // width = horizontalWidth.intValue();
      // height = verticalWidth.intValue();
      box = new BoundingBox();
      box.setLowerLeftX(llx.floatValue());
      box.setLowerLeftY(lly.floatValue());
      box.setUpperRightX(urx.floatValue());
      box.setUpperRightY(ury.floatValue());

      checkType3FirstOperator(arguments);
    }

    checkColorOperators(operation);
    validRenderingIntent(operator, arguments);
    checkSetColorSpaceOperators(operator, arguments);
    validNumberOfGraphicStates(operator);
    firstOperator = false;
  }

  /**
   * According to the PDF Reference, the first operator in a CharProc of a Type3
   * font must be "d0" or "d1". This method process this validation. This method
   * is called by the processOperator method.
   * 
   * @param arguments
   * @throws IOException
   */
  private void checkType3FirstOperator(List arguments) throws IOException {
    if (!firstOperator) {
      throw new IOException("Type3 CharProc : First operator must be d0 or d1");
    }

    Object obj = arguments.get(0);
    if (obj instanceof Number) {
      width = ((Number) obj).intValue();
    } else if (obj instanceof COSInteger) {
      width = ((COSInteger) obj).floatValue();
    } else if (obj instanceof COSFloat) {
    	width = ((COSFloat)obj).floatValue();
    } else {
      throw new IOException(
          "Unexpected argument type. Expected : COSInteger or Number / Received : "
              + obj.getClass().getName());
    }
  }

  /**
   * @return the width of the CharProc glyph description
   */
  public float getWidth() {
    return this.width;
  }
}
