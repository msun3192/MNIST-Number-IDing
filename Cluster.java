// Matthew Sun and Sean Nayebi
// Algorithms
// May 30, 2024
import java.util.ArrayList;

public class Cluster {
    private ArrayList<Image> cluster;
    public Cluster(){
        cluster = new ArrayList<>();
    }
    public void add(Image i){
        cluster.add(i);
    }
    public boolean contains(Image i){
        return cluster.contains(i);
    }
    public Image[] toArray(){
        Image[] newCluster = new Image[cluster.size()];
        for (int i = 0; i < cluster.size(); i++) {
            newCluster[i] = cluster.get(i);
        }
        return newCluster;
    }
    public void remove(Image i){
        cluster.remove(i);

    }
    public int size(){
        return cluster.size();
    }
}
