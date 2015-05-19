package de.tud.swporganize.controller;

import de.tud.github.GitHubManager;
import de.tud.groups.GroupManager;
import de.tud.jenkins.JenkinsJobManager;
import de.tud.jenkins.JenkinsServerManager;
import de.tud.util.LogStatementHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import sun.rmi.runtime.Log;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;


public class OverviewController implements Observer, Initializable {

    @FXML
    private TextField txtNamePrefix;
    @FXML
    private TextField txtJenkinsURL;
    @FXML
    private TextField txtJenkinsName;
    @FXML
    private TextField txtGhName;
    @FXML
    private TextField txtGhOauth;
    @FXML
    private PasswordField txtJenkinsPwd;
    @FXML
    private TextField txtGrpCount;
    @FXML
    private Label stateLabelGroups;
    @FXML
    private Label stateLabelRepositories;
    @FXML
    private Label stateLabelJenkins;
    @FXML
    private TextArea textStateLog;

    @FXML
    private void startSetup() {
        String namePrefix = this.txtNamePrefix.getText();
        String jenkinsURL = this.txtJenkinsURL.getText();
        String jenkinsUsername = this.txtJenkinsName.getText();
        char[] jenkinsPassword = this.txtJenkinsPwd.getText().toCharArray();
        String ghUsername = this.txtGhName.getText();
        String ghOauth = this.txtGhOauth.getText();

        Optional<Integer> grpCount = this.evaluateGrpCount();
        if (!grpCount.isPresent()) {
            showNoNumberErrorDialog();
            return;
        }

        this.registerObservables();

        GroupManager.instanceOf().setNamePrefix(namePrefix);
        GroupManager.instanceOf().setGroupCount(grpCount.get());
        GitHubManager.instanceOf().setUsername(ghUsername);
        GitHubManager.instanceOf().setOauthToken(ghOauth);
        JenkinsServerManager.instanceOf().setJenkinsUsername(jenkinsUsername);
        JenkinsServerManager.instanceOf().setJenkinsPassword(jenkinsPassword);
        JenkinsServerManager.instanceOf().setHostname(jenkinsURL);
        GitHubManager.instanceOf().connectToGitHub();
        GroupManager.instanceOf().createGroups();
    }

    private void registerObservables() {
        GroupManager.instanceOf().addObserver(this);
        GitHubManager.instanceOf().addObserver(this);
        JenkinsServerManager.instanceOf().addObserver(this);
        JenkinsJobManager.instanceOf().addObserver(this);
        LogStatementHelper.instanceOf().addObserver(this);
    }

    private Optional<Integer> evaluateGrpCount() {
        try {
            Integer grpCount = Integer.parseInt(this.txtGrpCount.getText());
            return Optional.of(grpCount);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private void showNoNumberErrorDialog() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Falsche Eingabe");
        error.setContentText("Bitte geben Sie als Gruppenanzahl nur einen numerischen Wert ein.");
        error.show();
    }

    private void setLabelToDone(Label label) {
        label.setText("Erfolg");
        label.getStyleClass().add("successState");
    }

    private void setLabelToError(Label label) {
        label.setText("Fehler");
        label.getStyleClass().add("errorState");
    }

    public void addToStateLog(String message) {
        System.out.println(message);
        this.textStateLog.appendText("\n");
        this.textStateLog.appendText(message);
        System.out.println(this.textStateLog.getText());
    }

    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void update(Observable o, Object arg) {

        if(o instanceof LogStatementHelper) {
            String message = String.valueOf(arg);
            this.addToStateLog(message);
            return;
        }

        Label toChange = null;

        if (o.equals(GroupManager.instanceOf())) {
            toChange = this.stateLabelGroups;
        } else if (o.equals(GitHubManager.instanceOf())) {
            toChange = this.stateLabelRepositories;
        } else if (o.equals(JenkinsJobManager.instanceOf())) {
            toChange = this.stateLabelJenkins;
        } else if (o.equals(JenkinsServerManager.instanceOf())) {
            toChange = this.stateLabelJenkins;
        }

        boolean error = (boolean) arg;

        if (error) {
            this.setLabelToError(toChange);
        } else {
            this.setLabelToDone(toChange);
        }


    }


}
