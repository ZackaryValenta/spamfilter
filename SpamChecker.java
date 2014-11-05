package spamfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
	private HashMap<String, QuantifiedWord> vocabulary;

	// constructor that takes a path to a folder of known ham files and a path to a folder of known spam files and
	// creates a vocabulary from all the words found
	public SpamChecker(String hamDatasetPath, String spamDatasetPath)
	{
		this.hamDatasetPath  = hamDatasetPath;
		this.spamDatasetPath = spamDatasetPath;
		this.hamDocuments    = filterDocuments(hamDatasetPath);
		this.spamDocuments   = filterDocuments(spamDatasetPath);
		this.hamProbability  = hamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.spamProbability = spamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.vocabulary      = this.createVocabulary();
		computeConditionalProbabilities();
	}
	
	// constructor that takes the path to a text file of a SpamCehcker model and initializes the attributes
	public SpamChecker(String modelPath)
	{
		parseModel(modelPath);
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
	public void exportModelToTextFile(String exportFilePath)
	{
		try
		{
			File exportFile = new File(exportFilePath);
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			BufferedWriter exportFileBuffer = new BufferedWriter(new FileWriter(exportFile, false));
			// ADD FIRST LINE WITH ALL NON-VOCABULARY DATA
			StringBuilder firstLine = new StringBuilder();
			firstLine.append(hamDatasetPath);
			firstLine.append("   ");
			firstLine.append(spamDatasetPath);
			firstLine.append("   ");
			firstLine.append(hamProbability);
			firstLine.append("   ");
			firstLine.append(spamProbability);
			firstLine.append("   ");
			exportFileBuffer.write(firstLine.toString());
			exportFileBuffer.newLine();
			exportFileBuffer.flush();
			// ADD A LINE FOR EVERY WORD IN VOCABULARY
			// create a sorted set of all the QuantifiedWords created from the dataset
			TreeSet<QuantifiedWord> sortedWords = new TreeSet<QuantifiedWord>(new QuantifiedWordComparator());
			sortedWords.addAll(vocabulary.values());
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
	
	// creates a classified document using dataset information
	public ClassifiedDocument classifyDocument(String documentPath)
	{
		return new ClassifiedDocument(documentPath, vocabulary, hamProbability, spamProbability);
	}

	// loop through files in specified folder and create a FilteredDocument for each
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
	
	// preconditions:- hamDocuments and spamDocuments must be populated with the file information from
	//                 the hamDatasetPath and spamDatasetPath folder files
	private HashMap<String, QuantifiedWord> createVocabulary()
	{
		this.hamWordCount  = 0;
		this.spamWordCount = 0;
		// create a QuantifiedWord for each unique word found in the ham documents, incrementing words that are repeated
		// also increment hamWordCount
		HashMap<String, QuantifiedWord> returnMap = new HashMap<String, QuantifiedWord>();
		for (FilteredDocument currentDocument : this.hamDocuments)
		{
			for (String currentWord : currentDocument.getFilteredWords())
			{
				currentWord = currentWord.toLowerCase();
				if (returnMap.containsKey(currentWord))
				{
					returnMap.get(currentWord).incrementHam();
				}
				else
				{
					QuantifiedWord currentQuantifiedWord = new QuantifiedWord(currentWord);
					currentQuantifiedWord.incrementHam();
					returnMap.put(currentWord, currentQuantifiedWord);
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
				if (returnMap.containsKey(currentWord))
				{
					returnMap.get(currentWord).incrementSpam();
				}
				else
				{
					QuantifiedWord currentQuantifiedWord = new QuantifiedWord(currentWord);
					currentQuantifiedWord.incrementSpam();
					returnMap.put(currentWord, currentQuantifiedWord);
				}
				++this.spamWordCount;
			}
		}
		// remove any returnTree entry with a word that appears 0 or 1 times in either class of document (spam/ham)
		ArrayList<String> removalKeys = new ArrayList<String>();
		for (Entry<String, QuantifiedWord> currentWord : returnMap.entrySet())
		{
			if (currentWord.getValue().getHamFrequency() < 2 && currentWord.getValue().getSpamFrequency() < 2)
			{
				removalKeys.add(currentWord.getKey());
			}
		}
		for (String currentKey : removalKeys)
		{
			returnMap.remove(currentKey);
		}
		return returnMap;
	}

	private void computeConditionalProbabilities()
	{
		for (Entry<String,QuantifiedWord> currentWordEntry : vocabulary.entrySet())
		{
			QuantifiedWord currentQuantifiedWord = currentWordEntry.getValue();
			
			// compute probability of current word given a ham document
			Double probabilityGivenHam = ((double)(currentQuantifiedWord.getHamFrequency() + SMOOTHING_FACTOR)) / 
											(hamWordCount + (vocabulary.size() * SMOOTHING_FACTOR));
			currentQuantifiedWord.setHamConditionalProbability(probabilityGivenHam);
			
			// compute probability of current word given a spam document
			Double probabilityGivenSpam = ((double)(currentQuantifiedWord.getSpamFrequency() + SMOOTHING_FACTOR)) /
											(spamWordCount + (vocabulary.size() * SMOOTHING_FACTOR));
			currentQuantifiedWord.setSpamConditionalProbability(probabilityGivenSpam);
		}
	}

	// initializes the hamDatasetPath, spamDatasetPath, hamProbability, spamProbability, and vocabulary
	// from the file at the specified path
	private void parseModel(String modelPath)
	{
		File modelFile = new File(modelPath);
		try
		{
			BufferedReader modelFileReader = new BufferedReader(new FileReader(modelFile));
			String line;
			// initialize non-vocabulary data using first line
			if ((line = modelFileReader.readLine()) != null)
			{
				String[] wordLine = line.split("\\s+");
				this.hamDatasetPath  = wordLine[0];
				this.spamDatasetPath = wordLine[1];
				this.hamProbability  = Double.parseDouble(wordLine[2]);
				this.spamProbability = Double.parseDouble(wordLine[3]);
			}
			// initialize vocabulary

			this.vocabulary = new HashMap<String, QuantifiedWord>();
			while ((line = modelFileReader.readLine()) != null)
			{
				String[] wordLine = line.split("\\s+");
				QuantifiedWord quantifiedWord = new QuantifiedWord(wordLine[1], Integer.parseInt(wordLine[2]),
																	Double.parseDouble(wordLine[3]),
																	Integer.parseInt(wordLine[4]),
																	Double.parseDouble(wordLine[5]));
				this.vocabulary.put(wordLine[1], quantifiedWord);
				
			}
			modelFileReader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}