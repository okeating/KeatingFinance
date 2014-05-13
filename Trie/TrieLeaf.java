package tries;
/**
 * The trieLeaf contains a single value
 * @author Oliver Keating
 *
 */
public class TrieLeaf implements  Trie{
	
	private final int value;
	
	public TrieLeaf(int value) {
		this.value=value;
	}

	@Override
	public int getIndex(String ticker) {
		return value;
	}

	


}
