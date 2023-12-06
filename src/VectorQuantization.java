import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class ImageCompressionGUI extends JFrame {
    public ImageCompressionGUI() {
        super("Image Compression");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Button compressButton = new Button("Compress");
        //color of button
        compressButton.setBackground(Color.GREEN);
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCompressForm();
            }
        });

        Button decompressButton = new Button("Decompress");
        decompressButton.setBackground(Color.CYAN);
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDecompressForm();
            }
        });
        Button exitButton = new Button("Exit");
        exitButton.setBackground(Color.RED);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(ImageCompressionGUI.this, "Thank you for using our application!, Goodbye!");
                System.exit(0);
            }
        });


        addComponent(panel, compressButton, gbc, 0, 0);
        addComponent(panel, decompressButton, gbc, 0, 1);
        addComponent(panel, exitButton, gbc, 0, 2);
        add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addComponent(Container container, Component component, GridBagConstraints gbc, int gridx, int gridy) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        container.add(component, gbc);
    }

    private void openCompressForm() {
        dispose(); // Close the current form
        new CompressForm();
    }

    private void openDecompressForm() {
        dispose(); // Close the current form
        new DecompressForm();
    }







    public static String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return path.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new ImageCompressionGUI();
            }
        });
    }
}
class VectorQuantization {
    public int height, width, VectorHeight, VectorWidth, codeBookSize;
    public List<float[][][]> Blocks = new ArrayList<>();
    public float[][][] originalImage;
    public List<float[][][]> codeBook = new ArrayList<>();
    public Map<float[][][], String> codeBookMap = new HashMap<>();
    public String[][] encodedImage;
    public Map<float[][][], List<float[][][]>> nearestVectors = new HashMap<>();

    public float[][][] readImage(String filename) {
        BufferedImage img = null;
        File f = null;
        try {
            f = new File(filename);
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println(e);
        }
        height = img.getHeight();
        width = img.getWidth();
        float[][][] pixels = new float[height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = img.getRGB(j, i);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                pixels[i][j][0] = r;
                pixels[i][j][1] = g;
                pixels[i][j][2] = b;
            }
        }
        return pixels;
    }

    public List<float[][][]> divideImage() {
        List<float[][][]> vectors = new ArrayList<>();
        height = originalImage.length;
        width = originalImage[0].length;
        int h = height % VectorHeight;
        int w = width % VectorWidth;

        if ((h != 0) || (w != 0)) {
            height -= h;
            width -= w;
            float[][][] newImage = new float[height][width][3];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    newImage[i][j] = originalImage[i][j];
                }
            }
            originalImage = newImage;
        }

        for (int i = 0; i < height; i += VectorHeight) {
            for (int j = 0; j < width; j += VectorWidth) {
                float[][][] vector = new float[VectorHeight][VectorWidth][3];
                h = i;
                for (int x = 0; x < VectorHeight; x++, h++) {
                    w = j;
                    for (int y = 0; y < VectorWidth; y++, w++) {
                        vector[x][y] = originalImage[h][w];
                    }
                }
                vectors.add(vector);
            }
        }
        return vectors;
    }

    public float[][][] getAverageVector(List<float[][][]> vectors) {
        float[][][] averageVector = new float[VectorHeight][VectorWidth][3];
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < VectorHeight; j++) {
                for (int k = 0; k < VectorWidth; k++) {
                    for (int l = 0; l < 3; l++) {
                        averageVector[j][k][l] += vectors.get(i)[j][k][l];
                    }
                }
            }
        }
        for (int i = 0; i < averageVector.length; i++) {
            for (int j = 0; j < averageVector[i].length; j++) {
                for (int k = 0; k < 3; k++) {
                    averageVector[i][j][k] /= vectors.size();
                }
            }
        }
        return averageVector;
    }

    public List<float[][][]> splitVectors(List<float[][][]> codeBooks) {
        List<float[][][]> temp = new ArrayList<>();
        for (int i = 0; i < codeBooks.size(); i++) {
            float[][][] temp1 = new float[VectorHeight][VectorWidth][3];
            float[][][] temp2 = new float[VectorHeight][VectorWidth][3];
            for (int j = 0; j < VectorHeight; j++) {
                for (int k = 0; k < VectorWidth; k++) {
                    for (int l = 0; l < 3; l++) {
                        temp1[j][k][l] = (float) Math.floor(codeBooks.get(i)[j][k][l]);
                        temp2[j][k][l] = (float) Math.ceil(codeBooks.get(i)[j][k][l]);
                        if (temp1[j][k][l] == temp2[j][k][l]) {
                            temp1[j][k][l] -= 1;
                            temp2[j][k][l] += 1;
                        }
                    }
                }
            }
            temp.add(temp1);
            temp.add(temp2);
        }
        codeBooks = temp;
        return codeBooks;
    }

    public void getNearestVectors() {
        for (int i = 0; i < codeBook.size(); i++) {
            nearestVectors.put(codeBook.get(i), new ArrayList<>());
        }
        for (float[][][] imageVector : Blocks) {
            float[][][] temp = new float[VectorHeight][VectorWidth][3];
            List<float[][][]> vectors = new ArrayList<>();
            temp = imageVector;
            float min = Float.MAX_VALUE; // Initialize min to maximum possible value
            int inde = -1;
            for (int j = 0; j < codeBook.size(); j++) {
                float[][][] temp1 = codeBook.get(j);
                float sum = 0;
                for (int k = 0; k < VectorHeight; k++) {
                    for (int l = 0; l < VectorWidth; l++) {
                        for (int m = 0; m < 3; m++) {
                            sum += (float) Math.pow((temp[k][l][m] - temp1[k][l][m]), 2);
                        }
                    }
                }
                if (sum < min) {
                    min = sum;
                    inde = j;
                }
            }
            vectors.add(temp);
            if (nearestVectors.containsKey(codeBook.get(inde))) {
                vectors.addAll(nearestVectors.get(codeBook.get(inde)));
            }
            nearestVectors.put(codeBook.get(inde), vectors);
        }
    }

    public void quantize() {
        boolean flag = true;
        while (codeBook.size() < codeBookSize) {
            codeBook.clear();
            for (float[][][] v : nearestVectors.keySet()) {
                float[][][] temp = new float[VectorHeight][VectorWidth][3];
                temp = getAverageVector(nearestVectors.get(v));
                codeBook.add(temp);
            }
            codeBook = splitVectors(codeBook);
            nearestVectors.clear();
            getNearestVectors();
        }

        List<float[][][]> t = new ArrayList<>();
        while (flag) {
            flag = false;
            for (float[][][] v : nearestVectors.keySet()) {
                float[][][] temp = getAverageVector(nearestVectors.get(v));
                if (!containsSameElements(codeBook, temp)) {
                    t.add(temp);
                    flag = true;
                } else {
                    t.add(v);
                }
            }
            codeBook = new ArrayList<>(t);
            t.clear();
            nearestVectors.clear();
            getNearestVectors();
        }
    }

    private static boolean containsSameElements(List<float[][][]> list1, float[][][] array) {
        for (float[][][] arr : list1) {
            if (Arrays.deepEquals(arr, array)) {
                return true;
            }
        }
        return false;
    }

    public void encode() {
        int numOfBits = (int) (Math.log(codeBookSize) / Math.log(2));
        for (int i = 0; i < codeBook.size(); i++) {
            String binary = Integer.toBinaryString(i);
            while (binary.length() < numOfBits) {
                binary = "0" + binary;
            }
            codeBookMap.put(codeBook.get(i), binary);
        }
    }

    public void compress() {
        int h = height / VectorHeight;
        int w = width / VectorWidth;
        encodedImage = new String[h][w];
        for (int i = 0; i < Blocks.size(); i++) {
            float[][][] temp = Blocks.get(i);
            float[][][] codeBook = new float[VectorHeight][VectorWidth][3];

            for (float[][][] v : nearestVectors.keySet()) {
                if (nearestVectors.get(v).contains(temp)) {
                    codeBook = v;
                    break;
                }
            }

            String code = codeBookMap.get(codeBook);
            int y = i / w;
            int z = i % w;
            encodedImage[y][z] = code;
        }
    }

    public void SaveAsBinFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("compressed.bin"))) {
            oos.writeObject(encodedImage);
            oos.writeInt(height);
            oos.writeInt(width);
            oos.writeInt(VectorHeight);
            oos.writeInt(VectorWidth);
            oos.writeObject(codeBookMap);
            System.out.println("Binary file written successfully.");
        } catch (Exception e) {
            System.out.println("Error writing to binary file: " + e.getMessage());
        }
    }

    public void readFromBinFile(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            encodedImage = (String[][]) ois.readObject();
            height = ois.readInt();
            width = ois.readInt();
            VectorHeight = ois.readInt();
            VectorWidth = ois.readInt();
            codeBookMap = (Map<float[][][], String>) ois.readObject();

            System.out.println("Binary file read successfully.");
        } catch (Exception e) {
            System.out.println("Error reading from binary file: " + e.getMessage());
        }
    }

    public void SaveDecompressedImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < encodedImage.length; i++) {
            for (int j = 0; j < encodedImage[i].length; j++) {
                String code = encodedImage[i][j];
                float[][][] codeBook = new float[VectorHeight][VectorWidth][3];
                for (float[][][] v : codeBookMap.keySet()) {
                    if (codeBookMap.get(v).equals(code)) {
                        codeBook = v;
                        break;
                    }
                }
                for (int k = 0; k < VectorHeight; k++) {
                    for (int l = 0; l < VectorWidth; l++) {
                        int r = (int) codeBook[k][l][0];
                        int g = (int) codeBook[k][l][1];
                        int b = (int) codeBook[k][l][2];
                        int rgb = (r << 16) | (g << 8) | b;
                        img.setRGB(j * VectorWidth + l, i * VectorHeight + k, rgb);
                    }
                }
            }
        }
        File f = null;
        try {
            f = new File("ReConstructed.jpg");
            ImageIO.write(img, "jpg", f);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
class CompressForm extends JFrame {
    private final JTextField inputField;
    private final JTextField heightVectorField;
    private final JTextField widthVectorField;
    private final JTextField codeBookSizeField;

    public CompressForm() {
        super("Image Compression");
        Panel panel = new Panel();
        Panel panel1 = new Panel();
        Panel panel2 = new Panel(new GridLayout(3, 2));
        Button decompressButton = new Button("Compress");
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompressImage();
            }
        });
        Button back = new Button("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new ImageCompressionGUI();
            }
        });
        Button exit = new Button("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(CompressForm.this, "Thank you for using our application!, Goodbye!");
                System.exit(0);
            }
        });

        inputField = new JTextField(20);
        panel.add(new JLabel("Input File: "));
        inputField.setEditable(false);
        Button browseButton = new Button("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImageFile();
            }
        });
        heightVectorField = new JTextField(20);
        panel2.add(new JLabel("Height Vector: "));
        panel2.add(heightVectorField);
        widthVectorField = new JTextField(20);
        panel2.add(new JLabel("Width Vector: "));
        panel2.add(widthVectorField);
        codeBookSizeField = new JTextField(20);
        panel2.add(new JLabel("Code Book Size: "));
        panel2.add(codeBookSizeField);
        panel.add(inputField);
        panel.add(browseButton);
        browseButton.setBackground(Color.ORANGE);
        panel1.add(decompressButton);
        decompressButton.setBackground(Color.CYAN);
        exit.setBackground(Color.RED);
        panel1.add(back);
        back.setBackground(Color.GREEN);
        panel1.add(exit);
        panel.add(panel2);
        panel.add(panel1);
        add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(CompressForm.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            inputField.setText(selectedFile.getAbsolutePath());
        }
    }
    private boolean isImageFile(String path) {
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "bmp"};
        String extension = ImageCompressionGUI.getFileExtension(path);
        for (String ext : allowedExtensions) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    private void CompressImage() {
        String inputPath = inputField.getText();
        String heightVector = heightVectorField.getText();
        String widthVector = widthVectorField.getText();
        String codeBookSize = codeBookSizeField.getText();
        VectorQuantization v = new VectorQuantization();
        if (isImageFile(inputPath)) {
            if (heightVector.isEmpty() || widthVector.isEmpty() || codeBookSize.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all the fields.");
            } else {
                v.originalImage = v.readImage(inputPath);
                v.VectorHeight = Integer.parseInt(heightVector);
                v.VectorWidth = Integer.parseInt(widthVector);
                v.codeBookSize = Integer.parseInt(codeBookSize);
                v.Blocks = v.divideImage();
                v.codeBook.add(v.getAverageVector(v.Blocks));
                v.codeBook = v.splitVectors(v.codeBook);
                v.getNearestVectors();
                v.quantize();
                v.encode();
                v.compress();
                v.SaveAsBinFile();

                JOptionPane.showMessageDialog(this, "Image Compressed Successfully! ,Saved as compressed.bin");
                dispose();
                new ImageCompressionGUI();}
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input file. Please choose an image file.");
        }

    }



}

class DecompressForm extends JFrame {
    private final JTextField inputField;
    VectorQuantization v;
    public DecompressForm() {
        super("Image Decompression");
        v = new VectorQuantization();
        Panel panel = new Panel();
        Panel panel1 = new Panel();
        Button decompressButton = new Button("Decompress");
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decompressBinaryFile();
            }
        });
        Button back = new Button("Back");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new ImageCompressionGUI();
            }
        });
        inputField = new JTextField(20);
        panel.add(new JLabel("Input File: "));

        Button browseButton = new Button("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseBinaryFile();
            }
        });
        inputField.setEditable(false);
        Button exit = new Button("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DecompressForm.this, "Thank you for using our application!, Goodbye!");
                System.exit(0);
            }
        });
        panel.add(inputField);
        panel.add(browseButton);
        browseButton.setBackground(Color.ORANGE);
        panel1.add(decompressButton);
        decompressButton.setBackground(Color.CYAN);
        panel1.add(back);
        back.setBackground(Color.GREEN);
        exit.setBackground(Color.RED);
        panel1.add(exit);
        panel.add(panel1);
        add(panel);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private boolean isBinaryFile(String path) {
        String extension = ImageCompressionGUI.getFileExtension(path);
        return "bin".equalsIgnoreCase(extension);
    }

    private void chooseBinaryFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Binary Files", "bin"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(DecompressForm.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            inputField.setText(selectedFile.getAbsolutePath());
        }
    }
    private void decompressBinaryFile() {
        String inputPath = inputField.getText();
        if (isBinaryFile(inputPath)) {
            v.readFromBinFile(inputPath);
            v.SaveDecompressedImage();
            JOptionPane.showMessageDialog(this, "Image Decompressed Successfully!, Saved as ReConstructed.jpg");
            dispose();
            new ImageCompressionGUI();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input file. Please choose a binary file.");
        }
    }
}