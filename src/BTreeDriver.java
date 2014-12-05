import java.io.*;
import java.util.*;

public class BTreeDriver {

	public static void main(String args[]) throws IOException{

		BTree b1 = new BTree(args[0], 5);
		BTree b2 = new BTree(args[1],11);

		BufferedReader r = new BufferedReader(new FileReader(args[2]));

		String line = r.readLine();
		while (line != null) {
			int k = Integer.parseInt(line);
			b1.insert(k);
			b2.insert(k);

			line = r.readLine();
		}
		b2.print();

		b1.close();
		b2.close();

		b1 = new BTree(args[0]);
		b2 = new BTree(args[1]);

		System.out.println("Enter i for insert, s for search, r for range search or q to quit");
		System.out.print("-->");
		

		Scanner s = new Scanner(System.in);
		String c = s.next();
		while (!c.equals("q")) {
			if (c.equals("i")) {
				int k = s.nextInt();
				b1.insert(k);
				b2.insert(k);
			}
			else if (c.equals("s")) {
				int k = s.nextInt();
				System.out.println("b1: "+b1.search(k));
				System.out.println("b2: "+b2.search(k));
			}
			else {
				int k1 = s.nextInt();
				int k2 = s.nextInt();
				Iterator<Integer> it1 = b1.iterator(k1, k2);
				Iterator<Integer> it2 = b2.iterator(k1, k2);
				System.out.println("b1 range: "+k1+":"+k2);
				while (it1.hasNext()) 
					System.out.println(""+it1.next());
				System.out.println("b2 range: "+k1+":"+k2);
				while (it2.hasNext()) 
					System.out.println(""+it2.next());
			}
			System.out.println("Enter i for insert, s for search, r for range search or q to quit");
			System.out.print("-->");

			c = s.next();
		}
		r.close();
		s.close();
	}
	
}
