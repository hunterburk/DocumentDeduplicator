package CBHackathon.DocumentDeduplicator;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
 
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import com.google.common.collect.Sets;
import com.planetj.math.rabinhash.RabinHashFunction64;
 
 
/**
* Hello world!
*
*/
public class LuceneShingles
{
	public static int SHINGLE_MIN = 4;
	public static int SHINGLE_MAX = SHINGLE_MIN;
	
    public static void main( String[] args )
    {
    	if (args.length < 1) {
    		System.err.println("Specify file path!!");
    		System.exit(-1);
    	}
    	
        try {
            File filePath = new File(args[0]);
            ArrayList<DocumentShingles> documentShingles = new ArrayList<DocumentShingles>();			//see if HashSet affects performance
            																								//on jacard similarity
            for (File file : listFilesForFolder(filePath)){
            	DocumentShingles docShingles = new DocumentShingles(file.getName(), getShingleSet(file));
            	documentShingles.add(docShingles);
            }
            
            for (int i = 0; i < documentShingles.size(); i++){
            	for (int j = i + 1; j < documentShingles.size(); j++){
            		DocumentShingles doc1 = documentShingles.get(i);
            		DocumentShingles doc2 = documentShingles.get(j);
            		System.out.println(String.format("%s : %s = %.2f", doc1.documentID, doc2.documentID, 
            				getJaccardSimilarity(doc1.shingles, doc2.shingles)));
            	}
            }
 
        }
 
        catch (Throwable t) {
            t.printStackTrace();
        }
 
    }
    
    private static ArrayList<File> listFilesForFolder(final File filePath) {
    	ArrayList<File> pathFiles = new ArrayList<File>();
    	
    	if (filePath.isDirectory()) {
    		for (final File fileEntry : filePath.listFiles()) {
    			if (fileEntry.isDirectory()) {
    				listFilesForFolder(fileEntry);
    			} else {
    				pathFiles.add(fileEntry);
    			}
    		}
    	} else {
    		pathFiles.add(filePath);
    	}
        
        return pathFiles;
    }
    
    private static HashSet<Long> getShingleSet(File inputFile){
    	
    	StandardAnalyzer analyzer = null;
    	ShingleAnalyzerWrapper shingleAnalyzer = null;
    	TokenStream stream = null;
    	HashSet<Long> shingleSet = new HashSet<Long>();
    	
    	try {
    		//SimpleAnalyzer analyzer = new SimpleAnalyzer(Version.LUCENE_46);
    		analyzer = new StandardAnalyzer(Version.LUCENE_46, CharArraySet.EMPTY_SET); //dont ignore stop words
            FileReader reader = new FileReader(inputFile);

            shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, SHINGLE_MIN, SHINGLE_MAX, " ", false, false); //(analyzer, 2, 2, );
            //shingleAnalyzer. setOutputUnigrams(false);

            stream = shingleAnalyzer.tokenStream("contents", reader);
            CharTermAttribute charTermAttribute = stream.getAttribute(CharTermAttribute.class);        

            RabinHashFunction64 rhash = RabinHashFunction64.DEFAULT_HASH_FUNCTION;
        
            stream.reset();
            while (stream.incrementToken()){
            	System.out.println(String.format("%s : %s", charTermAttribute.toString(), rhash.hash(charTermAttribute.toString())));
            	shingleSet.add(rhash.hash(charTermAttribute.toString()));
            }
        
    	} catch (Throwable t) {
    		t.printStackTrace();
    	} finally {
    		try {
				stream.end();
				stream.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
            
            shingleAnalyzer.close();
    	}
    	
    	return shingleSet;
    }
    
    public static float getJaccardSimilarity(Set<Long> shinglesA, Set<Long> shinglesB) {
    	float numerator = Sets.intersection(shinglesA, shinglesB).size();
    	float denominator = Sets.union(shinglesA, shinglesB).size();
    	return numerator / denominator;
    }
}
