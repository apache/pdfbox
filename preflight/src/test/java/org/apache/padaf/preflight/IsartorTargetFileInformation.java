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

package org.apache.padaf.preflight;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class IsartorTargetFileInformation {

  protected File targetFile = null;

  protected String expectedError = null;

  public IsartorTargetFileInformation(File targetFile, String expectedError) {
    this.targetFile = targetFile;
    this.expectedError = expectedError;
  }

  public File getTargetFile() {
    return targetFile;
  }

  public String getExpectedError() {
    return expectedError;
  }

  public static List<IsartorTargetFileInformation> loadConfiguration(File root)
      throws Exception {
    // load config
    InputStream expected = IsartorTargetFileInformation.class
        .getResourceAsStream("/expected_errors.txt");
    Properties props = new Properties();
    props.load(expected);
    // list files
    List<IsartorTargetFileInformation> result = new ArrayList<IsartorTargetFileInformation>();
    if (root.isDirectory()) {
      Collection<?> col = FileUtils.listFiles(root, new String[] { "pdf" },
          true);
      for (Object o : col) {
        File file = (File) o;
        IsartorTargetFileInformation info = getInformation(file, props);
        if (info == null) {
          continue;
        }
        result.add(info);
      }
    } else if (root.isFile()) {
      result.add(getInformation(root, props));
    }
    return result;
  }

  protected static IsartorTargetFileInformation getInformation(File file,
      Properties props) {
    Logger logger = Logger.getLogger("test.isartor");
    String key = file.getName();
    String line = props.getProperty(key);
    if (line != null) {
      // only one parameter for the moment
      String error = new StringTokenizer(line, "//").nextToken().trim();
      return new IsartorTargetFileInformation(file, error);
    } else {
      logger.error("There is no expected error for " + key);
      return null;
    }

  }

}
