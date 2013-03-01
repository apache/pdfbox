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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * <p>This represents a pdf signature build dictionary as specified in
 * <a href="http://partners.adobe.com/public/developer/en/acrobat/Acrobat_Signature_BuildDict.pdf">
 * http://partners.adobe.com/public/developer/en/acrobat/Acrobat_Signature_BuildDict.pdf</a></p>
 *
 * <p>The signature build properties dictionary provides signature properties for the software
 * application that was used to create the signature.</p>
 *
 * @author Thomas Chojecki
 * @version $Revision: 1.1 $
 */
public class PDPropBuild implements COSObjectable
{

    private COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDPropBuild()
    {
        dictionary = new COSDictionary();
        dictionary.setDirect(true); // the specification claim to use direct objects
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDPropBuild(COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true); // the specification claim to use direct objects
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return getDictionary();
    }

    /**
     * Convert this standard java object to a COS dictionary.
     *
     * @return The COS dictionary that matches this Java object.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * A build data dictionary for the signature handler that was
     * used to create the parent signature.
     *
     * @return the Filter as PDPropBuildFilter object
     */
    public PDPropBuildDataDict getFilter()
    {
        PDPropBuildDataDict filter = null;
        COSDictionary filterDic = (COSDictionary)dictionary.getDictionaryObject(COSName.FILTER);
        if (filterDic != null)
        {
            filter = new PDPropBuildDataDict(filterDic);
        }
        return filter;
    }

    /**
     * Set the build data dictionary for the signature handler.
     * This entry is optional but is highly recommended for the signatures.
     *
     * @param filter is the PDPropBuildFilter
     */
    public void setPDPropBuildFilter(PDPropBuildDataDict filter)
    {
        dictionary.setItem(COSName.FILTER, filter);
    }

    /**
     * A build data dictionary for the PubSec software module
     * that was used to create the parent signature.
     *
     * @return the PubSec as PDPropBuildPubSec object
     */
    public PDPropBuildDataDict getPubSec()
    {
        PDPropBuildDataDict pubSec = null;
        COSDictionary pubSecDic = (COSDictionary)dictionary.getDictionaryObject(COSName.PUB_SEC);
        if (pubSecDic != null)
        {
            pubSec = new PDPropBuildDataDict(pubSecDic);
        }
        return pubSec;
    }

    /**
     * Set the build data dictionary for the PubSec Software module.
     *
     * @param pubSec is the PDPropBuildPubSec
     */
    public void setPDPropBuildPubSec(PDPropBuildDataDict pubSec)
    {
        dictionary.setItem(COSName.PUB_SEC, pubSec);
    }

    /**
     * A build data dictionary for the viewing application software
     * module that was used to create the parent signature.
     *
     * @return the App as PDPropBuildApp object
     */
    public PDPropBuildDataDict getApp()
    {
        PDPropBuildDataDict app = null;
        COSDictionary appDic = (COSDictionary)dictionary.getDictionaryObject(COSName.APP);
        if (appDic != null)
        {
            app = new PDPropBuildDataDict(appDic);
        }
        return app;
    }

    /**
     * Set the build data dictionary for the viewing application
     * software module.
     *
     * @param app is the PDPropBuildApp
     */
    public void setPDPropBuildApp(PDPropBuildDataDict app)
    {
        dictionary.setItem(COSName.APP, app);
    }
}
