/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa.hbase;

import org.apache.log4j.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;

public class DeleteGenome
  {
  static Logger log = Logger.getLogger(DeleteGenome.class.getName());

  public static void main(String[] args) throws Exception
    {
    args = new String[]{"Test"};
    if (args.length < 1)
      {
      System.err.println("Genome name required.");
      System.exit(-1);
      }

    String genome = args[0];

    print("Deleteing genome " + genome);

    Configuration conf = HBaseConfiguration.create();
    //conf.setInt("timeout", 1);
    //    conf.set("hbase.zookeeper.quorum", "10.79.5.22");
    //    conf.set("hbase.zookeeper.property.clientPort","2182");
    //    conf.set("hbase.master", "10.79.5.22:60000");


    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);
    print("Connected");
    //admin.deleteGenome(genome);

    admin.deleteTables();
    admin.createTables();

    admin.closeConections();
    }




  private static void print(String str)
    {
    System.out.println(str);
    }

  }
