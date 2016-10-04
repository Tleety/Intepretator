import file.*;
class Symbol
{	/* **** Lay-out of symbol node **** */
	static final int S_Id=1,S_Type=2,S_Kind=3,S_Inf1=4,S_Inf2=5,S_Inf3=6,S_Adr=7;
	static final int S_Npar=S_Inf1,S_Dbn=S_Inf2;
	static final int S_Low=S_Id,S_High=S_Adr;
	static final int S_Size=S_High-S_Low+2;
/* **********************************************************
*			A Symbol Node
*	IntInfo:	
*		_____________
*		| Symbol Id  | The name code (1,2,.....)
*		| Type       | The type (void,int,real,...)
*		| Kind       | The kind (simple, array, func, funcvalue, ...)
*		| Info 1     | Information depending on kind
*		| Info 2     | Information depending on kind
*		| Info 3	    |	Information depending on kind
*		| RelAdr     |	Relative address or zero
*		|____________|
*						Info 1,2,3:s meaning depending on kind:
*						kind=simple|funcval
*							 not used
*						kind=array
*							Info 1: number of indeces
*							Info 2: not used
*							Info 3: qualification(if ref)/0
*						kind=function
*							Info 1: number of parameters
*							Info 2: defining block index
*							Info 3: qualification(if ref)/0
*
*	Text:			Textinfo depending on ...
*		external procedure name | 
*	Limits:				Used by arrays to hold the index limits
*							a linked list of ArrLim:s
*	Tok:					The name token of the symbol,
*							included for error handling purposes only.
******************************************************************** */
	int[]IntInfo=new int[S_Size];
	String Text;
	ArrLim Limits;
	Token Tok;
	/*---------------------------------------------------------
	!	Equal
	! Test if it is this symbol identified by Id.
	!-------------------------------------------------------- */
	public boolean Equal(int Id){return Id==IntInfo[S_Id];}
	/*---------------------------------------------------------
	!	Create
	! Fill the symbol node with some primary information
	! Symbol identification, type, kind of symbol, relative
	! address in AR, Token for error handling.
	!-------------------------------------------------------- */
	public void Create(int Id,int Typ,int Knd,int Adr,Token T)
	{	IntInfo[S_Id]=Id;
		IntInfo[S_Type]=Typ;
		IntInfo[S_Kind]=Knd;
		IntInfo[S_Adr]=Adr;
		Tok=T;
	}
	
	/*---------------------------------------------------------
	!	Set methods
	!	Set:
	!		Any Info field addressed by I may be set to Inf
	!	SetLim:
	!		Create a new limit object and set its low and high
	!		then put the object in the limits list
	!	SetString:
	!		Register an external name
	!-------------------------------------------------------*/
	public void Set(int I,int Inf){IntInfo[I]=Inf;}
	public void SetLim(int I,int L,int H)
	{	Limits=new ArrLim(Limits);
		Limits.Put(I,L,H);
	}
	public void SetString(String T) {Text=T;}
	
	/*---------------------------------------------------------
	!	Access methods
	!--------------------------------------------------------*/
	public int GetType() {return IntInfo[S_Type];}
	public int GetKind() {return IntInfo[S_Kind];}
	public int GetInf1() {return IntInfo[S_Inf1];}
	public int GetInf2() {return IntInfo[S_Inf2];}
	public int GetQual() {return IntInfo[S_Inf3];}
	public int GetRel()  {return IntInfo[S_Adr];}
	public String GetString(){return Text;}
	public int GetLow(int I)
	{	ArrLim P;
		P=Limits.Search(I);
		if(P!=null){return P.Low;} else {return Integer.MIN_VALUE;}
	}
	public int GetHigh(int I)
	{	ArrLim P;
		P=Limits.Search(I);
		if(P!=null){return P.High;} else {return Integer.MIN_VALUE;}
	}
	
	class ArrLim
	{	ArrLim Nxt;
		int Index,Low,High;
		ArrLim(ArrLim N){Nxt=N;}
		void Put(int I,int L,int H)
		{	Index=I;
			Low=L;
			High=H;
		}
		ArrLim Search(int I)
		{	if(I==Index) {return this;}
			else
			if(Nxt==null) {return null;}
			else {return Nxt.Search(I);}
		}
		void OutPut(outfile F)
		{	if(Index!=Integer.MAX_VALUE) {F.Write(Index,3);}
			else{F.Write(" * ");}
			if(Low!=Integer.MAX_VALUE) {F.Write(Low,3);}
			else{F.Write(" * ");}
			if(High!=Integer.MAX_VALUE) {F.Write(High,3);}
			else{F.Write(" * ");}
			if(Nxt!=null){Nxt.OutPut(F);}
		}
	}
	
	public void OutPut(outfile Fil)
	{	for(int I=S_Low; I<=S_High; I=I+1){Fil.Write(IntInfo[I],5);}
		if(Text!=null){Fil.Write(" "+Text+" ");}else{Fil.Write(" ** ");}
		if(Limits==null){Fil.Write(" # ");} else{Limits.OutPut(Fil);}
		Fil.Write("  "+Tok.GetName());
		Fil.WriteLine();
	}
	public void Debug()
	{
		System.out.print("  "+Tok.GetName()+" reladr:"+IntInfo[S_Adr]);
	}
}
