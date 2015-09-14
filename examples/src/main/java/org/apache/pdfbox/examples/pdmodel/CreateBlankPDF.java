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
package org.apache.pdfbox.examples.pdmodel;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Create a blank PDF and write the contents to a file.
 */
public final class CreateBlankPDF
{
    private CreateBlankPDF()
    {        
    }
    
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("usage: " + CreateBlankPDF.class.getName() + " <outputfile.pdf>");
            System.exit(1);
        }

        String filename = args[0];
        
        PDDocument doc = new PDDocument();
        try
        {
            // a valid PDF document requires at least one page
            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
            doc.save(filename);
        }
        finally
        {
            doc.close();
        }
    }
}
