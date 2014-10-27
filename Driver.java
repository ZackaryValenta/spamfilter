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

		System.out.print("Files in ham dataset: ");
		System.out.println(hamFiles.length);
		System.out.print("Files in spam dataset: ");
		System.out.println(spamFiles.length);

		System.out.print("\nCreating SpamChecker using documents in \"dataset/ham\" and \"dataset/spam\" folders...");
		SpamChecker testChecker = new SpamChecker("./dataset/ham", "./dataset/spam");
		System.out.println("Done\n");		
		System.out.print("Exporting spam checker's vocabulary to \"dataset/datasetWords.txt\"...");
		testChecker.exportDatasetWordsToTextFile("./dataset/datasetWords.txt");
		System.out.println("Done");		
	}
}