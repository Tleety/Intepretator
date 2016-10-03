import file.*;
class Interpret
{  public static void main(String[] args)
   {  Filhantering Fil; outfile UT; String Opt;
      Lex LX; IStruct Res; Ipass1 P1; Ipass2 P2;
		System.out.println("Start av interpretator");
      Fil=new Filhantering(args);
		Opt=Fil.GetOption();
      Res=new IStruct();
      LX=new Lex(Res,Fil);
		if(Opt.length()==0||Opt.equals("Prog"))
      {	System.out.println("Interpretering av program");
			P1=new Ipass1(LX,Res);
      	P2=new Ipass2(Res);
		}
		else
		if(Opt.equals("Sats"))
		{	System.out.println("Interpretering satsvis");
			P1=new Ipass1(LX,Res,true);
      	P2=new Ipass2(Res,P1);
		}
		else{System.out.println("Ok\344nd interpreteringstyp");}
      System.out.println("K\366rningen klar");
		if(Fil.GetOut().equals("Ja"))
		{	UT=new outfile("FIL.p1");
			UT.Open();
      	Res.OutPut(UT);
			UT.Close();
		}
   }
}
