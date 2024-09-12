package server.game.docker.examples.client;

class GameClientLauncher {
    // @Deprecated
    // public GameClientLauncher(String iPAddress){
    // }

    public static void main(String[] args) {
        // System.out.println("The client has started");

        // GameClientLauncher gameClient;
        // try {
            // gameClient = new GameClientLauncher(/*InetAddress.getLocalHost().getHostAddress()"127.0.0.1"*/"localhost");
            // new SimpleRTSGameClient(gameClient.socket, gameClient.iPAddress);
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
        new SimpleRTSGameClient(args);
    }
}
