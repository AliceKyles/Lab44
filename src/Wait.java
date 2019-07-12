import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Wait {
    private Socket socket;
    private ServerSocket serverSocket;
    private int port;

    public static void main(String[] args) throws Exception {
        Wait w=new Wait();


    }

    private Wait() throws IOException {
        Scanner in = new Scanner(System.in);
        port=in.nextInt();
        serverSocket = new ServerSocket(port);
        socket = new Socket("localhost", 5470);
        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
        byte[] buf;
        String s="Exec";
        buf=s.getBytes();
        din.writeInt(s.length());
        din.write(buf,0,s.length());
        s="localhost";
        buf=s.getBytes();
        din.writeInt(s.length());
        din.write(buf,0,s.length());
        din.writeInt(port);
        din.flush();
        socket.close();
        Wait.ShutdownHook shutdownHook = new Wait.ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        while (true){
            run();
        }
    }

    private void run() throws IOException {
        byte[] buf=new byte[512];
        socket=serverSocket.accept();
        DataInputStream os = new DataInputStream(socket.getInputStream());
        int year=os.readInt()-1900;
        if (year<0){
            os.close();
            socket.close();
        }
        else{
            int month=os.readInt()-1;
            int day=os.readInt();
            int hour=os.readInt();
            int minute=os.readInt();
            Date dat=new Date(year,month,day,hour,minute);
            System.out.println(dat.toString());
            int a=os.readInt();
            os.read(buf,0,a);
            String email=new String(buf, 0, a);
            a=os.readInt();
            os.read(buf,0,a);
            String text=new String(buf, 0, a);
            os.close();
            socket.close();
            client(dat,email,text);
        }
    }

    private void client(Date dat, String email, String text){
        new Time(dat, email, text).start();
    }

    class ShutdownHook extends Thread {

        public void run() {
            byte[] buf;
            String s="quitA";
            buf=s.getBytes();
            try {
                socket = new Socket("localhost", 5470);
                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                din.writeInt(s.length());
                din.write(buf,0,s.length());
                s="localhost";
                buf=s.getBytes();
                din.writeInt(s.length());
                din.write(buf,0,s.length());
                din.writeInt(port);
                din.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Time  extends Thread{
        private Date dat;
        String email;
        String text;

        private Time(Date d, String e, String t){
            dat=d;
            email=e;
            text=t;
            Time.ShutdownHook shutdownHook = new Time.ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }

        class ShutdownHook extends Thread {

            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] buf;
                String s="client";
                buf=s.getBytes();
                try {
                    socket = new Socket("localhost", 5470);
                    DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                    din.writeInt(s.length());
                    din.write(buf,0,s.length());
                    din.writeInt(dat.getYear()+1900);
                    din.writeInt(dat.getMonth()+1);
                    din.writeInt(dat.getDay()+17);
                    din.writeInt(dat.getHours());
                    din.writeInt(dat.getMinutes());
                    buf=email.getBytes();
                    din.writeInt(email.length());
                    din.write(buf,0,email.length());
                    buf=text.getBytes();
                    din.writeInt(text.length());
                    din.write(buf,0,text.length());
                    din.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void run(){
            boolean a=false;
            while (!a){
                Date date=new Date();
                if (dat.before(date)||dat.equals(date)){
                    a=true;
                    send();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void send(){
            try {
                System.out.println("Настало время");
                byte[] buf;
                String s="Time";
                buf=s.getBytes();
                socket = new Socket("localhost", 5470);
                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                din.writeInt(s.length());
                din.write(buf,0,s.length());
                buf=email.getBytes();
                din.writeInt(email.length());
                din.write(buf,0,email.length());
                buf=text.getBytes();
                din.writeInt(text.length());
                din.write(buf,0,text.length());
                din.flush();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }}
