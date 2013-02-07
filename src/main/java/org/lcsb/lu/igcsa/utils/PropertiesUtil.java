package org.lcsb.lu.igcsa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class PropertiesUtil
  {

  public static Properties readPropsFile(String fileName) throws IOException
    {
    Properties props = new Properties();
    ClassLoader cl = ClassLoader.getSystemClassLoader(); // not sure this is the best way to do it, but it works

    try { props.load(cl.getResourceAsStream(fileName)); }
    catch (NullPointerException npe)
      { throw new FileNotFoundException(fileName + " not found"); }
    return props;
    }


  }
