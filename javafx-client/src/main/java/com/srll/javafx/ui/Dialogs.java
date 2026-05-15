package com.srll.javafx.ui;

import javafx.scene.control.DialogPane;

/** Applies the neo-retro app stylesheet to any {@link DialogPane}. */
public final class Dialogs {

    private static String cssUrl;

    private Dialogs() {}

    /**
     * Injects app.css into the given pane and sets a comfortable minimum
     * width. Call this right after creating any Dialog or Alert, before
     * {@code showAndWait()}.
     */
    public static void style(DialogPane pane) {
        if (cssUrl == null) {
            var url = Dialogs.class.getResource("/com/srll/javafx/css/app.css");
            if (url != null) cssUrl = url.toExternalForm();
        }
        if (cssUrl != null && !pane.getStylesheets().contains(cssUrl)) {
            pane.getStylesheets().add(cssUrl);
        }
        pane.setMinWidth(400);
    }
}
