package View;
import java.util.ArrayList;


/**
 * Data structure used to merge iteratively nodes (i.e.: points).
 * The nodes merged together are stored in a tree.
 * 
 * Once all the merging is done, the function getClusters() returns an ArrayList<ArrayList<Integer>> which cooresponds to the list of all the trees (each tree is represented by the list of nodes it contains)
 * @author zach
 *
 */
public class Union_find {
	
	Node[] nodes;
	
	public Union_find(int size) {
		
		nodes = new Node[size];
		for(int i = 0 ; i < size ; ++i)
			nodes[i] = new Node(i);
		
	}
	
	/**
	 * Find the root of the tree which contains <n>
	 * @param id id of the node
	 * @return
	 */
	public Node find(int id) {
		
		Node n = nodes[id];
		
		if(n.parent == null)
			return n;
		
		/* Path compression, each time the root of the tree is searched for n, n is directly linked to the root */
		else {
			n.parent = find(n.parent.id);
			return n.parent;
		}
	}
	
	/**
	 * Merge two trees
	 * @param id1
	 * @param id2
	 */
	public void union(int id1, int id2) {
		
		Node n1Root = find(id1);
		Node n2Root = find(id2);

		if(n1Root.id != n2Root.id)
			
			/* If the size of both tree is not the same, set the parent of the root of the smallest tree to the root of the largest tree */
			if(n1Root.rank < n2Root.rank)
				n1Root.parent = n2Root;
			else {
				n2Root.parent = n1Root;
				
				/* If the size of both tree is the same, incremente the rank */
				if(n1Root.rank == n2Root.rank)
					n1Root.rank++;
			}
	
	}

	
	public class Node{
		
		public Node parent = null;
		public int id;
		public int rank = 0;
		
		public Node(int i) {
			this.id = i;
		}
		
	}
	
	
	public ArrayList<ArrayList<Integer>> getClusters(){
		
		/* Find the roots */
		ArrayList<Integer> roots = new ArrayList<Integer>();
		
		for(int i = 0 ; i < nodes.length ; ++i)
			if(nodes[i].parent == null)
				roots.add(i);
		
		/* Create the clusters by putting each node in the arraylist which corresponds to its root */
		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
		
		/* Create empty clusters */
		for(int i = 0 ; i < roots.size() ; ++i)
			clusters.add(new ArrayList<Integer>());
		
		for(int i = 0 ; i < nodes.length ; ++i) {
			
			/* Get the number of the cluster of node <i> (i.e.: the place of the index of the root in <roots> */
			int cluster_nb = roots.indexOf(find(i).id);

			/* Add <i> to this cluster */
			clusters.get(cluster_nb).add(i);
		}
		
		return clusters;
		
	}
	
}


