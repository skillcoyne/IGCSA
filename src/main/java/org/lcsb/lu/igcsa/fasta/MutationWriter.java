package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.insilico.Mutation;

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

  private File mutationFile;
  private FileWriter fileWriter;
  private BufferedWriter bufferedWriter;


  private StringBuffer buffer = new StringBuffer();


  public MutationWriter(File mutationFile) throws IOException
    {
    this.mutationFile = mutationFile;
    createFile(mutationFile);
    buffer.append( "Chromosome\tFragment\tStartLoc\tEndLoc\tVariation\tSequence"  );
    flush();
    }

  public void write(Mutation mutation) throws IOException
    {
    buffer.append( mutation.getChromosome() + "\t" );
    buffer.append( mutation.getFragment() + "\t" );
    buffer.append( mutation.getStartLocation() + "\t" );
    buffer.append( mutation.getEndLocation() + "\t" );
    buffer.append( mutation.getVariationType() + "\t" );
    buffer.append( mutation.getSequence() + "\t" );
    flush();
    }



  public void flush() throws IOException
    {
    bufferedWriter.write(buffer.toString() + "\n");
    bufferedWriter.flush();
    buffer = new StringBuffer();
    }

  public void close() throws IOException
    {
    bufferedWriter.close();
    fileWriter.close();
    }

  private File createFile(File file) throws IOException
    {
    File parentDir = new File(file.getParent());
    if (!file.exists())
      {
      parentDir.mkdirs();
      try
        { file.createNewFile(); }
      catch (IOException ioe) { throw new IOException(ioe.getMessage() + " " + file.getAbsolutePath()); }
      }
    fileWriter = new FileWriter(file.getAbsoluteFile());
    bufferedWriter = new BufferedWriter(fileWriter);
    return file;
    }

  }
