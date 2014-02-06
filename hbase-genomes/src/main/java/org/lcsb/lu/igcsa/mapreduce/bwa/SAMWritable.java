package org.lcsb.lu.igcsa.mapreduce.bwa;

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
public abstract class SAMWritable implements WritableComparable<SAMWritable>
  {
  static Logger log = Logger.getLogger(SAMWritable.class.getName());

  public enum Section {
  HEADER("header"), RECORD("record");

  private String samSection;
  private Section(String s)
    {
    samSection = s;
    }

  public String getSection()
    {
    return samSection;
    }
  }

  protected Section section;

  @Override
  public abstract void write(DataOutput dataOutput) throws IOException;

  @Override
  public abstract void readFields(DataInput dataInput) throws IOException;
  }
