package io.klinamen.joclray;

import io.klinamen.joclray.display.RadianceDisplay;
import io.klinamen.joclray.rendering.Renderer;
import io.klinamen.joclray.rendering.impl.SeparatePathTracingRenderer;
import io.klinamen.joclray.rendering.impl.VisibilityRenderer;
import io.klinamen.joclray.samples.Scene6_S4;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.tonemapping.*;
import io.klinamen.joclray.util.FloatVec4;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static picocli.CommandLine.*;

@Command(name = "joclrayui", mixinStandardHelpOptions = true, version = "JOCLRay v1.0")
public class JoclRayUI implements Runnable {
    private BufferedImage image;
    private float[] radiance;

    private String imageSize;

    private JLabel outputLabel;
    private JComboBox<ToneMappingOperatorItem> toneMappersCombo;

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

        outputLabel = new JLabel(new ImageIcon(image));
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

                int pxIndex = x + y * image.getWidth();
                if (radiance != null && pxIndex * FloatVec4.DIM < radiance.length) {
                    float rRad = radiance[pxIndex * FloatVec4.DIM];
                    float gRad = radiance[pxIndex * FloatVec4.DIM + 1];
                    float bRad = radiance[pxIndex * FloatVec4.DIM + 2];
                    System.out.println(String.format("Spectral radiance (R,G,B): %f, %f, %f", rRad, gRad, bRad));
                }

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
            display();
        });
        mainPanel.add(btRender, gbc);

        // Save button
        JButton btSave = new JButton();
        btSave.setText("Save Image...");
        btSave.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save image");
            fileChooser.setSelectedFile(new File("render.png"));

            saveWithConfirm(fileChooser, frame, fileToSave -> {
                try {
                    ImageIO.write(image, "PNG", fileToSave);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Error saving image to %s: %s", fileToSave.getAbsolutePath(), e.getMessage()), e);
                }
            });
        });

        mainPanel.add(btSave, gbc);

        // Save Radiance button
        JButton btSaveRadiance = new JButton();
        btSaveRadiance.setText("Save Radiance...");
        btSaveRadiance.addActionListener(actionEvent -> {
            if (radiance == null) {
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save radiance buffer");
            fileChooser.setSelectedFile(new File("radiance.bin"));

            saveWithConfirm(fileChooser, frame, fileToSave -> {
                try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileToSave))) {
                    outputStream.writeObject(radiance);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Error saving radiance buffer to %s: %s", fileToSave.getAbsolutePath(), e.getMessage()), e);
                }
            });
        });

        mainPanel.add(btSaveRadiance, gbc);

        // Load Radiance button
        JButton btLoadRadiance = new JButton();
        btLoadRadiance.setText("Load Radiance...");
        btLoadRadiance.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load radiance buffer");

            int userSelection = fileChooser.showOpenDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(selectedFile))) {
                    radiance = (float[]) inputStream.readObject();
                    display();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(String.format("Error loading radiance buffer from %s: %s", selectedFile.getAbsolutePath(), e.getMessage()), e);
                }
            }
        });

        mainPanel.add(btLoadRadiance, gbc);

        toneMappersCombo = new JComboBox<>(buildToneMapperItems());
        toneMappersCombo.addActionListener(event -> {
            display();
        });

        mainPanel.add(toneMappersCombo, gbc);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private void saveWithConfirm(JFileChooser fileChooser, JFrame frame, Consumer<File> saveAction) {
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
                saveAction.accept(fileToSave);
            }
        }
    }

    private Scene buildScene() {
//        Scene scene = Scene1.build();
//        Scene scene = Scene2.build();
//        Scene scene = Scene3.build();
//        Scene scene = Scene4.build();
//        Scene scene = Scene5.build();
//        Scene scene = Scene6.build();
//        Scene scene = Scene8.build();

//        Scene scene = Scene6_S1.build();
//        Scene scene = Scene6_S2.build();
//        Scene scene = Scene6_S3.build();
        Scene scene = Scene6_S4.build();

        scene.getCamera()
                .setFrameWidth(image.getWidth())
                .setFrameHeight(image.getHeight());

        return scene;
    }

    private Renderer getRenderer() {
        switch (rendererType) {
            case Visibility:
                return new VisibilityRenderer(platformIndex, deviceIndex);
            case Shading:
//                return new DistributionRayTracerRenderer(platformIndex, deviceIndex, 2, 16);
//                return new PathTracingRenderer(platformIndex, deviceIndex, 4, 2);
            return new SeparatePathTracingRenderer(platformIndex, deviceIndex, 4, 8);
        }

        throw new UnsupportedOperationException("Unsupported renderer type: " + rendererType);
    }

    private void render(Scene scene) {
        Renderer renderer = getRenderer();

        long[] runtimes = new long[5];
        for(int i=0; i<runtimes.length; i++) {
            long startTime = System.nanoTime();
            radiance = renderer.render(scene);
            runtimes[i] = System.nanoTime() - startTime;
        }

        long min = Arrays.stream(runtimes).min().getAsLong();
        long max = Arrays.stream(runtimes).max().getAsLong();
        double average = Arrays.stream(runtimes).average().getAsDouble();

        System.out.println(Arrays.toString(runtimes));
        System.out.println(String.format("min: %d, max: %d, avg: %f", min, max, average));

        if (renderer instanceof AutoCloseable) {
            try {
                ((AutoCloseable) renderer).close();
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error closing renderer: %s", e.getMessage()), e);
            }
        }
    }

    private void display() {
        if(radiance == null){
            return;
        }

        ToneMappingOperatorItem toneMappingItem = (ToneMappingOperatorItem)toneMappersCombo.getSelectedItem();

        new RadianceDisplay(new CompositeToneMapping(toneMappingItem.get(), new ClampToneMapping()))
                .display(radiance, image);
        outputLabel.repaint();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(this::buildUI);
    }

    private ToneMappingOperatorItem[] buildToneMapperItems() {
        return new ToneMappingOperatorItem[]{
                new ToneMappingOperatorItem("Clamp", ClampToneMapping::new),
                new ToneMappingOperatorItem("Reinhard (RGB)", ReinhardToneMapping::new),
                new ToneMappingOperatorItem("Extended Reinhard (RGB)", () -> ExtendedReinhardToneMapping.from(radiance)),
                new ToneMappingOperatorItem("Extended Reinhard (Luminance)", () -> ReinhardLuminanceToneMapping.from(radiance)),
                new ToneMappingOperatorItem("Hable Filmic", HableFilmicToneOperator::new)
        };
    }

    public static class ToneMappingOperatorItem {
        private final String name;
        private final Supplier<ToneMappingOperator> supplier;

        public ToneMappingOperatorItem(String name, Supplier<ToneMappingOperator> supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public String getName() {
            return name;
        }

        public ToneMappingOperator get() {
            return supplier.get();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
