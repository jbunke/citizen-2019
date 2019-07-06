package com.redsquare.citizen.systems.language;

import com.redsquare.citizen.util.Randoms;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class WritingSystem {

  private final Phonology phonology;
  private final Map<WordSubUnit, Glyph> glyphs;
  private final List<WordSubUnit> keys;
  private final Type type;

  // VISUAL CRITERIA
  final double avgLineCurve; // 0 - 1 skewed ^2
  final double curveDeviationMax; // 0.1 - 0.5
  final double avgLineLength; // 0 - 1
  final double avgContinuationProb; // 0.5 - 1
  final double continuationDeviationMax; // 0 - 0.5
  final double commonElemProbability; // 0 - 1

  final double directionalProclivity; // 0 - 1
  final int prefDirection; // 0 - 359
  final int maxDirectionSkew; // 0 - 180

  final Set<GlyphComponent> commonElements;

  private WritingSystem(Phonology phonology, Type type) {
    // Random likelihood of syllabic vs alphabetical
    this.phonology = phonology;
    this.type = type;

    avgLineCurve = Math.random() * Math.random();
    avgLineLength = Randoms.bounded(0.4, 1d);
    curveDeviationMax = Randoms.bounded(0.1, 0.5);
    avgContinuationProb = 1 - Math.pow(Randoms.bounded(0d, 0.5), 2);
    continuationDeviationMax = Math.pow(Randoms.bounded(0d, Math.sqrt(0.5)), 2);
    commonElemProbability = Math.random();

    directionalProclivity = Math.random();
    prefDirection = Randoms.bounded(0, 360);
    maxDirectionSkew = Randoms.bounded(0, 180);

    int amountCommonElements = Randoms.bounded(2, 5);
    commonElements = new HashSet<>();

    while (commonElements.size() < amountCommonElements) {
      commonElements.add(GlyphComponent.orig(this));
    }

    // populate and sort keys
    keys = enumerateKeys();
    sortKeys();

    glyphs = generateGlyphs();
  }

  public static WritingSystem generate(Phonology phonology) {
    Type type;

    if (Math.random() < 0.5) type = Type.ALPHABET;
    else type = Type.COMPONENT_SYLLABARY;

    return new WritingSystem(phonology, type);
  }

  public static WritingSystem generate(Phonology phonology,
                                       Type type) {
    return new WritingSystem(phonology, type);
  }

  public enum Type {
    ALPHABET, COMPONENT_SYLLABARY, DISTINCT_SYLLABARY
  }

  private void sortKeys() {
    for (int i = 0; i < keys.size(); i++) {
      for (int j = i + 1; j < keys.size(); j++) {
        if (keys.get(j).toString().length() > keys.get(i).toString().length()) {
          WordSubUnit temp = keys.get(i);
          keys.set(i, keys.get(j));
          keys.set(j, temp);
        }
      }
    }
  }

  private Map<WordSubUnit, Glyph> generateGlyphs() {
    Map<WordSubUnit, Glyph> glyphs = new HashMap<>();

    for (WordSubUnit key : keys) {
      // generate glyph
      Glyph candidate = Glyph.generate(this);
      glyphs.put(key, candidate);
    }

    glyphs.put(new Phoneme(" "), Glyph.empty());

    return glyphs;
  }

  private List<WordSubUnit> enumerateKeys() {
    List<WordSubUnit> keys = new ArrayList<>();

    switch (type) {
      case COMPONENT_SYLLABARY:
        Set<Syllable> vowelsOnly = new HashSet<>();
        Set<Syllable> prefixVowel = new HashSet<>();
        Set<Syllable> vowelSuffix = new HashSet<>();
        Set<Syllable> prefixVowelSuffix = new HashSet<>();

        for (String vowel : phonology.VOWEL_PHONEMES) {
          vowelsOnly.add(new Syllable("", vowel, ""));

          for (String prefix : phonology.PREFIX_CONS_PHONEMES) {
            if (!Phonemes.ILLEGAL_PREFIX_TO_VOWEL.containsKey(prefix) ||
                    !Phonemes.ILLEGAL_PREFIX_TO_VOWEL.
                    get(prefix).contains(vowel)) {
              prefixVowel.add(new Syllable(prefix, vowel, ""));

              for (String suffix : phonology.SUFFIX_CONS_PHONEMES) {
                if (!Phonemes.ILLEGAL_VOWEL_TO_SUFFIX.containsKey(vowel) ||
                        !Phonemes.ILLEGAL_VOWEL_TO_SUFFIX.
                        get(vowel).contains(suffix)) {
                  prefixVowelSuffix.add(new Syllable(prefix, vowel, suffix));
                }
              }
            }
          }

          for (String suffix : phonology.SUFFIX_CONS_PHONEMES) {
            if (!Phonemes.ILLEGAL_VOWEL_TO_SUFFIX.containsKey(vowel) ||
                    !Phonemes.ILLEGAL_VOWEL_TO_SUFFIX.
                    get(vowel).contains(suffix)) {
              vowelSuffix.add(new Syllable("", vowel, suffix));
            }
          }
        }

        keys.addAll(vowelsOnly);
        keys.addAll(prefixVowel);
        keys.addAll(prefixVowelSuffix);
        keys.addAll(vowelSuffix);
        break;
      case ALPHABET:
        for (String vowel : phonology.VOWEL_PHONEMES)
          keys.add(new Phoneme(vowel));
        for (String prefix : phonology.PREFIX_CONS_PHONEMES)
          keys.add(new Phoneme(prefix));
        for (String suffix : phonology.SUFFIX_CONS_PHONEMES)
          keys.add(new Phoneme(suffix));
        break;
    }

    keys.add(new Phoneme(" "));

    return keys;
  }

  private List<Glyph> translate(Word word) {
    List<Glyph> glyphs = new ArrayList<>();

    for (Syllable syllable : word.getSyllables()) {
      if (type == Type.COMPONENT_SYLLABARY) {
        glyphs.add(this.glyphs.get(syllable));
      } else {
        Phoneme prefix = new Phoneme(syllable.getPrefix());
        Phoneme vowel = new Phoneme(syllable.getVowel());
        Phoneme suffix = new Phoneme(syllable.getSuffix());

        if (prefix.toString().length() > 0)
          glyphs.add(this.glyphs.get(prefix));
        if (vowel.toString().length() > 0)
          glyphs.add(this.glyphs.get(vowel));
        if (suffix.toString().length() > 0)
          glyphs.add(this.glyphs.get(suffix));
      }
    }

    return glyphs;
  }

  private List<Glyph> translate(String text) {
    List<Glyph> glyphs = new ArrayList<>();

    boolean noMatchFound = false;
    while (text.length() > 0 && !noMatchFound) {
      noMatchFound = true;
      for (WordSubUnit key : keys) {
        String toMatch = key.toString();

        if (text.startsWith(toMatch) && this.glyphs.containsKey(key)) {
          glyphs.add(this.glyphs.get(key));
          text = text.substring(toMatch.length());
          noMatchFound = false;
          break;
        }
      }

      if (noMatchFound) text = text.substring(1);
    }

    return glyphs;
  }

  public BufferedImage draw(String[] lines, final int SIZE) {
    List<BufferedImage> images = new ArrayList<>();
    int widest = Integer.MIN_VALUE;
    int height = 0;

    for (String line : lines) {
      BufferedImage image = draw(line, SIZE);
      images.add(image);
      widest = Math.max(widest, image.getWidth());
      height += image.getHeight();
    }

    BufferedImage allLines =
            new BufferedImage(widest, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) allLines.getGraphics();

    height = 0;
    for (BufferedImage image : images) {
      g.drawImage(image, 0, height, null);
      height += image.getHeight();
    }

    return allLines;
  }

  public BufferedImage draw(String text, final int SIZE) {
    List<Glyph> glyphs = translate(text.toLowerCase());

    BufferedImage writing =
            new BufferedImage(glyphs.size() * SIZE,
            SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) writing.getGraphics();

    for (int i = 0; i < glyphs.size(); i++) {
      BufferedImage img = glyphs.get(i).draw(SIZE);
      g.drawImage(img, SIZE * i, 0, null);
    }

    return writing;
  }
}
