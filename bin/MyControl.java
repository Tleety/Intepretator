import komp.*;
/* **********************************************************
   Action Matrix
Index 1,2,....,14 of course the java matrix is indexed from 0 up to
13, therefor the i-1,j-1 in the indexing below.
Index for the different operators:
AM_Become=1(:=),AM_New=2(new not used),AM_Add=3(+ -),AM_Mul=4(* /),
AM_Cmp=5(= <> < > <= >=),AM_Func=6(user function)
AM_Array=7(array not used),AM_Class=8(class not used),AM_Comma=9(,),
AM_Lpar=10( left-( ),AM_Rpar=11(right-) )
AM_Fpar=12( function left-( ),AM_Dot=13( . not used),AM_Empty=14(end operator
or empty stack)
The actions:
S - stack operator
U - execute operator
A - accept (finish action)
E - error
F - stack user function,perform prolog (allocate AR)
C - stack function argument (
P - remove ( from stack and skip )
T - transfer parameter
L - transfer last parameter
M - stack user method (called by dot, not used here)
D - stack array (called by dot, not used here)
? - not used (error)

********************************************************** */
class MyControl extends Control
{	private char[][] ActionMatrix=
	{	{'E','S','S','S','S','F','S','E','U','S','E','E','S','U'},
		{'E','E','E','E','E','E','E','F','E','E','E','E','?','E'},
		{'E','E','U','S','U','F','S','E','U','S','U','E','S','U'},
		{'E','E','U','U','U','F','S','E','U','S','U','E','S','U'},
		{'E','E','S','S','E','F','S','E','U','S','U','E','S','U'},
		{'U','E','U','U','U','?','?','E','U','C','U','E','?','U'},
		{'U','E','U','U','U','?','?','E','U','S','U','E','U','U'},
		{'U','E','U','U','U','?','?','E','U','C','U','E','?','U'},
		{'E','E','E','E','E','E','E','E','E','E','E','E','S','E'},
		{'S','E','S','S','S','F','S','E','E','S','P','E','S','E'},
		{'E','E','E','E','E','E','E','E','E','E','E','E','E','E'},
		{'S','E','S','S','S','F','S','E','T','S','L','E','S','E'},
		{'?','?','U','U','U','M','D','U','U','U','U','U','U','U'},
		{'S','S','S','S','S','F','S','E','?','S','E','E','S','A'}
	};
	public char GetAction(int i,int j)
	{	if(i==0){i=14;}
		return ActionMatrix[i-1][j-1];
	}
	

	public void OtherAction(char A,OperatorPrecedence Q)
	{	Op P;
		Lexeme T;
		
		P=Q.GetCurrentOp();
		switch(A)
		{	case 'F':	Q.PutOperator((Operator)P);
							((Operator)P).Prolog(null);
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			case 'M':	Q.GetOperator(); // remove dot
							Q.PutOperator((Operator)P);
							((Operator)P).Prolog(Q.GetOperand());
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			case 'D':	Q.GetOperator(); // remove dot
							Q.PutOperator((Operator)P);
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			case 'T':	((OpPrec)Q).Transfer(Q.GetTopOperator());
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			case 'C':	Q.PutOperator(((OpPrec)Q).NewFunPar(Q.GetTopOperator()));
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			case 'L':	((OpPrec)Q).Transfer(Q.GetOperator());
							T=Q.GetToken();
							P=Q.MakeOp(T);
							break;
			default :	T=null;
							break;
		}
		Q.SetCurrentOp(P);
		Q.SetCurrentToken(T);
	}
/* **********************************************************
   Follow Matrix
Used for errorhandling!
Used to check an operand/operator directly following an operand/operator
A - OK permitted
1,2... - Error 
********************************************************** */
	private char[][] FollowMatrix=
	{	{'1','A','9','A','A','A','1','1','?','A','1','A','?','A','A'},
		{'A','2','A','3','3','3','A','A','?','8','A','4','?','?','2'},
		{'A','2','9','A','A','A','A','A','A','A','A','A','A','?','A'},
		{'A','2','9','3','3','3','A','A','?','3','A','3','?','?','3'},
		{'A','2','9','3','3','3','A','A','?','3','A','3','?','?','3'},
		{'A','2','9','3','3','3','A','A','?','3','A','3','?','?','3'},
		{'5','2','9','A','A','A','5','5','?','A','A','A','?','?','A'},
		{'6','2','9','6','6','6','6','6','?','6','A','6','?','?','6'},
		{'A','2','9','A','A','A','A','A','?','A','A','A','A','?','A'},
		{'A','2','9','3','3','3','A','A','?','8','A','3','?','?','3'},
		{'A','2','9','3','3','3','A','A','?','3','A','3','?','?','7'},
		{'1','A','A','A','A','A','1','1','?','A','1','A','?','A','A'},
		{'?','?','?','?','?','?','?','?','?','?','?','?','?','?','?'},
		{'A','A','A','A','A','A','A','A','A','A','A','A','A','A','A'},
		{'A','2','A','3','3','3','A','A','?','8','A','4','?','?','A'}
	};
	
	public char FollowOp(int I,int K)
	{	return FollowMatrix[I][K];}
/* **********************************************************
   Type Matrix
The type of the result depending on operands types 
A	- anytype used as the type of error results
I	- integer
R	- real
T	- text
B	- boolean
P	- pointer

********************************************************** */
	private char[][] TypeMatrix=
	{	{'A','A','A','A','A','A'},
		{'A','I','R','E','E','E'},
		{'A','R','R','E','E','E'},
		{'A','E','E','T','E','E'},
		{'A','E','E','E','B','E'},
		{'A','E','E','E','E','P'}
	};
	
	public char GetResType(int I,int K){return TypeMatrix[I][K];}
	
	private char[][] ModeMatrix=
	{	{'N','N','B','S','N','?'},
		{'N','N','B','S','N','?'},
		{'A','A','D','D','A','?'},
		{'F','F','R','R','F','?'},
		{'N','N','B','S','L','?'},
		{'?','?','?','?','?','?'}
	};
	
	public char GetResMode(int I,int K){return ModeMatrix[I][K];}
}
