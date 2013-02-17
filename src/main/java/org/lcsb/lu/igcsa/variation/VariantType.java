package org.lcsb.lu.igcsa.variation;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

// TODO this is not the best way to do it.  Hard to add a new variation. Work it out later I guess
public enum VariantType
  {
  SNP("snp", org.lcsb.lu.igcsa.variation.SNP.class),
  DELETION("del", Deletion.class),
  INSERTION("ins", Insertion.class),
  TRANSLOCATION("trans", Translocation.class);


  private String shortName;
  private Class<Variation> variantClass;

  VariantType(String name, Class vc)
    {
    this.shortName = name;
    this.variantClass = vc;
    }

  public String getShortName()
    {
    return this.shortName;
    }

  public Class<Variation> getVariation()
    {
    return this.variantClass;
    }

  public static VariantType fromShortName(String n)
    {
    for (VariantType vt: VariantType.values())
      {
      if (vt.getShortName().equals(n)) return vt;
      }
    return null;
    }


  }
