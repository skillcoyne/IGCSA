package org.lcsb.lu.igcsa.utils;


import org.lcsb.lu.igcsa.watchmaker.kt.PopulationEvaluation;
import org.uncommons.maths.statistics.DataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.utils
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenerationStatistics
  {
  private static GenerationStatistics ourInstance;

  private List<Double> fitnessMeans = new ArrayList<Double>();


  private List<Double> complexity = new ArrayList<Double>();
  private List<Double> complexitySD = new ArrayList<Double>();

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

    complexity.add( evaluation.getComplexityStats().getArithmeticMean() );
    complexitySD.add( evaluation.getComplexityStats().getStandardDeviation() );

    bpRepresentation.add( evaluation.getBpStats().getArithmeticMean() );
    bpSD.add( evaluation.getBpStats().getStandardDeviation() );


    fitnessMeans.add( evaluation.getFitnessStats().getArithmeticMean() );

    bpSizeMean.add(evaluation.getSizeStats().getArithmeticMean());
    bpSizeSD.add( evaluation.getSizeStats().getStandardDeviation() );

    //evaluation.getComplexityStats().g
    }

  public List<Double> getFitnessMeans()
    {
    return fitnessMeans;
    }

  public List<Double> getComplexityMeans()
    {
    return complexity;
    }

  public List<Double> getComplexitySD()
    {
    return complexitySD;
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
