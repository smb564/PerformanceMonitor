package monitor;

public class Metrics {

    private final String[] params;
    private final float[] values;

    public Metrics(String[] params, float[] values){
        this.params = params;
        this.values = values;
    }

    public float[] getValues() {
        return values;
    }

    public String[] getParams() {
        return params;
    }

}
