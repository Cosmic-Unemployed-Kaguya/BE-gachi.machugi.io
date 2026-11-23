package kaguya.domain.quiz.model.dto.request;

public record QuizCreateReq(
    String title,
    String description,
    String thumbnail,
    String category,
    Long creatorId
) {
}
