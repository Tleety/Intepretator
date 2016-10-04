
class Ipass2 {

    IStruct IO;                                 // The result from P1 lex and analysis
    MyControl Ctrl;                             // Operator precedens control matrices
    RTEnv Env;                                  // The run time environment
    OpPrec Prec;                                // Operator precedence object ("Expert object")
    Ipass1 TheP1;                               // P1 object, used for lex during interpretation
                                                // used if we interpret statement by statement
/* ***********************************************************************
			Constructor
    Ipass2(IStruct)             - Used when interpreting complete program
    Ipass2(IStruct,Ipass1)      - Used when interpreting statement by statement
*********************************************************************** */
    public Ipass2(IStruct I) {
        IO = I;                                 // Remember input object
        Ctrl = new MyControl();                 // Remember control object
        Env = new RTEnv(IO);                    // Remember environment object
        Prec = new OpPrec(IO, Ctrl, Env, this);	// Create OpPrec "expert" object
        Env.Init(Prec, this);                   // Init run time environment
        IntEnv();                               // Start interpretation
        Env.Finish();                           // Close the environment
    }

    /* ********* */
    public Ipass2(IStruct I, Ipass1 P1) {
        boolean B;
        Token T;
        int Adr;
        B = true;
        IO = I;                                 // The start is the same
        Ctrl = new MyControl();                 // as interpreting Program
        Env = new RTEnv(IO);
        Prec = new OpPrec(IO, Ctrl, Env, this);
        Env.Init(Prec, this);                   // until here!
        IO.InitBlock(0);                        // Statements considered within
        Prec.InitBlock(0);                      // block 0
        TheP1 = P1;                             // Remember the lex and analysis pass
        Env.Enter(0, 100);                      // Create template  first time.
        Env.Allocate(0, 0);                     // allocate block 0, level 0
        Env.Execute(0);				// start interpret statement
        while (B)                               // Interprete statements until end
        {
            System.out.println("");
            Adr = IO.GetNextTokNr();		// Remember adress of last token
            B = TheP1.ParseNext();		// Parse next statement/declaration
            IO.ContBlock(0, Adr);		// Continue block 0 with the added code
            T = GetToken();			// Read a token
            if (T != null) {
                IntSats(T);
            } else {
                //TODO: Are there anything? Interprete this!
            }                                   
        }
    }

    /* ****************************************************
    *	Private methods doing the recursive descent
    *	translation, for expressions we link to an OpPrec
    *	object.
    * ************************************************** */

        /* *****  Interpret Environment  *****
	*    A program may be regarded as embedded in
	*    a fictive block that just starts the
	*    interpretatin of the program.
	* ******************************** */
    private void IntEnv() {
        int Size;
        Size = IO.GetSize(0);	                // Get the data size of first block
        Env.Enter(0, Size);	                // Create template if first time.
        Env.Allocate(0, 0);	                // allocate block 0, level 0
        Env.Execute(0);		                // start interpreting the block
        Env.Retur();			        // return from environment(finish program)
    }

    /* *****  Interpret Block  *****
    * Interpreting one block of the
    * user written program.
    * Interpreting one block of the* 
    ******************************** */
    public void InterBlock(int N) {
        Token T;
        boolean Slut;
        IO.InitBlock(N);
        Prec.InitBlock(N);
        T = GetToken();
        /*  ### TODO: Fill in the interpretation of a block ### */
    }

    /* *****  Interpret Statement  *****
    * Interpreting one statement of the
    * user written program.
    * When interpreting code you have to
    * skip statements (There are no "jumps")
    * ******************************** */
    private Token IntSats(Token T) {
        /* ### TODO: Fill in code here ### */
        return T;
    }

    /* *****  Skip Statement  *****
    * 
    * When interpreting code you have to
    * skip statements, this is a way to
    * simulate jumps.
    * ******************************** */
    private Token SkipSats(Token T) {
        /* ### TODO: similar to IntSats ### */
        return T;
    }

    private Token SkipIf() {
        Token T;
        /* ### TODO: The code ### */
        return null;
    }

    private Token SkipWhile() {
        Token T;
        /* ### TODO: The code ### */
        return null;
    }

    /* *****  Interpret IfStatement  *****
    * Interpreting one if-statement of the
    * user written program.
    * ******************************** */
    private Token IntIf() {
        Token T;
        /* ### TODO: The code ### */
        return null;
    }

    /* *****  Interpret WhileStatement  *****
    * Interpreting one while-statement of the
    * user written program.
    * ******************************** */
    private Token IntWhile() {
        Token T = null;
        /* ### TODO: The code ### */
        return T;
    }

    /* *****  Compile BlockCall  *****
    * Compiling the call of a block. The block
    * used as a statement in user written program.
    * ******************************** */
    private Token IntBCall(Token T) {
        int Nr;
        Nr = T.GetBlkNr();
        /* ### TODO: The code ### */
        return T;
    }

    /* *****  Compile EmptyStatement  *****
    * Compiling an empty statement
    * 
    * ******************************** */
    private Token IntEmpty() {
        /* ### TODO: ? ### */
        return null;
    }

    /* *****  Compile Error  *****
    * Compiling an error found in pass 1
    * This is regarded as a statement.
    * ******************************** */
    private Token IntError(Token T) {
        /* ### TODO: The code ### */
        return null;
    }

    /* **** Interface to other objects **** */
    private Token GetToken() {
        Token T;
        T = (Token) (IO.GetToken());
        return T;
    }

    /* *************** Help Routines ************** */
 /* ### TODO: ????? ### */
}
