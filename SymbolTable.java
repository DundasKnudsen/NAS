
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a mapping of strings to integers starting from zero.
 */
class SymbolTable {

    /**
     * Underlying hash table mapping strings to integers.
     */
    final Map<String, Integer> map;

    /**
     * Underlying hash table mapping strings to integers.
     */
    final List<Symbol> symbols;

    /**
     * Creates an empty symbol index.
     */
    SymbolTable() {
        this.map = new HashMap<String, Integer>();
        this.symbols = new ArrayList<Symbol>();
    }

    /**
     * Stores the given symbol.
     *
     * @param symbol Symbol to be stored.
     */
    void put(final Symbol symbol) {
        map.put(symbol.name, symbols.size());
        symbols.add(symbol);
    }

    /**
     * Returns the symbol with the given name.
     *
     * @param name Name of symbol.
     */
    Symbol get(final String name) {
        final Integer index = map.get(name);
        if (index == null) {
            return null;
        } else {
            return symbols.get(index);
        }
    }

    /**
     * Relocates the symbols in this table relative the given end of
     * program.
     *
     * @param name Name of symbol.
     */
    void relocate(final int endOfProgram) {
        int address = endOfProgram;

        for (Symbol symbol : symbols) {

            if (symbol.type == SymbolType.WORD) {

                symbol.setAddress(address);
                address += 2 * symbol.values.length;

            } else if (symbol.type == SymbolType.CODE) {

                // padding for 4 byte alignment if needed.
                if (address % 4 != 0) {
                    address += 2;
                }
                symbol.setAddress(address);
                address += 4 * symbol.values.length;
            }
        }
    }

    /**
     * Writes the symbols of this table to the given writer.
     *
     * @param biw Writer of binary objects.
     */
    void print(final BiWriter biw) {

        // This must mirror the addressing traversal.
        // Keep track of alignment.
        int address = 0;
        for (final Symbol symbol : symbols) {

            switch (symbol.type) {
            case WORD:
                for (int n: symbol.values) {
                    biw.printWord(n);
                    address += 2;
                }
                break;

            case CODE:

                // padding for 4 byte alignment
                if (address % 4 != 0) {
                    biw.printWord(0x00);
                    address += 2;
                }

                for (int n: symbol.values) {
                    biw.printCode(n);
                    address += 4;
                }
                break;
            }
        }
    }
}
