package kaguya.domain.quiz.model.dto.request;

public record QuizUpdateReq (

    String title,
    String description,
    String thumbnail,
    String category
){}
