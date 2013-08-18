/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Version;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.parser.XmlResultParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is a simple main class used to check the validity of a pdf file.
 * 
 * Usage : java net.awl.edoc.pdfa.Validator <file path>
 * 
 * @author gbailleul
 * 
 */
public class Validator_A1b
{

    public static void main(String[] args) throws Exception
    {
        if (args.length == 0)
        {
            usage();
            System.exit(1);
        }

        // is output xml ?
        int posFile = 0;
        boolean outputXml = "xml".equals(args[posFile]);
        posFile += outputXml?1:0;
        // list
        boolean isGroup = "group".equals(args[posFile]);
        posFile += isGroup?1:0;
        // list
        boolean isBatch = "batch".equals(args[posFile]);
        posFile += isBatch?1:0;

        if (isGroup||isBatch) {
            // prepare the list
            List<File> ftp = listFiles(args[posFile]);
            int status = 0;
            if (!outputXml) {
                // simple list of files
                for (File file2 : ftp)
                {
                    status |= runSimple(new FileDataSource(file2));
                }
                System.exit(status);
            } else {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                XmlResultParser xrp = new XmlResultParser();
                if (isGroup) {
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element root = document.createElement("preflights");
                    document.appendChild(root);
                    root.setAttribute("count", String.format("%d", ftp.size()));
                    for (File file : ftp)
                    {
                        Element result = xrp.validate(document,new FileDataSource(file));
                        root.appendChild(result);
                    }
                    transformer.transform(new DOMSource(document), new StreamResult(new File(args[posFile]+".preflight.xml")));
                } else {
                    // isBatch
                    for (File file : ftp)
                    {
                        Element result = xrp.validate(new FileDataSource(file));
                        Document document = result.getOwnerDocument();
                        document.appendChild(result);
                        transformer.transform(new DOMSource(document), new StreamResult(new File(file.getAbsolutePath()+".preflight.xml")));
                    }
                }
            }



        } else {
            // only one file
            FileDataSource fd = new FileDataSource(args[posFile]);
            if (!outputXml) {
                // simple validation 
                System.exit(runSimple(fd));
            } else {
                // generate xml output
                XmlResultParser xrp = new XmlResultParser();
                Element result = xrp.validate(fd);
                Document document = result.getOwnerDocument();
                document.appendChild(result);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(new DOMSource(document), new StreamResult(System.out));
            }
        }

    }

    private static void usage () {
        System.out.println("Usage : java org.apache.pdfbox.preflight.Validator_A1b [xml] [mode] <file path>");
        System.out.println();
        System.out.println(" * xml : if set, generate xml output");
        System.out.println(" * mode : if set, <file path> must be a file containing PDF to parse, can have 2 values");
        System.out.println("       batch : for each file of the list and xml file is generated");
        System.out.println("       group : generate an xml result for all the file of the list.");
        System.out.println("Version : " + Version.getVersion());
    }

    private static int runSimple (DataSource fd) throws Exception {
        ValidationResult result = null;
        PreflightParser parser = new PreflightParser(fd);
        try
        {
            parser.parse();
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            document.close();
        }
        catch (SyntaxValidationException e)
        {
            result = e.getResult();
        }

        if (result.isValid())
        {
            System.out.println("The file " + fd.getName() + " is a valid PDF/A-1b file");
            System.out.println();
            return 0;
        }
        else
        {
            System.out.println("The file" + fd.getName() + " is not valid, error(s) :");
            for (ValidationError error : result.getErrorsList())
            {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
            System.out.println();
            return -1;
        }

    }


    private static List<File> listFiles (String path) throws IOException {
        List<File> files = new ArrayList<File>();
        File f = new File(path);
        if (f.isFile()) {
            FileReader fr = new FileReader(f);
            BufferedReader buf = new BufferedReader(fr);
            while (buf.ready()) {
                File fn = new File(buf.readLine());
                if (fn.exists()) {
                    files.add(fn);
                } // else warn ?
            }
            IOUtils.closeQuietly(buf);
        } else {
            files.addAll(Arrays.asList(f.listFiles()));
        }
        return files;
    }
}
