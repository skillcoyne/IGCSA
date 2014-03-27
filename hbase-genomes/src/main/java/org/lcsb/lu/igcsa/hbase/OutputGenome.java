/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RandomRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;
import org.lcsb.lu.igcsa.hbase.tables.variation.GCBin;
import org.lcsb.lu.igcsa.hbase.tables.variation.VariationTables;

import java.util.*;


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
//    //    conf.setInt("timeout", 120000);
//    //    conf.set("hbase.master", "*" + "bmf00004.uni.lux" + ":9000*");
//    //    conf.set("hbase.zookeeper.quorum", "bmf00004.uni.lux");
//    //    conf.set("hbase.zookeeper.property.clientPort", "2181");
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

//    String genome = "GRCh37";
//    String chr = "1";
//    char c = SequenceRow.initialChar(chr);
//    String rowKey = "????????????:" + "?" + "-" + genome;
//    List<Pair<byte[], byte[]>> fuzzyKeys = new ArrayList<Pair<byte[], byte[]>>();
//    fuzzyKeys.add(
//        new Pair<byte[],byte[]>(Bytes.toBytes(rowKey),
////                                         ? ? ? ? ? ? ? ? ? ? ? ? : ? - G R C h 3 7
//                                new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0,0,0,0})
//    );
//
//    FuzzyRowFilter filter = new FuzzyRowFilter(fuzzyKeys);
//    Scan scan = new Scan();
//    scan.setFilter(filter);
//
//    ResultScanner scanner = admin.getSequenceTable().getScanner(scan);
//    Iterator<Result> rI = scanner.iterator();
//
//    //Iterator<Result> rI =  admin.getSequenceTable().getSequencesFor("GRCh37", "1", 1, 1000);
//    List<Long> frags = new ArrayList<Long>();
//    int i=0;
//    while (rI.hasNext())
//      {
//      SequenceResult sr = admin.getSequenceTable().createResult(rI.next());
//      if (!sr.getGenome().equals(genome))
//        {
//        log.info(sr.getRowId() + "\t" + sr.getStart());
//        System.exit(-1);
//        }
//      System.out.print('.');
//      if (i > 0 && i%100 == 0)
//        System.out.println();
////      frags.add(sr.getStart());
//      i++;
//      }

//    Collections.sort(frags);

//    log.info("*****  " + i);
//    ChromosomeResult cr = admin.getChromosomeTable().getChromosome("GRCh37", chr);
//    log.info(cr.getChrName() + " len=" + cr.getLength() + " seg=" + cr.getSegmentNumber());

//    Iterator<Result> sI = admin.getSequenceTable().getSequencesFor("GRCh37", chr, 10000, 18001);
//    if (!sI.hasNext())
//      log.info("fucked up");
//    while(sI.hasNext())
//      {
//      SequenceResult sr =  admin.getSequenceTable().createResult(sI.next());
//      log.info(sr.getStart() + "-" + sr.getEnd() + "\t" + sr.getSegmentNum());
//      }

    //    SequenceResult r = admin.getSequenceTable().queryTable("AAAA00024002:1-GRCh37");
//
//    System.out.println(" " + r.getRowId() + " " + r.getChr());

    //Scan seqScan = admin.getSequenceTable().getScanFor(new Column("info", "genome", "GRCh37"));


    //        for (ChromosomeResult chr: admin.getChromosomeTable().getChromosomesFor("GRCh37"))
    //          {
    //          String c = chr.getChrName();
    //          log.info(c + " " + chr.getSegmentNumber() + " " + chr.getLength());
    //          }
    //
    //  SequenceResult sr =     admin.getSequenceTable().queryTable("GRCh37-11:00043501");
    //    log.info(sr);

    String chr = "1";
    VariationAdmin vadmin = VariationAdmin.getInstance();
    GCBin gcTable = (GCBin) vadmin.getTable(VariationTables.GC.getTableName());
    List<GCBin.GCResult> bins = gcTable.getBins().get(chr);

    GCBin.GCResult gcResult = gcTable.getMaxBin("1");

    int gcContent = 336;
    if (gcContent < gcResult.getMax())
      gcResult = gcTable.getBinFor("1", gcContent);
    log.info(gcContent >= gcResult.getMax());

    gcResult = bins.get( 8 );
    log.info(gcResult);


    RandomRowFilter randomFilter = new RandomRowFilter(.001f);
    FilterList orFilter = new FilterList(FilterList.Operator.MUST_PASS_ONE);
    orFilter.addFilter(randomFilter);

    Scan scan = new Scan();
    scan.setFilter(orFilter);
    orFilter.addFilter(scan.getFilter());


    ResultScanner scanner = vadmin.getTable(VariationTables.VPB.getTableName()).getScanner(scan);
    Iterator<Result> rI = scanner.iterator();
    while (rI.hasNext())
      {
      log.info(rI.next());
      }


    //
//    //
//    //    for (Object r: table.getRows())
//    //      {
//    //      GCBin.GCResult gc = table.createResult((Result) r);
//    //      log.info( gc.getChromosome() + " " + gc.getMin() + "-" + gc.getMax() + " " + gc.getTotalFragments());
//    //      }
//
//    //    GCBin.GCResult gc = table.getBinFor("1", 230);
//    //    log.info( gc.getChromosome() + " " + gc.getMin() + "-" + gc.getMax() + " " + gc.getTotalFragments());
//
//    //VariationCountPerBin table = (VariationCountPerBin) vadmin.getTable(VariationTables.VPB.getTableName());
//
//    //    VCPBResult result = table.queryTable(VCPBRow.createRowId("1", "SNV", 0, 85, 1) );
//
//    //    List<VCPBResult> results = table.getFragment("1", 340, 425, 6);
//    //    for (VCPBResult result: results)
//    //      {
//    //      log.info(result.getRowId());
//    //      log.info(result.getVariationClass());
//    //      log.info(result);
//    //      }
//
//
//    //    ResultScanner scanner = table.getScanner(new Scan());
//    //    Iterator<Result> rI = scanner.iterator();
//    //    while(rI.hasNext())
//    //      {
//    //      VCPBResult result = table.createResult(rI.next());
//    //      if(result.getFragmentNum() <= 1)
//    //        {
//    //        log.info(result.getRowId());
//    //        log.info(result);
//    //        }
//    //      }
//
//    //    VCPBResult result = table.getFragment("6", 252, 336, 1);
//    //    log.info(result);


    }


  }
