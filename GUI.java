

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * A GUI made with JavaFX.
 *
 * @author Luke Craig
 * @version 2017-11-22
 */
public class GUI extends Application
{
    // layout constants
    private static final int HORIZONTAL_SPACING = 10;
    private static final int VOTE_MAP_TABLE_CELL_SIZE = 25;

    // CSS ID's
    private static final String ID_EXPORTED_FILES_ROW = "exportedFilesRow";
    private static final String ID_VOTE_MAP_ROW = "voteMapRow";
    private static final String ID_IGNORED_QUESTIONS_ROW =
        "ignoredQuestionsRow";
    private static final String ID_EXPORT_BUTTON_ROW = "exportButtonRow";
    // Node references for disabling/enabling interactivity
    private Node voteMapRowNode;
    private Node exportButtonRowNode;
    private Node ignoredQuestionsRowNode;
    private Node exportedFilesRowNode;

    // fields to pass into the CLI as arguments:
    private File sessionXML;
    private File extractorJAR;
    private String ignoredQuestions = "";

    private boolean okayToExtract = false;
    private static String opSys = "";

    /*
    */
    private final ObservableList<VoteMapping> voteMappings =
        FXCollections.observableArrayList(
            new VoteMapping("A", "Yes"),
            new VoteMapping("B", ""),
            new VoteMapping("C", "No"),
            new VoteMapping("D", ""),
            new VoteMapping("E", "Abstain")
        );

    private Button exportedFileButton;

    /**
     * Launches the GUI.
     *
     * @param args the command line arguments passed to the application.
     *             An application may get these parameters using the
     *             {@link #getParameters()} method.
     */
    public static void main(String[] args)
    {
	String os = System.getProperty("os.name").toLowerCase();
	if (os.substring(0, 3).equals("mac")) 
	    opSys = "mac";
	else if (os.substring(0, 3).equals("win")) 
	    opSys = "win";

        launch(GUI.class, args);
    }

    @Override
    public void start(Stage stage)
    {
        VBox vbox = addVBox(stage);

        Scene scene = new Scene(vbox,600,600);
        stage.setScene(scene);
        stage.setTitle("IClicker Vote Extractor");
	//        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    /**
     * Creates the main vertical box layout that everything else goes in.
     *
     * @param stage The main stage
     * @return the VBox
     */
    private VBox addVBox(Stage stage)
    {

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(HORIZONTAL_SPACING));
        vbox.setSpacing(12);

        HBox sessionDataFileRow = makeSessionDataFileRow(stage);
        HBox extractorFileRow = makeExtractorFileRow(stage);

        HBox voteMapRow = makeVoteMapRow();
        voteMapRow.setId(ID_VOTE_MAP_ROW);
	/*
	*/

        HBox ignoredQuestionsRow = makeIgnoredQuestionsRow();
        ignoredQuestionsRow.setId(ID_IGNORED_QUESTIONS_ROW);

        Separator exportSeparator = new Separator();

        HBox exportButtonRow = makeExportButtonRow();
        exportButtonRow.setId(ID_EXPORT_BUTTON_ROW);

        HBox exportedFilesRow = makeExportedFilesRow();
        exportedFilesRow.setId(ID_EXPORTED_FILES_ROW);

        Separator exportSeparator2 = new Separator();

	HBox helpRow = makeHelpRow();

        vbox.getChildren().addAll(sessionDataFileRow, 
				  extractorFileRow,
				  voteMapRow,
				  ignoredQuestionsRow,
				  exportSeparator, 
				  exportButtonRow, 
				  exportedFilesRow,
				  exportSeparator2, 
				  helpRow
				  );

        for (Node child : vbox.getChildren())
        {
            VBox.setVgrow(child, Priority.ALWAYS);
        }

        voteMapRowNode = vbox.lookup("#" + ID_VOTE_MAP_ROW);
        ignoredQuestionsRowNode = vbox.lookup("#" + ID_IGNORED_QUESTIONS_ROW);
        voteMapRowNode.setDisable(true);
        ignoredQuestionsRowNode.setDisable(true);
	/*
	*/
        exportButtonRowNode = vbox.lookup("#" + ID_EXPORT_BUTTON_ROW);
        exportedFilesRowNode = vbox.lookup("#" + ID_EXPORTED_FILES_ROW);

        exportButtonRowNode.setDisable(true);
        exportedFilesRowNode.setVisible(false);

        return vbox;
    }

    private HBox makeHelpRow()
    {
        HBox hbox = new HBox();
        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("Problems? Contact Jay Fenwick: fenwickjb x2708");
        hbox.getChildren().addAll(text);
        return hbox;
    }

    /**
     * Make the row for opening the xml file.
     *
     * @param stage the main Stage
     * @return the HBox
     */
    private HBox makeSessionDataFileRow(Stage stage)
    {
        final FileChooser XML_CHOOSER = new FileChooser();

        FileChooser.ExtensionFilter extensionFilter =
            new FileChooser.ExtensionFilter(
                "IClicker Session Data files (*.xml)",
                "*.xml");
        XML_CHOOSER.getExtensionFilters().add(extensionFilter);

        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("Session Data File:");
        Button button = new Button("Choose");
        button.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);

        button.setOnAction(
            (final ActionEvent e) ->
            {
                sessionXML = XML_CHOOSER.showOpenDialog(stage);
                button.setText(sessionXML.toString());

                voteMapRowNode.setDisable(false);
                ignoredQuestionsRowNode.setDisable(false);
		/*
		*/
            });

        hbox.getChildren().addAll(text, button);

        return hbox;
    }

    /**
     * Make the row for opening the jar file.
     *
     * @param stage the main Stage
     * @return the HBox
     */
    private HBox makeExtractorFileRow(Stage stage)
    {
        final FileChooser JAR_CHOOSER = new FileChooser();

        FileChooser.ExtensionFilter extensionFilter =
            new FileChooser.ExtensionFilter(
                "Java jar files (*.jar)",
                "*.jar");
        JAR_CHOOSER.getExtensionFilters().add(extensionFilter);

        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("Session Data Extractor File:");
        Button button = new Button("Choose");
        button.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);

        button.setOnAction(
            (final ActionEvent e) ->
            {
                extractorJAR = JAR_CHOOSER.showOpenDialog(stage);

		// make sure jdom.jar in same location
		String fileJDOM = extractorJAR.toString();
		int chIndex = -1;
		if (opSys.equals("mac"))
		    chIndex = fileJDOM.lastIndexOf('/');
		else if (opSys.equals("win"))
		    chIndex = fileJDOM.lastIndexOf('\\');
		if (chIndex == -1) {
		    System.err.println("jdom base:"+fileJDOM);
		}
		fileJDOM = fileJDOM.substring(0,chIndex);
		if (opSys.equals("mac"))
		    fileJDOM = fileJDOM + "/jdom.jar";
		else if (opSys.equals("win"))
		    fileJDOM = fileJDOM + "\\jdom.jar";

		File jdomFile = new File(fileJDOM);
		if (jdomFile.exists()) {
		    button.setText(extractorJAR.toString());
		    okayToExtract = true;
		}
		else {
		    button.setText("CAN'T FIND JDOM.JAR FILE...");
		    okayToExtract = false;
		    throw new RuntimeException("CAN'T FIND JDOM.JAR FILE. Should be with SessionDataExtractor.jar file.");
		}

                voteMapRowNode.setDisable(false);
                ignoredQuestionsRowNode.setDisable(false);
		/*
		*/

		if (okayToExtract) 
		    exportButtonRowNode.setDisable(false);
            });

        hbox.getChildren().addAll(text, button);

        return hbox;
    }

    /**
     * Make the row for the votemap table.
     *
     * @return the HBox row
     */

    private HBox makeVoteMapRow()
    {
        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("ClickerButton-to-Vote Map:");

        TableView<VoteMapping> voteMapTable = makeVoteMapTableView();
        hbox.getChildren().addAll(text, voteMapTable);

        return hbox;
    }
    /*
    */

    /**
     * Make the table for makeVoteMapRow() to use.
     *
     * @return the TableView of VoteMapping's
     */

    private TableView<VoteMapping> makeVoteMapTableView()
    {
        TableView<VoteMapping> voteMapTable = new TableView<>();
        voteMapTable.setEditable(true);
        TableColumn<VoteMapping, String> buttonCol =
            new TableColumn<>("Button");
        buttonCol.setCellValueFactory(
            new PropertyValueFactory<>("button"));
        TableColumn<VoteMapping, String> mapsToCol =
            new TableColumn<>("Maps To");
        mapsToCol.setCellValueFactory(
            new PropertyValueFactory<>("mapsTo"));

        mapsToCol.setCellFactory(TextFieldTableCell.forTableColumn());
        mapsToCol.setOnEditCommit(
            (CellEditEvent<VoteMapping, String> t) ->
                t.getTableView().getItems().get(
                    t.getTablePosition().getRow()).setMapsTo(t.getNewValue())
        );

	voteMapTable.setItems(voteMappings);

        voteMapTable.getColumns().addAll(buttonCol, mapsToCol);

        voteMapTable.setFixedCellSize(VOTE_MAP_TABLE_CELL_SIZE);
        voteMapTable.setPrefHeight(VOTE_MAP_TABLE_CELL_SIZE
            * (voteMappings.size() + 1.1));
        return voteMapTable;
    }
    /*
    */

    /**
     * Make the Ignored Questions HBox.
     *
     * @return the HBox
     */
    private HBox makeIgnoredQuestionsRow()
    {

        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("Votes to Ignore:");
        TextField textField = new TextField();

        // make it red if it's invalid
        textField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!newValue.matches("(((\\d)+,?)*(\\d))?"))
            {
                textField.setStyle(
                    "-fx-control-inner-background: #" + "cd5c5cff");
            }
            else
            {
                textField.setStyle("");
            }
        });

        // fix it on lose focus if it's invalid
        textField.focusedProperty().addListener(
            (observable, oldValue, newValue) ->
            {
                if (!newValue)
                {
                    // textField is out focus
                    if (!textField.getText().matches("(((\\d)+,?)*(\\d))?"))
                    {
                        // it doesn't match the regex, so fix it
                        textField.setText(textField.getText().replaceAll(
                            "[^\\d|,]", ""));
                        textField.setText(textField.getText().replaceAll(
                            ",$", ""));
                        textField.setText(textField.getText().replaceAll(
                            ",,+", ","));

                    }
                    ignoredQuestions = textField.getText();
                }
            });

        hbox.getChildren().addAll(text, textField);

        return hbox;
    }

    /**
     * Make the Export button's row.
     *
     * @return The HBox of the Export button's row
     */
    private HBox makeExportButtonRow()
    {

        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Button button = new Button("Extract");
        button.setOnAction(
            (final ActionEvent e) ->
            {
		/*
                String[] args = new String[] {
                    Report.OPTION_IGNORED_QUESTIONS, ignoredQuestions,
                    Report.OPTION_VOTEMAP, getVoteMappingsString(),
                    Report.OPTION_EXPORT,
                    Report.OPTION_HIDE_IGNORED,
                    sessionXML.toString()};
		                String exportFeedback = Report.generate(args);
		*/
		String exportFeedback = gen(sessionXML.toString(), extractorJAR.toString(), ignoredQuestions);
                exportedFileButton.setText(exportFeedback);
                exportedFilesRowNode.setVisible(true);
            });

        hbox.getChildren().addAll(button);

        return hbox;
    }

    /**
     * Takes the command line command and does the actual report generation.
     *
     * @param args the command line options and input filename
     * @return
     */
    public static String gen(String xmlFilename, String jarFilename, String ignoredQs) {
        String[] command = null;
	if (ignoredQs == null || ignoredQs.length() == 0) {
	    String[] c = {"java", "-jar", jarFilename, xmlFilename};
	    command = c;
	}
	else {
	    String[] c = {"java", "-jar", jarFilename, "-qignore", ignoredQs, xmlFilename};
	    command = c;
	}

	//	System.out.println("Command[3]="+command[3]);
        StringBuilder cmdReturn = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            try (InputStream inputStream = process.getInputStream()) {
		    int c;
		    while ((c = inputStream.read()) != -1) {
			cmdReturn.append((char) c);
		    }
		}
            System.out.println(cmdReturn.toString());
 
        } catch (IOException ex) {
	    ex.printStackTrace();
	    //            Logger.getLogger(ListFiles.class.getName())
	    //		.log(Level.SEVERE, null, ex);
        }
	return cmdReturn.toString();
    }

    /**
     * Make the Exported Files feedback row.
     *
     * @return The HBox of the exported files feedback row
     */
    private HBox makeExportedFilesRow()
    {

        HBox hbox = new HBox();

        hbox.setSpacing(HORIZONTAL_SPACING);
        Text text = new Text("Extraction Report:");
        exportedFileButton = new Button();
        exportedFileButton.setMnemonicParsing(false);
        exportedFileButton.setOnAction(
            (final ActionEvent ae) ->
            {
                try
                {
                    String filepath = exportedFileButton.getText();
                    File dir = new File("/");
                    if (opSys.equals("mac"))
                    {
                        String[] cmdArray = new String[] {
                            "open", "-R", filepath};
                        Runtime.getRuntime().exec(cmdArray, null, dir);
                    }
                    else if (opSys.equals("win"))
                    {
                        // TODO: check on windows
                        Runtime.getRuntime().exec(
                            "Explorer.exe \"" + filepath + "\"");
                    }
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            });

        exportedFileButton.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        hbox.getChildren().addAll(text, exportedFileButton);

        return hbox;
    }

    /**
     * Make a votemap string like A=Apple:B=Book:C=Cat:D=Dog:E=Egg,
     * from the GUI's table to be passed to the CLI.
     *
     * @return a formatted votemap string
     */

    private String getVoteMappingsString()
    {
        StringBuilder vmString = new StringBuilder();
        for (VoteMapping vm : voteMappings)
        {
            vmString.append(vm.toString()).append(":");
        }
        vmString.deleteCharAt(vmString.length() - 1);
        return vmString.toString();
    }
    /*
    */

    /**
     * A class for the votemap table to use.
     */
    
    static class VoteMapping
    {
        private final SimpleStringProperty button;
        private final SimpleStringProperty mapsTo;

        /**
         * IClicker button is column one. The String it maps to is column two.
         *
         * @param button IClicker button (i.e A)
         * @param mapsTo The String it maps to (i.e Yes)
         */
        private VoteMapping(String button, String mapsTo)
        {
            this.button = new SimpleStringProperty(button);
            this.mapsTo = new SimpleStringProperty(mapsTo);

        }

        /**
         * Get the string that's stored in the button property.
         *
         * @return the string that's stored in the button property
         */
        String getButton()
        {
            return button.get();
        }

        /**
         * Get the string that's stored in the mapsTo property.
         *
         * @return the string that's stored in the mapsTo property
         */
        String getMapsTo()
        {
            return mapsTo.get();
        }

        /**
         * Set the string that's stored in the button property.
         *
         * @param button the string to be stored in the button property
         */
        void setButton(String button)
        {
            this.button.set(button);
        }

        /**
         * Set the string that's stored in the mapsTo property.
         *
         * @param mapsTo the string to be stored in the mapsTo property
         */
        void setMapsTo(String mapsTo)
        {
            this.mapsTo.set(mapsTo);
        }

        @Override
        public String toString()
        {
            return getButton() + "=" + getMapsTo();
        }
    }
}


