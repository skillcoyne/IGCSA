package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReadPairWritable implements WritableComparable<ReadPairWritable>
  {
  static Logger log = Logger.getLogger(ReadPairWritable.class.getName());

  private long key;

  private String readName;

  private String readSeqA;
  private String qualA;

  private String readSeqB;
  private String qualB;

  public ReadPairWritable()
    {}

  public ReadPairWritable(String[] data)
    {
    if (data.length != 5)
      throw new IllegalArgumentException("Read pair expects 5 columns, got " + data.length);

    this.readName = data[0];
    this.readSeqA = data[1];
    this.qualA = data[2];
    this.readSeqB = data[3];
    this.qualB = data[4];

    getReadKey();
    }

  public ReadPairWritable(String readName)
    {
    this.readName = readName;
    getReadKey();
    }

  public ReadPairWritable(String readName, String readSeqA, String readSeqB, String qualA, String qualB)
    {
    this.readName = readName;
    this.readSeqA = readSeqA;
    this.readSeqB = readSeqB;
    this.qualA = qualA;
    this.qualB = qualB;
    getReadKey();
    }

  public void setRead(String seq, String qual, int read)
    {
    if (read == 1)
      {
      readSeqA = seq;
      qualA = qual;
      }
    else
      {
      readSeqB = seq;
      qualB = qual;
      }
    }

  @Override
  public int compareTo(ReadPairWritable rpw)
    {
    return new Long(this.key).compareTo(new Long(rpw.key));
    }

  @Override
  public void write(DataOutput output) throws IOException
    {
    Text.writeString(output, readName);
    Text.writeString(output, readSeqA);
    Text.writeString(output, qualA);
    Text.writeString(output, readSeqB);
    Text.writeString(output, qualB);
    }

  @Override
  public void readFields(DataInput dataInput) throws IOException
    {
    readName = Text.readString(dataInput);
    readSeqA = Text.readString(dataInput);
    qualA = Text.readString(dataInput);
    readSeqB = Text.readString(dataInput);
    qualB = Text.readString(dataInput);
    getReadKey();
    }


  public String createRead(int read)
    {
    String str = readName;
    if (read == 1)
      str = str + "/1\n" + readSeqA + "\n+\n" + qualA + "\n";
    else
      str = str + "/2\n" + readSeqB + "\n+\n" + qualB + "\n";

    return str;
    }

  private void getReadKey()
    {
    this.key = Long.parseLong(readName.substring(readName.indexOf(".")+1, readName.indexOf(" ")) );
    }
  }
