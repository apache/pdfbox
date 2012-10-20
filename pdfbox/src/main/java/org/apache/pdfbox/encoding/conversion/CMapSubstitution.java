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
package org.apache.pdfbox.encoding.conversion;

import java.util.HashMap;

/**
 * This class provides a mapping from char code to unicode mapping files used for CJK-encoding.
 * @author Andreas Lehmkühler
 * @version $Revision: 1.0 $
 *
 */

public class CMapSubstitution 
{

    private static HashMap<String,String> cmapSubstitutions = new HashMap<String,String>();

    private CMapSubstitution()
    {
    }

    static 
    {
        // I don't know if these mappings are complete. Perhaps there 
        // has to be added still one or more

        // chinese simplified
        cmapSubstitutions.put( "Adobe-GB1-4", "Adobe-GB1-UCS2" );
        cmapSubstitutions.put( "GBK-EUC-H", "GBK-EUC-UCS2" );
        cmapSubstitutions.put( "GBK-EUC-V", "GBK-EUC-UCS2" );
        cmapSubstitutions.put( "GBpc-EUC-H", "GBpc-EUC-UCS2C" );
        cmapSubstitutions.put( "GBpc-EUC-V", "GBpc-EUC-UCS2C" );

        // chinese traditional
        cmapSubstitutions.put( "Adobe-CNS1-4", "Adobe-CNS1-UCS2" );
        cmapSubstitutions.put( "B5pc-H", "B5pc-UCS2" );
        cmapSubstitutions.put( "B5pc-V", "B5pc-UCS2" );
        cmapSubstitutions.put( "ETen-B5-H", "ETen-B5-UCS2" );
        cmapSubstitutions.put( "ETen-B5-V", "ETen-B5-UCS2" );
        cmapSubstitutions.put( "ETenms-B5-H", "ETen-B5-UCS2" );
        cmapSubstitutions.put( "ETenms-B5-V", "ETen-B5-UCS2" );

        // japanese
        cmapSubstitutions.put( "90ms-RKSJ-H", "90ms-RKSJ-UCS2" );
        cmapSubstitutions.put( "90ms-RKSJ-V", "90ms-RKSJ-UCS2" );
        cmapSubstitutions.put( "90msp-RKSJ-H", "90ms-RKSJ-UCS2" );
        cmapSubstitutions.put( "90msp-RKSJ-V", "90ms-RKSJ-UCS2" );
        cmapSubstitutions.put( "90pv-RKSJ-H", "90pv-RKSJ-UCS2");
        cmapSubstitutions.put( "UniJIS-UCS2-HW-H", "UniJIS-UCS2-H" );
        cmapSubstitutions.put( "Adobe-Japan1-4", "Adobe-Japan1-UCS2");

        cmapSubstitutions.put( "Adobe-Identity-0", "Identity-H");
        cmapSubstitutions.put( "Adobe-Identity-1", "Identity-H");
    }

    /**
     * 
     * @param cmapName The name of a cmap for which we have to find a possible substitution
     * @return the substitution for the given cmap name
     */
    public static String substituteCMap(String cmapName) 
    {
        if (cmapSubstitutions.containsKey(cmapName))
        {
            return (String)cmapSubstitutions.get(cmapName);
        }
        return cmapName;
    }
}
