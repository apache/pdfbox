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
package org.apache.pdfbox.cos;

import org.apache.pdfbox.filter.FilterManager;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

import org.apache.pdfbox.exceptions.COSVisitorException;

/**
 * The base object that all objects in the PDF document will extend.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public abstract class COSBase implements COSObjectable
{
    /**
     * Constructor.
     */
    public COSBase()
    {
    }

    /**
     * This will get the filter manager to use to filter streams.
     *
     * @return The filter manager.
     */
    public FilterManager getFilterManager()
    {
        /**
         * @todo move this to PDFdocument or something better
         */
        return new FilterManager();
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return this;
    }



    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public abstract Object accept(ICOSVisitor visitor) throws COSVisitorException;
}
