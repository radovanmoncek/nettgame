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
        //Await first "magic" byte for PDUType identification
        if(in.readableBytes() >= 1){
            in.markReaderIndex();
            PDUType type = PDUType.valueOf((byte) in.readUnsignedByte());

            //Determine, if any PDU body data are to be transported (their length is based on Integer length identifier which has to be awaited)
            if(!type.isEmpty() && (in.readableBytes() >= 5) && (in.readableBytes() < in.readInt())) {
                in.resetReaderIndex();
                return;
            }
            byte [] outBytes = new byte[in.readableBytes()];
            in.readBytes(outBytes);
            PDU outPDU = new PDU();
            outPDU.setData(Unpooled.wrappedBuffer(outBytes));
            outPDU.setPDUType(type);
            outPDU.setAddress(channelHandlerContext.channel().remoteAddress());
            out.add(outPDU);
        }
    }
}
