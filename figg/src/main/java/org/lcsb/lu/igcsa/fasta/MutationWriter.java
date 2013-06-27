package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class MutationWriter
  {
  static Logger log = Logger.getLogger(MutationWriter.class.getName());

  public static final int SMALL = 1;
  public static final int SV = 2;

  private File mutationFile;
  private FileWriter fileWriter;
  private BufferedWriter bufferedWriter;

  private String smallMutHeader = "Chromosome\tFragment\tGCBin\tStartLoc\tEndLoc\tVariation\tSequence\n";
  private String svMutHeader = "Chromosome\tStartLoc\tEndLoc\tVariation\n";

  private int type = 1;

  private StringBuffer buffer = new StringBuffer();


  public MutationWriter(File mutationFile, int type) throws IOException
    {
    this.mutationFile = mutationFile;
    createFile(mutationFile);
    if (type == SMALL)
      buffer.append(smallMutHeader);
    if (type == SV)
      buffer.append(svMutHeader);
    this.type = type;

    flush();
    }

  public File getMutationFile()
    {
    return this.mutationFile;
    }

  public void write(Mutation mutation) throws IOException
    {
    addToBuffer(mutation);
    flush();
    }

  public void write(Mutation[] mutations) throws IOException
    {
    for (Mutation m : mutations)
      addToBuffer(m);
    flush();
    }

  public void flush() throws IOException
    {
    bufferedWriter.write(buffer.toString());
    bufferedWriter.flush();
    buffer = new StringBuffer();
    }

  public void close() throws IOException
    {
    bufferedWriter.flush();
    bufferedWriter.close();
    fileWriter.close();
    }

  private void addToBuffer(Mutation mutation)
    {
    if (type == SMALL)
      {
      buffer.append(mutation.getChromosome() + "\t");
      buffer.append(mutation.getFragment() + "\t");
      buffer.append(mutation.getGCBin() + "\t");
      buffer.append(mutation.getStartLocation() + "\t");
      buffer.append(mutation.getEndLocation() + "\t");
      buffer.append(mutation.getVariationType() + "\t");
      buffer.append(mutation.getSequence() + "\n");
      }
    else
      {
      buffer.append(mutation.getChromosome() + "\t");
      buffer.append(mutation.getStartLocation() + "\t");
      buffer.append(mutation.getEndLocation() + "\t");
      buffer.append(mutation.getVariationType() + "\n");
      }
    }

  private File createFile(File file) throws IOException
    {
    File parentDir = new File(file.getParent());
    if (!file.exists())
      {
      parentDir.mkdirs();
      try
        {
        file.createNewFile();
        }
      catch (IOException ioe)
        {
        throw new IOException(ioe.getMessage() + " " + file.getAbsolutePath());
        }
      }
    fileWriter = new FileWriter(file.getAbsoluteFile());
    bufferedWriter = new BufferedWriter(fileWriter);
    return file;
    }

  }
