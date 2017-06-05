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
            // 
            logger.info("Start Initializing Webapp Context.");
            HttpCoreServer.instance().init();
            logger.info("Webapp Context has been initialized.");
            
            Properties properties = new Properties();
            properties.load(ServerBootStrap.class.getClassLoader().getResourceAsStream("microweb.properties"));
            String serverPortStr = properties.get("server.port").toString();
            int serverPort = serverPortStr == null ? 8080 : Integer.parseInt(serverPortStr);
            
            HttpServerConf conf = new HttpServerConf();
            conf.setPort(serverPort);
            HttpServer httpServer = new HttpServer(conf);
            
            httpServer.doStart();
            
        }
        catch (CertificateException | IOException e)
        {
            e.printStackTrace();
        }
        
    }
}
