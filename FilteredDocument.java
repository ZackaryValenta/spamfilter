package spamfilter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FilteredDocument
{
	public enum DocumentType
	{
		SPAM("Spam"), HAM("Ham");
		
		private String typeName;
		private DocumentType(String typeName)
		{
			this.typeName = typeName;
		}
		public String toString()
		{
			return this.typeName;
		}
	}
	
	private String absolutePath;
	private String documentContents;
	private ArrayList<String> filteredWords;
	
	public FilteredDocument(String absolutePath)
	{
		this.absolutePath     = absolutePath;
		this.documentContents = fileContentsToString(absolutePath);
		String tagsRemoved    = removeXMLTags(this.documentContents);
		this.filteredWords    = getFilteredWords(tagsRemoved);
	}

	public String getAbsolutePath()
	{
		return absolutePath;
	}

	public String getDocumentContents()
	{
		return documentContents;
	}

	public ArrayList<String> getFilteredWords()
	{
		return filteredWords;
	}

	public boolean equals(Object obj)
	{
		if (obj == null) {return false;}
		if (!(obj instanceof FilteredDocument)) {return false;}
		FilteredDocument other = (FilteredDocument) obj;
		return this.getAbsolutePath() == other.getAbsolutePath();
	}
	
	public int hashCode(FilteredDocument document)
	{
		return (this.getAbsolutePath().hashCode() * 31)
				+ (this.getDocumentContents().hashCode() * 31);
	}
	
	// returns a string containing the contents of a specified file
	private String fileContentsToString(String absolutePath)
	{
		String returnString = "";
		List<String> fileLines = new ArrayList<String>();
		
		try
		{
			FileInputStream in = new FileInputStream(absolutePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String currentLine;
			while((currentLine = br.readLine())!= null)
			{
				fileLines.add(currentLine);
			}
			for (String line : fileLines)
			{
				returnString += line + "\n";
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return returnString;
	}

	// returns the contents of the specified string with all XML tags removed
	private static String removeXMLTags(String documentContents)
	{
		String returnString     = "";
		ArrayList<Integer> tags = new ArrayList<Integer>();
		tags.add(-1);
		Integer openingChevron  = null;
		boolean inQuotes        = false;
		for (int i = 0; i < documentContents.length(); ++i)
		{
			if (documentContents.charAt(i) == '<' && !inQuotes)
			{
				openingChevron = i;
			}
			else if (documentContents.charAt(i) == '>' && !inQuotes && openingChevron != null)
			{
				tags.add(openingChevron);
				tags.add(i);
				openingChevron = null;
			}
			else if ((documentContents.charAt(i) == '\'' || documentContents.charAt(i) == '\"') &&
					documentContents.charAt(i-1) != '\\' && openingChevron != null)
			{
				inQuotes = !inQuotes;
			}
		}

		tags.add(documentContents.length());
		for (int i = 0; i < tags.size(); i += 2)
		{
			returnString += documentContents.substring(tags.get(i) + 1, tags.get(i + 1));
		}
		return returnString;
	}

	// search trough specified string for desirable words
	private static ArrayList<String> getFilteredWords(String documentContents)
	{
		ArrayList<String> returnSet = new ArrayList<String>();
		for (String currentWord : documentContents.split("\\s+"))
		{
			currentWord = trimEnds(currentWord);
			if (currentWord != "" && isDesirableWord(currentWord))
			{
				returnSet.add(currentWord);
			}
		}
		return returnSet;
	}
	
	// checks if the specified word has desirable traits
	private static boolean isDesirableWord(String word)
	{
		return hasWordCharactersOnly(word) && (word.length() > 3);
	}
	
	// remove any non uppercase or lowercase letter characters from the end of the specified string
	// return an empty string if no uppercase or lowercase letter characters were found
	private static String trimEnds(String word)
	{
		// find the beginning of a word
		int i = 0;
		while (i < word.length() && !isAlphabeticCharacter(word.charAt(i)))
		{
			++i;
		}
		// if there is no beginning of a word return an empty string
		if (i == word.length())
		{
			return "";
		}
		// find the end of the word
		int j = word.length()- 1;
		while (j >= 0 && !isAlphabeticCharacter(word.charAt(j)))
		{
			--j;
		}
		return word.substring(i, j + 1);
	}
	
	// checks if a specified character is an uppercase or lowercase letter
	private static boolean isAlphabeticCharacter(char character)
	{
		return (character >= 65 && character <= 90) || (character >= 97 && character <= 122);
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
}
