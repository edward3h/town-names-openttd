package io.github.edward3h.townnames.builtin;

import io.github.edward3h.townnames.builtin.data.AustrianData;
import io.github.edward3h.townnames.builtin.data.CatalanData;
import io.github.edward3h.townnames.builtin.data.CzechData;
import io.github.edward3h.townnames.builtin.data.DanishData;
import io.github.edward3h.townnames.builtin.data.DutchData;
import io.github.edward3h.townnames.builtin.data.EnglishAdditionalData;
import io.github.edward3h.townnames.builtin.data.EnglishOriginalData;
import io.github.edward3h.townnames.builtin.data.FinnishData;
import io.github.edward3h.townnames.builtin.data.FrenchData;
import io.github.edward3h.townnames.builtin.data.GermanData;
import io.github.edward3h.townnames.builtin.data.HungarianData;
import io.github.edward3h.townnames.builtin.data.ItalianData;
import io.github.edward3h.townnames.builtin.data.NorwegianData;
import io.github.edward3h.townnames.builtin.data.PolishData;
import io.github.edward3h.townnames.builtin.data.RomanianData;
import io.github.edward3h.townnames.builtin.data.SillyData;
import io.github.edward3h.townnames.builtin.data.SlovakData;
import io.github.edward3h.townnames.builtin.data.SpanishData;
import io.github.edward3h.townnames.builtin.data.SwedishData;
import io.github.edward3h.townnames.builtin.data.SwissData;
import io.github.edward3h.townnames.builtin.data.TurkishData;
import io.github.edward3h.townnames.engine.NameSource;
import java.util.Random;

/** Built-in town name generator sets, ported from OpenTTD's C++ implementation. */
public enum BuiltinSet implements NameSource {
  ENGLISH_ORIGINAL {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();
      // optional first segment (bias -50 means roughly 50/N chance to skip)
      int i = rng.nextInt(EnglishOriginalData.PART1.length + 50) - 50;
      if (i >= 0) sb.append(EnglishOriginalData.PART1[i]);

      sb.append(EnglishOriginalData.PART2[rng.nextInt(EnglishOriginalData.PART2.length)]);
      sb.append(EnglishOriginalData.PART3[rng.nextInt(EnglishOriginalData.PART3.length)]);
      sb.append(EnglishOriginalData.PART4[rng.nextInt(EnglishOriginalData.PART4.length)]);
      sb.append(EnglishOriginalData.PART5[rng.nextInt(EnglishOriginalData.PART5.length)]);

      // optional last segment
      i = rng.nextInt(EnglishOriginalData.PART6.length + 60) - 60;
      if (i >= 0) sb.append(EnglishOriginalData.PART6[i]);

      return replaceEnglishWords(sb.toString(), true);
    }
  },

  FRENCH {
    @Override
    public String generate(Random rng) {
      return FrenchData.REAL[rng.nextInt(FrenchData.REAL.length)];
    }
  },

  GERMAN {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();
      int seedDerivative = rng.nextInt(28);

      // optional prefix
      if (seedDerivative == 12 || seedDerivative == 19) {
        sb.append(GermanData.PRE[rng.nextInt(GermanData.PRE.length)]);
      }

      // mandatory middle: real name or constructed
      int total = GermanData.REAL.length + GermanData.PART1.length;
      int idx = rng.nextInt(total);
      if (idx < GermanData.REAL.length) {
        sb.append(GermanData.REAL[idx]);
      } else {
        sb.append(GermanData.PART1[idx - GermanData.REAL.length]);
        sb.append(GermanData.PART2[rng.nextInt(GermanData.PART2.length)]);
      }

      // optional suffix
      if (seedDerivative == 24) {
        int riverTotal = GermanData.RIVER_AN_DER.length + GermanData.RIVER_AM.length;
        int riverIdx = rng.nextInt(riverTotal);
        if (riverIdx < GermanData.RIVER_AN_DER.length) {
          sb.append(GermanData.SUFFIX_AN_DER[0]);
          sb.append(GermanData.RIVER_AN_DER[riverIdx]);
        } else {
          sb.append(GermanData.SUFFIX_AM[0]);
          sb.append(GermanData.RIVER_AM[riverIdx - GermanData.RIVER_AN_DER.length]);
        }
      }

      return sb.toString();
    }
  },

  ENGLISH_ADDITIONAL {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      // optional prefix
      int i = rng.nextInt(EnglishAdditionalData.PREFIX.length + 50) - 50;
      if (i >= 0) sb.append(EnglishAdditionalData.PREFIX[i]);

      if (rng.nextInt(20) >= 14) {
        sb.append(EnglishAdditionalData.PART1A[rng.nextInt(EnglishAdditionalData.PART1A.length)]);
      } else {
        sb.append(EnglishAdditionalData.PART1B1[rng.nextInt(EnglishAdditionalData.PART1B1.length)]);
        sb.append(EnglishAdditionalData.PART1B2[rng.nextInt(EnglishAdditionalData.PART1B2.length)]);
        if (rng.nextInt(20) >= 4) {
          sb.append(
              EnglishAdditionalData.PART1B3A[rng.nextInt(EnglishAdditionalData.PART1B3A.length)]);
        } else {
          sb.append(
              EnglishAdditionalData.PART1B3B[rng.nextInt(EnglishAdditionalData.PART1B3B.length)]);
        }
      }

      sb.append(EnglishAdditionalData.PART2[rng.nextInt(EnglishAdditionalData.PART2.length)]);

      // optional last segment
      i = rng.nextInt(EnglishAdditionalData.PART3.length + 60) - 60;
      if (i >= 0) sb.append(EnglishAdditionalData.PART3[i]);

      return replaceEnglishWords(sb.toString(), false);
    }
  },

  SPANISH {
    @Override
    public String generate(Random rng) {
      return SpanishData.REAL[rng.nextInt(SpanishData.REAL.length)];
    }
  },

  SILLY {
    @Override
    public String generate(Random rng) {
      return SillyData.PART1[rng.nextInt(SillyData.PART1.length)]
          + SillyData.PART2[rng.nextInt(SillyData.PART2.length)];
    }
  },

  SWEDISH {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      // optional first segment
      int i = rng.nextInt(SwedishData.PART1.length + 50) - 50;
      if (i >= 0) sb.append(SwedishData.PART1[i]);

      // middle: hardcoded or constructed
      if (rng.nextInt(5) >= 3) {
        sb.append(SwedishData.PART2[rng.nextInt(SwedishData.PART2.length)]);
      } else {
        sb.append(SwedishData.PART2A[rng.nextInt(SwedishData.PART2A.length)]);
        sb.append(SwedishData.PART2B[rng.nextInt(SwedishData.PART2B.length)]);
        sb.append(SwedishData.PART2C[rng.nextInt(SwedishData.PART2C.length)]);
      }

      sb.append(SwedishData.PART3[rng.nextInt(SwedishData.PART3.length)]);
      return sb.toString();
    }
  },

  DUTCH {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      // optional first segment
      int i = rng.nextInt(DutchData.PART1.length + 50) - 50;
      if (i >= 0) sb.append(DutchData.PART1[i]);

      // middle
      if (rng.nextInt(9) > 4) {
        sb.append(DutchData.PART2[rng.nextInt(DutchData.PART2.length)]);
      } else {
        sb.append(DutchData.PART3[rng.nextInt(DutchData.PART3.length)]);
        sb.append(DutchData.PART4[rng.nextInt(DutchData.PART4.length)]);
      }

      sb.append(DutchData.PART5[rng.nextInt(DutchData.PART5.length)]);
      return sb.toString();
    }
  },

  FINNISH {
    @Override
    public String generate(Random rng) {
      // 10/15 chance for a real name
      if (rng.nextInt(15) >= 10) {
        return FinnishData.REAL[rng.nextInt(FinnishData.REAL.length)];
      }

      StringBuilder sb = new StringBuilder();

      if (rng.nextInt(15) >= 5) {
        // two-part with "la"/"lä"
        String stem = FinnishData.PART1[rng.nextInt(FinnishData.PART1.length)];
        if (stem.endsWith("i")) {
          stem = stem.substring(0, stem.length() - 1) + "e";
        }
        sb.append(stem);
        // check for back vowels
        if (containsBackVowel(stem)) {
          sb.append("la");
        } else {
          sb.append("l\u00e4");
        }
        return sb.toString();
      }

      // two-part: PART1 or PART2 + PART3
      int total = FinnishData.PART1.length + FinnishData.PART2.length;
      int sel = rng.nextInt(total);
      if (sel >= FinnishData.PART1.length) {
        sb.append(FinnishData.PART2[sel - FinnishData.PART1.length]);
      } else {
        sb.append(FinnishData.PART1[sel]);
      }
      sb.append(FinnishData.PART3[rng.nextInt(FinnishData.PART3.length)]);
      return sb.toString();
    }
  },

  POLISH {
    @Override
    public String generate(Random rng) {
      int totalStems =
          PolishData.REAL.length
              + PolishData.STEM_M.length
              + PolishData.STEM_F.length
              + PolishData.STEM_N.length;
      int i = rng.nextInt(totalStems);
      int j = rng.nextInt(20);

      // "real" (other) names
      if (i < PolishData.REAL.length) {
        return PolishData.REAL[rng.nextInt(PolishData.REAL.length)];
      }

      StringBuilder sb = new StringBuilder();

      if (i < PolishData.STEM_M.length + PolishData.REAL.length) {
        // masculine
        if (j < 4) sb.append(PolishData.PREFIX_M[rng.nextInt(PolishData.PREFIX_M.length)]);
        sb.append(PolishData.STEM_M[rng.nextInt(PolishData.STEM_M.length)]);
        if (j >= 4 && j < 16)
          sb.append(PolishData.SUFFIX_M[rng.nextInt(PolishData.SUFFIX_M.length)]);
        return sb.toString();
      }

      if (i < PolishData.STEM_F.length + PolishData.STEM_M.length + PolishData.REAL.length) {
        // feminine
        if (j < 4) sb.append(PolishData.PREFIX_F[rng.nextInt(PolishData.PREFIX_F.length)]);
        sb.append(PolishData.STEM_F[rng.nextInt(PolishData.STEM_F.length)]);
        if (j >= 4 && j < 16)
          sb.append(PolishData.SUFFIX_F[rng.nextInt(PolishData.SUFFIX_F.length)]);
        return sb.toString();
      }

      // neuter
      if (j < 4) sb.append(PolishData.PREFIX_N[rng.nextInt(PolishData.PREFIX_N.length)]);
      sb.append(PolishData.STEM_N[rng.nextInt(PolishData.STEM_N.length)]);
      if (j >= 4 && j < 16) sb.append(PolishData.SUFFIX_N[rng.nextInt(PolishData.SUFFIX_N.length)]);
      return sb.toString();
    }
  },

  SLOVAK {
    @Override
    public String generate(Random rng) {
      return SlovakData.REAL[rng.nextInt(SlovakData.REAL.length)];
    }
  },

  NORWEGIAN {
    @Override
    public String generate(Random rng) {
      if (rng.nextInt(15) < 3) {
        return NorwegianData.REAL[rng.nextInt(NorwegianData.REAL.length)];
      }
      return NorwegianData.PART1[rng.nextInt(NorwegianData.PART1.length)]
          + NorwegianData.PART2[rng.nextInt(NorwegianData.PART2.length)];
    }
  },

  HUNGARIAN {
    @Override
    public String generate(Random rng) {
      if (rng.nextInt(15) < 3) {
        return HungarianData.REAL[rng.nextInt(HungarianData.REAL.length)];
      }

      StringBuilder sb = new StringBuilder();

      // optional first segment (1/3 chance)
      int i = rng.nextInt(HungarianData.PART1.length * 3);
      if (i < HungarianData.PART1.length) sb.append(HungarianData.PART1[i]);

      sb.append(HungarianData.PART2[rng.nextInt(HungarianData.PART2.length)]);
      sb.append(HungarianData.PART3[rng.nextInt(HungarianData.PART3.length)]);

      // optional last segment (1/3 chance)
      i = rng.nextInt(HungarianData.PART4.length * 3);
      if (i < HungarianData.PART4.length) sb.append(HungarianData.PART4[i]);

      return sb.toString();
    }
  },

  AUSTRIAN {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      // optional prefix (bias -15)
      int i = rng.nextInt(AustrianData.A1.length + 15) - 15;
      if (i >= 0) sb.append(AustrianData.A1[i]);

      int j = 0;
      int choice = rng.nextInt(6);
      if (choice >= 4) {
        // Kaisers-kirchen style
        sb.append(AustrianData.A2[rng.nextInt(AustrianData.A2.length)]);
        sb.append(AustrianData.A3[rng.nextInt(AustrianData.A3.length)]);
      } else if (choice >= 2) {
        // St. Johann style
        sb.append(AustrianData.A5[rng.nextInt(AustrianData.A5.length)]);
        sb.append(AustrianData.A6[rng.nextInt(AustrianData.A6.length)]);
        j = 1;
      } else {
        // Zell style
        sb.append(AustrianData.A4[rng.nextInt(AustrianData.A4.length)]);
      }

      int suffix = rng.nextInt(6);
      if (suffix >= 4 - j) {
        // an der (rivers)
        sb.append(AustrianData.F1[rng.nextInt(AustrianData.F1.length)]);
        sb.append(AustrianData.F2[rng.nextInt(AustrianData.F2.length)]);
      } else if (suffix >= 2 - j) {
        // am (mountains)
        sb.append(AustrianData.B1[0]);
        sb.append(AustrianData.B2[rng.nextInt(AustrianData.B2.length)]);
      }

      return sb.toString();
    }
  },

  ROMANIAN {
    @Override
    public String generate(Random rng) {
      return RomanianData.REAL[rng.nextInt(RomanianData.REAL.length)];
    }
  },

  CZECH {
    @Override
    public String generate(Random rng) {
      // 1:3 chance to use a real name
      if (rng.nextInt(4) == 0) {
        return CzechData.REAL[rng.nextInt(CzechData.REAL.length)];
      }

      StringBuilder sb = new StringBuilder();

      // Probability of prefixes/suffixes: 0..11 prefix, 12..13 prefix+suffix, 14..17 suffix, 18..31
      // nothing
      int probTails = rng.nextInt(32);
      boolean doPrefix = probTails < 12;
      boolean doSuffix = probTails > 11 && probTails < 17;

      int prefixIdx = doPrefix ? rng.nextInt(CzechData.ADJ_NAME.length) : -1;
      int suffixIdx = doSuffix ? rng.nextInt(CzechData.SUFFIX.length) : -1;

      // Choose substantive: 1:3 full vs dynamic
      int stemTotal = CzechData.SUBST_FULL.length + 3 * CzechData.SUBST_STEM.length;
      int stem = rng.nextInt(stemTotal);

      int gender;
      boolean colourAllowed;
      String substantiveName;

      if (stem < CzechData.SUBST_FULL.length) {
        // full substantive
        gender = CzechData.SUBST_FULL_GENDER[stem];
        colourAllowed = CzechData.SUBST_FULL_COLOUR[stem];
        substantiveName = CzechData.SUBST_FULL[stem];
      } else {
        // dynamic substantive: stem + optional postfix + ending
        int stemIdx = (stem - CzechData.SUBST_FULL.length) % CzechData.SUBST_STEM.length;
        String stemStr = CzechData.SUBST_STEM[stemIdx];

        // Use a random ending
        int endingIdx = rng.nextInt(CzechData.SUBST_ENDING.length);
        String ending = CzechData.SUBST_ENDING[endingIdx];

        // optional postfix (1:2 chance)
        int postfixChoice = rng.nextInt(CzechData.SUBST_POSTFIX.length * 2);
        String postfix =
            postfixChoice < CzechData.SUBST_POSTFIX.length
                ? CzechData.SUBST_POSTFIX[postfixChoice]
                : "";

        // Avoid "avava" and "Jananna"-like double-vowel duplications
        if (!postfix.isEmpty()) {
          if (postfix.charAt(1) != ending.charAt(0)) {
            stemStr = stemStr + postfix;
          }
        }
        substantiveName = stemStr + ending;

        // derive gender from ending index
        if (endingIdx < 7) gender = 0; // SMASC
        else if (endingIdx < 11) gender = 1; // SFEM
        else if (endingIdx < 14) gender = 3; // PMASC
        else if (endingIdx < 17) gender = 4; // PFEM
        else if (endingIdx == 17) gender = 5; // PNEUT
        else gender = 2; // SNEUT
        colourAllowed = true;
      }

      // Add prefix adjective if applicable
      if (doPrefix && prefixIdx >= 0) {
        boolean colourOnly = CzechData.ADJ_COLOUR_ONLY[prefixIdx];
        if (!colourOnly || colourAllowed) {
          int pattern = CzechData.ADJ_PATTERN[prefixIdx];
          sb.append(CzechData.ADJ_NAME[prefixIdx]);
          sb.append(CzechData.PATMOD[gender][pattern]);
          sb.append(' ');
        }
      }

      sb.append(substantiveName);

      if (doSuffix && suffixIdx >= 0) {
        sb.append(' ');
        sb.append(CzechData.SUFFIX[suffixIdx]);
      }

      return sb.toString();
    }
  },

  SWISS {
    @Override
    public String generate(Random rng) {
      return SwissData.REAL[rng.nextInt(SwissData.REAL.length)];
    }
  },

  DANISH {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      // optional first segment
      int i = rng.nextInt(DanishData.PART1.length + 50) - 50;
      if (i >= 0) sb.append(DanishData.PART1[i]);

      sb.append(DanishData.PART2[rng.nextInt(DanishData.PART2.length)]);
      sb.append(DanishData.PART3[rng.nextInt(DanishData.PART3.length)]);
      return sb.toString();
    }
  },

  TURKISH {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();
      int choice = rng.nextInt(5);

      switch (choice) {
        case 0 -> {
          sb.append(TurkishData.PREFIX[rng.nextInt(TurkishData.PREFIX.length)]);
          sb.append(TurkishData.MIDDLE[rng.nextInt(TurkishData.MIDDLE.length)]);
          if (rng.nextInt(7) == 0) {
            sb.append(TurkishData.SUFFIX[rng.nextInt(TurkishData.SUFFIX.length)]);
          }
        }
        case 1, 2 -> {
          sb.append(TurkishData.PREFIX[rng.nextInt(TurkishData.PREFIX.length)]);
          sb.append(TurkishData.SUFFIX[rng.nextInt(TurkishData.SUFFIX.length)]);
        }
        default -> sb.append(TurkishData.REAL[rng.nextInt(TurkishData.REAL.length)]);
      }

      return sb.toString();
    }
  },

  ITALIAN {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      if (rng.nextInt(6) == 0) {
        return ItalianData.REAL[rng.nextInt(ItalianData.REAL.length)];
      }

      if (rng.nextInt(8) == 0) {
        sb.append(ItalianData.PREF[rng.nextInt(ItalianData.PREF.length)]);
      }

      int gender = rng.nextInt(2);
      if (gender == 0) {
        sb.append(ItalianData.PART1M[rng.nextInt(ItalianData.PART1M.length)]);
      } else {
        sb.append(ItalianData.PART1F[rng.nextInt(ItalianData.PART1F.length)]);
      }

      if (rng.nextInt(3) == 0) {
        sb.append(ItalianData.PART2[rng.nextInt(ItalianData.PART2.length)]);
        sb.append(gender == 0 ? "o" : "a");
      } else {
        sb.append(ItalianData.PART2I[rng.nextInt(ItalianData.PART2I.length)]);
      }

      if (rng.nextInt(4) == 0) {
        if (rng.nextInt(2) == 0) {
          sb.append(ItalianData.PART3[rng.nextInt(ItalianData.PART3.length)]);
        } else {
          sb.append(ItalianData.RIVER1[rng.nextInt(ItalianData.RIVER1.length)]);
          sb.append(ItalianData.RIVER2[rng.nextInt(ItalianData.RIVER2.length)]);
        }
      }

      return sb.toString();
    }
  },

  CATALAN {
    @Override
    public String generate(Random rng) {
      StringBuilder sb = new StringBuilder();

      if (rng.nextInt(3) == 0) {
        return CatalanData.REAL[rng.nextInt(CatalanData.REAL.length)];
      }

      if (rng.nextInt(2) == 0) {
        sb.append(CatalanData.PREF[rng.nextInt(CatalanData.PREF.length)]);
      }

      int gender = rng.nextInt(2);
      if (gender == 0) {
        sb.append(CatalanData.PART1M[rng.nextInt(CatalanData.PART1M.length)]);
        sb.append(CatalanData.PART2M[rng.nextInt(CatalanData.PART2M.length)]);
      } else {
        sb.append(CatalanData.PART1F[rng.nextInt(CatalanData.PART1F.length)]);
        sb.append(CatalanData.PART2F[rng.nextInt(CatalanData.PART2F.length)]);
      }

      if (rng.nextInt(5) == 0) {
        if (rng.nextInt(2) == 0) {
          sb.append(CatalanData.PART3[rng.nextInt(CatalanData.PART3.length)]);
        } else {
          sb.append(CatalanData.RIVER1[rng.nextInt(CatalanData.RIVER1.length)]);
        }
      }

      return sb.toString();
    }
  };

  // ---------- shared helpers ----------

  private static String replaceEnglishWords(String name, boolean original) {
    String s = name;
    if (original) s = replaceWord("Ce", "Ke", s);
    if (original) s = replaceWord("Ci", "Ki", s);
    s = replaceWord("Cunt", "East", s);
    s = replaceWord("Slag", "Pits", s);
    s = replaceWord("Slut", "Edin", s);
    if (!original) s = replaceWord("Fart", "Boot", s);
    s = replaceWord("Drar", "Quar", s);
    s = replaceWord("Dreh", "Bash", s);
    s = replaceWord("Frar", "Shor", s);
    s = replaceWord("Grar", "Aber", s);
    s = replaceWord("Brar", "Over", s);
    s = replaceWord("Wrar", original ? "Inve" : "Stan", s);
    return s;
  }

  private static String replaceWord(String from, String to, String name) {
    if (name.startsWith(from)) {
      return to + name.substring(from.length());
    }
    return name;
  }

  private static boolean containsBackVowel(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == 'a' || c == 'o' || c == 'u' || c == 'A' || c == 'O' || c == 'U') {
        return true;
      }
    }
    return false;
  }
}
