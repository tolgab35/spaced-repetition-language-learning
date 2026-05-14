module com.srll.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    opens com.srll.javafx to javafx.graphics;
    opens com.srll.javafx.controller to javafx.fxml;
    opens com.srll.javafx.http.dto to com.fasterxml.jackson.databind;
}
