package org.zhenchao.dora.ascii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

/**
 * @author zhenchao.wang 2018-02-07 09:54
 * @version 1.0.0
 */
public class AsciiArtist {

    private static final Logger log = LoggerFactory.getLogger(AsciiArtist.class);

    public static String getAsciiArt(String word) {
        FontType[] types = FontType.values();
        Random random = new Random(System.currentTimeMillis());
        int idx = random.nextInt(types.length);
        return getAsciiArt(word, types[idx]);
    }

    public static String getAsciiArt(String word, FontType fontType) {
        try {
            FigletFont ff = new FigletFont(fontType);
            return ff.convert(word);
        } catch (IOException e) {
            log.error("Create figlet font instance exception, fontType[{}]", fontType, e);
        }
        return "";
    }

}
