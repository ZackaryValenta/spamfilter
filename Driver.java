package spamfilter;

import java.io.File;

public class Driver
{
	public static void main(String[] args)
	{
		File hamFolder = new File("./dataset/ham");
		File spamFolder = new File("./dataset/spam");
		File[] hamFiles = hamFolder.listFiles();
		File[] spamFiles = spamFolder.listFiles();
		
		System.out.println(hamFiles.length);
		System.out.println(spamFiles.length);
		System.out.println();
		
		FilteredDocument testDocument = new FilteredDocument(spamFiles[1].getAbsolutePath());
		System.out.println(testDocument.getDocumentContents());
		
		System.out.println("\ntest document's filtered words:\n");
		for (String s : testDocument.getFilteredWords())
		{
			System.out.println(s);
		}
		
		SpamChecker testChecker = new SpamChecker("./dataset/ham", "./dataset/spam");
		
		testChecker.exportDatasetWordsToTextFile("./dataset/datasetWords.txt");
		
	}
}
