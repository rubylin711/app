package com.prime.datastructure.sysdata;

public enum EnModulation
{
    UNDEFINED(0),
    QAM4_NR(0xfe),
    QAM4(0xff),
    QAM16(1),
    QAM32(2),
    QAM64(3),
    QAM128(4),
    QAM256(5),
    QAM512(0x105),
    QAM640(0x106),
    QAM768(0x107),
    QAM896(0x108),
    QAM1024(0x109),
    QPSK(6),
    BPSK(7),
    OQPSK(0x301),
    _8VSB(0x302),
    _16VSB(0x303);

    private int mIndex = 0;

    EnModulation(int nIndex)
    {
        mIndex = nIndex;
    }

    public int getValue()
    {
        return mIndex;
    }

    public static EnModulation valueOf(int value)
    {
        EnModulation ret = UNDEFINED;

        if (value == QAM4_NR.getValue())
        {
            ret = QAM4_NR;
        }
        else if (value == QAM4.getValue())
        {
            ret = QAM4;
        }
        else if (value == QAM16.getValue())
        {
            ret = QAM16;
        }
        else if (value == QAM32.getValue())
        {
            ret = QAM32;
        }
        else if (value == QAM64.getValue())
        {
            ret = QAM64;
        }
        else if (value == QAM128.getValue())
        {
            ret = QAM128;
        }
        else if (value == QAM256.getValue())
        {
            ret = QAM256;
        }
        else if (value == QAM512.getValue())
        {
            ret = QAM512;
        }
        else if (value == QAM640.getValue())
        {
            ret = QAM640;
        }
        else if (value == QAM768.getValue())
        {
            ret = QAM768;
        }
        else if (value == QAM896.getValue())
        {
            ret = QAM896;
        }
        else if (value == QAM1024.getValue())
        {
            ret = QAM1024;
        }
        else if (value == QPSK.getValue())
        {
            ret = QPSK;
        }
        else if (value == BPSK.getValue())
        {
            ret = BPSK;
        }
        else if (value == OQPSK.getValue())
        {
            ret = OQPSK;
        }
        else if (value == _8VSB.getValue())
        {
            ret = _8VSB;
        }
        else if (value == _16VSB.getValue())
        {
            ret = _16VSB;
        }
        else
        {
            ret = UNDEFINED;
        }
        return ret;
    }
}
