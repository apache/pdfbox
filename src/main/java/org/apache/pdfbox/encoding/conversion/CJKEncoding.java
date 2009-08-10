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
 * This class represents PDF encoding name to Java charset name mapping.
 *
 * @author  Pin Xue (http://www.pinxue.net), Holly Lee (holly.lee (at) gmail.com)
 * @version $Revision: 1.0 $
 */
class CJKEncodings
{
   // Mapping: PDF encoding name -> Java (IANA) charset name
   private static HashMap charsetMapping = new HashMap();

   private CJKEncodings()
   {
   }

   static
   {
       // Chinese (Simplified)
       // Microsoft Code Page 936 (lfCharSet 0x86), GB 2312-80 character set, EUC-CN encoding
       charsetMapping.put("GB-EUC-H",        "GB2312");              
       // Vertical version of GB-EUC-H
       charsetMapping.put("GB-EUC-V",        "GB2312");              
       // Mac OS, GB 2312-80 character set, EUC-CN encoding, Script Manager code 19
       charsetMapping.put("GBpc-EUC-H",      "GB2312");              
       // Vertical version of GBpc-EUC-H
       charsetMapping.put("GBpc-EUC-V",      "GB2312");              
       // Microsoft Code Page 936 (lfCharSet 0x86), GBK character set, GBK encoding
       charsetMapping.put("GBK-EUC-H",       "GBK");                 
       // Vertical version of GBK-EUC-H
       charsetMapping.put("GBK-EUC-V",       "GBK");                 
       // Same as GBK-EUC-H but replaces half-width Latin characters with proportional 
       // forms and maps character code 0x24 to a dollar sign ($) instead of a yuan symbol (ââ´)
       charsetMapping.put("GBKp-EUC-H",      "GBK");                 
       // Vertical version of GBKp-EUC-H
       charsetMapping.put("GBKp-EUC-V",      "GBK");                 
       // GB 18030-2000 character set, mixed 1-, 2-, and 4-byte encoding
       charsetMapping.put("GBK2K-H",         "GB18030");             
       // Vertical version of GBK2K-H
       charsetMapping.put("GBK2K-V",         "GB18030");             
       // Unicode (UCS-2) encoding for the Adobe-GB1 character collection
       charsetMapping.put("UniGB-UCS2-H",    "ISO-10646-UCS-2");     
       // Vertical version of UniGB-UCS2-H
       charsetMapping.put("UniGB-UCS2-V",    "ISO-10646-UCS-2");     
       // Unicode (UTF-16BE) encoding for the Adobe-GB1 character collection; contains mappings 
       // for all characters in the GB18030-2000 character set
       charsetMapping.put("UniGB-UTF16-H",   "UTF-16BE");            
       // Vertical version of UniGB-UTF16-H
       charsetMapping.put("UniGB-UTF16-V",   "UTF-16BE");            

       // Chinese (Traditional)
       // Mac OS, Big Five character set, Big Five encoding, Script Manager code 2
       charsetMapping.put("B5pc-H",  "BIG5");                    
       // Vertical version of B5pc-H
       charsetMapping.put("B5pc-V",  "BIG5");                    
       // Hong Kong SCS, an extension to the Big Five character set and encoding
       charsetMapping.put("HKscs-B5-H",      "Big5-HKSCS");          
       // Vertical version of HKscs-B5-H
       charsetMapping.put("HKscs-B5-V",      "Big5-HKSCS");          
       // Microsoft Code Page 950 (lfCharSet 0x88), Big Five character set with ETen extensions
       charsetMapping.put("ETen-B5-H",       "BIG5");                
       // Vertical version of ETen-B5-H
       charsetMapping.put("ETen-B5-V",       "BIG5");                
       // Same as ETen-B5-H but replaces half-width Latin characters with proportional forms
       charsetMapping.put("ETenms-B5-H",     "BIG5");                
       // Vertical version of ETenms-B5-H
       charsetMapping.put("ETenms-B5-V",     "BIG5");                
       // CNS 11643-1992 character set, EUC-TW encoding
       charsetMapping.put("CNS-EUC-H",       "HZ");          
       // Vertical version of CNS-EUC-H
       charsetMapping.put("CNS-EUC-V",       "HZ");          
       // Unicode (UCS-2) encoding for the Adobe-CNS1 character collection
       charsetMapping.put("UniCNS-UCS2-H",   "ISO-10646-UCS-2");             
       // Vertical version of UniCNS-UCS2-H
       charsetMapping.put("UniCNS-UCS2-V",   "ISO-10646-UCS-2");             
       // Unicode (UTF-16BE) encoding for the Adobe-CNS1 character collection; 
       // contains mappings for all the characters in the HKSCS-2001 character set and 
       // contains both 2- and 4- byte character codes
       charsetMapping.put("UniCNS-UTF16-H",  "UTF-16BE");            
       // Vertical version of UniCNS-UTF16-H
       charsetMapping.put("UniCNS-UTF16-V",  "UTF-16BE");            

       //Japanese
       // Mac OS, JIS X 0208 character set with KanjiTalk6 extensions, Shift-JIS encoding, Script Manager code 1
       charsetMapping.put("83pv-RKSJ-H",     "JIS");                 
       // Microsoft Code Page 932 (lfCharSet 0x80), JIS X 0208 character set with NEC and IBM- extensions
       charsetMapping.put("90ms-RKSJ-H",     "JIS");                 
       // Vertical version of 90ms-RKSJ-H
       charsetMapping.put("90ms-RKSJ-V",     "JIS");                 
       // Same as 90ms-RKSJ-H but replaces half-width Latin characters with proportional forms
       charsetMapping.put("90msp-RKSJ-H",    "JIS");                 
       // Vertical version of 90msp-RKSJ-H
       charsetMapping.put("90msp-RKSJ-V",    "JIS");                 
       // Mac OS, JIS X 0208 character set with KanjiTalk7 extensions, Shift-JIS encoding, Script Manager code 1
       charsetMapping.put("90pv-RKSJ-H",     "JIS");                 
       // JIS X 0208 character set with Fujitsu FMR extensions, Shift-JIS encoding
       charsetMapping.put("Add-RKSJ-H",      "JIS");                 
       // Vertical version of Add-RKSJ-H
       charsetMapping.put("Add-RKSJ-V",      "JIS");                 
       // JIS X 0208 character set, EUC-JP encoding
       charsetMapping.put("EUC-H",   "JIS");                    
       // Vertical version of EUC-H
       charsetMapping.put("EUC-V",   "JIS");                    
       // JIS C 6226 (JIS78) character set with NEC extensions, Shift-JIS encoding
       charsetMapping.put("Ext-RKSJ-H",      "JIS");                 
       // Vertical version of Ext-RKSJ-H
       charsetMapping.put("Ext-RKSJ-V",      "JIS");                 
       // JIS X 0208 character set, ISO-2022-JP encoding
       charsetMapping.put("H",       "JIS");                    
       // Vertical version of H
       charsetMapping.put("V",       "JIS");                   
       // Unicode (UCS-2) encoding for the Adobe-Japan1 character collection
       charsetMapping.put("UniJIS-UCS2-H",   "ISO-10646-UCS-2");             
       // Vertical version of UniJIS-UCS2-H
       charsetMapping.put("UniJIS-UCS2-V",   "ISO-10646-UCS-2");             
       // Same as UniJIS-UCS2-H but replaces proportional Latin characters with half-width forms
       charsetMapping.put("UniJIS-UCS2-HW-H",        "ISO-10646-UCS-2");     
       // Vertical version of UniJIS-UCS2-HW-H
       charsetMapping.put("UniJIS-UCS2-HW-V",        "ISO-10646-UCS-2");     
       // Unicode (UTF-16BE) encoding for the Adobe-Japan1 character collection; 
       // contains mappings for all characters in the JIS X 0213:1000 character set
       charsetMapping.put("UniJIS-UTF16-H",  "UTF-16BE");             
       // Vertical version of UniJIS-UTF16-H
       charsetMapping.put("UniJIS-UTF16-V",  "UTF-16BE");            
       // JIS X 0208 character set, ISO-2022-JP encoding
       charsetMapping.put("Identity-H",       "JIS");                    
       // Vertical version of H
       charsetMapping.put("Identity-V",       "JIS");                    

       //Korean
       // KS X 1001:1992 character set, EUC-KR encoding
       charsetMapping.put("KSC-EUC-H",       "KSC");                 
       // Vertical version of KSC-EUC-H
       charsetMapping.put("KSC-EUC-V",       "KSC");                 
       // Microsoft Code Page 949 (lfCharSet 0x81), KS X 1001:1992 character set 
       // plus 8822.putitional hangul, Unified Hangul Code (UHC) encoding
       charsetMapping.put("KSCms-UHC-H",     "KSC");                
       // Vertical version of KSCms-UHC-H
       charsetMapping.put("KSCms-UHC-V",     "KSC");                 
       // Same as KSCms-UHC-H but replaces proportional Latin characters with half-width forms
       charsetMapping.put("KSCms-UHC-HW-H",  "KSC");                
       // Vertical version of KSCms-UHC-HW-H
       charsetMapping.put("KSCms-UHC-HW-V",  "KSC");                
       // Mac OS, KS X 1001:1992 character set with Mac OS KH extensions, Script Manager Code 3
       charsetMapping.put("KSCpc-EUC-H",     "KSC");                 
       // Unicode (UCS-2) encoding for the Adobe-Korea1 character collection
       charsetMapping.put("UniKS-UCS2-H",    "ISO-10646-UCS-2");             
       // Vertical version of UniKS-UCS2-H
       charsetMapping.put("UniKS-UCS2-V",    "ISO-10646-UCS-2");             
       // Unicode (UTF-16BE) encoding for the Adobe-Korea1 character collection
       charsetMapping.put("UniKS-UTF16-H",   "UTF-16BE");            
       // Vertical version of UniKS-UTF16-H
       charsetMapping.put("UniKS-UTF16-V",   "UTF-16BE");            
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
       {
           encoding = encoding.substring(8, encoding.length()-1);
       }
       return (String)(charsetMapping.get(encoding));
   }

   /**
    *  Return an iterator to iterate through all encodings.
    */
   public static final Iterator getEncodingIterator()
   {
          return charsetMapping.keySet().iterator();
   }

}
