package kaguya.domain.question.model.dto.request;

public record QuestionReq(
        String problemText,
        String problemUrl,
        String type,
        Integer sortOrder
) {}