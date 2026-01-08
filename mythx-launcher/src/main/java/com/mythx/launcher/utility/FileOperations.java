package com.mythx.launcher.utility;

import com.mythx.launcher.handler.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileOperations {
    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    public static int TotalRead = 0;
    public static int TotalWrite = 0;
    public static int CompleteWrite = 0;

    public FileOperations() {
    }

    public static final byte[] ReadFile(String s) {
        try {
            File file = new File(s);
            int i = (int) file.length();
            byte[] abyte0 = new byte[i];
            DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(s)));
            datainputstream.readFully(abyte0, 0, i);
            datainputstream.close();
            TotalRead++;
            return abyte0;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static final void WriteFile(String s, byte[] abyte0) {
        try {
            (new File((new File(s)).getParent())).mkdirs();
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            fileoutputstream.write(abyte0, 0, abyte0.length);
            fileoutputstream.close();
            TotalWrite++;
            CompleteWrite++;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println((new StringBuilder()).append("Write Error: ").append(s).toString());
        }
    }

    public static boolean FileExists(String file) {
        File f = new File(file);
        return f.exists();
    }

    public static byte[] readFile(String name) {
        try {
            RandomAccessFile raf = new RandomAccessFile(name, "r");
            ByteBuffer buf = raf.getChannel().map(
                    FileChannel.MapMode.READ_ONLY, 0, raf.length());
            try {
                if (buf.hasArray()) {
                    return buf.array();
                } else {
                    byte[] array = new byte[buf.remaining()];
                    buf.get(array);
                    return array;
                }
            } finally {
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> readTextFile(String location) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            BufferedReader file = new BufferedReader(new FileReader(location));
            String line;
            while ((line = file.readLine()) != null) {
                if (!line.isEmpty()) {
                    arraylist.add(line);
                }
            }
            file.close();
        }
        catch (Exception e) {
            //if (ClientDebugConfiguration.PRINT_ALL_EXCEPTION) {
                e.printStackTrace();
           // }
        }
        return arraylist;
    }

    public static void saveArrayContents(String location, ArrayList<?> arraylist) {
        if (arraylist.isEmpty()) {
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(location, true));

            for (int index = 0; index < arraylist.size(); index++) {
                bw.write("" + arraylist.get(index));
                bw.newLine();
            }

            bw.flush();
            bw.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        RGBImageFilter filter = new RGBImageFilter() {
            public int markerRGB = color.getRGB() | 0xFF000000;
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    return 0x00FFFFFF & rgb;
                } else {
                    return rgb;
                }
            }
        };
        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public static BufferedImage trimImage(BufferedImage img) {
        final byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        int width = img.getWidth();
        int height = img.getHeight();
        int x0, y0, x1, y1;                      // the new corners of the trimmed image
        int i, j;                                // i - horizontal iterator; j - vertical iterator
        leftLoop:
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                if (pixels[(j * width + i) * 4] != 0) { // alpha is the very first byte and then every fourth one
                    break leftLoop;
                }
            }
        }
        x0 = i;
        topLoop:
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                if (pixels[(j * width + i) * 4] != 0) {
                    break topLoop;
                }
            }
        }
        y0 = j;
        rightLoop:
        for (i = width - 1; i >= 0; i--) {
            for (j = 0; j < height; j++) {
                if (pixels[(j * width + i) * 4] != 0) {
                    break rightLoop;
                }
            }
        }
        x1 = i + 1;
        bottomLoop:
        for (j = height - 1; j >= 0; j--) {
            for (i = 0; i < width; i++) {
                if (pixels[(j * width + i) * 4] != 0) {
                    break bottomLoop;
                }
            }
        }
        y1 = j + 1;
        return img.getSubimage(x0, y0, x1 - x0, y1 - y0);
    }

    public static String readFileToString(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            LOGGER.warn("Couldn't read file with errors " + e.getMessage());
            return null;
        }
    }
}
