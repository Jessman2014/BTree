import java.io.File;
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
		
		BTreeNode(long loc) {
			count = 0;
			keys = new int[max];
			children = new long[order];
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
				if(keys[i] > key) return i;
			}
			return count;
		}
		
		private int keyInNode(int key) {
			int i;
			for (i = 0; i < count; i++) {
				if(keys[i] >= key) return i;
			}
			return count;
		}
		
		private boolean inNode(int k) {
			for (int i = 0; i < count; i++) {
				if (keys[i] == k) return true;
			}
			return false;
		}
		
		
		private void insert(int k) {
			if (count != max) {
				int i = count-1;
				while (i >= 0 && keys[i] > k) {
					keys[i+1] = keys[i];
					i--;
				}
				keys[i+1] = k;
				count++;
				writeNode();
				return;
			}
			split = true;
			splitKey = k;
			splitChild = 0;
			split();
			
		}
		
		private void insertNonLeaf(){
			if (count != max) {
				int i = count-1;
				while (i >= 0 && keys[i] > splitKey) {
					keys[i+1] = keys[i];
					children[i+2] = children[i+1];
					i--;
				}
				keys[i+1] = splitKey;
				children[i+2] = splitChild;
				count++;
				writeNode();
				return;
			}
			//add both splitchild and splitkey
			split();
			
		}


		public void split() {
			BTreeNode n;
			long[] newChildren;
			long loc = getFree();
			boolean leaf = children[0] == 0;
			
			int l = locInNode(splitKey);
			int[] newKeys = new int[order];
			int m = 0;
			for (int i = 0; i < newKeys.length; i++) {
				if (i == l) {
					newKeys[i] = splitKey;
					m--;
				}
				else
					newKeys[i] = keys[i+m];
			}
			
			keys = Arrays.copyOfRange(newKeys, 0, min);
			keys = Arrays.copyOf(keys, max);
			
			newKeys = Arrays.copyOfRange(newKeys, min, order);
			newKeys = Arrays.copyOf(newKeys, max);
			splitKey = newKeys[0];
			if (!leaf) {
				int i = 1;
				for (; i <= min; i++) {
					newKeys[i-1] = newKeys[i];
				}
				newKeys[i-1] = 0;
			}
			if (!leaf) {
				newChildren = new long[order+1];
				m = 0;
				l++;
				for (int i = 0; i < newChildren.length; i++) {
					if (i == l) {
						newChildren[i] = splitChild;
						m--;
					}
					else
						newChildren[i] = children[i+m];
				}
				
				children = Arrays.copyOfRange(newChildren, 0, min+1); //add in padding
				children = Arrays.copyOf(children, order);
				newChildren = Arrays.copyOfRange(newChildren, min+1, order+1);
				newChildren = Arrays.copyOf(newChildren, order);
			}
			else {
				newChildren = new long[order];
				newChildren[max] = children[max];
				children[max] = loc;
			}
			count = min;
			writeNode();
			if (leaf)
				n = new BTreeNode(newKeys, newChildren, loc, min+1);
			else
				n = new BTreeNode(newKeys, newChildren, loc, min);
			n.writeNode();
			
			split = true;
			splitChild = loc;
			
			if (stack.empty()) {
				BTreeNode newRoot = new BTreeNode(getFree());
				newRoot.keys[0] = splitKey;
				newRoot.children[0] = location;
				newRoot.children[1] = loc;
				newRoot.count = 1;
				newRoot.writeNode();
				root = newRoot;
				rootAddress = newRoot.location;
				try {
					r.seek(4);
					r.writeLong(rootAddress);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				BTreeNode parent = new BTreeNode(stack.pop());
				parent.readNode();
				parent.insertNonLeaf();
			}
				
		}

		
		private void writeNode() {
			// TODO Auto-generated method stub
			try {
				r.seek(location);
				r.writeInt(count);
				for (int i = 0; i < keys.length; i++) {
					r.writeInt(keys[i]);
				}
				for (int j = 0; j < children.length; j++) {
					r.writeLong(children[j]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void readNode() {
			// TODO Auto-generated method stub
			try {
				r.seek(location);
				count = r.readInt();
				for (int i = 0; i < keys.length; i++) {
					keys[i] = r.readInt();
				}
				for (int i = 0; i < children.length; i++) {
					children[i] = r.readLong();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void print(int level) {
			// TODO Auto-generated method stub
			System.out.println ("Level: " + level);
			System.out.println ("Count: " + count);
			System.out.println ("Location: " + location);
			System.out.print("Keys: ");
			for (int i = 0; i < keys.length; i++) {
				System.out.print (keys[i] + ", ");
			}
			System.out.println();
			System.out.print("Children: ");
			for (int i = 0; i < children.length; i++) {
				System.out.print (children[i] + ", ");
			}
			System.out.println();
		}
	}
	
	BTreeNode root;
	int order, max, min, dataLen, splitKey;
	RandomAccessFile r;
	Stack<Long> stack;
	long rootAddress, splitChild;
	String name;
	boolean split;
	
	
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
		r.writeInt(order);
		rootAddress = 0;
		r.writeLong(rootAddress);
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
		
		order = r.readInt();
		rootAddress = r.readLong();
		setMaxMin();
		dataLen = order*12;
		stack = new Stack<>();
		if(rootAddress != 0) {
			root = new BTreeNode(rootAddress);
			root.readNode();
		}
	}
	
	private void setMaxMin() {
		max = (order-1);
		min = ((int)Math.ceil(order/2.0))-1;
	}
	
	private long getFree() {
		long loc = 0;
		try {
			loc = r.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loc;
	}
	
	public void insert (int k) throws IOException {
		//insert	a	new	key	with	value	k	into	the	tree
		
		if (rootAddress == 0){
			long loc = getFree();
			root = new BTreeNode(loc);
			root.insert(k);
			rootAddress = loc;
			r.seek(4);
			r.writeLong(rootAddress);
			return;
		}
		if (!search(k)) {
			BTreeNode n = new BTreeNode(stack.pop());
			n.readNode();
			n.insert(k);
		}
		split = false;
		stack.clear();
	}
	
	
	public boolean search (int k) {
		//if k	is	in	the	tree	return	true	otherwise	return	false
		if (rootAddress != 0) {
			if (root == null) {
				long l = 12;
				root = new BTreeNode(l);
				root.readNode();
				rootAddress = l;
				try {
					r.seek(4);
					r.writeLong(l);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			root.readNode();
			BTreeNode n = root;
			stack.push(n.location);
			while(n.children[0] != 0) {
				int loc = n.locInNode(k);
				long child = n.children[loc];
				stack.push(child);
				n = new BTreeNode(child);
				n.readNode();
			}
			return n.inNode(k);
		}
		return false;
	}
	
	public void print() {
		if (rootAddress != 0) {
			if(root != null) {
				root.readNode();
				print(root, 0);
			}
			else {
				root = new BTreeNode(rootAddress);
				root.readNode();
				print(root, 0);
			}
		}
	}
	
	private void print(BTreeNode b, int level) {
		b.print(level);
		if (b.children[0] != 0) {
		
			level++;
			for (int i = 0; i <= b.count; i++) {
				BTreeNode n = new BTreeNode(b.children[i]);
				n.readNode();
				print(n, level);
			}
		}
	}
	
	public class BTIterator implements Iterator<Integer> {
		BTreeNode currentLeaf;
		int highKey, index;
		
		
		public BTIterator (int low, int high) {
			//an	iterator	that	can	be	used	to	find	all	the	keys,	k,	in	
		 	//the	tree	such	that	low	<=	k	<=	high
			search(low);
			long loc = stack.pop();
			currentLeaf = new BTreeNode(loc);
			currentLeaf.readNode();
			index = currentLeaf.keyInNode(low);
			highKey = high;
		}
		
		@Override
		public boolean hasNext() {
			if (index >= currentLeaf.count) {
				long nextLoc = currentLeaf.children[max];
				if (nextLoc == 0)
					return false;
				currentLeaf = new BTreeNode(nextLoc);
				currentLeaf.readNode();
				index = 0;
			}
			boolean t = currentLeaf.keys[index] <= highKey;
			return t;
		}

		@Override
		public Integer next() {
			//PRE:	hasNext();
			Integer t = currentLeaf.keys[index++];
			return t;
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
		r.seek(4);
		r.writeLong(rootAddress);
		r.close();
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			BTree m = new BTree("file.txt", 3);
			m.insert(100);
			m.insert(50);
			m.insert(75);
			m.insert(125);
			
			m.insert(25);
			m.insert(30);
			
			m.insert(60);
			m.insert(150);
			m.insert(400);
			
			m.insert(20);
			
			m.insert(300);
			m.insert(500);
			m.insert(40);
			m.insert(70);
			
			
			Iterator<Integer> it = m.iterator(0, 1000);
			while(it.hasNext()) {
				System.out.print(it.next() + ", ");
			}
			m.close();
			
			BTree t = new BTree("file.txt");
			t.print();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
