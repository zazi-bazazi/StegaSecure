module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;

    opens org.example to javafx.fxml;
    opens org.example.controller to javafx.fxml;

    exports org.example;
    exports org.example.controller;
    exports org.example.model.stego;
    exports org.example.model.ga;
    exports org.example.model.ga.abstractClasses;
    exports org.example.model.image;
}
