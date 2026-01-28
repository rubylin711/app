package com.prime.launcher.teletextservice.fullpageteletext;

import java.util.ArrayList;

public class FttxPacket26 {

    private class Substitution {
        int column;
        int row;
        char substitute;
    }

    private ArrayList<Substitution> mSubstitutions;

    public FttxPacket26() {
        mSubstitutions = new ArrayList<>();
    }

    public void clear() {
        mSubstitutions.clear();
    }

    private void addSubstitution(int column, int row, int substitute) {
        Substitution substitution = new Substitution();
        substitution.column = column;
        substitution.row = row;
        substitution.substitute = (char) substitute;
        mSubstitutions.add(substitution);
    }

    public void parse(int dataBytePos, String[] items, FttxPage page) {
        FttxGXCharset charset = page.getCharset();

        // read triplets
        int row = 0;
        int column;
        boolean finished = false;
        for (int i = 0; i < 13; ++i) {
            int triplet = readTripletInfo(items, dataBytePos);
            dataBytePos = dataBytePos + 3;

            int address = (triplet >> 16) & 0xFF;
            int mode = (triplet >> 8) & 0xFF;
            int data = triplet & 0xFF;

            // see ETSI 300706, section 12.3, table 27
            if (address >= 40 && address <= 63) {
                switch (mode) {
                    case 0x7: // 00111 - Address Display Row 0
                        row = 0;
                        break;
                    case 0x4: // 00100 - Set Active Position
                        if (address == 40) {
                            row = 24;
                        } else {
                            row = address - 40;
                        }
                        break;
                    case 0x1F: // termination marker, no need to go further
                        finished = true;
                        break;
                    default:
                        break;
                }
            } else if (address >= 0 && address <= 39) {
                switch (mode) {
                    case 0x1: // 00001 - Full Row Colour
                        // column = 0;
                        break;
                    case 0x4: // 00100 - Set Active Position
                        // column = address;
                        break;
                    case 0xF: // 01111 - Character from G2 set
                        column = address;
                        addSubstitution(column, row, charset.getG2Char(data));
                        break;
                    case 0x10: // 10000 - G0 Character without diacritical mark
                        column = address;
                        addSubstitution(column, row, charset.getG0LatinChar(data));
                        break;
                    default:
                        if (mode >= 0x11 && mode <= 0x1F) {
                            // G0 Character with diacritical mark
                            column = address;
                            addSubstitution(column, row,
                                    charset.getG0DiacriticalChar(data, mode & 0xF));
                        }
                        break;
                }
            }
            if (finished) {
                break;
            }
        }
    }

    public void apply(FttxPage page) {
        for (Substitution substitution : mSubstitutions) {
            page.setCharAt(substitution.column, substitution.row, substitution.substitute);
        }
    }

    int readTripletInfo(String[] items, int dataBytePos) {
        int part1 = Integer.parseInt(items[dataBytePos]);
        int part2 = Integer.parseInt(items[dataBytePos + 1]);
        int part3 = Integer.parseInt(items[dataBytePos + 2]);

        int address = 0;
        address |= (part1 & 0x20) >> 5;
        address |= (part1 & 0x08) >> 2;
        address |= (part1 & 0x04);
        address |= (part1 & 0x02) << 2;
        address |= (part2 & 0x80) >> 3;
        address |= (part2 & 0x40) >> 1;

        int mode = 0;
        mode |= (part2 & 0x20) >> 5;
        mode |= (part2 & 0x10) >> 3;
        mode |= (part2 & 0x08) >> 1;
        mode |= (part2 & 0x04) << 1;
        mode |= (part2 & 0x02) << 3;

        int data = 0;
        data |= (part3 & 0x80) >> 7;
        data |= (part3 & 0x40) >> 5;
        data |= (part3 & 0x20) >> 3;
        data |= (part3 & 0x10) >> 1;
        data |= (part3 & 0x08) << 1;
        data |= (part3 & 0x04) << 3;
        data |= (part3 & 0x02) << 5;

        return ((address << 16 & 0x00FF0000) |
                (mode << 8 & 0x0000FF00) |
                (data & 0x000000FF));
    }
}