package com.termux.terminal;

import junit.framework.TestCase;

public class WcWidthTest extends TestCase {

	private static void assertWidthIs(int expectedWidth, int codePoint) {
		int wcWidth = WcWidth.width(codePoint);
		assertEquals(expectedWidth, wcWidth);
	}

	public void testPrintableAscii() {
		for (int i = 0x20; i <= 0x7E; i++) {
			assertWidthIs(1, i);
		}
	}

	public void testSomeWidthOne() {
		assertWidthIs(1, 'å');
		assertWidthIs(1, 'ä');
		assertWidthIs(1, 'ö');
		assertWidthIs(1, 0x23F2);
	}

	public void testSomeWide() {
		assertWidthIs(2, 'Ａ');
		assertWidthIs(2, 'Ｂ');
		assertWidthIs(2, 'Ｃ');
		assertWidthIs(2, '中');
		assertWidthIs(2, '文');

		assertWidthIs(2, 0x679C);
		assertWidthIs(2, 0x679D);

		assertWidthIs(2, 0x2070E);
		assertWidthIs(2, 0x20731);

		assertWidthIs(1, 0x1F781);
	}

	public void testSomeNonWide() {
		assertWidthIs(1, 0x1D11E);
		assertWidthIs(1, 0x1D11F);
	}

	public void testCombining() {
		assertWidthIs(0, 0x0302);
		assertWidthIs(0, 0x0308);
		assertWidthIs(0, 0xFE0F);
	}

	public void testWordJoiner() {
		// https://en.wikipedia.org/wiki/Word_joiner
		// The word joiner (WJ) is a code point in Unicode used to separate words when using scripts
		// that do not use explicit spacing. It is encoded since Unicode version 3.2
		// (released in 2002) as U+2060 WORD JOINER (HTML &#8288;).
		// The word joiner does not produce any space, and prohibits a line break at its position.
		assertWidthIs(0, 0x2060);
	}

	public void testSofthyphen() {
		// http://osdir.com/ml/internationalization.linux/2003-05/msg00006.html:
		// "Existing implementation practice in terminals is that the SOFT HYPHEN is
		// a spacing graphical character, and the purpose of my wcwidth() was to
		// predict the advancement of the cursor position after a string is sent to
		// a terminal. Hence, I have no choice but to keep wcwidth(SOFT HYPHEN) = 1.
		// VT100-style terminals do not hyphenate."
		assertWidthIs(1, 0x00AD);
	}

	public void testHangul() {
		assertWidthIs(1, 0x11A3);
	}

	public void testEmojis() {
		assertWidthIs(2, 0x1F428); // KOALA.
		assertWidthIs(2, 0x231a);  // WATCH.
		assertWidthIs(2, 0x1F643); // UPSIDE-DOWN FACE (Unicode 8).
	}

	public void testBengaliBaseConsonantsHaveWidthOne() {
		// Bengali base consonants must continue to occupy one terminal cell.
		assertWidthIs(1, 0x0995); // ক  Bengali Letter Ka
		assertWidthIs(1, 0x09AC); // ব  Bengali Letter Ba
		assertWidthIs(1, 0x09B6); // শ  Bengali Letter Sha
		assertWidthIs(1, 0x09A8); // ন  Bengali Letter Na
		assertWidthIs(1, 0x09B2); // ল  Bengali Letter La
	}

	public void testBengaliCombiningMarksAlreadyZeroWidth() {
		// Pre-existing zero-width entries from the upstream table must keep returning 0.
		assertWidthIs(0, 0x0981); // ঁ  Bengali Sign Candrabindu
		assertWidthIs(0, 0x09BC); // ়  Bengali Sign Nukta
		assertWidthIs(0, 0x09C1); // ু  Bengali Vowel Sign U
		assertWidthIs(0, 0x09C2); // ূ  Bengali Vowel Sign Uu
		assertWidthIs(0, 0x09CD); // ্  Bengali Sign Virama (halant)
	}

	public void testBengaliSpacingMarksAreTreatedAsZeroWidth() {
		// Termux deviation: Bengali spacing combining marks (general category Mc) are treated as
		// zero-width so each Bengali grapheme cluster lives in a single terminal cell and shapes
		// correctly. See WcWidth.BENGALI_SPACING_MARKS.
		assertWidthIs(0, 0x0982); // ং  Bengali Sign Anusvara
		assertWidthIs(0, 0x0983); // ঃ  Bengali Sign Visarga
		assertWidthIs(0, 0x09BE); // া  Bengali Vowel Sign Aa
		assertWidthIs(0, 0x09BF); // ি  Bengali Vowel Sign I (pre-base matra)
		assertWidthIs(0, 0x09C0); // ী  Bengali Vowel Sign Ii
		assertWidthIs(0, 0x09C7); // ে  Bengali Vowel Sign E (pre-base matra)
		assertWidthIs(0, 0x09C8); // ৈ  Bengali Vowel Sign Ai (pre-base matra)
		assertWidthIs(0, 0x09CB); // ো  Bengali Vowel Sign O
		assertWidthIs(0, 0x09CC); // ৌ  Bengali Vowel Sign Au
		assertWidthIs(0, 0x09D7); // ৗ  Bengali Au Length Mark
	}

	public void testBengaliClusterCellWidth() {
		// A Bengali word like "বাংলা" (bāṅlā) is six code points: ব া ং ল া. Five of those code
		// points are spacing/combining marks that should now be zero-width, so the whole word
		// occupies just two terminal cells (one per base consonant ব and ল).
		char[] banglaWord = "বাংলা".toCharArray();
		int totalWidth = 0;
		for (int i = 0; i < banglaWord.length; i++) {
			totalWidth += WcWidth.width(banglaWord, i);
		}
		assertEquals(2, totalWidth);
	}

}
