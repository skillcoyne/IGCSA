package org.lcsb.lu.igcsa.prob;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ProbabilityException extends Exception
  {
  public ProbabilityException()
    {
    }

  public ProbabilityException(String s, Throwable throwable)
    {
    super(s, throwable);
    }

  public ProbabilityException(Throwable throwable)
    {
    super(throwable);
    }

  public ProbabilityException(String s)
    {
    super(s);
    }
  }
