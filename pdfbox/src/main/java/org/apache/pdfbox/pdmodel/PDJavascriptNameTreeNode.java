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
package org.apache.pdfbox.pdmodel;

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionJavaScript;

/**
 * This class holds all of the name trees that are available at the document level.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.1 $
 */
public class PDJavascriptNameTreeNode extends PDNameTreeNode
{
    /**
     * Constructor.
     */
    public PDJavascriptNameTreeNode()
    {
        super( PDTextStream.class );
    }

    /**
     * Constructor.
     *
     * @param dic The COS dictionary.
     */
    public PDJavascriptNameTreeNode( COSDictionary dic )
    {
        super( dic, PDTextStream.class );
    }

    /**
     * {@inheritDoc}
     */
    protected COSObjectable convertCOSToPD( COSBase base ) throws IOException
    {
        if (!(base instanceof COSDictionary))
        {
            throw new IOException( "Error creating Javascript object, expected a COSDictionary and not " + base);
        }
        return (PDActionJavaScript)PDActionFactory.createAction((COSDictionary) base);
    }

    /**
     * {@inheritDoc}
     */
    protected PDNameTreeNode createChildNode( COSDictionary dic )
    {
        return new PDJavascriptNameTreeNode(dic);
    }
}
