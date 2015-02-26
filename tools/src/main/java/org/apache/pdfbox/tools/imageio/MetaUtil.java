/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.tools.imageio;

import java.io.StringWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Tilman Hausherr
 */
public final class MetaUtil
{
    private static final Log LOG = LogFactory.getLog(MetaUtil.class);

    static final String SUN_TIFF_FORMAT = "com_sun_media_imageio_plugins_tiff_image_1.0";
    static final String JPEG_NATIVE_FORMAT = "javax_imageio_jpeg_image_1.0";
    static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";
    
    private MetaUtil()
    {
    }    

    // logs metadata as an XML tree if debug is enabled
    static void debugLogMetadata(IIOMetadata metadata, String format)
    {
        if (!LOG.isDebugEnabled())
        {
            return;
        }

        // see http://docs.oracle.com/javase/7/docs/api/javax/imageio/
        //     metadata/doc-files/standard_metadata.html
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
        try
        {
            StringWriter xmlStringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(xmlStringWriter);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // see http://stackoverflow.com/a/1264872/535646
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(root);
            transformer.transform(domSource, streamResult);
            LOG.debug("\n" + xmlStringWriter);
        }
        catch (IllegalArgumentException ex)
        {
            LOG.error(ex, ex);
        }
        catch (TransformerException ex)
        {
            LOG.error(ex, ex);
        }
    }

}
