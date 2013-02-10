package org.lcsb.lu.igcsa.fasta;

import java.io.IOException;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAHeader
  {
  private String Accession;
  private String locus = "";
  private String description;

  private static final int MIN_ENTRIES = 3;

  public FASTAHeader(String line) throws IOException
    {
    if (!line.contains("|")) throw new IOException("Not the first line of a FASTA file: " + line);
    parse(line);
    }

  private void parse(String line) throws IOException
    {
    String[] entries = line.split("\\|");

    if (entries.length < MIN_ENTRIES)
      {
      throw new IOException("Missing parameters in header, expected " + MIN_ENTRIES + " found " + entries.length + " :" + line);
      }

    this.Accession = entries[0].replace(">", "") + entries[1];
    if (entries.length > MIN_ENTRIES)
      {
      this.locus = entries[3];
      this.description = entries[entries.length-1];
      }
    else this.description = entries[entries.length-1];
    }

  public String getAccession()
    {
    return Accession;
    }

  public String getLocus()
    {
    return locus;
    }

  public String getDescription()
    {
    return description;
    }
  }
