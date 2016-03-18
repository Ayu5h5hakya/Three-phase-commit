import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.System;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client{

    public static String COORDINATOR_IP = "localhost";
    public static String VOTE_REQUEST = "VOTE_REQUEST";
    public static int COORDINATOR_PORT =3000;
    public static boolean VOTE_ABORT_FLAG = false;
    public static int TIMEOUT = 3000;
    public static int SLEEP_TIME = 0;
    public static Socket socket;
    public static int COMMIT =1;
    public static int ABORT=0;
    public static int ACK=2;
    public static void main(String[] argv){
        String temp = "";
        try{
            socket = new Socket(COORDINATOR_IP,COORDINATOR_PORT);
            System.out.println("Connected to :"+socket.getRemoteSocketAddress());
        }
        catch (UnknownHostException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}

        try{
            DataInputStream dataInputStream =new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while(true)
            {
                String server_request = dataInputStream.readUTF();
                if(server_request.equals("x")){
                    dataInputStream.close();
                    dataOutputStream.close();
                    break;
                }
                System.out.println("Server said: "+server_request);
                if(server_request.equals(VOTE_REQUEST))
                {
                    System.out.println("Are you ready to commit?");
                    Scanner scanner = new Scanner(System.in);
                    temp = scanner.next().trim();
                    if(temp.equals("Y") || temp.equals("y"))
                    {
                        //dataOutputStream.writeUTF(""+COMMIT);
                        dataOutputStream.write(COMMIT);
                    }else if (temp.equals("N") || temp.equals("n")){
                        dataOutputStream.write(ABORT);
                    }
                }else if (server_request.equals("GLOBAL_COMMIT"))
                {
                    System.out.println("GLOBAL_COMMIT");
                    break;
                }
                else if (server_request.equals("GLOBAL_ABORT"))
                {
                    System.out.println("GLOBAL_ABORT");
                    break;
                }
                else if (server_request.equals("PRE_COMMIT"))
                {
                    dataOutputStream.write(ACK);
                }
            }
        }catch (IOException e){e.printStackTrace();}
    }
}