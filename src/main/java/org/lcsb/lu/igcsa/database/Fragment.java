package org.lcsb.lu.igcsa.database;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Fragment
  {

  private int SNV;
  private int deletion;
  private int indel;
  private int insertion;
  private int seqAlt;
  private int substitution;
  private int tandem_repeat;

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
    return tandem_repeat;
    }

  public void setTandemRepeat(int tandem_repeat)
    {
    this.tandem_repeat = tandem_repeat;
    }
  }
