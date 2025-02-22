// Matthew Sun and Sean Nayebi
// Algorithms
// May 30, 2024
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Image[] images;
        try {
            images = Image.read("train-images", "train-labels");
            KMeans kMeans = new KMeans(20);
            kMeans.kMeans(images,20);
        }catch(FileNotFoundException e){
            System.err.println("error"); // cluster 34
        }

    }
}