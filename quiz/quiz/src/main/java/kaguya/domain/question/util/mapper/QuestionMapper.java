package kaguya.domain.question.util.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.question.model.dto.request.QuestionReq;
import kaguya.domain.question.model.dto.response.QuestionRes;
import kaguya.domain.question.model.entity.QuestionEntity;

@Component
public class QuestionMapper {
    public QuestionEntity requestToEntity(Long quizIdx, QuestionReq request) {
        return QuestionEntity.builder()
                .quizIdx(quizIdx)
                .problemText(request.problemText())
                .problemUrl(request.problemUrl())
                .type(request.type())
                .sortOrder(request.sortOrder())
                .build();
    }

    public QuestionRes entityToResponse(QuestionEntity question) {
        return new QuestionRes(
                question.getIdx(),
                question.getQuizIdx(),
                question.getProblemText(),
                question.getProblemUrl(),
                question.getType(),
                question.getSortOrder(),
                question.getCreatedDate(),
                question.getUpdatedDate()
        );
    }
}
