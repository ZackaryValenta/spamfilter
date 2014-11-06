package spamfilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
		System.out.println("Done\n");	
		System.out.print("Exporting spam checker's vocabulary to \"dataset/datasetWords.txt\"...");
		testChecker.exportModelToTextFile("./dataset/model.txt");
		System.out.println("Done");
		
		File secondHamFolder = new File("./dataset/test_documents/Ham Test Documents");
		File secondSpamFolder = new File("./dataset/test_documents/Spam Test Documents");
		File[] secondHamFiles = secondHamFolder.listFiles();
		File[] secondSpamFiles = secondSpamFolder.listFiles();
		System.out.print("Files in second ham dataset: ");
		System.out.print(secondHamFiles.length);
		System.out.print("\nFiles in second spam dataset: ");
		System.out.print(secondSpamFiles.length);
		SpamChecker secondTestChecker = new SpamChecker("./dataset/test_documents/Ham Test Documents", "./dataset/test_documents/Spam Test Documents");
		secondTestChecker.exportModelToTextFile("./dataset/secondModel.txt");
		try
		{
			File exportFile = new File("./dataset/result.txt");
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			BufferedWriter exportFileBuffer = new BufferedWriter(new FileWriter(exportFile, false));
			
			for(int i = 0; i < secondHamFiles.length; i++)
			{
				
				StringBuilder line = new StringBuilder();
				line.append(i + 1);
				line.append("   ");
				line.append(secondHamFiles[i].getName());
				line.append("   ");
				ClassifiedDocument doc = secondTestChecker.classifyDocument(secondHamFiles[i].getName());
				if (doc.getHamProbability() > doc.getSpamProbability())
				{
					line.append("ham");
				}
				else
				{
					line.append("spam");
				}
				line.append(doc.hamProbability);
				line.append(doc.spamProbability);
				exportFileBuffer.write(line.toString());
		}
			exportFileBuffer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		
	}
}