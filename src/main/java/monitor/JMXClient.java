package monitor;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

public class JMXClient {
    private static JMXClient client;
    private MBeanServerConnection mbsc;

    private JMXClient(String serviceUrl){
        try {
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            this.mbsc = jmxc.getMBeanServerConnection();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static JMXClient getInstance(){
        if (JMXClient.client == null){
            JMXClient.client = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.11:9000/jmxrmi");
        }

        return JMXClient.client;
    }

    public long getParameter(String name, String objectName) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        ObjectName mbean = new ObjectName(objectName);
        return ((Integer) mbsc.getAttribute(mbean, name));
    }

    public boolean setParameter(String name, long value, String objectName) throws MalformedObjectNameException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException, IOException, InstanceNotFoundException, MBeanException {
        ObjectName mbean = new ObjectName(objectName);
        mbsc.setAttribute(mbean, new Attribute(name, value));

        // check whether it was successful
        return getParameter(name, objectName)==value;
    }


}
