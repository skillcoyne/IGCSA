/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.dist;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.log4j.Logger;

/**
 * Convinience class currently
 */
public class ContinuousDistribution
  {
  static Logger log = Logger.getLogger(ContinuousDistribution.class.getName());

  private PoissonDistribution poissonDistribution;
  private NormalDistribution normalDistribution;

  /**
   * Creates a continuous Normal distribution with the given mean and standard deviation.
   * @param mean
   * @param stdev
   */
  public ContinuousDistribution(double mean, double stdev)
    {
    normalDistribution = new NormalDistribution(mean, stdev);
    }

  /**
   * Creates a continuous Poisson distribution with the given mean.
   * @param lambda
   */
  public ContinuousDistribution(double lambda)
    {
    poissonDistribution = new PoissonDistribution(lambda);
    }

  public double sample()
    {
    if (normalDistribution != null)
      return normalDistribution.sample();
    else
      return poissonDistribution.sample();
    }




  }
