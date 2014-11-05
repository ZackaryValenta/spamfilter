package spamfilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassifiedDocument
{
	FilteredDocument document;
	String documentName;
	Double hamProbability;
	Double spamProbability;

	public ClassifiedDocument(String documentPath, HashMap<String, QuantifiedWord> vocabularyFilePath, Double hamProbability, Double spamProbability)
	{
		this.document                     = new FilteredDocument(documentPath);
		this.documentName                 = (new File(documentPath)).getName();
		List<Double> hamSpamProbabilities = computeHamSpamProbabilities(vocabularyFilePath, hamProbability, spamProbability);
		this.hamProbability               = hamSpamProbabilities.get(0);
		this.spamProbability              = hamSpamProbabilities.get(1);
	}
	
	public FilteredDocument getDocument()
	{
		return document;
	}

	public String getDocumentName()
	{
		return documentName;
	}

	public Double getHamProbability()
	{
		return hamProbability;
	}

	public Double getSpamProbability()
	{
		return spamProbability;
	}
	
	public boolean isSpam()
	{
		return this.spamProbability > this.hamProbability;
	}

	// the returned list has two elements. the first element is the probability that the document of this
	// object is ham and the second is the probability that it is spam
	private List<Double> computeHamSpamProbabilities(HashMap<String, QuantifiedWord> vocabulary, Double hamProbability, Double spamProbability)
	{
		List<Double> returnList        = new ArrayList<Double>();
		Double documentHamProbability  = Math.log10(hamProbability);
		Double documentSpamProbability = Math.log10(spamProbability);
		for (String currentWord : this.document.getFilteredWords())
		{
			if (vocabulary.containsKey(currentWord))
			{
				QuantifiedWord currentQuantifiedWord = vocabulary.get(currentWord);
				documentHamProbability  += Math.log10(currentQuantifiedWord.getHamConditionalProbability());
				documentSpamProbability += Math.log10(currentQuantifiedWord.getSpamConditionalProbability());
			}
		}
		returnList.add(documentHamProbability);
		returnList.add(documentSpamProbability);
		return returnList;
	}
}
