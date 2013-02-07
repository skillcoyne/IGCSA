package org.lcsb.lu.igcsa.prob;

/**
 * org.lcsb.lu.igcsa.prob
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

public class Probability
    {
    private String name;
    private double probability;

    public Probability(String name, double prob)
        {
        this.name = name;
        this.probability = prob;
        }

    public double getProbability()
        {
        return this.probability;
        }

    public String getName()
        {
        return this.name;
        }


    }
