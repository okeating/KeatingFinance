package tries;
/**
 * The trie maps a String to an index value
 * 
 * It does this in the traditional way, by looking
 * at the character of the string.
 * 
 * @author Oliver Keating
 *
 */
public interface Trie {
	int getIndex(String ticker);
}
