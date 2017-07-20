import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {

    final int PORT = 9980;
    final String ESC = "escape";
    final String ENTER = "enter";
    final String DELETE = "delete";

    Robot robot;

    DatagramSocket serverSocket;
    byte[] receiveData = new byte[1024];

    public Main() throws IOException, AWTException {
        robot = new Robot();
        serverSocket = new DatagramSocket(PORT);

        System.out.println("Server started");
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String input = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            System.out.println(input);
            for (int i = 0; i < input.length(); i++) {
                int code = -1;
                char ch = input.charAt(i);
                if(ch == '\\') {
                    if(i < input.length()-1 && input.charAt(i+1) != '\\') {
                        for(int j = i+1; j < input.length(); j++) {
                            if (input.charAt(j) == '\\') {
                                String action = input.substring(i + 1, j);
                                switch (action) {
                                    case ESC:
                                        code = KeyEvent.VK_ESCAPE;
                                        break;
                                    case ENTER:
                                        code = KeyEvent.VK_ENTER;
                                        break;
                                    case DELETE:
                                        code = KeyEvent.VK_DELETE;
                                        break;
                                }
                                i += action.length() + 1;
                                break;
                            }
                        }
                    } else {
                        code = KeyEvent.VK_BACK_SLASH;
                        i++;
                    }
                } else
                    code = KeyEvent.getExtendedKeyCodeForChar(ch);

                robot.keyPress(code);
                robot.delay(40);
                robot.keyRelease(code);
            }
        }
    }

    public static void main(String[] args) throws IOException, AWTException {
        new Main();
    }
}
