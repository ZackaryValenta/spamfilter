package spamfilter;

import java.io.File;
import java.util.HashSet;
import java.util.TreeSet;

public class SpamChecker
{
	private String hamDatasetPath;
	private String spamDatasetPath;
	private HashSet<FilteredDocument> hamDocuments;
	private HashSet<FilteredDocument> spamDocuments;
	private TreeSet<QuantifiedWord> words;

	public SpamChecker(String hamDatasetPath, String spamDatasetPath)
	{
		this.hamDatasetPath  = hamDatasetPath;
		this.spamDatasetPath = spamDatasetPath;
		this.hamDocuments    = filterDocuments(hamDatasetPath);
		this.spamDocuments   = filterDocuments(spamDatasetPath);
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
	
	
	
}
