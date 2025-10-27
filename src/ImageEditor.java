import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;

class ImageEditor extends JPanel {

    private final Stack<BufferedImage> UNDO_STACK;
    private final Stack<BufferedImage> REDO_STACK;
    private final JPanel IMAGE_PANEL;
    private final JMenuBar MENU_BAR;
    private final JScrollPane SCROLL_PANE;
    private final ShortcutKeyMap SHORTCUT_KEY_MAP;
    private final ZoomMouseEventListener ZOOM_LISTENER;
    private int zoomImageIndex;

    ImageEditor() {
        this.UNDO_STACK = new Stack<>();
        this.REDO_STACK = new Stack<>();
        this.SHORTCUT_KEY_MAP = new ShortcutKeyMap(this);
        this.IMAGE_PANEL = new ImagePanel(this);
        this.SCROLL_PANE = new JScrollPane(this.IMAGE_PANEL, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.MENU_BAR = new MenuBar(this);
        this.ZOOM_LISTENER = new ZoomMouseEventListener(this, this.IMAGE_PANEL);
        this.zoomImageIndex = 0;
        this.setLayout(new BorderLayout());
        this.add(this.MENU_BAR, BorderLayout.NORTH);
        this.add(this.SCROLL_PANE, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.IMAGE_PANEL.repaint();
    }

    @Override
    public void revalidate() {
        super.revalidate();
        if (this.IMAGE_PANEL != null) {
            this.IMAGE_PANEL.revalidate();
        }
    }

    /**
     * Read a file and store it as a new Buffered Image
     *
     * @param in filename
     */
    void readPpmImage(String in) {
        try (Scanner scan = new Scanner(new File(in))){
            scan.nextLine();
            ArrayList<ArrayList<Integer>> rgbList = new ArrayList<>();
            // Read the width, height into the "width" and "height" variables.
            int width = scan.nextInt();
            int height = scan.nextInt();
            scan.nextLine();
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // Read the pixel data.
            while(scan.hasNextLine()){
                scan.nextLine();
                ArrayList<Integer> list = new ArrayList<>();
                for(int i = 0;i < width*3;i++){
                    try{
                        list.add(scan.nextInt());
                    }catch(NoSuchElementException e){
                        if(scan.hasNextLine()){
                            scan.nextLine();
                            list.add(scan.nextInt());
                        }
                    };
                }
                rgbList.add(list);
            }

            for(int i = 0;i < height;i++){
                for(int j = 0;j < width;j++){
                    ArrayList<Integer> line = rgbList.get(i);
                    Color c = new Color(line.get(j*3),line.get(j*3 +1),line.get(j*3 +2));
                    img.setRGB(j,i,c.getRGB());
                }
            }
            // Do not modify the lines below.
            this.UNDO_STACK.clear();
            this.REDO_STACK.clear();
            this.zoomImageIndex = 0;
            this.addImage(img);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * write the BufferedImage to a ppm file
     *
     * @param out file name to write too
     */
    void writePpmImage(String out) {
        try {
            BufferedImage img = this.getImage();
            // write the image to the PPM file.

            PrintWriter pw = new PrintWriter(new FileWriter(out),true);
            pw.println("P3");
            pw.println(img.getWidth() + " " + img.getHeight());
            pw.println(255);
            for(int i = 0;i < img.getHeight();i++){
                StringBuilder str = new StringBuilder();
                for(int j = 0;j < img.getWidth();j++){
                    str.append(ColorOperations.getColorAtPos(img, j, i).getRed()).append(" ")
                            .append(ColorOperations.getColorAtPos(img, j, i).getGreen()).append(" ")
                            .append(ColorOperations.getColorAtPos(img, j, i).getBlue());
                    if(j < img.getWidth()-1){
                        str.append(" ");
                    }
                }
                pw.println(str);
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new image to the editor and the undo stack. It is assumed that the image
     * being passed is not zoomed. If so, use the other addImage method.
     *
     * @param img image to add.
     */
    void addImage(BufferedImage img) {
        this.UNDO_STACK.push(img);
        this.REDO_STACK.clear();
        this.revalidate();
        this.repaint();
        this.zoomImageIndex++;
    }

    /**
     * Adds a new zoomed image to the editor. Because we only want to apply transformations
     * to non-zoomed images, we need to keep track of where the last non-zoomed image is in
     * the undo stack.
     *
     * @param img    image to add.
     * @param zoomed flag indicating whether the image is zoomed. This is always true.
     */
    void addImage(BufferedImage img, boolean zoomed) {
        this.UNDO_STACK.push(img);
        this.REDO_STACK.clear();
        this.revalidate();
        this.repaint();
        if (!zoomed) {
            this.zoomImageIndex++;
        }
    }

    /**
     * Removes the current image from the editor and the undo stack.
     * The undone image is pushed to the redo stack. If there are no images
     * to undo, this method does nothing.
     */
    void undoImage() {
        if (!this.UNDO_STACK.isEmpty()) {
            this.REDO_STACK.push(this.UNDO_STACK.pop());
            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Redoes the last undone image. The redone image is pushed to the undo stack.
     * If there are no images to redo, this method does nothing.
     */
    void redoImage() {
        if (!this.REDO_STACK.isEmpty()) {
            this.UNDO_STACK.push(this.REDO_STACK.pop());
            this.revalidate();
            this.repaint();
        }
    }

    Stack<BufferedImage> getUndoStack() {
        return this.UNDO_STACK;
    }

    Stack<BufferedImage> getRedoStack() {
        return this.REDO_STACK;
    }

    BufferedImage getImage() {
        return this.UNDO_STACK.isEmpty() ? null : this.UNDO_STACK.peek();
    }

    BufferedImage getOriginalImage() {
        if (this.zoomImageIndex < 1 || this.zoomImageIndex >= this.UNDO_STACK.size()) {
            return null;
        } else {
            return this.UNDO_STACK.elementAt(this.zoomImageIndex - 1);
        }
    }

    MenuBar getMenuBar() {
        return (MenuBar) MENU_BAR;
    }

    JScrollPane getScrollPane() {
        return this.SCROLL_PANE;
    }

    ZoomMouseEventListener getZoomListener() {
        return this.ZOOM_LISTENER;
    }
}
