import java.util.HashMap;
import java.util.Map;

public class TrieNode {

	private final Character ch;
	private final String value;
	private Map<String, TrieNode> children = new HashMap<String, TrieNode>();
	private boolean isWord;

	public TrieNode(char ch, String value) {
		this.ch = ch;
		this.value = value;
	}
	
	/*
	 * Attempts to add a child node. If the child already exist then return false
	 * otherwise add the child to the letter and return true.
	 */
	public boolean addChild(TrieNode child) {
		if (children.containsKey(Character.toString(child.getChar()))) {
			return false;
		}

		children.put(Character.toString(child.getChar()), child);
		return true;
	}
	
	/*
	 * Checks if the child value exists as this node's child
	 */
	public boolean containsChildValue(char c) {
		return children.containsKey(Character.toString(c));
	}

	/*
	 * Returns the value of this node. 
	 */
	public String getValue() {
		return value.toString();
	}
	
	/*
	 * Returns the char value of this node. 
	 */
	public char getChar() {
		return ch;
	}
	
	/*
	 * Returns the child node
	 */
	public TrieNode getChild(char c) {
		return children.get(Character.toString(c));
	}

	/*
	 * Returns if it is a word or not
	 */
	public boolean isWord() {
		return isWord;
	}

	/*
	 * Change the isWord argument to the boolean argument
	 */
	public void setIsWord(boolean argIsWord) {
		isWord = argIsWord;
	}

}