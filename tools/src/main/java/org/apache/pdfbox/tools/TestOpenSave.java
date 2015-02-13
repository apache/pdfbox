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
package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

public class TestOpenSave
{

    /**
     * Constructor.
     */
    public TestOpenSave()
    {
        super();
    }

    /**
     * This will perform the document reading, decoding and writing.
     *
     * @param in The filename used for input.
     * @param out The filename used for output.
     *
     * @throws IOException if the output could not be written
     */
    public void doIt(String in, String out)
            throws IOException
    {
        PDDocument doc = null;
        try
        {
        	doc = PDDocument.load(new File(in), "");
            doc.getDocumentCatalog();
            doc.save( out );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    /**
     * This will write a PDF document with completely decoded streams.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        TestOpenSave app = new TestOpenSave();
//        app.doIt("/home/lehmi/workspace/pdfs/PDFBOX2440-lipsum.pdf", "/home/lehmi/workspace/pdfs/PDFBOX2440-lipsum_new.pdf");
//        app.doIt("/home/lehmi/workspace/pdfs/PDFBOX2445-Apache_Solr_4.7_Ref_Guide.pdf", "/home/lehmi/workspace/pdfs/PDFBOX2445-Apache_Solr_4.7_Ref_Guide_new.pdf");
//        app.doIt("/home/lehmi/workspace/pdfs/PDFBOX2440-asy-functionshading.pdf", "/home/lehmi/workspace/pdfs/PDFBOX2440-asy-functionshading_new.pdf");
        app.doIt("/home/lehmi/workspace/pdfs/PDFBOX1128-test4.pdf", "/home/lehmi/workspace/pdfs/PDFBOX1128-test4_new.pdf");
               
    }

}
