package kaguya.domain.answer.service;

import java.util.List;

import kaguya.domain.answer.model.dto.request.AnswerReq;
import kaguya.domain.answer.model.dto.response.AnswerRes;

public interface AnswerService {

    AnswerRes createAnswer(Long questionIdx, AnswerReq request);

    List<AnswerRes> getAnswerList(Long questionIdx);

    AnswerRes patchAnswer(Long answerIdx, AnswerReq request);

    void deleteAnswer(Long answerIdx);
}