package dk.kb.dbStuff;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.http.HttpEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

/**
 * Created by dgj on 17-11-2016.
 * Stolen by slu on 11-01-2019
 */
public class RunLoad {

    private static ConfigurableConstants consts = ConfigurableConstants.getInstance();

    public static void main(String args[]) {

        Logger logger = configureLog4j();

	String host = System.getProperty("queue.uri");
        if (host == null) host = consts.getConstants().getProperty("queue.uri");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            String queue = System.getProperty("queue.load.name");
            if (queue == null ) queue = consts.getConstants().getProperty("queue.load.name");
            Destination destination = session.createQueue(queue);

            consumer = session.createConsumer(destination);

            while (true) {
                String id = null;
                try {
                    logger.info("Waiting for next message");
                    Message message = consumer.receive();
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        id = textMessage.getText();
                    } else {
                        id = message.toString();
                    }
                    logger.info("Received: " + id);
                } catch (Exception e) {
                    logger.error("Error connecting "+e);
                    logger.error("Waiting 60 sek and try again");
                    sendToFailedQueue(id,"Error connecting "+e.getMessage(),logger);
                    e.printStackTrace();
                    Thread.sleep(60000);
                }
            }
        } catch (Exception e) {
            logger.fatal("Stopping execution ",e);
        } finally {
            try {
                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                logger.fatal("error while shutting donw ",e);
            }
        }
    }

    private static Logger configureLog4j() {

	String level = consts.getConstants().getProperty("queue.loglevel");
	if (System.getProperty("queue.loglevel") != null ) level = System.getProperty("queue.loglevel");

	String file = consts.getConstants().getProperty("queue.logfile");
	if (System.getProperty("queue.logfile") != null) file = System.getProperty("queue.logfile");

	Properties props = new Properties();
	props.put("log4j.rootLogger", level+", FILE");
	props.put("log4j.appender.FILE", "org.apache.log4j.DailyRollingFileAppender");
	props.put("log4j.appender.FILE.File",file);
	props.put("log4j.appender.FILE.ImmediateFlush","true");
	props.put("log4j.appender.FILE.Threshold",level);
	props.put("log4j.appender.FILE.Append","true");
	props.put("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
	props.put("log4j.appender.FILE.layout.conversionPattern","[%d{yyyy-MM-dd HH.mm:ss}] %-5p %C{1} %M: %m %n");
	PropertyConfigurator.configure(props);
	Logger logger = Logger.getLogger(RunPull.class);
	logger.info("logging at level " + level + " in file " + file + "\n");
	return logger;
    }

    private static void sendToFailedQueue(String id, String msg, Logger logger) {
        JMSstuff producer = null;
        try {
            producer = new JMSstuff(
				    consts.getConstants().getProperty("cop2.solrizr.queue.host"),
				    consts.getConstants().getProperty("cop2.solrizr.queue.update")+".failed");
            producer.sendMessage(id + "|" + msg);
        } catch (JMSException e) {
            logger.error("Error sending fail message ",e);
        } finally {
            if (producer != null) {
                producer.shutDownPRoducer();
            }
        }
    }
  
}
