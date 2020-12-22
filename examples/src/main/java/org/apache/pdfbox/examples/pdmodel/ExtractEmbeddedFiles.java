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
    public static void main(final String[] args ) throws IOException
    {
        if( args.length != 1 )
        {
            usage();
            System.exit(1);
        }

        final File pdfFile = new File(args[0]);
        final String filePath = pdfFile.getParent() + System.getProperty("file.separator");
        try (PDDocument document = Loader.loadPDF(pdfFile))
        {
            final PDDocumentNameDictionary namesDictionary =
                    new PDDocumentNameDictionary(document.getDocumentCatalog());
            final PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
            if (efTree != null)
            {
                extractFilesFromEFTree(efTree, filePath);
            }

            // extract files from page annotations
            for (final PDPage page : document.getPages())
            {
                extractFilesFromPage(page, filePath);
            }
        }
    }

    private static void extractFilesFromPage(final PDPage page, final String filePath) throws IOException
    {
        for (final PDAnnotation annotation : page.getAnnotations())
        {
            if (annotation instanceof PDAnnotationFileAttachment)
            {
                final PDAnnotationFileAttachment annotationFileAttachment = (PDAnnotationFileAttachment) annotation;
                final PDFileSpecification fileSpec = annotationFileAttachment.getFile();
                if (fileSpec instanceof PDComplexFileSpecification)
                {
                    final PDComplexFileSpecification complexFileSpec = (PDComplexFileSpecification) fileSpec;
                    final PDEmbeddedFile embeddedFile = getEmbeddedFile(complexFileSpec);
                    if (embeddedFile != null)
                    {
                        extractFile(filePath, complexFileSpec.getFilename(), embeddedFile);
                    }
                }
            }
        }
    }

    private static void extractFilesFromEFTree(final PDEmbeddedFilesNameTreeNode efTree, final String filePath) throws IOException
    {
        Map<String, PDComplexFileSpecification> names = efTree.getNames();
        if (names != null)
        {
            extractFiles(names, filePath);
        }
        else
        {
            final List<PDNameTreeNode<PDComplexFileSpecification>> kids = efTree.getKids();
            for (final PDNameTreeNode<PDComplexFileSpecification> node : kids)
            {
                names = node.getNames();
                extractFiles(names, filePath);
            }
        }
    }

    private static void extractFiles(final Map<String, PDComplexFileSpecification> names, final String filePath)
            throws IOException
    {
        for (final Entry<String, PDComplexFileSpecification> entry : names.entrySet())
        {
            final PDComplexFileSpecification fileSpec = entry.getValue();
            final PDEmbeddedFile embeddedFile = getEmbeddedFile(fileSpec);
            if (embeddedFile != null)
            {
                extractFile(filePath, fileSpec.getFilename(), embeddedFile);
            }
        }
    }

    private static void extractFile(final String filePath, final String filename, final PDEmbeddedFile embeddedFile)
            throws IOException
    {
        final String embeddedFilename = filePath + filename;
        final File file = new File(filePath + filename);
        System.out.println("Writing " + embeddedFilename);
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            fos.write(embeddedFile.toByteArray());
        }
    }
    
    private static PDEmbeddedFile getEmbeddedFile(final PDComplexFileSpecification fileSpec )
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
