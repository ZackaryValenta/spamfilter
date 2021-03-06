package spamfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Driver
{
	public static void main(String[] args)
	{
		final String TEST_HAM_DATASET      = "./dataset1000/ham";
		final String TEST_SPAM_DATASET     = "./dataset1000/spam";
		final String MODEL_EXPORT_LOCATION = "./dataset1000/model.txt";
		final String IGNORE_WORDS_FILE     = "./dataset1000/stopwords.txt";
		
		// display the number of files in the Ham and Span folders
		File hamFolder = new File(TEST_HAM_DATASET);
		File spamFolder = new File(TEST_SPAM_DATASET);
		File[] hamFiles = hamFolder.listFiles();
		File[] spamFiles = spamFolder.listFiles();
		System.out.print("Files in ham dataset: ");
		System.out.println(hamFiles.length);
		System.out.print("Files in spam dataset: ");
		System.out.println(spamFiles.length);

		// create a test SpamChecker object and export its model to "model.txt"
		System.out.print("\nCreating SpamChecker using documents in " + TEST_HAM_DATASET + " and\n"
				+ TEST_SPAM_DATASET + " folders and ignore-words in " + IGNORE_WORDS_FILE + "...");
		SpamChecker testChecker = new SpamChecker(TEST_HAM_DATASET, TEST_SPAM_DATASET, IGNORE_WORDS_FILE);
		System.out.println(" Done\n");
		
		System.out.print("Exporting spam checker's vocabulary to " + MODEL_EXPORT_LOCATION + "...");
		testChecker.exportModelToTextFile(MODEL_EXPORT_LOCATION);
		System.out.println(" Done");
		
		// use SpamChecker object to classify every document in testdata/test_document
		File testDocumentsFolder = new File("./testdata/test_documents");
		File[] testDocuments = testDocumentsFolder.listFiles();
		System.out.print("\nFiles in testdata/test_documents folder: ");
		System.out.println(testDocuments.length);
		System.out.print("\nClassifying every document in testdata/test_documents folder and\nwriting results to \"testdata/results.txt\"...");
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
		System.out.println("Done");
		
		// measure the accuracy of the SpamChecker and display the results, including correct classification counts,
		// accuracy, and confusion matrix
		int correctHam             = 0;
		int incorrectHam           = 0;
		int correctSpam            = 0;
		int incorrectSpam          = 0;
		try
		{
			Scanner scanner = new Scanner(new BufferedReader(new FileReader("./testdata/result.txt")));
			File exportFile = new File("./testdata/analysis.txt");
			if (!exportFile.exists())
			{
				exportFile.createNewFile();
			}
			BufferedWriter exportFileBuffer = new BufferedWriter(new FileWriter(exportFile, false));
			for (int i = 0; i < testDocuments.length; i++)
			{
				StringBuilder line = new StringBuilder();
				line.append(i + 1);
				line.append("   ");
				line.append(testDocuments[i].getName());
				line.append("   ");
				String str = scanner.nextLine();
				//if the filename starts with HAM
				if (str.contains("HAM"))
				{
					//if string contains ham and therefore we classified it correctly as ham
					if (str.contains("ham"))
					{
						line.append("ham");
						line.append("   ");
						line.append("ham");
						line.append("   ");
						line.append("correct");
						correctHam++;
					}
					//if string contains spam and therefore we classified it incorrectly as spam
					else
					{
						line.append("spam");
						line.append("   ");
						line.append("ham");
						line.append("   ");
						line.append("incorrect");
						incorrectHam++;
					}
				}
				//if the filename starts with SPAM
				else if (str.contains("SPAM"))
				{
					//if string contains spam and therefore we classified it correctly as spam
					if (str.contains("spam"))
					{
						line.append("spam");
						line.append("   ");
						line.append("spam");
						line.append("   ");
						line.append("correct");
						correctSpam++;
					}
					//if string contains ham and therefore we classified it correctly as ham
					else
					{
						line.append("ham");
						line.append("   ");
						line.append("spam");
						line.append("   ");
						line.append("incorrect");
						incorrectSpam++;
					}
				}
				exportFileBuffer.write(line.toString());
				if (i != (testDocuments.length - 1))
				{
					exportFileBuffer.newLine();
				}
				exportFileBuffer.flush();
			}
			exportFileBuffer.close();
			scanner.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		int testDocumentsLength = testDocuments.length;
		System.out.print("\nNumber of documents classified: " + testDocumentsLength + "\n");
		System.out.print("\nNumber of correct classifications: " + (correctHam + correctSpam) + "\n");
		double accuracy = ((double)(correctHam + correctSpam)/testDocumentsLength * 100.0);
		System.out.printf("Accuracy: %.2f%%", accuracy);
		System.out.println();
		System.out.print("Confusion Matrix: \n\n"
				+ "       ham         spam        \n"
				+ "ham    " + correctHam + "         " + incorrectHam + "\n"
				+ "spam   " + incorrectSpam + "          " + correctSpam);
	}
}