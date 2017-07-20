import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;

public class Main {

    final static int PORT = 9980;
    final static String INET_ADDR = "224.1.1.1";
    final static int TIMEOUT = 3000; // milliseconds
    final static int BUF_SIZE = 256;


    final String ESC = "escape";
    final String ENTER = "enter";
    final String DELETE = "delete";
    final String UP_A = "aup";
    final String DOWN_A = "adown";
    final String LEFT_A = "aleft";
    final String RIGHT_A = "aright";
    final String SHIFT = "shift";

    final String LEFT_CLICK = "left_click";
    final String RIGHT_CLICK = "right_click";

    final String UP = "up";
    final String DOWN = "down";
    final String LEFT = "left";
    final String RIGHT = "right";

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

            String input = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.printf("%s len:%d%n", input, input.length());
            mainLoop:
            for (int i = 0; i < input.length(); i++) {
                boolean capital = false;
                int code = -1;
                char ch = input.charAt(i);
                if (ch == '\\') {
                    if (i < input.length() - 1 && input.charAt(i + 1) != '\\') {
                        for (int j = i + 1; j < input.length(); j++) {
                            if (input.charAt(j) == '\\') {
                                String action = input.substring(i + 1, j);
                                String[] actions = action.split(" ");
                                if(actions.length == 0) // If we couldn't make a non-empty array
                                      continue mainLoop;

                                i += action.length() + 1;
                                switch (actions[0]) {
                                    case ESC:
                                        code = KeyEvent.VK_ESCAPE;
                                        break;
                                    case ENTER:
                                        code = KeyEvent.VK_ENTER;
                                        break;
                                    case DELETE:
                                        code = KeyEvent.VK_BACK_SPACE;
                                        break;
                                    case UP_A:
                                        code = KeyEvent.VK_UP;
                                        break;
                                    case DOWN_A:
                                        code = KeyEvent.VK_DOWN;
                                        break;
                                    case LEFT_A:
                                        code = KeyEvent.VK_LEFT;
                                        break;
                                    case RIGHT_A:
                                        code = KeyEvent.VK_RIGHT;
                                        break;
                                    case SHIFT:
                                        capital = true;
                                        System.out.println("CH "+actions[1].charAt(0));
                                        code = KeyEvent.getExtendedKeyCodeForChar(actions[1].charAt(0));
                                        break;
                                    default: {
                                        Point location = MouseInfo.getPointerInfo().getLocation();
                                        int x = location.x;
                                        int y = location.y;
                                        switch (actions[0]) {
                                            case UP:
                                                robot.mouseMove(x, y - Integer.parseInt(actions[1]));
                                                break;
                                            case DOWN:
                                                robot.mouseMove(x, y + Integer.parseInt(actions[1]));
                                                break;
                                            case LEFT:
                                                robot.mouseMove(x - Integer.parseInt(actions[1]), y);
                                                break;
                                            case RIGHT:
                                                robot.mouseMove(x + Integer.parseInt(actions[1]), y);
                                                break;
                                            case RIGHT_CLICK:
                                                robot.mousePress(InputEvent.BUTTON3_MASK);
                                                robot.delay(40);
                                                robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                                break;
                                            case LEFT_CLICK:
                                                robot.mousePress(InputEvent.BUTTON1_MASK);
                                                robot.delay(40);
                                                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                                break;
                                        }
                                        continue mainLoop;
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        code = KeyEvent.VK_BACK_SLASH;
                        i++;
                    }
                } else {
                    if (Character.isUpperCase(ch)) {
                        capital = true;
                    }
                    code = KeyEvent.getExtendedKeyCodeForChar(ch);
                }
                if (capital)
                    robot.keyPress(KeyEvent.VK_SHIFT);

                robot.keyPress(code);
                robot.delay(40);
                robot.keyRelease(code);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }

    public static void setupMulticast() throws UnknownHostException {
        // Get the address that we are going to connect to.
        InetAddress addr = InetAddress.getByName(INET_ADDR);

        // Open a new DatagramSocket, which will be used to send the data.
        try (DatagramSocket serverSocket = new DatagramSocket()) {
            String msg = "message";

            // Create a packet that will contain the data
            // (in the form of bytes) and send it.
            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                    msg.getBytes().length, addr, PORT);

            serverSocket.send(msgPacket);
            System.out.println("Server sent packet with msg: " + msg);

            // Receiving acknowledgement
            byte[] buf = new byte[BUF_SIZE];
            msgPacket = new DatagramPacket(buf, buf.length);
            serverSocket.setSoTimeout(TIMEOUT);

            serverSocket.receive(msgPacket);
            String received = new String(msgPacket.getData(), 0, msgPacket.getLength());
            System.out.println("Received: " + received);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, AWTException {
        new Main();
    }
}
