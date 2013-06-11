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
  private String locusdb = "";
  private String locus = "";
  private String description = "";

  private static final int MIN_ENTRIES = 3;
  private static final String separator = "|";


  public FASTAHeader(String db, String accession, String locus, String description)
    {
    this.db = db.replaceAll(" ", ".");
    this.accession = accession.replaceAll(" ", ".");
    if (locus != null)
      this.locus = locus.replaceAll(" ", ".");
    this.description = description.replaceAll(" ", ".");
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

    this.db = entries[0].replace(">", "");
    this.accession = entries[1];
    if (entries.length > MIN_ENTRIES)
      {
      this.locusdb = entries[2];
      this.locus = entries[3];
      this.description = entries[entries.length-1];
      }
    else this.description = entries[entries.length-1];
    }

  public String getDB()
    {
    return this.db;
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

  public String getFormattedHeader()
    {
    return ">" + toString();
    }

  public String toString()
    {
    String str =  this.db + separator + this.accession + separator;
    if (this.locusdb != null && !this.locusdb.equals(""))
      str = str + this.locusdb + separator;
    str = str + this.locus + separator + this.description;
    return str;
    }

  }
