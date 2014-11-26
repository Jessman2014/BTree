import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;


public class BTree {
	
	private class BTreeNode {
		int count;
		int[] keys;
		long[] children;
		boolean split;
		long location;
		
		BTreeNode(int k) {
			count = 1;
			keys = new int[max];
			children = new long[order];
			split = false;
			location = 0;
		}
		
		BTreeNode(int[] k, long[] ch, long loc, int c) {
			count = c;
			keys = k;
			children = ch;
			location = loc;
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
		
		private void insert(int k) {
			int l = locInNode(k);
			int m = 0;
			for (int i = 0; i < keys.length; i++) {
				if (i == l) {
					keys[i] = k;
					m--;
				}
				else
					keys[i] = keys[i+m];
			}
			count--;
		}

		public BTreeNode split(int k, long newLoc) {
			// TODO Auto-generated method stub
			int l = locInNode(k);
			int[] newKeys = new int[order];
			int m = 0;
			for (int i = 0; i < newKeys.length; i++) {
				if (i == l) {
					newKeys[i] = k;
					m--;
				}
				else
					newKeys[i] = keys[i+m];
			}
			keys = Arrays.copyOfRange(newKeys, 0, min);
			newKeys = Arrays.copyOfRange(newKeys, min, order);
			long[] newChildren = new long[order+1];
			m = 0;
			for (int i = 0; i < newChildren.length; i++) {
				if (i == l) {
					newChildren[i] = newLoc;
					m--;
				}
				else
					newChildren[i] = children[i+m];
			}
			children = Arrays.copyOfRange(newChildren, 0, min+1);
			newChildren = Arrays.copyOfRange(newChildren, min+1, order+1);
			count = min;
			return new BTreeNode(newKeys, newChildren, 0, min+1);
		}

		public long getLoc() {
			// TODO Auto-generated method stub
			return location;
		}

		public int firstKey() {
			// TODO Auto-generated method stub
			return keys[0];
		}

		public void setSplit() {
			// TODO Auto-generated method stub
			split = true;
		}

		public boolean getSplit() {
			return split;
		}
		
		public void shiftArrayLeft() {
			// TODO Auto-generated method stub
			keys = Arrays.copyOfRange(keys, 1, count);
		}

		public long getLink() {
			// TODO Auto-generated method stub
			return children[order-1];
		}

		public boolean atEnd(int i) {
			// TODO Auto-generated method stub
			return i < count-1;
		}

		public int getKey(int index) {
			// TODO Auto-generated method stub
			return keys[index];
		}
	}
	
	BTreeNode root;
	int order, max, min;
	RandomAccessFile r;
	Stack<BTreeNode> stack;
	private final int DATALEN = 4;
	long head;
	
	
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
		r.seek(0);
		r.
		
		
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
		max = (order-1);
		min = ((int)Math.ceil(order/2.0))-1;
	}
	
	public void insert (int k) {
		//insert	a	new	key	with	value	k	into	the	tree
		if(!search(k)) {
			if (stack.empty())
				root = new BTreeNode(k);
			insert(k, stack.pop(), 0);
		}
	}
	
	
	private void insert(int k, BTreeNode pop, long newLoc) {
		if (pop.getSplit()) {
			if(pop.isFull()) {
				BTreeNode newChild = pop.split(k, newLoc);
				if (stack.empty()) {
					int rootKey = newChild.firstKey(); 
					newChild.shiftArrayLeft();
					int[] rootKeys = new int[max];
					rootKeys[0] = rootKey;
					long[] rootChildren = new long[order];
					rootChildren[0] = pop.getLoc();
					rootChildren[1] = newChild.getLoc();
					root = new BTreeNode(rootKeys, rootChildren, 0, 1);
				}
				else {
					BTreeNode n = stack.pop();
					n.setSplit();
					insert(newChild.firstKey(), n, newChild.getLoc());
				}
			}
			else 
				pop.insert(k);
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
		BTreeNode currentLeaf;
		int highKey, index;
		
		
		public BTIterator (int low, int high) {
			//an	iterator	that	can	be	used	to	find	all	the	keys,	k,	in	
		 	//the	tree	such	that	low	<=	k	<=	high
			search(low);
			currentLeaf = stack.pop();
			index = currentLeaf.locInNode(low);
			highKey = high;
		}
		
		@Override
		public boolean hasNext() {
			if (currentLeaf.atEnd(index)) {
				long nextLoc = currentLeaf.getLink();
				if (nextLoc == 0)
					return false;
				currentLeaf = readNode(nextLoc);
				index = 0;
			}
			return currentLeaf.getKey(index) <= highKey;
		}

		@Override
		public Integer next() {
			//PRE:	hasNext();
			return currentLeaf.getKey(index++);
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
