import java.io.IOException;
import java.util.Scanner;
import java.net.Socket;
import java.io.DataOutputStream;

public class Client {

    Client() throws IOException {
        run();
    }

    private void run() throws IOException {
        Socket socket = new Socket("localhost", 5470);
        try{
            DataOutputStream din = new DataOutputStream(socket.getOutputStream());
            Scanner in = new Scanner(System.in);
            int year =0, month=0, day=0,hour=24, minute=60;
            String email=" ", text;
            System.out.println("Год");
            while (year<2000||year>3000){
                year=in.nextInt();
            }
            System.out.println("Месяц");
            while (month<1||month>12){
                month=in.nextInt();
            }
            System.out.println("День");
            while (day<1||day>31){
                day=in.nextInt();
                if ((month==4||month==6||month==9||month==11)&&day==31){
                    day=0;
                }
                if (month==2){
                    if ((year%4==0&&day>29)||(year%4!=0&&day>28)){
                        day=0;
                    }
                }

            }
            System.out.println("Час");
            while (hour<0||hour>23){
                hour=in.nextInt();
            }
            System.out.println("Минуты");
            while (minute<0||minute>59){
                minute=in.nextInt();
            }
            System.out.println("Почта");
            while (!email.contains("@")){
                email=in.nextLine();
            }
            System.out.println("Текст");
            text=in.nextLine();
            byte[] buf;
            String s="client";
            buf=s.getBytes();
            din.writeInt(s.length());
            din.write(buf,0,s.length());
            din.writeInt(year);
            din.writeInt(month);
            din.writeInt(day);
            din.writeInt(hour);
            din.writeInt(minute);
            buf=email.getBytes();
            din.writeInt(email.length());
            din.write(buf,0,email.length());
            buf=text.getBytes();
            din.writeInt(text.length());
            din.write(buf,0,text.length());
            din.flush();
            socket.close();

        }
        catch (Exception e) {
            e.printStackTrace();
            socket.close();

        }


    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }
}
