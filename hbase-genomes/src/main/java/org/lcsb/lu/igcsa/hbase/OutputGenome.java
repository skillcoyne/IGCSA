/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.lcsb.lu.igcsa.hbase.tables.variation.*;

import java.util.List;


public class OutputGenome
  {
  private static final Log log = LogFactory.getLog(OutputGenome.class);

  /**
   * org.lcsb.lu.igcsa
   * Author: sarah.killcoyne
   * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
   * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
   */


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    //    conf.setInt("timeout", 120000);
    //    conf.set("hbase.master", "*" + "bmf00004.uni.lux" + ":9000*");
    //    conf.set("hbase.zookeeper.quorum", "bmf00004.uni.lux");
    //    conf.set("hbase.zookeeper.property.clientPort", "2181");
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

    //  SequenceResult sr =     admin.getSequenceTable().queryTable("GRCh37-11:00043501");
    //    log.info(sr);

    VariationAdmin vadmin = VariationAdmin.getInstance();
//    GCBin table = (GCBin) vadmin.getTable(VariationTables.GC.getTableName());
//
//    for (Object r: table.getRows())
//      {
//      GCBin.GCResult gc = table.createResult((Result) r);
//      log.info( gc.getChromosome() + " " + gc.getMin() + "-" + gc.getMax() + " " + gc.getTotalFragments());
//      }

//    GCBin.GCResult gc = table.getBinFor("1", 230);
//    log.info( gc.getChromosome() + " " + gc.getMin() + "-" + gc.getMax() + " " + gc.getTotalFragments());

    VariationCountPerBin table = (VariationCountPerBin) vadmin.getTable(VariationTables.VPB.getTableName());

//    VCPBResult result = table.queryTable(VCPBRow.createRowId("1", "SNV", 0, 85, 1) );

//    List<VCPBResult> results = table.getFragment("1", 340, 425, 6);
//    for (VCPBResult result: results)
//      {
//      log.info(result.getRowId());
//      log.info(result.getVariationClass());
//      log.info(result);
//      }


//    ResultScanner scanner = table.getScanner(new Scan());
//    Iterator<Result> rI = scanner.iterator();
//    while(rI.hasNext())
//      {
//      VCPBResult result = table.createResult(rI.next());
//      if(result.getFragmentNum() <= 1)
//        {
//        log.info(result.getRowId());
//        log.info(result);
//        }
//      }

    //    VCPBResult result = table.getFragment("6", 252, 336, 1);
//    log.info(result);

//    for (ChromosomeResult chr: admin.getChromosomeTable().getChromosomesFor("Test"))
//      {
//      String c = chr.getChrName();
//      log.info(c + " " + chr.getSegmentNumber() + " " + chr.getLength());
//
//      long segments = chr.getSegmentNumber();
//      for (int i = 1; i <= segments; i++)
//        {
//        SequenceResult seq = admin.getSequenceTable().getSequenceFor(chr, (long)i);
//
//        log.info( seq.getStart() + ", " + seq.getEnd() );
//        }
//      }

    }


  }
