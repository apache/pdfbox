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
import java.util.Iterator;

/**
 * This class represents PDF encoding name to Java charset name mapping
 *
 * @author  Pin Xue (http://www.pinxue.net), Holly Lee (holly.lee (at) gmail.com)
 * @version $Revision: 1.0 $
 */
class CJKEncodings
{
   // Mapping: PDF encoding name -> Java (IANA) charset name
   private static HashMap _mapping = new HashMap();

   static
   {
       // Chinese (Simplified)
       _mapping.put("GB-EUC-H",        "GB2312");              // Microsoft Code Page 936 (lfCharSet 0x86), GB 2312-80 character set, EUC-CN encoding
       _mapping.put("GB-EUC-V",        "GB2312");              // Vertical version of GB-EUC-H
       _mapping.put("GBpc-EUC-H",      "GB2312");              // Mac OS, GB 2312-80 character set, EUC-CN encoding, Script Manager code 19
       _mapping.put("GBpc-EUC-V",      "GB2312");              // Vertical version of GBpc-EUC-H
       _mapping.put("GBK-EUC-H",       "GBK");                 // Microsoft Code Page 936 (lfCharSet 0x86), GBK character set, GBK encoding
       _mapping.put("GBK-EUC-V",       "GBK");                 // Vertical version of GBK-EUC-H
       _mapping.put("GBKp-EUC-H",      "GBK");                 // Same as GBK-EUC-H but replaces half-width Latin characters with proportional forms and maps character code 0x24 to a dollar sign ($) instead of a yuan symbol (ââ´)
       _mapping.put("GBKp-EUC-V",      "GBK");                 // Vertical version of GBKp-EUC-H
       _mapping.put("GBK2K-H",         "GB18030");             // GB 18030-2000 character set, mixed 1-, 2-, and 4-byte encoding
       _mapping.put("GBK2K-V",         "GB18030");             // Vertical version of GBK2K-H
       _mapping.put("UniGB-UCS2-H",    "ISO-10646-UCS-2");     // Unicode (UCS-2) encoding for the Adobe-GB1 character collection
       _mapping.put("UniGB-UCS2-V",    "ISO-10646-UCS-2");     // Vertical version of UniGB-UCS2-H
       _mapping.put("UniGB-UTF16-H",   "UTF-16BE");            // Unicode (UTF-16BE) encoding for the Adobe-GB1 character collection; contains mappings for all characters in the GB18030-2000 character set
       _mapping.put("UniGB-UTF16-V",   "UTF-16BE");            // Vertical version of UniGB-UTF16-H

       // Chinese (Traditional)
       _mapping.put("B5pc-H",  "BIG5");                    // Mac OS, Big Five character set, Big Five encoding, Script Manager code 2
       _mapping.put("B5pc-V",  "BIG5");                    // Vertical version of B5pc-H
       _mapping.put("HKscs-B5-H",      "Big5-HKSCS");          // Hong Kong SCS, an extension to the Big Five character set and encoding
       _mapping.put("HKscs-B5-V",      "Big5-HKSCS");          // Vertical version of HKscs-B5-H
       _mapping.put("ETen-B5-H",       "BIG5");                // Microsoft Code Page 950 (lfCharSet 0x88), Big Five character set with ETen extensions
       _mapping.put("ETen-B5-V",       "BIG5");                // Vertical version of ETen-B5-H
       _mapping.put("ETenms-B5-H",     "BIG5");                // Same as ETen-B5-H but replaces half-width Latin characters with proportional forms
       _mapping.put("ETenms-B5-V",     "BIG5");                // Vertical version of ETenms-B5-H
       _mapping.put("CNS-EUC-H",       "HZ");          // CNS 11643-1992 character set, EUC-TW encoding
       _mapping.put("CNS-EUC-V",       "HZ");          // Vertical version of CNS-EUC-H
       _mapping.put("UniCNS-UCS2-H",   "ISO-10646-UCS-2");             // Unicode (UCS-2) encoding for the Adobe-CNS1 character collection
       _mapping.put("UniCNS-UCS2-V",   "ISO-10646-UCS-2");             // Vertical version of UniCNS-UCS2-H
       _mapping.put("UniCNS-UTF16-H",  "UTF-16BE");            // Unicode (UTF-16BE) encoding for the Adobe-CNS1 character collection; contains mappings for all the characters in the HKSCS-2001 character set and contains both 2- and 4- byte character codes
       _mapping.put("UniCNS-UTF16-V",  "UTF-16BE");            // Vertical version of UniCNS-UTF16-H

       //Japanese
       _mapping.put("83pv-RKSJ-H",     "JIS");                 // Mac OS, JIS X 0208 character set with KanjiTalk6 extensions, Shift-JIS encoding, Script Manager code 1
       _mapping.put("90ms-RKSJ-H",     "JIS");                 // Microsoft Code Page 932 (lfCharSet 0x80), JIS X 0208 character set with NEC and IBM- extensions
       _mapping.put("90ms-RKSJ-V",     "JIS");                 // Vertical version of 90ms-RKSJ-H
       _mapping.put("90msp-RKSJ-H",    "JIS");                 // Same as 90ms-RKSJ-H but replaces half-width Latin characters with proportional forms
       _mapping.put("90msp-RKSJ-V",    "JIS");                 // Vertical version of 90msp-RKSJ-H
       _mapping.put("90pv-RKSJ-H",     "JIS");                 // Mac OS, JIS X 0208 character set with KanjiTalk7 extensions, Shift-JIS encoding, Script Manager code 1
       _mapping.put("Add-RKSJ-H",      "JIS");                 // JIS X 0208 character set with Fujitsu FMR extensions, Shift-JIS encoding
       _mapping.put("Add-RKSJ-V",      "JIS");                 // Vertical version of Add-RKSJ-H
       _mapping.put("EUC-H",   "JIS");                    // JIS X 0208 character set, EUC-JP encoding
       _mapping.put("EUC-V",   "JIS");                    // Vertical version of EUC-H
       _mapping.put("Ext-RKSJ-H",      "JIS");                 // JIS C 6226 (JIS78) character set with NEC extensions, Shift-JIS encoding
       _mapping.put("Ext-RKSJ-V",      "JIS");                 // Vertical version of Ext-RKSJ-H
       _mapping.put("H",       "JIS");                    // JIS X 0208 character set, ISO-2022-JP encoding
       _mapping.put("V",       "JIS");                    // Vertical version of H
       _mapping.put("UniJIS-UCS2-H",   "ISO-10646-UCS-2");             // Unicode (UCS-2) encoding for the Adobe-Japan1 character collection
       _mapping.put("UniJIS-UCS2-V",   "ISO-10646-UCS-2");             // Vertical version of UniJIS-UCS2-H
       _mapping.put("UniJIS-UCS2-HW-H",        "ISO-10646-UCS-2");     // Same as UniJIS-UCS2-H but replaces proportional Latin characters with half-width forms
       _mapping.put("UniJIS-UCS2-HW-V",        "ISO-10646-UCS-2");     // Vertical version of UniJIS-UCS2-HW-H
       _mapping.put("UniJIS-UTF16-H",  "UTF-16BE");             // Unicode (UTF-16BE) encoding for the Adobe-Japan1 character collection; contains mappings for all characters in the JIS X 0213:1000 character set
       _mapping.put("UniJIS-UTF16-V",  "UTF-16BE");            // Vertical version of UniJIS-UTF16-H
       _mapping.put("Identity-H",       "JIS");                    // JIS X 0208 character set, ISO-2022-JP encoding
       _mapping.put("Identity-V",       "JIS");                    // Vertical version of H

       //Korean
       _mapping.put("KSC-EUC-H",       "KSC");                 // KS X 1001:1992 character set, EUC-KR encoding
       _mapping.put("KSC-EUC-V",       "KSC");                 // Vertical version of KSC-EUC-H
       _mapping.put("KSCms-UHC-H",     "KSC");                 // Microsoft Code Page 949 (lfCharSet 0x81), KS X 1001:1992 character set plus 8822.putitional hangul, Unified Hangul Code (UHC) encoding
       _mapping.put("KSCms-UHC-V",     "KSC");                 // Vertical version of KSCms-UHC-H
       _mapping.put("KSCms-UHC-HW-H",  "KSC");                 // Same as KSCms-UHC-H but replaces proportional Latin characters with half-width forms
       _mapping.put("KSCms-UHC-HW-V",  "KSC");                 // Vertical version of KSCms-UHC-HW-H
       _mapping.put("KSCpc-EUC-H",     "KSC");                 // Mac OS, KS X 1001:1992 character set with Mac OS KH extensions, Script Manager Code 3
       _mapping.put("UniKS-UCS2-H",    "ISO-10646-UCS-2");             // Unicode (UCS-2) encoding for the Adobe-Korea1 character collection
       _mapping.put("UniKS-UCS2-V",    "ISO-10646-UCS-2");             // Vertical version of UniKS-UCS2-H
       _mapping.put("UniKS-UTF16-H",   "UTF-16BE");            // Unicode (UTF-16BE) encoding for the Adobe-Korea1 character collection
       _mapping.put("UniKS-UTF16-V",   "UTF-16BE");            // Vertical version of UniKS-UTF16-H
   }


   /**
    *  Get respective Java charset name from given PDF encoding name.
    *
    *  @param encoding   PDF encoding name
    *  @return Java charset name, or null if not found
    */
   public static final String getCharset( String encoding )
   {
       if ( encoding.startsWith("COSName"))
           encoding = encoding.substring(8, encoding.length()-1);

       return (String)(_mapping.get(encoding));
   }

   /**
    *  Return an iterator to iterate through all encodings
    */
   public static final Iterator getEncodingIterator()
   {
          return _mapping.keySet().iterator();
   }

}
