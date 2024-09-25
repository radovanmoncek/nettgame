package server.game.docker.net.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.PDUHandler;
import server.game.docker.net.pdu.PDUType;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

public class GameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        //Await first two bytes for maximum size identification
        //Await first "magic" byte for PDUType identification
        if(in.readableBytes() >= 1 /*5*/){
//            byte b1 = in.getByte(0), b2 = in.getByte(1);
//            byte [] temp = new byte[in.readableBytes()]/*in.asReadOnly().copy().array()*/;
//            for(int i = 0; i < in.readableBytes(); i++)
//                temp[i] = in.getByte(i);
            in.markReaderIndex();
            PDUType type = PDUType.valueOf((byte) /*(in.getByte(0) + in.getByte(1))*/in.readUnsignedByte());

            if(!type.isEmpty() && (in.readableBytes() >= 5) && (in.readableBytes() < in.readInt())) {
                in.resetReaderIndex();
                return;
            }
            //Await transport of the whole PDU body data
//            if(in.readableBytes() >= type.getMinimumTransportSize()) {
                //Prepare rest of PDU body data
            byte [] outBytes = new byte[/*type.getMinimumTransportSize()*/in.readableBytes()];
            in.readBytes(outBytes);
            PDU outPDU = new PDU(type, channelHandlerContext.channel().remoteAddress(), ((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getPort(), null/*actionPDUHandler.map(type).decode((ByteBuf) Unpooled.wrappedBuffer(outBytes)in.skipBytes(2)*//*)*/);
            outPDU.setByteBuf(Unpooled.wrappedBuffer(outBytes));
            out.add(outPDU);
//            }
//            else in.resetReaderIndex();
        }
//            out.add(new TimeExampleDTO(in.readUnsignedInt()));
    }

//    public static class TimeExampleDTO {
//        private final long value;
//
//        public TimeExampleDTO(long value) {
//            this.value = value;
//        }
//
//        public TimeExampleDTO() {
//            this(System.currentTimeMillis() / 1000L + 2208988800L);
//        }
//
//        @Override
//        public String toString(){
//            return new Date((value - 2208988800L) * 1000L).toString();
//        }
//
//        public long getValue() {
//            return value;
//        }
//    }
}
