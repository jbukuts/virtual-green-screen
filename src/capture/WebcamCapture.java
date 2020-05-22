package capture;
import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;

public class WebcamCapture {
    //private JLabel webcamLabel;
    private JPanel panelMain;
    private JLabel imageHolder;
    private Webcam webcam;

    // width and height of the webcam
    private int width = 640;
    private int height = 480;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private WebcamCapture() {
        webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(width,height));
        webcam.open();

        // take the video
        Thread videoFeed = new Thread() {
            @Override
            public void run() {
                while(true) {

                    try {
                        // get the original image from the webcam
                        BufferedImage raw = webcam.getImage();

                        // get byte data of image
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write( raw, "jpg", baos );
                        baos.flush();
                        byte[] byteImage = baos.toByteArray();
                        baos.close();

                        // call node to get image mask data
                        HttpRequest request = HttpRequest.newBuilder()
                                .POST(HttpRequest.BodyPublishers.ofByteArray(byteImage))
                                .uri(URI.create("http://34.69.176.234:9000"))
                                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                                .header("Content-Type", "application/octet-stream")
                                .build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        byte[] maskArray = response.body().getBytes();
                        BufferedImage maskImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                        int[] rawPixels = raw.getRGB(0, 0, width, height, null, 0, width);

                        // array of threads to draw the final image
                        CreateMaskImage[] threadArray = new CreateMaskImage[10];
                        int runLength = rawPixels.length/threadArray.length;

                        // these threads will edit pixel data
                        for (int i=0;i<threadArray.length;i++) {
                            threadArray[i] = new CreateMaskImage(i * (runLength), runLength, rawPixels, maskArray);
                            threadArray[i].start();
                        }

                        // wait for threads to finish
                        for (CreateMaskImage createMaskImage : threadArray) {
                            createMaskImage.join();
                        }

                        // set pixels of final image and display
                        maskImage.setRGB(0, 0, raw.getWidth(), raw.getHeight(), rawPixels, 0, raw.getWidth());
                        imageHolder.setIcon(new ImageIcon(maskImage));

                        //sleep thread to limit frames
                        Thread.sleep(5);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        videoFeed.start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("WebcamCapture");
        frame.setContentPane(new WebcamCapture().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    private static class CreateMaskImage extends Thread {

        int start;
        int runLength;
        int[] imageArray;
        byte[] maskArray;

        CreateMaskImage(int s, int l, int[] arr, byte[] m) {
            imageArray = arr;
            start = s;
            runLength = l;
            maskArray = m;
        }

        public void run() {
            for (int i=start;i<start+runLength;i++) {

                int red = (imageArray[i] & 0x00FF0000) >> 16;
                int green = (imageArray[i] & 0x0000FF00) >> 8;
                int blue = (imageArray[i] & 0x000000FF);

                if (maskArray[i] == 0) {
                    red = (0);
                    green = (255) ;
                    blue = (0);
                }

                imageArray[i] = (red & 0xFF) << 16
                        | (green & 0xFF) << 8
                        | (blue & 0xFF);

            }

        }
    }
}
