package org.lcsb.lu.igcsa.population.utils;


import org.lcsb.lu.igcsa.population.watchmaker.kt.PopulationEvaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.population.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenerationStatistics
  {
  private static GenerationStatistics ourInstance;

  private List<Double> fitnessMeans = new ArrayList<Double>();

  private List<Double> bpRepresentation = new ArrayList<Double>();
  private List<Double> bpSD = new ArrayList<Double>();

  private List<Double> bpSizeMean = new ArrayList<Double>();
  private List<Double> bpSizeSD = new ArrayList<Double>();

  //private DataSet fitnessMeans = new DataSet();
  
  public static GenerationStatistics getTracker()
    {
    if (ourInstance == null)
      ourInstance = new GenerationStatistics();
    return ourInstance;
    }

  private GenerationStatistics()
    {

    }

  public void track(PopulationEvaluation evaluation)
    {
    fitnessMeans.add( evaluation.getFitnessStats().getArithmeticMean() );

    bpRepresentation.add( evaluation.getBpStats().getArithmeticMean() );
    bpSD.add( evaluation.getBpStats().getStandardDeviation() );


    fitnessMeans.add( evaluation.getFitnessStats().getArithmeticMean() );

    bpSizeMean.add(evaluation.getSizeStats().getArithmeticMean());
    bpSizeSD.add( evaluation.getSizeStats().getStandardDeviation() );

    }

  public List<Double> getFitnessMeans()
    {
    return fitnessMeans;
    }

  public List<Double> getBpRepresentation()
    {
    return bpRepresentation;
    }

  public List<Double> getBpSD()
    {
    return bpSD;
    }

  public List<Double> getBpSizeMean()
    {
    return bpSizeMean;
    }

  public List<Double> getBpSizeSD()
    {
    return bpSizeSD;
    }
  }
