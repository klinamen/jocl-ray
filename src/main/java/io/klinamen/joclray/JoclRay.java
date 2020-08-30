package io.klinamen.joclray;

import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class JoclRay {
    BufferedImage image;

    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//            ex.printStackTrace();
//        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JoclRay();
            }
        });
    }

    public JoclRay() {
        image = new BufferedImage(
                1280, 1024, BufferedImage.TYPE_INT_RGB);

        Scene scene = buildScene();

        // Create the panel showing the input and output images
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel outputLabel = new JLabel(new ImageIcon(image));
        mainPanel.add(outputLabel, gbc);

        JButton btRender = new JButton();
        btRender.setText("Render");
        btRender.addActionListener(actionEvent -> {
            render(scene);
            outputLabel.repaint();
        });
        mainPanel.add(btRender, gbc);

        // Create the main frame
        JFrame frame = new JFrame("JOCL Simple Image Sample");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private Scene buildScene() {
        return new Scene(new Camera()
                .setFrameWidth(image.getWidth())
                .setFrameHeight(image.getHeight())
                .setFovGrad(60)
//                .setPosition(new FloatVec4(-3, 6, 0))
//                .setDistance(1000)
        )
                .add(new PointLight()
                        .setIntensity(1f)
                        .setPosition(new FloatVec4(-10, 10, 20))
                )
//                .addLight(new PointLight()
//                        .setIntensity(0.5f)
//                        .setPosition(new FloatVec4(-10, -10, -50))
//                )
                .add(new Sphere()
                        .setCenter(new FloatVec4(0, 0, -30))
                        .setRadius(10)
                        .setKd(new FloatVec4(0.5f, 0, 0))
                        .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setPhongExp(500)
                )
                .add(new Sphere()
                        .setCenter(new FloatVec4(-5, 0, -20))
                        .setRadius(5)
                        .setKd(new FloatVec4(0, 0.5f, 0))
                        .setKs(new FloatVec4(0.8f, 0, 0))
                        .setPhongExp(20)
                )
                .add(new Plane()
                        .setNormal(new FloatVec4(0, -1, 0))
                        .setPosition(new FloatVec4(0, -5, 0))
                        .setKd(new FloatVec4(0.2f, 0.6f, 0.3f))
                        .setKs(new FloatVec4(0, 0.2f, 0.7f))
                        .setPhongExp(10)
                )
                ;
    }

    private void render(Scene scene) {
        try (Renderer renderer = new Renderer()) {
//            renderer.cast(scene, image);
            renderer.render(scene, image);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error closing renderer: %s", e.getMessage()), e);
        }
    }
}
