import komp.*;
/* **************************************************************************
		This class is the real expert on Operator Precedence and is a subclass
		of the abstract class OperatorPrecedence in the package komp.
		This superclass is implementing the common features of Op.prec.
		and here we have to add the language specific parts.
************************************************************************** */
class OpPrec extends OperatorPrecedence
{
   static final int AM_Become=1,AM_New=2,AM_Add=3,AM_Mul=4,AM_Cmp=5,AM_Func=6;
   static final int AM_Array=7,AM_Class=8,AM_Comma=9,AM_Lpar=10,AM_Rpar=11;
   static final int AM_Fpar=12,AM_Dot=13,AM_Empty=14;
   static final int K_Simple=0,K_Array=1,K_Func=2,K_Funcval=3,K_Class=4,K_Ref=5;
   static final int TC_Plus=13,TC_Minus=14,TC_Mul=15,TC_Div=16;
   static final int C_Asis=0,C_Any=0,C_Void=0;
   static final int C_Integer=1,C_Real=2,C_Boolean=4,C_Text=3,C_Pointer=5;
   static final int C_G0=0,C_G5=5,C_O0=8,C_O1=9,C_O2=10,C_O3=11,C_Sp=24;
	/* ************************************************
	*		Intermediate Code
	*
	************************************************ */
   static final int M_Ar=0,M_Arp=1,M_Arr=2,M_Tmp=3,M_Lit=4,M_Reg=5;
   static final int M_Unspec=0,M_Readint=1,M_Readreal=2,M_Readtext=7;
   static final int M_Writeint=3,M_Writereal=4,M_Writetext=5,M_Writeln=6;
   static final int M_Local=0,M_Block=1,M_Template=2,M_Sys=3,M_Litadr=4;
	

	
   private int LitId=-1,TempNr=-1;
   IStruct IO;
   Op LastInputOp;
   SymSearch SM;
   Ipass2 Rec;
   RTEnv Env;
	/* *******************************************************************
	*		Constructor
	******************************************************************* */
   OpPrec(Interface I,Control C,RTEnv V,Ipass2 RD)
   {  super(I,C);					// use the constructor in super class
      Env=V;						// remember environment
      Rec=RD;						// remember the recursive descent object
      IO=(IStruct)I;				// remember input IStruct subclass of Interface
      SM=new SymSearch(IO);	// create an object for symbol search
   }
	/* *******************************************************************
	*		Recursive descent interface to translating routines.
	******************************************************************* */
   public void InitBlock(int N){SM.SetBlock(N);}
	public void ResetBlock(int N)
	{	Token T; Op P;
		SM.ResetBlock(N);
		T=(Token)IO.GetToken();
		P=MakeOp(T);
		SetCurrentOp(P);
		SetCurrentToken(T);
	}
/* *******************************************************************
			Interpreting an expression, Translate in super class is used
******************************************************************* */
   public Token IntExpr(Token T)
   {  Token TT;
		LastInputOp=null;
      TT=(Token)Translate(T);
      return TT;
   }
/* *******************************************************************
			Interpreting a condition. This will be used for a jump if false.
			Translate in super class is used
******************************************************************* */
   public Token IntJumpFalse(int L)
   {  Op P;
      Token T;
      LastInputOp=null;
      P=null;//P=new Lab(null,M_Local,L); use then Lab constructor is implemented.
      PutOperand((Operand)P);
      return (Token)Translate(IO.GetToken());
   }

	/* *******************************************************************
	*		MakeOp
	*	This routine is used to translate a token into an Op 
	*	(Operand or Operator)
	******************************************************************* */
   public Op MakeOp(Lexeme L)
   {  Token T;
      Op P=null;
		/* ### Code ### */
      return P;
   }

/* **********************************************************
   This method is used to get the result operand, with mode and type
   according to rules given in the matrices below.
   Type matrix used to determine the type of the result
The possible types are:
       input		   result
   A - any type		/ any
   I - integer		/ integer or if operation is div then real
   R - real		/ real
   T - text		/ any (text not used as result)
   B - boolean		/ any (boolean not used as result)
   P - an address	/ any (address not used as result)
   E - (not possible)	/ any
	opnd2
	  A   I   R   T   B   P
opnd1 A	{'A','A','A','A','A','A'}
      I	{'A','I','R','E','E','E'}
      R	{'A','R','R','E','E','E'}
      T	{'A','E','E','T','E','E'}
      B	{'A','E','E','E','B','E'}
      P	{'A','E','E','E','E','P'}
   Mode matrix used to determine the addressing mode of the result
The possible modes are:
   AR  -  Memory, AR on stack
   ARP -  Memory, AR pointed at by TMP		*not used here
   ARR -  Memory, array pointed at by TMP	*not used here
   TMP -  Temporary variable in memory
   LIT -  Literal
   REG -  Special system dependent("Register") *not used here
The possible result actions are:
   N   -  A new temporary (on stack!)
   F   -  First operand temporary is used
   S   -  Second operand temporary is used
   R   -  First operand temporary is used, and second is released (from stack!)
   A   -  First operand temporary is used, see below!
   B   -  Second operand temporary is used, see below!
   D   -  First operand temporary is used, second is released (stack!), see below
   L   -  A new literal computed at compile time(N may be used!)
   ?   -  System dependent action (not used here!)
The actions A,B and D are almost the same as F,S and R. The difference is that the
operands are "indirect operands", meaning the address of the operand in temp and the
result operand shall be a "direct operand", meaning the result in temporary.
	opnd 2
	 AR  ARP ARR TMP LIT REG
opnd1 AR{'N','N','B','S','N','?'},
     ARP{'N','N','B','S','N','?'},
     ARR{'A','A','D','D','A','?'},
     TMP{'F','F','R','R','F','?'},
     LIT{'N','N','B','S','L','?'},
     REG{'?','?','?','?','?','?'}
********************************************************** */
   private MyOperand MakeResOp(MyOperand P1,MyOperand P2,int C)
   {  MyOperand P=null;
		/* ### Code ### */
      return P;
   }
/* **********************************************************
   Make literal calculations.
In a compiler this may be done at compile time (efficient) or
code may be generated and computation left to run time.
In an interpreter it has to be done!
********************************************************** */
   private Literal MakeLit(Literal Q1,Literal Q2,int C)
   { Literal R=null;
		/* ### Code ### */
      return R;
   }

   private char ResType(int Ty1,int Ty2)
   {return ((MyControl)Ctrl).GetResType(Ty1,Ty2);}

   private char GetResMode(int M1,int M2)
   {return ((MyControl)Ctrl).GetResMode(M1,M2);}
/* **********************************************************
   Transfer a parameter to a function.
The operand to transfer (P2) is taken from operand stack
The parameter operand (P1) is taken from operator (function) P,
the parameter has to be searched in the function to get the correct type.
A system function may have a parameter with type "as-is", meaning the type
of the actual parameter, actual type has to be passed on to the "search".
Notice that if operand is in temporary this has to be released!
********************************************************** */
   public void Transfer(Operator P)
   {  MyOperand P1,P2; Adr A1,A2;
/*	Something like this ?????????
      P2=(MyOperand)GetOperand();
      P1=(MyOperand)((FunPar)P).GetParOp(P2.GetType());
      A2=P2.Address(0);
      A1=P1.Address(0);
      if(P1.GetMode()==M_Reg){Env.TransfStdPar(A2);}
      else{Env.SetValue(A1,A2);}
      if(P2 instanceof Temporary) {ReleaseTemp();} 
*/
   }
/* ******************************************************** 
   Get an operator of type Function Paren

   Used by MyControl, a left paren after a function operator
   is regarded as the start of a parameter list and not just
   as a paren.
******************************************************** */
   public Operator NewFunPar(Operator P) {return new FunPar(P);}
	
/* *******************************************************************
*  Search the symbol tables for a given symbol.
   If not found in current table the SF-chain is followed until
   symbol found. I not in tables the system functions are searched
   and if not there null is returned.
   The level in the chain may be retrieved from SymSearch object.
******************************************************************* */
   Op DSearch(Token T)
   {  Op P=null;
		/* ### Code ### */
      return P;
   }
	
   Op GetPar(int BlkId,int Nr)
   {  Op P=null;
		/* ### Code ### */
      return P;
   }
	
/* *******************************************************************
*		Definition of Operands and Operators
*	A type hierarchy of operators and operands is defined.
*
*	Op
*		Operand
*			MyOperand
*				ActivationRecord
*				Temporary
*				Literal
*				Label
*		Operator
*			ConstOperator
*				Lpar
*				Rpar
*				Stop
*				Comma
*			Become
*			ArithmOp
*			Compare
*			UserFunction
*			SystemFunction
*			FunPar
*
*		?More ?????
******************************************************************* */
   private Lpar TheLpar=new Lpar();
   private Rpar TheRpar=new Rpar();
   private Stop TheStop=new Stop();
   private Comma TheComma=new Comma();
	
   abstract class MyOperand extends Operand
   {  int Mode,Garp,Rel,Type;
      Symbol Sym;
      public int GetIndex(){return 0;}
      public void Address(){}
      public Adr Address(int d){return new Adr(Mode,Garp,Rel,Type);}
   }
	
   class ActivationRecord extends MyOperand
   {	
/* You have to define the constructor!
		ActivationRecord(.......)
      {  
      }
*/
   }

   class Temporary extends MyOperand
   {	
		/* ### Constructor(s) ### */
   }
	
   class IndirectTemporary extends MyOperand
   {	
		/* ### Constructor(s) ### */
   }

   class Literal extends MyOperand
   {  String Val;
		/* ### Constructor(s) ### */
		/* ??? Other code ??? */
   }
	
   class Lab extends MyOperand
   {	
		/* ### Constructor(s) ### */
	}
	
   abstract class ConstOperator extends Operator
   {  public void Exec(){}
      public void Prolog(Op P){}
		/*  ??? More ??? */
   }
   class Lpar extends ConstOperator{public int GetIndex(){return AM_Lpar;}}
   class Rpar extends ConstOperator{public int GetIndex(){return AM_Rpar;}}
   class Stop extends ConstOperator{public int GetIndex(){return AM_Empty;}}
   class Comma extends ConstOperator{public int GetIndex(){return AM_Comma;}}
	
   class Become extends Operator
   {  public int GetIndex(){return AM_Become;}
      public void Exec()
      {  		/* ### Code ### */
      }
      public void Prolog(Op P){}
   }
	
   class ArithmOp extends Operator
   {  		/* ### Constructor(s) ### */
		public int GetIndex(){return 0;} // Change this, its more complex!
      public void Exec()
      {    	/* ### Code ### */	}
      public void Prolog(Op P){}
   }
	
   class Compare extends Operator
   {  
      public int GetIndex(){return AM_Cmp;}
      public void Exec()
      {  /* ### Code ### */	}
      public void Prolog(Op P){}
   }
	
	class UserFunction extends Operator
	{	/* ### Constructor ### */
		public int GetIndex(){return AM_Func;}
		public void Exec()
		{	/* ### Code ### */
			/* THINK its IMPORTANT!!! */
		}
		public void Prolog(Op P)
		{	/* ### Code ### */
			/* You have to do something here!!! */
		}
	}
	
   class SystemFunction extends Operator
   {  
      public int GetIndex(){return AM_Func;}
      public void Exec()
      {/* ### Code ### */	}
      public void Prolog(Op P){}
   }
	
   class FunPar extends Operator
   {  
      FunPar(Op F)
      {
			/* Yes put something here! */
      }
      public int GetIndex(){return AM_Fpar;}
      public void Exec(){}
      public void Prolog(Op P){}
 
		/* ????? */
		
   }
}
