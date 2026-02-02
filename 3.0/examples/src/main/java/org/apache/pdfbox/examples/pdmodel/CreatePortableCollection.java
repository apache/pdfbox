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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

/**
 * This is an example on how to create a portable collection PDF, as described in the PDF 1.7
 * specification in chapter 12.3.5. It uses the COS methods because there are not any PD classes
 * yet. If you want to help, we'd need PDCollection, PDCollectionField, PDCollectionSort and
 * PDCollectionItem.
 *
 * @author Tilman Hausherr
 */
public class CreatePortableCollection
{

    /**
     * Constructor.
     */
    private CreatePortableCollection()
    {
    }

    /**
     * Create a portable collection PDF with two files.
     *
     * @param file The file to write the PDF to.
     *
     * @throws IOException If there is an error writing the data.
     */
    public void doIt(String file) throws IOException
    {
        try (PDDocument doc = new PDDocument())
        {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page))
            {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Example of a portable collection");
                contentStream.endText();
            }

            //embedded files are stored in a named tree
            PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

            //first create the file specification, which holds the embedded file
            PDComplexFileSpecification fs1 = new PDComplexFileSpecification();

            // use both methods for backwards, cross-platform and cross-language compatibility.
            fs1.setFile("Test1.txt");
            fs1.setFileUnicode("Test1.txt");

            //create a dummy file stream, this would probably normally be a FileInputStream
            byte[] data1 = "This is the contents of the first embedded file".getBytes(StandardCharsets.ISO_8859_1);
            PDEmbeddedFile ef1 = new PDEmbeddedFile(doc, new ByteArrayInputStream(data1), COSName.FLATE_DECODE);
            //now lets some of the optional parameters
            ef1.setSubtype("text/plain");
            ef1.setSize(data1.length);
            ef1.setCreationDate(new GregorianCalendar());

            // use both methods for backwards, cross-platform and cross-language compatibility.
            fs1.setEmbeddedFile(ef1);
            fs1.setEmbeddedFileUnicode(ef1);
            fs1.setFileDescription("The first file");

            //first create the file specification, which holds the embedded file
            PDComplexFileSpecification fs2 = new PDComplexFileSpecification();

            // use both methods for backwards, cross-platform and cross-language compatibility.
            fs2.setFile("Test2.txt");
            fs2.setFileUnicode("Test2.txt");

            //create a dummy file stream, this would probably normally be a FileInputStream
            byte[] data2 = "This is the contents of the second embedded file".getBytes(StandardCharsets.ISO_8859_1);
            PDEmbeddedFile ef2 = new PDEmbeddedFile(doc, new ByteArrayInputStream(data2), COSName.FLATE_DECODE);
            //now lets some of the optional parameters
            ef2.setSubtype("text/plain");
            ef2.setSize(data2.length);
            ef2.setCreationDate(new GregorianCalendar());

            // use both methods for backwards, cross-platform and cross-language compatibility.
            fs2.setEmbeddedFile(ef2);
            fs2.setEmbeddedFileUnicode(ef2);
            fs2.setFileDescription("The second file");

            Map<String, PDComplexFileSpecification> map = new HashMap<>();
            map.put("Attachment 1", fs1);
            map.put("Attachment 2", fs2);

            // create a new tree node and add the embedded file
            PDEmbeddedFilesNameTreeNode treeNode = new PDEmbeddedFilesNameTreeNode();
            treeNode.setNames(map);
            // add the new node as kid to the root node
            List<PDEmbeddedFilesNameTreeNode> kids = new ArrayList<>();
            kids.add(treeNode);
            efTree.setKids(kids);

            // add the tree to the document catalog
            PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
            names.setEmbeddedFiles(efTree);
            doc.getDocumentCatalog().setNames(names);

            // show attachments panel in some viewers 
            doc.getDocumentCatalog().setPageMode(PageMode.USE_ATTACHMENTS);

            // create collection directory
            COSDictionary collectionDic = new COSDictionary();
            COSDictionary schemaDict = new COSDictionary();
            schemaDict.setItem(COSName.TYPE, COSName.COLLECTION_SCHEMA);
            COSDictionary sortDic = new COSDictionary();
            sortDic.setItem(COSName.TYPE, COSName.COLLECTION_SORT);
            sortDic.setString(COSName.A, "true"); // sort ascending
            // "it identifies a field described in the parent collection dictionary"
            // sort by field 2
            sortDic.setItem(COSName.S, COSName.getPDFName("fieldtwo"));
            collectionDic.setItem(COSName.TYPE, COSName.COLLECTION);
            collectionDic.setItem(COSName.SCHEMA, schemaDict);
            collectionDic.setItem(COSName.SORT, sortDic);
            collectionDic.setItem(COSName.VIEW, COSName.D); // Details mode
            COSDictionary fieldDict1 = new COSDictionary();
            fieldDict1.setItem(COSName.TYPE, COSName.COLLECTION_FIELD);
            fieldDict1.setItem(COSName.SUBTYPE, COSName.S); // type: text field
            fieldDict1.setString(COSName.N, "field header one (description)"); // header text
            fieldDict1.setInt(COSName.O, 1); // order on the screen
            COSDictionary fieldDict2 = new COSDictionary();
            fieldDict2.setItem(COSName.TYPE, COSName.COLLECTION_FIELD);
            fieldDict2.setItem(COSName.SUBTYPE, COSName.S); // type: text field
            fieldDict2.setString(COSName.N, "field header two (name)");
            fieldDict2.setInt(COSName.O, 2);
            COSDictionary fieldDict3 = new COSDictionary();
            fieldDict3.setItem(COSName.TYPE, COSName.COLLECTION_FIELD);
            fieldDict3.setItem(COSName.SUBTYPE, COSName.N); // type: number field
            fieldDict3.setString(COSName.N, "field header three (size)");
            fieldDict3.setInt(COSName.O, 3);
            schemaDict.setItem("fieldone", fieldDict1); // field name (this is a key)
            schemaDict.setItem("fieldtwo", fieldDict2);
            schemaDict.setItem("fieldthree", fieldDict3);
            doc.getDocumentCatalog().getCOSObject().setItem(COSName.COLLECTION, collectionDic);
            doc.getDocumentCatalog().setVersion("1.7");

            // collection item dictionary with fields for 1st file
            COSDictionary ciDict1 = new COSDictionary();
            ciDict1.setItem(COSName.TYPE, COSName.COLLECTION_ITEM);
            // use the field names from earlier
            ciDict1.setString("fieldone", fs1.getFileDescription());
            ciDict1.setString("fieldtwo", fs1.getFile());
            ciDict1.setInt("fieldthree", fs1.getEmbeddedFile().getSize());
            fs1.getCOSObject().setItem(COSName.CI, ciDict1);

            // collection item dictionary with fields for 2nd file
            COSDictionary ciDict2 = new COSDictionary();
            ciDict2.setItem(COSName.TYPE, COSName.COLLECTION_ITEM);
            // use the field names from earlier
            ciDict2.setString("fieldone", fs2.getFileDescription());
            ciDict2.setString("fieldtwo", fs2.getFile());
            ciDict2.setInt("fieldthree", fs2.getEmbeddedFile().getSize());
            fs2.getCOSObject().setItem(COSName.CI, ciDict2);

            doc.save(file);
        }
    }

    /**
     * This will create a portable collection PDF.
     * <br>
     * see usage() for commandline
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws IOException
    {
        CreatePortableCollection app = new CreatePortableCollection();
        if (args.length != 1)
        {
            app.usage();
        }
        else
        {
            app.doIt(args[0]);
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println("usage: " + this.getClass().getName() + " <output-file>");
    }
}
