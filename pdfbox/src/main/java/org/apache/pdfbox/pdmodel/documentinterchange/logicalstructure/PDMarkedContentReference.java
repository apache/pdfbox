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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A marked-content reference.
 * 
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 */
public class PDMarkedContentReference implements COSObjectable
{

    public static final String TYPE = "MCR";

    private COSDictionary dictionary;

    protected COSDictionary getCOSDictionary()
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
     * @param pageDic the page dictionary
     * @param mcid the marked content indentifier
     */
    public PDMarkedContentReference(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return this.dictionary;
    }

    /**
     * Gets the page.
     * 
     * @return the page
     */
    public PDPage getPage()
    {
        COSDictionary pg = (COSDictionary) this.getCOSDictionary()
            .getDictionaryObject(COSName.PG);
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
        this.getCOSDictionary().setItem(COSName.PG, page);
    }

    /**
     * Gets the marked content identifier.
     * 
     * @return the marked content identifier
     */
    public int getMCID()
    {
        return this.getCOSDictionary().getInt(COSName.MCID);
    }

    /**
     * Sets the marked content identifier.
     * 
     * @param mcid the marked content identifier
     */
    public void setMCID(int mcid)
    {
        this.getCOSDictionary().setInt(COSName.MCID, mcid);
    }


    @Override
    public String toString()
    {
        return new StringBuilder()
            .append("mcid=").append(this.getMCID()).toString();
    }

}
