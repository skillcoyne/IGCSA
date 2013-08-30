package org.lcsb.lu.igcsa.utils;


import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class HammingDistance
  {
  static Logger log = Logger.getLogger(HammingDistance.class.getName());


  public static int getDistance(String a, String b)
    {
    return HammingDistance.getDistance( a.toUpperCase().getBytes(), b.toUpperCase().getBytes() );
    }


  public static int getDistance(DNASequence a, DNASequence b)
    {
    return getDistance(a.getSequence().getBytes(), b.getSequence().getBytes());
    }

  public static int getDistance(byte[] a, byte[] b)
    {
    if (a.length != b.length)
      throw new IllegalArgumentException("Strings must be of the same length.");

    int distance = 0;
    for (int i=0; i<a.length; i++)
      distance += (a[i] == b[i])? 0: 1;

    return distance;
    }



  }
