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

package org.apache.padaf.preflight.xmp;

import java.util.List;


import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class which all elements within an rdf:RDF have the same value for their
 * rdf:about attributes
 * 
 * @author Germain Costenobel
 * 
 */
public class RDFAboutAttributeConcordanceValidation {

  /**
   * 
   * @param metadata
   * @return
   * @throws DifferentRDFAboutException
   * @throws ValidationException
   */
  public void validateRDFAboutAttributes(XMPMetadata metadata)
      throws ValidationException, DifferentRDFAboutException {

    List<XMPSchema> schemas = metadata.getAllSchemas();
    if (schemas.size() == 0) {
      throw new ValidationException(
          "Schemas not found in the given metadata representation");
    }
    String about = schemas.get(0).getAboutValue();
    // rdf:description must have an about attribute, it has been checked during
    // parsing
    Element e;
    for (XMPSchema xmpSchema : schemas) {
      e = xmpSchema.getElement();
      checkRdfAbout(about, e);
    }

  }

  private void checkRdfAbout(String about, Element e)
      throws DifferentRDFAboutException {
    // System.out.println(e.getTagName());
    // TODO check if it need to test the 2 possibilities
    if (!e.getAttribute("rdf:about").equals(about)) {
      throw new DifferentRDFAboutException();
    }
    if (!e.getAttribute("about").equals(about)) {
      throw new DifferentRDFAboutException();
    }
    if (e.hasChildNodes()) {
      NodeList children = e.getChildNodes();
      int size = children.getLength();
      for (int i = 0; i < size; i++) {
        if (children.item(i) instanceof Element) {
          checkRdfAbout(about, (Element) children.item(i));
        }
      }
    }
  }

  public static class DifferentRDFAboutException extends Exception {

    private static final long serialVersionUID = 1L;

    public DifferentRDFAboutException() {
      super("all rdf:about in RDF:rdf must have the same value");
    }
  }
}
