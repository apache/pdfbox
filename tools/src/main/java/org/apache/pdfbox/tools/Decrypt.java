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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

/**
 * This will read a document from the filesystem, decrypt it and and then write
 * the result to the filesystem.
 *
 * @author  Ben Litchfield
 */
public final class Decrypt
{
    private static final String ALIAS = "alias";
    private static final String PASSWORD = "password";
    private static final String KEYSTORE = "keyStore";
    
    private String password;
    private String infile;
    private String outfile;
    private String alias;
    private String keyStore;


    private Decrypt()
    {
    }
    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws IOException If there is an error decrypting the document.
     */
    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");
        
        Decrypt decrypt = new Decrypt();
        decrypt.parseCommandLine(args);
        decrypt.decrypt();
    }
    
    private void parseCommandLine(String[] args)
    {
        Options options = defineOptions();
        CommandLine commandLine = parseArguments(options, args);
        
        this.alias = commandLine.getOptionValue(ALIAS);
        this.password = commandLine.getOptionValue(PASSWORD, "");
        this.keyStore = commandLine.getOptionValue(KEYSTORE);
        
        // get the additional command line parameters 
        // and handle these as the file names being passed
        List<String> fileNames = commandLine.getArgList();
        if (fileNames.isEmpty() || fileNames.size() > 2 )
        {
            usage(options);
        }
        
        this.infile = fileNames.get(0);
        
        if (fileNames.size() == 1)
        {
            this.outfile = fileNames.get(0);
        }
        else
        {
            this.outfile = fileNames.get(1);
        }
                
    }
    
    private static Options defineOptions()
    {
        Options options = new Options();
        
        options.addOption(Option.builder(ALIAS)
                    .hasArg()
                    .desc("The alias of the key in the certificate file (mandatory if several keys are available).")
                    .build()
               );
       options.addOption(Option.builder(PASSWORD)
                   .hasArg()
                   .desc("The password to open the certificate and extract the private key from it.")
                   .build()
               );
       options.addOption(Option.builder(KEYSTORE)
                   .hasArg()
                   .desc("The KeyStore that holds the certificate.")
                   .build()
               );
       return options;
    }
    
    private static CommandLine parseArguments(Options options, String[] commandLineArguments)
    {
        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try
        {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
        }
        catch (ParseException parseException)
        {
            System.out.println(parseException.getMessage());
            usage(options);
        }
        return commandLine;
    }

   private void decrypt() throws IOException
    {
        PDDocument document = null;
        InputStream keyStoreStream = null;
        try
        {
            if( keyStore != null )
            {
                keyStoreStream = new FileInputStream(keyStore);
            }
            document = PDDocument.load(new File(infile), password, keyStoreStream, alias);
            
            if (document.isEncrypted())
            {
                AccessPermission ap = document.getCurrentAccessPermission();
                if(ap.isOwnerPermission())
                {
                    document.setAllSecurityToBeRemoved(true);
                    document.save( outfile );
                }
                else
                {
                    throw new IOException(
                            "Error: You are only allowed to decrypt a document with the owner password." );
                }
            }
            else
            {
                System.err.println( "Error: Document is not encrypted." );
            }
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
            IOUtils.closeQuietly(keyStoreStream);
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        String syntax = "java -jar pdfbox-app-x.y.z.jar Decrypt [options] <inputfile> [outputfile]";
        String header = "\nOptions";

        formatter.setWidth(132);
        formatter.printHelp(syntax, header, options, "");
        
        System.exit(1);
    }

}
