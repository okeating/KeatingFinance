package tries;
/**
 * Class representing a Node in the Trie.
 * 
 * Public constructor is used to create the node. 
 * 
 * When constructed the ticker can be used to find the index that it was in the array.
 * 
 * Ultimately it is not as fast as java.util.HashMap so it is here for interest purposes
 * 
 * @author Oliver Keating
 *
 */
public class TrieNode implements Trie {

	final private int depth;
	final private Trie[] tries = new Trie[27];
	
	
	public TrieNode(String[] listOfTickers){
		depth=0;
		for (int i = 0 ; i < listOfTickers.length ; i++){
			processTicker(listOfTickers[i],i);
		}
		System.out.println(" Trie node with "+countTotalTrieNodes()+" nodes!");
		System.out.println(" Trie node with "+countTotalLeaves()+" leaves!");
		System.out.println(" Trie node with "+countTotalTrie()+"  total!");
	}
	/**
	 * Private constructor used internally
	 * @param depth
	 */
	private TrieNode(int depth){
		this.depth=depth;
	}
	
	
	private void processTicker(String ticker, int tickerIndex) {

		char c = ticker.charAt(depth);
		int trieIndex = c-64;
		if (depth>=(ticker.length()-1)){
			apppendTrieLeaf(trieIndex,tickerIndex);
		} else {
			appendTrieNode(trieIndex,tickerIndex,ticker);
		}

		
	}
	public int	countTotalTrie(){
		int total =0;
		for (int i=0; i < tries.length ; i++){
			Trie trie = tries[i];
			if (trie instanceof TrieNode){
				total++;
				total+= ((TrieNode)trie).countTotalTrie();
			} else if (trie instanceof TrieLeaf){
				total++;
			}
		}
		return total;
	}
	
	
	public int countTotalTrieNodes(){
		int total =0;
		for (int i=0; i < tries.length ; i++){
			Trie trie = tries[i];
			if (trie instanceof TrieNode){
				total++;
				total+= ((TrieNode)trie).countTotalTrieNodes();
			}
		}
		return total;
	}
	public int countTotalLeaves(){
		int total =0 ;
		for (int i =0 ; i < tries.length ; i++){
			Trie trie = tries[i];
			if (trie instanceof TrieNode){
				total+= ((TrieNode)trie).countTotalLeaves();
			} else if (trie instanceof TrieLeaf){
				total++;
			}
		}
		return total;
	}
	
	
	

	private void apppendTrieLeaf(int trieIndex, int tickerIndex) {
		Trie trie = tries[trieIndex];
		if (trie instanceof TrieNode){
			((TrieNode) trie).tries[0]=new TrieLeaf(tickerIndex);
		} else {
			tries[trieIndex]= new TrieLeaf(tickerIndex);
		}
		
		
		
	}

	private void appendTrieNode(int trieIndex, int tickerIndex, String ticker) {
		TrieNode node = getTrieNode(trieIndex);
		node.processTicker(ticker, tickerIndex);
	}
	
	private TrieNode getTrieNode(int trieIndex){
		Trie trie = tries[trieIndex];
		if (trie == null){
			trie = new TrieNode(depth+1);
			tries[trieIndex]=trie;
		} else if (trie instanceof TrieLeaf){
			TrieNode trieNode = new TrieNode(depth+1);
			trieNode.tries[0] = trie;
			tries[trieIndex]=trieNode;
			trie = trieNode;
		} 
		
		return (TrieNode) trie;
	}

	@Override
	public int getIndex(String ticker) {
		String s= ticker.toString();
		if (s.length() == depth){
			return tries[0].getIndex(ticker);
		}
		char c = s.charAt(depth);
		int index = c -64;
		return tries[index].getIndex(ticker);
	}
	


}
