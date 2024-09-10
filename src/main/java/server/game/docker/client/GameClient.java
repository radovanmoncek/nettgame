package server.game.docker.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

@Deprecated
class GameClient {
    //Config
    /**
     * The server port
     */
    // public static final Integer serverPort = 4321;
    /**
     * The client IP address
     */
    // public static final String iP = "127.0.0.1";
    private InetAddress iPAddress;
    /**
     * The client socket
     */
    private DatagramSocket socket;

    public InetAddress getiPAddress() {
        return iPAddress;
    }

    public GameClient(String iPAddress){
        try {
            socket = new DatagramSocket();
            this.iPAddress = InetAddress.getByName(iPAddress);
        } catch (UnknownHostException | SocketException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("The client has started");

        GameClient gameClient;
        // try {
            gameClient = new GameClient(/*InetAddress.getLocalHost().getHostAddress()"127.0.0.1"*/"localhost");
            new SimpleRTSGameClient(gameClient.socket, gameClient.iPAddress);
            // gameClient.buyGoldMine();
            // gameClient.sendData("koupit".getBytes());
        // } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        // }

        // try {
            // System.out.println("Connecting to the server");
            // Socket socket = new Socket(iP, serverPort);
            // PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            // printWriter.println("Client says hello");
            // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // System.out.println(String.format("Server says: %s", bufferedReader.readLine()));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    public void buyGoldMine(){
        // while(true){
            sendData("koupit".getBytes());
            byte [] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            System.out.println(String.format("Server sent: %s", new String(packet.getData()).trim()));
        // }
    }

    public void sendData(byte[] data){
        DatagramPacket packet = new DatagramPacket(data, data.length, iPAddress, 4321);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public Integer getPort(){
        return socket.getPort();
    }
}
