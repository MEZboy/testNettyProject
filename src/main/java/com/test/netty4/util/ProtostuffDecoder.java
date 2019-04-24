package com.test.netty4.util;

import java.util.List;

import com.test.netty4.model.Person;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffDecoder extends ByteToMessageDecoder {

	/**
	 * ProtostuffDecoder用将ByteBuf中的数据转换成Person对象
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
		Person person = schema.newMessage();
		byte[] array = new byte[in.readableBytes()];
		in.readBytes(array);
		ProtobufIOUtil.mergeFrom(array, person, schema);
		out.add(person);
	}
}