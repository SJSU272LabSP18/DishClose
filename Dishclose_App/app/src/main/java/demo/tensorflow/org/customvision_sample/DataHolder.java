package demo.tensorflow.org.customvision_sample;

public class DataHolder {
    private String data;
    private boolean showData =false;

    public boolean isShowData() {
        return showData;
    }

    public void setShowData(boolean showData) {
        this.showData = showData;
    }

    public String getData() {return data;}
    public void setData(String data) {this.data = data;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}