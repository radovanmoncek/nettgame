package server.game.docker.net.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.net.dto.ChatMessage;
import server.game.docker.net.parents.encoders.PDUHandlerEncoder;
import server.game.docker.net.pdu.PDU;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class PDUStringEncoder implements PDUHandlerEncoder {
    @Override
    public void encode(PDU in, Channel out) {
        ChatMessage chatMessage = (ChatMessage) in.getData();
        ByteBuf authorNameByteBuffer = ByteBufUtil.encodeString(Unpooled.buffer(8).alloc(), CharBuffer.wrap(chatMessage.getAuthorName() != null? chatMessage.getAuthorName() : ""), Charset.defaultCharset());
        in.setData(ByteBufUtil.encodeString(
                Unpooled.buffer(Long.BYTES + 8 + 64).writeLong(chatMessage.getAuthorID() != null? chatMessage.getAuthorID() : -1L).writeBytes(authorNameByteBuffer).alloc(),
                CharBuffer.wrap(chatMessage.getMessage()),
                Charset.defaultCharset()
        ));
        out.writeAndFlush(in);
    }
}
