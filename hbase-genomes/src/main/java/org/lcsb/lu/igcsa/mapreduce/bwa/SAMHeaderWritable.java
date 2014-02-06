package org.lcsb.lu.igcsa.mapreduce.bwa;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * org.lcsb.lu.igcsa.mapreduce.bwa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SAMHeaderWritable extends SAMWritable
  {
  static Logger log = Logger.getLogger(SAMHeaderWritable.class.getName());

  static enum HeaderType
    {
      RG, SQ, PG, CG
    }

  ;

  private String hd = "";
  private List<String> refSQDict = new ArrayList<String>();
  private List<String> readGroups = new ArrayList<String>();
  private List<String> program = new ArrayList<String>();
  private List<String> comments = new ArrayList<String>();

  public SAMHeaderWritable()
    {
    this("");
    }

  public SAMHeaderWritable(String hd)
    {
    this.section = Section.HEADER;
    this.hd = hd;
    }

  public void addRG(String rg)
    {
    this.readGroups.add(rg);
    }

  public void addSQ(String sq)
    {
    this.refSQDict.add(sq);
    }

  public void addPG(String pg)
    {
    this.program.add(pg);
    }

  public void addCG(String cg)
    {
    this.comments.add(cg);
    }

  public void addLine(String line)
    {
    String sec = line.substring(1, 3);
    switch (HeaderType.valueOf(sec))
      {
      case RG:
        this.addRG(line); break;
      case SQ:
        this.addSQ(line); break;
      case PG:
        this.addPG(line); break;
      case CG:
        this.addCG(line); break;
      }
    }

  public void setHeaderSection(List<String> entries, HeaderType type)
    {
    switch (type)
      {
      case RG:
        this.readGroups = entries;
        break;
      case SQ:
        this.refSQDict = entries;
        break;
      case PG:
        this.program = entries;
        break;
      case CG:
        this.comments = entries;
        break;
      }
    }


  @Override
  public void write(DataOutput dataOutput) throws IOException
    {
    Text.writeString(dataOutput, hd);

    Text.writeString(dataOutput, StringUtils.join(refSQDict.iterator(), ","));
    Text.writeString(dataOutput, StringUtils.join(readGroups.iterator(), ","));
    Text.writeString(dataOutput, StringUtils.join(program.iterator(), ","));
    Text.writeString(dataOutput, StringUtils.join(comments.iterator(), ","));
    }

  @Override
  public void readFields(DataInput dataInput) throws IOException
    {
    hd = Text.readString(dataInput);
    refSQDict = readArray(dataInput);
    readGroups = readArray(dataInput);
    program = readArray(dataInput);
    comments = readArray(dataInput);
    }

  private List<String> readArray(DataInput input) throws IOException
    {
    List<String> readInto = new ArrayList<String>();

    String str = Text.readString(input);
    if (str != null)
      {
      for (String rs : str.split(","))
        readInto.add(rs);
      }

    return readInto;
    }

  @Override
  public int compareTo(SAMWritable samWritable)
    {
    return 0;
    }
  }
