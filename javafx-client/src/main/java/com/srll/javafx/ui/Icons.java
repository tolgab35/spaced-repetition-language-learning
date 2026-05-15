package com.srll.javafx.ui;

import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

/**
 * Vector icon factory — pure JavaFX {@link SVGPath} shapes, no external
 * dependencies. Each constant is 24x24 viewBox path data; {@link #of} builds
 * a square, CSS-colourable node from it.
 */
public final class Icons {

    private Icons() {}

    /** Stacked layers — app brand / flashcards. */
    public static final String LAYERS =
        "M11.99 18.54l-7.37-5.73L3 14.07l9 7 9-7-1.63-1.27-7.38 5.74zM12 16l7.36-5.73L21 9l-9-7-9 7 1.63 1.27L12 16z";
    /** Waste bin — delete actions. */
    public static final String TRASH =
        "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    /** Solid play triangle. */
    public static final String PLAY = "M8 5v14l11-7z";
    /** Grid of tiles — card list. */
    public static final String GRID =
        "M4 11h5V5H4v6zm0 7h5v-6H4v6zm6 0h5v-6h-5v6zm6 0h5v-6h-5v6zm-6-7h5V5h-5v6zm6-6v6h5V5h-5z";
    /** Five-point star. */
    public static final String STAR =
        "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    /** Flame — streaks. */
    public static final String FLAME =
        "M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36"
        + "C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67z"
        + "M11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58"
        + ".39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z";
    /** Cut gem. */
    public static final String DIAMOND = "M19 3H5L2 9l10 13L22 9l-3-6z";
    /** Tick inside a circle. */
    public static final String CHECK_CIRCLE =
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41"
        + "L10 14.17l7.59-7.59L19 8l-9 9z";
    /** Trophy cup. */
    public static final String TROPHY =
        "M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94.63 1.5 1.98 2.63 3.61 2.96"
        + "V19H7v2h10v-2h-4v-3.1c1.63-.33 2.98-1.46 3.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2z"
        + "M5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z";
    /** Heraldic shield. */
    public static final String SHIELD =
        "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z";
    /** Crown. */
    public static final String CROWN =
        "M5 16L3 7l5.5 4L12 4l3.5 7L21 7l-2 9H5zm0 3h14v2H5v-2z";

    /**
     * Builds a square icon node of the given path, coloured via its background
     * fill so it can be re-tinted from CSS or inline style.
     *
     * @param path     24x24 SVG path data (use a constant above)
     * @param size     width and height in pixels
     * @param colorHex fill colour, e.g. {@code "#1A1A2E"}
     */
    public static Region of(String path, double size, String colorHex) {
        SVGPath svg = new SVGPath();
        svg.setContent(path);

        Region r = new Region();
        r.setShape(svg);
        r.setMinSize(size, size);
        r.setPrefSize(size, size);
        r.setMaxSize(size, size);
        r.setStyle("-fx-background-color: " + colorHex + ";");
        return r;
    }
}
