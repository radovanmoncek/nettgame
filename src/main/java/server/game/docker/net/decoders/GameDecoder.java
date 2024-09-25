package server.game.docker.net.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.net.InetSocketAddress;
import java.util.List;

public class GameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        //Await first two bytes for maximum size identification
        //Await first "magic" byte for PDUType identification
        if(in.readableBytes() >= 1){
            in.markReaderIndex();
            PDUType type = PDUType.valueOf((byte) in.readUnsignedByte());

            if(!type.isEmpty() && (in.readableBytes() >= 5) && (in.readableBytes() < in.readInt())) {
                in.resetReaderIndex();
                return;
            }
            //Await transport of the whole PDU body data
            //Prepare rest of PDU body data
            byte [] outBytes = new byte[in.readableBytes()];
            in.readBytes(outBytes);
            PDU outPDU = new PDU(type, channelHandlerContext.channel().remoteAddress(), ((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getPort(), null);
            outPDU.setByteBuf(Unpooled.wrappedBuffer(outBytes));
            out.add(outPDU);
        }
    }
}
