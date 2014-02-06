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
public class SAMCoordinateWritable extends SAMWritable
  {
  static Logger log = Logger.getLogger(SAMCoordinateWritable.class.getName());

  private long leftCoordinate;
  private String samRecord;
  private String rfname;
//  private String sqTag = "*";

  public SAMCoordinateWritable()
    {
    this.section = Section.RECORD;
    }

  public SAMCoordinateWritable(String toParse)
    {
    String[] cols = toParse.split("\t");
    if (cols.length < 11)
      throw new RuntimeException("Missing columns in SAM record: " + toParse);

    this.leftCoordinate = Long.parseLong(cols[3]);
    this.rfname = cols[2].trim();
    this.samRecord = toParse.trim();

    this.section = Section.RECORD;
    }

//  public void setSQTag(String tag)
//    {
//    if (tag == null)
//      log.warn("Tag is null for record: " + samRecord);
//    else
//      this.sqTag = tag;
//    }
//
//  public String getSQTag()
//    {
//    return sqTag;
//    }

  public String getRFName()
    {
    return rfname;
    }

  public String getSamRecord()
    {
    return samRecord;
    }

  @Override
  public int compareTo(SAMWritable sw)
    {
    SAMCoordinateWritable coord = (SAMCoordinateWritable) sw;

    long coordA = (this.leftCoordinate > 0)? this.leftCoordinate: Long.MAX_VALUE;
    long coordB = ( coord.leftCoordinate > 0)? coord.leftCoordinate: Long.MAX_VALUE;

    return new Long(coordA).compareTo(coordB);
    }

  @Override
  public void write(DataOutput dataOutput) throws IOException
    {
    dataOutput.writeLong(leftCoordinate);
    Text.writeString(dataOutput, samRecord);
    Text.writeString(dataOutput, rfname);
    //Text.writeString(dataOutput, sqTag);
    }

  @Override
  public void readFields(DataInput dataInput) throws IOException
    {
    leftCoordinate = dataInput.readLong();
    samRecord = Text.readString(dataInput);
    rfname = Text.readString(dataInput);
    //sqTag = Text.readString(dataInput);
    }

  }
