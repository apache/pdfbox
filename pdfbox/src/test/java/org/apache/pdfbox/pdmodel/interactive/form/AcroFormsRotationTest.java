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

package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.TestPDFToImage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AcroFormsRotationTest
{

    private static final File OUT_DIR = new File("target/test-output");
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "AcroFormsRotation.pdf";
    private static final String TEST_VALUE = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,"
            + " sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.";

    private PDDocument document;
    private PDAcroForm acroForm;

    @Before
    public void setUp() throws IOException
    {
        document = PDDocument.load(new File(IN_DIR, NAME_OF_PDF));
        acroForm = document.getDocumentCatalog().getAcroForm();
        OUT_DIR.mkdirs();
    }

    @Test
    public void fillFields() throws IOException
    {

        // portrait page
        // single line fields
        PDField field = acroForm.getField("pdfbox.portrait.single.rotation0");
        field.setValue(field.getFullyQualifiedName());
        field = acroForm.getField("pdfbox.portrait.single.rotation90");
        field.setValue(field.getFullyQualifiedName());
        field = acroForm.getField("pdfbox.portrait.single.rotation180");
        field.setValue(field.getFullyQualifiedName());
        field = acroForm.getField("pdfbox.portrait.single.rotation270");
        field.setValue(field.getFullyQualifiedName());

        // multiline fields
        field = acroForm.getField("pdfbox.portrait.multi.rotation0");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.portrait.multi.rotation90");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.portrait.multi.rotation180");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.portrait.multi.rotation270");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);

        // 90 degrees rotated page
        // single line fields
        field = acroForm.getField("pdfbox.page90.single.rotation0");
        field.setValue("pdfbox.page90.single.rotation0");
        field = acroForm.getField("pdfbox.page90.single.rotation90");
        field.setValue("pdfbox.page90.single.rotation90");
        field = acroForm.getField("pdfbox.page90.single.rotation180");
        field.setValue("pdfbox.page90.single.rotation180");
        field = acroForm.getField("pdfbox.page90.single.rotation270");
        field.setValue("pdfbox.page90.single.rotation270");

        // multiline fields
        field = acroForm.getField("pdfbox.page90.multi.rotation0");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.page90.multi.rotation90");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.page90.multi.rotation180");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);
        field = acroForm.getField("pdfbox.page90.multi.rotation270");
        field.setValue(field.getFullyQualifiedName() + "\n" + TEST_VALUE);

        // compare rendering
        File file = new File(OUT_DIR, NAME_OF_PDF);
        document.save(file);
        TestPDFToImage testPDFToImage = new TestPDFToImage(TestPDFToImage.class.getName());
        if (!testPDFToImage.doTestFile(file, IN_DIR.getAbsolutePath(), OUT_DIR.getAbsolutePath()))
        {
            // don't fail, rendering is different on different systems, result
            // must be viewed manually
            System.err.println("Rendering of " + file + " failed or is not identical to expected rendering in " + IN_DIR
                    + " directory");
        }
    }

    @After
    public void tearDown() throws IOException
    {
        document.close();
    }

}
