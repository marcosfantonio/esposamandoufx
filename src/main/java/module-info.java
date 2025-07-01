module alture.esposamandoufx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires rocksdbjni;

    opens alture.esposamandoufx to javafx.fxml;
    exports alture.esposamandoufx;

    exports alture.esposamandoufx.controller;
    opens alture.esposamandoufx.controller to javafx.fxml;

    exports alture.esposamandoufx.model;
    opens alture.esposamandoufx.model to javafx.fxml, javafx.base;

    exports alture.esposamandoufx.service;
    opens alture.esposamandoufx.service to javafx.fxml;
}
