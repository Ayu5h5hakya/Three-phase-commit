import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.System;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server
{
  static int LISTEN_PORT =3000;
  static int TIMEOUT = 10000;
  static int SLEEP_TIME = 10000;
  public static String VOTE_REQUEST = "VOTE_REQUEST";
  public static String GLOBAL_COMMIT = "GLOBAL_COMMIT";
  public static String GLOBAL_ABORT = "GLOBAL_ABORT";
  public static String PRE_COMMIT = "PRE_COMMIT";
  public static HashMap<Integer,Socket> connections = new HashMap<>();
  public static DataInputStream[] responses;
  public static DataOutputStream dataOutputStream=null;
  static Socket tempSocket=null;
  static boolean check = true;
  public static void main(String[] argv)
  {
      NewClients newClients = new NewClients(LISTEN_PORT);
      new Thread(newClients).start();
      Scanner scanner = new Scanner(System.in);
      while (true)
      {
        System.out.print("Enter a command c/a/* : ");
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

  private static void ThreePC(){
    phase1();
    if(check) phase2();
    if(check) phase3();
  }

  private static void phase1()
  {
      sendResponse(VOTE_REQUEST);
      check = acceptResponse();
      if (check)
      {
        System.out.println("\nFirst phase complete!!!!");
      }
      else
      {
        sendResponse(GLOBAL_ABORT);
      }

  }
  private static void phase2()
  {
    System.out.print("\n Sending pre commit message");
    sendResponse(PRE_COMMIT);
    check = acceptResponse();
    if (check)
    {
      System.out.println("\nSecond phase complete!!!!");
    }
    else
    {
      sendResponse(GLOBAL_ABORT);
    }

  }
  private static void phase3()
  {
    System.out.print("\n Sending global commit message");
    sendResponse(GLOBAL_COMMIT);
  }
  private static void sendResponse(String response)
  {
   try{
     for (int i=0;i<connections.size();++i)
     {
       tempSocket = connections.get(i+1);
       dataOutputStream = new DataOutputStream(tempSocket.getOutputStream());
       dataOutputStream.writeUTF(response);
     }
   }catch (IOException e){e.printStackTrace();}
  }

  private static boolean acceptResponse()
  {
    int count =0;
    boolean check = true;
    Socket tempSocket = null;
    DataInputStream[] responses = new DataInputStream[connections.size()];
    try
    {
      for (int i=0;i<connections.size();++i)
      {
        tempSocket = connections.get(i + 1);
        responses[i] = new DataInputStream(tempSocket.getInputStream());
      }
      while(count<connections.size())
      {
        for (int i=0;i<responses.length;++i)
        {
          if(responses[i].available()>0)
          {
            int response = responses[i].read();
            if(response!=-1)
            {
              System.out.print("" + response);
              // System.out.println(count+"");
              if(response==0)
              {
                System.out.println("Some coverts are not ready .... Aborting commit");
                check=false;
                break;
              }
              else if (response == 1)
              {
                count++;
              }
              else if(response == 2)
              {
                count++;
              }
            }

          }
          //responses[i].close();
        }

//
        if(!check) break;
      }
    }catch (IOException e){e.printStackTrace();}

    return check;
  }
}
