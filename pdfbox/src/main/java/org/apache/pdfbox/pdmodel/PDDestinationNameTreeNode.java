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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

/**
 * This class holds all of the name trees that are available at the document level.
 *
 * @author Ben Litchfield
 */
public class PDDestinationNameTreeNode extends PDNameTreeNode<PDPageDestination>
{
    /**
     * Constructor.
     */
    public PDDestinationNameTreeNode()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param dic The COS dictionary.
     */
    public PDDestinationNameTreeNode(final COSDictionary dic )
    {
        super(dic);
    }

    @Override
    protected PDPageDestination convertCOSToPD(final COSBase base ) throws IOException
    {
        COSBase destination = base;
        if( base instanceof COSDictionary )
        {
            //the destination is sometimes stored in the D dictionary
            //entry instead of being directly an array, so just dereference
            //it for now
            destination = ((COSDictionary)base).getDictionaryObject( COSName.D );
        }
        return (PDPageDestination)PDDestination.create( destination );
    }

    @Override
    protected PDNameTreeNode<PDPageDestination> createChildNode(final COSDictionary dic )
    {
        return new PDDestinationNameTreeNode(dic);
    }
}
