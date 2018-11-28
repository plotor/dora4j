package org.zhenchao.dora.ascii;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * @author zhenchao.wang 2018-02-07 09:28
 * @version 1.0.0
 */
public class FigletFont {

    private final static int MAX_CHARS = 1024;
    private final static int REGULAR_CHARS = 102;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private char hardBlank;
    private int height;
    private int heightWithoutDescenders;
    private int maxLine;
    private int smushMode;
    private char font[][][];
    private String fontName = "";

    /**
     * Returns all character from this Font. Each character is defined as
     * char[][]. So the whole font is a char[][][].
     *
     * @return The representation of all characters.
     */
    public char[][][] getFont() {
        return font;
    }

    /**
     * Return a single character represented as char[][].
     *
     * @param c The numerical id of the character.
     * @return The definition of a single character.
     */
    public char[][] getChar(int c) {
        return font[c];
    }

    /**
     * Selects a single line from a character.
     *
     * @param c Character id
     * @param l Line number
     * @return The selected line from the character
     */
    public String getCharLineString(int c, int l) {
        if (font[c][l] == null) {
            return null;
        } else {
            return new String(font[c][l]);
        }
    }

    public FigletFont(FontType fontType) throws IOException {
        this(FigletFont.class.getClassLoader().getResourceAsStream("flf/" + fontType.filename()));
    }

    public FigletFont(InputStream stream) throws IOException {
        font = new char[MAX_CHARS][][];
        BufferedReader data = null;
        try {
            data = new BufferedReader(
                    new InputStreamReader(new BufferedInputStream(stream), "UTF-8"));

            String dummyS = data.readLine();
            StringTokenizer st = new StringTokenizer(dummyS, " ");
            String s = st.nextToken();
            hardBlank = s.charAt(s.length() - 1);
            height = Integer.parseInt(st.nextToken());
            heightWithoutDescenders = Integer.parseInt(st.nextToken());
            maxLine = Integer.parseInt(st.nextToken());
            smushMode = Integer.parseInt(st.nextToken());
            int dummyI = Integer.parseInt(st.nextToken());

            /*
             * try to read the font name as the first word of the first comment
             * line, but this is not standardized !
             */
            if (dummyI > 0) {
                st = new StringTokenizer(data.readLine(), " ");
                if (st.hasMoreElements()) {
                    fontName = st.nextToken();
                }
            }

            int[] charsTo = new int[REGULAR_CHARS];

            int j = 0;
            for (int c = 32; c <= 126; ++c) {
                charsTo[j++] = c;
            }
            for (int additional : new int[] {196, 214, 220, 228, 246, 252, 223}) {
                charsTo[j++] = additional;
            }

            // skip the comments
            for (int i = 0; i < dummyI - 1; i++) {
                dummyS = data.readLine();
            }

            int charPos = 0;
            int charCode;
            while (dummyS != null) {  // for all the characters
                if (charPos < REGULAR_CHARS) {
                    charCode = charsTo[charPos++];
                } else {
                    dummyS = data.readLine();
                    if (dummyS == null) {
                        continue;
                    }
                    charCode = this.convertCharCode(dummyS);
                }
                for (int h = 0; h < height; h++) {
                    dummyS = data.readLine();
                    if (dummyS != null) {
                        if (h == 0) {
                            font[charCode] = new char[height][];
                        }
                        int t = dummyS.length() - 1 - ((h == height - 1) ? 1 : 0);
                        if (height == 1) {
                            t++;
                        }
                        font[charCode][h] = new char[t];
                        for (int l = 0; l < t; l++) {
                            char a = dummyS.charAt(l);
                            font[charCode][h][l] = (a == hardBlank) ? ' ' : a;
                        }
                    }
                }
            }
        } finally {
            if (data != null) {
                data.close();
            }
        }
    }

    public int convertCharCode(String input) {
        String codeTag = input.concat(" ").split(" ")[0];
        if (codeTag.matches("^0[xX][0-9a-fA-F]+$")) {
            return Integer.parseInt(codeTag.substring(2), 16);
        } else if (codeTag.matches("^0[0-7]+$")) {
            return Integer.parseInt(codeTag.substring(1), 8);
        } else {
            return Integer.parseInt(codeTag);
        }
    }

    public String convert(String message) {
        StringBuilder result = new StringBuilder();
        // for each line
        for (int l = 0; l < this.height; l++) {
            // for each char
            for (int c = 0; c < message.length(); c++) {
                result.append(this.getCharLineString((int) message.charAt(c), l));
            }
            result.append(LINE_SEPARATOR);
        }
        return result.toString();
    }

    public static String convertOneLine(String fontPath, String message) throws IOException {
        InputStream fontStream;
        if (fontPath.startsWith("classpath:")) {
            fontStream = FigletFont.class.getResourceAsStream(fontPath.substring(10));
        } else if (fontPath.startsWith("http://") || fontPath.startsWith("https://")) {
            fontStream = new URL(fontPath).openStream();
        } else {
            fontStream = new FileInputStream(fontPath);
        }
        return convertOneLine(fontStream, message);
    }

    public static String convertOneLine(String message) throws IOException {
        return convertOneLine(FigletFont.class.getClassLoader().getResourceAsStream("flf/" + FontType.STANDARD.filename()), message);
    }

    public static String convertOneLine(File fontFile, String message) throws IOException {
        return convertOneLine(new FileInputStream(fontFile), message);
    }

    public static String convertOneLine(InputStream fontFileStream, String message) throws IOException {
        return new FigletFont(fontFileStream).convert(message);
    }

    /* getter & setter */

    public char getHardBlank() {
        return hardBlank;
    }

    public int getHeight() {
        return height;
    }

    public int getHeightWithoutDescenders() {
        return heightWithoutDescenders;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public int getSmushMode() {
        return smushMode;
    }

    public String getFontName() {
        return fontName;
    }
}
