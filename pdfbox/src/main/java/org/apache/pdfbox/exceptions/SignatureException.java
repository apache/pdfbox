package org.apache.pdfbox.exceptions;

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

/**
 * An exception that indicates a problem during the signing process.
 *
 * @author Thomas Chojecki
 * @version $Revision: $
 */
public class SignatureException extends Exception
{

    public final static int WRONG_PASSWORD = 1;

    public final static int UNSUPPORTED_OPERATION = 2;
    
    public final static int CERT_PATH_CHECK_INVALID = 3;
    
    public final static int NO_SUCH_ALGORITHM = 4;
    
    public final static int INVALID_PAGE_FOR_SIGNATURE = 5;

    public final static int VISUAL_SIGNATURE_INVALID = 6;

    private int no;
  
    /**
     * Constructor.
     *
     * @param msg A msg to go with this exception.
     */
    public SignatureException( String msg )
    {
        super( msg );
    }

    /**
     * Constructor.
     * 
     * @param errno A error number to fulfill this exception
     * @param msg A msg to go with this exception.
     */
    public SignatureException( int errno , String msg ) 
    {
      super( msg );
      no = errno;
    }

    /**
     * Constructor.
     * 
     * @param e The exception that should be encapsulate.
     */
    public SignatureException(Throwable e) 
    {
      super(e);
    }
    
    /**
     * Constructor.
     * 
     * @param errno A error number to fulfill this exception
     * @param e The exception that should be encapsulate.
     */
    public SignatureException( int errno, Throwable e) 
    {
      super(e);
    }

    /**
     * A error number to fulfill this exception
     * 
     * @return the error number if available, otherwise 0
     */
    public int getErrNo() 
    {
      return no;
    }
}
