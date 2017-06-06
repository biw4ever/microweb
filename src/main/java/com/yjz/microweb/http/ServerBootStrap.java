package com.yjz.microweb.http;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.slf4j.Logger;

import com.yjz.microweb.transport.HttpServer;
import com.yjz.microweb.transport.HttpServerConf;

public class ServerBootStrap
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ServerBootStrap.class);
    
    public static void bootStrap()
    {
        try
        {  
            /**************************************************************************************/
            logger.info("Start loading microweb.properties.");
            
            Properties properties = new Properties();
            properties.load(ServerBootStrap.class.getClassLoader().getResourceAsStream("microweb.properties"));
            
            String serverPortStr = properties.getProperty("server.port");
            int serverPort = serverPortStr == null ? 8080 : Integer.parseInt(serverPortStr);
            
            String servletContextName = properties.getProperty("servletcontext.name");
            
            String sslEnabledStr = properties.getProperty("ssl.enabled");
            boolean sslEnabled = sslEnabledStr == null ? false : Boolean.valueOf(sslEnabledStr);
            
            String corePoolSizeStr = properties.getProperty("corepool.size");
            int corePoolSize = corePoolSizeStr == null ? 100 : Integer.parseInt(corePoolSizeStr);
            
            String maximumPoolSizeStr = properties.getProperty("maximumPoolSize");
            int maximumPoolSize = maximumPoolSizeStr == null ? 1000 : Integer.parseInt(maximumPoolSizeStr);
            
            String poolQueueSizeStr = properties.getProperty("poolQueueSizeStr");
            int poolQueueSize = poolQueueSizeStr == null ? 100 : Integer.parseInt(poolQueueSizeStr);
            
            logger.info("Finished loading microweb.properties.");
            /**************************************************************************************/
            
            /**************************************************************************************/
            logger.info("Start Initializing Webapp Context.");
            HttpCoreServer.instance().init(servletContextName);
            logger.info("Webapp Context has been initialized.");
            /**************************************************************************************/
            
            /**************************************************************************************/
            HttpServerConf conf = new HttpServerConf();
            conf.setPort(serverPort);
            conf.setSslEnabled(sslEnabled);
            conf.setCorePoolSize(corePoolSize);
            conf.setMaximumPoolSize(maximumPoolSize);
            conf.setPoolQueueSize(poolQueueSize);
            
            logger.info("Start Initializing httpServer.");
            HttpServer httpServer = new HttpServer(conf);
            httpServer.doStart();
            logger.info("HttpServer has been initialized.");
            /**************************************************************************************/
        }
        catch (CertificateException | IOException e)
        {
            e.printStackTrace();
        }
        
    }
}
