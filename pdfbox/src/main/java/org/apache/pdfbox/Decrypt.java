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
package org.apache.pdfbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

/**
 * This will read a document from the filesystem, decrypt it and and then write
 * the results to the filesystem. <br/><br/>
 *
 * usage: java org.apache.pdfbox.Decrypt &lt;password&gt; &lt;inputfile&gt; &lt;outputfile&gt;
 *
 * @author  <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class Decrypt
{
    private static final String ALIAS = "-alias";
    private static final String PASSWORD = "-password";
    private static final String KEYSTORE = "-keyStore";

    private Decrypt()
    {
    }
    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main( String[] args ) throws Exception
    {
        Decrypt decrypt = new Decrypt();
        decrypt.decrypt( args );
    }

    private void decrypt( String[] args ) throws Exception
    {
        if( args.length < 2 || args.length > 5 )
        {
            usage();
        }
        else
        {
            String password = null;
            String infile = null;
            String outfile = null;
            String alias = null;
            String keyStore = null;
            for( int i=0; i<args.length; i++ )
            {
                if( args[i].equals( ALIAS ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    alias = args[i];
                }
                else if( args[i].equals( KEYSTORE ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    keyStore = args[i];
                }
                else if( args[i].equals( PASSWORD ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    password = args[i];
                }
                else if( infile == null )
                {
                    infile = args[i];
                }
                else if( outfile == null )
                {
                    outfile = args[i];
                }
                else
                {
                    usage();
                }
            }
            if( infile == null )
            {
                usage();
            }
            if( outfile == null )
            {
                outfile = infile;
            }
            if( password == null )
            {
                password = "";
            }


            PDDocument document = null;

            try
            {
                document = PDDocument.load( infile );

                if( document.isEncrypted() )
                {
                    DecryptionMaterial decryptionMaterial = null;
                    if( keyStore != null )
                    {
                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        ks.load(new FileInputStream(keyStore), password.toCharArray());

                        decryptionMaterial = new PublicKeyDecryptionMaterial(ks, alias, password);
                    }
                    else
                    {
                        decryptionMaterial = new StandardDecryptionMaterial(password);
                    }
                    document.openProtection(decryptionMaterial);
                    AccessPermission ap = document.getCurrentAccessPermission();
                    if(ap.isOwnerPermission())
                    {
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
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java -jar pdfbox-app-x.y.z.jar Decrypt " +
                            "[options] <inputfile> [outputfile]" );
        System.err.println( "-alias      The alias of the key in the certificate file " +
                                         "(mandatory if several keys are available)");
        System.err.println( "-password   The password to open the certificate and extract the private key from it." );
        System.err.println( "-keyStore   The KeyStore that holds the certificate." );
        System.exit( -1 );
    }

}
