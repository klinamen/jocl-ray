package io.klinamen.joclray;

import io.klinamen.joclray.geom.Box;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.light.SpotLight;
import io.klinamen.joclray.rendering.FullRenderer;
import io.klinamen.joclray.rendering.Renderer;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class JoclRay {
    BufferedImage image;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                new JoclRay();
            }
        });
    }

    public JoclRay() {
        // Create the main frame
        JFrame frame = new JFrame("JOCLRay");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        image = new BufferedImage(
                1920, 1080, BufferedImage.TYPE_INT_RGB);

        Scene scene = buildScene();

        // Create the panel showing the input and output images
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel outputLabel = new JLabel(new ImageIcon(image));
        mainPanel.add(outputLabel, gbc);

        outputLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                PointerInfo a = MouseInfo.getPointerInfo();
                Point point = new Point(a.getLocation());
                SwingUtilities.convertPointFromScreen(point, mouseEvent.getComponent());
                int x = (int) point.getX();
                int y = (int) point.getY();
                System.out.println(String.format("(x, y) = (%d, %d), pixelIndex=%d", x, y, y * image.getWidth() + x));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        JButton btRender = new JButton();
        btRender.setText("Render");
        btRender.addActionListener(actionEvent -> {
            render(scene);
            outputLabel.repaint();
        });
        mainPanel.add(btRender, gbc);

        // Save button
        JButton btSave = new JButton();
        btSave.setText("Save Image...");
        btSave.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save image to");

            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                try {
                    ImageIO.write(image, "bmp", fileToSave);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Error saving image to %s: %s", fileToSave.getAbsolutePath(), e.getMessage()), e);
                }
            }
        });

        mainPanel.add(btSave, gbc);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private Scene buildScene() {
        return new Scene(new Camera()
                .setFrameWidth(image.getWidth())
                .setFrameHeight(image.getHeight())
                .setFovGrad(50)
        )
                .setAmbientLightIntensity(0.2f)
                .add(new PointLight()
                        .setIntensity(0.8f)
                        .setPosition(new FloatVec4(-12, 15, -10))
                )
                .add(new SpotLight()
                        .setIntensity(3.5f)
                        .setPosition(new FloatVec4(-3, 5, 0))
                        .setAngleGrad(40)
                        .setDirection(new FloatVec4(0.5f, -0.3f, -1))
                )
                .add(new PointLight()
                        .setIntensity(0.5f)
                        .setPosition(new FloatVec4(12, 8, 0))
                )
                .add("Red_Sphere", new Sphere()
                        .setCenter(new FloatVec4(2, 0, -40))
                        .setRadius(10)
                        .setKd(new FloatVec4(0.5f, 0, 0))
                        .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setKr(new FloatVec4(0.8f, 0.8f, 0.8f))
                        .setPhongExp(500)
                )
                .add("Green_Sphere", new Sphere()
                        .setCenter(new FloatVec4(-5, 0, -20))
                        .setRadius(5)
                        .setKd(new FloatVec4(0, 0.5f, 0))
                        .setKs(new FloatVec4(0.8f, 0.8f, 0.8f))
                        .setKr(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setPhongExp(100)
                )
                .add("Blue_Sphere", new Sphere()
                        .setCenter(new FloatVec4(6, -2, -25))
                        .setRadius(3)
                        .setKd(new FloatVec4(0, 0f, 0.5f))
                        .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setKr(new FloatVec4(0.4f, 0.4f, 0.2f))
                        .setPhongExp(800)
                )
                .add("Box", new Box()
                        .setVertexMin(new FloatVec4(12.5f, -5, -18))
                        .setVertexMax(new FloatVec4(12.7f, 3, -27))
                        .setKd(new FloatVec4(0, 0.3f, 0.3f))
                        .setKs(new FloatVec4(0.3f, 0.3f, 0.3f))
                        .setKr(new FloatVec4(0.4f, 0.4f, 0.7f))
                        .setPhongExp(1000)
                )
                .add("Ceiling", new Plane()
                        .setNormal(new FloatVec4(0, -1, 0))
                        .setPosition(new FloatVec4(0, 20, 0))
                        .setKd(new FloatVec4(0.752f, 0.901f, 0.925f))
                )
                .add("Floor", new Plane()
                        .setNormal(new FloatVec4(0, 1, 0))
                        .setPosition(new FloatVec4(0, -5, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setKs(new FloatVec4(0.3f, 0.3f, 0.3f))
                        .setKr(new FloatVec4(0.2f, 0.2f, 0.2f))
                        .setPhongExp(10)
                )
                .add("Left_Wall", new Plane()
                        .setNormal(new FloatVec4(1, 0, 0))
                        .setPosition(new FloatVec4(-15, 0, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setPhongExp(1000)
                )
                .add("Right_Wall", new Plane()
                        .setNormal(new FloatVec4(-1, 0, 0))
                        .setPosition(new FloatVec4(30, 0, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setPhongExp(1000)
                )
                ;
    }

    private Renderer getRenderer(){
//        return new VisibilityRenderer();
        return new FullRenderer();
    }

    private void render(Scene scene) {
        Renderer renderer = getRenderer();
        renderer.render(scene, image);

        if(renderer instanceof AutoCloseable){
            try {
                ((AutoCloseable) renderer).close();
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error closing renderer: %s", e.getMessage()), e);
            }
        }
    }
}
