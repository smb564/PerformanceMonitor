package monitor;

import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.management.*;
import java.io.IOException;

@RestController
public class MonitorController {

    private final String THREAD_POOL_OBJECT = "Catalina:type=Executor,name=tomcatThreadPool";
    private final String PERFORMANCE_OBJECT = "metrics:name=response_times";
    private final String IAR_OBJECT = "metrics:name=InterArrivalRate";
    private final String AJP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8009";
    private final String HTTP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8080";
    private final String AJP_THREAD_POOL_OBJECT = "Catalina:type=ThreadPool,name=\"ajp-bio-8009\"";
    private final String EXECUTOR_THREAD_POOL_OBJECT = "Catalina:type=Executor,name=tomcatThreadPool";
    private final String RBE_OBJECT = "rbe:type=RBE";



    private JMXClient tomcatClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.11:9000/jmxrmi");
    private JMXClient apacheClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.10:9010/jmxrmi");
    private JMXClient rbeClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.6:9010/jmxrmi");
//     private JMXClient rbeClient = new JMXClient("service:jmx:rmi:///jndi/rmi://localhost:9010/jmxrmi");


    @RequestMapping(value = "/performance", method=RequestMethod.GET)
    public Number[] performance(@RequestParam String server){
        System.out.println("Querying the current system performance");
        // Read the values using the JMX Client
        JMXClient client;

        if (server.equalsIgnoreCase("tomcat"))
            client = tomcatClient;
        else if (server.equalsIgnoreCase("apache"))
            client = apacheClient;
        else
            client = rbeClient;

        try {
            Number iar = -1;
            if (server.equalsIgnoreCase("tomcat"))
                iar = client.getParameter("Value", IAR_OBJECT);

            // TODO: this is not the request count for the window. It is the total request count
            Number request_count = client.getParameter("Count", PERFORMANCE_OBJECT);
            Number mean_latency = client.getParameter("Mean", PERFORMANCE_OBJECT);
            Number latency_99 = client.getParameter("99thPercentile", PERFORMANCE_OBJECT);

            return new Number[]{iar, request_count, mean_latency, latency_99};


        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            // It is not the best to capture exceptions like this. Okay for POC level.
            e.printStackTrace();
        }

        return new Number[]{-1, -1};
    }

    @RequestMapping(value = "/setparam", method = RequestMethod.PUT)
    public boolean setParam(@RequestParam(value = "name") String name, @RequestParam(value = "value") Number value){
        System.out.println("Setting the value of parameter "+ name + " to " + value);

        // find the relevant attribute and set the value
        // check whether the update succeed
        try {
            if (name.equalsIgnoreCase("keepAliveTimeout"))
                return tomcatClient.setParameter(name, value, HTTP_CONNECTOR_OBJECT);
            else
                return tomcatClient.setParameter(name, value, EXECUTOR_THREAD_POOL_OBJECT);

        } catch (MalformedObjectNameException | AttributeNotFoundException | InvalidAttributeValueException | ReflectionException | IOException | InstanceNotFoundException | MBeanException e) {
            e.printStackTrace();
        }

        return false;
    }

    @RequestMapping(value = "/getparam", method = RequestMethod.GET)
    public Number getParam(@RequestParam(value = "name") String name){
        System.out.println(String.format("Querying the values of parameter \"%s\"", name));
        // Query the JMX and get the param
        try {
            return tomcatClient.getParameter(name, EXECUTOR_THREAD_POOL_OBJECT);
        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @RequestMapping(value="/reconnect", method = RequestMethod.GET)
    public boolean reconnect(){
        System.out.println("Reconnecting to the Tomcat Server...");
        return tomcatClient.reconnect() & apacheClient.reconnect() & rbeClient.reconnect();
    }

    @RequestMapping(value = "/changeEBCount", method = RequestMethod.GET)
    public boolean changeEBCount(@RequestParam(value = "count") int count) throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException, IOException {
        rbeClient.invokeMethod("changeEBCount", new Object[]{count}, new String[]{int.class.getName()}, RBE_OBJECT);
        return true;
    }

    @RequestMapping(value = "/changeMix", method = RequestMethod.GET)
    public boolean changeMix(@RequestParam(value = "mix") int mix, @RequestParam(value = "count") int count) throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException, IOException {
        rbeClient.invokeMethod("changeMix", new Object[]{mix, count}, new String[]{int.class.getName(), int.class.getName()}, RBE_OBJECT);
        return true;
    }

    @RequestMapping(value = "/changeThinkTime", method = RequestMethod.GET)
    public boolean setThinkTime(@RequestParam(value = "scale") double tt_scale) throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException, IOException {
        rbeClient.invokeMethod("changeThinkTime", new Object[]{tt_scale}, new String[]{double.class.getName()}, RBE_OBJECT);
        return true;
    }

}
