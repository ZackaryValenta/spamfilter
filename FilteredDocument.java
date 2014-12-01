package spamfilter;

import java.io.IOException;
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

	private static final boolean REMOVE_XML_TAGS = false;
	private String absolutePath;
	private String documentContents;
	private ArrayList<String> filteredWords;

	public FilteredDocument(String absolutePath)
	{
		this.absolutePath     = absolutePath;
		this.documentContents = fileContentsToString(absolutePath);
		String tagsRemoved    = removeXMLTags(this.documentContents);
		this.filteredWords    = getFilteredWords((REMOVE_XML_TAGS) ? tagsRemoved : this.documentContents);		// based on the REMOVE_XML_TAGS variable, a different verison of the document is passed here
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
		List<String> fileLines = new ArrayList<String>();
		StringBuffer returnStringBuffer = new StringBuffer();
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)));
			String currentLine;
			while((currentLine = bufferedReader.readLine()) != null)
			{
				fileLines.add(currentLine);
			}
			bufferedReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for (String currentLine : fileLines)
		{
			returnStringBuffer.append(currentLine);
			returnStringBuffer.append("\n");
		}
		return returnStringBuffer.toString();
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
			if (currentWord != "")
			{
				returnSet.add(currentWord);
			}
		}
		return returnSet;
	}

	// remove any non uppercase or lowercase letter characters from the end of the specified string
	// return an empty string if no uppercase or lowercase letter characters were found
	private static String trimEnds(String word)
	{
		// find the beginning of a word
		int i = 0;
		while (i < word.length() && !SpamChecker.isAlphabeticCharacter(word.charAt(i)))
		{
			++i;
		}
		// find the end of the word
		int j = word.length()- 1;
		while (j >= 0 && !SpamChecker.isAlphabeticCharacter(word.charAt(j)))
		{
			--j;
		}
		// trim apostrophized "s" from the end
		if (j > 1 && word.charAt(j - 1) == '\'' && (word.charAt(j) == 's' || word.charAt(j) == 'S'))
		{
			j -= 2;
		}
		// if the beginning found is after the end then return an empty string
		if (i > j)
		{
			return "";
		}
		return word.substring(i, j + 1);
	}
}