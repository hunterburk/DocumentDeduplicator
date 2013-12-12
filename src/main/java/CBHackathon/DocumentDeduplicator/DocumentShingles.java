package CBHackathon.DocumentDeduplicator;

import java.util.HashSet;

public class DocumentShingles {
	public String documentID;
	public HashSet<Long> shingles;
	
	public DocumentShingles(String docID, HashSet<Long> shingles){
		this.documentID = docID;
		this.shingles = shingles;
	}
}
