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

public class Email {
    private Socket socket;
    private ServerSocket serverSocket;
    private int port;

    private Email() throws IOException {
        Scanner in = new Scanner(System.in);
        port=in.nextInt();
        serverSocket = new ServerSocket(port);
        socket = new Socket("localhost", 5470);
        DataOutputStream din = new DataOutputStream(socket.getOutputStream());
        byte[] buf;
        String s="mail";
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
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        while (true){
            run();
        }
    }

    class ShutdownHook extends Thread {

        public void run() {
            byte[] buf;
            String s="quitB";
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


    private void run() throws IOException {
        byte[] buf=new byte[512];
        socket=serverSocket.accept();
        DataInputStream os = new DataInputStream(socket.getInputStream());
        int a=os.readInt();
        os.read(buf,0,a);
        String to=new String(buf, 0, a);
        a=os.readInt();
        os.read(buf,0,a);
        os.close();
        socket.close();
        String text=new String(buf, 0, a);
        final String ENCODING = "UTF-8";
        Authenticator auth = new MyAuthenticator("someemail", "somepassword");
        String from = "someemail@yandex.ru";
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


    public static void main(String[] args) throws Exception {
        Email em=new Email();


    }
}
