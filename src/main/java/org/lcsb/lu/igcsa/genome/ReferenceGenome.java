package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenome extends AbstractGenome
  {
  private String build;
  private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

  public ReferenceGenome()
    {
    super();
    }

  public ReferenceGenome(String build)
    {
    super(build);
    }

  }



