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
package org.apache.pdfbox.service.interactive.form;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * (Re-) Generate the appearance for a field.
 * 
 * The fields appearance defines the 'look' the field has when it's rendered for display or printing.
 * 
 */

public final class AppearanceGenerator
{

    private static final Log LOG = LogFactory.getLog(AppearanceGenerator.class);

    private AppearanceGenerator()
    {
    }

    /**
     * Generate the appearances for a single field.
     * 
     * @param field The field which appearances need to be generated.
     */
    public static void generateFieldAppearances(PDField field)
    {
        if (field instanceof PDVariableText)
        {
            PDAppearanceString pdAppearance = new PDAppearanceString(field.getAcroForm(),
                    (PDVariableText) field);

            Object fieldValue = null;
            
            try
            {
                fieldValue = field.getValue();
            }
            catch (IOException e)
            {
                // TODO: Implement the removal of the appearance as this 
                //       exception occurred either because the value couldn't be read or
                //       the type is not valid for the field.

            }
            
            // TODO: implement the handling for additional values.
            if (fieldValue instanceof String)
            {
                try
                {
                    pdAppearance.setAppearanceValue((String) fieldValue);
                }
                catch (IOException e)
                {
                    LOG.debug("Unable to generate the field appearance.", e);
                }
            } 
            else if (fieldValue instanceof PDTextStream)
            {
                try
                {
                    pdAppearance.setAppearanceValue(((PDTextStream) fieldValue).getAsString());
                }
                catch (IOException e)
                {
                    LOG.debug("Unable to generate the field appearance.", e);
                }                
            }
            else if (fieldValue != null)
            {
                LOG.debug("Can't generate the appearance for values typed "
                        + fieldValue.getClass().getName() + ".");
            }
        }
        // TODO: implement the handling for additional field types
        else
        {
            LOG.debug("Unable to generate the field appearance.");
        }
    }
}
