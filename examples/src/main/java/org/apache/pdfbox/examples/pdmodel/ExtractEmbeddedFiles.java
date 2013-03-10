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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

/**
 * This is an example on how to extract all embedded files from a PDF document.
 * <p>
 * Usage: java org.apache.pdfbox.examples.pdmodel.ExtractEmbeddedFiles &lt;input-pdf&gt;
 *
 * @version $Revision$
 */
public class ExtractEmbeddedFiles
{
    private ExtractEmbeddedFiles()
    {
    }

    /**
     * This is the main method.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            usage();
            System.exit(1);
        }
        else
        {
            PDDocument document = null;
            try
            {
                File pdfFile = new File(args[0]);
                String filePath = pdfFile.getParent() + System.getProperty("file.separator");
                document = PDDocument.load( pdfFile );
                if (document.isEncrypted()) 
                {
                    try
                    {
                        document.decrypt("");
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: The document is encrypted." );
                    }
                    catch( org.apache.pdfbox.exceptions.CryptographyException e )
                    {
                        e.printStackTrace();
                    }
                }
                PDDocumentNameDictionary namesDictionary = 
                        new PDDocumentNameDictionary( document.getDocumentCatalog() );
                PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
                if (efTree != null)
                {
                    Map<String,COSObjectable> names = efTree.getNames();
                    if (names != null)
                    {
                        extractFiles(names, filePath);
                    }
                    else
                    {
                        List<PDNameTreeNode> kids = efTree.getKids();
                        for (PDNameTreeNode node : kids)
                        {
                            names = node.getNames();
                            extractFiles(names, filePath);
                        }
                    }
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }


    private static void extractFiles(Map<String,COSObjectable> names, String filePath) 
            throws IOException
    {
        for (String filename : names.keySet())
        {
            PDComplexFileSpecification fileSpec = (PDComplexFileSpecification)names.get(filename);
            PDEmbeddedFile embeddedFile = fileSpec.getEmbeddedFile();
            String embeddedFilename = filePath+filename;
            File file = new File(filePath+filename);
            System.out.println("Writing "+ embeddedFilename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(embeddedFile.getByteArray());
            fos.close();
        }
    }
    /**
     * This will print the usage for this program.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + ExtractEmbeddedFiles.class.getName() + " <input-pdf>" );
    }
}
