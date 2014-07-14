package org.juitar.netty.proxy.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelOption, EventLoopGroup}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
 * @author sha1n
 * @since 7/5/14
 */
object Launcher {

  def main(args: Array[String]) {
    startServer(port = 9090, remoteHost = "localhost", remotePort = 8080)
  }

  def startServer(port: Int, remoteHost: String, remotePort: Int) {
    val bossGroup: EventLoopGroup = new NioEventLoopGroup(1)
    val workerGroup: EventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime.availableProcessors)
    try {
      val bootstrap = new ServerBootstrap

      bootstrap.option(ChannelOption.valueOf("SO_BACKLOG"), 1000)
        .group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ProxyServerChannelInitializer(remoteHost, remotePort))
        .childOption(ChannelOption.valueOf("AUTO_READ"), false)

      bootstrap.bind(port)
        .sync()
        .channel
        .closeFuture
        .sync()
    }
    finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}
