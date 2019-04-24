package com.test.netty4.util;

import com.test.netty4.model.Person;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffEncoder extends MessageToByteEncoder<Person> {

	/**
	 * ProtostuffEncoder编码器，用于将Person对象编码成字节数组
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, Person msg, ByteBuf out) throws Exception {
		LinkedBuffer buffer = LinkedBuffer.allocate(1024);
		Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
		byte[] array = ProtobufIOUtil.toByteArray(msg, schema, buffer);
		out.writeBytes(array);
	}

}