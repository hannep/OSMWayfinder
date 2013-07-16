public class QueueEntry implements Comparable {
	double distance;
	GraphNode graphNode;

	public QueueEntry(GraphNode graphNode, double distance) {
		this.distance = distance;
		this.graphNode = graphNode;
	}

	@Override
	public int compareTo(Object arg0) {
		QueueEntry other = (QueueEntry) arg0;
		if (this.distance < other.distance) {
			return -1;
		} else if (this.distance > other.distance) {
			return 1;
		} else
			return 0;
	}

}