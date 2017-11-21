package application;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;




/*
 * This project was created for an interview coding assessment for Rakuten Marketing
 * Steven Brown 2017
 * https://github.com/StevenBrown17
 * https://www.linkedin.com/in/stevenbrown17/
 * 
 */






public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("MainFXML.fxml"));//loads the Tabs.fxml page as the first page you see on initial run
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
