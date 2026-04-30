import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class CheckImage {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("src/main/resources/assets/buttons/btn_start_normal.png"));
        System.out.println("Width: " + img.getWidth());
        System.out.println("Height: " + img.getHeight());
    }
}
