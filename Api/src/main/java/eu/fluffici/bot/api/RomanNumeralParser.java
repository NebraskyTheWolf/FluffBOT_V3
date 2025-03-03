/*
---------------------------------------------------------------------------------
File Name : RomanNumeralParser

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 17/07/2024
Last Modified : 17/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RomanNumeralParser {
    private static final Map<String, Integer> romanToNumber;

    static {
        romanToNumber = new HashMap<>();
        romanToNumber.put("I", 1);
        romanToNumber.put("IV", 4);
        romanToNumber.put("V", 5);
        romanToNumber.put("IX", 9);
        romanToNumber.put("X", 10);
        romanToNumber.put("XL", 40);
        romanToNumber.put("L", 50);
        romanToNumber.put("XC", 90);
        romanToNumber.put("C", 100);
        romanToNumber.put("CD", 400);
        romanToNumber.put("D", 500);
        romanToNumber.put("CM", 900);
        romanToNumber.put("M", 1000);
    }

    /**
     * Parses a Roman numeral and converts it into an integer value.
     *
     * @param roman the input Roman numeral to be parsed (as a String)
     * @return an integer representing the parsed Roman numeral
     */
    public static int parseRomanNumeral(@NotNull String roman) {
        int result = 0;
        for (int i = 0; i < roman.length(); ) {
            if (i + 1 < roman.length() && romanToNumber.containsKey(roman.substring(i, i + 2))) {
                result += romanToNumber.get(roman.substring(i, i + 2));
                i += 2;
            } else {
                result += romanToNumber.get(roman.substring(i, i + 1));
                i++;
            }
        }
        return result;
    }
}