import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Override;
import java.lang.System;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

class Timetest{
    public static int TIMEOUT=5000;
    public static void main(String[] argv) throws IOException {
        String str = "SomeMoreTextIsHere";
        File newTextFile = new File("thetextfile.txt");

        FileWriter fw = new FileWriter(newTextFile);
        fw.write(str);
        fw.close();
    }
}