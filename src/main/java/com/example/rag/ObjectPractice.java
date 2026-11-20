package com.example.rag;

public class ObjectPractice {
    public static void main(String[] args) {
        Question first = new Question(1L, "도서관은 언제 열어요?", "안내문을 준비 중입니다.");
        Question second = new Question(2L, "동아리는 언제 신청해요?", "안내문을 준비 중입니다.");
        System.out.println(first.getId() + " " + first.getContent() + " " + first.getAnswer());
        System.out.println(second.getId() + " " + second.getContent() + " " + second.getAnswer());
    }
}
