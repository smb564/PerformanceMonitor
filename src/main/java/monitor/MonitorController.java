package monitor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.management.*;
import java.io.IOException;

@RestController
public class MonitorController {

    @RequestMapping(value = "/performance", method=RequestMethod.GET)
    public Metrics performance(){
        System.out.println("Querying the current system performance");
        // Read the values using the JMX Client
        return new Metrics(null, null);
    }

    @RequestMapping(value = "/setparam", method = RequestMethod.PUT)
    public boolean setParam(@RequestParam(value = "name") String name, @RequestParam(value = "value") long value){
        System.out.println(String.format("Setting the value of parameter \"%s\" to %d", name, value));

        // find the relevant attribute and set the value
        // check whether the update succeed
        try {
            return JMXClient.getInstance().setParameter(name, value);
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
            return JMXClient.getInstance().getParameter(name);
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
