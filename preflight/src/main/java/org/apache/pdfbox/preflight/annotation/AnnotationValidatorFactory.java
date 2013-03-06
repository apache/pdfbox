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

package org.apache.pdfbox.preflight.annotation;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_ANNOT_FORBIDDEN_SUBTYPE;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.action.ActionManagerFactory;
import org.apache.pdfbox.preflight.exception.ValidationException;

public abstract class AnnotationValidatorFactory
{
    protected ActionManagerFactory actionFact = null;

    protected Map<String, Class<? extends AnnotationValidator>> validatorClasses = new HashMap<String, Class<? extends AnnotationValidator>>();

    public AnnotationValidatorFactory()
    {
        initializeClasses();
    }

    public AnnotationValidatorFactory(ActionManagerFactory actionFact)
    {
        super();
        this.actionFact = actionFact;
    }

    public final void setActionFact(ActionManagerFactory _actionFact)
    {
        this.actionFact = _actionFact;
    }

    /**
     * Initialize the map of Validation classes used to create a validation object according to the Annotation subtype.
     */
    protected abstract void initializeClasses();

    /**
     * Return an instance of AnnotationValidator.
     * 
     * @param ctx
     * @param annotDic
     * @return
     */
    public final AnnotationValidator getAnnotationValidator(PreflightContext ctx, COSDictionary annotDic)
            throws ValidationException
    {

        AnnotationValidator result = null;
        String subtype = annotDic.getNameAsString(COSName.SUBTYPE);
        Class<? extends AnnotationValidator> clazz = this.validatorClasses.get(subtype);

        if (clazz == null)
        {
            ctx.addValidationError(new ValidationError(ERROR_ANNOT_FORBIDDEN_SUBTYPE, "The subtype isn't authorized : "
                    + subtype));
        }
        else
        {
            try
            {
                Constructor<? extends AnnotationValidator> constructor = clazz.getConstructor(PreflightContext.class,
                        COSDictionary.class);
                result = constructor.newInstance(ctx, annotDic);
                result.setFactory(this);
            }
            catch (Exception e)
            {
                throw new ValidationException(e.getMessage());
            }
        }
        return result;
    }
}
