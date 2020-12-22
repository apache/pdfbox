/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.parser.XmlResultParser;
import org.apache.pdfbox.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is a simple main class used to check the validity of a pdf file.
 * 
 * Usage : java org.apache.pdfbox.preflight.Validator_A1b &lt;file path&gt;
 * 
 * @author gbailleul
 * 
 */
public class Validator_A1b
{

    public static void main(final String[] args)
            throws IOException, TransformerException, ParserConfigurationException
    {
        if (args.length == 0)
        {
            usage();
            System.exit(1);
        }

        // is output xml ?
        int posFile = 0;
        final boolean outputXml = "xml".equals(args[posFile]);
        posFile += outputXml?1:0;
        // list
        final boolean isGroup = "group".equals(args[posFile]);
        posFile += isGroup?1:0;
        // list
        final boolean isBatch = "batch".equals(args[posFile]);
        posFile += isBatch?1:0;

        if (isGroup||isBatch)
        {
            // prepare the list
            final List<File> ftp = listFiles(args[posFile]);
            int status = 0;
            if (!outputXml)
            {
                // simple list of files
                for (final File file2 : ftp)
                {
                    status |= runSimple(file2);
                }
                System.exit(status);
            }
            else
            {
                @SuppressWarnings({"squid:S2755"}) final // self-created XML
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                final XmlResultParser xrp = new XmlResultParser();
                if (isGroup)
                {
                    @SuppressWarnings({"squid:S2755"}) final // self-created XML
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    final Element root = document.createElement("preflights");
                    document.appendChild(root);
                    root.setAttribute("count", String.format("%d", ftp.size()));
                    for (final File file : ftp)
                    {
                        final Element result = xrp.validate(document, file);
                        root.appendChild(result);
                    }
                    transformer.transform(new DOMSource(document), 
                            new StreamResult(new File(args[posFile]+".preflight.xml")));
                }
                else
                {
                    // isBatch
                    for (final File file : ftp)
                    {
                        final Element result = xrp.validate(file);
                        final Document document = result.getOwnerDocument();
                        document.appendChild(result);
                        transformer.transform(new DOMSource(document), 
                                new StreamResult(new File(file.getAbsolutePath()+".preflight.xml")));
                    }
                }
            }
        } 
        else
        {
            if (!outputXml)
            {
                // simple validation 
                System.exit(runSimple(new File(args[posFile])));
            }
            else
            {
                // generate xml output
                final XmlResultParser xrp = new XmlResultParser();
                final Element result = xrp.validate(new File(args[posFile]));
                final Document document = result.getOwnerDocument();
                document.appendChild(result);
                @SuppressWarnings({"squid:S2755"}) final // self-created XML
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(new DOMSource(document), new StreamResult(System.out));
            }
        }

    }

    private static void usage()
    {
        final String version = Version.getVersion();

        System.out.println("Usage : java org.apache.pdfbox.preflight.Validator_A1b [xml] [<mode>] <file path>");
        System.out.println();
        System.out.println(" * xml : if set, generate xml output instead of text");
        System.out.println(" * <mode> : if set, <file path> must be a file containing the PDF files to parse. <mode> can have 2 values:");
        System.out.println("       batch : generate xml result files for each PDF file in the list");
        System.out.println("       group : generate one xml result file for all the PDF files in the list.");
        System.out.println("Version : " + version);
    }

    private static int runSimple(final File file) throws IOException
    {
        final ValidationResult result = PreflightParser.validate(file);

        if (result.isValid())
        {
            System.out.println("The file " + file.getName() + " is a valid PDF/A-1b file");
            System.out.println();
            return 0;
        }
        else
        {
            System.out.println("The file " + file.getName() + " is not a valid PDF/A-1b file, error(s) :");
            for (final ValidationError error : result.getErrorsList())
            {
                System.out.print(error.getErrorCode() + " : " + error.getDetails());
                if (error.getPageNumber() != null)
                {
                    System.out.println(" on page " + (error.getPageNumber() + 1));
                }
                else
                {
                    System.out.println();
                }
            }
            System.out.println();
            return -1;
        }

    }


    private static List<File> listFiles(final String path) throws IOException
    {
        final List<File> files = new ArrayList<>();
        final File f = new File(path);
        if (f.isFile())
        {
            final FileReader fr = new FileReader(f);
            try (BufferedReader bufferedReader = new BufferedReader(fr))
            {
                while (bufferedReader.ready())
                {
                    final File fn = new File(bufferedReader.readLine());
                    if (fn.exists())
                    {
                        files.add(fn);
                    } // else warn ?
                }
            }
        }
        else
        {
            final File[] fileList = f.listFiles();
            if (fileList != null)
            {
                files.addAll(Arrays.asList(fileList));
            }
        }
        return files;
    }
}
