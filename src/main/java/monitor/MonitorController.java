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
    private final String PERFORMANCE_OBJECT = "Catalina:type=GlobalRequestProcessor,name=\"ajp-bio-8009\"";

    @RequestMapping(value = "/performance", method=RequestMethod.GET)
    public long[] performance(){
        System.out.println("Querying the current system performance");
        // Read the values using the JMX Client
        try {
            long processingTime = JMXClient.getInstance().getParameter("processingTime", PERFORMANCE_OBJECT);
            long requestCount = JMXClient.getInstance().getParameter("requestCount", PERFORMANCE_OBJECT);
            return new long[]{processingTime, requestCount};

        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new long[]{-1, -1};
    }

    @RequestMapping(value = "/setparam", method = RequestMethod.PUT)
    public boolean setParam(@RequestParam(value = "name") String name, @RequestParam(value = "value") long value){
        System.out.println(String.format("Setting the value of parameter \"%s\" to %d", name, value));

        // find the relevant attribute and set the value
        // check whether the update succeed
        try {
            return JMXClient.getInstance().setParameter(name, value, THREAD_POOL_OBJECT);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        }

        return false;
    }

    @RequestMapping(value = "getparam", method = RequestMethod.GET)
    public long getParam(@RequestParam(value = "name") String name){
        System.out.println(String.format("Querying the values of parameter \"%s\"", name));
        // Query the JMX and get the param
        try {
            return JMXClient.getInstance().getParameter(name, THREAD_POOL_OBJECT);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
