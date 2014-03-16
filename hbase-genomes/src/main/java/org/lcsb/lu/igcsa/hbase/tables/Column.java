package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class Column
  {
  private byte[] famliy;
  private byte[] qualifier;
  private byte[] value;

  public Column()
    {
    this.setColumn(null, null);
    }

  public Column(String family)
    {
    this.setColumn(family, null);
    }

  public Column(String family, String qualifier)
    {
    this.setColumn(family, qualifier);
    }

  public Column(String famliy, String qualifier, String value)
    {
    this.setColumn(famliy, qualifier);
    this.value = (value != null) ? (Bytes.toBytes(value)) : (new byte[0]);
    }

  public Column(String famliy, String qualifier, int value)
    {
    this.setColumn(famliy, qualifier);
    this.value = Bytes.toBytes(value);
    }

  public Column(String famliy, String qualifier, double value)
    {
    this.setColumn(famliy, qualifier);
    this.value = Bytes.toBytes(value);
    }

  public Column(String family, String qualifier, long value)
    {
    this.setColumn(family, qualifier);
    this.value = Bytes.toBytes(value);
    }

  public byte[] getFamliy()
    {
    return famliy;
    }

  public byte[] getQualifier()
    {
    return qualifier;
    }

  public byte[] getValue()
    {
    return value;
    }

  public String getFamilyAsString()
    {
    return Bytes.toString(famliy);
    }

  public String getQualifierAsString()
    {
    return Bytes.toString(qualifier);
    }

  public String getValueAsString()
    {
    return Bytes.toString(value);
    }

  public boolean hasFamily()
    {
    return (this.famliy != null && this.famliy.length > 0) ? true : false;
    }

  public boolean hasQualifier()
    {
    return (this.hasFamily() && this.qualifier != null && this.qualifier.length > 0) ? true : false;
    }

  public boolean hasValue()
    {
    return (hasFamily() && hasQualifier() && this.value != null && this.value.length > 0) ? true : false;
    }


  private void setColumn(String family, String qualifier)
    {
    this.famliy = (family != null) ? (Bytes.toBytes(family)) : (new byte[0]);
    this.qualifier = (qualifier != null) ? (Bytes.toBytes(qualifier)) : (new byte[0]);
    }

  }
