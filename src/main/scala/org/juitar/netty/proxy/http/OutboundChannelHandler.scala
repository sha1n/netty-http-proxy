package org.juitar.netty.proxy.http

import io.netty.buffer.Unpooled
import io.netty.channel._

/**
 * @author sha1n
 * @since 7/5/14
 */
class OutboundChannelHandler(val inboundChannel: Channel) extends ChannelHandlerAdapter {

  override def channelActive(ctx: ChannelHandlerContext) {
    ctx.read
    ctx.write(Unpooled.EMPTY_BUFFER)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) {
    inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          ctx.channel.read
        }
        else {
          future.channel.close
        }
      }
    })
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    InboundChannelHandler.closeOnFlush(inboundChannel)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    InboundChannelHandler.closeOnFlush(ctx.channel)
  }
}
