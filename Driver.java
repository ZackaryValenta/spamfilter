package spamfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Driver
{
	public static void main(String[] args)
	{
		// display the number of files in the Ham and Span folders
		File hamFolder = new File("./dataset/ham");
		File spamFolder = new File("./dataset/spam");
		File[] hamFiles = hamFolder.listFiles();
		File[] spamFiles = spamFolder.listFiles();
		System.out.print("Files in ham dataset: ");
		System.out.println(hamFiles.length);
		System.out.print("Files in spam dataset: ");
		System.out.println(spamFiles.length);

		// create a test SpamChecker object and export its model to "model.txt"
		System.out.print("\nCreating SpamChecker using documents in \"dataset/ham\" and \"dataset/spam\" folders...");
		SpamChecker testChecker = new SpamChecker("./dataset/ham", "./dataset/spam");
		System.out.println(" Done\n");	
		System.out.print("Exporting spam checker's vocabulary to \"dataset/model.txt\"...");
		testChecker.exportModelToTextFile("./dataset/model.txt");
		System.out.println(" Done");
		
		File testDocumentsFolder = new File("./testdata/test_documents");
		File[] testDocuments = testDocumentsFolder.listFiles();
		System.out.print("Files in test_documents folder: ");
		System.out.print(testDocuments.length);
		
		try
		{
			File exportFile = new File("./testdata/result.txt");
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			BufferedWriter exportFileBuffer = new BufferedWriter(new FileWriter(exportFile, false));
			
			for(int i = 0; i < testDocuments.length; i++)
			{
				
				StringBuilder line = new StringBuilder();
				line.append(i + 1);
				line.append("   ");
				line.append(testDocuments[i].getName());
				line.append("   ");
				ClassifiedDocument classifiedDocument = testChecker.classifyDocument(testDocuments[i].getAbsolutePath());
				if (classifiedDocument.getHamProbability() > classifiedDocument.getSpamProbability())
				{
					line.append("ham");
				}
				else
				{
					line.append("spam");
				}
				line.append("   ");
				line.append(classifiedDocument.getHamProbability());
				line.append("   ");
				line.append(classifiedDocument.getSpamProbability());
				exportFileBuffer.write(line.toString());
				if (i != (testDocuments.length - 1))
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
		
		try
		{
			Scanner scanner = new Scanner("./testdata/result.txt");
			File exportFile = new File("./testdata/analysis.txt");
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			for (int i = 0; i < testDocuments.length; i++)
			{
				StringBuilder line = new StringBuilder();
				line.append(i + 1);
				line.append("   ");
				line.append(testDocuments[i].getName());
				line.append("   ");
				if (scanner.nextLine().contains("HAM"))
				{
					line.append("ham");
				}
				else
				{
					line.append("spam");
				}
				line.append("   ");
				ClassifiedDocument classifiedDocument = testChecker.classifyDocument(testDocuments[i].getAbsolutePath());
				
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}