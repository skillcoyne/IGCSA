package org.lcsb.lu.igcsa.variation;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class VariantException extends Exception
  {
  public VariantException()
    {
    }

  public VariantException(String s)
    {
    super(s);
    }

  public VariantException(String s, Throwable throwable)
    {
    super(s, throwable);
    }

  public VariantException(Throwable throwable)
    {
    super(throwable);
    }
  }
