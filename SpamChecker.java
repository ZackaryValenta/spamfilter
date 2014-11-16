package spamfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

public class SpamChecker
{
	private static final int MIN_WORD_LENGTH = 4;
	private static final int MAX_WORD_LENGTH = 22;			// 28 is the size of the longest non-coined and nontechnical word
	private static final double SMOOTHING_FACTOR = 0.1;
	private static final boolean UPPER_LOWER_CASE_FOLDING = true;
	private static final boolean USE_STOPWORDS = true;
	private String hamDatasetPath;
	private String spamDatasetPath;
	private String stopWordsPath;
	private HashSet<FilteredDocument> hamDocuments;
	private HashSet<FilteredDocument> spamDocuments;
	private int hamWordCount;
	private int spamWordCount;
	private double hamProbability;
	private double spamProbability;
	private HashSet<String> stopWords;
	private HashMap<String, QuantifiedWord> vocabulary;

	// constructor that takes a path to a folder of known ham files and a path to a folder of known spam files and
	// creates a vocabulary from all the words found
	public SpamChecker(String hamDatasetPath, String spamDatasetPath, String stopWordsPath)
	{
		this.hamDatasetPath  = hamDatasetPath;
		this.spamDatasetPath = spamDatasetPath;
		this.stopWordsPath   = stopWordsPath;
		this.hamDocuments    = filterDocuments(hamDatasetPath);
		this.spamDocuments   = filterDocuments(spamDatasetPath);
		this.hamProbability  = (double)hamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.spamProbability = (double)spamDocuments.size() / (hamDocuments.size() + spamDocuments.size());
		this.stopWords       = parseStopWords(stopWordsPath);
		this.vocabulary      = this.createVocabulary();
		computeConditionalProbabilities();
	}

	// constructor that takes the path to a text file of a SpamChecker model and initializes the attributes
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

	public String getStopWordsPath()
	{
		return stopWordsPath;
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
		return new ClassifiedDocument(documentPath, vocabulary, UPPER_LOWER_CASE_FOLDING, hamProbability, spamProbability);
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
	
	// parse specified file and returns a HashSet containing its lines
	private HashSet<String> parseStopWords(String filePath)
	{
		HashSet<String> returnSet = new HashSet<String>();
		if ((new File(filePath)).exists())
		{
			try
			{
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
				String currentLine;
				while((currentLine = bufferedReader.readLine()) != null)
				{
					currentLine = currentLine.trim();
					currentLine = UPPER_LOWER_CASE_FOLDING ? currentLine.toLowerCase() : currentLine;
					returnSet.add(currentLine);
				}
				bufferedReader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return returnSet;
	}
	
	// preconditions:- hamDocuments and spamDocuments must be populated with the file information from
	//                 the hamDatasetPath and spamDatasetPath folder files
	private HashMap<String, QuantifiedWord> createVocabulary()
	{
		HashMap<String, QuantifiedWord> returnMap = new HashMap<String, QuantifiedWord>();
		this.hamWordCount  = 0;
		this.spamWordCount = 0;
		// create a QuantifiedWord for each unique word found in the ham documents, incrementing words that are repeated
		// while also incrementing hamWordCount
		for (FilteredDocument currentDocument : this.hamDocuments)
		{
			for (String currentWord : currentDocument.getFilteredWords())
			{
				currentWord = UPPER_LOWER_CASE_FOLDING ? currentWord.toLowerCase() : currentWord;
				if (isAcceptableWord(currentWord))
				{
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
		}
		// create a QuantifiedWord for each unique word found in the spam documents, incrementing words that are repeated
		// while also incrementing spamWordCount
		for (FilteredDocument currentDocument : this.spamDocuments)
		{
			for (String currentWord : currentDocument.getFilteredWords())
			{
				currentWord = UPPER_LOWER_CASE_FOLDING ? currentWord.toLowerCase() : currentWord;
				if (isAcceptableWord(currentWord))
				{
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
		}
		// remove any returnTree entry with a word that appears 0 or 1 times in either class of document (spam/ham) and
		// decrement their appearance in the ham and spam word counts
		ArrayList<String> removalKeys = new ArrayList<String>();
		for (Entry<String, QuantifiedWord> currentWord : returnMap.entrySet())
		{
			if (currentWord.getValue().getHamFrequency() < 2 && currentWord.getValue().getSpamFrequency() < 2)
			{
				removalKeys.add(currentWord.getKey());
				this.hamWordCount  -= currentWord.getValue().getHamFrequency();
				this.spamWordCount -= currentWord.getValue().getSpamFrequency();
			}
		}
		for (String currentKey : removalKeys)
		{
			returnMap.remove(currentKey);
		}
		return returnMap;
	}

	// this method controls the assumptions used when accepting or rejecting words to be used in the vocabulary
	private boolean isAcceptableWord(String word)
	{
		return isAcceptableLength(word, MIN_WORD_LENGTH, MAX_WORD_LENGTH) &&
				hasWordCharactersOnly(word) &&
				!isCharacterRepetition(word) &&
				(USE_STOPWORDS ? !this.stopWords.contains(word) : true);
	}

	// checks if every character in a word is the same
	private static boolean isCharacterRepetition(String word)
	{
		for (int i = 1; i < word.length(); ++i)
		{
			if (word.charAt(i) != word.charAt(i - 1))
			{
				return false;
			}
		}
		return true;
	}

	// checks if the specified word is between the MIN and MAX lengths
	private static boolean isAcceptableLength(String word, int minLength, int maxLength)
	{
		return word.length() >= minLength && word.length() <= maxLength;
	}

	// checks every character in the string and returns false if one is not a letter or a hyphen ('-')
	private static boolean hasWordCharactersOnly(String word)
	{
		for (int i = 0; i < word.length(); ++i)
		{
			if (!isAlphabeticCharacter(word.charAt(i)) && word.charAt(i) != '-')
			{
				return false;
			}
		}
		return true;
	}

	// checks if a specified character is an uppercase or lowercase letter
	public static boolean isAlphabeticCharacter(char character)
	{
		return (character >= 65 && character <= 90) || (character >= 97 && character <= 122);
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