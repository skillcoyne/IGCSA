/**
 * org.lcsb.lu.igcsa.hbase.tables
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.tables;

import org.apache.log4j.Logger;

public enum SmallMutation
  {
  SNV("SNV"), DEL("deletion"), INS("insertion"), INDEL("indel"), SUB("substitution");

  private String type;
  SmallMutation(String type)
    {
    this.type = type;
    }

  public String getType()
    {
    return this.type;
    }
  }
