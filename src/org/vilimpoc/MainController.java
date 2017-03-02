/**
    Copyright (c) 2017 Max Vilimpoc

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package org.vilimpoc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import net.sourceforge.plantuml.SourceStringReader;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Max
 */
public class MainController implements Initializable {

    // PlantUML syntax is defined in the LanguageDescriptor class:
    // https://raw.githubusercontent.com/plantuml/plantuml/master/src/net/sourceforge/plantuml/syntax/LanguageDescriptor.java
    
    protected static final String[] PUML_ATS = new String[] {
        "@startuml", "@enduml", "@startdot", "@enddot", "@startsalt", 
        "@endsalt"
    };

    protected static final String[] PUML_PREPROCS = new String[] {
        "!include", "!pragma", "!define", "!undef", "!ifdef", 
        "!endif", "!ifndef", "!else", "!definelong", "!enddefinelong"
    };
    
    protected static final String[] PUML_TYPES = new String[] {
        "actor", "participant", "usecase", "class", "interface", 
        "abstract", "enum", "component", "state", "object", 
        "artifact", "folder", "rectangle", "node", "frame", "cloud", 
        "database", "storage", "agent", "boundary", "control", "entity", 
        "card", "file", "package", "queue"
    };
    
    protected static final String[] PUML_KEYWORDS = new String[] {
        "as", "also", "autonumber", "caption", "title", 
        "newpage", "box", "alt", "else", "opt", "loop", "par", "break", 
        "critical", "note", "rnote", "hnote", "legend", "group", "left", 
        "right", "of", "on", "link", "over", "end", "activate", "deactivate", 
        "destroy", "create", "footbox", "hide", "show", "skinparam", "skin", 
        "top", "bottom", "top to bottom direction", "package", "namespace", 
        "page", "up", "down", "if", "else", "elseif", "endif", "partition", 
        "footer", "header", "center", "rotate", "ref", "return", "is", 
        "repeat", "start", "stop", "while", "endwhile", "fork", "again", 
        "kill"
    };

    private static final String ATS_PATTERN        = "("    + String.join("|", PUML_ATS) + ")\\b";
    private static final String PREPROC_PATTERN    = "("    + String.join("|", PUML_PREPROCS) + ")\\b";
    private static final String TYPES_PATTERN      = "\\b(" + String.join("|", PUML_TYPES)       + ")\\b";
    private static final String KEYWORD_PATTERN    = "\\b(" + String.join("|", PUML_KEYWORDS)    + ")\\b";
    
    private static final String PAREN_PATTERN      = "\\(|\\)";
    private static final String BRACE_PATTERN      = "\\{|\\}";
    private static final String BRACKET_PATTERN    = "\\[|\\]";
    private static final String SEMICOLON_PATTERN  = "\\;";
    private static final String STRING_PATTERN     = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN    = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
               "(?<AT>"        + ATS_PATTERN        + ")"
            + "|(?<PREPROC>"   + PREPROC_PATTERN    + ")"
            + "|(?<TYPE>"      + TYPES_PATTERN      + ")"
            + "|(?<KEYWORD>"   + KEYWORD_PATTERN    + ")"

            + "|(?<PAREN>"     + PAREN_PATTERN      + ")"
            + "|(?<BRACE>"     + BRACE_PATTERN      + ")"
            + "|(?<BRACKET>"   + BRACKET_PATTERN    + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN  + ")"
            + "|(?<STRING>"    + STRING_PATTERN     + ")"
            + "|(?<COMMENT>"   + COMMENT_PATTERN    + ")"
    );

    private static final String sampleCode = String.join("\n", new String[] {
        "!define",
        "",
        "@startuml",
        "",
        "actor",
        "",
        "Alice -> Bob: Authentication Request",
        "Bob --> Alice: Authentication Response",
        "",
        "Alice -> Bob: Another authentication Request",
        "Alice <-- Bob: another authentication Response",
        "@enduml"
    });

    private       CodeArea        codeArea;
    public static ExecutorService executor;

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("AT")         != null ? "at"        :
                    matcher.group("PREPROC")    != null ? "preproc"   :
                    matcher.group("TYPE")       != null ? "type"      :
                    matcher.group("KEYWORD")    != null ? "keyword"   :
                    
                    matcher.group("PAREN")      != null ? "paren"     :
                    matcher.group("BRACE")      != null ? "brace"     :
                    matcher.group("BRACKET")    != null ? "bracket"   :
                    matcher.group("SEMICOLON")  != null ? "semicolon" :
                    matcher.group("STRING")     != null ? "string"    :
                    matcher.group("COMMENT")    != null ? "comment"   :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    // Absolute file paths are unique.
    protected final ObservableList<String> filenames = FXCollections.observableArrayList();
    
    @FXML
    protected ListView<String> documentListView;
    
    @FXML
    protected StackPane codeAreaPane;
    
    @FXML
    protected StackPane imagePane;
    
    @FXML
    protected ImageView imageView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        documentListView.setItems(filenames);
        
        // Attach the CodeArea.
        executor = Executors.newSingleThreadExecutor();
        
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.richChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);
        codeArea.replaceText(0, 0, sampleCode);

//        Scene scene = new Scene(new StackPane(new VirtualizedScrollPane<>(codeArea)), 600, 400);
//        scene.getStylesheets().add(FabrikUml.class.getResource("java-keywords.css").toExternalForm());

//        codeArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        codeAreaPane.getChildren().add(codeArea);
//        codeAreaPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // codeAreaPane.resize(600, 400);
        codeAreaPane.getStylesheets().add(FabrikUml.class.getResource("plantuml-keywords.css").toExternalForm());
        
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Java Keywords Async Demo");
//        primaryStage.show();
    }
    
    @FXML
    protected void handleNewAction(ActionEvent e) {
    }
    
    @FXML
    protected void handleDragOver(DragEvent e) {
        if (e.getGestureSource() != documentListView) {
            e.acceptTransferModes(TransferMode.ANY);
        }
        
        e.consume();
    }
    
    @FXML
    protected void handleDragDropped(DragEvent e) {
        Dragboard b = e.getDragboard();
        boolean success = false;

        if (b.hasFiles()) {
            Logger.getGlobal().warning("Files dropped!");
            
            success = true;
            for (File f : b.getFiles()) {
                Logger.getGlobal().warning(f.getAbsolutePath());
                
                if (filenames.contains(f.getAbsolutePath())) {
                    Logger.getGlobal().warning("Don't add duplicate path.");
                }
                else {
                    filenames.add(f.getAbsolutePath());
                }
            }
            
            openFile(filenames.get(filenames.size() - 1));
        }
        
        e.setDropCompleted(success);
        e.consume();
    }
    
    @FXML
    protected void handleMouseClicked(MouseEvent e) {
        String filename = documentListView.getSelectionModel().getSelectedItem();
        Logger.getGlobal().warning(filename);
        
        openFile(filename);
    }

    protected void openFile(String filename) {
        try {
            // Open file in editor.
            String data = new String(Files.readAllBytes(Paths.get(filename)));

//            codeArea.clear();
            codeArea.replaceText(data);
            // applyHighlighting(computeHighlighting(data));
            
            // Go ahead and generate an Image to display.
            generatePng(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void generatePng(String uml) throws IOException
    {
        ByteArrayOutputStream png = new ByteArrayOutputStream(1000000);
        SourceStringReader reader = new SourceStringReader(uml);

        // Write the first image to "png"
        String desc = reader.generateImage(png);
        
        Logger.getGlobal().warning(desc);
        
        InputStream pngLoad = new ByteArrayInputStream(png.toByteArray());
        
        Image diagram = new Image(pngLoad);
        // imagePane.set
        // imagePane.getBackground().getImages().add(new BackgroundImage(diagram, null, null, null, null));
        
        imageView.setImage(diagram);
        
        // Return a null string if no generation.
    }

}
