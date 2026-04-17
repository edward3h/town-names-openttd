package red.ethel.townnames.builtin.data;

public final class CzechData {
    private CzechData() {}

    public static final String[] REAL = {
        "A\u0161",
        "Bene\u0161ov",
        "Beroun",
        "Bezdru\u017eice",
        "Blansko",
        "B\u0159eclav",
        "Brno",
        "Brunt\u00e1l",
        "\u010cesk\u00e1 L\u00edpa",
        "\u010cesk\u00e9 Bud\u011bjovice",
        "\u010cesk\u00fd Krumlov",
        "D\u011b\u010d\u00edn",
        "Doma\u017elice",
        "Dub\u00ed",
        "Fr\u00fddek-M\u00edstek",
        "Havl\u00ed\u010dk\u016fv Brod",
        "Hodon\u00edn",
        "Hradec Kr\u00e1lov\u00e9",
        "Humpolec",
        "Cheb",
        "Chomutov",
        "Chrudim",
        "Jablonec nad Nisou",
        "Jesen\u00edk",
        "Ji\u010d\u00edn",
        "Jihlava",
        "Jind\u0159ich\u016fv Hradec",
        "Karlovy Vary",
        "Karvin\u00e1",
        "Kladno",
        "Klatovy",
        "Kol\u00edn",
        "Kosmonosy",
        "Krom\u011b\u0159\u00ed\u017e",
        "Kutn\u00e1 Hora",
        "Liberec",
        "Litom\u011b\u0159ice",
        "Louny",
        "Man\u011bt\u00edn",
        "M\u011bln\u00edk",
        "Mlad\u00e1 Boleslav",
        "Most",
        "N\u00e1chod",
        "Nov\u00fd Ji\u010d\u00edn",
        "Nymburk",
        "Olomouc",
        "Opava",
        "Or\u00e1\u010dov",
        "Ostrava",
        "Pardubice",
        "Pelh\u0159imov",
        "Pol\u017eice",
        "P\u00edsek",
        "Plze\u0148",
        "Praha",
        "Prachatice",
        "P\u0159erov",
        "P\u0159\u00edbram",
        "Prost\u011bjov",
        "Rakovn\u00edk",
        "Rokycany",
        "Rudn\u00e1",
        "Rychnov nad Kn\u011b\u017enou",
        "Semily",
        "Sokolov",
        "Strakonice",
        "St\u0159edokluky",
        "\u0160umperk",
        "Svitavy",
        "T\u00e1bor",
        "Tachov",
        "Teplice",
        "T\u0159eb\u00ed\u010d",
        "Trutnov",
        "Uhersk\u00e9 Hradi\u0161t\u011b",
        "\u00dast\u00ed nad Labem",
        "\u00dast\u00ed nad Orlic\u00ed",
        "Vset\u00edn",
        "Vy\u0161kov",
        "\u017d\u010f\u00e1r nad S\u00e1zavou",
        "Zl\u00edn",
        "Znojmo",
    };

    /**
     * Adjective names (base stems, without pattern suffix). Indices match ADJ_PATTERN and ADJ_CHOOSE.
     * Pattern: 0=JARNI, 1=MLADY, 2=PRIVL
     */
    public static final String[] ADJ_NAME = {
        "Horn",
        "Horn",
        "Doln",
        "Doln",
        "P\u0159edn",
        "Zadn",
        "Kosteln",
        "Havran",
        "\u0158\u00ed\u010dn",
        "Jezern",
        "Velk",
        "Velk",
        "Mal",
        "Mal",
        "Vysok",
        "\u010cesk",
        "Moravsk",
        "Slov\u00e1ck",
        "Slezsk",
        "Uhersk",
        "Star",
        "Star",
        "Nov",
        "Nov",
        "Mlad",
        "Kr\u00e1lovsk",
        "Kamenn",
        "Cihlov",
        "Divn",
        "\u010cerven",
        "\u010cerven",
        "\u010cerven",
        "Zelen",
        "\u017dlut",
        "Siv",
        "\u0160ed",
        "B\u00edl",
        "B\u00edl",
        "Modr",
        "R\u016f\u017eov",
        "\u010cern",
        "Kr\u00e1l",
        "Jan",
        "Karl",
        "Kry\u0161tof",
        "Ji\u0159\u00edk",
        "Petr",
        "Sud",
    };

    /** 0=JARNI, 1=MLADY, 2=PRIVL */
    public static final int[] ADJ_PATTERN = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2,
    };

    /**
     * Colour-only flag; true means only use with colour-allowing substatives. CZC_ANY = false
     * (matches any), CzechChooseFlag::Colour = true.
     */
    public static final boolean[] ADJ_COLOUR_ONLY = {
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true,
        true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false,
    };

    /**
     * [gender][pattern] adjective suffixes. gender: 0=SMASC, 1=SFEM, 2=SNEUT, 3=PMASC, 4=PFEM,
     * 5=PNEUT pattern: 0=JARNI, 1=MLADY, 2=PRIVL
     */
    public static final String[][] PATMOD = {
        {"\u00ed", "\u00fd", "uv"}, // SMASC
        {"\u00ed", "\u00e1", "ova"}, // SFEM
        {"\u00ed", "\u00e9", "ovo"}, // SNEUT
        {"\u00ed", "\u00e9", "ovy"}, // PMASC
        {"\u00ed", "\u00e9", "ovy"}, // PFEM
        {"\u00ed", "\u00e1", "ova"}, // PNEUT
    };

    /** Full-name substantives (no stem+ending needed). */
    public static final String[] SUBST_FULL = {
        "Sedlec",
        "Brod",
        "Brod",
        "\u00daval",
        "\u017d\u010f\u00e1r",
        "Smrk",
        "Hora",
        "Lhota",
        "Lhota",
        "Hlava",
        "L\u00edpa",
        "Pole",
        "\u00dadol\u00ed",
        "\u00davaly",
        "Luka",
        "Pole",
    };

    /** Gender for each full substantive (0=SMASC,1=SFEM,2=SNEUT,3=PMASC,4=PFEM,5=PNEUT). */
    public static final int[] SUBST_FULL_GENDER = {
        0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 3, 4, 5,
    };

    /** Whether full substantive allows colour adjectives. */
    public static final boolean[] SUBST_FULL_COLOUR = {
        true, true, true, false, true, true, true, true, true, true, true, true, true, false, true, true,
    };

    /** Stem substantives (combined with postfix + ending). */
    public static final String[] SUBST_STEM = {
        "Kostel",
        "Kl\u00e1\u0161ter",
        "Lhot",
        "Lhot",
        "Hur",
        "Sedl",
        "Hrad",
        "Pras",
        "Ba\u017e",
        "Tes",
        "U\u017e",
        "B\u0159",
        "Vod",
        "Jan",
        "Prach",
        "Kunr",
        "Strak",
        "V\u00edt",
        "Vy\u0161",
        "\u017dat",
        "\u017der",
        "St\u0159ed",
        "Harv",
        "Pruh",
        "Tach",
        "P\u00edsn",
        "Jin",
        "Jes",
        "Jar",
        "Sok",
        "Hod",
        "Net",
        "Pra\u017e",
        "Nerat",
        "Kral",
        "Hut",
        "Pan",
        "Odst\u0159ed",
        "Mrat",
        "Hlav",
        "M\u011b\u0159",
        "Lip",
    };

    /** Optional postfixes inserted between stem and ending. */
    public static final String[] SUBST_POSTFIX = {
        "av", "an", "at",
        "ov", "on", "ot",
        "ev", "en", "et",
    };

    /** Ending names for dynamic substantives. */
    public static final String[] SUBST_ENDING = {
        "ec",
        "\u00edn",
        "ov",
        "kov",
        "\u00edn",
        "n\u00edk",
        "burk",
        "ka",
        "inka",
        "n\u00e1",
        "ava",
        "\u00edky",
        "upy",
        "olupy",
        "avy",
        "ice",
        "i\u010dky",
        "na",
        "no",
        "i\u0161t\u011b",
    };

    /** Suffixes appended after the full name (with a space separator). */
    public static final String[] SUFFIX = {
        "nad Cidlinou",
        "nad Dyj\u00ed",
        "nad Jihlavou",
        "nad Labem",
        "nad Lesy",
        "nad Moravou",
        "nad Nisou",
        "nad Odrou",
        "nad Ostravic\u00ed",
        "nad S\u00e1zavou",
        "nad Vltavou",
        "pod Prad\u011bdem",
        "pod Radho\u0161t\u011bm",
        "pod \u0158\u00edpem",
        "pod Sn\u011b\u017ekou",
        "pod \u0160pi\u010d\u00e1kem",
        "pod Sedlem",
        "v \u010cech\u00e1ch",
        "na Morav\u011b",
    };
}
