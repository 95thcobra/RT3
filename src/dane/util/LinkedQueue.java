package dane.util;

public final class LinkedQueue {

	Node head = new Node();
	Node selected;

	public LinkedQueue() {
		head.previousNode = head;
		head.nextNode = head;
	}

	public void push(Node n) {
		if (n.nextNode != null) {
			n.unlink();
		}
		n.nextNode = head.nextNode;
		n.previousNode = head;
		n.nextNode.previousNode = n;
		n.previousNode.nextNode = n;
	}

	public Node poll() {
		Node n = head.previousNode;
		if (n == head) {
			return null;
		}
		n.unlink();
		return n;
	}

	public Node peekLast() {
		Node n = head.previousNode;
		if (n == head) {
			selected = null;
			return null;
		}
		selected = n.previousNode;
		return n;
	}

	public Node peekFirst() {
		Node n = head.nextNode;
		if (n == head) {
			selected = null;
			return null;
		}
		selected = n.nextNode;
		return n;
	}

	public Node getPrevious() {
		Node n = selected;
		if (n == head) {
			selected = null;
			return null;
		}
		selected = n.previousNode;
		return n;
	}

	public Node getNext() {
		Node n = selected;
		if (n == head) {
			selected = null;
			return null;
		}
		selected = n.nextNode;
		return n;
	}

	public int size() {
		int i = 0;
		for (Node n = head.previousNode; n != head; n = n.previousNode) {
			i++;
		}
		return i;
	}

	public void clear() {
		for (;;) {
			Node n = head.previousNode;
			if (n == head) {
				break;
			}
			n.unlink();
		}
	}
}
