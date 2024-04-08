public class UnionFind {
    private int[] parent;
    private int[] rank;

    // Constructor to initialize the union-find structure
    public UnionFind(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;  // Each element is initially its own parent (self loop)
            rank[i] = 0;    // Initial rank of trees is 0
        }
    }

    // Method to find the root of the set containing 'p'
    public int find(int p) {
        if (parent[p] != p) {
            parent[p] = find(parent[p]);  // Path compression heuristic
        }
        return parent[p];
    }

    // Method to unify the sets containing 'p' and 'q'
    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP != rootQ) {
            if (rank[rootP] < rank[rootQ]) {
                parent[rootP] = rootQ;  // Link root of lesser rank under root of greater rank
            } else if (rank[rootP] > rank[rootQ]) {
                parent[rootQ] = rootP;
            } else {
                parent[rootQ] = rootP;  // If equal, link one under the other
                rank[rootP]++;         // Increase the rank of the new root
            }
        }
    }

    // Helper method to check if two elements are in the same set
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }
}
