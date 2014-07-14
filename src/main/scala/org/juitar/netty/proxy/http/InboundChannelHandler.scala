package org.juitar.netty.proxy.http

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.handler.codec.http._

/**
 * @author sha1n
 * @since 7/5/14
 */
class InboundChannelHandler(remoteHost: String, remotePort: Int) extends ChannelHandlerAdapter {

  @volatile private var outboundChannel: Channel = _

  override def channelActive(ctx: ChannelHandlerContext) {
    val inboundChannel = ctx.channel
    val bootstrap = new Bootstrap

    bootstrap
      .group(inboundChannel.eventLoop)
      .channel(ctx.channel.getClass)
      .handler(new OutboundChannelHandler(inboundChannel))
      .option(ChannelOption.valueOf("AUTO_READ"), false)

    val channelFuture = bootstrap.connect(remoteHost, remotePort)
    outboundChannel = channelFuture.channel

    channelFuture.addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          inboundChannel.read
        }
        else {
          inboundChannel.close
        }
      }
    })

    outboundChannel.pipeline.addLast(new HttpClientCodec)
  }

  override def write(ctx: ChannelHandlerContext, msg: AnyRef, promise: ChannelPromise) {
    super.write(ctx, msg, promise)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) {
    if (outboundChannel.isActive) {
      outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener {
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
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    if (outboundChannel != null) {
      InboundChannelHandler.closeOnFlush(outboundChannel)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    InboundChannelHandler.closeOnFlush(ctx.channel)
  }

}

object InboundChannelHandler {

  private[proxy] def closeOnFlush(ch: Channel) = {
    if (ch.isActive) {
      ch.writeAndFlush(Unpooled.EMPTY_BUFFER)
        .addListener(ChannelFutureListener.CLOSE)
    }
  }
}


