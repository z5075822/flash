package com.example.vinguyen.assignmentprototype.Model;

public class Topic {
    private String topicID;
    private String title;
    private String QuestionDoc;
    private String Image;

    public Topic() {
    }

    public Topic(String topicID, String title, String QuestionDoc, String Image) {
        this.topicID = topicID;
        this.title = title;
        this.QuestionDoc = QuestionDoc;
        this.Image = Image;
    }

    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionDoc() {
        return QuestionDoc;
    }

    public void setQuestionDoc(String questionDoc) {
        QuestionDoc = questionDoc;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
