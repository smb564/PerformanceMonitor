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
    private final String IAR_OBJECT = "metrics:name=InterArrivalRate";
    private final String AJP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8009";
    private final String HTTP_CONNECTOR_OBJECT = "Catalina:type=Connector,port=8080";
    private final String AJP_THREAD_POOL_OBJECT = "Catalina:type=ThreadPool,name=\"ajp-bio-8009\"";

    @RequestMapping(value = "/performance", method=RequestMethod.GET)
    public Number[] performance(){
        System.out.println("Querying the current system performance");
        // Read the values using the JMX Client
        try {
            Number iar = JMXClient.getInstance().getParameter("Value", IAR_OBJECT);
            // TODO: this is not the request count for the window. It is the total request count
            Number request_count = JMXClient.getInstance().getParameter("Count", PERFORMANCE_OBJECT);
            Number mean_latency = JMXClient.getInstance().getParameter("Mean", PERFORMANCE_OBJECT);
            Number latency_99 = JMXClient.getInstance().getParameter("99thPercentile", PERFORMANCE_OBJECT);

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
            return JMXClient.getInstance().setParameter(name, value, AJP_CONNECTOR_OBJECT);
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
            if (name.equals("currentThreadCount"))
                return JMXClient.getInstance().getParameter(name, AJP_THREAD_POOL_OBJECT);
            return JMXClient.getInstance().getParameter(name, AJP_CONNECTOR_OBJECT);
        } catch (MalformedObjectNameException | AttributeNotFoundException | MBeanException | ReflectionException | InstanceNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
