package server.game.docker.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.net.decoders.GameDecoder;
import server.game.docker.net.encoders.GameEncoder;
import server.game.docker.net.PDUHandler;
import server.game.docker.server.net.handlers.GameServerHandler;

public class GameServer {
    private final int port;
    private final PDUHandler PDUHandler;

    public GameServer(int port) {
        this.port = port;
        PDUHandler = new PDUHandler();
    }

    public GameServer(String [] args) {
        this(4321);
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.ERROR), new GameDecoder(), new GameEncoder(), new GameServerHandler(PDUHandler));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.printf("GameServer running on port %d\n", port);

            //Blocking method
            future.channel().closeFuture().sync();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
