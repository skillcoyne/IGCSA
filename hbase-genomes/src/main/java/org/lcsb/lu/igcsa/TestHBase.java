/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa;

import org.apache.hadoop.hbase.ipc.HBaseServer;
import org.apache.log4j.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestHBase
  {
  static Logger log = Logger.getLogger(TestHBase.class.getName());

  static String tableName = "genome";


  public static void foo(String[] args) throws Exception
    {
    tableName = "stuff";
    //    DriverManagerDataSource ds = new DriverManagerDataSource("localhost");
    //    ds.setDriverClassName("jdbc:phoenix:localhost");
    Connection conn = DriverManager.getConnection("jdbc:phoenix:localhost");

    //    JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);


    Statement stmt = conn.createStatement();
    stmt.execute("CREATE TABLE stuff (d, seq) ");


    conn.close();
    }


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    conf.setInt("timeout", 10);

    HBaseAdmin admin = new HBaseAdmin(conf);

    try
      {
      HTableDescriptor descriptor = new HTableDescriptor(tableName);
      descriptor.addFamily(new HColumnDescriptor("d"));
      descriptor.addFamily(new HColumnDescriptor("seq"));

      if (!admin.tableExists(tableName))
        admin.createTable(descriptor);

      HTable table = new HTable(conf, tableName);
      Put put = new Put(Bytes.toBytes("GRCh37:1:1000"));
      put.add(Bytes.toBytes("d"), Bytes.toBytes("name"), Bytes.toBytes("GRCh37"));
      //put.add(Bytes.toBytes("d"), Bytes.toBytes("parent"), Bytes.toBytes("2012"));


      put.add(Bytes.toBytes("seq"), Bytes.toBytes("sequence"), Bytes.toBytes("AAAAAA"));
      put.add(Bytes.toBytes("seq"), Bytes.toBytes("start"), Bytes.toBytes("1"));
      put.add(Bytes.toBytes("seq"), Bytes.toBytes("end"), Bytes.toBytes("1000"));

      table.put(put);

      put = new Put(Bytes.toBytes("igcsa1:1:1000"));
      put.add(Bytes.toBytes("d"), Bytes.toBytes("name"), Bytes.toBytes("igcsa1"));
      put.add(Bytes.toBytes("d"), Bytes.toBytes("parent"), Bytes.toBytes("GRCh37"));


      put.add(Bytes.toBytes("seq"), Bytes.toBytes("sequence"), Bytes.toBytes("GCGCGC"));
      put.add(Bytes.toBytes("seq"), Bytes.toBytes("start"), Bytes.toBytes("1"));
      put.add(Bytes.toBytes("seq"), Bytes.toBytes("end"), Bytes.toBytes("1000"));


      table.put(put);


      Get get = new Get(Bytes.toBytes("GRCh37:1:1000"));
      Result result = table.get(get);
      System.out.println(new String(result.value()));
      System.out.println( new String(result.getValue(Bytes.toBytes("seq"), Bytes.toBytes("sequence")))  );

      }
    finally
      {
      //      admin.disableTable(tableName);
      //      admin.deleteTable(tableName);
      admin.close();
      }
    }

  }
