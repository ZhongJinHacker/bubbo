package com.grady.bubbo.common.codec;

import com.grady.bubbo.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author gradyjiang
 * @Date 2019/11/21 - 下午7:35
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> clazz;

    public RpcEncoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object inob, ByteBuf out)
            throws Exception {
        //序列化
        if (clazz.isInstance(inob)) {
            byte[] data = SerializationUtil.serialize(inob);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
