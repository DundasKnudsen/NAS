
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.io.IOException;
import se.kth.csc.nic.Computer;

/**
 * Wrapper of Nilsson Instructional Computer (NIC) for use with the
 * Kattis {@link https://kth.kattis.com/} system.
 *
 * @author Douglas Wikstrom
 */
public class Kattis {

    final static String PROGRAM_TEMPLATE =
  "public class Kattis {\n"
+ "\n"
+ "    /**\n"
+ "     * Reads each line of standard in, executes the program, and write\n"
+ "     * the result on standard out.\n"
+ "     */\n"
+ "    public static String main(final String[] args) {\n"
+ "        final Computer computer = new Computer(PROGRAM_PLACE_HOLDER);\n"
+ "        final String executable =\n"
+ "            NAS.assemble(null, PROGRAM, NAS.MAX_ERRORS, errorStream);\n"
+ "        computer.setProgram(executable);\n"
+ "    }\n"
+ "}";
}