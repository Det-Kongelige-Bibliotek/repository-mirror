package dk.kb.dbStuff;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

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
import java.io.BufferedReader;
import java.io.FileReader;




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

	ApiClient htclient = new ApiClient();

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            String queue = System.getProperty("queue.load.name");
            if (queue == null ) queue = consts.getConstants().getProperty("queue.load.name");
            Destination destination = session.createQueue(queue);

            consumer = session.createConsumer(destination);

            while (true) {
                String msg = null;
                try {
                    logger.info("Waiting for next message");
                    Message message = consumer.receive();
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        msg = textMessage.getText();
                    } else {
                        msg = message.toString();
                    }

		    String reg = ";";
		    String[] arr = msg.split(reg);

		    String collection = arr[0];
		    String repository = arr[1];
		    String branch     = arr[2];
		    String target     = arr[3];
		    String document   = arr[4];
		    String op         = arr[5];

                    logger.info("Received: " + msg);

		    String db_uri = consts.getConstants().getProperty(target);
		    String credField = target + ".credentials";

                    logger.info("credField: " + credField);

		    String user   = consts.getConstants().getProperty(credField).split(reg)[0];
		    String passwd = consts.getConstants().getProperty(credField).split(reg)[1];

		    String URI = db_uri + collection + "/" + document;

		    String file = consts.getConstants().getProperty("data.home") + repository + "/" + document;

		    htclient.setLogin(user,passwd);
                    logger.info(op + " " + URI);
                    logger.info("File " + file);

		    String res = "";
		    if(op.matches(".*PUT.*")) { 
			logger.info("operation = " + op);
			try {
			    String text = readFile(file);
			    res = htclient.restPut(text, URI);
			    logger.info("res: " + res);
			} catch (IOException fileprblm) {
			    logger.error("Error reading: " + file);
			    logger.error("Problem: " + fileprblm);
			}
		    } else if(op.matches(".*DELETE.*")) { 
			logger.info("delete operation = " + op);
			res = htclient.restDelete(URI);
		    } else if(op.matches(".*GET.*")) { 
			logger.info("GET operation = " + op);
		    } else {
			res =  htclient.restHead(URI);
		    }

                    logger.info(op + " result: " + res);

                } catch (Exception e) {
                    logger.error("Error connecting " + e);
                    logger.error("Waiting 6 sek and try again");

                    e.printStackTrace();
                    Thread.sleep(6000);
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

    static String readFile(String fileName) throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(fileName));
	try {
	    StringBuilder sb = new StringBuilder();
	    String line = br.readLine();

	    while (line != null) {
		sb.append(line);
		sb.append("\n");
		line = br.readLine();
	    }
	    return sb.toString();
	} finally {
	    br.close();
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
	Logger logger = Logger.getLogger(RunLoad.class);
	logger.info("logging at level " + level + " in file " + file + "\n");
	return logger;
    }
  
}
