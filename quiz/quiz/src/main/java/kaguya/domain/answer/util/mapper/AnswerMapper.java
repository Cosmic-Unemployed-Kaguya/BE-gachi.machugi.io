package kaguya.domain.answer.util.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.answer.model.dto.request.AnswerReq;
import kaguya.domain.answer.model.dto.response.AnswerRes;
import kaguya.domain.answer.model.entity.AnswerEntity;

@Component
public class AnswerMapper {

    public AnswerEntity requestToEntity(Long questionIdx, AnswerReq request) {
        return AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer(request.answer())
                .build();
    }

    public AnswerRes entityToResponse(AnswerEntity answer) {
        return new AnswerRes(
                answer.getIdx(),
                answer.getQuestionIdx(),
                answer.getAnswer(),
                answer.getCreatedDate(),
                answer.getUpdatedDate()
        );
    }
}