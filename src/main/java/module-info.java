module alture.esposamandoufx {
    requires javafx.controls;
    requires javafx.fxml;


    opens alture.esposamandoufx to javafx.fxml;
    exports alture.esposamandoufx;
}