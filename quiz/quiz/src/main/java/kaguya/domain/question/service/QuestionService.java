package kaguya.domain.question.service;

import java.util.List;

import kaguya.domain.question.model.dto.request.QuestionReq;
import kaguya.domain.question.model.dto.response.QuestionRes;

public interface QuestionService {

    // 문제 생성
    QuestionRes createQuestion(Long quizIdx, QuestionReq request);

    // 문제 전체 조회
    List<QuestionRes> getQuestionList(Long quizIdx);

    // 문제 단일 조회
    QuestionRes getQuestion(Long questionIdx);

    // 문제 수정
    QuestionRes patchQuestion(Long questionIdx, QuestionReq request);

    // 문제 삭제
    void deleteQuestion(Long questionIdx);
}