/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.core.jquizweb;

import com.core.jquiz.IQuizItem;
import com.core.jquiz.OneSelectableOption;
import com.core.jquiz.Quiz;
import com.core.jquiz.SimpleQuizItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.primefaces.context.RequestContext;

/**
 * 
 * @author Gonzalo H. Mendoza
 * email: yogonza524@gmail.com
 * StackOverflow: http://stackoverflow.com/users/5079517/gonza
 */
@ManagedBean(name="quizBean")
@SessionScoped
public class QuizBean {

    private String step; //Window view
    private int totalTime; //Total time in minutes to test
    private int numberOfCorrects; //Number of correct answers to pass
    private int itemNumber;
    private Integer[] answersSelected;
    
    private boolean started;
    
    private Quiz quiz;
    
    @PostConstruct
    public void init(){
        totalTime = 2; //In minutes
        step = "init";
        numberOfCorrects = 3;
        itemNumber = 0; //Index 0 of question choice at begin
        
        createQuizExample(); //Create a simple quiz
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getStep() {
        return step;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getNumberOfCorrects() {
        return numberOfCorrects;
    }

    public void setNumberOfCorrects(int numberOfCorrects) {
        this.numberOfCorrects = numberOfCorrects;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Integer[] getAnswersSelected() {
        return answersSelected;
    }

    public void setAnswersSelected(Integer[] answersSelected) {
        this.answersSelected = answersSelected;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setStep(String step) {
        this.step = step;
        update("formQuiz");
    }
    
    private void update(String component){
        RequestContext.getCurrentInstance().update(component);
    }
    
    private void createQuizExample(){
         
         List<IQuizItem> items = new ArrayList<>();
         IQuizItem item1 = new SimpleQuizItem();
         IQuizItem item2 = new SimpleQuizItem();
         IQuizItem item3 = new SimpleQuizItem();
         IQuizItem item4 = new SimpleQuizItem();
         
         item1.setLeyend("How is the correct way to define a class in Java?");
         item1.setIndexAnswer(3);
         item1.addOption(new OneSelectableOption("public static void main(String[] args){...}"));
         item1.addOption(new OneSelectableOption("public enum MyClass {...}"));
         item1.addOption(new OneSelectableOption("public class a Class() {...}"));
         item1.addOption(new OneSelectableOption("class MyClass() {...}"));
         
         item2.setLeyend("Encapsulation helps to...");
         item2.setIndexAnswer(2);
         item2.addOption(new OneSelectableOption("Share behaviour between methods"));
         item2.addOption(new OneSelectableOption("Know who object can access to other object using messages"));
         item2.addOption(new OneSelectableOption("Hide the behaviour to prevent damages from external context"));
         item2.addOption(new OneSelectableOption("Access directly to attributes of objects"));
         
         item3.setLeyend("How many instances can have an Abstract class");
         item3.setIndexAnswer(3);
         item3.addOption(new OneSelectableOption("1 or more"));
         item3.addOption(new OneSelectableOption("only 1"));
         item3.addOption(new OneSelectableOption("2"));
         item3.addOption(new OneSelectableOption("0"));
         
         item4.setLeyend("A method defined two or more times with differents parameters is called");
         item4.setIndexAnswer(0);
         item4.addOption(new OneSelectableOption("Overloading"));
         item4.addOption(new OneSelectableOption("Polimorfism"));
         item4.addOption(new OneSelectableOption("Inheritance"));
         item4.addOption(new OneSelectableOption("Agregation"));
         
         items.add(item1);
         items.add(item2);
         items.add(item3);
         items.add(item4);
         
         answersSelected = new Integer[items.size()];
         
         for (int i = 0; i < answersSelected.length; i++) {
            answersSelected[i] = 0;
        }
         
         quiz = new Quiz(items,totalTime);
         quiz.passWith(numberOfCorrects);
    }
    
    public void beginQuiz(){
        if (quiz != null) {
            quiz.getItems().forEach(item -> {
                item.setIndexOfUserChoice(-1);
            });
            quiz.start(); //Start time
            started = true;
            new Thread(() -> {
                while(quiz.remainMillis() > 0){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(QuizBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                step = "stop";
            }).start();
        }
        setStep("running"); //Select window running and start to count time
    }
    
    public void nextQuestion(){
        itemNumber++;
        update("formQuiz");
    }
    
    public void backQuestion(){
        itemNumber--;
        update("formQuiz");
//        RequestContext.getCurrentInstance().execute("$('label-'" + itemNumber + "-" + answersSelected[itemNumber] + ").trigger('click');");
    }
    
    public void askFinish(){
        RequestContext.getCurrentInstance().execute(""
                + "swal({\n" +
                    "  title: \"Are you sure you want to finish Quiz?\",\n" +
                    "  text: \"You will not be able to revert this action!\",\n" +
                    "  type: \"warning\",\n" +
                    "  showCancelButton: true,\n" +
                    "  confirmButtonColor: \"#DD6B55\",\n" +
                    "  confirmButtonText: \"Yes, I want out\",\n" +
                    "  closeOnConfirm: false\n" +
                    "},\n" +
                    "function(){\n" +
                    "  finish();" +
                    "});");
    }
    
    public void finish(){
        stopQuiz();
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        } catch (IOException ex) {
            Logger.getLogger(QuizBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void select(String item, String optionSelected){
        Integer itemInteger = Integer.valueOf(item);
        Integer optionSelectedNumber = Integer.valueOf(optionSelected);
        
        this.answersSelected[itemInteger] = optionSelectedNumber;
        
        System.out.println("Item: " + item + ", respuesta: " + optionSelected);
        
        quiz.response(itemInteger, optionSelectedNumber);
        
        System.out.println("Quiz respuesta al item: " + quiz.getItems().get(itemInteger).getIndexOfUserChoice());
        
//        for (Iterator<IQuizItem> it = quiz.getItems().iterator(); it.hasNext();) {
//            IQuizItem itemQuiz = it.next();
//            System.out.println("Choice " + itemQuiz.getIndexOfUserChoice());
//            System.out.println("Correct " + itemQuiz.getIndexAnswer());
//        }
        System.out.println("-----------------------------------");
//        RequestContext.getCurrentInstance().execute("alert('" + item + ": " + optionSelected + "');");
    }
    
    public void select(int item, int optionSelected){
        
        this.answersSelected[item] = optionSelected;
        
        this.quiz.response(item, optionSelected);
//        System.out.println("Item: " + item + " and value " + answersSelected[item]);
    }
    
    public String isSelected(int questionNumber,int item, int optionSelected){
//        System.out.println("isSelected says: Item: " + item + " and option selected " + optionSelected);
//        System.out.println(ArrayUtils.toString(answersSelected));
//        System.out.println("--------------------------");
        return answersSelected != null && answersSelected[item].equals(optionSelected)? "checked" : "";
//        return quiz.getItems().get(item).getIndexOfUserChoice() == quiz.getItems().get(item).getIndexAnswer() ? "checked" : "";
    }
    
    public String remainTime(){
        return quiz.remainTime();
    }
    
    public boolean checkTime(){
        boolean hasTime = started && quiz.remainMillis() > 0;
        if (!hasTime) {
            finish();
        }
        return hasTime;
    }
    
    public void stopQuiz(){
        started = false;
        itemNumber = 0;
        for (int i = 0; i < answersSelected.length; i++) {
            answersSelected[i] = 0;
        }
        
        setStep("stop");
    }
}
