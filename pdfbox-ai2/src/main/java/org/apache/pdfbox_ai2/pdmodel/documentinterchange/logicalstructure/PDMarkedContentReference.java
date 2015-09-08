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
package org.apache.pdfbox_ai2.pdmodel.documentinterchange.logicalstructure;

import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.pdmodel.PDPage;
import org.apache.pdfbox_ai2.pdmodel.common.COSObjectable;

/**
 * A marked-content reference.
 * 
 * @author Johannes Koch
 */
public class PDMarkedContentReference implements COSObjectable
{
    public static final String TYPE = "MCR";

    private final COSDictionary dictionary;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return this.dictionary;
    }

    /**
     * Default constructor
     */
    public PDMarkedContentReference()
    {
        this.dictionary = new COSDictionary();
        this.dictionary.setName(COSName.TYPE, TYPE);
    }

    /**
     * Constructor for an existing marked content reference.
     * 
     * @param dictionary the page dictionary
     */
    public PDMarkedContentReference(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    /**
     * Gets the page.
     * 
     * @return the page
     */
    public PDPage getPage()
    {
        COSDictionary pg = (COSDictionary) this.getCOSObject().getDictionaryObject(COSName.PG);
        if (pg != null)
        {
            return new PDPage(pg);
        }
        return null;
    }

    /**
     * Sets the page.
     * 
     * @param page the page
     */
    public void setPage(PDPage page)
    {
        this.getCOSObject().setItem(COSName.PG, page);
    }

    /**
     * Gets the marked content identifier.
     * 
     * @return the marked content identifier
     */
    public int getMCID()
    {
        return this.getCOSObject().getInt(COSName.MCID);
    }

    /**
     * Sets the marked content identifier.
     * 
     * @param mcid the marked content identifier
     */
    public void setMCID(int mcid)
    {
        this.getCOSObject().setInt(COSName.MCID, mcid);
    }


    @Override
    public String toString()
    {
        return new StringBuilder().append("mcid=").append(this.getMCID()).toString();
    }

}
