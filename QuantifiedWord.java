package spamfilter;

import java.util.Comparator;

public class QuantifiedWord
{
	private String word;
	private int hamFrequency;
	private double hamConditionalProbability;
	private int spamFrequency;
	private double spamConditionalProbability;
	
	public QuantifiedWord(String word, int hamFrequency, double hamConditionalProbability, int spamFrequency, double spamConditionalProbability)
	{
		this.word = word;
		this.hamFrequency = hamFrequency;
		this.hamConditionalProbability = hamConditionalProbability;
		this.spamFrequency = spamFrequency;
		this.spamConditionalProbability = spamConditionalProbability;
	}

	public String getWord()
	{
		return word;
	}

	public int getHamFrequency()
	{
		return hamFrequency;
	}

	public double getHamConditionalProbability()
	{
		return hamConditionalProbability;
	}

	public int getSpamFrequency()
	{
		return spamFrequency;
	}

	public double getSpamConditionalProbability()
	{
		return spamConditionalProbability;
	}
	
}