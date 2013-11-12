package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class GenomeRow
  {
  private final byte[] rowName;

  private GenomeColumnFamily columnFamily = GenomeColumnFamily.getColumnFamilies();

  protected GenomeRow(String name, int start, int end)
    {
    if (end <= start)
      throw new IllegalArgumentException("End cannot come before start.");

    rowName = Bytes.toBytes(name + ":" + start + ":" + end);
    }

  protected Put addColumnValue(GenomeColumnFamily.Families f, String col, String value)
    {
    Put put = new Put(rowName);
    put.add(columnFamily.getFamily(f), Bytes.toBytes(col), Bytes.toBytes(value));

    return put;
    }

  protected Get getValue()
    {
    Get get = new Get(rowName);
    return get;
    //Result result = table.get(get);
//    System.out.println(new String(result.value()));
//    System.out.println( new String(result.getValue(Bytes.toBytes("seq"), Bytes.toBytes("sequence")))  );

    }




  }
