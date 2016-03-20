import org.omg.PortableInterceptor.ServerRequestInfo;

import java.io.IOException;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.System;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class NewClients implements Runnable{

    private boolean stop;
    private int port;
    private ServerSocket socket;
    NewClients(int port,ServerSocket socket){
        stop = false;
        this.port = port;
        this.socket = socket;
    }
    @Override
    public void run() {
            //System.out.println(Thread.currentThread().getName()+": listening on port:"+port);
            while(!stop){
                Socket newClient = null;
                try{
                    newClient=socket.accept();
                    //System.out.println(Thread.currentThread().getName()+": connection accepted:");
                    InetAddress ip= newClient.getInetAddress();
                    int clientport = newClient.getPort();
                    //System.out.println("Got TCP connection with " + ip+ ":" + port);
                    Server.connections.put(Server.connections.size()+1,newClient);
                }
                catch (SocketException e){break;}
                catch (SocketTimeoutException e){e.printStackTrace();}
                catch (IOException e){e.printStackTrace();}
            }
    }
    public void finish(){
        stop = true;
    }
}