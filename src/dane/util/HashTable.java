package dane.util;

/**
 * This class implements a hash table, which maps long keys to values.
 *
 * @author Dane
 */
public final class HashTable {

	private final int capacity;
	private final Node[] nodes;

	/**
	 * Constructs a new, empty hashtable with the specified capacity.
	 *
	 * @param capacity the capacity.
	 */
	public HashTable(int capacity) {
		this.capacity = capacity;
		this.nodes = new Node[capacity];

		for (int i = 0; i < capacity; i++) {
			Node n = nodes[i] = new Node();
			n.previousNode = n;
			n.nextNode = n;
		}
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
	 * key.
	 *
	 * @param key the key whose associated value is to be returned.
	 * @return the value to whic hthe specified key is mapped, or {@code null} if this map contains no mapping for the
	 * key.
	 */
	public Node get(long key) {
		Node node = nodes[(int) (key & (long) (capacity - 1))];
		for (Node n = node.previousNode; n != node; n = n.previousNode) {
			if (n.key == key) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Maps the specified <code>key</code> to the specified <code>node</code> in this hashtable. The node cannot be
	 * <code>null</code>.
	 * <p>
	 *
	 * The value can be retrieved by calling the {@link #get(long) } method with a key that is equal to the original
	 * key.
	 *
	 * @param key the hashtable key.
	 * @param node the node.
	 */
	public void put(long key, Node node) {
		if (node.nextNode != null) {
			node.unlink();
		}
		Node n = nodes[(int) (key & (long) (capacity - 1))];
		node.nextNode = n.nextNode;
		node.previousNode = n;
		node.nextNode.previousNode = node;
		node.previousNode.nextNode = node;
		node.key = key;
	}
}
