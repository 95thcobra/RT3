package dane.util;

public class Node {

	long key;
	Node previousNode;
	Node nextNode;

	public void unlink() {
		if (nextNode != null) {
			nextNode.previousNode = previousNode;
			previousNode.nextNode = nextNode;
			previousNode = null;
			nextNode = null;
		}
	}
}
