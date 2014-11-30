package sentiment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class FeatureSelection {

	public static void main(String[] args) throws IOException {

		/* programma pou afinei mono tis pio simantikes le3is tis eisodou. 
		 * o xristis dinei poses le3is thelei na afisei kai ti methodo pou
		 * thelei na epile3i. kalutera na dinete to keimeno meta to analyze
		 * epeidi an den ginei analyze apaitei para poly ram
		 */
		
		if (args.length != 4) {
			System.err.println("Arguments: [input file] [output file] [maxWords] [method]");
			return;
		}
		
		String path = args[0];
		String pathOutput = args[1];

		int method = Integer.parseInt(args[2]); // 0:mi, 1:chi, 2:ig 3:bi
		int maxWords = Integer.parseInt(args[3]);

		BufferedReader src = null;
		src = new BufferedReader(new FileReader(path));

		Hashtable<String, Integer> vocabulary = new Hashtable<String, Integer>();
		Hashtable<String, Integer> wordDocuments1 = new Hashtable<String, Integer>();
		Hashtable<String, Integer> wordDocuments2 = new Hashtable<String, Integer>();
		Hashtable<String, Integer> check = new Hashtable<String, Integer>();

		int documents1 = 0;
		int documents2 = 0;

		String line = "";

		while ((line = src.readLine()) != null) {

			char category = line.charAt(0);
			String[] words = line.split("\\s+");
			check.clear();

			if (category == '1' || category == '2') {
				documents1++;

				for (int i = 1; i < words.length; i++) {
					String word = words[i];
					if (word.length() == 0) {
						continue;
					}

					vocabulary.put(word, new Integer(0));

					Integer occurences = wordDocuments1.get(word);
					if (occurences == null) {
						occurences = 1;
					} else {
						if (!check.containsKey(word)) {
							occurences++;
						}
					}
					wordDocuments1.put(word, occurences);

					check.put(word, new Integer(0));
				}
			} else if (category == '4' || category == '5') {
				documents2++;

				for (int i = 1; i < words.length; i++) {
					String word = words[i];
					if (word.length() == 0) {
						continue;
					}

					vocabulary.put(word, new Integer(0));

					Integer occurences = wordDocuments2.get(word);
					if (occurences == null) {
						occurences = 1;
					} else {
						if (!check.containsKey(word)) {
							occurences++;
						}
					}
					wordDocuments2.put(word, occurences);

					check.put(word, new Integer(0));
				}
			}
		}

		src.close();

		Enumeration<String> vocEnumeration = vocabulary.keys();

		double n10, n11, n00, n01;

		LinkedList<MyType> list1 = new LinkedList<MyType>();
		LinkedList<MyType> list2 = new LinkedList<MyType>();
		Hashtable<String, Integer> array = new Hashtable<String, Integer>();

		int nonBoundary = 0;

		while (vocEnumeration.hasMoreElements()) {
			String word = vocEnumeration.nextElement();

			Integer numDocuments1 = wordDocuments1.get(word);
			Integer numDocuments2 = wordDocuments2.get(word);

			if (numDocuments1 == null) {
				n11 = 0;
			} else {
				n11 = numDocuments1.intValue();
			}

			if (numDocuments2 == null) {
				n10 = 0;
			} else {
				n10 = numDocuments2.intValue();
			}

			n00 = documents2 - n10;
			n01 = documents1 - n11;

			FeatureSelectionMetrics fsm = new FeatureSelectionMetrics(n11, n01,
					n10, n00);

			Double featureValue = null;

			if (method == 0) {
				featureValue = fsm.getMI();
			} else if (method == 1) {
				featureValue = fsm.getChiSquare();
			} else if (method == 2) {
				featureValue = fsm.getIG();
			} else if (method == 3) {
				featureValue = fsm.getBiNormal();
			}

			if (featureValue != null) {
				nonBoundary++;
				list1.add(new MyType(word, featureValue));
			} else { // boundary cases
				if (n11 == 0)
					list2.add(new MyType(word, n10 / documents2));
				else if (n10 == 0)
					list2.add(new MyType(word, n11 / documents1));
			}
		}

		System.out.println("Vocabulary has " + vocabulary.size() + " words!");
		System.out.println("Non boundary cases: " + nonBoundary);

		Collections.sort(list1);
		Iterator<MyType> iterator = list1.iterator();

		int maxWordsTemp = maxWords;
		while (iterator.hasNext() && maxWordsTemp > 0) {
			MyType my = iterator.next();
			array.put(my.token, new Integer(0));
			maxWordsTemp--;
		}

		Collections.sort(list2);
		iterator = list2.iterator();

		while (iterator.hasNext() && maxWordsTemp > 0) {
			MyType my = iterator.next();
			array.put(my.token, new Integer(0));
			maxWordsTemp--;
		}

		src = new BufferedReader(new FileReader(path));
		BufferedWriter dst = null;
		dst = new BufferedWriter(new FileWriter(pathOutput, false));

		String newLine;
		boolean erase;
		while ((line = src.readLine()) != null) {

			String[] words = line.split("\\s+");
			newLine = words[0] + "\t";
			erase = true;

			for (int i = 1; i < words.length; i++) {
				String word = words[i];
				if (array.containsKey(word)) {
					erase = false;
					newLine += word + " ";
				}
			}
			if (!erase) {
				dst.append(newLine + "\n");
			}
		}
		src.close();
		dst.close();

		System.out.println("Program has finished successfully!");
	}

}
