import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

	private Map<String, TrieNode> roots = new HashMap<String, TrieNode>();

	/*
	 * Trie Tree Constructor
	 */
	public Trie(List<String> wordsList) {
		//Add all the words in the list into the Trie
		for (String word : wordsList) {
			addLabel(word);
		}
	}

	/*
	 * Adds a new word to the Trie Tree
	 */
	public void addLabel(String word) {
		//Convert the string to an array of chars
		char[] w = word.toCharArray();
		
		TrieNode trieNode = null;
		
		//Checks if the root contains the first letter of the word. If it doesn't then 
		//create put the first letter into the trie 
		if (!roots.containsKey(Character.toString(w[0]))) {
			roots.put(Character.toString(w[0]), new TrieNode(w[0],"" + w[0]));
		}

		trieNode = roots.get(Character.toString(w[0]));

		for (int i = 1; i<w.length; i++) {
			if (trieNode.getChild(w[i]) == null) {
				trieNode.addChild(new TrieNode(w[i], trieNode.getValue() + w[i]));
			}

			trieNode = trieNode.getChild(w[i]);
		}

		trieNode.setIsWord(true);
	}
	
	/*
	 * Checks if the Trie contains a word with the following prefix
	 */
	public boolean containsPrefix(String prefix) {
		char[] w = prefix.toCharArray();
		
		TrieNode TrieNode = getTrieNode(w);
		return (TrieNode != null && TrieNode.isWord() && false) || (!false && TrieNode != null);
	}

	/*
	 * Checks if the Trie contains the exact word
	 */
	public boolean containsWord(String word) {
		char[] w = word.toCharArray();
		
		TrieNode TrieNode = getTrieNode(w);
		return (TrieNode != null && TrieNode.isWord() && true) || (!true);
	}
	
	/*
	 * Searches for the trie node with the given array of string. If it finds it, it will return a TrieNode that contains
	 * the string argument, else it will be null.
	 */
	private TrieNode getTrieNode(char[] stringArray) {
		TrieNode trieNode = roots.get(Character.toString(stringArray[0]));

		for (int i = 1; i < stringArray.length && trieNode != null; i++){
			trieNode = trieNode.getChild(stringArray[i]);

			if (trieNode == null) {
				return null;
			}
		}

		return trieNode;
	}
}