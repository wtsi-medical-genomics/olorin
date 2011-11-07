
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
import java.awt.event.MouseEvent;
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
    private int minMatches;
    

    public IdeogramView(double r) {
        // dragmode
        selectionBackground = new Color(0xdd, 0xdd, 0xff);
        viewHistory = new LinkedList<Interval>();

        scaleX = 1.0;
        scaleY = r;
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

    public void setSegments (MarkerCollection mc, int mm) {
        segments = mc;
        minMatches = mm;
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
        paintIdeogramBuffered(graphics);
        
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

        byte last_arm = 0, next_arm = 0, last_type = Band.DENSITY_UNKNOWN, next_type = Band.DENSITY_UNKNOWN;

        boolean top_closed = true, bottom_closed = true;

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
        }

        // draw chromosome name
        String name = chromosome + "";
        g.setFont(g.getFont().deriveFont(9.0f));
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
        int y1 = BaseToYCoord(interval.from);
        int y2 = BaseToYCoord(interval.to);
        if (matches >= minMatches) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(new Color(255/matches,255/matches,255));
        }
        g.fillRect(rightMarkerBounds.x +5, y1, 6 * matches, y2 - y1);	// draw single marker		
    }

    public Dimension updateLayout() {
        int height           = scaleY(350);
        ideogramBounds.width = scaleX(15);

        deltaX = scaleX(5);
        // compute x coordinates
        int x = 0;

        ideogramBounds.x = x;
        x += ideogramBounds.width;

        rightInfoBounds.x = x;
        x += rightInfoBounds.width;

        rightMarkerBounds.x = x;
        x += rightMarkerBounds.width;

        ideogramBounds.y = scaleY(20); // 2*metrics.getHeight(); // there is a small label on the top
        ideogramBounds.height = height - ideogramBounds.y - scaleY(4 * 20); //(4*metrics.getHeight()); // there is a small label on to bottom

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

    public void directPaintToGraphics(Graphics graphics, int width, int height) {
        //
        double old_scaleX = scaleX,
                old_scaleY = scaleY;

        updateLayout();


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
