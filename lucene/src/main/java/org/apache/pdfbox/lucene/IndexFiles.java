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
package org.apache.pdfbox.lucene;

/*
 * This source was originally written as an example for the lucene project.
 * It has been modified to use PDFBox as a  lucene document creator.
 * -Ben Litchfield
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.demo.HTMLDocument;

import org.apache.lucene.document.Document;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import java.util.Arrays;


import java.io.File;
import java.io.IOException;

import java.util.Date;


/**
 * This is a class that will index some files on a local filesystem.  This code
 * was modified from a demo that comes with the lucene search engine.
 *
 * @author Lucene team
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 *
 * @version $Revision: 1.8 $
 */
public class IndexFiles
{
    private boolean deleting = false;     // true during deletion pass
    private IndexReader reader;       // existing index
    private IndexWriter writer;       // new index being built
    private TermEnum uidIter;         // document id iterator

    /**
     * This is the main entry point for the indexer.
     *
     * @param argv The command line arguments.
     */
    public static void main(String[] argv)
    {

        String index = "index";
        boolean create = false;
        File root = null;

        String usage = "org.apache.pdfbox.searchengine.lucene.IndexFiles [-create] [-index <index>] <root_directory>";

        if (argv.length == 0)
        {
            System.err.println("Usage: " + usage);
            return;
        }

        for (int i = 0; i < argv.length; i++)
        {
            if (argv[i].equals("-index"))
            {         // parse -index option
                index = argv[++i];
            }
            else if (argv[i].equals("-create"))
            {     // parse -create option
                create = true;
            }
            else if (i != argv.length-1)
            {
                System.err.println("Usage: " + usage);
                return;
            }
            else
            {
                System.out.println( "root=" +argv[i] );
                root = new File(argv[i]);
            }
        }
        IndexFiles indexer = new IndexFiles();
        indexer.index( root, create, index );
    }

    /**
     * This will index a directory.
     *
     * @param root The root directory to start indexing.
     * @param create Should we create a new index?
     * @param index The name of the index.
     */
    public void index( File root, boolean create, String index )
    {

        try
        {
            Date start = new Date();

            writer = new IndexWriter(index, new StandardAnalyzer(), create, IndexWriter.MaxFieldLength.LIMITED);

            if (!create)
            {                 // delete stale docs
                deleting = true;
                indexDocs(root, index, create);
            }

            indexDocs(root, index, create);       // add new docs

            System.out.println("Optimizing index...");
            writer.optimize();
            writer.close();

            Date end = new Date();

            System.out.print(end.getTime() - start.getTime());
            System.out.println(" total milliseconds");

        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Walk directory hierarchy in uid order, while keeping uid iterator from
     * existing index in sync.  Mismatches indicate one of: (a) old documents to
     * be deleted; (b) unchanged documents, to be left alone; or (c) new
     * documents, to be indexed.
     *
     * @param file The directory to index.
     * @param index The index to add the file to.
     * @param create A flag telling if we should create the index.
     *
     * @throws Exception If there is any error indexing the directory.
     */
    private void indexDocs(File file, String index, boolean create) throws Exception
    {
        if (!create)
        {                 // incrementally update

            reader = IndexReader.open(index);         // open existing index
            uidIter = reader.terms(new Term("uid", "")); // init uid iterator

            indexDocs(file);

            if (deleting)
            {                 // delete rest of stale docs
                while (uidIter.term() != null && uidIter.term().field().equals( "uid" ) )
                {
                    System.out.println("deleting " +
                    HTMLDocument.uid2url(uidIter.term().text()));
                    reader.deleteDocuments(uidIter.term());
                    uidIter.next();
                }
                deleting = false;
            }

            uidIter.close();                  // close uid iterator
            reader.close();               // close existing index

        }
        else
        {
            indexDocs(file);
        }
    }


    private void indexDocs(File file) throws Exception
    {
        if (file.isDirectory())
        {             // if a directory
            String[] files = file.list();         // list its files
            Arrays.sort(files);           // sort the files
            for (int i = 0; i < files.length; i++)    // recursively index them
            {
                indexDocs(new File(file, files[i]));
            }
        }
        else
        {
            if (uidIter != null)
            {
                String uid = HTMLDocument.uid(file);      // construct uid for doc

                while( uidIter.term() != null &&
                uidIter.term().field().equals( "uid" ) &&
                uidIter.term().text().compareTo(uid) < 0)
                {
                    if (deleting)
                    {             // delete stale docs
                        System.out.println("deleting " +
                        HTMLDocument.uid2url(uidIter.term().text()));
                        reader.deleteDocuments(uidIter.term());
                    }
                    uidIter.next();
                }
                if( uidIter.term() != null &&
                uidIter.term().field().equals( "uid" ) &&
                uidIter.term().text().compareTo(uid) == 0)
                {
                    System.out.println( "Next uid=" +uidIter );
                    uidIter.next();           // keep matching docs
                }
            }
            else
            {
                try
                {
                    addDocument( file );
                }
                catch( IOException e )
                {
                    //catch exception and move onto the next document
                    System.out.println( e.getMessage() );
                }
            }
        }
    }

    private void addDocument( File file ) throws IOException, InterruptedException
    {
        String path = file.getName().toUpperCase();
        Document doc = null;
        //Gee, this would be a great place for a command pattern
        if( path.endsWith(".HTML") || // index .html files
            path.endsWith(".HTM") || // index .htm files
            path.endsWith(".TXT"))
        {
            System.out.println( "Indexing Text document: " + file );
            doc = HTMLDocument.Document(file);
        }
        else if( path.endsWith( ".PDF" ) )
        {
            System.out.println( "Indexing PDF document: " + file );
            doc = LucenePDFDocument.getDocument( file );
        }
        else
        {
            System.out.println( "Skipping " + file );
        }

        if( doc != null )
        {
            writer.addDocument(doc);
        }
    }
}
