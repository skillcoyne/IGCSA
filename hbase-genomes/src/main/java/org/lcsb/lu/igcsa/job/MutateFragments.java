/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.job;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Scan;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;

import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.Column;

import org.lcsb.lu.igcsa.mapreduce.figg.FragmentMutationMapper;


public class MutateFragments extends JobIGCSA
  {
  private static final Log log = LogFactory.getLog(MutateFragments.class);

  public MutateFragments()
    {
    super(new Configuration());

    Option m = new Option("m", "mutation", true, "Name for mutated genome.");
    m.setRequired(true);
    this.addOptions(m);

    Option p = new Option("p", "parent", true, "Name for parent genome.");
    p.setRequired(true);
    this.addOptions(p);

    Option t = new Option("t", "Test", false, "");
    p.setRequired(false);
    this.addOptions(t);
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(), this.getClass());
    if (args.length < 1)
      {
      System.err.println("Usage: " + MutateFragments.class.getSimpleName() + " -p <parent genome> -m <new genome name>");
      System.exit(-1);
      }

    String genome = cl.getOptionValue("m");
    String parent = cl.getOptionValue("p");
    getConf().set("genome", genome);
    getConf().set("parent", parent);

    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    if (genomeAdmin.getGenomeTable().getGenome(parent) == null)
      {
      System.err.println("Parent genome '" + parent + "' does not exist! Exiting.");
      System.exit(-1);
      }

    if (genomeAdmin.getGenomeTable().getGenome(genome) != null)
      {
      log.info(genome + " already exists, deleting.");
      genomeAdmin.deleteGenome(genome);
      }
    log.info("Adding genome " + genome);
    genomeAdmin.getGenomeTable().addGenome(genome, parent);

    Job job = new Job(getConf(), "Genome Fragment Mutation");
    job.setJarByClass(MutateFragments.class);

    // this scan will get all sequences for the given genome (so 200 million) -- or it would if you could use 0.94
//    List<Pair<byte[], byte[]>> fuzzyKeys = new ArrayList<Pair<byte[], byte[]>>();
//    fuzzyKeys.add(
//        new Pair<byte[],byte[]>(Bytes.toBytes("????????????:" + "?" + "-" + parent),
//                                //         ? ? ? ? ? ? ? ? ? ? ? ? : ? - G R C h 3 7
//                                new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0,0,0,0})
//    );
//
//    FuzzyRowFilter filter = new FuzzyRowFilter(fuzzyKeys);
    //Scan scan = new Scan();
    Scan scan = genomeAdmin.getSequenceTable().getScanFor(new Column("info", "genome", parent));
    scan.setCaching(100);

    TableMapReduceUtil.initTableMapperJob(genomeAdmin.getSequenceTable().getTableName(), scan, FragmentMutationMapper.class, null, null,
                                          job);
    // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);

    job.setSpeculativeExecution(false);
    job.setReduceSpeculativeExecution(false);

    return (job.waitForCompletion(true) ? 0 : 1);
    }


  public static void main(String[] args) throws Exception
    {
    ToolRunner.run(new MutateFragments(), args);
    }
  }
