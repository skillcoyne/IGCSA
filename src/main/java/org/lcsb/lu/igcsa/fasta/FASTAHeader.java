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
  private String db;
  private String accession;
  private String locus = "";
  private String description;
  private String asString;

  private static final int MIN_ENTRIES = 3;
  private static final String separator = "|";


  public FASTAHeader(String db, String accession, String locus, String description)
    {
    this.db = db;
    this.accession = accession.replaceAll(" ", ".");
    this.locus = locus.replaceAll(" ", ".");
    this.description = description;
    this.asString = this.db + separator + this.accession + separator + this.locus + separator + this.description;
    }

  public FASTAHeader(String line) throws IOException
    {
    if (!line.contains(separator)) throw new IOException("Not the first line of a FASTA file: " + line);
    parse(line);
    }

  private void parse(String line) throws IOException
    {
    String[] entries = line.split("\\|");

    if (entries.length < MIN_ENTRIES)
      {
      throw new IOException("Missing parameters in header, expected " + MIN_ENTRIES + " found " + entries.length + " :" + line);
      }

    this.accession = entries[0].replace(">", "") + entries[1];
    if (entries.length > MIN_ENTRIES)
      {
      this.locus = entries[3];
      this.description = entries[entries.length-1];
      }
    else this.description = entries[entries.length-1];
    asString = line;
    }

  public String getAccession()
    {
    return accession;
    }

  public String getLocus()
    {
    return locus;
    }

  public String getDescription()
    {
    return description;
    }

//  public void setAccession(String accession)
//    {
//    this.accession = accession;
//    }
//
//  public void setLocus(String locus)
//    {
//    this.locus = locus;
//    }
//
//  public void setDescription(String description)
//    {
//    this.description = description;
//    }

  public String toString()
    {
    return asString;
    }

  }
