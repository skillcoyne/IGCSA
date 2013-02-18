package org.lcsb.lu.igcsa.fasta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FastaWriter
  {
  private File fasta;
  private FASTAHeader header;

  public FastaWriter(File fasta) throws IOException
    {
    this.fasta = fasta;
    }

  public FastaWriter(File fasta, FASTAHeader header) throws IOException
    {
    this.fasta = fasta;
    this.header = header;
    }
  }
