import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Override;
import java.lang.System;
import java.lang.Thread;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Client
{

    public static String COORDINATOR_IP = "localhost";
    public static String VOTE_REQUEST = "VOTE_REQUEST";
    public static int COORDINATOR_PORT =3000;
    public static boolean VOTE_ABORT_FLAG = false;
    public static int TIMEOUT = 10000;
    public static int SLEEP_TIME = 0;
    public static Socket socket;
    public static int COMMIT =1;
    public static int ABORT=0;
    public static int ACK=2;
    public static String last = "";
    public static boolean response_sent=false;
    public static PrintWriter printWriter=null;
    public static void main(String[] argv) throws IOException
    {
        File newTextFile = new File("cohert"+argv[0]+"log.txt");
        FileWriter fw = new FileWriter(newTextFile);
        String temp = "";
        try{
            socket = new Socket(COORDINATOR_IP,COORDINATOR_PORT);
            System.out.println("Connected to :"+socket.getRemoteSocketAddress());
        }
        catch (UnknownHostException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}

        try
        {
            DataInputStream dataInputStream =new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while(true)
            {
//                if(startTime!=-1 && timeout(startTime)){
//                    if (!socket.getInetAddress().isReachable(TIMEOUT))
//                    {
//                        System.out.println("The cordinator has failed and precommit was already issued : GLOBAL_COMMIT");
//                        break;
//                    }
//                }
                String server_request = dataInputStream.readUTF();
                if(server_request.equals("x"))
                {
                    dataInputStream.close();
                    dataOutputStream.close();
                    break;
                }
                System.out.println("Server said: "+server_request);
                if(server_request.equals(VOTE_REQUEST))
                {
                    last = VOTE_REQUEST;
                    System.out.println("Are you ready to commit?");
                    final Scanner scanner = new Scanner(System.in);
                   // AbortListener abortListener = new AbortListener(dataInputStream,scanner);
                   // new Thread(abortListener).start();
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    if(!response_sent)
                                    {
                                        try {
                                            System.out.print("GLOBAL_ABORT");
                                            fw.write("GLOBAL_ABORT");
                                            fw.close();
                                        }
                                        catch (IOException e){e.printStackTrace();}
                                    }
                                    scanner.close();
                                }
                            },
                            10000
                    );
                    temp = scanner.next().trim();
                    if(temp.equals("Y") || temp.equals("y"))
                    {
                        //dataOutputStream.writeUTF(""+COMMIT);
                        dataOutputStream.write(COMMIT);
                    }else if (temp.equals("N") || temp.equals("n"))
                    {
                        dataOutputStream.write(ABORT);
                    }
                    response_sent = true;
                }
                else if (server_request.equals("GLOBAL_COMMIT"))
                {
                    last = "GLOBAL_COMMIT";
                    fw.write("GLOBAL_COMMIT");
                    System.out.println("GLOBAL_COMMIT");
                    break;
                }
                else if (server_request.equals("GLOBAL_ABORT"))
                {
                    last = "GLOBAL_ABORT";
                    fw.write("GLOBAL_ABORT");
                    System.out.println("GLOBAL_ABORT");
                    break;
                }
                else if (server_request.equals("PRE_COMMIT"))
                {
                    fw.write("PRE_COMMIT");
                    if (last.equals("PRE_COMMIT"))
                    {
                       // System.out.println(""+socket.getInetAddress().isReachable(TIMEOUT));
                        System.out.println("The cordinator has failed and precommit was already issued : GLOBAL_COMMIT");
                        break;
                    }
                    last="PRE_COMMIT";
                    dataOutputStream.write(ACK);
                }
            }
        }catch (IOException e){e.printStackTrace();}
    }
    private static boolean timeout(long start)
    {
        if(System.currentTimeMillis() - start>TIMEOUT)
        {
            System.out.println("Timeout occured...aborting procedure");
            return true;
        }
        else return false;
    }
}