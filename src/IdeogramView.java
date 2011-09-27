
/**
 * Ideogram bean.
 *
 */
import ideogram.GraphUtil;
import ideogram.JMultiLineToolTip;
import ideogram.Marker;
import ideogram.MarkerCollection;
import ideogram.db.Band;
import ideogram.db.IdeogramDB;
import ideogram.input.DataSlot;
import ideogram.tree.Interval;
import ideogram.tree.IntervalTree;
import ideogram.tree.IntervalTreeNode;
import ideogram.tree.IntervalTreeQuery;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Visual representation of a single ideogram.
 * To show the ideogram a database has to be loaded or attached
 * with IdeogramBean.LoadDatabase or IdeogramBean.setIdeogramDB
 * 
 * TODO: Split this component into different view sub-classes for
 *   1. the karyogram
 *   2. the gene markers
 *   3. the different marker lines
 *   4. the SNP markers
 * These different components could be synchronized (i.e. start/stop basepair) 
 * and could have their own routines for e.g. showing tooltips.   
 * 
 * 
 * @author muellera
 *
 */
public class IdeogramView extends JPanel implements Serializable, ActionListener, Printable {

    private static final long serialVersionUID = 2L;
    /**
     * Maximum view, basepairs (=zoomed out).
     */
    public static final long MAX_BASEPAIRS = 250000000;
    /**
     * Minimum view, basepairs (=zoomed in)
     */
    public static final long MIN_LENGTH = 100000;
    /**
     * Maximum number of characters of a chromsomal label.
     */
    protected static final int MAX_LABEL_LENGTH = 8;
    protected static final int MIN_MARKER_WIDTH = 4;
    protected IdeogramDB db;
    protected byte chromosome;
    /**
     * Array of {@link MarkerCollection}'s
     */
    protected List<DataSlot> dataSlots = null;
    protected ArrayList<MarkerCollection> markers;
    // visual options
    protected boolean showLabels;
    protected boolean showMarkers;
    private boolean consensusMode;
    protected boolean showProfileLines;
    protected boolean condensedMode;
    protected int minWidth;
    protected int maxWidth;
    protected int markerWidth;
    // marker selected
    private Marker selectedMarker;
    private BitSet leftVisible;
    private BitSet rightVisible;
    private int selectedSampleIndex;
    private boolean marker_state;       // for toggleMarker
    // non streamable variables
    transient private Interval view; // shown basepair range
    transient private Rectangle ideogramBounds;   // ideogram region
    transient private Rectangle labelBounds;    // labels '1q22' region
    transient private Rectangle leftMarkerBounds;
    transient private Rectangle rightMarkerBounds;
    transient private Rectangle leftInfoBounds;
    transient private Rectangle rightInfoBounds;     // info region
    transient private long chromosomeLength;
    transient private boolean active;
    transient private int deltaX;
    transient private BufferedImage paintBuffer;
    transient private boolean paintBufferValid;
    private IdeogramMainWindow mainWindow;
    private Timer timer;
    private int lineWidth;
    private Marker selected_gene;
    private LinkedList<ChangeListener> selectionChangedListeners;
    private Color colSample;
    private Color colSampleSelected;
    private Dimension dim;
    private double scaleX;
    private double scaleY;
    // drag mode
    private Interval rangeSelection;
    private Point beginPoint;
    private Point endPoint;
    private boolean isDragging;
    private Color selectionBackground;
    private LinkedList<Interval> viewHistory;
    private MarkerCollection M;
    
    // for drawing segments
    private boolean drawSegments;
    private MarkerCollection segments;
    

    public IdeogramView() {
        // dragmode
        selectionBackground = new Color(0xdd, 0xdd, 0xff);
        viewHistory = new LinkedList<Interval>();

        scaleX = 1.0;
        scaleY = 1.0;
        chromosome = 1;
        showMarkers = true;

        showProfileLines = true;
        condensedMode = false;
        consensusMode = false;
        minWidth = 5;
        maxWidth = 50;
        markerWidth = 2;
        markers = new ArrayList<MarkerCollection>();

        selectedSampleIndex = -1;
        selectedMarker = null;

        leftVisible = new BitSet();
        rightVisible = new BitSet();

        dim = null;

        setBackground(Color.WHITE);
        initTransientState();
    }

    public IdeogramView(IdeogramDB db) {
        this();
        this.db = db;
    }

    public void setSegments (MarkerCollection mc) {
        segments = mc;
        drawSegments = true;
        
    }
    
    private boolean drawSegments() {
        return drawSegments;
    }
    
    /**
     * 
     * @return The currently attached ideogram database 
     * (or null if no database is attached).
     */
    public IdeogramDB getIdeogramDB() {
        return db;
    }

    /**
     * Attaches an ideogram database to the IdeogramBean.
     * @param db
     */
    public void setIdeogramDB(IdeogramDB db) {
        if (this.db != db) {
            this.db = db;
            repaint();
        }
    }

    /**
     * 
     * @return True if a database is attached.
     */
    public boolean isIdeogramDBLoaded() {
        return db != null;
    }

    /**
     * 
     * @return The shown chromosome (1..24)
     */
    public int getChromosome() {
        return chromosome;
    }

    /**
     * Sets the currently shown chromosome. <var>chromosome</var> has to be
     * in the range 1..24 (where 23=X,24=Y).
     * 
     * @param chromosome
     * @throws java.lang.IllegalArgumentException
     */
    public void setChromosome(int chromosome) {
        if (chromosome < 1 || chromosome > 24) {
            throw new IllegalArgumentException(
                    "chromosome has to be in the range 1..24 '" + chromosome + "'");
        }
        this.chromosome = (byte) chromosome;
        chromosomeLength = 0;
        repaint();
    }

    /**
     * @return True if the chromosomal labels are shown.
     */
    public boolean getShowLabels() {
        return showLabels;
    }

    /**
     * Activates/Deactivates the chromosomal location label view (e.g. "1p22")
     * 
     * @param showLabels
     */
    public void setShowLabels(boolean showLabels) {
        if (this.showLabels != showLabels) {
            this.showLabels = showLabels;
            invalidatePaintBuffer();
        }
    }

    public void setConsensusMode(boolean mode) {
        if (this.consensusMode != mode) {
            this.consensusMode = mode;
            invalidatePaintBuffer();
        }
    }

    /**
     * 
     * @return True if the markers are shown.
     * @see #setShowMarkers
     * @see #getMarkers
     * 
     */
    public boolean getShowMarkers() {
        return showMarkers;
    }

    /**
     * Show markers. Set with {@link #addMarkers}.
     * 
     * @param showMarkers
     */
    public void setShowMarkers(boolean showMarkers) {
        if (showMarkers != this.showMarkers) {
            this.showMarkers = showMarkers;
            invalidatePaintBuffer();
        }
    }

    /**
     * 
     * @return MarkerCollection with markers.
     */
    public MarkerCollection getMarkers(int i) {
        return markers.get(i);
    }

    public int getNumMarkers() {
        return markers.size();
    }

    public MarkerCollection[] getMarkers() {
        MarkerCollection[] mc = new MarkerCollection[markers.size()];
        for (int i = 0; i < markers.size(); ++i) {
            mc[i] = markers.get(i);
        }
        return mc;
    }

    /**
     * Adds a marker set {@link MarkerCollection}.
     * 
     * @param M Collection of markers.
     */
    public synchronized void addMarkers(MarkerCollection M) {
        markers.add(M);
        updateSize();
    }

    /**
     * Removes all markers from this ideogram.
     * synchronized ???
     */
    public synchronized void clearMarkers() {
        selectedMarker = null;
        markers.clear();
        updateSize();
    }

    /**
     * Zoom fully out.
     *
     */
    public void resetView() {
        viewHistory.clear();
        setView(new Interval(0, MAX_BASEPAIRS));
    }

    /**
     * 
     * @return The current field of view (in basepairs).
     */
    public Interval getView() {
        return view;
    }

    public long getChromosomeLength() {
        if (chromosomeLength == 0) {
            chromosomeLength = getTree().getRange().to;
        }

        return chromosomeLength;
    }

    /**
     * Call this function if the view changes.
     *
     */
    private void invalidatePaintBuffer() {
        paintBufferValid = false;
        repaint();
    }

    /**
     * Sets the current view for this ideogram (in basepairs).
     * @param view
     */
    public void setView(Interval view) {
        long length =
                Math.max(MIN_LENGTH, Math.min(view.getLength(), MAX_BASEPAIRS));

        if (length == MAX_BASEPAIRS || view.from < 0) {
            view.from = 0;
        } else {
            if (getChromosomeLength() > 0) {
                if (view.from > getChromosomeLength() - length / 2) {
                    // scrolled out of visible area
                    view.from = Math.max(0, getChromosomeLength() - length / 2);
                }
            } else {
                view.from = 0;
                view.to = MAX_BASEPAIRS;
            }
        }

        view.to = view.from + length;

        if (!view.equals(this.view)) {
            this.view = view;
            invalidatePaintBuffer();
        }
        fireSelectionChanged();   // TODO: is this the right place for this??
    }

    /**
     * 
     * @return Current zoom factor 1.0 = 1:1, 20.0 = 20:1
     */
    public double getZoom() {
        return (double) MAX_BASEPAIRS / (double) view.getLength();
    }

    /**
     * 
     * @return True if details should be shown (zoom dependent)
     */
    public boolean getShowDetails() {
        return getZoom() >= 10.0;
    }

    /**
     * 
     * Sets the width-range of the ideogram graphic. So if the size of the
     * panel changes the graphic will not exceed the given boundary. 
     *
     * @param minWidth Minimum width of the ideogram graphic.
     * @param maxWidth Maximum width of the ideogram graphic.
     */
    public void setWidth(int minWidth, int maxWidth) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        repaint();
    }

    /**
     * Transforms the basepair location <var>pos</var> to y-coordinates of the canvas.
     * @param pos
     * @return y coordinate.
     */
    public int BaseToYCoord(long pos) {
        return ideogramBounds.y
                + (int) ((float) (pos - view.from)
                * (float) ideogramBounds.height
                / (float) view.getLength());
    }

    /**
     * Transforms a canvas y coordinate to basepairs.
     * @param y
     * @return Basepair offset.
     */
    public long YCoordToBase(int y) {
        y -= ideogramBounds.y;
        if (y < 0) {
            y = 0;
        }
        if (y >= ideogramBounds.height) {
            y = ideogramBounds.height - 1;
        }
        if ((y >= 0) && (y < ideogramBounds.height)) {
            return view.from
                    + (long) ((float) y
                    * (float) view.getLength()
                    / (float) ideogramBounds.height);
        } else {
            return 0;
        }
    }

    /**
     * Compute the x-coordinate of a marker position.
     * If count is > 0 the markers are placed on the right side of the ideogram.
     * If count is < 0 the markers will be on the left side.
     * 
     * @param count Maker value.
     * @return x-coordinate
     * 
     */
    public int markerToXCoord(int count) {
        if (count > 0) { // right side
            return rightMarkerBounds.x + deltaX * (count - 1) + deltaX / 2;
        } else {
            if (count < 0) { // left side
                return leftMarkerBounds.x + leftMarkerBounds.width + deltaX * (count + 1) - deltaX / 2;
            } else { // middle (unused)
                return ideogramBounds.x + ideogramBounds.width / 2;
            }
        }
    }

    /**
     * Converts a x-coordinate in this panel into a marker value.
     * 
     * @param x
     * @return Integer marker value between -n ... n. Returns 0
     *   if the pointer is outside the marker regions
     */
    public int xCoordToMarker(int x) {
        if (leftMarkerBounds.x <= x && x <= leftMarkerBounds.x + leftMarkerBounds.width) {
            return -Math.round((float) (leftMarkerBounds.x + leftMarkerBounds.width - x) / (float) deltaX + 0.5f);
        }

        if (rightMarkerBounds.x <= x && x <= rightMarkerBounds.x + rightMarkerBounds.width) {
            return Math.round((float) (x - rightMarkerBounds.x) / (float) deltaX + 0.5f);
        }

        return 0;
    }

    public void paintIdeogramBuffered(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;

        if (paintBuffer != null) {
            if ((paintBuffer.getWidth() != getWidth()) || (paintBuffer.getHeight() != getHeight())) {
                paintBufferValid = false;
                paintBuffer = null; // paint buffer must be resized
            }
        }

        if (!paintBufferValid) {
            Graphics2D g = null;
            // create a new paint buffer
            if (paintBuffer == null) {
                paintBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                g = paintBuffer.createGraphics();
            } else {
                g = paintBuffer.createGraphics();
                g.setBackground(new Color(0xff, 0x00, 0x00, 0x00));
                g.clearRect(0, 0, paintBuffer.getWidth(), paintBuffer.getHeight());
            }

            directPaint(g);
            g.dispose();

            paintBufferValid = true;
        }

        //AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        //g2.setComposite(ac);
        g2.drawImage(paintBuffer, 0, 0, this);
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setPaintMode();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        paintRangeSelection(graphics);

        paintBackgroundSelections(graphics);
        paintIdeogramBuffered(graphics);
        paintForegroundSelections(graphics);
        
        if (drawSegments) {
            drawSegments(graphics);
        }
         
    }

    /**
     * Paints the ideogram into the given graphical context.
     * 
     * @param g
     * @param width
     * @param height
     */
    public void directPaint(Graphics g) {
        g.setPaintMode();

        byte minLevel, maxLevel;

        if (view.getLength() < 25000000) {
            minLevel = 2;
            maxLevel = 2;
        } else {
            minLevel = 1;
            maxLevel = 1;
        }

        //g.setColor(getBackground());
        //g.fillRect(0, 0, width-1, height-1);

        if (db == null) {	// no database: draw a cross
            g.setColor(Color.RED);
            g.drawLine(0, 0, dim.width - 1, dim.height - 1);
            g.drawLine(dim.width - 1, dim.height - 1, 0, 0);
            return;
        }
        // TODO : setfont

        // g.setFont(labelFont);
        float fontSize = Math.min(20.0f, Math.max(6.0f, 8.0f * (float) dim.width / 100));
        Font font = g.getFont().deriveFont(fontSize);
        g.setFont(font);


        // FontMetrics metrics = g.getFontMetrics();

        // allocate colors
        Color[] col = new Color[6];

        col[Band.DENSITY_UNKNOWN] = new Color(255, 0, 0);
        col[Band.DENSITY_GNEG] = Color.WHITE;
        col[Band.DENSITY_GPOS] = new Color(10, 10, 10);
        col[Band.DENSITY_GVAR] = new Color(150, 150, 200); // new Color(100, 255, 100);
        col[Band.DENSITY_ACEN] = new Color(32, 32, 255);
        col[Band.DENSITY_STALK] = Color.WHITE;

        // find bands
        IntervalTreeQuery query = new IntervalTreeQuery(getTree());
        ArrayList list = query.Query(view, minLevel, maxLevel);

        int xofs = ideogramBounds.x,
                r = ideogramBounds.width / 5,
                // radius of the edges
                y_start = -1,
                // contains the last upper end (or -1 if there was no end)
                count = 0; // number of shown bands

        byte last_arm = 0,
                next_arm = 0,
                last_type = Band.DENSITY_UNKNOWN,
                next_type = Band.DENSITY_UNKNOWN;

        boolean top_closed = true,
                bottom_closed = true;

        for (int i = 0; i < list.size(); ++i) {
            IntervalTreeNode node = (IntervalTreeNode) list.get(i);
            Interval I = node.getInterval();
            Band band = (Band) node.content;

            ++count;
            next_arm = 0;
            next_type = Band.DENSITY_UNKNOWN;
            for (int i2 = i + 1; i2 < list.size(); ++i2) {
                IntervalTreeNode node2 = (IntervalTreeNode) list.get(i2);
                Interval I2 = node2.getInterval();
                Band band2 = (Band) node2.content;

                if (I2.intersects(view)) //  (band2.subsubband == 0) && 
                {
                    next_arm = band2.arm;
                    next_type = band2.density;
                    break;
                }
            }

            long start = I.from,
                    end = I.to;

            if (start < view.from) {
                start = view.from;
                top_closed = false;
            }
            if (end > view.to) {
                end = view.to;
                bottom_closed = false;
            }

            int y1 = BaseToYCoord(start),
                    y2 = BaseToYCoord(end);

            boolean upper_end = false,
                    lower_end = false;

            if (last_arm != band.arm) {
                upper_end = true;
            }

            if ((last_type == Band.DENSITY_STALK)
                    && (band.density != Band.DENSITY_STALK)) {
                upper_end = true;
            }

            if (band.arm != next_arm) {
                lower_end = true;
            }

            if ((band.density != Band.DENSITY_STALK)
                    && (next_type == Band.DENSITY_STALK)) {
                lower_end = true;
            }

            if ((band.density == Band.DENSITY_STALK)
                    && next_type != Band.DENSITY_STALK) {
                lower_end = true;
            }

            if (band.density != Band.DENSITY_STALK) {
                // adapt edge radius if y2-y1 is too small
                int rr = r;
                int cnt = 0;
                if (upper_end) {
                    ++cnt;
                }
                if (lower_end) {
                    ++cnt;
                }

                if (cnt * r * 2 > (y2 - y1 + 1)) {
                    rr = (y2 - y1 + 1) / (2 * cnt);
                }
                int rt = upper_end ? rr : 0,
                        rb = lower_end ? rr : 0;

                g.setColor(col[band.density]);
                GraphUtil.roundFilledRectangle(
                        g,
                        xofs,
                        y1,
                        xofs + ideogramBounds.width,
                        y2,
                        (top_closed ? rt : 0),
                        (top_closed ? rt : 0),
                        (bottom_closed ? rb : 0),
                        (bottom_closed ? rb : 0));

                if (y_start < 0) {
                    y_start = y1;
                }
                if (lower_end && y_start >= 0) {
                    // draw surrounding box if a lower end is given
                    g.setColor(Color.black);
                    GraphUtil.roundRectangle(
                            g,
                            xofs,
                            y_start,
                            xofs + ideogramBounds.width,
                            y2,
                            rr,
                            rr,
                            rr,
                            rr,
                            top_closed,
                            true,
                            bottom_closed,
                            true);
                    /*
                    if( ! top_closed )
                    drawTerminator( image_, xofs, y_start - 5, xofs + getChrWidth(), y_start + 5, col_white, col_black );
                    
                    if( ! bottom_closed )
                    drawTerminator( image_, xofs, y2-5, xofs + getChrWidth(), y2+5, col_white, col_black );
                     */

                    y_start = -1;
                    top_closed = true;
                    bottom_closed = true;
                }
            } else {
                // Band.DENSITY_STALK -> no bounding box
                y_start = -1;
                top_closed = true;
                bottom_closed = true;
            }

            last_arm = band.arm;
            last_type = band.density;

            if (showLabels) {
                String label = band.toString();
                int yy = (y1 + y2) / 2;
                // xbegin = labelBounds.x;
                //xend = xbegin + metrics.stringWidth(label),
                //dy = metrics.getHeight() / 2;

                g.setColor(Color.black);
                g.drawString(label, labelBounds.x, yy);
                g.drawLine(
                        labelBounds.x,
                        yy,
                        ideogramBounds.x + ideogramBounds.width,
                        yy);
            }
        }
        if (showMarkers) {
            drawAberrations(g);
        }

        // draw chromosome name
        String name;
        switch (chromosome) {
            case 23:
                name = "Chr. X";
                break;
            case 24:
                name = "Chr. Y";
                break;
            default:
                name = "Chr. " + chromosome;
        }
        Font cfont = g.getFont().deriveFont(9.0f);
        g.setFont(cfont);
        FontMetrics cmetrics = g.getFontMetrics();
        g.setColor(Color.BLUE);
        g.drawString(name, (dim.width - cmetrics.stringWidth(name)) / 2, cmetrics.getHeight());
        // dim.height-cmetrics.getHeight()/2);	

        // show boundaries
        g.setColor(new Color(0, 128, 128));
        long L = view.getLength(), S;
        String sym = "";
        if (L < 100000) {
            S = 1;
        } else {
            if (L < 10000000) {
                S = 1000;
                sym = "K";
            } else {
                S = 1000000;
                sym = "M";
            }
        }
        NumberFormat nf = DecimalFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /**
     * 
     * @return The current ideogram tree for the chromosome of this ideogram.
     */
    private IntervalTree getTree() {
        if (db == null) {
            throw new IllegalStateException("Ideogram database is not initialized");
        }

        if (chromosome < 1 || chromosome > db.getTree().length) {
            throw new IllegalStateException("Chromosome " + chromosome + " is invalid!");
        }

        return db.getTree()[chromosome - 1];
    }

    public boolean isSampleSelected() {
        return (0 <= selectedSampleIndex) && (selectedSampleIndex < markers.size());
    }

    public int getSelectedSampleIndex() { 
        return selectedSampleIndex;
    }

    // print all the segments on the chromosome
    public void drawSegments(Graphics g) {
        for (Marker seg : segments.getMarkers()) {
            drawInterval(g, seg.interval, seg.value);
        }
    }

    private void drawInterval(Graphics g, Interval interval, int matches) {
        int y1 = BaseToYCoord(interval.from), y2 = BaseToYCoord(interval.to);
                
        // create colours for all the possible sharing states 
        // number of states == number of selected inds
        // create a colour gradient
        // getSegmentColor(matches)
        float h = new Float(0.333);
        float s = new Float(1);
        float b = new Float(1/matches);
        
        System.out.println(h +" "+ s +" "+ b );
        
        g.setColor(Color.getHSBColor(h, s, b));
        g.fillRect(rightMarkerBounds.x +5, y1, 5 * matches, y2 - y1);	// draw single marker		
    }

    private void drawInterval(Graphics g, int column, Interval interval, int lineWidth, boolean show_band, Color band_color) {
        int y1 = BaseToYCoord(interval.from),
                y2 = BaseToYCoord(interval.to),
                x = markerToXCoord(column),
                x1 = markerToXCoord(-getNumLeft()),
                x2 = markerToXCoord(getNumRight());

        y2--;

        boolean cutTop = false,
                cutBot = false;

        if (y1 < ideogramBounds.y) {
            cutTop = true;
            y1 = ideogramBounds.y;
        }

        if (y2 > ideogramBounds.y + ideogramBounds.height) {
            cutBot = true;
            y2 = ideogramBounds.y + ideogramBounds.height;
        }

        if (y2 - y1 < 1) {
            y2 = y1 + 1;
        }

        g.fillRect(x - lineWidth / 2, y1, lineWidth, y2 - y1);	// draw single marker		

        int dx = Math.max((int) (deltaX / 3), 1);

        // draw cross line if the ends are cut
        if (cutTop) {
            g.drawLine(x - dx, y1 - dx, x + dx, y1 + dx);
        }

        if (cutBot) {
            g.drawLine(x - dx, y2 - dx, x + dx, y2 + dx);
        }

        if (getShowDetails()) {
            // draw line ends with higher details
            if (!cutTop) //g.fillRect(x-dx1,y1,dx1+dx2,dy);
            {
                g.drawLine(x - dx, y1, x + dx, y1);
            }

            if (!cutBot) {
                g.drawLine(x - dx, y2, x + dx, y2);
            }
        }


        if (show_band) {
            if (band_color != null) {
                g.setColor(band_color);
            }
            if (!cutTop) {
                g.drawLine(x1, y1, x2, y1);
            }
            if (!cutBot) {
                g.drawLine(x1, y2, x2, y2);
            }
        }
    }

    /**
     * Draws a single marker.
     * 
     * @param g
     * @param marker
     * @param column The column index (0..n-1)
     * @param width The (line) width of the marker
     * @param color_left Color of a loss
     * @param color_right Color of a gain
     * @param show_band Shows a band across the ideogram
     * @param band_color Color of the band
     */
    private void drawMarker(Graphics g, Marker marker, int column, int width, Color color_left, Color color_right, boolean show_band, Color band_color) {
        column = Math.abs(column) - 1;
        if (marker.value == 2) {
            return; // do not draw unchanged clones
        }
        if (!view.intersects(marker.interval)) {
            return;
        }

        if (width <= 0) {
            width = 1;
        }

        boolean left_side = marker.value < 2 || marker.value < 0,
                right_side = marker.value > 2 || marker.value < 0;


        if (marker.value < 0) {	// copy number undefined ()
            color_left = Color.MAGENTA;
            color_right = Color.MAGENTA;
        } else {
            if (marker.color != null) {
                color_left = marker.color;
                color_right = marker.color;
            }
            /*if( color_left == null ) 	{
            color_left = new Color(0,90,0);
            
            }
            if( color_right == null )  {
            color_right = new Color(140,0,0);
            }*/
        }

        if (left_side) {
            g.setColor(color_left);
            drawInterval(g, -(1 + column), marker.interval, width, show_band, band_color);
        }

        if (right_side) {
            g.setColor(color_right);
            drawInterval(g, (1 + column), marker.interval, width, show_band, band_color);
        }
    }

    /**
     * Finds for each MarkerCollection if it is empty or not and updates 
     * the two BitSets leftVisible and rightVisible.
     *
     */
    private void updateVisibility() {
        leftVisible.clear();
        rightVisible.clear();

        if (!isCondensedMode()) {
            leftVisible.set(0, getNumMarkers());
            rightVisible.set(0, getNumMarkers());
            return;
        }

        for (int i = 0; i < markers.size(); ++i) {
            for (Marker marker : markers.get(i).markers) {
                if (marker == null) {
                    continue;
                }
                if (marker.value < 2 || marker.value < 0) {
                    leftVisible.set(i);
                }
                if (marker.value > 2 || marker.value < 0) {
                    rightVisible.set(i);
                }

                if (leftVisible.get(i) && rightVisible.get(i)) {
                    break;
                }
            }
        }
    }

    /**
     * 
     * @param markerIndex
     * @return -1 if the index line is NOT found
     */
    public int markerToSampleIndex(int markerIndex) {
        if (!isCondensedMode()) {
            return Math.abs(markerIndex) - 1;
        }

        updateVisibility();

        BitSet set = null;
        if (markerIndex < 0) {
            set = leftVisible;
        } else {
            set = rightVisible;
        }

        markerIndex = Math.abs(markerIndex);

        for (int i = set.nextSetBit(0), column = 1; i >= 0; i = set.nextSetBit(i + 1), ++column) {
            if (column == markerIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 
     * @param sampleIndex
     * @param leftSide
     * @return 0 if there is no profile line for the given sample
     */
    public int sampleToMarkerIndex(int sampleIndex, boolean leftSide) {
        if (sampleIndex < 0 || sampleIndex >= getMarkers().length) {
            throw new IndexOutOfBoundsException("sampleIndex is out of bounds");
        }

        if (!isCondensedMode()) {
            return leftSide ? -(1 + sampleIndex) : (1 + sampleIndex);
        }

        updateVisibility();

        BitSet set = null;

        if (leftSide) {
            set = leftVisible;
        } else {
            set = rightVisible;
        }

        for (int i = set.nextSetBit(0), j = 1; i >= 0; i = set.nextSetBit(i + 1), ++j) {
            if (i == sampleIndex) {
                return leftSide ? -j : j;
            }
        }
        return 0;
    }

    protected void drawAberrations(Graphics g) {
        updateVisibility();

        // left side
        for (int i = leftVisible.nextSetBit(0), pos = -1; i >= 0; i = leftVisible.nextSetBit(i + 1), --pos) {
            // find all markers in view
            M = markers.get(i);
            for (Marker marker : M.find(view)) {
                if (marker == null) {
                    continue;
                }

                Color color = mainWindow.parameters.getColor(marker.value, false);

                if (marker.value != 2) {
                    drawMarker(g, marker, pos, lineWidth, color, color, false, null);
                }

            }
        }

        // right side
        for (int i = rightVisible.nextSetBit(0), pos = 1; i >= 0; i = rightVisible.nextSetBit(i + 1), ++pos) {
            // find all markers in view
            M = markers.get(i);
            for (Marker marker : M.find(view)) {
                if (marker == null) {
                    continue;
                }

                Color color = mainWindow.parameters.getColor(marker.value, false);

                if (marker.value != 2) {
                    drawMarker(g, marker, pos, lineWidth, color, color, false, null);
                }
            }
        }
    }

    public Dimension updateLayout() {
        //	FontMetrics metrics = g.getFontMetrics();
        int height = scaleY(350);

        // calculate widths of all 6 regions
        // 1. leftMarkerBounds
        // 2. leftInfoBounds (SNP)        
        // 3. labelBounds
        // 4. ideogramBounds
        // 5. rightInfoBounds (genes)
        // 6. rightMarkerBounds

        if (showLabels) {
            //labelBounds.width = (metrics.stringWidth("22q13.33a")*13)/12; // metrics.charWidth('X') * MAX_LABEL_LENGTH;
            labelBounds.width = scaleX(20);
        } else {
            labelBounds.width = 0;
        }


        ideogramBounds.width = scaleX(20);

        deltaX = scaleX(5);
        leftMarkerBounds.width = deltaX * getNumLeft() + scaleX(8);
        rightMarkerBounds.width = deltaX * getNumRight() + scaleX(8);

        // compute x coordinates
        int x = 0;

        leftMarkerBounds.x = x;
        x += leftMarkerBounds.width;

        leftInfoBounds.x = x;
        x += leftInfoBounds.width;

        labelBounds.x = x;
        x += labelBounds.width;

        ideogramBounds.x = x;
        x += ideogramBounds.width;

        rightInfoBounds.x = x;
        x += rightInfoBounds.width;

        rightMarkerBounds.x = x;
        x += rightMarkerBounds.width;

        // adapt heights + y coordinates

        //ideogramBounds.height = Math.min( (height * 9) / 10, height - 20 );
        //ideogramBounds.y = (height - ideogramBounds.height) / 2;
        ideogramBounds.y = scaleY(20); // 2*metrics.getHeight(); // there is a small label on the top
        ideogramBounds.height = height - ideogramBounds.y - scaleY(4 * 20); //(4*metrics.getHeight()); // there is a small label on to bottom

        leftMarkerBounds.y = ideogramBounds.y;
        leftMarkerBounds.height = ideogramBounds.height;

        leftInfoBounds.y = ideogramBounds.y;
        leftInfoBounds.height = ideogramBounds.height;

        labelBounds.y = ideogramBounds.y;
        labelBounds.height = ideogramBounds.height;

        rightInfoBounds.y = ideogramBounds.y;
        rightInfoBounds.height = ideogramBounds.height;

        rightMarkerBounds.y = ideogramBounds.y;
        rightMarkerBounds.height = ideogramBounds.height;

        lineWidth = 2;

        return new Dimension(x, height);
    }

    private int scaleX(int x) {
        return (int) Math.round((double) x * scaleX);
    }

    private int scaleY(int y) {
        return (int) Math.round((double) y * scaleY);
    }

    /**
     * Necessary actions for deserializing the object.
     *  
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initTransientState();
    }

    /**
     * Shows an activation frame around the ideogram.
     * @param active Set true if frame should be shown.
     * @return True if something changed
     */
    public boolean setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            if (!active) {
                clearSelections();
                setRangeSelection(null);
            } else {
                fireSelectionChanged();
            }
            repaint();
            return true;
        }
        return false;
    }

    /**
     * 
     * @return True if the current ideogram is selected and active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Inits all transient variables (excluded in the deserialization process).
     *
     */
    private void initTransientState() {
        setEnabled(true);
        setFocusable(true);
        setOpaque(true);

        selectionChangedListeners = new LinkedList<ChangeListener>();

        //setOpaque(true);

        setToolTipText("");
        view = new Interval(0, MAX_BASEPAIRS);
        ideogramBounds = new Rectangle();
        labelBounds = new Rectangle();
        leftMarkerBounds = new Rectangle();
        leftInfoBounds = new Rectangle();
        rightInfoBounds = new Rectangle();
        rightMarkerBounds = new Rectangle();
        active = false;
        chromosomeLength = 0;
        paintBuffer = null;
        paintBufferValid = false;

        addMouseListener(new IdeogramMouseListener());
        addMouseMotionListener(new IdeogramMouseMotionListener());
        addMouseWheelListener(new IdeogramMouseWheelListener());

        // fix tab button bug
        setFocusTraversalKeysEnabled(false);

        // add context menus

        // blinking caret timer
        timer = new Timer(300, this);
        timer.start();

        updateSize();
    }

    // EVENT LISTENERS
    public JToolTip createToolTip() {
        return new JMultiLineToolTip();
    }

    /**
     * Custom tooltip texts (showing markers and chromosomal regions).
     */
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();

        String tip = null;

        long bp = YCoordToBase(p.y);

        if (ideogramBounds.contains(p) || labelBounds.contains(p)) {
            // find chromosomal location
            if (db != null) {
                IntervalTreeQuery query = new IntervalTreeQuery(getTree());
                ArrayList list = query.Query(new Interval(bp, bp), (byte) 2, (byte) 2);
                if (list.size() > 0) {
                    IntervalTreeNode node = (IntervalTreeNode) list.get(0);
                    Band band = (Band) node.content;
                    tip = " " + band.toString() + " ";
                }
            }
            return tip;
        }

        // find marker(s)
        int value = xCoordToMarker(p.x);

        DecimalFormat format = new DecimalFormat("0.00");

        int idx = Math.abs(value) - 1;
        if (idx >= 0 && idx < markers.size()) {
            MarkerCollection M = (MarkerCollection) markers.get(idx);
            int marker_index = M.findNearest(bp, value > 0);
            Marker marker = null;
            if (marker_index >= 0) {
                marker = (Marker) M.getMarkers().get(marker_index);
            }
            if (marker == null) {
                return tip;
            }

            if (tip == null) {
                tip = new String();
            }
            if (tip.length() > 0) {
                tip += "\n";
            }
            if (marker.info != null) {
                tip += marker.info + "\n";
            }

            tip += "[" + format.format((float) marker.interval.from / (float) 1000000) + " M - "
                    + format.format((float) marker.interval.to / (float) 1000000) + " M ]\n";

            // TODO: understand and change the following!!!
			/*
            if( idx < getMainWindow().dataModels.size() )
            {
            CopyNumberTransformer trf = (CopyNumberTransformer)getMainWindow().dataModels.get(idx);
            Object val = trf.getDataModel().getValue(getMainWindow().comboMarker.getSelectedItem().toString(),marker_index);
            if( val != null )
            tip += val.toString();
            }
             */
            /*
            LinkedList list = M.find(new Interval(bp,bp)); // TODO: side???
            Iterator iter = list.iterator();
            while( iter.hasNext() )
            {
            Marker marker = (Marker)iter.next();
            }
             */

        }

        return tip;
    }

    /**
     * Selects a point in the ideogram (usually a left mouse click). 
     * @param p Point in component coordinates. 
     */
    public void selectPoint(Point p) {
        boolean found = false;

        long bp = YCoordToBase(p.y);	// convert the y coordinate of the mouse pointer to basepairs
        // clearSelections();
        if (ideogramBounds.contains(p) || labelBounds.contains(p)) {	// IDEOGRAM
            found = true;
            // select chromosomal location with the mouse
            if (db != null) {
                IntervalTreeQuery query = new IntervalTreeQuery(db.getTree()[chromosome - 1]);
                ArrayList list = query.Query(new Interval(bp, bp), (byte) 2, (byte) 2);
                if (list.size() > 0) {
                    /*
                    IntervalTreeNode node = (IntervalTreeNode)list.get(0);
                    Band band = (Band)node.content;
                     */
                    // TODO: selected band
                }
            }
        }


        // finds the nearest marker
        if (!found) {
            int markerIndex = xCoordToMarker(p.x),
                    sampleIndex = markerToSampleIndex(markerIndex);

            if (sampleIndex >= 0 && sampleIndex < markers.size()) {
                MarkerCollection M = (MarkerCollection) markers.get(sampleIndex);

                List<Marker> list = M.find(new Interval(bp, bp));

                int idx = list.indexOf(getSelectedMarker());

                if (idx >= 0) {	// cycle through selection
                    setSelectedSample(list.get((idx + 1) % list.size()), sampleIndex);
                } else {
                    if (list.size() > 0) {
                        setSelectedSample(list.get(0), sampleIndex);
                    } else {
                        int marker_index = M.findNearest(bp, markerIndex > 0);
                        if (marker_index >= 0) {
                            setSelectedSample(M.getMarkers().get(marker_index), sampleIndex);
                        } else {
                            setSelectedSample(null, sampleIndex);
                        }
                    }
                }
            }
        }

        setActive(true);
    }

    /**
     * Selects a marker/sample
     * @param marker
     * @param sampleIndex Which marker collection 0..n-1
     * @return True if the selection changed
     */
    private boolean setSelectedSample(Marker marker, int sampleIndex) {
        if (marker != selectedMarker || sampleIndex != selectedSampleIndex) {
            selectedMarker = marker;
            selectedSampleIndex = sampleIndex;

            // invalidatePaintBuffer();
            repaint();
            fireSelectionChanged();
            return true;
        }
        return false;
    }

    public Marker getSelectedMarker() {
        return selectedMarker;
    }

    /**
     * 
     * @return The selected marker line
     */
    public int getSelectedMarkerIndex() {
        if (selectedMarker == null) {
            return 0;
        }
        return sampleToMarkerIndex(selectedSampleIndex, selectedMarker.value < 2);
    }

    /**
     * 
     * @return True if an item is selected (marker, gene etc.)
     */
    public boolean isSelection() {
        return (selectedMarker != null) || (selected_gene != null);
    }

    /**
     * 
     * @return True if at least one selections changed.
     */
    public boolean clearSelections() {
        boolean changed = false;
        changed |= setSelectedSample(null, -1);
        return changed;
    }

    public void addSelectionChangedListener(ChangeListener obj) {
        if (obj != null) {
            if (!selectionChangedListeners.contains(obj)) {
                selectionChangedListeners.add(obj);
            }
        }
    }

    protected void fireSelectionChanged() {
        ChangeEvent event = new ChangeEvent(this);

        for (ChangeListener listener : selectionChangedListeners) {
            listener.stateChanged(event);
        }
    }

    /**
     * Redraws the currently selected marker (blinking).
     *
     */
    public void toggleMarker() {
        if (!isSelection()) {
            return;
        }

        marker_state = !marker_state;
        repaint();
    }

    /**
     * draw marker lines and selected profile lines
     */
    public void paintBackgroundSelections(Graphics g) {
        for (int i = leftVisible.nextSetBit(0), pos = -1; i >= 0; i = leftVisible.nextSetBit(i + 1), --pos) {
            boolean sel = (i == selectedSampleIndex);
            if (sel) {
                g.setColor(colSampleSelected);
            } else {
                g.setColor(colSample);
            }

            // draw vertical orientation/marker profile lines
            if (isShowProfileLines() || sel) {
                int x1 = markerToXCoord(pos),
                        y1 = ideogramBounds.y,
                        y2 = ideogramBounds.y + ideogramBounds.height;

                g.drawLine(x1, y1, x1, y2);
            }
        }

        // right side
        for (int i = rightVisible.nextSetBit(0), pos = 1; i >= 0; i = rightVisible.nextSetBit(i + 1), ++pos) {
            boolean sel = (i == selectedSampleIndex);
            if (sel) {
                g.setColor(colSampleSelected);
            } else {
                g.setColor(colSample);
            }

            // draw vertical orientation/marker profile lines
            if (isShowProfileLines() || sel) {
                int x1 = markerToXCoord(pos),
                        y1 = ideogramBounds.y,
                        y2 = ideogramBounds.y + ideogramBounds.height;

                g.drawLine(x1, y1, x1, y2);
            }
        }
    }

    public void paintForegroundSelections(Graphics graphics) {
        if (selectedMarker != null) {
            Color colLeft = null, colRight = null;

            colLeft = mainWindow.parameters.getColor(selectedMarker.value, marker_state);
            colRight = mainWindow.parameters.getColor(selectedMarker.value, marker_state);

            drawMarker(graphics,
                    selectedMarker,
                    getSelectedMarkerIndex(),
                    lineWidth, colLeft, colRight, true, null);
        }
    }

    private class IdeogramMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                zoom(+1);
            }
        }
        /*public void mouseEntered(MouseEvent e)
        {
        //IdeogramBean ideo = (IdeogramBean)e.getSource();
        //ideo.requestFocusInWindow();
        
        }
        
        public void mouseExited(MouseEvent e)
        {
        //IdeogramBean ideo = (IdeogramBean)e.getSource();
        //ideo.setActive(false);
        }*/

        public void mousePressed(MouseEvent e) {
            IdeogramView ideo = (IdeogramView) e.getSource();
            ideo.requestFocusInWindow();
            if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
                // open popup menu if desired
                // select next marker
                // ideo.selectPoint(e.getPoint());
                //if(getSelectedMarker()==null || (Math.abs(xCoordToMarker(e.getX())) >= markers.size() && consensusMode) )

                int idx = getSelectedSampleIndex();
                boolean canMerge = false;
                if (idx >= 0) {	// check if this data slot enables merging
                    //DataSlot slot = dataSlots.get(idx);
                    canMerge = true;
                }

                if (canMerge) {
                    mainWindow.mergeMarkerItem.setEnabled(true);
                    mainWindow.removeMarkerItem.setEnabled(true);
                } else {
                    mainWindow.mergeMarkerItem.setEnabled(false);
                    mainWindow.removeMarkerItem.setEnabled(false);
                }


                mainWindow.popmenu.show(e.getComponent(), e.getX(), e.getY());
            }

            if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) > 0) {
                ideo.resetView();

                return;
            }
            if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                // select next marker
                ideo.selectPoint(e.getPoint());
                beginSelection(e.getPoint());
            }
        }

        public void mouseReleased(MouseEvent event) {
            if (isDragging) {
                if (Math.abs(endPoint.y - beginPoint.y) < 5) {
                    setRangeSelection(null);
                }
                isDragging = false;
                if (getRangeSelection() != null) {
                    zoom(-1);
                }
                firePropertyChange("isDragging", true, false);
            }
        }
    }

    private class IdeogramMouseMotionListener extends MouseMotionAdapter {

        public void mouseMoved(MouseEvent e) {
            /*
            IdeogramBean ideo = (IdeogramBean) e.getSource();
            Graphics g = ideo.getGraphics();
            g.drawRect(e.getX() - 1, e.getY() - 1, 2, 2);
             */
        }

        public void mouseDragged(MouseEvent event) {

            if (!isDragging) {
                isDragging = true;
                firePropertyChange("isDragging", false, true);
            }
            endSelection(event.getPoint());
        }
    }

    private class IdeogramMouseWheelListener
            implements MouseWheelListener {

        /**
         * Scroll and Zoom into the ideogram.
         * 
         * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
         */
        public void mouseWheelMoved(MouseWheelEvent event) {
            IdeogramView ideo = (IdeogramView) event.getSource();
            int dy = event.getWheelRotation();

            if (event.isShiftDown()) {
                ideo.zoom(dy);
            } else {
                ideo.scroll(dy);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        // timer action (blinking caret)
        if (event.getSource() == timer) {
            toggleMarker();
        }
    }

    /**
     * Croll to the given basepair (such that the position is centered)
     * @param pos
     */
    public void center(long pos) {
        Interval view = getView();
        long delta = view.getLength(),
                start = pos - delta / 2;
        setView(new Interval(start, start + delta));
    }

    public IdeogramMainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(IdeogramMainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void scroll(int dy) {
        long delta = (view.getLength() * dy) / 5;
        setView(new Interval(view.from + delta, view.to + delta));
    }

    public void scrollTo(boolean top) {
        long L = getView().getLength();
        if (top) {
            setView(new Interval(0, L));
        } else {
            if (L < getChromosomeLength()) {
                setView(new Interval(getChromosomeLength() - L, getChromosomeLength()));
            }
        }
    }

    public void pushView() {
        if ((viewHistory.size() == 0) || !viewHistory.peek().equals(getView())) {
            viewHistory.addFirst(getView());
            while (viewHistory.size() > 5) {
                viewHistory.removeLast();
            }
        }
    }

    public Interval popView() {
        if (viewHistory.size() > 0) {
            return viewHistory.poll();
        } else {
            return null; // new Interval(0,MAX_BASEPAIRS);
        }
    }

    /**
     * Zooms in (for dy < 0) or out (for dy > 0). 
     * Zooms in into a range selection (if it exists).
     * @param dy Zoom step directly proportional to 1/5th of the current field of view. 
     */
    public void zoom(int dy) {
        if (dy < 0 && (getRangeSelection() != null)) {	// zoom in into range selection
            Interval range = getRangeSelection();
            range.to = Math.max(range.to, range.from + MIN_LENGTH);
            pushView();
            setView(range);
            return;
        }
        if (dy > 0) {
            Interval view = popView();
            if (view != null) {
                setView(view);
                return;
            }
        }

        long length = view.getLength();
        length += (length * dy) / 5;

        // if a selection is in the current view keep it in the zoomed view
        Marker m = selected_gene;
        if (selectedMarker != null) {
            m = selectedMarker;
        }
        if ((m != null) && (m.interval != null)) {
            if (view.intersects(m.interval)) {
                // center the marker in the view
                long start = Math.max(0, m.interval.from - length / 10);
                setView(new Interval(start, start + length));
                return;
            }
        }
        setView(new Interval(view.from, view.from + length));
    }

    public void setDataModel(List<DataSlot> data) {
        dataSlots = data;
        updateSize();
    }

    public int getNumLeft() {
        updateVisibility();
        return leftVisible.cardinality();
    }

    public int getNumRight() {
        updateVisibility();
        return rightVisible.cardinality();
    }

    public void directPaintToGraphics(Graphics graphics, int width, int height) {
        //
        double old_scaleX = scaleX,
                old_scaleY = scaleY;

        updateLayout();

        paintBackgroundSelections(graphics);
        directPaint(graphics);

        scaleX = old_scaleX;
        scaleY = old_scaleY;

        updateLayout();
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) graphics;

        int res_x = getDim().width,
                res_y = getDim().height;


        double pWidth = pageFormat.getImageableWidth(),
                pHeight = pageFormat.getImageableHeight();

        double scale = Math.min(pWidth / res_x,
                pHeight / res_y);

        double w = getWidth() * scale,
                h = getHeight() * scale;

        g2.translate(pageFormat.getImageableX() + (pWidth - w) * 0.5,
                pageFormat.getImageableY() + (pHeight - h) * 0.5);
        g2.scale(scale, scale);
        try {
            //directPaintToGraphics(g2, res_x, res_y);
            paintBackgroundSelections(g2);
            directPaint(g2);
        } catch (Exception e) {
            throw new PrinterException("Exception while printing\n" + e.getMessage());
        }
        return PAGE_EXISTS;
    }

    public Dimension updateSize() {
        dim = updateLayout();

        setPreferredSize(dim);
        invalidatePaintBuffer();
        invalidate();
        return dim;
    }

    public Dimension getDim() {
        return dim;
    }

    public void setZoomSize(int sx, int sy) {
        scaleX = 1.0;
        scaleY = 1.0;
        Dimension dim = updateLayout();
        scaleX = (double) sx / (double) dim.width;
        scaleY = (double) sy / (double) dim.height;
        updateLayout();
        // updateSize();
    }

    public Dimension setZoomFactor(double sx, double sy) {
        scaleX = sx;
        scaleY = sy;
        return updateSize();
    }

    public boolean isShowProfileLines() {
        return showProfileLines;
    }

    public void setShowProfileLines(boolean drawProfileLines) {
        if (this.showProfileLines != drawProfileLines) {
            this.showProfileLines = drawProfileLines;
            invalidatePaintBuffer();
        }
    }

    public boolean isCondensedMode() {
        return condensedMode;
    }

    public void setCondensedMode(boolean condensedMode) {
        if (this.condensedMode != condensedMode) {
            this.condensedMode = condensedMode;
            updateSize();
        }
    }

    // +++++++++++++++++++++ RANGE SELECTION ++++++++++++++++++++++
    public Interval getRangeSelection() {
        return rangeSelection;
    }

    public void setRangeSelection(Interval selection) {
        rangeSelection = selection;
        repaint();
        firePropertyChange("rangeSelection", null, rangeSelection);
    }

    private void paintRangeSelection(Graphics g) {
        if (getRangeSelection() == null) {
            return;
        }

        int y1 = BaseToYCoord(getRangeSelection().from),
                y2 = BaseToYCoord(getRangeSelection().to);

        // g.setXORMode(getSelectionBackground());
        g.setPaintMode();
        g.setColor(getSelectionBackground());

        g.fillRect(0, y1, getWidth(), y2 - y1 + 1);
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    public void setSelectionBackground(Color selectionBackground) {
        this.selectionBackground = selectionBackground;
        repaint();
    }

    private void beginSelection(Point point) {
        beginPoint = point;
        endPoint = point;
        setRangeSelection(null);
    }

    private void endSelection(Point point) {
        endPoint = point;
        if (beginPoint == null) {
            beginPoint = point;
        }
        Interval x = new Interval(YCoordToBase(beginPoint.y),
                YCoordToBase(endPoint.y));
        setRangeSelection(x);
    }
}
