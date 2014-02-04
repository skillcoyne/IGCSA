package org.lcsb.lu.igcsa;

import org.apache.log4j.Logger;

import java.io.*;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ThreadedStreamConnector implements Runnable
  {
  static Logger log = Logger.getLogger(ThreadedStreamConnector.class.getName());

  private InputStream is;
  private OutputStream os;

  public ThreadedStreamConnector(InputStream is, OutputStream os)
    {
    this.is = is;
    this.os = os;
    }

  @Override
  public void run()
    {
    byte[] data = new byte[1024];
    int len;
    try
      {
      while ((len = is.read(data)) != -1)
        {
        os.write(data, 0, len);
        os.flush();
        progress();
        }
      os.flush();
      }
    catch (Exception e)
      {
      System.err.println(e);
      }
    }

  /**
   * Report progress, override hook
   */
  public void progress()
    {
    }

  }
