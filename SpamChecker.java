package spamfilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

public class SpamChecker
{
	private static final double SMOOTHING_FACTOR = 0.5;
	private String hamDatasetPath;
	private String spamDatasetPath;
	private HashSet<FilteredDocument> hamDocuments;
	private HashSet<FilteredDocument> spamDocuments;
	private int hamWordCount;
	private int spamWordCount;
	private double hamProbability;
	private double spamProbability;
	private HashMap<String, QuantifiedWord> datasetWords;

	public SpamChecker(String hamDatasetPath, String spamDatasetPath)
	{
		this.hamDatasetPath  = hamDatasetPath;
		this.spamDatasetPath = spamDatasetPath;
		this.hamDocuments    = filterDocuments(hamDatasetPath);
		this.spamDocuments   = filterDocuments(spamDatasetPath);
		this.hamProbability  = hamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.spamProbability = spamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.datasetWords    = this.createWordSet();
		computeConditionalProbabilities();
	}

	public String getHamDatasetPath()
	{
		return hamDatasetPath;
	}

	public String getSpamDatasetPath()
	{
		return spamDatasetPath;
	}

	public int getHamWordCount()
	{
		return hamWordCount;
	}

	public int getSpamWordCount()
	{
		return spamWordCount;
	}

	public double getHamProbability()
	{
		return hamProbability;
	}

	public double getSpamProbability()
	{
		return spamProbability;
	}
	
	// output all values of datasetWords to a specified text file sorted alphabetically
	public void exportDatasetWordsToTextFile(String exportFilePath)
	{
		try
		{
			File exportFile = new File(exportFilePath);
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			BufferedWriter exportFileBuffer = new BufferedWriter(new FileWriter(exportFile, false));
			// create a sorted set of all the QuantifiedWords created from the dataset
			TreeSet<QuantifiedWord> sortedWords = new TreeSet<QuantifiedWord>(new QuantifiedWordComparator());
			sortedWords.addAll(datasetWords.values());
			// loop through sorted set and, for each word in dataSetWords, output a line to datasetWords.txt
			for (int i = 0; sortedWords.size() != 0; ++i)
			{
				QuantifiedWord currentWord = sortedWords.pollFirst();
				StringBuilder wordLine = new StringBuilder();
				wordLine.append(i + 1);
				wordLine.append("   ");
				wordLine.append(currentWord.getWord());
				wordLine.append("   ");
				wordLine.append(currentWord.getHamFrequency());
				wordLine.append("   ");
				wordLine.append((currentWord.getHamConditionalProbability()) != null ? currentWord.getHamConditionalProbability() : "N/A");
				wordLine.append("   ");
				wordLine.append(currentWord.getSpamFrequency());
				wordLine.append("   ");
				wordLine.append((currentWord.getSpamConditionalProbability()) != null ? currentWord.getSpamConditionalProbability() : "N/A");
				exportFileBuffer.write(wordLine.toString());
				if (sortedWords.size() != 0)
				{
					exportFileBuffer.newLine();
				}
				exportFileBuffer.flush();
			}
			exportFileBuffer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// preconditions:- hamDocuments and spamDocuments must be populated with the file information from
	//                 the hamDatasetPath and spamDatasetPath folder files
	private HashMap<String, QuantifiedWord> createWordSet()
	{
		this.hamWordCount  = 0;
		this.spamWordCount = 0;
		// create a QuantifiedWord for each unique word found in the ham documents, incrementing words that are repeated
		// also increment hamWordCount
		HashMap<String, QuantifiedWord> returnTree = new HashMap<String, QuantifiedWord>();
		for (FilteredDocument currentDocument : this.hamDocuments)
		{
			for (String currentWord : currentDocument.getFilteredWords())
			{
				currentWord = currentWord.toLowerCase();
				if (returnTree.containsKey(currentWord))
				{
					returnTree.get(currentWord).incrementHam();
				}
				else
				{
					QuantifiedWord currentQuantifiedWord = new QuantifiedWord(currentWord);
					currentQuantifiedWord.incrementHam();
					returnTree.put(currentWord, currentQuantifiedWord);
				}
				++this.hamWordCount;
			}
		}
		// create a QuantifiedWord for each unique word found in the spam documents, incrementing words that are repeated
		// also increment spamWordCount
		for (FilteredDocument currentDocument : this.spamDocuments)
		{
			for (String currentWord : currentDocument.getFilteredWords())
			{
				currentWord = currentWord.toLowerCase();
				if (returnTree.containsKey(currentWord))
				{
					returnTree.get(currentWord).incrementSpam();
				}
				else
				{
					QuantifiedWord currentQuantifiedWord = new QuantifiedWord(currentWord);
					currentQuantifiedWord.incrementSpam();
					returnTree.put(currentWord, currentQuantifiedWord);
				}
				++this.spamWordCount;
			}
		}
		// remove any returnTree entry with a word that appears 0 or 1 times in either class of document (spam/ham)
		ArrayList<String> removalKeys = new ArrayList<String>();
		for (Entry<String, QuantifiedWord> currentWord : returnTree.entrySet())
		{
			if (currentWord.getValue().getHamFrequency() < 2 && currentWord.getValue().getSpamFrequency() < 2)
			{
				removalKeys.add(currentWord.getKey());
			}
		}
		for (String currentKey : removalKeys)
		{
			returnTree.remove(currentKey);
		}
		return returnTree;
	}

	private static HashSet<FilteredDocument> filterDocuments(String documentsFolderPath)
	{
		HashSet<FilteredDocument> returnSet = new HashSet<FilteredDocument>();
		File folder = new File(documentsFolderPath);
		File[] files = folder.listFiles();
		for (File currentFile : files)
		{
			returnSet.add(new FilteredDocument(currentFile.getAbsolutePath()));
		}
		return returnSet;
	}
	
	private void computeConditionalProbabilities()
	{
		for (Entry<String,QuantifiedWord> currentWordEntry : datasetWords.entrySet())
		{
			QuantifiedWord currentQuantifiedWord = currentWordEntry.getValue();
			// compute probability of current word given a ham document
			Double probabilityGivenHam = ((double)(currentQuantifiedWord.getHamFrequency() + SMOOTHING_FACTOR)) / 
											(hamWordCount + (datasetWords.size() * SMOOTHING_FACTOR));
			currentQuantifiedWord.setHamConditionalProbability(probabilityGivenHam);
			// compute probability of current word given a spam document
			Double probabilityGivenSpam = ((double)(currentQuantifiedWord.getSpamFrequency() + SMOOTHING_FACTOR)) / 
											(spamWordCount + (datasetWords.size() * SMOOTHING_FACTOR));
			currentQuantifiedWord.setSpamConditionalProbability(probabilityGivenSpam);
		}
	}
}
