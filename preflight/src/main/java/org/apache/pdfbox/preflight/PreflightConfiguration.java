/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.preflight.action.ActionManagerFactory;
import org.apache.pdfbox.preflight.annotation.AnnotationValidatorFactory;
import org.apache.pdfbox.preflight.annotation.pdfa.PDFAbAnnotationFactory;
import org.apache.pdfbox.preflight.exception.MissingValidationProcessException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.apache.pdfbox.preflight.process.AcroFormValidationProcess;
import org.apache.pdfbox.preflight.process.BookmarkValidationProcess;
import org.apache.pdfbox.preflight.process.CatalogValidationProcess;
import org.apache.pdfbox.preflight.process.EmptyValidationProcess;
import org.apache.pdfbox.preflight.process.FileSpecificationValidationProcess;
import org.apache.pdfbox.preflight.process.MetadataValidationProcess;
import org.apache.pdfbox.preflight.process.PageTreeValidationProcess;
import org.apache.pdfbox.preflight.process.StreamValidationProcess;
import org.apache.pdfbox.preflight.process.TrailerValidationProcess;
import org.apache.pdfbox.preflight.process.ValidationProcess;
import org.apache.pdfbox.preflight.process.XRefValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ActionsValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.AnnotationValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ExtGStateValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.FontValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.GraphicObjectPageValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ResourcesValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ShaddingPatternValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.SinglePageValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.TilingPatternValidationProcess;

public class PreflightConfiguration
{

    // TODO documentation
    public static final String CATALOG_PROCESS = "catalog-process";
    public static final String STREAM_PROCESS = "stream-process";
    public static final String TRAILER_PROCESS = "trailer-process";
    public static final String XREF_PROCESS = "xref-process";
    public static final String BOOKMARK_PROCESS = "bookmark-process";
    public static final String ACRO_FORM_PROCESS = "acro-form-process"; // MayBe rename in Interactive Object validation
    public static final String FILE_SPECIF_PROCESS = "file-specification-process";
    public static final String PAGES_TREE_PROCESS = "pages-tree-process";

    public static final String META_DATA_PROCESS = "metadata-process";

    public static final String PAGE_PROCESS = "page-process";
    public static final String RESOURCES_PROCESS = "resources-process";
    public static final String ACTIONS_PROCESS = "actions-process";
    public static final String ANNOTATIONS_PROCESS = "annotations-process";
    public static final String GRAPHIC_PROCESS = "graphic-process";
    public static final String FONT_PROCESS = "font-process";
    public static final String EXTGSTATE_PROCESS = "extgstate-process";
    public static final String SHADDING_PATTERN_PROCESS = "shadding-pattern-process";
    public static final String TILING_PATTERN_PROCESS = "tiling-pattern-process";

    /*
     * TODO other configuration option should be possible : - skip some validation process ? - ???
     */

    /**
     * Boolean to know if an exception must be thrown if a ValidationProcess is missing.
     */
    private boolean errorOnMissingProcess = true;

    /**
     * Boolean to know mark some error as a Warning, if the validation result contains only warning the validation is
     * successful
     */
    private boolean lazyValidation = false;

    private Map<String, Class<? extends ValidationProcess>> processes = new HashMap<String, Class<? extends ValidationProcess>>();
    // TODO use annotation to mark these validation processes as inner page validation and factorize the access method
    private Map<String, Class<? extends ValidationProcess>> innerProcesses = new HashMap<String, Class<? extends ValidationProcess>>();

    /**
     * Define the AnnotationFactory used by ValidationProcess
     */
    private AnnotationValidatorFactory annotFact;

    /**
     * Define the ActionManagerFactory used by ValidationProcess
     */
    private ActionManagerFactory actionFact;

    /**
     * Define the ColorSpaceHelperFactory used by the validationProcess.
     */
    private ColorSpaceHelperFactory colorSpaceHelperFact;

    public static PreflightConfiguration createPdfA1BConfiguration()
    {
        PreflightConfiguration configuration = new PreflightConfiguration();

        configuration.replaceProcess(CATALOG_PROCESS, CatalogValidationProcess.class);
        configuration.replaceProcess(FILE_SPECIF_PROCESS, FileSpecificationValidationProcess.class);
        configuration.replaceProcess(TRAILER_PROCESS, TrailerValidationProcess.class);
        configuration.replaceProcess(XREF_PROCESS, XRefValidationProcess.class);
        configuration.replaceProcess(ACRO_FORM_PROCESS, AcroFormValidationProcess.class);
        configuration.replaceProcess(BOOKMARK_PROCESS, BookmarkValidationProcess.class);
        configuration.replaceProcess(PAGES_TREE_PROCESS, PageTreeValidationProcess.class);
        configuration.replaceProcess(META_DATA_PROCESS, MetadataValidationProcess.class);

        configuration.replaceProcess(STREAM_PROCESS, StreamValidationProcess.class);

        configuration.replacePageProcess(PAGE_PROCESS, SinglePageValidationProcess.class);
        configuration.replacePageProcess(EXTGSTATE_PROCESS, ExtGStateValidationProcess.class);
        configuration.replacePageProcess(SHADDING_PATTERN_PROCESS, ShaddingPatternValidationProcess.class);
        configuration.replacePageProcess(GRAPHIC_PROCESS, GraphicObjectPageValidationProcess.class);
        configuration.replacePageProcess(TILING_PATTERN_PROCESS, TilingPatternValidationProcess.class);
        configuration.replacePageProcess(RESOURCES_PROCESS, ResourcesValidationProcess.class);
        configuration.replacePageProcess(FONT_PROCESS, FontValidationProcess.class);
        configuration.replacePageProcess(ACTIONS_PROCESS, ActionsValidationProcess.class);
        configuration.replacePageProcess(ANNOTATIONS_PROCESS, AnnotationValidationProcess.class);

        configuration.actionFact = new ActionManagerFactory();
        configuration.annotFact = new PDFAbAnnotationFactory();
        configuration.colorSpaceHelperFact = new ColorSpaceHelperFactory();
        return configuration;
    }

    public Collection<String> getProcessNames()
    {
        return this.processes.keySet();
    }

    /**
     * Return the validation process linked with the given name
     * 
     * @param processName
     * @return an instance of validationProcess, null if it doesn't exist and if the errorOnMissingProcess is false
     * @throws MissingValidationProcessException
     *             if the Process doesn't exist (errorOnMissingProcess is true)
     */
    public ValidationProcess getInstanceOfProcess(String processName) throws MissingValidationProcessException,
            ValidationException
    {
        Class<? extends ValidationProcess> clazz = null;
        if (processes.containsKey(processName))
        {
            clazz = processes.get(processName);
        }
        else if (innerProcesses.containsKey(processName))
        {
            clazz = innerProcesses.get(processName);
        }
        else if (errorOnMissingProcess)
        {
            throw new MissingValidationProcessException(processName);
        }
        else
        {
            return new EmptyValidationProcess();
        }

        try
        {
            return clazz.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new ValidationException(processName + " can't be created", e);
        }
        catch (IllegalAccessException e)
        {
            throw new ValidationException(processName + " can't be created", e);
        }

    }

    public void replaceProcess(String processName, Class<? extends ValidationProcess> process)
    {
        if (process == null) 
        {
            removeProcess(processName);
        }
        else 
        {
            this.processes.put(processName, process);
        }
    }

    public void removeProcess(String processName)
    {
        this.processes.remove(processName);
    }
    
    public Collection<String> getPageValidationProcessNames()
    {
        return this.innerProcesses.keySet();
    }

    public void replacePageProcess(String processName, Class<? extends ValidationProcess> process)
    {
        if (process == null) {
            removePageProcess(processName);    
        }
        else {
            this.innerProcesses.put(processName, process);
        }
    }

    public void removePageProcess(String processName)
    {
        this.innerProcesses.remove(processName);
    }
    
    public boolean isErrorOnMissingProcess()
    {
        return errorOnMissingProcess;
    }

    public void setErrorOnMissingProcess(boolean errorOnMissingProcess)
    {
        this.errorOnMissingProcess = errorOnMissingProcess;
    }

    public boolean isLazyValidation()
    {
        return lazyValidation;
    }

    public void setLazyValidation(boolean lazyValidation)
    {
        this.lazyValidation = lazyValidation;
    }

    public AnnotationValidatorFactory getAnnotFact()
    {
        return annotFact;
    }

    public void setAnnotFact(AnnotationValidatorFactory annotFact)
    {
        this.annotFact = annotFact;
    }

    public ActionManagerFactory getActionFact()
    {
        return actionFact;
    }

    public void setActionFact(ActionManagerFactory actionFact)
    {
        this.actionFact = actionFact;
    }

    public ColorSpaceHelperFactory getColorSpaceHelperFact()
    {
        return colorSpaceHelperFact;
    }

    public void setColorSpaceHelperFact(ColorSpaceHelperFactory colorSpaceHelperFact)
    {
        this.colorSpaceHelperFact = colorSpaceHelperFact;
    }

}
