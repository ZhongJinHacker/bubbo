package com.grady.bubbo.common.codec;

import com.grady.bubbo.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author gradyjiang
 * @Date 2019/11/21 - 下午7:35
 */
public class RpcDecoder<T> extends ByteToMessageDecoder {


    private Class<T> clazz =
            (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
        }
        //将ByteBuf转换为byte[]
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        //将data转换成object
        Object obj = SerializationUtil.deserialize(data, clazz);
        out.add(obj);
    }
}
