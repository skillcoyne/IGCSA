package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.fasta.FASTAWriter;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Mutable implements Runnable
  {
  private Chromosome chromosome;
  private int window;
  private FASTAWriter writer;
  private Thread thread;

  public Mutable(Chromosome chr, int window, FASTAWriter writer)
    {
    this.chromosome = chr;
    this.window = window;
    this.writer = writer;

    thread = new Thread(this);
    thread.start();
    }

  public void run()
    {


    }
  }
