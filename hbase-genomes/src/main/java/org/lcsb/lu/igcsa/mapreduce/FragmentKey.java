/**
 * org.lcsb.lu.igcsa.mapreduce
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class FragmentKey implements Serializable
  {
  static Logger log = Logger.getLogger(FragmentKey.class.getName());

  private String chr;
  private long start;
  private long end;

  public FragmentKey(String chr,long start, long end)
    {
    this.chr = chr;
    this.start = start;
    this.end = end;
    }


  public String getChr()
    {
    return chr;
    }

  public long getStart()
    {
    return start;
    }

  public long getEnd()
    {
    return end;
    }

  @Override
  public String toString()
    {
    return chr + ":" + "(" + start + "-" + end + ")";
    }

  public static byte[] toBytes(FragmentKey obj)
    {
    return SerializationUtils.serialize(obj);
    }

  public static FragmentKey fromBytes(byte[] bytes)
    {
    return (FragmentKey) SerializationUtils.deserialize(bytes);
    }



  //  public byte[] toBytes()
//    {
//    SerializationUtils.serialize();
//    }

  }
