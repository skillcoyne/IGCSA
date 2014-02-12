package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CommandExecution
  {
  static Logger log = Logger.getLogger(CommandExecution.class.getName());

  private Mapper.Context context;
  private OutputStream errorOS;
  private OutputStream outputOS;

  public CommandExecution(Mapper.Context context, OutputStream errorOS, OutputStream outputOS)
    {
    this.context = context;
    this.errorOS = errorOS;
    this.outputOS = outputOS;
    }

  public int execute(String cmd) throws IOException, InterruptedException
    {
    Thread error, out;
    Process p = Runtime.getRuntime().exec(cmd);
    // Reattach stderr and write System.stdout to tmp file
    error = new Thread(new ThreadedStreamConnector(p.getErrorStream(), errorOS)
    {
    @Override
    public void progress()
      {
      context.progress();
      }
    });
    out = new Thread(new ThreadedStreamConnector(p.getInputStream(), outputOS));
    out.start();
    out.join();
    error.start();
    error.join();

    int exitVal = p.waitFor();
    errorOS.close(); outputOS.close();
    return exitVal;
    }

  /**
   * org.lcsb.lu.igcsa
   * Author: Sarah Killcoyne
   * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
   * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
   */
  public static class ThreadedStreamConnector implements Runnable
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
  }
