package application;

import java.net.URL;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainController implements Initializable{
	
	@FXML TextField txtNumBalls, txtMinutes;
	@FXML TextArea txtJson;
	@FXML Label lblCycle, lblError;
	@FXML Button btnExecute;
	@FXML ProgressIndicator indicator;
	@FXML ImageView process;
	Task task;//thread
	
	public static int twelveCount=0, minuteCount=0, dayCount=0, numBalls = 0, numberMinutesToRun=-1;
	static String originalOrder ="",originalId="";

	public static Stack<String> minuteStack;
	public static Stack<String> fiveStack;
	public static Stack<String> hourStack;
	public static Queue<String> ballQueue;
	
	
	
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.setImplicitExit(false);
		indicator.setVisible(false);
		lblCycle.setText("");
		lblError.setVisible(false);
		
		txtNumBalls.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	txtNumBalls.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		txtMinutes.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	txtMinutes.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		Image img =new Image("file:src/res/processIndicator.gif");
		process.setImage(img);
		process.setVisible(false);
				    
	}//end initialize

	
	
	public void onExecute() {
		
		minuteStack = new Stack<String>();//minutes stack
		fiveStack = new Stack<String>();//5 min stack
		hourStack = new Stack<String>();//hour stack
		ballQueue = new LinkedList<String>();
		txtJson.clear();
		lblCycle.setText("");
		minuteCount = 0;
		dayCount=0;
		twelveCount=0;
		
		
		if(!txtNumBalls.getText().trim().isEmpty())
			numBalls = Integer.parseInt(txtNumBalls.getText());

		if(!txtMinutes.getText().trim().isEmpty())
			numberMinutesToRun = Integer.parseInt(txtMinutes.getText());
		else
			numberMinutesToRun = -1;
		
		hourStack.push("0"); //this is the fixed ball that will always remain in the hour stack.
		loadQueue(numBalls);
		
		if(numBalls < 27 || numBalls > 127) {
			lblError.setVisible(true);
			return;
		}else {
			lblError.setVisible(false);
		}
		
		if(numberMinutesToRun != -1) {
		
		task = new Task<Void>() {
			
			//call() is the first thing run in a Task
		    @Override public Void call() throws Exception{
		    	indicator.setVisible(true);//makes progress bar visible.
		    	
		    	
		    	
		    	for(int i = 0; i < numberMinutesToRun; i++) {
		    		
		    		executeMinute();
		    		
		    		updateProgress(i, numberMinutesToRun);
		    		
		    		
		    		if (isCancelled()) {
						indicator.setVisible(false);
						return null;
			        }
		    		
		    		
		    	}

		    	
		    	txtJson.setText(printJson());
		    	
		    	
		    	indicator.setVisible(false);

		    	 return null;//Task requires some kind of return value, so give it a null
		    }
		};
		
		indicator.progressProperty().bind(task.progressProperty());//binds the progress bar with the task, so it will recieve results while the thread is running
		new Thread(task).start();//executes the thread/task
		
		
		}else {
			loadStacks();
			originalOrder = getCurrentOrder();
	
	    	task = new Task<Void>() {
				
				//call() is the first thing run in a Task
			    @Override public Void call() throws Exception{
			    	//indicator.setVisible(true);//makes progress bar visible.
			    	process.setVisible(true);
			    	
			    	
			    	
			    	
                	do {                          
                		executeMinute();
                		updateProgress(minuteCount, 100000000);
    		
			    		
			    	}while(!originalOrder.equals(getCurrentOrder()));

                	Platform.runLater(new Runnable() {
                        @Override public void run() {
                        	process.setVisible(false);
                        	lblCycle.setText(numBalls + " balls cycle after "+dayCount+ " days");
                        }
                    });


			    	 return null;//Task requires some kind of return value, so give it a null
			    }
			};
			
			new Thread(task).start();//executes the thread/task

		}//end if/else
		
		
		
		
	}//end on execute
	
	
	
	/**
	 * loadQueue will place id in the ballQueue. However many id's 
	 * depends on how many the user inputs as the first argument
	 * @param numberOfBalls
	 */
	public static void loadQueue(int numberOfBalls) {
		
		for(int i=1; i <= numberOfBalls; i++) {
			ballQueue.add(i+"");
		}
		
	}//end loadQueue
	
	/**
	 * loadStacks will load the hourStack with 11 id's from the ballQueue,
	 * load the fiveStack with 11 id's from the ballQueue,
	 * and load the minuteStack with 4 id's from the ballQueue.
	 * 
	 * This method will be used when determining hom many days before the balls return to the original positions.
	 */
	public static void loadStacks() {
		
		//hourStack.push(new Ball());
		
		while(hourStack.size() != 12) {
			hourStack.push(ballQueue.poll());

		}
		
		while(fiveStack.size() != 11) {
			fiveStack.push(ballQueue.poll());	
		}
		
		while(minuteStack.size()!= 4) {
			minuteStack.push(ballQueue.poll());
		}		

	}//endLoadStacks
	
	/**
	 * executeMinute will cycle 1 ball through the ball clock.
	 */
	public static void executeMinute() {
		minuteCount++; //minute count used for debugging purposes
		
		if(minuteStack.size() != 4) {
			minuteStack.push(ballQueue.poll());//if there is room in the minuteStack, add a ball from the queue.
			
		}else {
			//this code will execute iff the minuteStack was full
			
			for(int i=0; i<4;i++) {ballQueue.add(minuteStack.pop());}//pop all off the minute stack, add to ballQueue
		
			if(fiveStack.size() != 11) {
				fiveStack.push(ballQueue.poll());//if there is room in the Fives Stack, add ball from the queue.
			}else {
				
				//this code will execute if the fiveStack was full
				
				for(int j=0; j<11;j++) {ballQueue.add(fiveStack.pop());}//pop all off the fiveStack, and add to ballQueue
				
				if(hourStack.size() != 12) {
					hourStack.push(ballQueue.poll());
				}else {

					for(int k=0; k<11;k++) {ballQueue.add(hourStack.pop());}
					ballQueue.add(ballQueue.poll());
					
					twelveCount++;
					
					if(twelveCount == 2) {
						dayCount++;
						twelveCount =0;
					}
					
					
				}//end hourStack else
				
			}//end fiveStack else
			
		}//end if/else if
		
		//System.out.println(getTime());
		
	}//end executeMinute
	
	
	/**
	 * executeMinute will cycle however many time the user specified in the 2nd argument
	 */
	public static void executeMinute(int minutes) {
	
	while(minutes!=0) {
	
		if(minuteStack.size() != 4) {
			minuteStack.push(ballQueue.poll());//if there is room in the Minutes Stack, add a ball from the queue.
		}else {
			
			//if there is no room in the Minutes queue, pop the Minutes Stack, and add to queue, then poll the queue onto the Five Stack
			//for(int i=0; i<minuteStack.size();i++) {ballQueue.add(minuteStack.pop());}
			for(int i=0; i<4;i++) {ballQueue.add(minuteStack.pop());}
		
			if(fiveStack.size() != 11) {
				fiveStack.push(ballQueue.poll());//if there is room in the Fives Stack, add ball from the queue.
			}else {
				
				//if there is no room in the Minutes queue, pop the Fives Stack, and add to queue, then poll the queue onto the Five Stack
				//for(int j=0; j<fiveStack.size();j++) {ballQueue.add(fiveStack.pop());}
				for(int j=0; j<11;j++) {ballQueue.add(fiveStack.pop());}
				
				if(hourStack.size() != 12) {
					hourStack.push(ballQueue.poll());
				}else {
					
					//if there is no room in the Minutes queue, pop the Fives Stack, and add to queue, then poll the queue onto the Five Stack
					//for(int k=0; k<hourStack.size();k++) {ballQueue.add(hourStack.pop());}
					for(int k=0; k<11;k++) {ballQueue.add(hourStack.pop());}
					//hourStack.push(new Ball());
					ballQueue.add(ballQueue.poll());
					
					twelveCount++;
					
					if(twelveCount == 2) {
						dayCount++;
						twelveCount =0;
					}
					
				}
			}
			
		}
		
		minutes--;
		//System.out.println(getTime());
	}
		
	}//end executeMinute(int)
	
	
	/**
	 * returns the current time on the ball clock
	 * @return current time
	 */
	public static String getTime() {
		
		String hour = hourStack.size() +"";
		String minutes;
		
		
		if(fiveStack.size() ==1 || fiveStack.size() ==0) {
			minutes = "0"+minuteStack.size();
		}else
			minutes = ((fiveStack.size()*5) + minuteStack.size()) + "";
		
		
		
		return hour + ":" + minutes;
		
		
	}//end getTime()
	
	/**
	 * getCurrentOrder will return the current order of id's. hours+fives+minutes+queue.
	 * This method is used to capture the original order of the balls, and the compared when a cycle is performed.
	 * @return current order of id's/balls
	 */
	public static String getCurrentOrder() {
		String currentOrder="";	
		
		for(int i=1; i<hourStack.size();i++) {
			currentOrder+=hourStack.get(i);
		}
		
		if(!fiveStack.isEmpty()) {
			for(int i=0; i<fiveStack.size();i++) {
				
				currentOrder+=fiveStack.get(i);
			}
		}
		
		if(!minuteStack.isEmpty()) {
			for(int i=0; i<minuteStack.size();i++) {
				currentOrder+=minuteStack.get(i);
			}
		}
		
		ArrayList list = new ArrayList(ballQueue);
		for(int i=0;i<list.size();i++) {
			currentOrder+=list.get(i);
		}		
		
		return currentOrder;
	}


	/**
	 * printJson will print the current ball clock in a json format
	 * @return ball clock in json
	 */
	public static String printJson() {
		
		String hours = Arrays.toString(hourStack.toArray());
		
		//when we convert the hour stack to a string, it keeps the fixed ball with index 0. We need to remove that id.
		hours = hours.replaceFirst("0,", "");//if there are id's in the hour, remove  ' 0, '
		hours = hours.replaceFirst("0", "");//if there are no id's in the hour, only 0 will appear without the ','
		
		String json="{\"Min\":"+Arrays.toString(minuteStack.toArray())+
				",\"FiveMin\":"+Arrays.toString(fiveStack.toArray())+
				",\"Hour\":"+hours+
				",\"Main\""+Arrays.toString(ballQueue.toArray())+"}";
		json = json.replaceAll(" ", "");
		return json;
	}
	
	
	
	
	
	
	
	
	
		

}//end MainController

