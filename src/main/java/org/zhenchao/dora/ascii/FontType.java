package org.zhenchao.dora.ascii;

/**
 * @author zhenchao.wang 2018-02-07 09:14
 * @version 1.0.0
 */
public enum FontType {

    THREE_D("3-d"),
    THREE_X5("3x5"),
    FIVE_LINEOBLIQUE("5lineoblique"),
    ACROBATIC("acrobatic"),
    ALLIGATOR("alligator"),
    ALLIGATOR2("alligator2"),
    ALPHABET("alphabet"),
    AVATAR("avatar"),
    /*BANNER("banner"),*/
    BANNER3_D("banner3-D"),
    BANNER3("banner3"),
    BANNER4("banner4"),
    BARBWIRE("barbwire"),
    BASIC("basic"),
    BELL("bell"),
    /*BIG("big"),*/
    BIGCHIEF("bigchief"),
    /*BINARY("binary"),*/
    BLOCK("block"),
    BUBBLE("bubble"),
    BULBHEAD("bulbhead"),
    CALGPHY2("calgphy2"),
    CALIGRAPHY("caligraphy"),
    CATWALK("catwalk"),
    CHUNKY("chunky"),
    COINSTAK("coinstak"),
    COLOSSAL("colossal"),
    COMPUTER("computer"),
    CONTESSA("contessa"),
    CONTRAST("contrast"),
    COSMIC("cosmic"),
    COSMIKE("cosmike"),
    CRICKET("cricket"),
    CURSIVE("cursive"),
    CYBERLARGE("cyberlarge"),
    CYBERMEDIUM("cybermedium"),
    CYBERSMALL("cybersmall"),
    DIAMOND("diamond"),
    DIGITAL("digital"),
    DOH("doh"),
    DOOM("doom"),
    DOTMATRIX("dotmatrix"),
    DRPEPPER("drpepper"),
    EFTICHESS("eftichess"),
    EFTIFONT("eftifont"),
    /*EFTIPITI("eftipiti"),*/
    EFTIROBOT("eftirobot"),
    EFTITALIC("eftitalic"),
    EFTIWALL("eftiwall"),
    EFTIWATER("eftiwater"),
    EPIC("epic"),
    FENDER("fender"),
    FOURTOPS("fourtops"),
    FUZZY("fuzzy"),
    GOOFY("goofy"),
    GOTHIC("gothic"),
    GRAFFITI("graffiti"),
    HOLLYWOOD("hollywood"),
    INVITA("invita"),
    ISOMETRIC1("isometric1"),
    ISOMETRIC2("isometric2"),
    ISOMETRIC3("isometric3"),
    ISOMETRIC4("isometric4"),
    ITALIC("italic"),
    /*IVRIT("ivrit"),*/
    JAZMINE("jazmine"),
    JERUSALEM("jerusalem"),
    KATAKANA("katakana"),
    KBAN("kban"),
    LARRY3D("larry3d"),
    LCD("lcd"),
    LEAN("lean"),
    LETTERS("letters"),
    LINUX("linux"),
    LOCKERGNOME("lockergnome"),
    MADRID("madrid"),
    MARQUEE("marquee"),
    /*MAXFOUR("maxfour"),*/
    MIKE("mike"),
    MINI("mini"),
    MIRROR("mirror"),
    /*MNEMONIC("mnemonic"),*/
    /*MORSE("morse"),*/
    MOSCOW("moscow"),
    NANCYJ_FANCY("nancyj-fancy"),
    NANCYJ_UNDERLINED("nancyj-underlined"),
    NANCYJ("nancyj"),
    NIPPLES("nipples"),
    NTGREEK("ntgreek"),
    O8("o8"),
    OGRE("ogre"),
    PAWP("pawp"),
    PEAKS("peaks"),
    PEBBLES("pebbles"),
    PEPPER("pepper"),
    POISON("poison"),
    PUFFY("puffy"),
    /*PYRAMID("pyramid"),*/
    RECTANGLES("rectangles"),
    RELIEF("relief"),
    RELIEF2("relief2"),
    REV("rev"),
    ROMAN("roman"),
    /*ROT13("rot13"),*/
    ROUNDED("rounded"),
    ROWANCAP("rowancap"),
    ROZZO("rozzo"),
    /*RUNIC("runic"),*/
    RUNYC("runyc"),
    SBLOOD("sblood"),
    SCRIPT("script"),
    SERIFCAP("serifcap"),
    SHADOW("shadow"),
    SHORT("short"),
    SLANT("slant"),
    SLIDE("slide"),
    SLSCRIPT("slscript"),
    SMALL("small"),
    SMISOME1("smisome1"),
    SMKEYBOARD("smkeyboard"),
    SMSCRIPT("smscript"),
    SMSHADOW("smshadow"),
    SMSLANT("smslant"),
    SMTENGWAR("smtengwar"),
    SPEED("speed"),
    STAMPATELLO("stampatello"),
    STANDARD("standard"),
    STARWARS("starwars"),
    STELLAR("stellar"),
    STOP("stop"),
    STRAIGHT("straight"),
    TANJA("tanja"),
    TENGWAR("tengwar"),
    /*TERM("term"),*/
    THICK("thick"),
    THIN("thin"),
    THREEPOINT("threepoint"),
    TICKS("ticks"),
    TICKSSLANT("ticksslant"),
    TINKER_TOY("tinker-toy"),
    TOMBSTONE("tombstone"),
    TREK("trek"),
    TSALAGI("tsalagi"),
    TWOPOINT("twopoint"),
    UNIVERS("univers"),
    USAFLAG("usaflag"),
    /*WAVY("wavy"),*/
    WEIRD("weird");

    private String name;

    FontType(String name) {
        this.name = name;
    }

    public String filename() {
        return this.getName() + ".flf";
    }

    public String getName() {
        return name;
    }
}
