package org.lcsb.lu.igcsa.genome;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Location
    {
    private int Start;
    private int End;
    private int Length;

    private HumanChromosome Chromosome;

    public Location(int s, int e, HumanChromosome chr) throws Exception
        {
        this.Chromosome = chr;
        this.Start = s;
        this.End = e;

        if (Start > End)
            {
            throw new Exception("The Start position should come before the End position.");
            }

        this.Length = End - Start;
        }

    public int getStart()
        {
        return Start;
        }

    public int getEnd()
        {
        return End;
        }

    public int getLocationSize()
        {
        return Length;
        }

    public HumanChromosome getChromosome()
        {
        return Chromosome;
        }
    }
