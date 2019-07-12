import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Scanner;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Check {
    private Socket socket;
    private ServerSocket serverSocket;
    private int port;
    Vector <Integer> waitPort;
    Vector <String> waitAddress;
    Vector <Clients> client;
    int wait, cl;
    String txt;

    private Check() throws IOException {
        Scanner in = new Scanner(System.in);
        port=in.nextInt();
        serverSocket = new ServerSocket(port);
        socket = new Socket("localhost", 5470);
        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
        byte[] buf;
        String s="check";
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
        txt="We are sorry to inform you, that your reminder would not be sent to you at the time you requested. Our sincere apologies for the inconvenience.";
        waitAddress=new Vector<>();
        waitPort=new Vector<>();
        client=new Vector<>();
        cl=0;
        wait=0;
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        while (true){
            run();
        }
    }

    class ShutdownHook extends Thread {

        public void run() {
            byte[] buf;
            String s="quitC";
            buf=s.getBytes();
            try {
                socket = new Socket("localhost", 5470);
                DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                din.writeInt(s.length());
                din.write(buf,0,s.length());
                din.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void run() throws IOException {
        byte[] buf=new byte[512];
        socket=serverSocket.accept();
        DataInputStream os = new DataInputStream(socket.getInputStream());
        int a=os.readInt();
        os.read(buf,0,a);
        String s = new String(buf, 0, a);
        if (s.contains("check")){
            new CheckUp().start();
        }
        if (s.contains("client")){
            int year=os.readInt()-1900;
            int month=os.readInt()-1;
            int day=os.readInt();
            int hour=os.readInt();
            int minute=os.readInt();
            Date dat=new Date(year,month,day,hour,minute);
            Clients c=new Clients();
            c.date=dat;
            System.out.println(c.date);
            a=os.readInt();
            os.read(buf,0,a);
            c.email=new String(buf, 0, a);
            System.out.println(c.email);
            a=os.readInt();
            os.read(buf,0,a);
            c.text=new String(buf, 0, a);
            System.out.println(c.text);
            a=os.readInt();
            os.read(buf,0,a);
            c.waitadress=new String(buf, 0, a);
            c.waitport=os.readInt();
            System.out.println(c.waitadress);
            System.out.println(c.waitport);
            client.addElement(c);
            cl++;
        }
        if (s.contains("shut")){
            a=os.readInt();
            os.read(buf,0,a);
            String data = new String(buf, 0, a);
            int b=os.readInt();
            int f=waitPort.indexOf(b);
            while (!waitAddress.get(f).contains(data)){
                f=waitPort.indexOf(b,f+1);
            }
            waitPort.remove(f);
            waitAddress.remove(f);
            wait--;
        }
        if (s.contains("add")){
            a=os.readInt();
            os.read(buf,0,a);
            String data = new String(buf, 0, a);
            waitPort.add(os.readInt());
            waitAddress.add(data);
            wait++;
        }
        if (s.contains("mistake")){
            a=os.readInt();
            os.read(buf,0,a);
            String to = new String(buf, 0, a);
            a=os.readInt();
            os.read(buf,0,a);
            String text = new String(buf, 0, a);
            send(to,text);
        }
        os.close();
        socket.close();
    }


    private void send(String to,String text) {
        final String ENCODING = "UTF-8";
        Authenticator auth = new MyAuthenticator("kulikcha7", "mo5skva");
        String from = "kulikcha7@yandex.ru";
        String host = "localhost";
        String smtpHost="smtp.yandex.ru";
        String smtpPort="25";
        Properties props = System.getProperties();
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.mime.charset", ENCODING);
        Session session = Session.getDefaultInstance(props, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Напоминание");
            message.setText(text);
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    static class MyAuthenticator extends Authenticator {
        private String user;
        private String password;

        MyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            String user = this.user;
            String password = this.password;
            return new PasswordAuthentication(user, password);
        }
    }

    private class CheckUp  extends Thread{

        private CheckUp(){
        }


        public void run(){
            while (true){
                Date date=new Date();
                for (int i=cl-1; i>=0; i--){
                    if (client.get(i).date.before(date)||client.get(i).date.equals(date)){
                        client.remove(i);
                        cl--;
                    }
                }
                try{
                    socket = new Socket("localhost", 5470);
                    DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                    byte[] buf;
                    String s="check";
                    buf=s.getBytes();
                    din.writeInt(s.length());
                    din.write(buf,0,s.length());
                    din.flush();
                    socket.close();
                }
                catch(IOException e){
                    for (int i=0;i<cl;i++){
                            send(client.get(i).email,txt);
                    }
                    System.exit(0);
                }
                for (int i=wait-1; i>=0; i--){
                    try{
                        Socket socket = new Socket(waitAddress.get(i), waitPort.get(i));
                        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                        din.writeInt(0);
                        din.flush();
                        socket.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        byte[] buf;
                        String s="quitA";
                        buf=s.getBytes();
                        try {
                            socket = new Socket("localhost", 5470);
                            DataOutputStream din = new DataOutputStream(socket.getOutputStream());
                            din.writeInt(s.length());
                            din.write(buf,0,s.length());
                            s=waitAddress.get(i);
                            buf=s.getBytes();
                            din.writeInt(s.length());
                            din.write(buf,0,s.length());
                            din.writeInt(waitPort.get(i));
                            din.flush();
                            socket.close();
                            int cl1=cl-1;
                            for (int j=cl1; j>=0; j--){
                                if (client.get(j).waitadress.contains(waitAddress.get(i))&&client.get(j).waitport==waitPort.get(i)){
                                    socket = new Socket("localhost", 5470);
                                    din = new DataOutputStream(socket.getOutputStream());
                                    s="client";
                                    buf=s.getBytes();
                                    din.writeInt(s.length());
                                    din.write(buf,0,s.length());
                                    din.writeInt(client.get(j).date.getYear()+1900);
                                    din.writeInt(client.get(j).date.getMonth()+1);
                                    din.writeInt(client.get(j).date.getDay()+17);
                                    din.writeInt(client.get(j).date.getHours());
                                    din.writeInt(client.get(j).date.getMinutes());
                                    buf=client.get(j).email.getBytes();
                                    din.writeInt(client.get(j).email.length());
                                    din.write(buf,0,client.get(j).email.length());
                                    buf=client.get(j).text.getBytes();
                                    din.writeInt(client.get(j).text.length());
                                    din.write(buf,0,client.get(j).text.length());
                                    socket.close();
                                    din.flush();
                                    client.removeElementAt(j);
                                    cl--;
                                }
                            }
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Clients{
        String text,email;
        String waitadress;
        int waitport;
        Date date;
    }

    public static void main(String[] args) throws Exception {
        Check em=new Check();


    }
}

