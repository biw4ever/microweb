package com.yjz.microweb.transport;

import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 
 * @ClassName HttpServer
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author biw
 * @Date 2017年5月25日 下午4:35:11
 * @version 1.0.0
 */
public class HttpServer
{
    
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    private HttpServerConf conf;
    
    private static final int BACKLOG = 1024;
    
    private static final int TIMEOUT = 300;
    
    public boolean running = false;
    
    public HttpServer()
    {
        this.conf = new HttpServerConf();
    }
    
    public HttpServer(HttpServerConf conf)
    {
        this.conf = conf;
    }
    
    public void doStart()
        throws CertificateException, SSLException
    {
        final SslContext sslCtx;
        if (conf.sslEnabled)
        {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        }
        else
        {
            sslCtx = null;
        }
        
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final ExecutorService executorService = new ThreadPoolExecutor(conf.corePoolSize, conf.maximumPoolSize, 5,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(conf.poolQueueSize));
        
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, BACKLOG) // 设定最大连接队列
                .option(ChannelOption.SO_RCVBUF, 1024 * 256) // 设定数据接收缓冲区大小
                .option(ChannelOption.SO_SNDBUF, 1024 * 256) // 设定数据发送缓冲区大小
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 是否保持连接
                .childHandler(new HttpPipelineInitializer(executorService, sslCtx, TIMEOUT)); // 传入附带异步线程池的channelHandler
            
            logger.error("Start binding http server port on + " + conf.port);
            Channel channel = b.bind(conf.port).sync().channel(); // 绑定端口直到绑定完成
            logger.error("Http server port is bound.");
            
            channel.closeFuture().sync(); // 阻塞关闭操作
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
}
