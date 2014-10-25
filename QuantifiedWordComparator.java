package spamfilter;

import java.util.Comparator;

public class QuantifiedWordComparator implements Comparator<QuantifiedWord>{

	@Override
	public int compare(QuantifiedWord word1, QuantifiedWord word2) {
		return word1.getWord().compareTo(word2.getWord());
	}

}
