import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Stack;


public class BTree {
	
	private class BTreeNode {
		int count;
		int[] keys;
		long[] children;
		boolean split;
		long location;
		
		BTreeNode(int ord, int c, long loc) {
			count = c;
			keys = new int[M];
			children = new long[ord];
			split = false;
			location = loc;
		}
		
		private void splitNonRoot() {
			
		}
		
		private void splitRoot() {
			
		}
		
		private int locInNode(int key) {
			int i;
			for (i = 0; i < count; i++) {
				if(keys[i] >= key) return i;
			}
			return count;
		}
		
		private long getChild(int key) {
			return children[key];
		}
		
		private boolean isLeaf() {
			return children[0] == 0;
		}
		
		private boolean inNode(int k) {
			for (int i = 0; i < count; i++) {
				if (keys[i] == k) return true;
			}
			return false;
		}
		
		private boolean isFull() {
			return count == keys.length;
		}
		
	}
	
	BTreeNode root;
	int order;
	RandomAccessFile r;
	private final int M;
	private final int N;
	Stack<BTreeNode> stack;
	
	
	public BTree(String n, int ord) throws IOException {
		//create	a	new	B+Tree	with	order	ord	
		//n	is	the	name	of	the	file	used	to	store	the	tree	
		//if	a	file	with	name	n	already	exists	it	should	be	deleted	
		order = ord;
		setMaxMin();
		stack = new Stack<>();
		
		
		File f = new File(n);
		if(f.exists()) f.delete();
		
		r = new RandomAccessFile(f, "rw");
		
		
		
	}
	
	public BTree (String n) throws FileNotFoundException {
		//open	an	exis2ng	B+Tree	
		//n	is	the	name	of	the	file	that	stores	the	tree	
		//if	a	file	with	name	n	does	not	exists	throw	a	RunTimeExcep2on	
		File f = new File(n);
		if(!f.exists()) throw new RuntimeException();
		r = new RandomAccessFile(f, "rw");
		
	}
	
	private void setMaxMin() {
		M = (order-1);
		N = ((int)Math.ceil(order/2.0))-1;
	}
	
	public void insert (int k) {
		//insert	a	new	key	with	value	k	into	the	tree
		if(!search(k)) {
			while(!stack.empty()) {
				
			}
		}
		
		
	}
	
	
	public boolean search (int k) {
		//if k	is	in	the	tree	return	true	otherwise	return	false
		BTreeNode n = root;
		stack.push(n);
		while(!n.isLeaf()) {
			int loc = n.locInNode(k);
			n = readNode(n.getChild(loc));
			stack.push(n);
		}
		return n.inNode(k);
		
	}
	
	public class BTIterator implements Iterator<Integer> {
		BTreeNode n = root;
		
		public BTIterator (int low, int high) {
			//an	iterator	that	can	be	used	to	find	all	the	keys,	k,	in	
		 	//the	tree	such	that	low	<=	k	<=	high
			
			
			
		}
		
		
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Integer next() {
			//PRE:	hasNext();
			return null;
		}
		public void remove () {
			//op2onal	method	not	implemented	
		}
	}
	
	
	public Iterator<Integer> iterator (int low, int high) {
		//return	a	new	iterator	object	
		
		return new BTIterator(low, high);
	}
	
	
	public void close() throws IOException {
		r.close();
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
