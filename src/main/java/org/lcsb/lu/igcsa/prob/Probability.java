package org.lcsb.lu.igcsa.prob;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
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
