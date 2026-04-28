import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;

public class CheckImage {
    public static void main(String[] args) {
        try {
            File file = new File("src/main/resources/assets/background.png");
            if (!file.exists()) {
                System.out.println("File not found");
                return;
            }
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            System.out.println("Dimensions: " + icon.getIconWidth() + "x" + icon.getIconHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
