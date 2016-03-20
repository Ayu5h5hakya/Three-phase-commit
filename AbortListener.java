import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.System;
import java.lang.Thread;
import java.util.Scanner;

class AbortListener implements Runnable{
    DataInputStream dataInputStream;
    Scanner scanner;
    AbortListener(DataInputStream dataInputStream,Scanner scanner){
        this.dataInputStream = dataInputStream;
        this.scanner = scanner;
    }
    @Override
    public void run() {
        while (true)
        {
            try{
                if (dataInputStream.available()>0){
                    String server_status = dataInputStream.readUTF();
                    if (server_status.equals("GLOBAL_ABORT"))
                    {
                        System.out.println("GLOBAL_ABORT");
                        scanner.close();
                    }
                    break;
                }
            }catch (IOException e){e.printStackTrace();}
        }
        Thread.yield();
    }
}