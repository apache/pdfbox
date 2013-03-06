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

package org.apache.pdfbox.preflight.xobject;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.exception.ValidationException;

public class XObjPostscriptValidator extends AbstractXObjValidator
{

    public XObjPostscriptValidator(PreflightContext context, COSStream xobj)
    {
        super(context, xobj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#validate()
     */
    @Override
    public void validate() throws ValidationException
    {
        super.validate();
    }

    /*
     * (non-Javadoc)
     * 
     * @seenet.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator# checkMandatoryFields(java.util.List)
     */
    @Override
    protected void checkMandatoryFields()
    {
        // PostScript XObjects are forbidden. Whatever the result of this function,
        // the validation will fail
    }

}
