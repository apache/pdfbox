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
package org.apache.pdfbox.pdmodel.fixup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormDefaultsProcessor;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormGenerateAppearancesProcessor;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormOrphanWidgetsProcessor;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

public class AcroFormDefaultFixup extends AbstractFixup
{ 
    public AcroFormDefaultFixup(PDDocument document)
    { 
        super(document); 
    }

    @Override
    public void apply() {
        new AcroFormDefaultsProcessor(document).process();

        /*
         * Get the AcroForm in it's current state.
         *
         * Also note: getAcroForm() applies a default fixup which this processor
         * is part of. So keep the null parameter otherwise this will end
         * in an endless recursive call
         */
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm(null);

        // PDFBOX-4985
        // build the visual appearance as there is none for the widgets
        if (acroForm != null && acroForm.getNeedAppearances())
        {
            if (acroForm.getFields().isEmpty())
            {
                new AcroFormOrphanWidgetsProcessor(document).process();
            }
            
            // PDFBOX-4985
            // build the visual appearance as there is none for the widgets
            new AcroFormGenerateAppearancesProcessor(document).process();
        }
    }
} 