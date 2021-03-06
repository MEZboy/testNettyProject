package com.test.netty3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;

import java.nio.ByteOrder;
import java.util.List;

/**
 * An encoder that prepends the length of the message. The length value is
 * prepended as a binary form.
 * <p>
 * For example, <tt>{@link LengthFieldPrepender}(2)</tt> will encode the
 * following 12-bytes string:
 * 
 * <pre>
 * +----------------+
 * | "HELLO, WORLD" |    原始消息长度12个字节
 * +----------------+
 * </pre>
 * 
 * into the following:
 * 
 * <pre>
 * +--------+----------------+
 * + 0x000C | "HELLO, WORLD" |     实际发送消息前面加了长度(0x000C为16进制，转换为10进制为12)
 * +--------+----------------+
 * </pre>
 * 
 * If you turned on the {@code lengthIncludesLengthFieldLength} flag in the
 * constructor, the encoded data would look like the following (12 (original
 * data) + 2 (prepended data) = 14 (0xE)):
 * 
 * <pre>
 * +--------+----------------+
 * + 0x000E | "HELLO, WORLD" |     如果lengthIncludesLengthFieldLength=true,则长度为14，多加了2个字节
 * +--------+----------------+
 * </pre>
 */
@Sharable
public class LengthFieldPrepender extends MessageToMessageEncoder<ByteBuf> {

	// 字节序
	private final ByteOrder byteOrder;
	// 标识长度字段的字节数
	private final int lengthFieldLength;
	// 内容长度是否包含lengthFieldLength
	private final boolean lengthIncludesLengthFieldLength;
	// 偏移量
	private final int lengthAdjustment;

	public LengthFieldPrepender(int lengthFieldLength) {
		this(lengthFieldLength, false);
	}

	public LengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength) {
		this(lengthFieldLength, 0, lengthIncludesLengthFieldLength);
	}

	public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment) {
		this(lengthFieldLength, lengthAdjustment, false);
	}

	public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength) {
		this(ByteOrder.BIG_ENDIAN, lengthFieldLength, lengthAdjustment, lengthIncludesLengthFieldLength);
	}

	public LengthFieldPrepender(ByteOrder byteOrder, int lengthFieldLength, int lengthAdjustment,
			boolean lengthIncludesLengthFieldLength) {
		// 表示长度的字节数只能为 1,2,3,4,8
		// lengthFieldLength=1 用1个字节表示长度 最大长度255
		// lengthFieldLength=2 用2个字节表示长度 最大长度65535
		// 以此类推.....
		if (lengthFieldLength != 1 && lengthFieldLength != 2 && lengthFieldLength != 3 && lengthFieldLength != 4
				&& lengthFieldLength != 8) {
			throw new IllegalArgumentException(
					"lengthFieldLength must be either 1, 2, 3, 4, or 8: " + lengthFieldLength);
		}
		ObjectUtil.checkNotNull(byteOrder, "byteOrder");

		this.byteOrder = byteOrder;
		this.lengthFieldLength = lengthFieldLength;
		this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
		this.lengthAdjustment = lengthAdjustment;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		// 消息中的内容长度+偏移量
		int length = msg.readableBytes() + lengthAdjustment;
		if (lengthIncludesLengthFieldLength) {
			// 如果lengthIncludesLengthFieldLength=true 则在加上表示长度的字节数
			length += lengthFieldLength;
		}

		// 逻辑检查
		if (length < 0) {
			throw new IllegalArgumentException("Adjusted frame length (" + length + ") is less than zero");
		}

		// 根据lengthFieldLength创建ByteBuf
		// 然后将计算后的长度大小写入到ByteBuf
		switch (lengthFieldLength) {
		case 1:
			if (length >= 256) {
				throw new IllegalArgumentException("length does not fit into a byte: " + length);
			}
			out.add(ctx.alloc().buffer(1).order(byteOrder).writeByte((byte) length));
			break;
		case 2:
			if (length >= 65536) {
				throw new IllegalArgumentException("length does not fit into a short integer: " + length);
			}
			out.add(ctx.alloc().buffer(2).order(byteOrder).writeShort((short) length));
			break;
		case 3:
			if (length >= 16777216) {
				throw new IllegalArgumentException("length does not fit into a medium integer: " + length);
			}
			out.add(ctx.alloc().buffer(3).order(byteOrder).writeMedium(length));
			break;
		case 4:
			out.add(ctx.alloc().buffer(4).order(byteOrder).writeInt(length));
			break;
		case 8:
			out.add(ctx.alloc().buffer(8).order(byteOrder).writeLong(length));
			break;
		default:
			throw new Error("should not reach here");
		}
		// 这里要添加一次引用计数器,因为在父类方法对msg进行了释放，这里要多引用一次
		out.add(msg.retain());
	}
}