/**
 * Copyright (c) 2003-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.encryption.AccessPermission;
import org.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.pdfbox.pdmodel.encryption.PublicKeyDecryptionMaterial;
import org.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;

/**
 * This will read a document from the filesystem, decrypt it and and then write
 * the results to the filesystem. <br/><br/>
 *
 * usage: java org.pdfbox.Decrypt &lt;password&gt; &lt;inputfile&gt; &lt;outputfile&gt;
 *
 * @author  <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class Decrypt
{
    private static final String ALIAS = "-alias";
    private static final String PASSWORD = "-password";
    private static final String KEYSTORE = "-keyStore";
    
    
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
        if( args.length < 2 || args.length > 3 )
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
        System.err.println( "usage: java org.pdfbox.Decrypt " + 
                            "[options] <inputfile> [outputfile]" );
        System.err.println( "-alias      The alias of the key in the certificate file " + 
                                         "(mandatory if several keys are available)");
        System.err.println( "-password   The password to open the certificate and extract the private key from it." );
        System.err.println( "-keyStore   The KeyStore that holds the certificate." );
        System.exit( -1 );
    }

}