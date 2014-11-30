package sentiment;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.TF;
import org.apache.mahout.common.*;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class Classifier {
	
    public final static String MODEL_PATH_CONF = "/user/root/mahout-work/model";
    public final static String DICTIONARY_PATH_CONF = "/user/root/mahout-work/reviews-vectors/dictionary.file-0";
    public final static String DOCUMENT_FREQUENCY_PATH_CONF = "/user/root/mahout-work/reviews-vectors/df-count/part-r-00000";
    
    private final static String HADOOP_CONF = "/opt/hadoop-1.1.2/conf/"; 
 
    private static StandardNaiveBayesClassifier classifier;
    private static Map<String, Integer> dictionary;
    private static Map<Integer, Long> documentFrequency;
    private static Analyzer analyzer;
 
    public Classifier(Configuration configuration) throws IOException{
    	configuration.addResource(new Path(HADOOP_CONF+"hdfs-site.xml"));
    	configuration.addResource(new Path(HADOOP_CONF+"core-site.xml"));
    	configuration.addResource(new Path(HADOOP_CONF+"mapred-site.xml"));
   
        dictionary = readDictionnary(configuration, new Path(DICTIONARY_PATH_CONF));
        
        analyzer = new SimpleAnalyzer(Version.LUCENE_43);

        NaiveBayesModel model = NaiveBayesModel.materialize(new Path(MODEL_PATH_CONF), configuration);
       
        classifier = new StandardNaiveBayesClassifier(model);
    }
 
    public int classify(String text) throws IOException {
        int documentCount = documentFrequency.get(-1).intValue();
 
        Multiset<String> words = ConcurrentHashMultiset.create();
 
        // extract words from tweet
        
        TokenStream ts = analyzer.tokenStream("text", new StringReader(text));
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        int wordCount = 0;
        while (ts.incrementToken()) {
            if (termAtt.length() > 0) {
                String word = ts.getAttribute(CharTermAttribute.class).toString();
                Integer wordId = dictionary.get(word);
                // if the word is not in the dictionary, skip it
                if (wordId != null) {
                    words.add(word);
                    wordCount++;
                }
            }
        }
 
        // create vector wordId => weight using tf
        Vector vector = new RandomAccessSparseVector(10000);
        TF tf = new TF();
        for (Multiset.Entry<String> entry:words.entrySet()) {
            String word = entry.getElement();
            int count = entry.getCount();
            Integer wordId = dictionary.get(word);
            Long freq = documentFrequency.get(wordId);
            double tfValue = tf.calculate(count, freq.intValue(), wordCount, documentCount);
            vector.setQuick(wordId, tfValue);
        }
        // With the classifier, we get one score for each label
        // The label with the highest score is the one the tweet is more likely to
        // be associated to
        Vector resultVector = classifier.classifyFull(vector);
        double bestScore = -Double.MAX_VALUE;
        int bestCategoryId = -1;
        for(Vector.Element element: resultVector.all()) {
            int categoryId = element.index();
            double score = element.get();
            if (score > bestScore) {
                bestScore = score;
                bestCategoryId = categoryId;
            }
        }
 
        return bestCategoryId;
    }
 
    private static Map<String, Integer> readDictionnary(Configuration conf, Path dictionnaryPath) {
        Map<String, Integer> dictionnary = new HashMap<String, Integer>();
        for (Pair<Text, IntWritable> pair : new SequenceFileIterable<Text, IntWritable>(dictionnaryPath, true, conf)) {
            dictionnary.put(pair.getFirst().toString(), pair.getSecond().get());
        }
        return dictionnary;
    }
 
    private static Map<Integer, Long> readDocumentFrequency(Configuration conf, Path documentFrequencyPath) {
        Map<Integer, Long> documentFrequency = new HashMap<Integer, Long>();
        for (Pair<IntWritable, LongWritable> pair : new SequenceFileIterable<IntWritable, LongWritable>(documentFrequencyPath, true, conf)) {
            documentFrequency.put(pair.getFirst().get(), pair.getSecond().get());
        }
        return documentFrequency;
    }
 
}
