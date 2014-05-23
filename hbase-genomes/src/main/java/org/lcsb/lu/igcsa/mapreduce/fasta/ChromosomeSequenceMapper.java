package org.lcsb.lu.igcsa.mapreduce.fasta;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.mapreduce.Job;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.genomes.SequenceResult;
import org.lcsb.lu.igcsa.mapreduce.FragmentWritable;
import org.lcsb.lu.igcsa.mapreduce.SegmentOrderComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * *************************
 * MAPPER & REDUCER
 * **************************
 */
public class ChromosomeSequenceMapper extends TableMapper<SegmentOrderComparator, FragmentWritable>
  {
  public enum SegmentCounters {
    SEGMENTS
  }


  private static final Log log = LogFactory.getLog(ChromosomeSequenceMapper.class);

  private List<String> chrs;
  private HBaseGenomeAdmin admin;

  public static void setChromosomes(Job job, String... chrs)
    {
    job.getConfiguration().setStrings("chromosomes", chrs);
    log.info("Setting chromosomes in config: " + chrs);
    }

  @Override
  protected void setup(Context context) throws IOException, InterruptedException
    {
    chrs = new ArrayList<String>(context.getConfiguration().getStringCollection("chromosomes"));
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());
    }

  @Override
  protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException
    {
    SequenceResult sr = admin.getSequenceTable().createResult(value);

    String sequence = sr.getSequence();
    SegmentOrderComparator soc = new SegmentOrderComparator(chrs.indexOf(sr.getChr()), sr.getSegmentNum());
    if (soc.getOrder() < 0)
      throw new IOException("Failed to load all chromosomes, missing " + sr.getChr());

    FragmentWritable fw = new FragmentWritable(sr.getChr(), sr.getStart(), sr.getEnd(), sr.getSegmentNum(), sequence);

    context.getCounter(SegmentCounters.SEGMENTS).increment(1);
    context.write(soc, fw);
    }
  }
