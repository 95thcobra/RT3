package dane.util;

public class QueueNode extends Node {

	QueueNode nextQueueNode;
	QueueNode previousQueueNode;

	public void unlinkQueue() {
		if (previousQueueNode != null) {
			previousQueueNode.nextQueueNode = nextQueueNode;
			nextQueueNode.previousQueueNode = previousQueueNode;
			nextQueueNode = null;
			previousQueueNode = null;
		}
	}
}
