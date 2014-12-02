import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;


public class BTree {
	
	private class BTreeNode{
		int count;
		int[] keys;
		long[] children;
		long location;
		
		BTreeNode(int k, long loc) throws IOException {
			count = 1;
			keys = new int[max];
			keys[0] = k;
			children = new long[order];
			split = false;
			location = loc;
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
		
		private boolean isLeaf() {
			return children[0] == 0;
		}
		
		private boolean inNode(int k) {
			for (int i = 0; i < count; i++) {
				if (keys[i] == k) return true;
			}
			return false;
		}
		
		private void insert(int k) {
			if (count != max) {
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
				count++;
				writeNode();
				return;
			}
			split = true;
			BTreeNode n = null;
			while(split && !stack.empty()) {
				split(k);
				n = stack.pop();
			}
			if (split && stack.empty()) {
				root = new BTreeNode(splitKey, root.location);
				root.children[0] = n.location;
				root.children[1] = splitChild;
			}
			
		}

		private void writeNode() {
			// TODO Auto-generated method stub
			
		}

		public void split(int k) throws IOException {
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
			if (children[0] != 0) {
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
			}
			else {
				children[max] = 
			}
			count = min;
			writeNode();
			BTreeNode n = new BTreeNode(newKeys, newChildren, r.length(), min+1);
			n.writeNode();
		}

		public long getLoc() {
			// TODO Auto-generated method stub
			return location;
		}

		public int firstKey() {
			// TODO Auto-generated method stub
			return keys[0];
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
	int order, max, min, dataLen, splitKey;
	RandomAccessFile r;
	Stack<BTreeNode> stack;
	long head, splitChild;
	String name;
	boolean split;
	private static final long HEADER = 8;
	
	
	public BTree(String n, int ord) throws IOException {
		//create	a	new	B+Tree	with	order	ord	
		//n	is	the	name	of	the	file	used	to	store	the	tree	
		//if	a	file	with	name	n	already	exists	it	should	be	deleted	
		order = ord;
		split = false;
		setMaxMin();
		stack = new Stack<>();
		name = n;
		File f = new File(n);
		if(f.exists()) f.delete();
		dataLen = order*12;
		r = new RandomAccessFile(name, "rw");
		r.seek(0);
		r.writeInt(dataLen);
		r.writeInt(order);
		head = 0;
		r.writeLong(head);
	}
	
	public BTree (String n) throws IOException {
		//open	an	exis2ng	B+Tree	
		//n	is	the	name	of	the	file	that	stores	the	tree	
		//if	a	file	with	name	n	does	not	exists	throw	a	RunTimeExcep2on	
		split = false;
		File f = new File(n);
		if(!f.exists()) throw new RuntimeException("The	BTree does not exist");
		name = n;
		r = new RandomAccessFile(name, "rw");
		r.seek(0);
		dataLen = r.readInt();
		order = r.readInt();
		head = r.readLong();
		setMaxMin();
		stack = new Stack<>();
	}
	
	private void setMaxMin() {
		max = (order-1);
		min = ((int)Math.ceil(order/2.0))-1;
	}
	
	public void insert (int k) throws IOException {
		//insert	a	new	key	with	value	k	into	the	tree
		
		if (head == 0){
			long loc = r.length();
			root = new BTreeNode(k, loc);
			head = loc;
			r.seek(HEADER);
			r.writeLong(head);
			return;
		}
		search(k);
		BTreeNode n = stack.pop();
		n.insert(k);
		
	}
	
	/*
	private void insert(int k, BTreeNode pop, long newLoc) throws IOException {
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
*/
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
