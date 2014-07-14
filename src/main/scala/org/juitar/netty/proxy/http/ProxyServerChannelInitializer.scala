package org.juitar.netty.proxy.http

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder}

/**
 * @author sha1n
 * @since 7/5/14
 */
class ProxyServerChannelInitializer(remoteHost: String, remotePort: Int) extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel) {
    val pipeline = ch.pipeline

    //    pipeline.addLast("logger", new LoggingHandler(LogLevel.DEBUG))
    pipeline.addLast("decoder", new HttpRequestDecoder)
    pipeline.addLast("aggregator", new HttpObjectAggregator(65536))
    pipeline.addLast("handler", new InboundChannelHandler(remoteHost, remotePort))
  }

}

