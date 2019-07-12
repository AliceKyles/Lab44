import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.*;


public class Server {
    public static void main(String[] args) throws Exception {
        remServer s=new remServer();



    }

    public static class remServer extends Thread{
        Vector <Integer> mailPort;
        Vector <String> mailAddress;
        Vector <Integer> waitPort;
        Vector <String> waitAddress;
        int counta, ca, countb, cb;
        ServerSocket serverSocket;
        boolean c;
        int checkport;
        String checkaddress;


        public class Res  extends Thread{
            private Res(){

            }

            public void run(){
                byte[] buffer = new byte[256];
                while(true){
                    try {
                        Socket sc=serverSocket.accept();
                        DataInputStream os = new DataInputStream(sc.getInputStream());
                        int a=os.readInt();
                        os.read(buffer,0,a);
                        String s = new String(buffer, 0, a);
                        if (s.contains("mail")){
                            a=os.readInt();
                            os.read(buffer,0,a);
                            String data = new String(buffer, 0, a);
                            mailPort.add(os.readInt());
                            mailAddress.add(data);
                            counta++;
                            System.out.println("E-mail");
                        }
                        if (s.contains("Exec")){
                            a=os.readInt();
                            os.read(buffer,0,a);
                            String data = new String(buffer, 0, a);
                            waitPort.add(os.readInt());
                            waitAddress.add(data);
                            countb++;
                            if (c){
                                Socket socket = new Socket(checkaddress, checkport);
                                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                s="add";
                                byte[] buf=s.getBytes();
                                din.writeInt(s.length());
                                din.write(buf,0,s.length());
                                s=waitAddress.lastElement();
                                din.writeInt(s.length());
                                din.write(buffer,0,s.length());
                                din.writeInt(waitPort.lastElement());
                                din.flush();
                                socket.close();
                            }
                            System.out.println("Time");
                        }
                        if (s.contains("quitB")){
                            a=os.readInt();
                            os.read(buffer,0,a);
                            String data = new String(buffer, 0, a);
                            int b=os.readInt();
                            int f=mailPort.indexOf(b);
                            while (!mailAddress.get(f).contains(data)){
                                f=mailPort.indexOf(b,f+1);
                            }
                            mailPort.remove(f);
                            mailAddress.remove(f);
                            counta--;
                            ca=0;
                            System.out.println("E-mail shut down");
                        }
                        if (s.contains("quitA")){
                            a=os.readInt();
                            os.read(buffer,0,a);
                            String data = new String(buffer, 0, a);
                            int b=os.readInt();
                            int f=waitPort.indexOf(b);
                            while (!waitAddress.get(f).contains(data)){
                                f=waitPort.indexOf(b,f+1);
                            }
                            if (c){
                                Socket socket = new Socket(checkaddress, checkport);
                                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                s="shut";
                                byte[] buf=s.getBytes();
                                din.writeInt(s.length());
                                din.write(buf,0,s.length());
                                din.writeInt(a);
                                din.write(buffer,0,a);
                                din.writeInt(b);
                                din.flush();
                                socket.close();
                            }
                            waitPort.remove(f);
                            waitAddress.remove(f);
                            countb--;
                            cb=0;
                            System.out.println("Time shut down");
                        }
                            if (s.contains("quitC")){
                            c=false;
                            System.out.println("Checking shut down");
                        }
                        if (s.contains("client")){
                            System.out.println("Новый клиент");
                            if (countb==0&&c){
                                Socket st = new Socket(checkaddress, checkport);
                                DataOutputStream din = new DataOutputStream(st.getOutputStream());
                                for (int i=0; i<5;i++){
                                    os.readInt();
                                }
                                s="mistake";
                                buffer=s.getBytes();
                                din.writeInt(s.length());
                                din.write(buffer,0,s.length());
                                a=os.readInt();
                                din.writeInt(a);
                                os.read(buffer,0,a);
                                din.write(buffer,0,a);
                                s="Service is temporarily unavailable. Please try again later.";
                                buffer=s.getBytes();
                                din.writeInt(s.length());
                                din.write(buffer,0,s.length());
                            }
                            if (countb>0){
                                try{
                                    Socket socket = new Socket(waitAddress.get(cb), waitPort.get(cb));
                                    DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                    if (!c){
                                        for (int i=0; i<5;i++){
                                            din.writeInt(os.readInt());
                                        }
                                        for (int i=0; i<2;i++){
                                            int a1=os.readInt();
                                            din.writeInt(a1);
                                            os.read(buffer,0,a1);
                                            din.write(buffer,0,a1);
                                        }
                                    }
                                    if (c){
                                        Socket st = new Socket(checkaddress, checkport);
                                        DataOutputStream din1 = new DataOutputStream(st.getOutputStream());
                                        s="client";
                                        byte[] buf=s.getBytes();
                                        din1.writeInt(s.length());
                                        din1.write(buf,0,s.length());
                                        for (int i=0; i<5;i++){
                                            int j=os.readInt();
                                            din.writeInt(j);
                                            din1.writeInt(j);
                                        }
                                        for (int i=0; i<2;i++){
                                            byte[] b=new byte[1024];
                                            int a1=os.readInt();
                                            os.readNBytes(b,0,a1);
                                            din.writeInt(a1);
                                            din1.writeInt(a1);
                                            din.write(b,0,a1);
                                            din1.write(b,0,a1);
                                        }
                                        din1.writeInt(waitAddress.get(cb).length());
                                        buffer=waitAddress.get(cb).getBytes();
                                        din1.write(buffer,0,waitAddress.get(cb).length());
                                        din1.writeInt(waitPort.get(cb));
                                        st.close();
                                        din1.flush();
                                    }
                                    din.flush();
                                    socket.close();
                                    cb++;
                                    if (cb==countb)
                                        cb=0;
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    waitPort.remove(cb);
                                    waitAddress.remove(cb);
                                    countb--;
                                    cb=0;
                                }
                            }
                        }
                        if (s.contains("Time")){
                            if (counta>0){
                                try{
                                    Socket socket = new Socket(mailAddress.get(ca), mailPort.get(ca));
                                    DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                    for (int i=0; i<2;i++){
                                        int a1=os.readInt();
                                        din.writeInt(a1);
                                        os.read(buffer,0,a1);
                                        din.write(buffer,0,a1);
                                    }
                                    din.flush();
                                    socket.close();
                                    ca++;
                                    if (ca==counta)
                                        ca=0;
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    Socket socket = new Socket(checkaddress, checkport);
                                    DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                    s="mistake";
                                    byte[] buf=s.getBytes();
                                    din.writeInt(s.length());
                                    din.write(buf,0,s.length());
                                    for (int i=0; i<2;i++){
                                        int a1=os.readInt();
                                        din.writeInt(a1);
                                        os.read(buffer,0,a1);
                                        din.write(buffer,0,a1);
                                    }
                                    din.flush();
                                    socket.close();
                                    mailPort.remove(ca);
                                    mailAddress.remove(ca);
                                    counta--;
                                    ca=0;
                                }
                            }
                            else {
                                Socket socket = new Socket(checkaddress, checkport);
                                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                                s="mistake";
                                byte[] buf=s.getBytes();
                                din.writeInt(s.length());
                                din.write(buf,0,s.length());
                                for (int i=0; i<2;i++){
                                    int a1=os.readInt();
                                    din.writeInt(a1);
                                    os.read(buffer,0,a1);
                                    din.write(buffer,0,a1);
                                }
                                din.flush();
                                socket.close();
                            }
                        }
                        os.close();
                        sc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        private remServer() throws IOException {

            int port=5470;
            serverSocket = new ServerSocket(port);
            ca=0;
            cb=0;
            counta=0;
            countb=0;
            mailAddress=new Vector<>();
            mailPort=new Vector<>();
            waitAddress=new Vector<>();
            waitPort=new Vector<>();
            boolean ready=false;
            boolean ready1=false;
            c=false;
            boolean ready2=false;
            byte[] buffer = new byte[256];
            while (!ready){
                Socket s=serverSocket.accept();
                DataInputStream os = new DataInputStream(s.getInputStream());
                int a=os.readInt();
                os.read(buffer,0,a);
                String data = new String(buffer, 0, a);
                if (data.contains("check")){
                    a=os.readInt();
                    os.read(buffer,0,a);
                    data = new String(buffer, 0, a);
                    checkport=os.readInt();
                    checkaddress=data;
                    c=true;
                    System.out.println("Checking system");
                }
                if (data.contains("mail")){
                    a=os.readInt();
                    os.read(buffer,0,a);
                    data = new String(buffer, 0, a);
                    mailPort.add(os.readInt());
                    mailAddress.add(data);
                    counta++;
                    System.out.println("E-mail");
                    ready1=true;
                }
                if (data.contains("Exec")){
                    a=os.readInt();
                    os.read(buffer,0,a);
                    data = new String(buffer, 0, a);
                    waitPort.add(os.readInt());
                    waitAddress.add(data);
                    countb++;
                    if (c){
                        Socket socket = new Socket(checkaddress, checkport);
                        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                        String s1="add";
                        byte[] buf=s1.getBytes();
                        din.writeInt(s1.length());
                        din.write(buf,0,s1.length());
                        s1=waitAddress.lastElement();
                        din.writeInt(s1.length());
                        din.write(buffer,0,s1.length());
                        din.writeInt(waitPort.lastElement());
                        socket.close();
                    }
                    System.out.println("Time");
                    ready2=true;
                }
                if (ready1&ready2){
                    ready=true;
                    if (c){
                        Socket socket = new Socket(checkaddress, checkport);
                        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                        String s1="check";
                        byte[] buf=s1.getBytes();
                        din.writeInt(s1.length());
                        din.write(buf,0,s1.length());
                        socket.close();
                    }
                }
                os.close();
                s.close();
            }
            new Res().start();
        }
    }



}
