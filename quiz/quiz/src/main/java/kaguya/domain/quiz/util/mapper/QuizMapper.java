package kaguya.domain.quiz.util.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.quiz.model.dto.request.QuizCreateReq;
import kaguya.domain.quiz.model.dto.response.QuizRes;
import kaguya.domain.quiz.model.entity.QuizEntity;

@Component
public class QuizMapper {

    public QuizEntity requestToEntity(QuizCreateReq request) {
        return QuizEntity.builder()
                .title(request.title())
                .description(request.description())
                .thumbnail(request.thumbnail())
                .category(request.category())
                .creatorId(request.creatorId())
                .build();
    }

    public QuizRes entityToResponse(QuizEntity quizEntity) {
        return new QuizRes(
                quizEntity.getIdx(),
                quizEntity.getTitle(),
                quizEntity.getDescription(),
                quizEntity.getQuestionCount(),
                quizEntity.getThumbnail(),
                quizEntity.getCreatorId(),
                quizEntity.getCreatedDate(),
                quizEntity.getUpdatedDate(),
                quizEntity.getCategory()
        );
    }
}