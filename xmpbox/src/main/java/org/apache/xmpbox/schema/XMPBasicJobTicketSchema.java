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

package org.apache.xmpbox.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.JobType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

@StructuredType(preferedPrefix = "xmpBJ", namespace = "http://ns.adobe.com/xap/1.0/bj/")
public class XMPBasicJobTicketSchema extends XMPSchema
{

    @PropertyType(type = Types.Job, card = Cardinality.Bag)
    public static final String JOB_REF = "JobRef";

    private ArrayProperty bagJobs;

    public XMPBasicJobTicketSchema(XMPMetadata metadata)
    {
        this(metadata, null);
    }

    public XMPBasicJobTicketSchema(XMPMetadata metadata, String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    public void addJob(String id, String name, String url)
    {
        addJob(id, name, url, null);
    }

    public void addJob(String id, String name, String url, String fieldPrefix)
    {
        JobType job = new JobType(getMetadata(), fieldPrefix);
        job.setId(id);
        job.setName(name);
        job.setUrl(url);
        addJob(job);
    }

    public void addJob(JobType job)
    {
        String prefix = getNamespacePrefix(job.getNamespace());
        if (prefix != null)
        {
            // use same prefix for all jobs
            job.setPrefix(prefix);
        }
        else
        {
            // add prefix
            addNamespace(job.getNamespace(), job.getPrefix());
        }
        // create bag if not existing
        if (bagJobs == null)
        {
            bagJobs = createArrayProperty(JOB_REF, Cardinality.Bag);
            addProperty(bagJobs);
        }
        // add job
        bagJobs.getContainer().addProperty(job);
    }

    public List<JobType> getJobs() throws BadFieldValueException
    {
        List<AbstractField> tmp = getUnqualifiedArrayList(JOB_REF);
        if (tmp != null)
        {
            List<JobType> layers = new ArrayList<JobType>();
            for (AbstractField abstractField : tmp)
            {
                if (abstractField instanceof JobType)
                {
                    layers.add((JobType) abstractField);
                }
                else
                {
                    throw new BadFieldValueException("Job expected and " + abstractField.getClass().getName()
                            + " found.");
                }
            }
            return layers;
        }
        return null;

    }

}
