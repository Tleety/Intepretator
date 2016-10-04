
import file.*;

class Filhantering {

    infile P1Infil;
    String Stam;
    String Option, Ut;
    int RadNummer;

    /* *************************** Constructor *************************** */
    public Filhantering(String[] Cmnd) {
        int N;
        N = Cmnd.length;
        Option = "";
        Ut = "Nej";
        if (N == 0) {
            //In is the file we wanna read/interpret.
            infile In;
            In = new infile();
            In.Open();
            System.out.println("Ange infil!");
            Stam = In.ReadText(20).trim();
            Option = "Sats";
            Ut = "Nej";
            if (Stam.length() > 0) {
                P1Infil = new infile(Stam + ".prg");
            } else {
                P1Infil = new infile();
            }
        } else {
            char Ch;
            Stam = Cmnd[0];
            Ch = Stam.charAt(0);
            if (Ch == '-' || Ch == '+') {
                Option = Stam.substring(1);
                if (Ch == '+') {
                    Ut = "Ja";
                } else {
                    Ut = "Nej";
                }
                if (N > 1) {
                    Stam = Cmnd[1];
                    Ch = Stam.charAt(0);
                } else {
                    Ch = '?';
                }
            }
            if (Ch == '?') {
                P1Infil = new infile();
            } else {
                P1Infil = new infile(Stam + ".prg");
            }
        }
        P1Infil.Open();
    }

    /* *************************** Methods *************************** */
    public String GetOption() {
        return Option;
    }

    public String GetOut() {
        return Ut;
    }

    public String GetLine() {
        if (P1Infil.Endfile()) {
            return "\025";
        } else {
            P1Infil.InImage();
            RadNummer = RadNummer + 1;
            return P1Infil.GetImage();
        }
    }

    public int GetLineNumber() {
        return RadNummer;
    }

    public void P2Finish() {
        P1Infil.Close();
    }
}
