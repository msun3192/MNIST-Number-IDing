// Matthew Sun and Sean Nayebi
// Algorithms
// May 30, 2024
import java.io.IOException;
import java.util.ArrayList;

public class KMeans {
    private Cluster[] clusters;
    private int[] ids;
    public KMeans(int k){
        clusters = new Cluster[k];
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster();
        }
        ids = new int[60000];
    }
    public int distance (Image a, Image b) {
        // Various distance metrics to consider:
        //   Hamming distance
        //   Manhattan distance
        //   Euclidean distance
        //   Cosine distance
        //   L_k norm (esp k=3, k=4)
        int euclideanDistance = 0;
        for(int i = 0; i < a.columns(); i++){
            for (int j = 0; j < a.rows(); j++) {
                int distance = a.get(j,i) - b.get(j,i);
                euclideanDistance += distance*distance;
            }
        }
        return euclideanDistance;
    }


    public int reclassify(Image[] items, Image[] centroids) {
        int updates = 0;
        // Assign each item to a cluster
        // Each cluster is represented by a canonical item
        // Use the centroid of the cluster as its canonical item
//        for (Image item : items) {
//            int previous = getCluster(item);
//            int current = nearest(item, centroids);
//            if (current != previous) updates++;
//            setCluster(current,item, previous, item.id());
//        }
        for (int i = 0; i < items.length; i++) {
            int previous = ids[items[i].id()];
            int current = nearest(items[i], centroids);
            if (current != previous) updates++;
            setCluster(current,items[i], previous);
        }
        return updates;
    }

    private void setCluster(int current, Image item, int previous){
        clusters[previous].remove(item);
        clusters[current].add(item);
        ids[item.id()] = current;
    }

    //
    public int nearest(Image item, Image[] centroids) {
        // Find the cluster (centroid) to which this item is nearest
        int min = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < centroids.length; i++) {
            int d = distance(item, centroids[i]);
            if (d < min) {
                min = d;
                index = i;
            }
        }
        return index;
    }

    public void kMeans(Image[] items, int k) {
        Image[] random = new Image[k];
        for (int i = 0; i < k; i++) {
            random[i] = items[(int)(items.length*Math.random())];
            System.out.println(random[i]);
        }
        Image[] centroids = createInitialCentroids(k, random);

        int updates;
        do {
            updates = reclassify(items, centroids);
            for (int i = 0; i < k; i++) {
                centroids[i] = centroid(clusters[i].toArray());
                centroids[i].changeLabel(i);
            }
            System.out.println("updates = " + updates);
        } while (updates > 0); // change later
        // Each item is now labeled with its cluster
        Viewer.Attributes attributes = new Viewer.Attributes();
        attributes.showLabels(false);
        attributes.showClassify();
        Viewer.invoke(centroids,"Centroids", attributes);
        double total = 0;
        for (int i = 0; i < centroids.length; i++) {
            try {
                double accuracy = computeAccuracy(clusters[i], centroids[i].label());
                total+= computeAccuracy(clusters[i], centroids[i].label());
                System.out.println(accuracy/clusters[i].size());
                Image.writeImages(centroids,"centroid-images");
                Image.writeLabels(centroids, "centroid-labels");

            }catch(IOException e){
                System.out.println("No Functiona");
            }
        }
        System.out.println("Total Accuracy: " +  total/items.length);

    }

    private Image[] createInitialCentroids(int k, Image[] randomImages) {
        Image[] centroids = new Image[k];
        System.arraycopy(randomImages, 0, centroids, 0, centroids.length);
        return centroids;
    }

    public static Image centroid(Image[] images) {
        int[][] newImage = new int[28][28];
        for (Image image : images) {
            for (int i = 0; i < image.columns(); i++) {
                for (int j = 0; j < image.rows(); j++) {
                    newImage[j][i] += image.get(j, i);
                }
            }
        }

        for (int i = 0; i < newImage.length; i++) {
            for (int j = 0; j < newImage[0].length; j++) {

                newImage[j][i] /= images.length;
            }
        }
        Image image = new Image(28,28);
        for (int i = 0; i < image.columns(); i++) {
            for (int j = 0; j < image.rows(); j++) {
                image.set(j,i,newImage[j][i]);
            }
        }
        return image;
    }
    public static double computeAccuracy (Cluster cluster, int label) throws IOException {
        Image[] clusterArray = cluster.toArray();
        ArrayList<Image> incorrectImages = new ArrayList<>();
        double totalCorrect = 0;
        for (Image image : clusterArray) {
            if (label == image.label()) {
                totalCorrect ++;
            } else {
                incorrectImages.add(image);
            }
        }
        Image[] listIncorrect = new Image[incorrectImages.size()];
        for (int i = 0; i < incorrectImages.size(); i++) {
            listIncorrect[i] = incorrectImages.get(i);
        }
        Image.writeImages(clusterArray, "Cluster" + label + "-images");
        Image.writeImages(listIncorrect, "Cluster" + label + "-incorrect");
        return totalCorrect;
    }
}
