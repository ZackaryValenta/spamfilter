package spamfilter;

public class QuantifiedWord
{
	private String word;
	private int hamFrequency;
	private Double hamConditionalProbability;
	private int spamFrequency;
	private Double spamConditionalProbability;

	public QuantifiedWord(String word)
	{
		this(word, 0, null, 0, null);
	}
	
	public QuantifiedWord(String word, int hamFrequency, Double hamConditionalProbability, int spamFrequency, Double spamConditionalProbability)
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

	public Double getHamConditionalProbability()
	{
		return hamConditionalProbability;
	}

	public int getSpamFrequency()
	{
		return spamFrequency;
	}

	public Double getSpamConditionalProbability()
	{
		return spamConditionalProbability;
	}

	public void setHamConditionalProbability(Double hamConditionalProbability)
	{
		this.hamConditionalProbability = hamConditionalProbability;
	}

	public void setSpamConditionalProbability(Double spamConditionalProbability)
	{
		this.spamConditionalProbability = spamConditionalProbability;
	}

	public void incrementHam()
	{
		++this.hamFrequency;
	}

	public void incrementSpam()
	{
		++this.spamFrequency;
	}
}