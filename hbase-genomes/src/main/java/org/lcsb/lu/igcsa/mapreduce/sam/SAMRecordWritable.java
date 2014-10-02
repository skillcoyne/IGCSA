/**
 * org.lcsb.lu.igcsa.mapreduce.sam
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.mapreduce.sam;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// This writeble does NOT contain all the information in a SAMRecord, just the info that I need currently
public class SAMRecordWritable implements Writable
  {
  private static final Log log = LogFactory.getLog(SAMRecordWritable.class);

  private SAMRecord samRecord;

  public SAMRecordWritable()
    {}

  public SAMRecordWritable(SAMRecord record)
    {
    samRecord = record;
    }

  public SAMRecord getSamRecord()
    {
    return samRecord;
    }

  @Override
  public void write(DataOutput out) throws IOException
    {
    // boolean flags
//    out.writeBoolean(samRecord.getDuplicateReadFlag());
//    out.writeBoolean(samRecord.getProperPairFlag());
//    out.writeBoolean(samRecord.getFirstOfPairFlag());
//    out.writeBoolean(samRecord.getMateNegativeStrandFlag());
//    out.writeBoolean(samRecord.getMateUnmappedFlag());
//    out.writeBoolean(samRecord.getNotPrimaryAlignmentFlag());
//    out.writeBoolean(samRecord.getReadFailsVendorQualityCheckFlag());
//    out.writeBoolean(samRecord.getSecondOfPairFlag());

    // ints
    out.writeInt(samRecord.getFlags());
    out.writeInt(samRecord.getAlignmentStart());
    out.writeInt(samRecord.getMateAlignmentStart());
    out.writeInt(samRecord.getReferenceIndex());
    out.writeInt(samRecord.getMappingQuality());
    out.writeInt(samRecord.getInferredInsertSize());

    // Strings
    Text.writeString(out, samRecord.getReferenceName());
    Text.writeString(out, samRecord.getMateReferenceName());
    Text.writeString(out, samRecord.getReadName());
    Text.writeString(out, samRecord.getCigarString());
    Text.writeString(out, samRecord.getBaseQualityString());
    Text.writeString(out, new String(samRecord.getReadBases()));
    Text.writeString(out, new String(samRecord.getOriginalBaseQualities()));
    }

  @Override
  public void readFields(DataInput in) throws IOException
    {
    samRecord = new SAMRecord(null);
    // boolean flags
//    samRecord.setDuplicateReadFlag(in.readBoolean());
//    samRecord.setProperPairFlag(in.readBoolean());
//    samRecord.setFirstOfPairFlag(in.readBoolean());
//    samRecord.setMateNegativeStrandFlag(in.readBoolean());
//    samRecord.setMateUnmappedFlag(in.readBoolean());
//    samRecord.setNotPrimaryAlignmentFlag(in.readBoolean());
//    samRecord.setReadFailsVendorQualityCheckFlag(in.readBoolean());
//    samRecord.setSecondOfPairFlag(in.readBoolean());

    // ints
    samRecord.setFlags(in.readInt());
    samRecord.setAlignmentStart(in.readInt());
    samRecord.setMateAlignmentStart(in.readInt());
    samRecord.setReferenceIndex(in.readInt());
    samRecord.setMappingQuality(in.readInt());
    samRecord.setInferredInsertSize(in.readInt());

    // Strings
    samRecord.setReferenceName(Text.readString(in));
    samRecord.setMateReferenceName(Text.readString(in));
    samRecord.setReadName(Text.readString(in));
    samRecord.setCigarString(Text.readString(in));
    samRecord.setBaseQualityString(Text.readString(in));
    samRecord.setReadBases( Text.readString(in).getBytes() );
    samRecord.setOriginalBaseQualities( Text.readString(in).getBytes() );
    }
  }
