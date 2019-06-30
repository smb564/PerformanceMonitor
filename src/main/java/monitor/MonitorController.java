package monitor;

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
    // This is a timer (DropWizard Timer) which collects metrics for the measuring interval period
    private final String IAR_OBJECT = "metrics:name=InterArrivalRate";
    private final String AJP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8009";
    private final String HTTP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8080";
    private final String AJP_THREAD_POOL_OBJECT = "Catalina:type=ThreadPool,name=\"ajp-bio-8009\"";
    private final String EXECUTOR_THREAD_POOL_OBJECT = "Catalina:type=Executor,name=tomcatThreadPool";
    private final String RBE_OBJECT = "rbe:type=RBE";
    private final String ERROR_OBJECT = "metrics:name=errors";

    private final String GAUGE_MEAN_LATENCY = "metrics:name=mean_response_time";
    private final String GAUGE_P99_LATENCY = "metrics:name=p99_latency";
    private final String GAUGE_STD_DEV_LATENCY = "metrics:name=std_deviation";
    private final String GAUGE_REQUEST_COUNT = "metrics:name=total_requests";

    private final String NETTY_PERFORMANCE_OBJECT = "metrics:name=response_times";
    private final String NETTY_THREAD_POOL = "thirdThreadPool:type=CustomThreadPool";

    private JMXClient tomcatClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.11:9000/jmxrmi");
    private JMXClient apacheClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.10:9010/jmxrmi");
    private JMXClient rbeClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.6:9010/jmxrmi");
    private JMXClient nettyClient = new JMXClient("service:jmx:rmi:///jndi/rmi://192.168.32.11:9010/jmxrmi");
//     private JMXClient nettyClient = new JMXClient("service:jmx:rmi:///jndi/rmi://localhost:9010/jmxrmi");


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
            Number stddev = rbeClient.getParameter("StdDev", PERFORMANCE_OBJECT);
            Number errors = rbeClient.getParameter("Count", ERROR_OBJECT);
            return new Number[]{iar, request_count, mean_latency, latency_99, stddev, errors};


        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            // It is not the best to capture exceptions like this. Okay for POC level.
            e.printStackTrace();
        }

        return new Number[]{-1, -1};
    }

    @RequestMapping(value = "/performance-netty", method = RequestMethod.GET)
    public Number[] getPerformanceNetty(){
        Number iar = -1;
        Number errors = -1;
        try {
            Number request_count = nettyClient.getParameter("Count", NETTY_PERFORMANCE_OBJECT);
            Number mean_latency = nettyClient.getParameter("Mean", NETTY_PERFORMANCE_OBJECT);
            Number latency_99 = nettyClient.getParameter("99thPercentile", NETTY_PERFORMANCE_OBJECT);
            Number stddev = nettyClient.getParameter("StdDev", NETTY_PERFORMANCE_OBJECT);
            return new Number[]{iar, request_count, mean_latency, latency_99, stddev, errors};
        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return new Number[]{-1, -1};
    }

    @RequestMapping(value = "setThreadPoolNetty", method = RequestMethod.PUT)
    public boolean setThreadPoolNetty(@RequestParam(value = "size") int size){
        try {
            return nettyClient.setParameter("PoolSize", size, NETTY_THREAD_POOL);
        } catch (MalformedObjectNameException | AttributeNotFoundException | InvalidAttributeValueException | ReflectionException | IOException | InstanceNotFoundException | MBeanException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping(value = "getThreadPoolNetty", method = RequestMethod.GET)
    public Number getThreadPoolNetty(){
        try {
            return nettyClient.getParameter("PoolSize", NETTY_THREAD_POOL);
        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @RequestMapping(value = "/performance-mi", method = RequestMethod.GET)
    public Number[] getPerformanceMI(){
        // return the performance metrics collected only for the measurement interval
        System.out.println("Querying the current system performance mi");
        // Read the values using the JMX Client

        try {
            Number iar = -1;
            Number request_count = rbeClient.getParameter("Value", GAUGE_REQUEST_COUNT);
            Number mean_latency = rbeClient.getParameter("Value", GAUGE_MEAN_LATENCY);
            Number latency_99 = rbeClient.getParameter("Value", GAUGE_P99_LATENCY);
            Number stddev = rbeClient.getParameter("Value", GAUGE_STD_DEV_LATENCY);
            Number errors = rbeClient.getParameter("Count", ERROR_OBJECT);
            return new Number[]{iar, request_count, mean_latency, latency_99, stddev, errors};


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

    @RequestMapping(value = "/reconnect-netty", method = RequestMethod.GET)
    public boolean reconnectNetty(){
        System.out.println("Reconnecting to Netty Server...");
        return nettyClient.reconnect();
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
