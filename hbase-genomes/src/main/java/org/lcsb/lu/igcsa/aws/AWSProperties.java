package org.lcsb.lu.igcsa.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * org.lcsb.lu.igcsa.aws
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class AWSProperties
  {
  private static AWSProperties ourInstance;

  private Properties accessProps;

  public static AWSProperties getProperties()
    {
    if (ourInstance == null)
      ourInstance = new AWSProperties();
    return ourInstance;
    }

  private AWSProperties()
    {
    accessProps = new Properties();
    final InputStream is = AWSProperties.class.getResourceAsStream("/AwsCredentials.properties");
    try
      {
      accessProps.load(is);
      }
    catch (IOException e)
      {
      throw new RuntimeException(e);
      }
    }

  public String getAccessKey()
    {
    return (String) accessProps.get("accessKey");
    }

  public String getSecretKey()
    {
    return (String) accessProps.get("secretKey");
    }

  }
