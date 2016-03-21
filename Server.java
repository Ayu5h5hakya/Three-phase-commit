import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.InterruptedException;
import java.lang.Override;
import java.lang.System;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Server
{
  static int LISTEN_PORT =3000;
  static int TIMEOUT = 10000;
  static int SLEEP_TIME =10000;
  public static String VOTE_REQUEST = "VOTE_REQUEST";
  public static String GLOBAL_COMMIT = "GLOBAL_COMMIT";
  public static String GLOBAL_ABORT = "GLOBAL_ABORT";
  public static String PRE_COMMIT = "PRE_COMMIT";
  public static HashMap<Integer,Socket> connections = new HashMap<>();
  public static DataInputStream[] responses;
  public static DataOutputStream dataOutputStream=null;
  static Socket tempSocket=null;
  static boolean check = true;
  static Timer timer;
  static long startTime;
  static Scanner scanner;
  static ServerSocket socket;
  static FileWriter fw;
  static File newTextFile;
  static PrintWriter printWriter= null;
  public static void main(String[] argv) throws IOException
  {
      newTextFile = new File("cordinatorlog.txt");
      fw = new FileWriter(newTextFile);
      socket = new ServerSocket(LISTEN_PORT);
      NewClients newClients = new NewClients(LISTEN_PORT,socket);
      new Thread(newClients).start();
      scanner = new Scanner(System.in);
      while (true)
      {
        System.out.print("\nEnter a command c/a/* : ");
        String input =scanner.next().trim();
        if(input.equals("c")){
          System.out.println("Beginning Three phase commit");
          ThreePC();
        }else if(input.equals("a")){
          System.out.println("Connected cohrets: "+connections.size());
          for (int i=0;i<connections.size();++i)
          {
            System.out.println(connections.get(i+1));
          }
        }
        else{
          System.out.println("Exiting...");
          scanner.close();
          newClients.finish();
          break;
        }
      }
  }

  private static void ThreePC() throws IOException{
    phase1();
    if(check) phase2();
    if(check) phase3();
    socket.close();
  }

  private static void phase1()
  {
      startTime = System.currentTimeMillis();
      sendResponse(VOTE_REQUEST);
      acceptResponse(startTime);
      if (check)
      {
        System.out.println("First phase complete!!!!");
      }
      else
      {
        sendResponse(GLOBAL_ABORT);
      }

  }
  private static void phase2()throws IOException
  {
    startTime = System.currentTimeMillis();
    System.out.print("\n Sending pre commit message");
    sendResponse(PRE_COMMIT);
    acceptResponse(startTime);
    if (check)
    {
      System.out.println("\nSecond phase complete!!!!");
    }
    else
    {
      sendResponse(GLOBAL_ABORT);
    }
    System.out.print("\nKill server:(y/n):");
    String kill_response = scanner.next().trim();
    if(kill_response.equals("y") || kill_response.equals("Y")){
      sendResponse(PRE_COMMIT);
      socket.close();
      check = false;
    }
//    else if(kill_response.equals("n") || kill_response.equals("N")){
//      sendResponse(GLOBAL_COMMIT);
//    }

  }
  private static void phase3()
  {
    System.out.println("Sending global commit message");
    sendResponse("GLOBAL_COMMIT");
  }
  private static void sendResponse (String response)
  {
    try{
      fw.write("asdasdas");
     for (int i=0;i<connections.size();++i)
     {
       tempSocket = connections.get(i+1);
       dataOutputStream = new DataOutputStream(tempSocket.getOutputStream());
       dataOutputStream.writeUTF(response);
     }
   }catch (IOException e){e.printStackTrace();}
  }

  private static void acceptResponse(long startTime)
  {
    int count =0;
    Socket tempSocket = null;
    DataInputStream[] responses = new DataInputStream[connections.size()];
    try
    {
      for (int i=0;i<connections.size();++i)
      {
        tempSocket = connections.get(i + 1);
        responses[i] = new DataInputStream(tempSocket.getInputStream());
      }
      while(count<connections.size() && !timeout(startTime))
      {
        for (int i=0;i<responses.length;++i)
        {
          if(responses[i].available()>0)
          {
            int response = responses[i].read();
            if(response!=-1)
            {
              System.out.print("" + response);
              if(response==0)
              {
                System.out.println("Some coverts are not ready .... Aborting commit");
                check=false;
                break;
              }
              else if (response == 1 || response == 2)
              {
                count++;
              }
            }

          }else
          {
//            if(!timeout(startTime))
//            {
//              break;
//            }
          }
          //responses[i].close();
        }

//
        if(!check) break;
      }
    }catch (IOException e){e.printStackTrace();}

  }

  private static boolean timeout(long start)
  {
    if(System.currentTimeMillis() - start>TIMEOUT)
    {
      System.out.println("Timeout occured...aborting procedure::::::"+(System.currentTimeMillis()-start));
      check = false;
      return true;
    }
    else return false;
  }
}
