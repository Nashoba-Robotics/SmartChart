//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.nr.widgets;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.IntegerProperty;
import edu.wpi.first.smartdashboard.properties.MultiProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

public class NRCameraWidget extends StaticWidget implements Runnable {
    public static final String NAME = "NR USB Webcam Viewer";
    public final IntegerProperty fpsProperty = new IntegerProperty(this, "FPS", 15);
    public final MultiProperty sizeProperty = new MultiProperty(this, "Size");
    private static final int PORT = 1180;
    private static final byte[] MAGIC_NUMBERS = new byte[]{(byte)1, (byte)0, (byte)0, (byte)0};
    private static final int SIZE_640x480 = 0;
    private static final int SIZE_320x240 = 1;
    private static final int SIZE_160x120 = 2;
    private static final int HW_COMPRESSION = -1;
    private BufferedImage frame = null;
    private final Object frameMutex = new Object();
    private String errorMessage = null;
    private Socket socket;
    private Thread thread;
    static final int[] huffman_table_int = new int[]{255, 196, 1, 162, 0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 16, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125, 1, 2, 3, 0, 4, 17, 5, 18, 33, 49, 65, 6, 19, 81, 97, 7, 34, 113, 20, 50, 129, 145, 161, 8, 35, 66, 177, 193, 21, 82, 209, 240, 36, 51, 98, 114, 130, 9, 10, 22, 23, 24, 25, 26, 37, 38, 39, 40, 41, 42, 52, 53, 54, 55, 56, 57, 58, 67, 68, 69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100, 101, 102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120, 121, 122, 131, 132, 133, 134, 135, 136, 137, 138, 146, 147, 148, 149, 150, 151, 152, 153, 154, 162, 163, 164, 165, 166, 167, 168, 169, 170, 178, 179, 180, 181, 182, 183, 184, 185, 186, 194, 195, 196, 197, 198, 199, 200, 201, 202, 210, 211, 212, 213, 214, 215, 216, 217, 218, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 17, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 119, 0, 1, 2, 3, 17, 4, 5, 33, 49, 6, 18, 65, 81, 7, 97, 113, 19, 34, 50, 129, 8, 20, 66, 145, 161, 177, 193, 9, 35, 51, 82, 240, 21, 98, 114, 209, 10, 22, 36, 52, 225, 37, 241, 23, 24, 25, 26, 38, 39, 40, 41, 42, 53, 54, 55, 56, 57, 58, 67, 68, 69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100, 101, 102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120, 121, 122, 130, 131, 132, 133, 134, 135, 136, 137, 138, 146, 147, 148, 149, 150, 151, 152, 153, 154, 162, 163, 164, 165, 166, 167, 168, 169, 170, 178, 179, 180, 181, 182, 183, 184, 185, 186, 194, 195, 196, 197, 198, 199, 200, 201, 202, 210, 211, 212, 213, 214, 215, 216, 217, 218, 226, 227, 228, 229, 230, 231, 232, 233, 234, 242, 243, 244, 245, 246, 247, 248, 249, 250};
    static final byte[] huffman_table;

    public NRCameraWidget() {
        this.sizeProperty.add("640x480", Integer.valueOf(0));
        this.sizeProperty.add("320x240", Integer.valueOf(1));
        this.sizeProperty.setDefault("320x240");
    }

    public void init() {
        this.setPreferredSize(new Dimension(320, 240));
        this.thread = new Thread(this);
        this.thread.start();
        ImageIO.setUseCache(false);
    }

    public void disconnect() {
        this.thread.stop();
        if(this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    public void propertyChanged(Property property) {
        this.thread.interrupt();
    }

    public void run() {
        while(true) {
            while(true) {
                while(true) {
                    while(true) {
                        try {
                            this.socket = new Socket(Robot.getHost(), 1180);
                            DataInputStream e = new DataInputStream(this.socket.getInputStream());
                            DataOutputStream outputStream = new DataOutputStream(this.socket.getOutputStream());
                            int framesize = ((Integer)this.sizeProperty.getValue()).intValue();
                            outputStream.writeInt(this.fpsProperty.getValue().intValue());
                            outputStream.writeInt(-1);
                            outputStream.writeInt(framesize);
                            outputStream.flush();

                            while(!Thread.interrupted() && framesize == ((Integer)this.sizeProperty.getValue()).intValue()) {
                                byte[] magic = new byte[4];
                                e.readFully(magic);
                                int size = e.readInt();

                                assert Arrays.equals(magic, MAGIC_NUMBERS);

                                byte[] data = new byte[size + huffman_table.length];
                                e.readFully(data, 0, size);

                                assert size >= 4 && (data[0] & 255) == 255 && (data[1] & 255) == 216 && (data[size - 2] & 255) == 255 && (data[size - 1] & 255) == 217;

                                int pos = 2;

                                boolean has_dht;
                                int marker_size;
                                for(has_dht = false; !has_dht; pos += marker_size + 2) {
                                    assert pos + 4 <= size;

                                    assert (data[pos] & 255) == 255;

                                    if((data[pos + 1] & 255) == 196) {
                                        has_dht = true;
                                    } else if((data[pos + 1] & 255) == 218) {
                                        break;
                                    }

                                    marker_size = ((data[pos + 2] & 255) << 8) + (data[pos + 3] & 255);
                                }

                                if(!has_dht) {
                                    System.arraycopy(data, pos, data, pos + huffman_table.length, size - pos);
                                    System.arraycopy(huffman_table, 0, data, pos, huffman_table.length);
                                    size += huffman_table.length;
                                }

                                Object marker_size1 = this.frameMutex;
                                synchronized(this.frameMutex) {
                                    if(this.frame != null) {
                                        this.frame.flush();
                                    }

                                    this.frame = ImageIO.read(new ByteArrayInputStream(data));
                                    this.errorMessage = null;
                                    this.repaint();
                                }
                            }
                        } catch (ConnectException var31) {
                            if(this.errorMessage == null) {
                                this.errorMessage = var31.getMessage();
                            }
                        } catch (EOFException var32) {
                            if(this.errorMessage == null) {
                                this.errorMessage = "Robot stopped returning images";
                            }
                        } catch (IOException var33) {
                            if(this.errorMessage == null) {
                                this.errorMessage = var33.getMessage();
                            }
                        } finally {
                            if(this.socket != null) {
                                try {
                                    this.socket.close();
                                } catch (IOException var29) {
                                    ;
                                }
                            }

                            this.repaint();

                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException var28) {
                                ;
                            }

                        }
                    }
                }
            }
        }
    }

    protected void paintComponent(Graphics g) {
        int imageX = 0;
        int imageY = 0;
        int imageWidth = this.getWidth();
        int imageHeight = this.getHeight();
        Object var6 = this.frameMutex;
        synchronized(this.frameMutex) {
            if(this.frame != null) {
                float font = (float)this.getWidth() / (float)this.getHeight();
                float imageAspectRatio = (float)this.frame.getWidth((ImageObserver)null) / (float)this.frame.getHeight((ImageObserver)null);
                if(imageAspectRatio < font) {
                    imageWidth = (int)((float)this.getHeight() * imageAspectRatio);
                    imageX = (this.getWidth() - imageWidth) / 2;
                } else {
                    imageHeight = (int)((float)this.getWidth() / imageAspectRatio);
                    imageY = (this.getHeight() - imageHeight) / 2;
                }

                g.drawImage(this.frame, imageX, imageY, imageWidth, imageHeight, (Color)null, (ImageObserver)null);
            }

            if(this.errorMessage != null) {
                g.setClip(imageX, imageY, imageWidth, imageHeight);
                g.setColor(Color.pink);
                g.fillRect(imageX, imageY + imageHeight - 18, imageWidth, 18);
                g.setColor(Color.black);
                Font font1 = g.getFont();
                g.setFont(font1.deriveFont(1));
                g.drawString("Error: ", imageX + 2, imageY + imageHeight - 6);
                g.setFont(font1);
                g.drawString(this.errorMessage, imageX + 40, imageY + imageHeight - 6);
            }

        }
    }


    static {
        huffman_table = new byte[huffman_table_int.length];

        for(int i = 0; i < huffman_table.length; ++i) {
            huffman_table[i] = (byte)huffman_table_int[i];
        }

    }
}
