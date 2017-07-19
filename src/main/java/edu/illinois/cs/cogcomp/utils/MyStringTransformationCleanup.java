package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by haowu4 on 7/18/17.
 */
public class MyStringTransformationCleanup {
    private static final String LATIN1 = "ISO-8859-1";

    /**
     * tries to normalize string to specified encoding. The number of characters returned should be
     * the same, and tokens should remain contiguous in the output; non-recognized characters will
     * be substituted for *something*.
     */
    static public StringTransformation normalizeToEncoding(StringTransformation stringTransformation, Charset encoding) {

        String startStr = stringTransformation.getTransformedText();

        CharsetEncoder encoder = encoding.newEncoder();

        if (!encoder.canEncode(startStr)) {
            final int length = startStr.length();

            int charNum = 0;

            for (int offset = 0; offset < length; ) {
                // do something with the codepoint
                BooleanIntPair replacement =
                        normalizeCharacter(startStr, encoding, offset);

                Character replacedChar = (char) (replacement.getSecond());

                if (null != replacedChar) {

                    stringTransformation.transformString(charNum, charNum + 1, String.valueOf(replacedChar));
                    charNum++;
                }
                offset += Character.charCount(replacedChar);
            }
        }

        return stringTransformation;
    }


    public static class BooleanIntPair {
        boolean first;
        int second;

        public BooleanIntPair(boolean first, int second) {
            this.first = first;
            this.second = second;
        }

        public boolean getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }
    }

    /**
     * substitute based on types: for a character encoding not in range, replace punctuation with
     * generic punctuation whitespace with whitespace letter with letter number with number currency
     * symbol with currency
     */
    private static BooleanIntPair normalizeCharacter(String origString,
                                                     Charset encoding, int offset) {

        char normalizedChar = ' ';

        final int codepoint = origString.codePointAt(offset);

        boolean isOk = checkIsInEncoding(codepoint, encoding);

        if (isOk) {
            normalizedChar = (char) codepoint;
        } else {
            Pair<Boolean, Character> charInfo = fixCharByType(codepoint);
            normalizedChar = charInfo.getSecond();
            isOk = charInfo.getFirst();
        }

        Character newChar = null;
        if (isOk)
            newChar = normalizedChar;

        return new BooleanIntPair(isOk, newChar);
    }


    private static boolean checkIsLatin(int codepoint) {
        return checkIsInEncoding(codepoint, Charset.forName(LATIN1));
    }


    private static boolean checkIsInEncoding(int codepoint, Charset encoding) {

        boolean isOk = false;

        if (encoding.equals(Charset.forName("US-ASCII"))) {
            if (codepoint < 128)
                isOk = true;
        } else if (encoding.equals(Charset.forName(LATIN1))) // latin1
        {
            if (codepoint < 256)
                isOk = true;
        } else if (encoding.equals(Charset.forName("UTF-8"))) {
            if (codepoint < 1114111)
                isOk = true;
        }

        return isOk;
    }


    /**
     * Attempt to replace an out-of-charset character with something appropriate, so that the resulting
     * string a) makes sense and b) resembles the original. Otherwise, use whitespace.
     *
     * @param codepoint codepoint of character to change
     * @return flag indicating whether a substitution was found, and the substituted character.
     */
    public static Pair<Boolean, Character> fixCharByType(int codepoint) {

        final int type = Character.getType(codepoint);

        char normalizedChar = ' ';

        boolean isOk = true;

        if (type == Character.CURRENCY_SYMBOL)
            normalizedChar = '$';
        else if (type == Character.DASH_PUNCTUATION)
            normalizedChar = '-';
        else if (type == Character.FINAL_QUOTE_PUNCTUATION) {
            normalizedChar = '"';
        } else if (type == Character.INITIAL_QUOTE_PUNCTUATION) {
            normalizedChar = '"';
        } else if (type == Character.END_PUNCTUATION)
            normalizedChar = '.';
        else if (type == Character.DASH_PUNCTUATION)
            normalizedChar = '-';
        else if (type == Character.OTHER_LETTER)
            normalizedChar = 'a';
        else if (type == Character.OTHER_NUMBER)
            normalizedChar = '0';
        else if (type == Character.OTHER_PUNCTUATION)
            normalizedChar = '-';
        else if (type == Character.OTHER_SYMBOL)
            normalizedChar = ' ';
        else
            isOk = false;

        return new Pair(isOk, normalizedChar);
    }


    static public StringTransformation removeDiacritics(StringTransformation origStringSt) {
        char[] startChars = origStringSt.getTransformedText().toCharArray();
        for (int i = 0; i < startChars.length; ++i) {
            int c = Character.codePointAt(startChars, i);
            if (checkIsLatin(c))
                continue;
            else {
                String newC = StringUtils.normalizeUnicodeDiacriticChar(startChars[i]);
                origStringSt.transformString(i, i + 1, newC);
            }
        }
        origStringSt.getTransformedText(); // applies edits
        return origStringSt;
    }


    static public StringTransformation normalizeToLatin1(StringTransformation origStringSt) {
        return normalizeToEncoding(origStringSt, Charset.forName("ISO-8859-1"));
    }


    static public StringTransformation normalizeToAscii(StringTransformation origStringSt) {
        StringTransformation latin1St = normalizeToLatin1(origStringSt);
        return normalizeToEncoding(latin1St, Charset.forName("ascii"));
    }


}
