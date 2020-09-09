package io.klinamen.joclray;

import io.klinamen.joclray.geom.Box;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.light.SpotLight;
import io.klinamen.joclray.rendering.FullRenderer;
import io.klinamen.joclray.rendering.Renderer;
import io.klinamen.joclray.rendering.VisibilityRenderer;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static picocli.CommandLine.*;

@Command(name = "joclrayui", mixinStandardHelpOptions = true, version = "JOCLRay v1.0")
public class JoclRayUI implements Runnable {
    private BufferedImage image;

    private String imageSize;

    @Option(names = {"-p", "--platform"}, description = "Index of the OpenCL platform to use (default: ${DEFAULT-VALUE}).")
    private int platformIndex = 0;

    @Option(names = {"-d", "--device"}, description = "Index of the OpenCL device to use. (default: ${DEFAULT-VALUE})")
    private int deviceIndex = 0;

    @Option(names = {"-r", "--renderer"}, description = "The name of the renderer to use (default: ${DEFAULT-VALUE}) . Valid values: ${COMPLETION-CANDIDATES}")
    private RendererType rendererType = RendererType.Shading;

    @Spec
    Model.CommandSpec spec;

    @Option(names = {"-s", "--imageSize"}, defaultValue = "1920x1080", description = "Size of the output image in pixels <W>x<H> (default: ${DEFAULT-VALUE}).")
    public JoclRayUI setImageSize(String imageSize) {
        if (imageSize == null || !imageSize.matches("\\d+x\\d+")) {
            throw new ParameterException(spec.commandLine(), "Invalid image size specification.");
        }

        this.imageSize = imageSize;
        return this;
    }

    private int getImageWidth() {
        String[] res = imageSize.split("x", 2);
        return Integer.parseInt(res[0]);
    }

    private int getImageHeight() {
        String[] res = imageSize.split("x", 2);
        return Integer.parseInt(res[1]);
    }

    public static void main(String[] args) {
        new CommandLine(new JoclRayUI()).execute(args);
    }

    private void buildUI() {
        // Create the main frame
        JFrame frame = new JFrame("JOCLRay");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        image = new BufferedImage(
                getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_RGB);

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
            fileChooser.setDialogTitle("Save image");
            fileChooser.setSelectedFile(new File("render.bmp"));

            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                boolean doWrite = true;
                File fileToSave = fileChooser.getSelectedFile();
                if (fileToSave.exists()) {
                    int result = JOptionPane.showConfirmDialog(
                            frame,
                            "File exists, overwrite?", "File exists",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            break;
                        default:
                            doWrite = false;
                    }
                }

                if (doWrite) {
                    try {
                        ImageIO.write(image, "BMP", fileToSave);
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("Error saving image to %s: %s", fileToSave.getAbsolutePath(), e.getMessage()), e);
                    }
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
                .add("Bix_Mirror", new Box()
                        .setVertexMin(new FloatVec4(13.5f, -5, -21))
                        .setVertexMax(new FloatVec4(13.7f, 5, -30))
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

    private Renderer getRenderer() {
        switch (rendererType) {
            case Visibility:
                return new VisibilityRenderer(platformIndex, deviceIndex);
            case Shading:
                return new FullRenderer(platformIndex, deviceIndex);
        }

        throw new UnsupportedOperationException("Unsupported renderer type: " + rendererType);
    }

    private void render(Scene scene) {
        Renderer renderer = getRenderer();
        renderer.render(scene, image);

        if (renderer instanceof AutoCloseable) {
            try {
                ((AutoCloseable) renderer).close();
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error closing renderer: %s", e.getMessage()), e);
            }
        }
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(this::buildUI);
    }
}
