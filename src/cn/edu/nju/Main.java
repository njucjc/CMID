package cn.edu.nju;

//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;

import cn.edu.nju.builder.CheckerBuilder;

public class Main  {

//    @Override
//    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();
//    }


    public static void main(String[] args) {
//        launch(args);
        if (args.length == 1) {
            CheckerBuilder checkerParser = new CheckerBuilder(args[0]);
            checkerParser.run();
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }
}
