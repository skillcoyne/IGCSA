/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.aberrations;


import java.util.HashMap;
import java.util.Map;

public enum AberrationTypes
  {

    DUPLICATION   ("dup", "org.lcsb.lu.igcsa.aberrations.single.Duplication"),
    INVERSION     ("inv", "org.lcsb.lu.igcsa.aberrations.single.Inversion"),
    ADDITION      ("add", "org.lcsb.lu.igcsa.aberrations.single.Addition"),
    ISOCENTRIC    ("iso", "org.lcsb.lu.igcsa.aberrations.single.Iso"),
    DELETION      ("del", "org.lcsb.lu.igcsa.aberrations.single.Deletion"),

    //INSERTION     ("ins",   "org.lcsb.lu.igcsa.aberrations.multiple.Insertion"),  // Insertion requires that 2 derivative chromosomes are created.  One is a translocation, the other is a deletion. Current implementation doesn't easily allow for this
    DICENTRIC     ("dic",   "org.lcsb.lu.igcsa.aberrations.multiple.Dicentric"),
    TRANSLOCATION ("trans", "org.lcsb.lu.igcsa.aberrations.multiple.Translocation");

  private static Map<String, AberrationTypes> nameMap = new HashMap<String, AberrationTypes>();
  private static Map<String, AberrationTypes> classMap = new HashMap<String, AberrationTypes>();
  static
    {
    for (AberrationTypes at: values())
      {
      nameMap.put(at.getShortName(), at);
      classMap.put(at.aberrationClass, at);
      }
    }


  private String cytogeneticDesignation;
  private String aberrationClass;

  private AberrationTypes(String cytogeneticDesignation, String aberrationClass)
    {
    this.cytogeneticDesignation = cytogeneticDesignation;
    this.aberrationClass = aberrationClass;
    }

  public String getShortName()
    {
    return cytogeneticDesignation;
    }

  public String getCytogeneticDesignation()
    {
    return cytogeneticDesignation;
    }

  public Class getAberrationClass() throws ClassNotFoundException
    {
    return Class.forName(aberrationClass);
    }

  public static Class getClassFor(String cytogenetic) throws ClassNotFoundException
    {
    return nameMap.get(cytogenetic).getAberrationClass();
    }

  public static boolean hasClassFor(String cytogenetic)
    {
    return nameMap.containsKey(cytogenetic);
    }

  public static String getNameFor(String classname)
    {
    return classMap.get(classname).getShortName();
    }



  }
