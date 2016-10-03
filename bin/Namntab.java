import struct.*;
class Namntab
{	HashTable Tabellen;
	static final int Hashsize=13;
	int NextId=1;
	class HashTable
	{	Set[] HTab;
		int Size;
	
		public HashTable(int sz)
		{	Size=sz;
			HTab=new Set[Size];
			for(int i=0;i<Size;i=i+1) HTab[i]=new Set();
		}
		public void insert(Node E) {HTab[E.hash(Size)].Insert(E);}
		public void delete(Node E) {HTab[E.hash(Size)].Delete(E);}
		public Node find(Node E)
		{return (Node)(HTab[E.hash(Size)].SearchElement(E));}
		public void Map(Body B)
		{for(int i=0;i<Size;i=i+1) HTab[i].Map(B);}
	}
	
	class Node extends Element
	{	String Namn;
		int Id;
		Node(String S) {Namn=S; Id=0;}
		Node(String S,int I) {Namn=S; Id=I;}
		int GetCode() {return Id;}
		
		public int hash(int sz)
		{	int sum;
			sum=0;
			for(int i=0; i<Namn.length();i=i+1){sum=sum+(int)(Namn.charAt(i));}
			return sum%sz;
		}
		public boolean Equal(Element E)
		{return E instanceof Node && ((Node)E).Namn.equals(Namn);}
		public boolean Key(Element E) {return Equal(E);}
		public boolean Before (Element E)
		{	if(E instanceof Node)
			{	int I;
				I=((Node)E).Namn.compareTo(Namn);
				return I<0;
			}
			else {return false;}
		}
		public boolean After (Element E) {return !Before(E);}
	}
	
	public Namntab()
	{
		Tabellen=new HashTable(Hashsize);
		Getcode("begin");
		Getcode("end");
		Getcode("array");
		Getcode("function");
		Getcode("integer");
		Getcode("real");
		Getcode("text");
		Getcode("while");
		Getcode("do");
		Getcode("if");
		Getcode("then");
		Getcode("else");
		Getcode("is");
		Getcode("external");
		Getcode("class");
		Getcode("ref");
		Getcode("new");
		Getcode("dynamic");
		Getcode("anytype");
	}
	
	public int Getcode(String S)
	{	Node U;
		U=new Node(S);
		U=Tabellen.find(U);
		if (U!=null) {return U.GetCode();}
		else
		{	Tabellen.insert(new Node(S,NextId));
			NextId=NextId+1;
			return NextId-1;
		}
	}
}
