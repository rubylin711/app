package com.prime.dtv.sysdata;

public enum EnPolarity
{
    /**
     * Horizontal polarization. <br>
     *
     */
    HORIZONTAL,
    /**
     * Vertical polarization. <br>
     *
     */
    VERTICAL,
    /**
     * Left-handed circularly polarized. <br>
     *
     */
    LEFTHAND,
    /**
     * Right-handed circularly polarized. <br>
     *
     */
    RIGHTHAND;

    /**
     * Get enumeration type value based on the value of the enumeration index. <br>
     *
     * @return EnPolarity.
     */
    public static EnPolarity valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("EnPolarity Invalid ordinal=" + ordinal);
        }
        return values()[ordinal];
    }
}
