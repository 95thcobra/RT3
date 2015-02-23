package dane.util;

/**
 * A linear collection that supports element insertion at the end and removal at the front.
 *
 * @author Dane
 */
public class Deque {

	QueueNode head = new QueueNode();

	/**
	 * Constructs a new empty deque.
	 */
	public Deque() {
		head.nextQueueNode = head;
		head.previousQueueNode = head;
	}

	/**
	 * Inserts the specified node at the end of the deque.
	 *
	 * @param n the node.
	 */
	public void push(QueueNode n) {
		if (n.previousQueueNode != null) {
			n.unlinkQueue();
		}
		n.previousQueueNode = head.previousQueueNode;
		n.nextQueueNode = head;
		n.previousQueueNode.nextQueueNode = n;
		n.nextQueueNode.previousQueueNode = n;
	}

	/**
	 * Retrieves and removes the first element of this deque, or returns {@code null} if this deque is empty.
	 *
	 * @return the first element of this deque, or {@code null} if this deque is empty.
	 */
	public QueueNode pull() {
		QueueNode l = head.nextQueueNode;
		if (l == head) {
			return null;
		}
		l.unlinkQueue();
		return l;
	}
}
