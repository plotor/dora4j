package org.zhenchao.dora.ascii;

import org.junit.Test;

/**
 * @author zhenchao.wang 2018-02-07 10:06
 * @version 1.0.0
 */
public class AsciiArtistTest {

    @Test
    public void getAsciiArt() {
        for (final FontType fontType : FontType.values()) {
            String art = AsciiArtist.getAsciiArt("flglet for java", fontType);
            System.out.println(fontType.getName() + "\n" + art);
        }
    }

    @Test
    public void getAsciiArt2() {
        String art = AsciiArtist.getAsciiArt("success", FontType.ALLIGATOR);
        System.out.println(art);
    }

    @Test
    public void getRandomAsciiArt() {
        System.out.println(AsciiArtist.getAsciiArt("success"));
    }

}