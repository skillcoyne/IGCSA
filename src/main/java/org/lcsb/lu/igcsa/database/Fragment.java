package org.lcsb.lu.igcsa.database;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Fragment
  {
  private String chr;
  private int binId;
  private int SNV;
  private int deletion;
  private int indel;
  private int insertion;
  private int seqAlt;
  private int substitution;
  private int tandemRepeat;

  public String getChr()
    {
    return chr;
    }

  public void setChr(String chr)
    {
    this.chr = chr;
    }

  public int getBinId()
    {
    return binId;
    }

  public void setBinId(int binId)
    {
    this.binId = binId;
    }

  public int getSNV()
    {
    return SNV;
    }

  public void setSNV(int SNV)
    {
    this.SNV = SNV;
    }

  public int getDeletion()
    {
    return deletion;
    }

  public void setDeletion(int deletion)
    {
    this.deletion = deletion;
    }

  public int getIndel()
    {
    return indel;
    }

  public void setIndel(int indel)
    {
    this.indel = indel;
    }

  public int getInsertion()
    {
    return insertion;
    }

  public void setInsertion(int insertion)
    {
    this.insertion = insertion;
    }

  public int getSeqAlt()
    {
    return seqAlt;
    }

  public void setSeqAlt(int seqAlt)
    {
    this.seqAlt = seqAlt;
    }

  public int getSubstitution()
    {
    return substitution;
    }

  public void setSubstitution(int substitution)
    {
    this.substitution = substitution;
    }

  public int getTandemRepeat()
    {
    return tandemRepeat;
    }

  public void setTandemRepeat(int tandem_repeat)
    {
    this.tandemRepeat = tandem_repeat;
    }

  public String toString()
    {
    String str = "Chr " + this.chr + ", bin " + this.binId + ": " + this.SNV + " " + this.deletion + " " + this.indel + " " + this.insertion + " " + this.seqAlt + " " +
        this.substitution + " " + this.tandemRepeat;
    return super.toString() + ": " + str;
    }
  }
