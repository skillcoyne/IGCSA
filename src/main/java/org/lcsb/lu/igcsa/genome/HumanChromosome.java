package org.lcsb.lu.igcsa.genome;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public enum HumanChromosome
    {
    CHR1(1),CHR2(2),CHR3(3),CHR4(4),CHR5(5),CHR6(6),CHR7(7),CHR8(8),
        CHR9(9),CHR10(10),CHR11(11),CHR12(12),CHR13(13),CHR14(14),CHR15(15),CHR16(16),
        CHR17(17),CHR18(18),CHR19(19),CHR20(20),CHR21(21),CHR22(22),CHRX(23),CHRY(24);

    private int chrNum;

    private HumanChromosome(int t)
        {
        chrNum = t;
        }

    public int getChromosomeNum()
        {
        return this.chrNum;
        }

    public String getChromsome()
        {
        return super.toString();
        }
    }
