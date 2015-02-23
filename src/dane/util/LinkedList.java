package dane.util;

/**
 * Doubly-linked list wrap of the {@code HashTable} and {@code Deque} classes.
 *
 * @author Dane
 */
public class LinkedList {

	private final int capacity;
	private int available;
	private final HashTable table = new HashTable(1024);
	private final Deque deque = new Deque();

	public LinkedList(int capacity) {
		this.capacity = capacity;
		this.available = capacity;
	}

	public QueueNode get(long key) {
		QueueNode n = (QueueNode) this.table.get(key);

		if (n != null) {
			this.deque.push(n);
		}

		return n;
	}

	public void put(long key, QueueNode node) {
		if (this.available == 0) {
			QueueNode n = this.deque.pull();
			n.unlink();
			n.unlinkQueue();
		} else {
			this.available--;
		}
		this.table.put(key, node);
		this.deque.push(node);
	}

	public void clear() {
		for (;;) {
			QueueNode n = this.deque.pull();
			if (n == null) {
				break;
			}
			n.unlink();
			n.unlinkQueue();
		}
		this.available = this.capacity;
	}
}
