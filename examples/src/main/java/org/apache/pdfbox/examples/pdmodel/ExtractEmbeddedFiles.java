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
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;

/**
 * This is an example on how to extract all embedded files from a PDF document.
 *
 */
public final class ExtractEmbeddedFiles
{
    private ExtractEmbeddedFiles()
    {
    }

    /**
     * This is the main method.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 1 )
        {
            usage();
            System.exit(1);
        }

        File pdfFile = new File(args[0]);
        String filePath = pdfFile.getParent() + FileSystems.getDefault().getSeparator();
        try (PDDocument document = Loader.loadPDF(pdfFile))
        {
            PDDocumentNameDictionary namesDictionary =
                    new PDDocumentNameDictionary(document.getDocumentCatalog());
            PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
            if (efTree != null)
            {
                extractFilesFromEFTree(efTree, filePath);
            }

            // extract files from page annotations
            for (PDPage page : document.getPages())
            {
                extractFilesFromPage(page, filePath);
            }
        }
    }

    private static void extractFilesFromPage(PDPage page, String filePath) throws IOException
    {
        for (PDAnnotation annotation : page.getAnnotations())
        {
            if (annotation instanceof PDAnnotationFileAttachment)
            {
                PDAnnotationFileAttachment annotationFileAttachment = (PDAnnotationFileAttachment) annotation;
                PDFileSpecification fileSpec = annotationFileAttachment.getFile();
                if (fileSpec instanceof PDComplexFileSpecification)
                {
                    PDComplexFileSpecification complexFileSpec = (PDComplexFileSpecification) fileSpec;
                    PDEmbeddedFile embeddedFile = getEmbeddedFile(complexFileSpec);
                    if (embeddedFile != null)
                    {
                        extractFile(filePath, complexFileSpec.getFilename(), embeddedFile);
                    }
                }
            }
        }
    }

    private static void extractFilesFromEFTree(PDNameTreeNode<PDComplexFileSpecification> efTree, String filePath) throws IOException
    {
        Map<String, PDComplexFileSpecification> names = efTree.getNames();
        if (names != null)
        {
            extractFiles(names, filePath);
        }
        else
        {
            List<PDNameTreeNode<PDComplexFileSpecification>> kids = efTree.getKids();
            if (kids == null)
            {
                return;
            }
            for (PDNameTreeNode<PDComplexFileSpecification> node : kids)
            {
                extractFilesFromEFTree(node, filePath);
            }
        }
    }

    private static void extractFiles(Map<String, PDComplexFileSpecification> names, String filePath) 
            throws IOException
    {
        for (Entry<String, PDComplexFileSpecification> entry : names.entrySet())
        {
            PDComplexFileSpecification fileSpec = entry.getValue();
            PDEmbeddedFile embeddedFile = getEmbeddedFile(fileSpec);
            if (embeddedFile != null)
            {
                extractFile(filePath, fileSpec.getFilename(), embeddedFile);
            }
        }
    }

    private static void extractFile(String filePath, String filename, PDEmbeddedFile embeddedFile)
            throws IOException
    {
        String embeddedFilename = filePath + filename;
        File file = new File(embeddedFilename);
        File parentDir = file.getParentFile();
        if (!parentDir.exists())
        {
            // sometimes paths contain a directory
            System.out.println("Creating " + parentDir);
            parentDir.mkdirs();
        }
        System.out.println("Writing " + embeddedFilename);
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            fos.write(embeddedFile.toByteArray());
        }
    }
    
    private static PDEmbeddedFile getEmbeddedFile(PDComplexFileSpecification fileSpec )
    {
        // search for the first available alternative of the embedded file
        PDEmbeddedFile embeddedFile = null;
        if (fileSpec != null)
        {
            embeddedFile = fileSpec.getEmbeddedFileUnicode(); 
            if (embeddedFile == null)
            {
                embeddedFile = fileSpec.getEmbeddedFileDos();
            }
            if (embeddedFile == null)
            {
                embeddedFile = fileSpec.getEmbeddedFileMac();
            }
            if (embeddedFile == null)
            {
                embeddedFile = fileSpec.getEmbeddedFileUnix();
            }
            if (embeddedFile == null)
            {
                embeddedFile = fileSpec.getEmbeddedFile();
            }
        }
        return embeddedFile;
    }
    
    /**
     * This will print the usage for this program.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + ExtractEmbeddedFiles.class.getName() + " <input-pdf>" );
    }
}
