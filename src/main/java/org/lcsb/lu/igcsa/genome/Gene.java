package org.lcsb.lu.igcsa.genome;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Gene
    {
    private Location location;
    private String name;
    private String sequence;

    public Gene(String n, Location loc, String seq)
        {
        location = loc;
        name = n;
        sequence = seq;
        }

    public Gene(String n, int s, int e, HumanChromosome c, String seq) throws Exception
        {
        this(n, new Location(s, e, c), seq);
        }
    }
